import socket
import sys
import json

userData=open('userList.json')
userList = json.load(userData)
userData.close()

username= userList['username'].split(",")
password= userList['password'].split(",")

arguments = sys.argv

print 'pytelnClient test begin'
print 'address:' + arguments[1]
print 'port:' + arguments[2]
print 'reportFile:' + arguments[3]

#address = '127.0.0.1'
address = str(arguments[1])
#port = 10000
port = int(arguments[2])
reportFile = str(arguments[3])
dataLength =64
#username=['root','admin','manager']
#password=['root','pass','friend']
expected=['login:','Password:','Last login:','Login incorrect','Connection closed by foreign host.']
expected1=['username:','password:','success','login failed','Connection closed by foreign host.']

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_address = (address,port)

sock.connect(server_address)

loginReport='\n'
u=0
p=0

try:
	while True:
		data = sock.recv(dataLength)
		if data:
			print data
			if data == expected[2]:
				loginReport = loginReport + 'Username:' + username[u-1] + '\n'
				loginReport = loginReport + 'Password:' + password[p-1] + '\n'
				file = open(reportFile, "w")
				file.write(loginReport)
				sock.close()
				break
			elif data[-6:] == expected[0]:
				sock.sendall(username[u])
				u+=1
				if len(username) < u:
					loginReport = loginReport + 'Failed at username\n'
					file = open(reportFile, "w")
					file.write(loginReport)
					break
			elif data == expected[1]:
				sock.sendall(password[p])
				p+=1
				if len(password) < p:
					loginReport = loginReport + 'Failed at password\n'
					file = open(reportFile, "w")
					file.write(loginReport)
					break
			elif data == expected[4]:
				loginReport = loginReport + data
				file = open(reportFile,"w")
				file.write(loginReport)
				break
finally:
	sock.close()
