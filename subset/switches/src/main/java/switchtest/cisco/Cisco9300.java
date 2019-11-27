package switchtest.cisco;

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

import switchtest.SwitchInterrogator;

import java.util.HashMap;
import java.util.Map;

public class Cisco9300 extends SwitchInterrogator {

  private int commandIndex = 0;
  private boolean commandPending = false;
  // Cisco Terminal Prompt ends with # when enabled
  private String consolePromptEndingEnabled = "#";

  private StringBuilder rxData = new StringBuilder();

  public Cisco9300(String remoteIpAddress, int interfacePort, boolean deviceConfigPoeEnabled) {
    super(remoteIpAddress, interfacePort, deviceConfigPoeEnabled);
    telnetClientSocket =
        new CiscoSwitchTelnetClientSocket(remoteIpAddress, remotePort, this, debug);
    // TODO: enabled the user to input their own username and password
    this.username = "admin";
    this.password = "password";
    // Adjust commands to active switch configuration
    command[0] = command[0].replace("*", interfacePort + "");
    command[1] = command[1].replace("*", interfacePort + "");
  }

  public void receiveData(String data) {
    if (debug) {
      System.out.println(
          java.time.LocalTime.now() + "receiveDataLen:" + data.length() + "receiveData:" + data);
    }
    if (data != null) {
      if (!data.isEmpty()) {
        if (data.indexOf("--More--") > 0) {
          handleMore(data);
          return;
        } else {
          rxData.append(data);
        }
      }
      if (parseData(rxData.toString())) {
        // If we have processed the current buffers data we will clear the buffer
        rxData = new StringBuilder();
      }
    }
  }

  /**
   * If the message --More-- is present in the current data packet, this indicates the message is
   * incomplete. To complete the message, we need to tell the console to continue the response and
   * strip the --More-- entry from the data packet as it is not actually part of the response.
   *
   * @param consoleData Current unprocessed data packet
   */
  public void handleMore(String consoleData) {
    consoleData = consoleData.substring(0, consoleData.length() - "--More--".length());
    telnetClientSocket.writeData("\n");
    rxData.append(consoleData);
  }

  /**
   * Handles current data in the buffer read from the telnet console InputStream and sends it to the
   * appropriate process.
   *
   * @param consoleData Current unhandled data in the buffered reader
   * @return true if the data was an expected value and appropriately processed and return false if
   *     the data is not-expected.
   */
  public boolean parseData(String consoleData) {
    consoleData = consoleData.trim();
    ;
    if (!getUserAuthorised()) {
      return handleLoginMessage(consoleData);
    } else if (!getUserEnabled()) {
      return handleEnableMessage(consoleData);
    } else {
      // Logged in and enabled
      if (commandPending) {
        // Command has been sent and awaiting a response
        return handleCommandResponse(consoleData);
      } else if (command.length > commandIndex) {
        sendNextCommand();
        return true;
      } else {
        generateTestResults();
        writeReport();
        telnetClientSocket.disposeConnection();
      }
    }
    return false;
  }

  public void generateTestResults() {
    login_report += "\n";
    login_report += validateLinkTest();
    login_report += validateSpeedTests();
    login_report += validateDuplexTests();
    login_report += validatePowerTests();
  }

  public String validateLinkTest() {
    String testResults = "";
    if (interface_map.get("status").equals("connected")) {
      testResults += "RESULT pass connection.port_link\n";
    } else {
      testResults += "RESULT fail connection.port_link Link is down\n";
    }
    return testResults;
  }

  public String validateSpeedTests() {
    String testResults = "";
    if (interface_map.get("speed") != null) {
      String speed = interface_map.get("speed");
      if (speed.startsWith("a-")) { // Interface in Auto Speed
        speed = speed.replaceFirst("a-", "");
      }
      if (Integer.parseInt(speed) >= 10) {
        testResults += "RESULT pass connection.port_speed\n";
      } else {
        testResults += "RESULT fail connection.port_speed Speed is too slow\n";
      }
    } else {
      testResults += "RESULT fail connection.port_speed Cannot detect current speed\n";
    }
    return testResults;
  }

