#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
'''
from Crypto import Random
from Crypto.Hash import SHA256
from Crypto.PublicKey import RSA
'''

import json
import webapp2
import base64

from cgi import escape
from struct import unpack

from entities import User
from entities import RawUser
from entities import Message
from entities import RawMessage

from google.appengine.api import taskqueue

    
class MainHandler(webapp2.RequestHandler):
    def get(self):
        self.response.write('<h1>Hello world!</h1>')      
        self.response.write('''
            <p>This is the main api for Arcanum.</p>
            <p>Following methods are available:</p>
            <p><i>TODO</i></p>
        ''')
        
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
                    user.put()  # Save user in db!
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
                        loaded_user.put()
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

def as_user(dct):
    raw = RawUser()
    raw.parse(dct)
    return raw
            
class ContactsHandler(webapp2.RequestHandler):
    def post(self):
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        
        counter = 0
        userlist = json.loads(self.request.body, object_hook=as_user)        
        
        self.response.write('[')
        for u in userlist:
            for p in u.phones:
                usr = User.all().filter('phoneHash=', p).get()
                if usr is not None:
                    if counter > 0:
                        self.response.write(',')
                    self.response.write(usr.to_json())
                    counter += 1
        
        self.response.write(']')

class ContactHandler(webapp2.RequestHandler):
    def get(self, phoneHash):
        self.response.write('Requested phone hash is :\"'+ escape(phoneHash) +'\"\n')
        users = User.all().filter("phoneHash = ", escape(phoneHash))
        
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.response.write(json.dumps([u.to_dict() for u in users.run(limit=1)]))       

class MessageHandler(webapp2.RequestHandler):
    def post(self):
        msg = self.request.body
        self.response.headers['Content-Type'] = 'text/html; charset=utf-8'
        self.response.write('<p>Message posted:</p>\n')
        self.response.write('<p>' + escape(msg) + '</p>\n')

        raw = RawMessage()
        raw.content = base64.standard_b64decode(msg)
        raw.put()
        self.response.write('<p>Saved raw message in datastore.</p>\n')
        
        
        # Split RawMessage to Message 
        msg_version = unpack(">I", raw.content[0:4])[0]
        self.response.write('<p>Message version is ' + str(msg_version) + '</p>\n')
        
        if msg_version == 1:
            message = Message()
            message.sender      = base64.standard_b64encode(raw.content[4:4+32])
            message.recipient   = base64.standard_b64encode(raw.content[36:36+32])
            message.iv          = raw.content[68:68+16]
            message.secretkey   = raw.content[84:84+32]
            #message.content_length = raw.content[116:116+4]
            message.content     = raw.content[120:]
            message.put()
            
            taskqueue.add(queue_name='notifications', 
                          url='/task/msg', 
                          params={'key': message.key()})
        else:
            self.error(404)
        # ...
        
        # Load public Key from User
        
        # Verify message
        # !Attention!: this function performs the plain, primitive RSA encryption (textbook). In real applications, you always need to use proper cryptographic padding, and you should not directly verify data with this method. Failure to do so may lead to security vulnerabilities. It is recommended to use modules Crypto.Signature.PKCS1_PSS or Crypto.Signature.PKCS1_v1_5 instead.
        # https://www.dlitz.net/software/pycrypto/api/current/Crypto.PublicKey.RSA._RSAobj-class.html#verify

        # Send to receiving client
        
class CryptoHandler(webapp2.RequestHandler):
    def get(self, size):
        self.response.headers['Content-Type'] = 'text/plain; charset=utf-8'
        self.response.write('Generated RSA keys for an size of {0}.\n'.format(size))
        '''
        rng = Random.new().read
        keys = RSA.generate(int(size), rng)
        pub = keys.exportKey(format='PEM', passphrase='_ArCanuM!', pkcs=8)
        prv = keys.publickey().exportKey(format='PEM', passphrase=None, pkcs=1)
        
        self.response.write(pub);
        self.response.write('\n\n')        
        self.response.write(prv);
        '''
app = webapp2.WSGIApplication([
    (r'/', MainHandler),
    (r'/auth', AuthHandler),
    (r'/contacts', ContactsHandler),
    (r'/contact/(\w+)', ContactHandler),
    (r'/msg', MessageHandler),
    (r'/gen/crypto/(\d+)', CryptoHandler)
], debug=True)
