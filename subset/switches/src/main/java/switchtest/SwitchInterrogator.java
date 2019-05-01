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

public class SwitchInterrogator implements Runnable {
  SwitchTelnetClientSocket telnetClientSocket;
  Thread telnetClientSocketThread;

  String username = "manager";
  String password = "friend";

  String remoteIpAddress;
  int remotePort = 23;
  int interfacePort = 12;

  String reportFilename = "tmp/report.txt";

  int data_length = 32768;
  int shortPacketLength = 20;

  String[] expected = {
    "login:",
    "Password:",
    "Last login:",
    "#",
    "Login incorrect",
    "Connection closed by foreign host."
  };

  String[] command = {
    "enable",
    "show interface port1.0.",
    "show platform port port1.0.",
    "show power-inline interface port1.0.",
    "show run",
    "show stack"
  };

  String[] commandToggle = {"interface ethernet port1.0.", "shutdown", "no shutdown"};

  int interfacePos = 1;
  int platformPos = 2;
  int powerinlinePos = 3;

  HashMap<stack_expected, String> stack_map = new HashMap<stack_expected, String>();

  enum stack_expected {
    id,
    pending_id,
    mac_address,
    priority,
    status,
    role
  }

  String[] show_stack_expected = {
    "ID", "Pending ID", "MAC address", "Priority", "Status", "Role", "\n"
  };

  int[] show_stack_pointers = new int[show_stack_expected.length];
  String[] show_stack_data = new String[show_stack_expected.length - 1];

  HashMap<power_expected, String> power_map = new HashMap<power_expected, String>();

  enum power_expected {
    dev_interface,
    admin,
    pri,
    oper,
    power,
    device,
    dev_class,
    max
  }

  String[] show_power_expected = {
    "Interface", "Admin", "Pri", "Oper", "Power", "Device", "Class", "Max", "\n"
  };

  int[] show_power_pointers = new int[show_power_expected.length];
  String[] show_power_data = new String[show_power_expected.length - 1];

  HashMap<interface_expected, String> interface_map = new HashMap<interface_expected, String>();

  enum interface_expected {
    port_number,
    link_status,
    administrative_state,
    current_duplex,
    current_speed,
    current_polarity,
    configured_duplex,
    configured_speed,
    cofigured_polarity,
    left_chevron,
    input_packets,
    bytes,
    dropped,
    multicast_packets,
    output_packets,
    multicast_packets2,
    broadcast_packets,
    input_average_rate,
    output_average_rate,
    input_peak_rate,
    time_since_last_state_change
  };

  String[] show_interface_expected = {
    "port1",
    "\n",
    "Link is ",
    ",",
    "administrative state is ",
    "\n",
    "current duplex ",
    ",",
    "current speed ",
    ",",
    "current polarity ",
    "\n",
    "configured duplex ",
    ",",
    "configured speed ",
    ",",
    "configured polarity ",
    "\n",
    "<",
    "\n",
    "input packets ",
    ",",
    "bytes ",
    ",",
    "dropped ",
    ",",
    "multicast packets ",
    "\n",
    "output packets ",
    ",",
    "multicast packets ",
    ",",
    "broadcast packets ",
    "\n",
    "input average rate : ",
    "\n",
    "output average rate: ",
    "\n",
    "input peak rate ",
    "\n",
    "Time since last state change: ",
    "\n"
  };
  int[] show_interface_pointers = new int[show_interface_expected.length];
  String[] show_interface_data = new String[show_interface_expected.length / 2];

  String[] show_interface_port_expected = {"port1", "\n", "Time since last state change: ", "\n"};
  int[] show_interface_port_pointers = new int[show_interface_port_expected.length];

  HashMap<platform_expected, String> platform_map = new HashMap<platform_expected, String>();

  enum platform_expected {
    port_number,
    enabled,
    loopback,
    link,
    speed,
    max_speed,
    duplex,
    linkscan,
    autonegotiate,
    master,
    tx_pause,
    rx_pause,
    untagged_vlan,
    vlan_filter,
    stp_state,
    learn,
    discard,
    jam,
    max_frame_size,
    mc_disable_sa,
    mc_disable_ttl,
    mc_egress_untag,
    mc_egress_vid,
    mc_ttl_threshold
  }

  String[] show_platform_expected = {
    "port1",
    "\n",
    "enabled:",
    "\n",
    "loopback:",
    "\n",
    "link:",
    "\n",
    "speed:",
    "m",
    "max speed:",
    "\n",
    "duplex:",
    "\n",
    "linkscan:",
    "\n",
    "autonegotiate:",
    "\n",
    "master:",
    "\n",
    "tx pause:",
    "r",
    "rx pause:",
    "\n",
    "untagged vlan:",
    "\n",
    "vlan filter:",
    "\n",
    "stp state:",
    "\n",
    "learn:",
    "\n",
    "discard:",
    "\n",
    "jam:",
    "\n",
    "max frame size:",
    "\n",
    "MC Disable SA:",
    "\n",
    "MC Disable TTL:",
    "\n",
    "MC egress untag:",
    "\n",
    "MC egress vid:",
    "\n",
    "MC TTL threshold:",
    "\n"
  };
  int[] show_platform_pointers = new int[show_platform_expected.length];
  String[] show_platform_data = new String[show_platform_expected.length / 2];

