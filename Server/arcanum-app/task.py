import webapp2
import logging

from gcm import GCM
from entities import User
from entities import Message

class TaskBaseWorker(webapp2.RequestHandler):
    def debug(self, msg):
        self.response.write(msg)
        logging.debug(msg)
    def warn(self, msg):       
        self.response.write(msg)
        logging.warn(msg)
    def err(self, msg):
        self.response.write(msg)
        logging.error(msg)        
        
class DefaultHandler():
    def get(self):
        self.response.headers['Content-Type'] = 'text/plain; charset=utf-8'
        self.response.write('Nothing')

class AdminHandler(webapp2.RequestHandler):
    def get(self):
        self.response.headers['Content-Type'] = 'text/html; charset=utf-8'
        self.response.write('<h1>Admin</h1>')
        

class MessageWorker(TaskBaseWorker):
    def post(self):
        eid = self.request.get('id', default_value='')
        key = self.request.get('key', default_value='')
        
        if eid is not None and eid != '':
            msg = Message.get_by_id(int(eid))
        elif key is not None and key != '':
            msg = Message.get(key)
        else:
            self.err('Neither id or key is passed!\n')
            
        if msg is None:
            self.err('Loading message failed!\n')
            return       
        
        # Send to phone notification!
        usr = User()
        usr.hash = msg.recipient
        usr = usr.loadme()
        
        data = {'type': 'message', 'sender': msg.sender}
        
        notfication = GCM('AIzaSyD-z2JuoBkD51RyRHG6ULoWX2gE84apP7M')
        response = notfication.json_request(registration_ids=usr.registration_ids, 
                         data=data,
                         collapse_key=None, 
                         delay_while_idle=False,
                         time_to_live=None,
                         retries=5)
        
        self.response.headers['Content-Type'] = 'application/json; charset=utf-8'
        self.debug(response)
        
app = webapp2.WSGIApplication([
    (r'/task/',      DefaultHandler),
    (r'/task/msg',   MessageWorker),
    (r'/task/admin/', AdminHandler)]
, debug=True)