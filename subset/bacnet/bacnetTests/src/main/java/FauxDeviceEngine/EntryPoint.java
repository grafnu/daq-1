package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import helper.FileManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntryPoint implements IBacnetObjectInitializer {

    private static int deviceId = 0;
    private static IpNetwork network;
    private static LocalDevice localDevice;
    private static String testCase = "";

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new RuntimeException("Usage: testCase localIpAddr broadcastIpAddr");
        }
        testCase = args[0];
        String localIpAddr = args[1];
        String broadcastIpAddr = args[2];
        int port = IpNetwork.DEFAULT_PORT;
        network = new IpNetwork(broadcastIpAddr, port,
                IpNetwork.DEFAULT_BIND_IP, 0, localIpAddr);
        Transport transport = new Transport(network);
        transport.setTimeout(1000);

        try {
            JSONArray bacnetObjectArray = readJSONFile();
            getDeviceID(bacnetObjectArray);
            if(deviceId == 0) {
                System.out.println("Device ID not found in JSON file. Generating random ID...");
                deviceId = (int) Math.floor(Math.random() * 1000.0);
            }
            System.out.println("Creating LoopDevice id " + deviceId);
            localDevice = new LocalDevice(deviceId, transport);
            localDevice.getConfiguration().setProperty(PropertyIdentifier.modelName,
                    new CharacterString("BACnet4J LoopDevice"));
            System.out.println("Local device is running with device id " + deviceId);
            addBacnetProperties(bacnetObjectArray);
            localDevice.initialize();
            System.out.println("Device initialized...");

        } catch (RuntimeException e) {
            System.out.println("Ex in LoopDevice() ");
            e.printStackTrace();
            localDevice.terminate();
            localDevice = null;
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONArray readJSONFile() {
        String jsonFile = "";
        if (testCase.equals("pass")) {
            jsonFile = "Faux-Device-Pass.json";
        } else if (testCase.equals("fail")) {
            jsonFile = "Faux-Device-Fail.json";
        }
        FileManager fileManager = new FileManager();
        String absolute_path = fileManager.getAbsolutePath();
        System.out.println(absolute_path + "tmp/" + jsonFile);
        JSON json = new JSON(absolute_path + "tmp/" + jsonFile);
        JSONArray bacnetObjectTypesList = json.read();
        return bacnetObjectTypesList;
    }

    private static void addBacnetProperties(JSONArray bacnetObjectsList) {
        bacnetObjectsList.forEach(bacnetObject -> addProperty((JSONObject) bacnetObject));
    }

    private static void getDeviceID(JSONArray bacnetObjectsList) {
        bacnetObjectsList.forEach(bacnetObject -> getID((JSONObject) bacnetObject));
    }

    private static void getID(JSONObject bacnetObject) {
        List<String> bacnetObjectTypeArr = new ArrayList<>(bacnetObject.keySet());
        String bacnetObjectType = bacnetObjectTypeArr.get(0);
        if(bacnetObjectType.contains("DeviceID")) {
            String IDString = (String) bacnetObject.get(bacnetObjectType);
            int DeviceID = Integer.parseInt(IDString);
            System.out.println("Device ID found in JSON file.");
            deviceId = DeviceID;
        }
    }

    private static void addProperty(JSONObject bacnetObject) {
        IBacnetObjectInitializer bacnet = new EntryPoint();
        try {
            List<String> bacnetObjectTypeArr = new ArrayList<>(bacnetObject.keySet());
            String bacnetObjectType = bacnetObjectTypeArr.get(0);
            if (bacnetObjectType.contains("AnalogInput")) {
                bacnet.initializeAnalogObject(localDevice, ObjectType.analogInput, bacnetObject,
                        bacnetObjectType, false);
            } else if (bacnetObjectType.contains("AnalogOutput")) {
                bacnet.initializeAnalogObject(localDevice, ObjectType.analogOutput, bacnetObject,
                        bacnetObjectType, false);
            } else if (bacnetObjectType.contains("AnalogValue")) {
                bacnet.initializeAnalogObject(localDevice, ObjectType.analogValue, bacnetObject,
                        bacnetObjectType, false);
            } else if (bacnetObjectType.contains("BinaryInput")) {
                bacnet.initializeBinaryObject(localDevice, ObjectType.binaryInput, bacnetObject,
                        bacnetObjectType, false);
            } else if (bacnetObjectType.contains("BinaryOutput")) {
                bacnet.initializeBinaryObject(localDevice, ObjectType.binaryOutput, bacnetObject,
                        bacnetObjectType, false);
            } else if (bacnetObjectType.contains("BinaryValue")) {
                bacnet.initializeBinaryObject(localDevice, ObjectType.binaryValue, bacnetObject,
                        bacnetObjectType, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void initializeAnalogObject(LocalDevice localDevice, ObjectType objectType, JSONObject bacnetObject,
                                       String bacnetObjectType, boolean setDefaultValues) {
        BACnetObject bacnetType;
        try {
            bacnetType = initialiseBacnetObject(localDevice, objectType, setDefaultValues);
            Map<String, String> map = getObejctMap(bacnetObject, bacnetObjectType);
            new Analog(localDevice, bacnetType, map);
        } catch (BACnetServiceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initializeBinaryObject(LocalDevice localDevice, ObjectType objectType, JSONObject bacnetObject,
                                       String bacnetObjectType, boolean setDefaultValues) {
        BACnetObject bacnetType;
        try {
            bacnetType = initialiseBacnetObject(localDevice, objectType, setDefaultValues);
            Map<String, String> map = getObejctMap(bacnetObject, bacnetObjectType);
            new Binary(localDevice, bacnetType, map);
        } catch (BACnetServiceException e) {
            e.printStackTrace();
        }
    }

    private BACnetObject initialiseBacnetObject(LocalDevice localDevice, ObjectType objectType,
                                                boolean setDefaultValues) throws BACnetServiceException {
        BACnetObject bacnetType = null;
        bacnetType = new BACnetObject(localDevice, localDevice.getNextInstanceObjectIdentifier(objectType),
                setDefaultValues);
        return bacnetType;
    }

    private Map<String, String> getObejctMap(JSONObject bacnetObject, String bacnetObjectType) {
        return (Map<String, String>) bacnetObject.get(bacnetObjectType);
    }
}
