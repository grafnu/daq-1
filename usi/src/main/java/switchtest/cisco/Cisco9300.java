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

import grpc.*;
import switchtest.ResponseHandler;
import switchtest.SwitchController;

import java.util.*;
import java.util.stream.Collectors;

public class Cisco9300 extends SwitchController {

    private static final String[] interfaceExpected = {"interface", "name", "status", "vlan", "duplex", "speed", "type"};
    private static final String[] showInterfaceExpected = {"Port", "Name", "Status", "Vlan", "Duplex", "Speed", "Type"};
    private static final Map<String, String> powerInlineMap = Map.of("Interface", "dev_interface",
            "Inline Power Mode", "admin",
            "Operational status", "oper",
            "Measured at the port", "power",
            "Device Type", "device",
            "IEEE Class", "dev_class",
            "Power available to the device", "max");
    private static final Map<String, POEStatus> poeStatusMap = Map.of("on", POEStatus.ON,
            "off", POEStatus.OFF, "fault", POEStatus.FAULT, "power-deny", POEStatus.DENY);
    private static final Map<String, POESupport> poeSupportMap = Map.of("auto", POESupport.ENABLED,
            "off", POESupport.DISABLED);
    private static final int WAIT_MS = 100;
    private ResponseHandler<String> responseHandler;

    public Cisco9300(
            String remoteIpAddress,
            int telnetPort,
            String user,
            String password) {
        super(remoteIpAddress, telnetPort, user, password);
        this.username = user == null ? "admin" : user;
        this.password = password == null ? "password" : password;
    }

    /**
     * Generic Cisco Switch command to retrieve the Status of an interface.
     */
    private String showIfaceStatusCommand(int interfacePort) {
        return "show interface gigabitethernet1/0/" + interfacePort + " status";
    }

    /**
     * Generic Cisco Switch command to retrieve the Power Status of an interface. Replace asterisk
     * with actual port number for complete message
     */
    private String showIfacePowerStatusCommand(int interfacePort) {
        return "show power inline gigabitethernet1/0/" + interfacePort + " detail";
    }

    /**
     * @param interfacePort
     * @param enabled       for bringing up/down interfacePort
     * @return
     */
    private String[] portManagementCommand(int interfacePort, boolean enabled) {
        return new String[]{
                "configure terminal",
                "interface FastEthernet0/" + interfacePort,
                (enabled ? "no " : "") + "shutdown",
                "end"
        };
    }

    /**
     * Handles the process when using the enter command. Enable is a required step before commands can
     * be sent to the switch.
     *
     * @param consoleData Raw console data received the the telnet connection.
     */
    @Override
    public void handleEnableMessage(String consoleData) throws Exception {
        if (consoleData.indexOf("Password:") >= 0) {
            telnetClientSocket.writeData(password + "\n");
        } else if (containsPrompt(consoleData)) {
            userEnabled = true;
        } else if (consoleData.indexOf("% Bad passwords") >= 0) {
            telnetClientSocket.disposeConnection();
            throw new Exception("Could not Enable the User, Bad Password");
        }
    }

    /**
     * Handles the process when logging into the switch.
     *
     * @param consoleData Raw console data received the the telnet connection.
     */
    @Override
    public void handleLoginMessage(String consoleData) throws Exception {
        if (consoleData.indexOf("Username:") >= 0) {
            telnetClientSocket.writeData(username + "\n");
        } else if (consoleData.indexOf("Password:") >= 0) {
            telnetClientSocket.writeData(password + "\n");
        } else if (consoleData.endsWith(CONSOLE_PROMPT_ENDING_LOGIN)) {
            userAuthorised = true;
            hostname = consoleData.split(CONSOLE_PROMPT_ENDING_LOGIN)[0];
            telnetClientSocket.writeData("enable\n");
        } else if (consoleData.indexOf("% Login invalid") >= 0) {
            telnetClientSocket.disposeConnection();
            throw new Exception("Failed to Login, Login Invalid");
        } else if (consoleData.indexOf("% Bad passwords") >= 0) {
            telnetClientSocket.disposeConnection();
            throw new Exception("Failed to Login, Bad Password");
        }
    }

    /**
     * Handles current data in the buffer read from the telnet console InputStream and sends it to the
     * appropriate process.
     *
     * @param consoleData Current unhandled data in the buffered reader
     */
    @Override
    public void parseData(String consoleData) throws Exception {
        if (commandPending) {
            responseHandler.receiveData(consoleData);
        }
    }

