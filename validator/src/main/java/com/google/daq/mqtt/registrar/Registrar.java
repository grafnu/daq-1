package com.google.daq.mqtt.registrar;

import com.google.api.services.cloudiot.v1.model.Device;
import com.google.common.base.Preconditions;
import com.google.daq.mqtt.util.CloudIotManager;
import com.google.daq.mqtt.util.ExceptionMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Registrar {

  private static final String CLOUD_IOT_CONFIG_JSON = "cloud_iot_config.json";
  private static final String DEVICES_DIR = "devices";
  private String gcpCredPath;
  private CloudIotManager cloudIotManager;
  private File cloudIotConfig;
  private File siteConfig;
  private Schema schema;
  private File schemaBase;

  public static void main(String[] args) {
    Registrar registrar = new Registrar();
    try {
      if (args.length != 3) {
        throw new IllegalArgumentException("Args: [gcp_cred_file] [site_dir] [schema_file]");
      }
      registrar.setGcpCredPath(args[0]);
      registrar.setSiteConfigPath(args[1]);
      registrar.setSchemaFile(args[2]);
      registrar.processDevices();
    } catch (ExceptionMap em) {
      System.exit(2);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    System.exit(0);
  }

  private void setSiteConfigPath(String siteConfigPath) {
    siteConfig = new File(siteConfigPath);
    cloudIotConfig = new File(siteConfig, CLOUD_IOT_CONFIG_JSON);
    cloudIotManager = new CloudIotManager(new File(gcpCredPath), cloudIotConfig);
  }

  private void processDevices() {
    try {
      Map<String, LocalDevice> localDevices = getLocalDevices();
      Map<String, Device> cloudDevices = makeCloudDevices();
      Set<String> extraDevices = new HashSet<>(cloudDevices.keySet());
      for (String localName : localDevices.keySet()) {
        extraDevices.remove(localName);
        LocalDevice localDevice = localDevices.get(localName);
        if (cloudIotManager.registerDevice(localName, localDevice.getSettings())) {
          System.err.println("Created new device entry " + localName);
        } else {
          System.err.println("Updated device entry " + localName);
        }
      }
      for (String extraName : extraDevices) {
        System.err.println("Blocking extra device " + extraName);
        cloudIotManager.blockDevice(extraName, true);
      }
    } catch (Exception e) {
      throw new RuntimeException("While processing devices", e);
    }
  }

  private Map<String,Device> makeCloudDevices() {
    System.err.println("Fetching remote registry");
    List<Device> devices = cloudIotManager.fetchDevices();
    Map<String, Device> deviceMap = new HashMap<>();
    devices.stream().map(Device::getId)
        .forEach(deviceName -> {
          try {
            System.err.println("Fetching remote device " + deviceName);
            deviceMap.put(deviceName, cloudIotManager.fetchDevice(deviceName));
          } catch (IOException e) {
            throw new RuntimeException("While fetching device " + deviceName, e);
          }
        });
    return deviceMap;
  }

  private Map<String,LocalDevice> getLocalDevices() {
    HashMap<String, LocalDevice> localDevices = new HashMap<>();
    File devicesDir = new File(siteConfig, DEVICES_DIR);
    String[] devices = devicesDir.list();
    if (devices == null) {
      return localDevices;
    }
    for (String deviceName : devices) {
      if (LocalDevice.deviceDefined(devicesDir, deviceName)) {
        System.err.println("Loading local device " + deviceName);
        localDevices.put(deviceName, new LocalDevice(devicesDir, deviceName, schema));
      }
    }
    return localDevices;
  }

  private void setGcpCredPath(String gcpConfigPath) {
    this.gcpCredPath = gcpConfigPath;
  }

  private void setSchemaFile(String path) {
    File schemaFile = new File(path);
    schemaBase = schemaFile.getParentFile();
    try (InputStream schemaStream = new FileInputStream(schemaFile)) {
      JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));
      schema = SchemaLoader.load(rawSchema, new Loader());
    } catch (Exception e) {
      throw new RuntimeException("While loading schema " + schemaFile.getAbsolutePath(), e);
    }
  }

  private class Loader implements SchemaClient {

    public static final String FILE_PREFIX = "file:";

    @Override
    public InputStream get(String schema) {
      try {
        Preconditions.checkArgument(schema.startsWith(FILE_PREFIX));
        return new FileInputStream(new File(schemaBase, schema.substring(FILE_PREFIX.length())));
      } catch (Exception e) {
        throw new RuntimeException("While loading sub-schema " + schema, e);
      }
    }
  }
}
