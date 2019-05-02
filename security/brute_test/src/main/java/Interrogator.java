

public class Interrogator {

	String[] expected = {"OFS2 login:","Password:","Last login:","Login incor","Connection closed by foreign host."};
	String[] username;
	String[] password;
	String usedUsername;
	String usedPassword;
	int usernameCount = 0;
	int usernameTemp = 0;
	int attemptCount = 0;
	int passwordIndex = 0;
	int passwordAttempts = 0;
	String report;
	String testName = "security.passwords";
	Report reportHandler;
	TelnetSocket telnetSocket;
	boolean debug = false;
	
	public Interrogator(TelnetSocket telnetSocket, String[] username, String[] password, String macAddress,String manufacturer) {
		this.telnetSocket = telnetSocket;
		reportHandler = new Report();
		this.username = username;
		this.password = password;
		reportHandler.addText("MAC Address : " + macAddress + "*");
		reportHandler.addText("Manufacturer : " + manufacturer + "*");
	}
	
	public void receiveData(String data) {
		
		if(debug) {
			 System.out.println(
			          java.time.LocalTime.now() + "receiveDataLen:" + data.length() + "receiveData:" + data);
		}
		if(data != null) {
			Thread parseDataThread = new Thread(() -> parseData(data));
			parseDataThread.start();
		}
	}
	
	public void parseData(String data) {
		data=data.trim();
			if(data.contains(expected[3])) {
				reportHandler.addText("Failed Login" + ":-" + usedUsername + " : " +  usedPassword + "*");
				attemptCount++;
				System.out.println("number of attempts: " + attemptCount);
			}
			if(data.contains(expected[2])) {
				System.out.println("Login Success");
				reportHandler.addText("Login Success" + ":-" + usedUsername + " : " + usedPassword + "*");
				reportHandler.addText("RESULT fail " + testName + "*");
				writeData("\n");  
				telnetSocket.disconnect();
				reportHandler.printReport();
				reportHandler.writeReport("telnet");
			}
		
			else if(data.endsWith(expected[0])) {
				try {
					if(usernameCount == username.length) {
						reportHandler.addText("RESULT pass " + testName + "*");
						telnetSocket.disconnect();
						reportHandler.writeReport("telnet");
					}
					else {
						if(passwordIndex == password.length) {
							usernameCount++;
							passwordIndex = 0;
						}
						String value = username[usernameCount] + "\n";
						String trimmedVal = value.trim();
						usedUsername = trimmedVal;
						writeData(trimmedVal);
					}
				}
				catch(ArrayIndexOutOfBoundsException e) {
					reportHandler.addText("RESULT pass " + testName + "*");
					telnetSocket.disconnect();
					reportHandler.writeReport("telnet");
					System.out.println("Could not log into server with provided credentials ");
				}
			}
			else if(data.endsWith(expected[1])) {
				String passValue = password[passwordIndex] + "\n";
				String trimmedPass = passValue.trim();
				usedPassword = trimmedPass;
				writeData(trimmedPass);
				passwordIndex++;
			}
			else if(data.indexOf(expected[4]) >= 0) {
				System.out.println("Failed after 3 tries");
				telnetSocket.disconnect();
			}
			if(data.contains(expected[3]) && attemptCount %3== 0) {
				System.out.println(data + attemptCount + "restart");
				telnetSocket.resetConnection();
			}
		}
	
	public void writeData(String data) {
		telnetSocket.writeData(data);
	}
}