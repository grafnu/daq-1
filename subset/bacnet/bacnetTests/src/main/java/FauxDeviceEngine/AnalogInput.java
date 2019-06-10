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

import java.sql.SQLOutput;
import java.util.Map;

public class AnalogInput {

    private static float presentValue = 0.1f;
    private static String objectName = "device_run_command";
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
    private static float maxPresValue = 150.96f;
    private static int reliability = 4;
    private static SequenceOf<EventTransitionBits> eventTransitionBits = new SequenceOf<EventTransitionBits>();
    private static int notificationClass = 0;
    private static String description = "Faux Device";
    private static boolean eventAlgorithmInhibit = false;
    private static int units = 62;

//    public enum objectProperty {
//        Present_Value(PropertyIdentifier.presentValue, new Real(presentValue)),
//        Object_Name(PropertyIdentifier.objectName, new CharacterString(objectName)),
//        Deadband(PropertyIdentifier.deadband, new Real(deadband)),
//        Out_Of_Service(PropertyIdentifier.outOfService, new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService)),
//        Resolution(PropertyIdentifier.resolution, new Real(resolution)),
//        Event_Enable(PropertyIdentifier.eventEnable, new EventTransitionBits(eventEnable[0], eventEnable[1], eventEnable[2])),
//        Event_State(PropertyIdentifier.eventState, new EventState(eventState)),
//        Object_Type(PropertyIdentifier.objectType, new ObjectType(objectType)),
//        Time_Delay_Normal(PropertyIdentifier.timeDelayNormal, new Real(timeDelayNormal)),
//        Low_Limit(PropertyIdentifier.lowLimit, new Real(lowLimit)),
//        Limit_Enable(PropertyIdentifier.limitEnable, new LimitEnable(limitEnable[0], limitEnable[1])),
//        Cov_Increment(PropertyIdentifier.covIncrement, new Real(covIncrement)),
//        Status_Flags(PropertyIdentifier.statusFlags, new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3])),
//        Update_Interval(PropertyIdentifier.updateInterval, new UnsignedInteger(updateInterval)),
//        Acked_Transitions(PropertyIdentifier.ackedTransitions, new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2])),
//        High_Limit(PropertyIdentifier.highLimit, new Real(highLimit)),
//        Notify_Type(PropertyIdentifier.notifyType, new NotifyType(notifyType)),
//        Event_Detection_Enable(PropertyIdentifier.eventDetectionEnable, new com.serotonin.bacnet4j.type.primitive.Boolean(eventDetectionEnable)),
//        Max_Pres_Value(PropertyIdentifier.maxPresValue, new Real(maxPresValue)),
//        Reliability(PropertyIdentifier.reliability, new Reliability(reliability)),
//        Event_Message_Texts(PropertyIdentifier.presentValue, eventTransitionBits),
//        Notigication_Class(PropertyIdentifier.notificationClass, new UnsignedInteger(notificationClass)),
//        Description(PropertyIdentifier.description, new CharacterString(description)),
//        Event_Algorithm_Inhibit(PropertyIdentifier.eventAlgorithmInhibit, new com.serotonin.bacnet4j.type.primitive.Boolean(eventAlgorithmInhibit)),
//        Units(PropertyIdentifier.units, new EngineeringUnits(units));
//
//
//        private PropertyIdentifier propertyIdentifier;
//        private Encodable encodable;
//
//        objectProperty(PropertyIdentifier propertyIdentifier, Encodable encodable) {
//            this.propertyIdentifier = propertyIdentifier;
//            this.encodable = encodable;
//        }
//
//        public PropertyIdentifier getKey() {
//            return propertyIdentifier;
//        }
//
//        public Encodable getValue() {
//            return encodable;
//        }
//
//        public <T> void setProperty( T... ts) {
//            for(T t : ts) {
//                encodable = t;
//            }
//        }
//    }


    public AnalogInput(LocalDevice localDevice, BACnetObject bacnetObjectType, Map<String, Object>bacnetObjectMap) {
        for(Map.Entry<String, Object> map : bacnetObjectMap.entrySet()) {
            String propertyName = map.getKey();
            Object propertyValue = map.getValue();
            addObjectProperty(localDevice, bacnetObjectType, propertyName, propertyValue);
        }
    }

