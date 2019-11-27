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
  protected boolean switchSupportsPoe = false;

  private boolean userAuthorised = false;
  private boolean userEnabled = false;

  protected String device_hostname = "";

  protected int shortPacketLength = 20;

  protected int requestFlag = 0;

  protected String login_report = "";
  protected boolean extendedTests = false;
  protected int number_switch_ports = 1; // 48

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
  public void run() {
    telnetClientSocketThread = new Thread(telnetClientSocket);
    telnetClientSocketThread.start();
  }

  public abstract void receiveData(String data);

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
