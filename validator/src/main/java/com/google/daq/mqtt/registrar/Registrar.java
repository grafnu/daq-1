package com.google.daq.mqtt.registrar;

import com.google.api.services.cloudiot.v1.model.Device;
import com.google.daq.mqtt.util.CloudIotManager;
import com.google.daq.mqtt.util.ExceptionMap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Registrar {

  public static final String CLOUD_IOT_CONFIG_JSON = "cloud_iot_config.json";
  public static final String DEVICES_DIR = "devices";
  private String gcpCredPath;
  private CloudIotManager cloudIotManager;
  private File cloudIotConfig;
  private File siteConfig;

  public static void main(String[] args) {
    Registrar registrar = new Registrar();
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Args: [gcp_cred_file] [site_dir]");
      }
      registrar.setGcpCredPath(args[0]);
      registrar.setSiteConfigPath(args[1]);
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
        if (cloudIotManager.registerDevice(localName, localDevice.loadCredentials())) {
          System.err.println("Created new device entry " + localName);
        } else {
          System.err.println("Updated device entry " + localName);
        }
      }
      for (String extraName : extraDevices) {
        System.err.println("Blocking extra device " + extraName);
        cloudIotManager.blockDevice(extraName);
      }
    } catch (Exception e) {
      throw new RuntimeException("While processing devices", e);
    }
  }

  private Map<String,Device> makeCloudDevices() {
    List<Device> devices = cloudIotManager.fetchDevices();
    Map<String, Device> deviceMap = new HashMap<>();
    devices.stream().map(Device::getId)
        .forEach(deviceName -> {
          try {
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
      localDevices.put(deviceName, new LocalDevice(deviceName, devicesDir));
    }
    return localDevices;
  }

  private void setGcpCredPath(String gcpConfigPath) {
    this.gcpCredPath = gcpConfigPath;
  }
}
