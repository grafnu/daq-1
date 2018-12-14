import socket
import sys

arguments = sys.argv

print 'address:' + arguments[1]
print 'port:' + arguments[2]
print 'clients:' + arguments[3]

#address = '0.0.0.0'
address = str(arguments[1])
#port = 10000
port = int(arguments[2])
dataLength =64
#simultaneousClients=1
simultaneousClients= int(arguments[3])
username='manager'
password='friend'
requestMsg=['OFS1 login:','Password:','Last login:','Login incorrect','Connection closed by foreign host.']
flag=0
enabled=1
triesCount=0

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_address = (address,port)

sock.bind(server_address)

sock.listen(simultaneousClients)

while enabled:
	connection, client_address = sock.accept()
	try:
		connection.sendall(requestMsg[0])

		while True:
			data = connection.recv(dataLength)
			print data
			if data:
				if flag==0:
					user = data
					connection.sendall(requestMsg[1])
					flag=1
				elif flag==1:
					salt=data
					if username==user and password==salt:
						connection.sendall(requestMsg[2])
						flag=2
					else:
						if triesCount ==2:
							connection.sendall(requestMsg[4])
							break
						else:
							connection.sendall(requestMsg[3])
							triesCount = triesCount + 1
							connection.sendall(requestMsg[0])
							flag=0
			else:
				flag=0
				break
	finally:
		enabled=0
		connection.close()
