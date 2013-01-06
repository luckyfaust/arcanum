from google.appengine.ext import db
import json

#
# Class to enable json serialization
class DictModel(db.Model):
    def to_dict(self):
       return dict([(p, unicode(getattr(self, p))) for p in self.properties()])
    def to_json(self):
        return json.dumps(self.to_dict())
#
# Defines a user in database
#
# Help:
#   https://developers.google.com/appengine/docs/python/datastore/typesandpropertyclasses
class User(DictModel):
    phoneHash = db.StringProperty()                 # Hashed Phone Number
    phoneHashType = db.StringProperty()             # https://www.dlitz.net/software/pycrypto/api/current/Crypto.Hash-module.html
    publicKey = db.StringProperty(multiline=True)   # Public Key (PEM-Format)
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

class Message(DictModel):
    sender = db.StringProperty()        # Hashed Phone Number
    recipient = db.StringProperty()     # Hashed Phone Number
    content = db.BlobProperty()         # Encrypted content
    contentType = db.StringProperty()   # Type of encrypted content (Text, Image, Video, etc.)

    def parse(self, dict):
        self.sender = dict.get("sender")
        self.recipient = dict.get("recipient")
        self.content = db.Blob(dict.get("content"))
        self.contentType = dict.get("contentType")

    def isValid(self):
        if self.sender is None:
            return False
        if self.recipient is None:
            return False
        if self.content is None:
            return False
        if self.contentType is None:
            return False
        return True