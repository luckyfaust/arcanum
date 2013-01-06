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

import json
import webapp2

#
# Class to enable json serialization
class DictModel(db.Model):
    def to_dict(self):
       return dict([(p, unicode(getattr(self, p))) for p in self.properties()])

#
# Defines a user in database
#
# Help:
#   https://developers.google.com/appengine/docs/python/datastore/typesandpropertyclasses
class User(DictModel):
    phoneHash = db.StringProperty()         # Hashed Phone Number
    phoneHashType = db.StringProperty()     # https://www.dlitz.net/software/pycrypto/api/current/Crypto.Hash-module.html
    publicKey = db.StringProperty()         # Public Key
    created = db.DateTimeProperty(auto_now_add=True)
    modified = db.DateTimeProperty(auto_now=True)

    def parse(self, dict):
        self.phoneHash = dict.get("phoneHash")
        self.phoneHashType = dict.get("phoneHashType")
        self.publicKey = dict.get("publicKey")
        self.created = dict.get("created")
        self.modified = dict.get("modified")

    def isValid(self):
        if self.phoneHash is None:
            return False
        if self.phoneHashType is None:
            return False
        if self.publicKey is None:
            return False
        return True

    def isUnique(self):
        users = User.all()
        users.filter("phoneHash = ", self.phoneHash)
        users.filter("phoneHashType = ", self.phoneHashType)
        users.filter("publicKey = ", self.publicKey)

        isInDatastore = bool(users.count(limit=1))
        return not isInDatastore

    
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
        #pem = open('publicKey.pem', 'r')
        #publicKey = pem.read()

        pem = open('privKey.pem', 'r')
        keys = RSA.importKey(pem.read(), passphrase='Arcanum')
        pem.close()

        publicKey = keys.publickey().exportKey(format='PEM')
        self.response.write(publicKey)

        self.response.write('\n<p>')
        if keys.has_private():
            self.response.write('Loading private key: <b>success</b>.')
        else:
            self.response.write('Loading private key: <b>failed</b>.')
        self.response.write('</p>')
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
    def get(self, id):
        self.response.write('<p>With id: '+ escape(id) +'</p>')

class MessageHandler(webapp2.RequestHandler):
    def post(self):
        message = self.request.body
        self.response.write('<p>Secret-Message</p>')
        self.response.write('<p>' + escape(message) + '</p>')

        # Load private Key
        pem = open('privKey.pem', 'r')
        keys = RSA.importKey(pem.read(), passphrase='Arcanum')
        pem.close()

        # Verify message
        # !Attention!: this function performs the plain, primitive RSA encryption (textbook). In real applications, you always need to use proper cryptographic padding, and you should not directly verify data with this method. Failure to do so may lead to security vulnerabilities. It is recommended to use modules Crypto.Signature.PKCS1_PSS or Crypto.Signature.PKCS1_v1_5 instead.
        # https://www.dlitz.net/software/pycrypto/api/current/Crypto.PublicKey.RSA._RSAobj-class.html#verify


        # Decrypt message container to server
        # !Attention!: this function performs the plain, primitive RSA decryption (textbook). In real applications, you always need to use proper cryptographic padding, and you should not directly decrypt data with this method. Failure to do so may lead to security vulnerabilities. It is recommended to use modules Crypto.Cipher.PKCS1_OAEP or Crypto.Cipher.PKCS1_v1_5 instead.
        # https://www.dlitz.net/software/pycrypto/api/current/Crypto.PublicKey.RSA._RSAobj-class.html#decrypt
        plainText = keys.decrypt(message)

        # Send to receiving client
        self.response.write('<p>Plain-Message</p>')
        self.response.write('<p>' + escape(plainText) + '</p>')

class UserExampleHandler(webapp2.RequestHandler):
    def get(self):
        h = SHA.new()
        h.update(b'+49 123 1234567')
        h.hexdigest()

        rng = Random.new().read
        keys = RSA.generate(2048, rng)
        publicKey = keys.publickey().exportKey()
        
        user = User()
        user.id = 42
        user.phoneHash = h.hexdigest()
        user.phoneHashType = 'SHA-1'
        user.publicKey = publicKey

        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.response.write(json.dumps(user.to_dict()))
        

app = webapp2.WSGIApplication([
    (r'/', MainHandler),
    (r'/auth', AuthHandler),
    (r'/contacts', ContactsHandler),
    (r'/contact/(\d+)', ContactHandler),
    (r'/msg', MessageHandler),
    (r'/userExample', UserExampleHandler),
], debug=True)
