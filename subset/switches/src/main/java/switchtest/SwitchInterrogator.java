package switchtest;

/*
 * Licensed to the Google under one or more contributor license agreements.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public abstract class SwitchInterrogator implements Runnable {

  public boolean promptReady = false;

  //  int data_length = 32768;
  //
  //  int count = 0;
  //
  //  int continue_flag = 1;
  public SwitchInterrogator(
      String remoteIpAddress, int interfacePort, boolean deviceConfigPoeEnabled) {
    this.remoteIpAddress = remoteIpAddress;
    this.interfacePort = interfacePort;
    this.deviceConfigPoeEnabled = deviceConfigPoeEnabled;
    // Load all the switch specific variables
    this.command = commands();
    this.commandToggle = commandToggle();
    this.expected = expected();
    this.interface_expected = interfaceExpected();
    this.login_expected = loginExpected();
    this.platform_expected = platformExpected();
    this.power_expected = powerExpected();
    this.show_interface_expected = showInterfaceExpected();
    this.show_interface_port_expected = showInterfacePortExpected();
    this.show_platform_expected = showPlatformExpected();
    this.show_platform_port_expected = showPlatformPortExpected();
    this.show_power_expected = showPowerExpected();
    this.stack_expected = stackExpected();
    this.show_stack_expected = showStackExpected();

    // Initialize data arrays based on switch specific command sets
    show_platform_pointers = new int[show_platform_expected.length];
    show_platform_data = new String[show_platform_expected.length / 2];
    show_platform_port_pointers = new int[show_platform_port_expected.length];

    show_interface_pointers = new int[show_interface_expected.length];
    show_interface_data = new String[show_interface_expected.length - 1];

    show_interface_data = new String[show_interface_expected.length / 2];
    show_interface_port_pointers = new int[show_interface_port_expected.length];

    show_power_pointers = new int[show_power_expected.length];
    show_power_data = new String[show_power_expected.length - 1];

    show_stack_pointers = new int[show_stack_expected.length];
    show_stack_data = new String[show_stack_expected.length - 1];
  }

  protected SwitchTelnetClientSocket telnetClientSocket;
  protected Thread telnetClientSocketThread;

  // TODO: enabled the user to input their own username and password
  protected String username = "manager";
  protected String password = "friend";

  protected String remoteIpAddress;
  protected int remotePort = 23;
  protected int interfacePort = 12;

  protected String reportFilename = "tmp/report.txt";

  protected int interfacePos = 1;
  protected int platformPos = 2;
  protected int powerinlinePos = 3;

  protected boolean deviceConfigPoeEnabled = false;

  protected boolean debug = true;
  boolean switchSupportsPoe = false;

  boolean userAuthorised = false;
  boolean userEnabled = false;

  protected String device_hostname = "";

  int shortPacketLength = 20;

  int requestFlag = 0;

  protected String login_report = "";
  boolean extendedTests = false;
  int number_switch_ports = 1; // 48

  protected String[] command;
  protected String[] commandToggle;
  protected String[] expected;
  protected String[] interface_expected;
  public String[] login_expected;
  protected String[] platform_expected;
  protected String[] power_expected;
  protected String[] show_platform_expected;
  protected String[] show_power_expected;
  protected String[] show_interface_expected;
  protected String[] show_interface_port_expected;
  protected String[] show_platform_port_expected;
  protected String[] stack_expected;
  protected String[] show_stack_expected;

  protected int[] show_platform_pointers;
  protected String[] show_platform_data;
  protected int[] show_platform_port_pointers;
  protected int[] show_interface_pointers;
  protected String[] show_interface_data;
  protected int[] show_interface_port_pointers;
  protected int[] show_power_pointers;
  protected String[] show_power_data;
  protected int[] show_stack_pointers;
  protected String[] show_stack_data;

  protected abstract String[] commands();

  protected abstract String[] commandToggle();

  protected abstract String[] expected();

  protected abstract String[] interfaceExpected();

  protected abstract String[] loginExpected();

  protected abstract String[] platformExpected();

  protected abstract String[] powerExpected();

  protected abstract String[] showInterfaceExpected();

  protected abstract String[] showInterfacePortExpected();

  protected abstract String[] showPlatformExpected();

  protected abstract String[] showPlatformPortExpected();

  protected abstract String[] showPowerExpected();

  protected abstract String[] stackExpected();

  protected abstract String[] showStackExpected();

  protected HashMap<String, String> interface_map = new HashMap<String, String>();

  protected HashMap<String, String> platform_map = new HashMap<String, String>();

  protected HashMap<String, String> power_map = new HashMap<String, String>();

  protected HashMap<String, String> stack_map = new HashMap<String, String>();

  public int getRequestFlag() {
    return requestFlag - 1;
  }

  public String getHostname() {
    return device_hostname;
  }

  public boolean getUserAuthorised() {
    return userAuthorised;
  }

  public void setUserAuthorised(boolean userAuthorised) {
    this.userAuthorised = userAuthorised;
  }

  public boolean getUserEnabled() {
    return userEnabled;
  }

  public void setUserEnabled(boolean userEnabled) {
    this.userEnabled = userEnabled;
  }

  @Override
  public abstract void run();

  public abstract boolean processConsoleMessage(String consoleData);

  public void receiveData(String data) {
    if (debug) {
      System.out.println(
          java.time.LocalTime.now() + "receiveDataLen:" + data.length() + "receiveData:" + data);
    }
    if (data != null) {
      Thread parseThread = new Thread(() -> parseData(data));
      parseThread.start();
    }
  }

  private void parseData(String data) {
    try {
      if (data.length() > 0) {

        if (!userAuthorised) {
          data = data.substring(0, data.indexOf(":") + 1);
          System.out.println("decoded_data:" + data);

          // login procedure
          if (data.indexOf(expected[0]) >= 0) {
            // username request
            String[] data_array = data.split(" ");
            device_hostname = data_array[0];
            telnetClientSocket.writeData(username + "\n");
          } else if (data.indexOf(expected[1]) >= 0) {
            // password request
            telnetClientSocket.writeData(password + "\n");
          }
        } else {
          if (!userEnabled) {
            data = data.substring(0, data.indexOf(":") + 1);
            if (data.indexOf(expected[2]) >= 0) {
              // login success
              telnetClientSocket.writeData(command[requestFlag] + "\n");
              requestFlag = 1;
            }
          } else {
            // running configuration requests
            if (data.indexOf(device_hostname) >= 0 && data.length() < shortPacketLength) {
              int requestFinish = powerinlinePos;
              if (extendedTests) {
                requestFinish = command.length;
              }
              if (requestFlag <= requestFinish) {
                telnetClientSocket.writeData(command[requestFlag] + "\n");
                System.out.println(
                    "command:" + command[requestFlag] + " request_flag:" + requestFlag);
                requestFlag += 1;
              } else {
                System.out.println("finished running configuration requests");
                validateTests();
                telnetClientSocket.disposeConnection();
              }
            } else {
              parseRequestFlag(data, requestFlag);
            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Exception parseData:" + e.getMessage());
    }
  }

  private void parse_packet(String raw_data, String[] show_expected, int[] show_pointers) {
    try {
      int start = 0;
      for (int port = 0; port < number_switch_ports; port++) {
        for (int x = 0; x < show_expected.length; x++) {
          start = recursive_data(raw_data, show_expected[x], start);
          show_pointers[x] = start;
        }

        int chunk_s = show_pointers[0];
        int chunk_e = show_pointers[3];

        if (chunk_e == -1) chunk_e = raw_data.length();

        String temp_data = raw_data.substring(chunk_s, chunk_e);

        if (debug) System.out.println("length" + temp_data.length() + "temp_data:" + temp_data);

        if (requestFlag == (interfacePos + 1)) {
          parse_single(
              temp_data, show_interface_expected, show_interface_pointers, show_interface_data);
        } else if (requestFlag == (platformPos + 1)) {
          parse_single(
              temp_data, show_platform_expected, show_platform_pointers, show_platform_data);
        }
      }
    } catch (Exception e) {
      System.err.println("Exception parse_packet:" + e.getMessage());
      System.exit(1);
    }
  }

  private void parse_single(
      String raw_data, String[] expected_array, int[] pointers_array, String[] data_array) {
    try {
      int start = 0;
      for (int x = 0; x < expected_array.length; x++) {
        start = recursive_data(raw_data, expected_array[x], start);
        if (start == -1) {
          start = pointers_array[x - 1];
          pointers_array[x] = -1;
        } else {
          pointers_array[x] = start;
        }
      }

      for (int x = 0; x < data_array.length; x++) {
        int chunk_start = x * 2;
        int chunk_end = 1 + (x * 2);

        int chunk_s = pointers_array[chunk_start];
        int chunk_e = pointers_array[chunk_end];

        if (chunk_s > 0) {
          if (chunk_e == -1) chunk_e = raw_data.length();

          data_array[x] = raw_data.substring(chunk_s, chunk_e);

          data_array[x] = data_array[x].substring(expected_array[chunk_start].length());

          String extracted_data = expected_array[chunk_start] + data_array[x];

          if (debug) System.out.println(extracted_data);

          login_report += extracted_data + "\n";
        }
      }
    } catch (Exception e) {
      System.err.println("Exception parse_single:" + e.getMessage());
      System.exit(1);
    }
  }

  protected void parse_inline(
      String raw_data, String[] expected, int[] pointers, String[] data_array) {
    try {
      int start = 0;

      for (int x = 0; x < expected.length; x++) {
        start = recursive_data(raw_data, expected[x], start);
        pointers[x] = start;
        if (x > 0) {
          pointers[x - 1] = pointers[x] - pointers[x - 1];
        }
      }
      int chunk_start = pointers[pointers.length - 1];
      int chunk_end = pointers[pointers.length - 1];

      for (int x = 0; x < data_array.length; x++) {
        chunk_start = chunk_end;

        if (x > 0) {
          chunk_start += 1;
        }

        chunk_end += pointers[x];

        if (x != data_array.length - 1) {
          data_array[x] =
              raw_data.substring(chunk_start, chunk_end).replace("\n", "").replace(" ", "");
        } else {
          chunk_end = recursive_data(raw_data, "\n", chunk_start);
          data_array[x] = raw_data.substring(chunk_start, chunk_end).replace("\n", "");
        }
        login_report += expected[x] + ":" + data_array[x] + "\n";
      }
    } catch (Exception e) {
      System.err.println("Exception parse_inline:" + e.getMessage());
      System.exit(1);
    }
  }

  private int recursive_data(String data, String search, int start) {
    int pointer = data.indexOf(search, start);
    if (debug) {
      System.out.println("pointer:" + pointer);
    }
    return pointer;
  }

  protected HashMap dataToMap(String[] expected_key, String[] data_array) {
    System.out.println("Data To mape");
    HashMap<String, String> hashMap = new HashMap<String, String>();
    for (int i = 0; i < expected_key.length; i++) {
      System.out.println("expectedKey: " + expected_key[i]);
      hashMap.put(expected_key[i], data_array[i]);
    }
    return hashMap;
  }

  protected String trashLagLines(String data, int index, int lineIndex) {
    byte[] dataBytes = data.getBytes();
    int counter = 0;
    for (int i = 0; i < index; i++) {
      if (dataBytes[i] == '\n') {
        counter++;
      }
    }
    if (counter > 0) {
      counter -= lineIndex;
    }
    for (int i = counter; i > 0; i--) {
      data = trash_line(data, lineIndex);
    }
    return data;
  }

  private String poeNormalizeData(String data) {
    data = trashLagLines(data, data.indexOf("Interface"), 0);
    data = trashLagLines(data, data.indexOf("port"), 1);
    return data;
  }

  private void parseRequestFlag(String data, int requestFlag) {
    try {
      switch (requestFlag) {
        case 2:
          // parse show interface
          login_report += "show interface:\n";
          parse_packet(data, show_interface_port_expected, show_interface_port_pointers);
          interface_map = dataToMap(interface_expected, show_interface_data);
          writeReport();
          telnetClientSocket.writeData("\n");
          break;
        case 3:
          // parse show platform
          login_report += "\nshow platform:\n";
          parse_packet(data, show_platform_port_expected, show_platform_port_pointers);
          platform_map = dataToMap(platform_expected, show_platform_data);
          writeReport();
          telnetClientSocket.writeData("\n");
          break;
        case 4:
          // parse show power-inline
          login_report += "\nshow power-inline:\n";
          if (data.contains("Power-inline is disabled")) {
            login_report += "Power-inline is disabled\n";
          } else {
            switchSupportsPoe = true;
            data = poeNormalizeData(data);
            parse_inline(data, show_power_expected, show_power_pointers, show_power_data);
            power_map = dataToMap(power_expected, show_power_data);
          }
          writeReport();
          telnetClientSocket.writeData("\n");
          break;
        case 5:
          // parse show run
          data = cleanShowRunData(data);
          login_report += "\n" + data;
          writeReport();
          telnetClientSocket.writeData("\n");
          break;
        case 6:
          // parse show stack
          login_report += "\nshow stack:\n";
          parse_inline(data, show_stack_expected, show_stack_pointers, show_stack_data);
          stack_map = dataToMap(stack_expected, show_stack_data);
          writeReport();
          telnetClientSocket.writeData("\n");
          break;
      }
    } catch (Exception e) {
      System.err.println("Exception parseRequestFlag:" + e.getMessage());
      System.exit(1);
    }
  }

  private String trash_line(String data, int trash_line) {
    String[] lineArray = data.split("\n");
    String tempData = "";
    for (int i = 0; i < lineArray.length; i++) {
      if (i != trash_line) {
        tempData += lineArray[i] + "\n";
      }
    }
    return tempData;
  }

  private String cleanShowRunData(String data) {
    data = data.replace("\n\n\n\n", "\n");
    data = data.replace("\n\n\n", "\n");
    data = data.replace("\n\n", "\n");
    data = data.replace("end", "");
    return data;
  }

  protected void validateTests() {
    try {
      login_report += "\n";

      if (interface_map.get("link_status").equals("UP")) {
        login_report += "RESULT pass connection.port_link\n";
      } else {
        login_report += "RESULT fail connection.port_link Link is down\n";
      }

      if (interface_map.get("current_speed") != null) {
        if (interface_map.get("configured_speed").equals("auto")
            && Integer.parseInt(interface_map.get("current_speed")) >= 10) {
          login_report += "RESULT pass connection.port_speed\n";
        } else {
          login_report += "RESULT fail connection.port_speed Speed is too slow\n";
        }
      } else {
        login_report += "RESULT fail connection.port_speed Cannot detect current speed\n";
      }

      if (interface_map.get("current_duplex") != null) {
        if (interface_map.get("configured_duplex").equals("auto")
            && interface_map.get("current_duplex").equals("full")) {
          login_report += "RESULT pass connection.port_duplex\n";
        } else {
          login_report += "RESULT fail connection.port_duplex Incorrect duplex mode set\n";
        }
      } else {
        login_report += "RESULT fail connection.port_duplex Cannot detect duplex mode\n";
      }

      if (switchSupportsPoe && deviceConfigPoeEnabled) {
        String current_max_power = power_map.get("max").replaceAll("\\D+", "");
        String current_power = power_map.get("power").replaceAll("\\D+", "");
        String current_PoE_admin = power_map.get("admin");
        String current_oper = power_map.get("oper");

        System.out.println(
            "current_max_power:"
                + current_max_power
                + "current_power:"
                + current_power
                + "current_PoE_admin:"
                + current_PoE_admin
                + "current_oper:"
                + current_oper);

        if (current_max_power.length() > 0
            && current_power.length() > 0
            && current_PoE_admin.length() > 0
            && current_oper.length() > 0) {
          if (Integer.parseInt(current_max_power) > Integer.parseInt(current_power)
              && !current_oper.equals("Fault")) {
            login_report += "RESULT pass poe.power\n";
          } else {
            login_report +=
                "RESULT fail poe.power The DUT is drawing too much current or there is a fault on the line\n";
          }
          if (current_PoE_admin.equals("Enabled")) {
            login_report += "RESULT pass poe.negotiation\n";
          } else {
            login_report += "RESULT fail poe.negotiation Incorrect privilege for negotiation\n";
          }
          if (!current_oper.equals("Off")) {
            login_report += "RESULT pass poe.support\n";
          } else {
            login_report +=
                "RESULT fail poe.support The AT switch does not support PoE or it is disabled\n";
          }
        } else {
          login_report += "RESULT fail poe.power Could not detect any current being drawn\n";
          login_report += "RESULT fail poe.negotiation Could not detect any current being drawn\n";
          login_report += "RESULT fail poe.support Could not detect any current being drawn\n";
        }
      } else {
        login_report +=
            "RESULT skip poe.power The AT switch does not support PoE or this test is disabled\n";
        login_report +=
            "RESULT skip poe.negotiation The AT switch does not support PoE or this test is disabled\n";
        login_report +=
            "RESULT skip poe.support The AT switch does not support PoE or this test is disabled\n";
      }

      writeReport();
    } catch (Exception e) {
      System.err.println("Exception validateTests:" + e.getMessage());
      e.printStackTrace();
    }
  }

  protected void writeReport() {
    try {
      if (debug) {
        System.out.println("login_report:" + login_report);
      }

      String[] directory = reportFilename.split("/");

      File dir = new File(directory[directory.length - 2]);
      if (!dir.exists()) dir.mkdirs();

      BufferedWriter writer = new BufferedWriter(new FileWriter(reportFilename));
      writer.write(login_report);
      writer.close();
    } catch (IOException e) {
      System.err.println("Exception writeReport:" + e.getMessage());
    }
  }
}
