from google.appengine.ext import db
import json

#
# Class to enable json serialization
class DictModel(db.Model):
    def to_dict(self):
        return dict([(p, unicode(getattr(self, p))) for p in self.properties()])
    def to_json(self):
        return json.dumps(self.to_dict())

class RawUser(DictModel):
    lookup_key  = db.StringProperty()
    phones      = db.StringListProperty()
    
    def parse(self, dct):
        self.lookup_key = dct.get("lookup_key")
        self.phones     = dct.get("phones")

class User(DictModel):
    phoneHash = db.StringProperty()                 # Hashed Phone Number
    phoneType = db.StringProperty()                 # Phone type (Android, iOS, Win8, ...)
    publicKey = db.StringProperty(multiline=True)   # Public Key (PEM-Format)
    created = db.DateTimeProperty(auto_now_add=True)
    modified = db.DateTimeProperty(auto_now=True)

    def parse(self, dictionary):
        self.phoneHash = dictionary.get("phoneHash")
        self.publicKey = dictionary.get("publicKey")
        self.phoneType = dictionary.get("phoneType")
        self.created = dictionary.get("created")
        self.modified = dictionary.get("modified")

    def isValid(self):
        if self.phoneHash is None:
            return False
        if self.phoneType is None:
            return False
        if self.publicKey is None:
            return False
        return True

    def isUnique(self):
        users = User.all()
        users.filter("phoneHash = ", self.phoneHash)
        users.filter("phoneType = ", self.phoneType)
        users.filter("publicKey = ", self.publicKey)

        isInDatastore = bool(users.count(limit=1))
        return not isInDatastore
    
class Message(DictModel):
    sender      = db.StringProperty()
    recipient   = db.StringProperty()
    iv          = db.BlobProperty()
    secretkey   = db.BlobProperty()
    content     = db.BlobProperty()
    contentType = db.StringProperty()

    def parse(self, dictionary):
        self.sender = dictionary.get("sender")
        self.recipient = dictionary.get("recipient")
        self.content = db.Blob(dictionary.get("content"))
        self.contentType = dictionary.get("contentType")

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
    
class RawMessage(db.Model):
    content = db.BlobProperty()