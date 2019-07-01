import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.IO;
import org.apache.commons.net.telnet.*;

public class TelnetSocket implements Runnable {

  String host;
  TelnetClient telnetClient;
  OutputStream outputStream;
  InputStream inputStream;
  InetAddress ip;
  int connectionPort;
  Interrogator interrogator;
  //MACHandler macHandler;
  String macAddress;
  int bytesRead = 0;
  byte[] messageByte = new byte[1024];
  Charset chars = Charset.forName("UTF-8");
  Map macDevices;
  Queue<String> readData = new LinkedList();
  boolean debug = false;
  Thread gatherThread;
  Thread readThread;
  Thread consoleThread;
  Thread checkThread;
  Gson gson = new Gson();
  File jsonFile = new File("resources/defaultPasswords.json");
  InputStream jsonStream = this.getClass().getResourceAsStream("/defaultPasswords.json");
  String[] jsonPasswords;
  String[] jsonUsernames;
  String formattedMac;
  String temp = null;

  public TelnetSocket(String host, Map macDevices, int connectionPort, String macAddress) {
    this.connectionPort = connectionPort;
    this.macDevices = macDevices;
    this.host = host;
    this.macAddress = macAddress;
    //macHandler = new MACHandler();
  }

  private void getMACAddress() {
    try {
      macAddress = macAddress.replace(":", "");
      formattedMac = macAddress.substring(0, 6).toUpperCase();
      System.out.println("MAC ADDRESS : " + formattedMac + "  " + macDevices.get(formattedMac));
      getJsonFile(formattedMac);

    } catch (Exception e) {
      Report reportHandler = new Report();
      reportHandler.addText("RESULT skip security.passwords");
      reportHandler.writeReport("telnet");
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
      System.err.println("Json Files Syntax Error");
      System.err.println(e);
    } catch (JsonIOException e) {
      System.err.println("Json File Read Error");
      System.err.println(e);
    } catch (NullPointerException e) {
      System.out.println(e);
      Report reportHandler = new Report();
      reportHandler.addText("RESULT skip security.passwords");
      reportHandler.writeReport("telnet");

    }
  }

  public void connectTelnetClient() {
    telnetClient = null;
    telnetClient = new TelnetClient();
    System.out.println("Starting Telnet Connection");
    try {
      telnetClient.connect(host, connectionPort);
      System.out.println("Connected");
    } catch (Exception e) {
      Report reportHandler = new Report();
      reportHandler.addText("RESULT skip security.passwords");
      reportHandler.writeReport("telnet");
      System.err.println(e);
      System.out.println("port was " + connectionPort);
      System.out.println("ipaddress was " + host);

    }
  }

