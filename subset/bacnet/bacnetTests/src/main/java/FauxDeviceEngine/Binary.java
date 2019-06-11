package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.EventTransitionBits;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import java.sql.Date;
import java.util.Map;

public class Binary {
    private static int presentValue = 1;
    private static String objectName = "chiller_water_valve_percentage_command";
    private static String deviceType = "Analog0To10Volts";
    private static boolean outOfService = false;
    private static boolean[] eventEnable = {true, true, false};
    private static int eventState = 0;
    private static int objectType = 0;
    private static int timeDelayNormal = 0;
    private static boolean[] statusFlags = {false, false, false, false};
    private static boolean[] ackedTransitions = {true, true, true};
    private static int notifyType = 0;
    private static boolean eventDetectionEnable = false;
    private static int reliability = 4;
    private static SequenceOf<EventTransitionBits> eventTransitionBits = new SequenceOf<EventTransitionBits>();
    private static int notificationClass = 0;
    private static String description = "Faux Device";
    private static boolean eventAlgorithmInhibit = false;
    private static int units = 62;
    private static float relinquishDefault = 1.4f;
    private static String activeText = "TRUE";
    private static long timeOfStateCountReset;
    private static int changeOfStateCount = 0;
    private static String inactiveText = "FALSE";
    private static int polarity = 0;
    private static int alarmValue = 0;
    private static long changeOfStateTime;
    private static long timeOfActiveTimeReset;
    private static int elapsedActiveTime = 0;
    private static int minimumOnTime = 0;
    private static int minimumOffTime = 0;
    private static int feedbackValue = 0;


    public Binary(LocalDevice localDevice, BACnetObject bacnetObjectType, Map<String, String> bacnetObjectMap) {
        for(Map.Entry<String, String> map : bacnetObjectMap.entrySet()) {
            String propertyName = map.getKey();
            String propertyValue = map.getValue();
            addObjectProperty(bacnetObjectType, propertyName, propertyValue);
        }
        addObjectType(localDevice, bacnetObjectType);
    }

    private void addObjectProperty(BACnetObject bacnetObjectType, String objectProperty, String propertyValue) {
        Encodable encodable;
        switch (objectProperty) {
            case "Present_Value":
                presentValue = Integer.parseInt(propertyValue);
                encodable = new BinaryPV(presentValue);
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
            case "Out_Of_Service":
                outOfService = Boolean.parseBoolean(propertyValue);
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService);
                add(bacnetObjectType, PropertyIdentifier.outOfService, encodable);
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
            case "Status_Flags":
                statusFlags = castToArrayBoolean(propertyValue);
                encodable = new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3]);
                add(bacnetObjectType, PropertyIdentifier.statusFlags, encodable);
                break;
            case "Acked_Transitions":
                ackedTransitions = castToArrayBoolean(propertyValue);
                encodable = new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2]);
                add(bacnetObjectType, PropertyIdentifier.ackedTransitions, encodable);
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
            case "Relinquish_Default":
                relinquishDefault = Float.parseFloat(propertyValue);
                encodable = new Real(relinquishDefault);
                add(bacnetObjectType, PropertyIdentifier.relinquishDefault, encodable);
                break;
            case "Active_Text":
                activeText = propertyValue;
                encodable = new CharacterString(activeText);
                add(bacnetObjectType, PropertyIdentifier.activeText, encodable);
                break;
            case "Time_Of_State_Count_Reset":
                timeOfStateCountReset = Date.parse(propertyValue);
                encodable = new DateTime(timeOfStateCountReset);
                add(bacnetObjectType, PropertyIdentifier.timeOfStateCountReset, encodable);
                break;
            case "Change_Of_State_Count":
                changeOfStateCount = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(changeOfStateCount);
                add(bacnetObjectType, PropertyIdentifier.changeOfStateCount, encodable);
                break;
            case "Inactive_Text":
                inactiveText = propertyValue;
                encodable = new CharacterString(inactiveText);
                add(bacnetObjectType, PropertyIdentifier.inactiveText, encodable);
                break;
            case "Polarity":
                polarity = Integer.parseInt(propertyValue);
                encodable = new Polarity(polarity);
                add(bacnetObjectType, PropertyIdentifier.polarity, encodable);
                break;
            case "Alarm_Value":
                alarmValue = Integer.parseInt(propertyValue);
                encodable = new BinaryPV(alarmValue);
                add(bacnetObjectType, PropertyIdentifier.changeOfStateCount, encodable);
                break;
            case "Change_Of_State_Time":
                changeOfStateTime = Date.parse(propertyValue);
                encodable = new DateTime(changeOfStateTime);
                add(bacnetObjectType, PropertyIdentifier.changeOfStateTime, encodable);
                break;
            case "Time_Of_Active_Time_Reset":
                timeOfActiveTimeReset = Date.parse(propertyValue);
                encodable = new DateTime(timeOfActiveTimeReset);
                add(bacnetObjectType, PropertyIdentifier.timeOfActiveTimeReset, encodable);
                break;
            case "Elapsed_Active_Time":
                elapsedActiveTime = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(elapsedActiveTime);
                add(bacnetObjectType, PropertyIdentifier.elapsedActiveTime, encodable);
                break;
            case "Minimum_On_Time":
                minimumOnTime = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(minimumOnTime);
                add(bacnetObjectType, PropertyIdentifier.minimumOnTime, encodable);
                break;
            case "Minimum_Off_Time":
                minimumOffTime = Integer.parseInt(propertyValue);
                encodable = new UnsignedInteger(minimumOffTime);
                add(bacnetObjectType, PropertyIdentifier.minimumOffTime, encodable);
                break;
            case "Feeback_Value":
                feedbackValue = Integer.parseInt(propertyValue);
                encodable = new BinaryPV(feedbackValue);
                add(bacnetObjectType, PropertyIdentifier.feedbackValue, encodable);
                break;
        }
    }

    private void add(BACnetObject bacnetObjectType, PropertyIdentifier propertyIdentifier, Encodable encodable) {
        try {
            bacnetObjectType.setProperty(propertyIdentifier, encodable);
        } catch (BACnetServiceException e) {
            System.out.println("Error adding bacnet property: " + e.getMessage() + " " + propertyIdentifier.toString());
        }
    }

    private void addObjectType(LocalDevice localDevice, BACnetObject bacnetObject) {
        try {
            localDevice.addObject(bacnetObject);
        } catch (BACnetServiceException e) {
            System.out.println("Error adding bacnet object: " + e.getMessage() + " " + bacnetObject.toString());
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
