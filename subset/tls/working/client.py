import socket, ssl, pprint

#hostname='www.mozilla.org'
hostname='localhost'
port=443

def get_certificate(host):
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	ssl_sock = ssl.wrap_socket(sock,
				ca_certs="server.crt",
				cert_reqs=ssl.CERT_REQUIRED)

	ssl_sock.connect((host,port))

	print repr(ssl_sock.getpeername())
	print ssl_sock.cipher()
	print pprint.pformat(ssl_sock.getpeercert())

	ssl_sock.write("test packet from client!")

	if False:

    		ssl_sock.write("""GET / HTTP/1.0r\nHost: www.verisign.comnn""")

    		data = ssl_sock.read()

		ssl_sock.close()

get_certificate(hostname)
