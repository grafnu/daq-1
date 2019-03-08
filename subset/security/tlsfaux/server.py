import socket, ssl, pprint

hostname = '0.0.0.0'
port = 443

filename_cert = 'certs/server.crt'
filename_key = 'certs/server.key'

context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
context.load_cert_chain(certfile = filename_cert, keyfile = filename_key)

bind_socket = socket.socket()
bind_socket.bind((hostname,port))
bind_socket.listen(5)

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

def parse_data(connstream,data):
	print "parse_data:",data
	return false

def read_client_data(connstream):
	data = connstream.read()
	while data:
		if not parse_data(connstream,data):
			break
		data=connstream.read()

print "SSL Server started..."
while True:
	newsocket, fromaddr = bind_socket.accept()
	connstream = context.wrap_socket(newsocket,server_side=True)
	try:
		read_client_data(connstream)
	finally:
		connstream.shutdown(socket.SHUT_RDWR)
		connstream.close()