    private void addObjectProperty(LocalDevice localDevice, BACnetObject bacnetObjectType, String objectProperty, Object propertyValue) {
        Encodable encodable;
        switch (objectProperty) {
            case "Present_Value":
                presentValue = (float) propertyValue;
                encodable = new Real(presentValue);
                add(bacnetObjectType, PropertyIdentifier.presentValue, encodable);
                break;
            case "Object_Name":
                objectName = (String) propertyValue;
                encodable = new CharacterString(objectName);
                add(bacnetObjectType, PropertyIdentifier.objectName, encodable);
                break;
            case "Deadband":
                deadband = (float) propertyValue;
                encodable = new Real(deadband);
                add(bacnetObjectType, PropertyIdentifier.deadband, encodable);
                break;
            case "Out_Of_Service":
                outOfService = (boolean) propertyValue;
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(outOfService);
                add(bacnetObjectType, PropertyIdentifier.outOfService, encodable);
                break;
            case "Resolution" :
                resolution = (float) propertyValue;
                encodable = new Real(resolution);
                add(bacnetObjectType, PropertyIdentifier.resolution, encodable);
                break;
            case "Event_Enable":
                eventEnable = (boolean[]) propertyValue;
                encodable = new EventTransitionBits(eventEnable[0], eventEnable[1], eventEnable[2]);
                add(bacnetObjectType, PropertyIdentifier.eventEnable, encodable);
                break;
            case "Event_State":
                eventState = (int) propertyValue;
                encodable = new EventState(eventState);
                add(bacnetObjectType, PropertyIdentifier.eventState, encodable);
                break;
            case "Object_Type":
                objectType = (int) propertyValue;
                encodable = new ObjectType(objectType);
                add(bacnetObjectType, PropertyIdentifier.objectType, encodable);
                break;
            case "Time_Delay_Normal":
                timeDelayNormal = (int) propertyValue;
                encodable = new Real(timeDelayNormal);
                add(bacnetObjectType, PropertyIdentifier.timeDelayNormal, encodable);
                break;
            case "Low_Limit":
                lowLimit = (float) propertyValue;
                encodable = new Real(lowLimit);
                add(bacnetObjectType, PropertyIdentifier.lowLimit, encodable);
                break;
            case "Limit_Enable":
                limitEnable = (boolean[]) propertyValue;
                encodable = new LimitEnable(limitEnable[0], limitEnable[1]);
                add(bacnetObjectType, PropertyIdentifier.limitEnable, encodable);
                break;
            case "Cov_Increment":
                covIncrement = (float) propertyValue;
                encodable = new Real(covIncrement);
                add(bacnetObjectType, PropertyIdentifier.covIncrement, encodable);
                break;
            case "Status_Flags":
                statusFlags = (boolean[]) propertyValue;
                encodable = new StatusFlags(statusFlags[0], statusFlags[1], statusFlags[2], statusFlags[3]);
                add(bacnetObjectType, PropertyIdentifier.statusFlags, encodable);
                break;
            case "Update_Interval":
                updateInterval = (int) propertyValue;
                encodable = new UnsignedInteger(updateInterval);
                add(bacnetObjectType, PropertyIdentifier.updateInterval, encodable);
                break;
            case "Acked_Transitions":
                ackedTransitions = (boolean[]) propertyValue;
                encodable = new EventTransitionBits(ackedTransitions[0], ackedTransitions[1], ackedTransitions[2]);
                add(bacnetObjectType, PropertyIdentifier.ackedTransitions, encodable);
                break;
            case "High_Limit":
                highLimit = (float) propertyValue;
                encodable = new Real(highLimit);
                add(bacnetObjectType, PropertyIdentifier.highLimit, encodable);
                break;
            case "Notify_Type":
                notifyType = (int) propertyValue;
                encodable = new NotifyType(notifyType);
                add(bacnetObjectType, PropertyIdentifier.notifyType, encodable);
                break;
            case "Event_Detection_Enable":
                eventDetectionEnable = (boolean) propertyValue;
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventDetectionEnable);
                add(bacnetObjectType, PropertyIdentifier.eventDetectionEnable, encodable);
                break;
            case "Max_Pres_Value":
                maxPresValue = (float) propertyValue;
                encodable = new Real(maxPresValue);
                add(bacnetObjectType, PropertyIdentifier.maxPresValue, encodable);
                break;
            case "Reliability":
                reliability = (int) propertyValue;
                encodable = new Reliability(reliability);
                add(bacnetObjectType, PropertyIdentifier.reliability, encodable);
                break;
            case "Event_Message_Texts":
                if((boolean)propertyValue) {
                    eventTransitionBits = new SequenceOf<EventTransitionBits>();
                    encodable = eventTransitionBits;
                add(bacnetObjectType, PropertyIdentifier.eventMessageTexts, encodable);
                }
                break;
            case "Notification_Class":
                notificationClass = (int) propertyValue;
                encodable = new UnsignedInteger(notificationClass);
                add(bacnetObjectType, PropertyIdentifier.notificationClass, encodable);
                break;
            case "Description":
                description = (String) propertyValue;
                encodable = new CharacterString(description);
                add(bacnetObjectType, PropertyIdentifier.description, encodable);
                break;
            case "Event_Algorithm_Inhibit":
                eventAlgorithmInhibit = (boolean) propertyValue;
                encodable = new com.serotonin.bacnet4j.type.primitive.Boolean(eventAlgorithmInhibit);
                add(bacnetObjectType, PropertyIdentifier.eventAlgorithmInhibit, encodable);
                break;

        }
    }

    private void add(BACnetObject bacnetObjectType, PropertyIdentifier propertyIdentifier, Encodable encodable) {
        try {
            bacnetObjectType.setProperty(propertyIdentifier, encodable);
        } catch (BACnetServiceException e) {
            System.out.println("Error adding bacnet property: " + e.getMessage());
        }
    }

    private void addObjectType(LocalDevice localDevice, BACnetObject bacnetObject) {
        try {
            localDevice.addObject(bacnetObject);
        } catch (BACnetServiceException e) {
            System.out.println("Error adding bacnet object: " + e.getMessage());
        }
    }

}