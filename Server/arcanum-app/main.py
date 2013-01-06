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

from Crypto import Random
from Crypto.Hash import SHA
from Crypto.PublicKey import RSA

from cgi import escape
from google.appengine.ext import db

import base64
import json
import webapp2

import entities
User    = entities.User
Message = entities.Message

    
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
        pem = open('publicKey.pem', 'r')
        publicKey = pem.read()

        #pem = open('privKey.pem', 'r')
        #keys = RSA.importKey(pem.read(), passphrase='Arcanum')
        pem.close()

        #publicKey = keys.publickey().exportKey(format='PEM')
        self.response.headers['Content-Type'] = 'text/plain; charset=utf-8'
        self.response.write(publicKey)

        #self.response.write('\n')
        #if keys.has_private():
        #self.response.write('Loading private key: success.')
        #else:
        #    self.response.write('Loading private key: failed.')
    def post(self):
        self.response.headers['Content-Type'] = 'text/plain; charset=utf-8'
        user = User()
        try:
            jsonUser = json.loads(self.request.body)  
            user.parse(jsonUser)
            if user.isValid() and user.isUnique():
                user.put()  # Save user in db!
                self.response.write('Success!')
            else:
                self.response.write('Failed!\n')
                self.response.write('User not valid or not unique.\n')
        except Exception, e:
            self.response.write('Failed!\n')
            self.response.write(e)

class ContactsHandler(webapp2.RequestHandler):
    def get(self):
        users = User.all()
        
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.response.write(json.dumps([u.to_dict() for u in users]))        

class ContactHandler(webapp2.RequestHandler):
    def get(self, phoneHash):
        self.response.write('Requested phone hash is :\"'+ escape(phoneHash) +'\"\n')
        users = User.all().filter("phoneHash = ", escape(phoneHash))
        
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.response.write(json.dumps([u.to_dict() for u in users.run(limit=1)]))       

class MessageHandler(webapp2.RequestHandler):
    def post(self):
        message = self.request.body
        self.response.write('<p>Secret-Message</p>')
        self.response.write('<p>' + escape(message) + '</p>')

        # Load private Key
        #pem = open('privKey.pem', 'r')
        #keys = RSA.importKey(pem.read(), passphrase='Arcanum')
        #pem.close()

        # Verify message
        # !Attention!: this function performs the plain, primitive RSA encryption (textbook). In real applications, you always need to use proper cryptographic padding, and you should not directly verify data with this method. Failure to do so may lead to security vulnerabilities. It is recommended to use modules Crypto.Signature.PKCS1_PSS or Crypto.Signature.PKCS1_v1_5 instead.
        # https://www.dlitz.net/software/pycrypto/api/current/Crypto.PublicKey.RSA._RSAobj-class.html#verify


        # Decrypt message container to server
        # !Attention!: this function performs the plain, primitive RSA decryption (textbook). In real applications, you always need to use proper cryptographic padding, and you should not directly decrypt data with this method. Failure to do so may lead to security vulnerabilities. It is recommended to use modules Crypto.Cipher.PKCS1_OAEP or Crypto.Cipher.PKCS1_v1_5 instead.
        # https://www.dlitz.net/software/pycrypto/api/current/Crypto.PublicKey.RSA._RSAobj-class.html#decrypt
        #plainText = keys.decrypt(message)

        # Send to receiving client
        self.response.write('<p>Plain-Message</p>')
        #self.response.write('<p>' + escape(plainText) + '</p>')

class UserExampleHandler(webapp2.RequestHandler):
    def get(self):
        #h = SHA.new()
        #h.update(b'+49 123 1234567')
        #h.hexdigest()

        #rng = Random.new().read
        #keys = RSA.generate(2048, rng)
        #publicKey = keys.publickey().exportKey()
        
        user = User()
        user.id = 42
        #user.phoneHash = h.hexdigest()
        user.phoneHashType = 'SHA-1'
        #user.publicKey = publicKey

        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.response.write(json.dumps(user.to_dict()))
        
class MessageExampleHandler(webapp2.RequestHandler):
    def get(self):
        # Client Security
        #rng = Random.new().read
        #clientKeys = RSA.generate(2048, rng)
        
        # Server PublicKey
        #pem = open('privKey.pem', 'r')
        #serverKeys = RSA.importKey(pem.read(), passphrase='Arcanum')
        #pem.close()

        # The message
        msg = Message()
        msg.sender = '123'
        msg.recipient = '123'
        #msg.content = db.Blob(base64.encodestring(clientKeys.encrypt('Das ist eine Beispielnachricht', 16)[0]))
        msg.content = db.Blob(base64.encodestring(b'Das ist Text'))
        msg.contentType = 'Text'

        # Encrypt hole message with server public key
        #msg_sec = serverKeys.publickey().encrypt(msg.to_json(), 256)[0]
        
        # Output
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.response.write(msg.to_json())


app = webapp2.WSGIApplication([
    (r'/', MainHandler),
    (r'/auth', AuthHandler),
    (r'/contacts', ContactsHandler),
    (r'/contact/(\w+)', ContactHandler),
    (r'/msg', MessageHandler),
    (r'/authExample', UserExampleHandler),
    (r'/msgExample', MessageExampleHandler),
], debug=True)
