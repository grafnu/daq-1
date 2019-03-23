package com.google.daq.mqtt.registrar;

import com.google.api.services.cloudiot.v1.model.Device;
import com.google.common.base.Preconditions;
import com.google.daq.mqtt.util.CloudIotManager;
import com.google.daq.mqtt.util.ExceptionMap;
import com.google.daq.mqtt.util.ExceptionMap.ErrorTree;
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

  static final String METADATA_JSON = "metadata.json";
  static final String ENVELOPE_JSON = "envelope.json";

  private static final String CLOUD_IOT_CONFIG_JSON = "cloud_iot_config.json";
  private static final String DEVICES_DIR = "devices";
  private static final String ERROR_FORMAT_INDENT = "  ";
  private String gcpCredPath;
  private CloudIotManager cloudIotManager;
  private File cloudIotConfig;
  private File siteConfig;
  private Map<String, Schema> schemas = new HashMap<>();
  private File schemaBase;

  public static void main(String[] args) {
    Registrar registrar = new Registrar();
    try {
      if (args.length != 3) {
        throw new IllegalArgumentException("Args: [gcp_cred_file] [site_dir] [schema_file]");
      }
      registrar.setGcpCredPath(args[0]);
      registrar.setSiteConfigPath(args[1]);
      registrar.setSchemaBase(args[2]);
      registrar.processDevices();
    } catch (ExceptionMap em) {
      ErrorTree errorTree = ExceptionMap.format(em, ERROR_FORMAT_INDENT);
      errorTree.write(System.err);
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
    ExceptionMap exceptionMap = new ExceptionMap("Error processing local devices");
    try {
      Map<String, LocalDevice> localDevices = getLocalDevices();
      Map<String, Device> cloudDevices = makeCloudDevices();
      Set<String> extraDevices = new HashSet<>(cloudDevices.keySet());
      for (String localName : localDevices.keySet()) {
        extraDevices.remove(localName);
        try {
          LocalDevice localDevice = localDevices.get(localName);
          if (cloudIotManager.registerDevice(localName, localDevice.getSettings())) {
            System.err.println("Created new device entry " + localName);
          } else {
            System.err.println("Updated device entry " + localName);
          }
        } catch (Exception e) {
          exceptionMap.put(localName, e);
        }
      }
      for (String extraName : extraDevices) {
        try {
          System.err.println("Blocking extra device " + extraName);
          cloudIotManager.blockDevice(extraName, true);
        } catch (Exception e) {
          exceptionMap.put(extraName, e);
        }
      }
    } catch (ExceptionMap em) {
      throw em;
    } catch (Exception e) {
      throw new RuntimeException("While processing devices", e);
    }
    exceptionMap.throwIfNotEmpty();
  }

  private Map<String,Device> makeCloudDevices() {
    System.err.println("Fetching remote registry " + cloudIotManager.getCloudIotConfig().registry_id);
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
    ExceptionMap exceptionMap = new ExceptionMap("Error loading local devices");
    String[] devices = devicesDir.list();
    if (devices == null) {
      return localDevices;
    }
    for (String deviceName : devices) {
      try {
        if (LocalDevice.deviceExists(devicesDir, deviceName)) {
          System.err.println("Loading local device " + deviceName);
          LocalDevice localDevice = new LocalDevice(devicesDir, deviceName, schemas);
          localDevice.validate(cloudIotManager.getCloudIotConfig());
          localDevices.put(deviceName, localDevice);
        }
      } catch (Exception e) {
        exceptionMap.put(deviceName, e);
      }
    }
    exceptionMap.throwIfNotEmpty();

    for (String deviceName : devices) {
      try {
        System.err.println("Writing normalized device " + deviceName);
        localDevices.get(deviceName).writeNormlized();
      } catch (Exception e) {
        exceptionMap.put(deviceName, e);
      }
    }
    exceptionMap.throwIfNotEmpty();

    return localDevices;
  }

  private void setGcpCredPath(String gcpConfigPath) {
    this.gcpCredPath = gcpConfigPath;
  }

  private void setSchemaBase(String schemaBase) {
    this.schemaBase = new File(schemaBase);
    loadSchema(METADATA_JSON);
    loadSchema(ENVELOPE_JSON);
  }

  private void loadSchema(String key) {
    File schemaFile = new File(schemaBase, key);
    try (InputStream schemaStream = new FileInputStream(schemaFile)) {
      JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));
      schemas.put(key, SchemaLoader.load(rawSchema, new Loader()));
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
