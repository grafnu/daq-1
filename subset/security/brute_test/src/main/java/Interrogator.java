public class Interrogator {

  String[] expected = {
          "login:", "P", "Last login:", "Login incor", "Connection closed by foreign host.", "Welcome","timed out"
  };
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
  int nameCount = 0;
  int passCount =0;
  boolean userValid = false;
  int attempts = 0;

  public Interrogator(
          TelnetSocket telnetSocket,
          String[] username,
          String[] password,
          String macAddress) {
    this.telnetSocket = telnetSocket;
    reportHandler = new Report();
    this.username = username;
    this.password = password;
    reportHandler.addText("MAC Address : " + macAddress);
  }

  public void receiveData(String data) {

    if (debug) {
      System.out.println(
              java.time.LocalTime.now() + "receiveDataLen:" + data.length() + "receiveData:" + data);
    }
    if (data != null) {
      parseData(data);
    }
  }

  private void parseData(String data) {
    data = data.trim();
    if (data.contains(expected[3]) && passwordIndex == 0) {
      reportHandler.addText("Failed Login" + ":-" + usedUsername + " : " + usedPassword);
      usernameCount++;
    }
    attemptCount++;
  //  System.out.println("number of attempts: " + attemptCount);
    if(data.contains(expected[6])){
      System.out.println("TIMEOUT");
      reportHandler.addText("RESULT skip security.passwords");
      reportHandler.writeReport("telnet");
      telnetSocket.disconnect();
    }

    else if (data.contains(expected[2]) || data.contains(expected[5])) {
      System.out.println("Login Success");
      reportHandler.addText("Login Success" + ":-" + usedUsername + " : " + usedPassword);
      reportHandler.addText("RESULT fail " + testName);
      writeData("\n");
      telnetSocket.disconnect();
      reportHandler.printReport();
      reportHandler.writeReport("telnet");
    } else if (data.endsWith(expected[0])) {
      try {
     //   System.out.println(username.length);
        if (usernameCount == username.length) {
          reportHandler.addText("RESULT pass " + testName);
          telnetSocket.disconnect();
          reportHandler.writeReport("telnet");
        } else {
          if (passwordIndex == password.length) {
            usernameCount++;
            passwordIndex = 0;
          }
          String value = username[usernameCount];
          String trimmedVal = value.trim();
          usedUsername = trimmedVal;
          writeData(trimmedVal+"\n");
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        reportHandler.addText("RESULT pass " + testName);
        telnetSocket.disconnect();
        reportHandler.writeReport("telnet");
        System.out.println("Could not log into server with provided credentials ");
      }
    } else if (data.startsWith(expected[1])) {
      String passValue = password[passwordIndex];
      String trimmedPass = passValue.trim();
      usedPassword = trimmedPass;
      writeData(trimmedPass+"\n");
      passwordIndex++;
    } else if (data.indexOf(expected[4]) >= 0) {
      reportHandler.addText("RESULT skip "+ testName);
      reportHandler.writeReport("telnet");
      System.out.println("Failed after 3 tries");
      telnetSocket.disconnect();
    }
    if (data.contains(expected[3]) && attemptCount % 3 == 0) {
      System.out.println(data + attemptCount + "restart");
      telnetSocket.resetConnection();
    }
  }

  public void writeData(String data) {
    telnetSocket.writeData(data);
  }
}