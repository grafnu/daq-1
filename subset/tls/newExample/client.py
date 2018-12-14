import socket, ssl, pprint

#hostname='www.mozilla.org'
hostname='localhost'
port=443

def get_certificate(host):

	print 'getting ssl cert for:',host

	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	#context = ssl.create_default_context()

	conn = ssl.wrap_socket(sock,
	#conn = context.wrap_socket(sock,
				ca_certs="server.crt",
				cert_reqs=ssl.CERT_REQUIRED)
				#server_hostname=host)

	conn.connect((host,port))

	peername= conn.getpeername()

	cipher= conn.cipher()
	
	cert = conn.getpeercert()

	print 'cipher:\n',cipher
	print 'cert:\n',cert
	print 'getpeercert:'

	pprint.pprint(cert)

	#conn.write("test packet from client!")

	#if False:

    	#	conn.write("""GET / HTTP/1.0r\nHost: www.verisign.comnn""")

    	#	data = conn.read()

	#	conn.close()

get_certificate(hostname)