  String[] show_platform_port_expected = {"port1", "\n", "MC TTL threshold:", "\n"};
  int[] show_platform_port_pointers = new int[show_platform_port_expected.length];

  int number_switch_ports = 1; // 48

  int requestFlag = 0;

  public int getRequestFlag() {
    return requestFlag - 1;
  }

  int count = 0;

  String device_hostname = "";

  String login_report = "";

  int continue_flag = 1;

  boolean debug = true;
  boolean switchSupportsPoe = false;
  boolean extendedTests = false;

  public SwitchInterrogator(String remoteIpAddress, int interfacePort, boolean switchSupportsPoe) {
    this.remoteIpAddress = remoteIpAddress;
    this.interfacePort = interfacePort;
    this.switchSupportsPoe = switchSupportsPoe;
    command[interfacePos] = command[interfacePos] + interfacePort;
    command[platformPos] = command[platformPos] + interfacePort;
    command[powerinlinePos] = command[powerinlinePos] + interfacePort;
  }

  @Override
  public void run() {
    System.out.println("Interrogator new connection...");
    telnetClientSocket = new SwitchTelnetClientSocket(remoteIpAddress, remotePort, this, debug);
    telnetClientSocketThread = new Thread(telnetClientSocket);
    telnetClientSocketThread.start();
  }

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
    }
  }

  private void parse_inline(
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
    }
  }

  private int recursive_data(String data, String search, int start) {
    int pointer = data.indexOf(search, start);
    if (debug) {
      System.out.println("pointer:" + pointer);
    }
    return pointer;
  }

  public String getHostname() {
    return device_hostname;
  }

  boolean userAuthorised = false;
  boolean userEnabled = false;

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
              int requestFinish = 0;
              if (switchSupportsPoe) {
                requestFinish = 4;
              } else {
                requestFinish = 3;
              }
              if (extendedTests) {
                requestFinish = command.length;
              }
              if (requestFlag < requestFinish) {
                telnetClientSocket.writeData(command[requestFlag] + "\n");
                System.out.println(
                    "command:" + command[requestFlag] + " request_flag:" + requestFlag);
                requestFlag += 1;
              } else {
                System.out.println("finished running configuration requests");
                // addDataToMaps(requestFinish);
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

  private void parseRequestFlag(String data, int requestFlag) {
    try {
      switch (requestFlag) {
        case 2:
          // parse show interface
          login_report += "show interface:\n";
          parse_packet(data, show_interface_port_expected, show_interface_port_pointers);
          writeReport();
          telnetClientSocket.writeData("\n");
          break;
        case 3:
          // parse show platform
          login_report += "\nshow platform:\n";
          parse_packet(data, show_platform_port_expected, show_platform_port_pointers);
          writeReport();
          telnetClientSocket.writeData("\n");
          break;
        case 4:
          // parse show power-inline
          login_report += "\nshow power-inline:\n";
          data = trash_line(trash_line(data, 0), 1);
          parse_inline(data, show_power_expected, show_power_pointers, show_power_data);
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

  private void validateTests() {
    try {
      login_report += "\n";
      String link_status = show_interface_data[1];
      String dropped_packets = show_interface_data[12];

      String current_speed = show_interface_data[4];
      String configured_speed = show_interface_data[7];

      String current_duplex = show_interface_data[3];
      String configured_duplex = show_interface_data[6];

      String current_max_power = show_power_data[7];
      String current_power = show_power_data[4];
      String current_PoE_admin = show_power_data[1];

      if (link_status.equals("UP") && Integer.parseInt(dropped_packets) == 0) {
        login_report += "RESULT connection.port_link=true\n";
      } else {
        login_report += "RESULT connection.port_link=false\n";
      }

      if (current_speed != null) {
        if (configured_speed.equals("auto") && Integer.parseInt(current_speed) >= 10) {
          login_report += "RESULT connection.port_speed=true\n";
        } else {
          login_report += "RESULT connection.port_speed=false\n";
        }
      } else {
        login_report += "RESULT connection.port_speed=false\n";
      }

      if (current_duplex != null) {
        if (configured_duplex.equals("auto") && current_duplex.equals("full")) {
          login_report += "RESULT connection.port_duplex=true\n";
        } else {
          login_report += "RESULT connection.port_duplex=false\n";
        }
      } else {
        login_report += "RESULT connection.port_duplex=false\n";
      }

      if (switchSupportsPoe) {
        if (Integer.parseInt(current_max_power) > Integer.parseInt(current_power)
            && current_PoE_admin.equals("Enabled")) {
          login_report += "RESULT poe.power=true\n";
          login_report += "RESULT poe.negotiation=true\n";
          login_report += "RESULT poe.support=true\n";
        } else {
          login_report += "RESULT poe.power=false\n";
          login_report += "RESULT poe.negotiation=false\n";
          login_report += "RESULT poe.support=false\n";
        }
      }

      writeReport();
    } catch (Exception e) {
      System.err.println("Exception validateTests:" + e.getMessage());
    }
  }

  private void writeReport() {
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