  public void read(){
    InputStream instr = telnetClient.getInputStream();

    try
    {
      byte[] buff = new byte[1024];
      int ret_read = 0;

      do
      {
        ret_read = instr.read(buff);
        if(ret_read > 0)
        {
        String item = (new String(buff, 0, ret_read));
        System.out.println(item);

        interrogator.receiveData(item);
        }

//          try {
//            TimeUnit.MILLISECONDS.sleep(50);
//          }catch(InterruptedException e){
//            System.out.println(e);
//          }
//
         }
      while (ret_read >= 0);
    }
    catch (IOException e)
    {
      System.err.println("Exception while reading socket:" + e.getMessage());
    }

    try
    {
      telnetClient.disconnect();
    }
    catch (IOException e)
    {
      System.err.println("Exception while closing telnet:" + e.getMessage());
    }
  }

//  public void readLines(InputStream in){
//      while(telnetClient.isConnected()) {
//        InputStreamReader is = new InputStreamReader(in);
//        StringBuilder sb = new StringBuilder();
//        BufferedReader br = new BufferedReader(is);
//        try {
//          String read = br.readLine();
//          System.out.println(read);
//          sb.append(read);
//          read = br.readLine();
//          interrogator.receiveData(read);
//        } catch (IOException e) {
//          System.out.println(e);
//        }
//



//}
//  private String normalizeLineEnding(byte[] bytes, char endChar) {
//    List<Byte> bytesBuffer = new ArrayList<Byte>();
//    int countBreak = 0;
//    int countESC = 0;
//    for (int i = 0; i < bytes.length; i++) {
//      if (bytes[i] != 0) {
//        switch (bytes[i]) {
//          case 8:
//            // backspace \x08
//            break;
//          case 10:
//            // newLineFeed \x0A
//            countBreak++;
//            bytesBuffer.add((byte) endChar);
//            break;
//          case 13:
//            // carriageReturn \x0D
//            countBreak++;
//            bytesBuffer.add((byte) endChar);
//            break;
//          case 27:
//            // escape \x1B
//            countESC = 2;
//            break;
//          case 33:
//            // character:!
//            break;
//          default:
//            if (countESC == 0) {
//              if (countBreak > 1) {
//                int size = bytesBuffer.size();
//                for (int x = 0; x < countBreak - 1; x++) {
//                  bytesBuffer.remove(size - 1 - x);
//                }
//                countBreak = 0;
//              }
//              bytesBuffer.add(bytes[i]);
//            } else {
//              countESC--;
//            }
//            break;
//        }
//      }
//    }
//
//    String bytesString = "";
//    for (Byte byteBuffer : bytesBuffer) {
//      bytesString = bytesString + (char) (byte) byteBuffer;
//    }
//    return bytesString;
//  }
//
//  public void readData() {
//    int bytesRead = 0;
//    inputStream = telnetClient.getInputStream();
//    while (telnetClient.isConnected()) {
//      inputStream = telnetClient.getInputStream();
//      try {
//        byte[] buffer = new byte[1024];
//        bytesRead = inputStream.read(buffer);
//        if (bytesRead > 0) {
//          String rawData = normalizeLineEnding(buffer, '\n');
//          readData.add(rawData);
//        } else {
//          try {
//            Thread.sleep(100);
//          } catch (InterruptedException e) {
//            System.err.println("InterruptedException readData:" + e.getMessage());
//          }
//        }
//      } catch (IOException e) {
//        System.err.println("Exception while reading socket:" + e.getMessage());
//      }
//    }
//  }
//
//  public void gatherData() {
//    StringBuilder receivedData = new StringBuilder();
//    String gatheredString = "";
//    int receiveWaitCounter = 0;
//    int tempWaitCounter = 0;
//    while (telnetClient.isConnected()) {
//      try {
//        if (readData.isEmpty()) {
//          Thread.sleep(20);
//          receiveWaitCounter++;
//          if(receiveWaitCounter > 70) {
//            receiveWaitCounter = 0;
//          }
//        } else {
//          receiveWaitCounter = 0;
//          String tempString = readData.poll();
//          if (tempString.equals("")) {
//            Thread.sleep(20);
//            tempWaitCounter++;
//          } else {
//            receiveWaitCounter = 0;
//            tempWaitCounter = 0;
//            receivedData.append(tempString);
//            gatheredString = receivedData.toString();
//            System.out.println(
//                    java.time.LocalTime.now()
//                            + "dataLength:"
//                            + gatheredString.length()
//                            + "receivedData:"
//                            + gatheredString);
//
//            interrogator.receiveData(gatheredString);
//            receivedData.delete(0, gatheredString.length());
//          }
//        }
//      } catch (InterruptedException e) {
//        System.err.println("InterruptedException gatherData:" + e.getMessage());
//      }
//    }
//  }

  public void writeData(String data) {
    System.out.println(data);
    try {
      outputStream = telnetClient.getOutputStream();
      outputStream.write(data.getBytes());
      outputStream.flush();
    } catch (IOException e) {
      resetConnection();
      e.printStackTrace();
    }
  }

  public void resetConnection() {
    disconnect();
    connectTelnetClient();
    read();

  }

  public void checkConnection() {
    if (!telnetClient.isConnected()) {
      connectTelnetClient();
    }
  }

  public void disconnect() {
    try {
      telnetClient.disconnect();
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  @Override
  public void run() {
    connectTelnetClient();
    getMACAddress();

 //   readLines(telnetClient.getInputStream());
//    try {
      interrogator =
              new Interrogator(
                      this,
                      jsonUsernames,
                      jsonPasswords,
                      macAddress);
    read();
////      Runnable readDataRunnable =
////              () -> {
////                readData();
////              };
////      readThread = new Thread(readDataRunnable);
////
////      readThread.start();
////
////      Runnable readConsoleRunnable =
////    		  ()->{
////    			  readConsole();
////    		  };
////    consoleThread = new Thread(readConsoleRunnable);
////    consoleThread.start();
////
////      Runnable gatherDataRunnable =
////              () -> {
////                gatherData();
////              };
////      gatherThread = new Thread(gatherDataRunnable);
////
////      gatherThread.start();
//
//
//
//      Runnable checkConnectionRunnable =
//              () -> {
//                checkConnection();
//              };
//
//      checkThread = new Thread(checkConnectionRunnable);
//      checkThread.start();
//    } catch (NullPointerException e) {
//      System.out.println("Unfound manufacturer of device");
//      System.out.println(e);
//      System.out.println(this);
//      System.out.println(jsonUsernames);
//      System.out.println(jsonPasswords);
//      System.out.println(macAddress);
//      System.out.println(macAddress.substring(0,6).toString().toUpperCase());
//      Report reportHandler = new Report();
//      reportHandler.addText(macAddress.substring(0, 6).toString());
//      reportHandler.addText("Manufacturer not found to run telnet tests");
//      reportHandler.addText("RESULT skip security.passwords");
//      reportHandler.writeReport("telnet");
//    }
  }
}