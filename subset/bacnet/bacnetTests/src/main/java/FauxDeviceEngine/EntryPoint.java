package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
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

public class EntryPoint {

    private static final int deviceId = (int) Math.floor(Math.random() * 1000.0);
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
        System.out.println("Creating LoopDevice id " + deviceId);
        Transport transport = new Transport(network);
        transport.setTimeout(1000);

        try {
            localDevice = new LocalDevice(deviceId, transport);
            localDevice.getConfiguration().setProperty(PropertyIdentifier.modelName,
                    new CharacterString("BACnet4J LoopDevice"));
            System.out.println("Local device is running with device id " + deviceId);
            JSONArray bacnetObjectArray = readJSONFile();
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
        JSON json = new JSON(absolute_path + "tmp/" + jsonFile);
        JSONArray bacnetObjectTypesList = json.read();
        return bacnetObjectTypesList;
    }

    private static void addBacnetProperties(JSONArray bacnetObjectsList) {
        bacnetObjectsList.forEach( bacnetObject -> addProperty((JSONObject) bacnetObject));
    }

    private static void addProperty(JSONObject bacnetObject) {
        try {
            List<String> bacnetObjectTypeArr = new ArrayList<>(bacnetObject.keySet());
            String bacnetObjectType = bacnetObjectTypeArr.get(0);
            if(bacnetObjectType.contains("AnalogInput")){
                BACnetObject bacnetType = new BACnetObject(localDevice, localDevice.getNextInstanceObjectIdentifier(ObjectType.analogInput), false);
                Map<String, String > map = (Map<String, String>) bacnetObject.get(bacnetObjectType);
                new Analog(localDevice, bacnetType, map);
            } else if(bacnetObjectType.contains("AnalogOutput")) {
                BACnetObject bacnetType = new BACnetObject(localDevice, localDevice.getNextInstanceObjectIdentifier(ObjectType.analogOutput), false);
                Map<String, String > map = (Map<String, String>) bacnetObject.get(bacnetObjectType);
                new Analog(localDevice, bacnetType, map);
            } else if(bacnetObjectType.contains("AnalogValue")) {
                BACnetObject bacnetType = new BACnetObject(localDevice, localDevice.getNextInstanceObjectIdentifier(ObjectType.analogValue), false);
                Map<String, String > map = (Map<String, String>) bacnetObject.get(bacnetObjectType);
                new Analog(localDevice, bacnetType, map);
            }
            else if(bacnetObjectType.contains("BinaryInput")) {
                BACnetObject bacnetType = new BACnetObject(localDevice, localDevice.getNextInstanceObjectIdentifier(ObjectType.binaryInput), false);
                Map<String, String > map = (Map<String, String>) bacnetObject.get(bacnetObjectType);
                new Binary(localDevice, bacnetType, map);
            } else if(bacnetObjectType.contains("BinaryOutput")) {
                BACnetObject bacnetType = new BACnetObject(localDevice, localDevice.getNextInstanceObjectIdentifier(ObjectType.binaryOutput), false);
                Map<String, String > map = (Map<String, String>) bacnetObject.get(bacnetObjectType);
                new Binary(localDevice, bacnetType, map);
            } else if(bacnetObjectType.contains("BinaryValue")) {
                BACnetObject bacnetType = new BACnetObject(localDevice, localDevice.getNextInstanceObjectIdentifier(ObjectType.binaryValue), false);
                Map<String, String > map = (Map<String, String>) bacnetObject.get(bacnetObjectType);
                new Binary(localDevice, bacnetType, map);
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
