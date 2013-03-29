'''
from Crypto import Random
from Crypto.Hash import SHA256
from Crypto.PublicKey import RSA
'''

import json
import webapp2
import base64
from datetime import datetime

from cgi import escape
from struct import pack
from struct import unpack

from entities import User
from entities import RawUser
from entities import Message
from entities import RawMessage

from google.appengine.ext import ndb
from google.appengine.api import memcache
from google.appengine.api import taskqueue

def as_user(dct):
    raw = RawUser()
    raw.parse(dct)
    return raw

def as_dct(dct):
    return dct

def print_user(key, usr):
    dct = { 'lookup_key': key, 'public_key': usr.public_key, 'hash':usr.hash }
    return json.dumps(dct)

class AuthHandler(webapp2.RequestHandler):
    def get(self):
        pem = open('key.pub', 'r')
        publicKey = pem.read()
        pem.close()

        self.response.headers['Content-Type'] = 'text/plain; charset=utf-8'
        self.response.write(publicKey)

    def post(self):
        self.response.headers['Content-Type'] = 'text/plain; charset=utf-8'
        user = User()
        try:
            jsonUser = json.loads(self.request.body)  
            user.parse(jsonUser)
            if user.isValid():
                if user.isUnique():
                    user.created = datetime.today()
                    user.save()  # Save user in db!
                    self.response.write('You are created!\n')
                else:
                    self.response.write('We know you already.\n')
                    # Trying to add notification ids.
                    loaded_user = user.loadme()
                    loaded_user_changed = False
                    for reg_id in user.registration_ids:
                        if reg_id not in loaded_user.registration_ids:
                            loaded_user.registration_ids.append(reg_id)
                            loaded_user_changed = True
                    if loaded_user_changed:
                        loaded_user.save()
                        self.response.write('We updated your person.\n')
                self.response.write('Success!\n')
            else:
                self.response.write('Failed!\n')
                self.response.write('User not valid.\n')
                self.response.write(user.hash)
                self.response.write(user.type)
        except Exception, e:
            self.response.write('Failed!\n')
            self.response.write(e)
          
class ContactsHandler(webapp2.RequestHandler):
    def post(self):
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        
        counter = 0
        userlist = json.loads(self.request.body, object_hook=as_user)        
        
        self.response.write('[')
        for u in userlist:
            for p in u.phones:
                usr = User.query(User.hash == p).get()
                if usr is not None:
                    if counter > 0:
                        self.response.write(',')
                    self.response.write(print_user(u.lookup_key, usr))
                    counter += 1
        
        self.response.write(']')
    
class ContactHandler(webapp2.RequestHandler):
    def get(self, phoneHash):
        self.response.write('Requested phone hash is :\"'+ escape(phoneHash) +'\"\n')
        user = User.query(User.hash == escape(phoneHash)).get()
        
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.response.write(user.to_json())