  public String validateDuplexTests() {
    String testResults = "";
    if (interface_map.get("duplex") != null) {
      String duplex = interface_map.get("duplex");
      if (duplex.startsWith("a-")) { // Interface in Auto Duplex
        duplex = duplex.replaceFirst("a-", "");
      }
      if (duplex.equals("full")) {
        testResults += "RESULT pass connection.port_duplex\n";
      } else {
        testResults += "RESULT fail connection.port_duplex Incorrect duplex mode set\n";
      }
    } else {
      testResults += "RESULT fail connection.port_duplex Cannot detect duplex mode\n";
    }
    return testResults;
  }

  public String validatePowerTests() {
    String testResults = "";
    double maxPower = 0;
    double currentPower = 0;
    boolean powerAuto = false;
    boolean poeDisabled = false;
    boolean poeOn = false;
    boolean poeOff = false;
    boolean poeFault = false;
    boolean poeDeny = false;
    try {
      // Generate test data from mapped results
      maxPower = Double.parseDouble(power_map.get("max"));
      currentPower = Double.parseDouble(power_map.get("power"));
      powerAuto = "auto".equals(power_map.get("admin"));
      poeDisabled = "off".equals(power_map.get("admin"));
      poeOn = "on".equals(power_map.get("oper"));
      poeOff = "off".equals(power_map.get("oper"));
      poeFault = "fault".equals(power_map.get("oper"));
      poeDeny = "power-deny".equals(power_map.get("oper"));
    } catch (Exception e) {
      // ToDo: Make these failures specific to the data resolve errors instead of all or nothing
      testResults += "RESULT fail poe.power Could not detect any current being drawn\n";
      testResults += "RESULT fail poe.negotiation Could not detect any current being drawn\n";
      testResults += "RESULT fail poe.support Could not detect any current being drawn\n";
    }

    if (!deviceConfigPoeEnabled) {
      testResults += "RESULT skip poe.power This test is disabled\n";
      testResults += "RESULT skip poe.negotiation This test is disabled\n";
      testResults += "RESULT skip poe.support This test is disabled\n";

    } else if (poeDisabled) {
      testResults += "RESULT skip poe.power The switch does not support PoE\n";
      testResults += "RESULT skip poe.negotiation The switch does not support PoE\n";
      testResults += "RESULT skip poe.support The switch does not support PoE\n";
    } else {

      // Determine PoE power test result
      if (maxPower >= currentPower && poeOn) {
        testResults += "RESULT pass poe.power\n";
      } else if (poeOff) {
        testResults += "RESULT fail poe.power No poE is applied\n";
      } else if (poeFault) {
        testResults +=
            "RESULT fail poe.power Device detection or a powered device is in a faulty state\n";
      } else if (poeDeny) {
        testResults +=
            "RESULT fail poe.power A powered device is detected, but no PoE is available, or the maximum wattage exceeds the detected powered-device maximum.\n";
      }

      // Determine PoE auto negotiation result
      if (powerAuto) {
        testResults += "RESULT pass poe.negotiation\n";
      } else {
        testResults += "RESULT fail poe.negotiation Incorrect privilege for negotiation\n";
      }

      // Determine PoE support result
      if (poeOn) {
        testResults += "RESULT pass poe.support\n";
      } else {
        testResults +=
            "RESULT fail poe.support The switch does not support PoE or it is disabled\n";
      }
    }

    return testResults;
  }

  public boolean handleCommandResponse(String consoleData) {
    if (consoleData == null) return false;
    if (consoleData.endsWith(getHostname() + consolePromptEndingEnabled)) {
      // Strip trailing command prompt
      String response =
          consoleData.substring(0, consoleData.length() - (getHostname() + "#").length());
      // Strip leading command that was sent
      response = response.substring(command[commandIndex].length()).trim();
      processCommandResponse(response);
      promptReady = true;
      commandPending = false;
      ++commandIndex;
      return true;
    }
    return false;
  }

  private void processCommandResponse(String response) {
    switch (commandIndex) {
      case 0: // show interface status
        processInterfaceStatus(response);
        break;
      case 1: // show power status
        processPowerStatus(response);
    }
  }

