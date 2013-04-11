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
from datetime import datetime
from google.appengine.ext import ndb
from google.appengine.api import users
from google.appengine.api import memcache

import jinja2
import os
import webapp2


class UserPrefs(ndb.Model):
    userid = ndb.StringProperty()

class MainHandler(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/html'
        
        user = users.get_current_user()
        if user:
            userid    = user.user_id()
            userprefs = self.get_userPrefs(userid)
            
            displayname = user.nickname()
            url = users.create_logout_url(self.request.uri)
            url_linktext = 'Logout'
        else:
            displayname = 'Anonymous'
            url = users.create_login_url(self.request.uri)
            url_linktext = 'Login'
        
        template_values = {
            'displayname': displayname,
            'url': url,
            'url_linktext': url_linktext,
        }
        template = jinja_environment.get_template('templates/index.html')
        self.response.out.write(template.render(template_values))
        
    def get_userPrefs(self,userid):
        key = str(userid)
        userprefs = memcache.get(key,namespace='app.arcanum.backend.userprefs')
        if userprefs is not None:
            return userprefs
        else:
            q = UserPrefs.query(UserPrefs.userid == userid)
            userprefs = q.get()
            if userprefs is None:
                userprefs = UserPrefs()
                userprefs.userid = userid
                userprefs.put()
            memcache.add(key,userprefs,namespace='app.arcanum.backend.userprefs')

class SettingsHandler(webapp2.RequestHandler):
    def get(self):
        template_values = {}
        template = jinja_environment.get_template('templates/settings.html')
        self.response.out.write(template.render(template_values))
            
class CryptoHandler(webapp2.RequestHandler):
    def get(self, size):
        self.response.headers['Content-Type'] = 'text/plain; charset=utf-8'
        self.response.write('Generated RSA keys for an size of {0}.\n'.format(size))
        self.response.write(str(datetime.today()) + '\n')
        '''
        rng = Random.new().read
        keys = RSA.generate(int(size), rng)
        pub = keys.exportKey(format='PEM', passphrase='_ArCanuM!', pkcs=8)
        prv = keys.publickey().exportKey(format='PEM', passphrase=None, pkcs=1)
        
        self.response.write(pub);
        self.response.write('\n\n')        
        self.response.write(prv);
        '''
        
class WarmupHandler(webapp2.RequestHandler):
    def get(self):
        pass
        
jinja_environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__))
)
app = webapp2.WSGIApplication([
    (r'/', MainHandler),
    (r'/settings/', SettingsHandler),
    (r'/gen/crypto/(\d+)', CryptoHandler),
    (r'/_ah/warmup', WarmupHandler)
], debug=True)