class MessageGetHandler(webapp2.RequestHandler):
    def post(self):
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        body = json.loads(self.request.body, object_hook=as_dct)
        
        '''
        # Should always be the first all
        {
            "me": "y2wpfWBH5AoEgjxyw2sPVgOb2+VG9Dl/aVsY9NyQPH4=",
            "type": "ALL_CONTACT",
            "contact": "y2wpfWBH5AoEgjxyw2sPVgOb2+VG9Dl/aVsY9NyQPH4=",
            "id_only": true
        }
        
        # Should be the second call, only with unknown ids
        # This Request will always be cached:
        {
            "me": "y2wpfWBH5AoEgjxyw2sPVgOb2+VG9Dl/aVsY9NyQPH4=",
            "type": "IDS",
            "id": [1, 2, 3, 4]
        }
        '''
        me = body["me"]
        msgType = body["type"]
        if msgType == 'IDS':
            msg_result = self.load_by_ids(body["id"])
        else:
            msg_result = self.load_by_sender(body["contact"], me, msgType)
        
        counter = 0
        self.response.write('[')
        for msg in msg_result:
            if msg is None:
                continue            
            msg_json = json.loads(msg)
            
            # Skip message not for or from me
            if msg_json["sender"] != me and msg_json["recipient"] != me:
                continue
            
            # Add delimiter to output message
            if counter > 0:
                self.response.write(',')
            
            # Dump Message as JSON-Object!
            if "id_only" in body and body["id_only"]:
                self.response.write(msg_json["key"])
            else:
                self.response.write(msg)
            
            # Increment counter 
            counter += 1
        self.response.write(']')
        # TODO:
        # !!ADD SECURITY!!
        # USE SERVER PUBLIC KEY!
    def load_by_ids(self, ids):
        data = memcache.get_multi([str(id) for id in ids],namespace='app.arcanum.backend.messages').viewvalues()
        #data = [memcache.get(str(id)) for id in ids]
        return data
    
    def load_by_sender(self, sender, recipient, msgType):
        if msgType == 'ALL':
            msg_query = Message.query(ndb.OR(
                Message.sender == recipient,
                Message.recipient == recipient
            ))
        elif msgType == 'ALL_CONTACT':
            msg_query = Message.query(ndb.AND(
                ndb.OR(
                    Message.sender == sender,
                    Message.sender == recipient
                ),
                ndb.OR(
                    Message.recipient == sender,
                    Message.recipient == recipient     
                )
            ))
        elif msgType == 'UNREAD':
            msg_query = Message.query(ndb.AND(
                ndb.OR(
                    Message.sender == recipient,
                    Message.recipient == recipient
                ),
                Message.readed == None
            ))
        elif msgType == 'UNREAD_CONTACT':
            msg_query = Message.query(ndb.AND(
                ndb.OR(
                    Message.sender == sender,
                    Message.sender == recipient
                ),
                ndb.OR(
                    Message.recipient == sender,
                    Message.recipient == recipient     
                ),
                Message.readed == None
            ))
            
        messages = msg_query.fetch(limit=5)
        msg_result = []
        for msg in messages:
            # Mark message as readed!
            msg.readed = datetime.today()
            msg.put()
            
            # Save message in memcache
            key = str(msg.key.integer_id())
            msg_json = self.get_message_as_json(msg)
            memcache.set(key,msg_json,namespace='app.arcanum.backend.messages')
            
            # Add message to result
            msg_result.append(msg_json)
            
        return msg_result
    
    def get_message_as_json(self, msg):
        return '{' + \
            '"key":' + str(msg.key.integer_id()) + ',' + \
            '"version":' + str(msg.version) + ',' + \
            '"sender":"' + msg.sender + '",' + \
            '"recipient":"' + msg.recipient + '",' + \
            '"content_type":"' + msg.contentType + '",' + \
            '"content":"' + base64.standard_b64encode(msg.content) + '",' + \
            '"timestamp":"' + msg.pushed.strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + msg.pushed.strftime("%Z") + '"' + \
            '}'
        '''
        self.response.write('{')
        self.response.write('"key":' + str(msg.key.integer_id()) + ',')
        self.response.write('"version":' + str(msg.version) + ',')
        self.response.write('"sender":"' + msg.sender + '",')
        self.response.write('"recipient":"' + msg.recipient + '",')
        self.response.write('"content_type":"' + msg.contentType + '",')
        self.response.write('"content":"' + base64.standard_b64encode(msg.content) + '",')
        self.response.write('"timestamp":"' + msg.pushed.strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + msg.pushed.strftime("%Z") + '"')
        self.response.write('}')
        '''
        
class MessageSendHandler(webapp2.RequestHandler):
    def post(self):
        msg = self.request.body
        self.response.headers['Content-Type'] = 'text/html; charset=utf-8'
        self.response.write('<p>Message posted:</p>\n')
        self.response.write('<p>' + escape(msg) + '</p>\n')

        raw = RawMessage()
        raw.content = base64.standard_b64decode(msg)
        raw.put()
        self.response.write('<p>Saved raw message in datastore.</p>\n')
        
        msg_version = unpack(">I", raw.content[0:4])[0]
        self.response.write('<p>Message version is ' + str(msg_version) + '</p>\n')
        
        if msg_version == 1:
            message = Message()
            #message.raw_key     = raw.key
            message.version     = msg_version
            message.sender      = base64.standard_b64encode(raw.content[4:4+32])
            message.recipient   = base64.standard_b64encode(raw.content[36:36+32])
            #message.iv          = raw.content[68:68+16]
            #message.secretkey   = raw.content[84:84+32]
            #message.content_length = raw.content[116:116+4]
            message.content     = raw.content
            #message.contentType = 'TEXT'
            message.created     = datetime.today()
            msg_key = message.put()
            
            self.response.write('<p>Datastore key: ' + str(msg_key) + '</p>\n')
            self.response.write('<p>Datastore key_urlsafe: ' + str(msg_key.urlsafe()) + '</p>\n')
            self.response.write('<p>Datastore key_integer: ' + str(msg_key.integer_id()) + '</p>\n')
            taskqueue.add(queue_name='notifications', 
                          url='/task/msg', 
                          params={
                              'key':msg_key.urlsafe(),
                              'id':msg_key.integer_id()
                          })
        else:
            self.error(404)
        # ...
        
        # Load public Key from User
        
        # Verify message
        # !Attention!: this function performs the plain, primitive RSA encryption (textbook). In real applications, you always need to use proper cryptographic padding, and you should not directly verify data with this method. Failure to do so may lead to security vulnerabilities. It is recommended to use modules Crypto.Signature.PKCS1_PSS or Crypto.Signature.PKCS1_v1_5 instead.
        # https://www.dlitz.net/software/pycrypto/api/current/Crypto.PublicKey.RSA._RSAobj-class.html#verify

        # Send to receiving client
        
app = webapp2.WSGIApplication([
    (r'/api/auth', AuthHandler),
    (r'/api/contacts', ContactsHandler),
    (r'/api/contact/(\w+)', ContactHandler),
    (r'/api/msg/send', MessageSendHandler),
    (r'/api/msg/get', MessageGetHandler)
], debug=True)

