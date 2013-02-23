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
    hash = db.StringProperty()                          # Hashed Phone Number
    type = db.StringProperty()                          # Phone type (Android, iOS, Win8, ...)
    public_key = db.TextProperty()                      # Public Key (PEM-Format)
    registration_ids = db.StringListProperty()          # IDs for Push Notifications
    created = db.DateTimeProperty(auto_now_add=True)
    modified = db.DateTimeProperty(auto_now=True)

    def parse(self, dictionary):
        self.hash = dictionary.get("hash")
        self.type = dictionary.get("type")
        self.public_key = dictionary.get("public_key")
        self.registration_ids = dictionary.get("registration_ids")
        self.created = dictionary.get("created")
        self.modified = dictionary.get("modified")

    def isValid(self):
        if self.hash is None:
            return False
        if self.type is None:
            return False
        if self.public_key is None:
            return False
        if self.registration_ids is None:
            return False
        return True

    def isUnique(self):
        users = User.all()
        users.filter("hash = ", self.hash)
        users.filter("type = ", self.type)

        isInDatastore = bool(users.count(limit=1))
        return not isInDatastore
    
    def loadme(self):
        users = User.all()
        users.filter("hash = ", self.hash)
        if self.type is not None:
            users.filter("type = ", self.type)
        return users.get()

class RawMessage(db.Model):
    content = db.BlobProperty()        
    
class Message(DictModel):
    sender      = db.StringProperty()
    recipient   = db.StringProperty()
    iv          = db.BlobProperty()
    secretkey   = db.BlobProperty()
    content     = db.BlobProperty()
    contentType = db.StringProperty()
    raw = db.Reference(reference_class=RawMessage)

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
    
