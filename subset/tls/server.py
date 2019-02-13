import socket, ssl, pprint

hostname='0.0.0.0'
port=443

filenameCert='certs/server.crt'
filenameKey='certs/server.key'

context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
context.load_cert_chain(certfile=filenameCert, keyfile=filenameKey)

bindsocket = socket.socket()
bindsocket.bind((hostname,port))
bindsocket.listen(5)

#PROTOCOL_TLSv1
#PROTOCOL_TLSv1_1
#PROTOCOL_TLSv1_2
#PROTOCOL_TLS
#PROTOCOL_SSLv2
#PROTOCOL_SSLv3

#OP_ALL
#OP_NO_SSLv2
#OP_NO_SSLv3
#OP_NO_TLSv1
#OP_NO_TLSv1_1
#OP_NO_TLSv1_2
#OP_NO_TLSv1_3

def do_something(connstream,data):
	print "do_something:",data
	return false

def deal_with_client(connstream):
	data = connstream.read()
	while data:
		if not do_something(connstream,data):
			break
		data=connstream.read()

while True:
	newsocket, fromaddr = bindsocket.accept()
	connstream = context.wrap_socket(newsocket,server_side=True)
	try:
		deal_with_client(connstream)
	finally:
		connstream.shutdown(socket.SHUT_RDWR)
		connstream.close()
