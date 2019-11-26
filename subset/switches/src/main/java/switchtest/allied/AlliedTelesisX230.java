package switchtest.allied;

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

public class AlliedTelesisX230 extends SwitchInterrogator {

  public AlliedTelesisX230(
      String remoteIpAddress, int interfacePort, boolean deviceConfigPoeEnabled) {
    super(remoteIpAddress, interfacePort, deviceConfigPoeEnabled);
    // Adjust commands to active switch configuration
    command[interfacePos] = command[interfacePos] + interfacePort;
    command[platformPos] = command[platformPos] + interfacePort;
    command[powerinlinePos] = command[powerinlinePos] + interfacePort;
  }

  @Override
  public void run() {
    System.out.println("Interrogator new connection...");
    telnetClientSocket =
        new AlliedSwitchTelnetClientSocket(remoteIpAddress, remotePort, this, debug);
    telnetClientSocketThread = new Thread(telnetClientSocket);
    telnetClientSocketThread.start();
  }

  public boolean processConsoleMessage(String consoleData) {
    return true;
  }

  public String[] commands() {
    return new String[] {
      "enable",
      "show interface port1.0.",
      "show platform port port1.0.",
      "show power-inline interface port1.0.",
      "show run",
      "show stack"
    };
  }

  public String[] commandToggle() {
    return new String[] {"interface ethernet port1.0.", "shutdown", "no shutdown"};
  }

  public String[] expected() {
    return new String[] {
      "login:",
      "Password:",
      "Last login:",
      "#",
      "Login incorrect",
      "Connection closed by foreign host."
    };
  }

  public String[] interfaceExpected() {
    return new String[] {
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
  }

  public String[] loginExpected() {
    return new String[] {":", ":", ">"};
  }

  public String[] platformExpected() {
    return new String[] {
      "port_number",
      "enabled",
      "loopback",
      "link",
      "speed",
      "max_speed",
      "duplex",
      "linkscan",
      "autonegotiate",
      "master",
      "tx_pause",
      "rx_pause",
      "untagged_vlan",
      "vlan_filter",
      "stp_state",
      "learn",
      "discard",
      "jam",
      "max_frame_size",
      "mc_disable_sa",
      "mc_disable_ttl",
      "mc_egress_untag",
      "mc_egress_vid",
      "mc_ttl_threshold"
    };
  }

  public String[] powerExpected() {
    return new String[] {
      "dev_interface", "admin", "pri", "oper", "power", "device", "dev_class", "max"
    };
  }

  public String[] showInterfaceExpected() {
    return new String[] {
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
  }

  public String[] showInterfacePortExpected() {
    return new String[] {"port1", "\n", "Time since last state change: ", "\n"};
  }

  public String[] showPlatformExpected() {
    return new String[] {
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
  }

  public String[] showPlatformPortExpected() {
    return new String[] {"port1", "\n", "MC TTL threshold:", "\n"};
  }

  public String[] showPowerExpected() {
    return new String[] {
      "Interface", "Admin", "Pri", "Oper", "Power", "Device", "Class", "Max", "\n"
    };
  }

  public String[] stackExpected() {
    return new String[] {"id", "pending_id", "mac_address", "priority", "status", "role"};
  }

  public String[] showStackExpected() {
    return new String[] {"ID", "Pending ID", "MAC address", "Priority", "Status", "Role", "\n"};
  }
}
