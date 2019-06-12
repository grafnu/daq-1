package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.EventTransitionBits;
import com.serotonin.bacnet4j.type.constructed.LimitEnable;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import java.util.Map;

public class Analog {

    private static float presentValue = 0.1f;
    private static String objectName = "device_run_command";
    private static String deviceType = "";
    private static float deadband = 14;
    private static boolean outOfService = false;
    private static float resolution = 0.1f;
    private static boolean[] eventEnable = {true, true, false};
    private static int eventState = 0;
    private static int objectType = 0;
    private static int timeDelayNormal = 0;
    private static float lowLimit = 0;
    private static boolean[] limitEnable = {false, false};
    private static float covIncrement = 1.0f;
    private static boolean[] statusFlags = {false, false, false, false};
    private static int updateInterval = 1000;
    private static boolean[] ackedTransitions = {true, true, true};
    private static float highLimit = 0;
    private static int notifyType = 0;
    private static boolean eventDetectionEnable = false;
    private static float minPresValue = 100.90f;
    private static float maxPresValue = 150.96f;
    private static int reliability = 4;
    private static SequenceOf<EventTransitionBits> eventTransitionBits = new SequenceOf<EventTransitionBits>();
    private static int notificationClass = 0;
    private static String description = "Faux Device";
    private static boolean eventAlgorithmInhibit = false;
    private static int units = 62;
    private static String profileName = "";

    public Analog(LocalDevice localDevice, BACnetObject bacnetObjectType, Map<String, String>bacnetObjectMap) {
        for(Map.Entry<String, String> map : bacnetObjectMap.entrySet()) {
            String propertyName = map.getKey();
            String propertyValue = map.getValue();
            addObjectProperty(bacnetObjectType, propertyName, propertyValue);
        }
        addObjectType(localDevice, bacnetObjectType, bacnetObjectMap);
    }

