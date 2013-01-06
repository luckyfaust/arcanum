#!/usr/bin/python

from Crypto import Random
from Crypto.Cipher import AES
from Crypto.Hash import SHA256
from Crypto.Cipher import PKCS1_OAEP
from Crypto.PublicKey import RSA
from Crypto.Protocol.KDF import PBKDF2
from Crypto.Util import Counter

class ArcanumCrypt:
    def loadRSA(self, file='privKey.pem'):
        return RSA.importKey(self.loadPem(file), passphrase='Arcanum')

    def loadPem(self, file):
        pem = open(file, 'r')
        value = pem.read()
        pem.close()
        return value

    def hash(self, msg):
        h = SHA256.new()
        h.update(msg)
        print h.hexdigest()

    def encrypt(self, message):
        key = self.loadRSA()
        cipher = PKCS1_OAEP.new(key, SHA256.new())
        ciphertext = cipher.encrypt(message)
        return ciphertext
    
    def decrypt(self, ciphertext):
        key = self.loadRSA()
        cipher = PKCS1_OAP.new(key, SHA256.new())
        message = cipher.decrypt(ciphertext)
        return message

    def buildKey(self, password):
        iterations = 5000
        key = ''
        salt = Random.new().read
        key = PBKDF2(password, salt, dkLen=32, count=iterations)
 
        print 'Random salt (in hex):'
        print salt.encode('hex')
        print 'PBKDF2-derived key (in hex) of password after %d iterations: ' % iterations
        print key.encode('hex')
        return key.encode('hex')
