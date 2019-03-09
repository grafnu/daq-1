package com.google.daq.mqtt.registrar;

import com.google.api.services.cloudiot.v1.model.Device;
import com.google.daq.mqtt.util.CloudIotManager;
import com.google.daq.mqtt.util.ExceptionMap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registrar {

  public static final String CLOUD_IOT_CONFIG_JSON = "cloud_iot_config.json";
  private String gcpCredPath;
  private CloudIotManager cloudIotManager;
  private File cloudIotConfig;

  public static void main(String[] args) {
    Registrar registrar = new Registrar();
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Args: [gcp_cred_file] [site_dir]");
      }
      registrar.setGcpCredPath(args[0]);
      registrar.setSiteConfigPath(args[1]);
      registrar.registerDevices();
    } catch (ExceptionMap em) {
      System.exit(2);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    System.exit(0);
  }

  private void setSiteConfigPath(String siteConfigPath) {
    cloudIotConfig = new File(siteConfigPath, CLOUD_IOT_CONFIG_JSON);
    cloudIotManager = new CloudIotManager(new File(gcpCredPath), cloudIotConfig);
  }

  private void registerDevices() {
    Map<String, LocalDevice> localDevices = getLocalDevices();
    Map<String, Device> cloudDevices = makeCloudDevices();
    if (localDevices.size() != cloudDevices.size()) {
      throw new RuntimeException("Device map size mismatch");
    }
  }

  private Map<String,Device> makeCloudDevices() {
    List<Device> devices = cloudIotManager.fetchDevices();
    Map<String, Device> deviceMap = new HashMap<>();
    devices.stream().map(Device::getName)
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
    return new HashMap<>();
  }

  private void setGcpCredPath(String gcpConfigPath) {
    this.gcpCredPath = gcpConfigPath;
  }
}
