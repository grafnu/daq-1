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
  Report reportHandler;
  Map macDevices;
  boolean testFinished = false;

  InputStream jsonStream = this.getClass().getResourceAsStream("/defaultPasswords.json");
  String testName = "security.passwords";
  String[] jsonUsernames;
  String[] jsonPasswords;
  Gson gson = new Gson();
  boolean macRetrieved = false;
  String macAddress;
  String formattedMac;

  public SshSocket(String host, Map macDevices, int connectionPort, String macAddress) {
    this.macDevices = macDevices;
    this.connectionPort = connectionPort;
    this.host = host;
    this.macAddress = macAddress;
    reportHandler = new Report();
  }

  private void getMACAddress() {
    try {
      macAddress = macAddress.replace(":", "");
      formattedMac = macAddress.substring(0, 6).toUpperCase();
      System.out.println("MAC ADDRESS : " + macAddress + "  " + macDevices.get(formattedMac));
      getJsonFile(formattedMac);
    } catch (Exception e) {
      System.out.println(e);
      Report reportHandler = new Report();
      reportHandler.addText("RESULT skip security.passwords");
      reportHandler.writeReport();
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
      macRetrieved = true;
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    } catch (JsonIOException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      System.out.println("can not find manufacturer in password list. Not yet implemented");
      reportHandler = new Report();
      reportHandler.addText(macAddress);
      reportHandler.addText("RESULT skip security.passwords");
      reportHandler.writeReport();
    }
  }

  public void connectSshSocket() {
    System.out.println("Port number is:  " + connectionPort);
    while (!testFinished) {
      if (passwordIndex == jsonPasswords.length) {
        usernameIndex++;
        passwordIndex = 0;
      }
      if (usernameIndex > jsonUsernames.length - 1) {
        testFinished = true;
        System.err.println("ran out of sign in options");
        reportHandler.addText("RESULT pass " + testName);
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
            reportHandler.addText("RESULT fail " + testName);
            testFinished = true;
          } catch (JSchException e) {
            if (e.toString().contains("Connection refused")) {
              reportHandler.addText("RESULT skip " + testName);
              System.out.println("SSH not set up on device");
              testFinished = true;
              break;
            } else {
              System.out.println(e + "username/password incorrect");
              passwordIndex++;
            }
          }
        } catch (JSchException e) {
          System.out.println("session issue");
          System.out.println(e);
        }
      }
    }

    reportHandler.writeReport();
    System.out.println("All tests have been run");
    session.disconnect();
  }

  @Override
  public void run() {
    getMACAddress();
    if (macRetrieved) {
      connectSshSocket();
    }
  }
}