    @Override
    public void getPower(int devicePort, ResponseHandler<Power> powerResponseHandler) throws Exception {
        while (commandPending) {
            Thread.sleep(WAIT_MS);
        }
        String command = showIfacePowerStatusCommand(devicePort);
        synchronized (this) {
            commandPending = true;
            responseHandler = data -> {
                Map<String, String> powerMap = processPowerStatusInline(data);
                powerResponseHandler.receiveData(buildPowerResponse(powerMap));
                synchronized (this) {
                    commandPending = false;
                }
            };
            telnetClientSocket.writeData(command + "\n");
        }
    }

    @Override
    public void getInterface(int devicePort, ResponseHandler<Interface> handler) throws Exception {
        while (commandPending) {
            Thread.sleep(WAIT_MS);
        }
        String command = showIfaceStatusCommand(devicePort);
        synchronized (this) {
            commandPending = true;
            responseHandler = data -> {
                Map<String, String> interfaceMap = processInterfaceStatus(data);
                handler.receiveData(buildInterfaceResponse(interfaceMap));
                synchronized (this) {
                    commandPending = false;
                }
            };
            telnetClientSocket.writeData(command + "\n");
        }
    }

    private void managePort(int devicePort, ResponseHandler<SwitchActionResponse> handler, boolean enabled) throws Exception {
        while (commandPending) {
            Thread.sleep(WAIT_MS);
        }
        Queue<String> commands = new LinkedList<>(Arrays.asList(portManagementCommand(devicePort, enabled)));
        SwitchActionResponse.Builder response = SwitchActionResponse.newBuilder();
        synchronized (this) {
            commandPending = true;
            responseHandler = data -> {
                if (!commands.isEmpty()) {
                    telnetClientSocket.writeData(commands.poll() + "\n");
                    return;
                }
                synchronized (this) {
                    commandPending = false;
                    handler.receiveData(response.setSuccess(true).build());
                }
            };
            telnetClientSocket.writeData(commands.poll() + "\n");
        }
    }

    @Override
    public void connect(int devicePort, ResponseHandler<SwitchActionResponse> handler) throws Exception {
        managePort(devicePort, handler, true);
    }

    @Override
    public void disconnect(int devicePort, ResponseHandler<SwitchActionResponse> handler) throws Exception {
        managePort(devicePort, handler, false);
    }

    private Interface buildInterfaceResponse(Map<String, String> interfaceMap) {
        Interface.Builder response = Interface.newBuilder();
        String duplex = interfaceMap.getOrDefault("duplex", "");
        if (duplex != null && duplex.startsWith("a-")) { // Interface in Auto Duplex
            duplex = duplex.replaceFirst("a-", "");
        }

        String speed = interfaceMap.getOrDefault("speed", "");
        if (speed != null && speed.startsWith("a-")) { // Interface in Auto Speed
            speed = speed.replaceFirst("a-", "");
        }

        String linkStatus = interfaceMap.getOrDefault("status", "");
        return response.setLinkStatus(linkStatus.equals("connected") ? LinkStatus.UP : LinkStatus.DOWN)
                .setDuplex(duplex)
                .setLinkSpeed(Integer.parseInt(speed))
                .build();
    }

    private Power buildPowerResponse(Map<String, String> powerMap) {
        Power.Builder response = Power.newBuilder();
        float maxPower = Float.parseFloat(powerMap.get("max"));
        float currentPower = Float.parseFloat(powerMap.get("power"));

        String poeSupport = powerMap.getOrDefault("admin", null);
        String poeStatus = powerMap.getOrDefault("oper", null);
        return response.setPoeStatus(poeStatusMap.getOrDefault(poeStatus, null))
                .setPoeSupport(poeSupportMap.getOrDefault(poeSupport, null))
                .setMaxPowerConsumption(maxPower)
                .setCurrentPowerConsumption(currentPower).build();
    }

    private Map<String, String> processInterfaceStatus(String response) {
        String filtered = Arrays.stream(response.split("\n"))
                .filter(s -> !containsPrompt(s))
                .collect(Collectors.joining("\n"));
        Map<String, String> interfaceMap = mapSimpleTable(filtered, showInterfaceExpected, interfaceExpected);
        return interfaceMap;
    }

    private Map<String, String> processPowerStatusInline(String response) {
        Map<String, String> powerMap = new HashMap<>();
        Arrays.stream(response.split("\n"))
                .forEach(
                        line -> {
                            String[] lineParts = line.trim().split(":");
                            if (lineParts.length > 1) {
                                String powerMapKey = powerInlineMap.getOrDefault(lineParts[0], null);
                                if (powerMapKey != null) {
                                    powerMap.put(powerMapKey, lineParts[1].trim());
                                }
                            }
                        });
        return powerMap;
    }


}
