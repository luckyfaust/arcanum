#!/usr/bin/python

from Crypto import Random

from Crypto.Cipher import AES
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA

from Crypto.Protocol.KDF import PBKDF2

from Crypto.Hash import SHA256

class ArcanumCrypt:
    self.rsa_key = None

    def load(self, keyFile='key.prv'):
        f = open(keyfile, 'r')
        self.rsa_key = RSA.importKey(f.read(), passphrase='Arcanum')
        f.close()

    def hash(self, msg, readable=False):
        h = SHA256.new()
        h.update(msg)
        if readable:
            return h.hexdigest()
        else:
            return h.digest()

    def encrypt(self, message):
        # Init RSA with PKCS1_OAEP
        if self.rsa_key is None:
            self.load()       
        pkcs = PKCS1_OAEP.new(self.rsa_key, SHA256.new())

        # Init AES
        iv = generate_iv()
        pw = generate_pw('My secret password')

        aes_key = AES.new(pw, AES.MODE_CBC, iv)

        cipher = PKCS1_OAEP.new(key, SHA256.new())
        ciphertext = cipher.encrypt(message)
        return ciphertext
    
    def decrypt(self, ciphertext):
        key = self.loadRSA()
        cipher = PKCS1_OAP.new(key, SHA256.new())
        message = cipher.decrypt(ciphertext)
        return message

    # http://kfalck.net/2011/03/07/decoding-pkcs1-padding-in-python
    def pkcs1_unpad(text):
        if len(text) > 0 and text[0] == '\x02':
            # Find end of padding marked by nul
            pos = text.find('\x00')
            if pos > 0:
                return text[pos+1:]
        return None

    def generate_pw(password, iterations=5000, readable=False):
        salt = Random.new().read
        key = PBKDF2(password, salt, dkLen=32, count=iterations)
 
        print 'Random salt (in hex):'
        print salt.encode('hex')
        print 'PBKDF2-derived key (in hex) of password after %d iterations: ' % iterations
        print key.encode('hex')
        if readable:
            key.encode('hex')
        else:
            return key

    def generate_iv(length=16):
        return ''.join(chr(random.randint(0, 0xFF)) for i in range(length))

    def pad_data(data):
        # return data if no padding is required
        if len(data) % 16 == 0:
            return data
        # subtract one byte that should be the 0x80
        # if 0 bytes of padding are required, it means only
        # a single \x80 is required.
        padding_required = 15 - (len(data) % 16)
        data = '%s\x80' % data
        data = '%s%s' % (data, '\x00' * padding_required)
        return data

    def unpad_data(data):
        if not data:
            return data
        data = data.rstrip('\x00')
        if data[-1] == '\x80':
            return data[:-1]
        else:
            return data