    private void addObjectProperty(BACnetObject bacnetObjectType, String objectProperty, String propertyValue) {
        Encodable encodable;
        switch (objectProperty) {
            case "Present_Value":
                presentValue = Float.parseFloat(propertyValue);
                encodable = new Real(presentValue);
                add(bacnetObjectType, PropertyIdentifier.presentValue, encodable);
                break;
            case "Object_Name":
                objectName = propertyValue;
                encodable = new CharacterString(objectName);
                add(bacnetObjectType, PropertyIdentifier.objectName, encodable);
                break;
            case "Device_Type":
                deviceType = propertyValue;
                encodable = new CharacterString(deviceType);
                add(bacnetObjectType, PropertyIdentifier.deviceType, encodable);
                break;
            case "Deadband":
                deadband = Float.parseFloat(propertyValue);
                encodable = new Real(deadband);
                add(bacnetObjectType, PropertyIdentifier.deadband, encodable);
                break;
            case "Out_Of_Service":
                outOfService = Boolean.valueOf(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService);
                add(bacnetObjectType, PropertyIdentifier.outOfService, encodable);
                break;
            case "Resolution" :
                resolution = Float.parseFloat(propertyValue);
                encodable = new Real(resolution);
                add(bacnetObjectType, PropertyIdentifier.resolution, encodable);
                break;
            case "Event_Enable":
                eventEnable = castToArrayBoolean(propertyValue);
                encodable = new EventTransitionBits(eventEnable[0], eventEnable[1], eventEnable[2]);
                add(bacnetObjectType, PropertyIdentifier.eventEnable, encodable);
                break;
            case "Event_State":
                eventState = Integer.parseInt(propertyValue);
                encodable = new EventState(eventState);
                add(bacnetObjectType, PropertyIdentifier.eventState, encodable);
                break;
            case "Object_Type":
                objectType = Integer.parseInt(propertyValue);
                encodable = new ObjectType(objectType);
                add(bacnetObjectType, PropertyIdentifier.objectType, encodable);
                break;
            case "Time_Delay_Normal":
                timeDelayNormal = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(timeDelayNormal);
                add(bacnetObjectType, PropertyIdentifier.timeDelayNormal, encodable);
                break;
            case "Low_Limit":
                lowLimit = Float.parseFloat(propertyValue);
                encodable = new Real(lowLimit);
                add(bacnetObjectType, PropertyIdentifier.lowLimit, encodable);
                break;
            case "Limit_Enable":
                limitEnable = castToArrayBoolean(propertyValue);
                encodable = new LimitEnable(limitEnable[0], limitEnable[1]);
                add(bacnetObjectType, PropertyIdentifier.limitEnable, encodable);
                break;
            case "Cov_Increment":
                covIncrement = Float.parseFloat(propertyValue);
                encodable = new Real(covIncrement);
                add(bacnetObjectType, PropertyIdentifier.covIncrement, encodable);
                break;
            case "Status_Flags":
                statusFlags = castToArrayBoolean(propertyValue);
                encodable = new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3]);
                add(bacnetObjectType, PropertyIdentifier.statusFlags, encodable);
                break;
            case "Update_Interval":
                updateInterval = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(updateInterval);
                add(bacnetObjectType, PropertyIdentifier.updateInterval, encodable);
                break;
            case "Acked_Transitions":
                ackedTransitions = castToArrayBoolean(propertyValue);
                encodable = new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2]);
                add(bacnetObjectType, PropertyIdentifier.ackedTransitions, encodable);
                break;
            case "High_Limit":
                highLimit = Float.parseFloat(propertyValue);
                encodable = new Real(highLimit);
                add(bacnetObjectType, PropertyIdentifier.highLimit, encodable);
                break;
            case "Notify_Type":
                notifyType = Integer.parseInt(propertyValue);
                encodable = new NotifyType(notifyType);
                add(bacnetObjectType, PropertyIdentifier.notifyType, encodable);
                break;
            case "Event_Detection_Enable":
                eventDetectionEnable = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventDetectionEnable);
                add(bacnetObjectType, PropertyIdentifier.eventDetectionEnable, encodable);
                break;
            case "Max_Pres_Value":
                maxPresValue = Float.parseFloat(propertyValue);
                encodable = new Real(maxPresValue);
                add(bacnetObjectType, PropertyIdentifier.maxPresValue, encodable);
                break;
            case "Min_Pres_Value":
                minPresValue = Float.parseFloat(propertyValue);
                encodable = new Real(minPresValue);
                add(bacnetObjectType, PropertyIdentifier.minPresValue, encodable);
                break;
            case "Reliability":
                reliability = Integer.parseInt(propertyValue);
                encodable = new Reliability(reliability);
                add(bacnetObjectType, PropertyIdentifier.reliability, encodable);
                break;
            case "Event_Message_Texts":
                if(Boolean.parseBoolean(propertyValue)) {
                    eventTransitionBits = new SequenceOf<EventTransitionBits>();
                    encodable = eventTransitionBits;
                add(bacnetObjectType, PropertyIdentifier.eventMessageTexts, encodable);
                }
                break;
            case "Notification_Class":
                notificationClass = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(notificationClass);
                add(bacnetObjectType, PropertyIdentifier.notificationClass, encodable);
                break;
            case "Description":
                description = propertyValue;
                encodable = new CharacterString(description);
                add(bacnetObjectType, PropertyIdentifier.description, encodable);
                break;
            case "Event_Algorithm_Inhibit":
                eventAlgorithmInhibit = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventAlgorithmInhibit);
                add(bacnetObjectType, PropertyIdentifier.eventAlgorithmInhibit, encodable);
                break;
            case "Units":
                units = Integer.parseInt(propertyValue);
                encodable = new EngineeringUnits(units);
                add(bacnetObjectType, PropertyIdentifier.units, encodable);
                break;
            case "Profile_Name":
                profileName = propertyValue;
                encodable = new CharacterString(profileName);
                add(bacnetObjectType, PropertyIdentifier.profileName, encodable);
                break;

                default:
                System.out.println(objectProperty + " not found.");
        }
    }

    private void add(BACnetObject bacnetObjectType, PropertyIdentifier propertyIdentifier, Encodable encodable) {
        try {
            bacnetObjectType.setProperty(propertyIdentifier, encodable);
        } catch (BACnetServiceException e) {
            e.printStackTrace();
            System.out.println("Error adding bacnet property: " + e.getMessage() + propertyIdentifier.toString());
        }
    }

    private void addObjectType(LocalDevice localDevice, BACnetObject bacnetObject, Map<String, String>map) {
        try {
            localDevice.addObject(bacnetObject);
        } catch (BACnetServiceException e) {
            System.out.println("Error adding bacnet object: " + e.getMessage() + " " + map.toString());
        }
    }

    private boolean[] castToArrayBoolean(String string_of_booleans) {
        String[] booleans_arr = string_of_booleans.split(" ");
        boolean[] array = new boolean[booleans_arr.length];
        for (int i = 0; i < booleans_arr.length; i++) {
            array[i] = Boolean.parseBoolean(booleans_arr[i]);
        }
        return array;
    }
}