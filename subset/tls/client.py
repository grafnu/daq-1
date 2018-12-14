import socket, ssl, pprint

hostname='127.0.0.1'
port=443

def get_certificate(host):

	print 'getting ssl cert for:',host

	sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

	SSLContext = ssl.create_default_context()
#	SSLContext = ssl.create_default_context(purpose=ssl.Purpose.SERVER_AUTH)
	
	if host==hostname:
		SSLContext.verify_mode=ssl.CERT_REQUIRED
		SSLContext.load_verify_locations(cafile='newExample/server.crt')

	conn = SSLContext.wrap_socket(sock,server_hostname=host)
	
	conn.connect((host,port))

	peername= conn.getpeername()

	cipher= conn.cipher()

	cert = conn.getpeercert()

	print 'cipher:\n',cipher
	print 'cert:\n',cert
	print 'getpeercert:'

	pprint.pprint(cert)

	conn.close()
	sock.close()
	#conn.sendall(b"HEAD / HTTP/1.0\r\nHost: mozilla.org\r\n\r\n")

	#pprint.pprint(conn.recv(1024).split(b"\r\n"))

#get_certificate('www.mozilla.org')
get_certificate('www.google.org')
get_certificate(hostname)
