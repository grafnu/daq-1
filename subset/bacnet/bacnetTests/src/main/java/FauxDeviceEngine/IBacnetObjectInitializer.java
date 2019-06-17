package FauxDeviceEngine;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import org.json.simple.JSONObject;

public interface IBacnetObjectInitializer {
    void initializeAnalogObject(LocalDevice localDevice, ObjectType objectType, JSONObject bacnetObject,
                                String bacnetObjectType, boolean setDefaultValues);

    void initializeBinaryObject(LocalDevice localDevice, ObjectType objectType, JSONObject bacnetObject,
                                String bacnetObjectType, boolean setDefaultValues);
}
