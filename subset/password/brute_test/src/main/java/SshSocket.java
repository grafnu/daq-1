import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class SshSocket implements Runnable {

  String host;
  int connectionPort;
  Channel channel;
  Session session;
  JSch jsch = new JSch();
  int attempts = -1;
  int passwordIndex = 0;
  int usernameIndex = 0;
  int passedTests;
  int passedTestsIndex;
  Report reportHandler;
  Map<String, String> passedCombination;
  Map macDevices;
  boolean testFinished = false;
  int timeout = 30000;
  InputStream jsonStream = this.getClass().getResourceAsStream("/defaultPasswords.json");
  String testName = "security.passwords";
  String[] jsonUsernames;
  String[] jsonPasswords;
  Gson gson = new Gson();
  //    MACHandler macHandler;
  String macAddress;

  public SshSocket(String host, Map macDevices, int connectionPort, String macAddress) {
    this.macDevices = macDevices;
    this.connectionPort = connectionPort;
    this.host = host;
    this.macAddress = macAddress;
    //          this.macHandler = new MACHandler();
    reportHandler = new Report();
  }

  private void getMACAddress() {
    try {

      // macAddress = macHandler.runShellCommand("arp " + host);
      macAddress = macAddress.replace(":", "");
      System.out.println(
          "MAC ADDRESS : " + macAddress + "  " + macDevices.get(macAddress.substring(0, 6)));
      getJsonFile((macAddress.substring(0, 6)));
    } catch (Exception e) {
    	System.out.println(e);
        e.printStackTrace();
        Report reportHandler = new Report();
        reportHandler.addText("RESULT security.passwords FAILED : manufacturer not found");
        reportHandler.writeReport("ssh");
    }
  }

  public void getJsonFile(String model) {
    try {
      JsonObject jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
      JsonObject parent = jsonObject.getAsJsonObject(model);
      String usernames = parent.get("Usernames").getAsString();
      String passwords = parent.get("Passwords").getAsString();
      jsonUsernames = usernames.split(",");
      jsonPasswords = passwords.split(",");

    } catch (JsonSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonIOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(NullPointerException e) {
    	System.out.println("can not find manufacturer in password list. Not yet implmeneted");
    }
    //                  catch (FileNotFoundException e) {
    //                  // TODO Auto-generated catch block
    //                  e.printStackTrace();
    //          }
  }

  public void connectSshSocket() {

    reportHandler.addText("MAC Address : " + macAddress + "*");
    reportHandler.addText("Manufacturer : " + macDevices.get(macAddress.substring(0, 6)) + "*");

    while (!testFinished) {
      if (passwordIndex == jsonPasswords.length) {
        usernameIndex++;
        passwordIndex = 0;
      }

      if (usernameIndex > jsonUsernames.length - 1) {
        testFinished = true;
        System.err.println("ran out of sign in options");
        reportHandler.addText("RESULT pass " + testName + " *");

      } else {
        attempts++;
        System.out.println(
            "attempt "
                + (attempts + 1)
                + " made using: "
                + jsonUsernames[usernameIndex]
                + "/"
                + jsonPasswords[passwordIndex]);
        try {

          session = jsch.getSession(jsonUsernames[usernameIndex], host, connectionPort);

          session.setPassword(jsonPasswords[passwordIndex]);
          try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("username/password correct");
            reportHandler.addText(
                "Successful Login"
                    + ":-"
                    + jsonUsernames[usernameIndex]
                    + " : "
                    + jsonPasswords[passwordIndex]
                    + "*");
            reportHandler.addText("RESULT fail " + testName + " *");
            testFinished = true;

          } catch (Exception e) {
            System.out.println("username/password incorrect");
            reportHandler.addText(
                "Failed Login"
                    + ":-"
                    + jsonUsernames[usernameIndex]
                    + " : "
                    + jsonPasswords[passwordIndex]
                    + "*");
            passwordIndex++;
          }

        } catch (JSchException e) {
          System.out.println("session issue");
          System.out.println(e);
        }
      }
    }

    reportHandler.writeReport("ssh");
    System.out.println("All tests have been run");
    session.disconnect();
  }

  @Override
  public void run() {
    getMACAddress();
    connectSshSocket();
  }
}