  public void processInterfaceStatus(String response) {
    interface_map = mapSimpleTable(response, show_interface_expected, interface_expected);
  }

  public void processPowerStatus(String response) {
    // Pre-process raw data to be map ready
    String[] lines = response.split("\n");
    response = lines[0] + " \n" + lines[3];
    power_map = mapSimpleTable(response, show_power_expected, power_expected);
  }

  /**
   * Map a simple table containing a header and 1 row of data to a hashmap
   *
   * @param rawPacket Raw table response from a switch command
   * @param colNames Array containing the names of the columns in the response
   * @param mapNames Array containing names key names to map values to
   * @return A HashMap containing the values mapped to the key names provided in the mapNames array
   */
  public HashMap<String, String> mapSimpleTable(
      String rawPacket, String[] colNames, String[] mapNames) {
    HashMap<String, String> colMap = new HashMap();
    String[] lines = rawPacket.split("\n");
    if (lines.length > 0) {
      String header = lines[0].trim();
      String values = lines[1].trim();
      for (int i = 0; i < colNames.length; ++i) {
        int startIx = header.indexOf(colNames[i]);
        int endIx = (i + 1) < colNames.length ? header.indexOf(colNames[i + 1]) : values.length();
        String curVal = values.substring(startIx, endIx).trim();
        colMap.put(mapNames[i], curVal);
      }
    }
    return colMap;
  }

  public void sendNextCommand() {
    telnetClientSocket.writeData(command[commandIndex] + "\n");
    System.out.println("Command Sent: " + command[commandIndex]);
    commandPending = true;
    promptReady = false;
  }

  public boolean handleLoginMessage(String consoleData) {
    if (consoleData == null) return false;
    if (consoleData.indexOf("Username:") >= 0) {
      telnetClientSocket.writeData(username + "\n");
      return true;
    } else if (consoleData.indexOf("Password:") >= 0) {
      telnetClientSocket.writeData(password + "\n");
      return true;
    } else if (consoleData.endsWith(">")) {
      setUserAuthorised(true);
      device_hostname = consoleData.split(">")[0];
      telnetClientSocket.writeData("enable\n");
      return true;
    } else if (consoleData.indexOf("% Bad passwords:") >= 0) {
      setUserAuthorised(false);
      return true;
    }
    return false;
  }

  public boolean handleEnableMessage(String consoleData) {
    if (consoleData == null) return false;
    if (consoleData.indexOf("Password:") >= 0) {
      telnetClientSocket.writeData(password + "\n");
      return true;
    } else if (consoleData.endsWith("#")) {
      setUserEnabled(true);
      return true;
    } else if (consoleData.indexOf("% Bad passwords:") >= 0) {
      setUserEnabled(false);
      return true;
    }
    return false;
  }

  public String[] commands() {
    return new String[] {
      "show interface gigabitethernet1/0/* status", "show power inline gigabitethernet1/0/*"
    };
  }

  public String[] commandToggle() {
    return new String[] {};
  }

  public String[] expected() {
    return new String[] {"login:", "Password:"};
  }

  public String[] interfaceExpected() {
    return new String[] {"interface", "name", "status", "vlan", "duplex", "speed", "type"};
  }

  public String[] loginExpected() {
    return new String[] {":", ">", ">"};
  }

  public String[] platformExpected() {
    return new String[] {};
  }

  public String[] powerExpected() {
    return new String[] {"dev_interface", "admin", "oper", "power", "device", "dev_class", "max"};
  }

  public String[] showInterfaceExpected() {
    return new String[] {"Port", "Name", "Status", "Vlan", "Duplex", "Speed", "Type"};
  }

  public String[] showInterfacePortExpected() {
    return new String[] {};
  }

  public String[] showPlatformExpected() {
    return new String[] {};
  }

  public String[] showPlatformPortExpected() {
    return new String[] {};
  }

  public String[] showPowerExpected() {
    return new String[] {"Interface", "Admin", "Oper", "Power", "Device", "Class", "Max"};
  }

  public String[] stackExpected() {
    return new String[] {};
  }

  public String[] showStackExpected() {
    return new String[1];
  }
}
