package com.google.daq.mqtt.registrar;

import com.google.daq.mqtt.util.CloudDevice;
import com.google.daq.mqtt.util.CloudIotManager;
import com.google.daq.mqtt.util.ExceptionMap;
import java.util.HashMap;
import java.util.Map;

public class Registrar {

  private String deviceDirectory;
  private String gcpCredFile;
  private CloudIotManager cloudIotManager;
  private String cloudIotConfigFile;

  public static void main(String[] args) {
    Registrar registrar = new Registrar();
    try {
      if (args.length != 3) {
        throw new IllegalArgumentException("Args: [gcp_cred_file] [project_config] [devices]");
      }
      registrar.setGcpCredFile(args[0]);
      registrar.setCloudIotConfigFile(args[1]);
      registrar.setDevicesDirectory(args[2]);
      registrar.registerDevices();
    } catch (ExceptionMap em) {
      System.exit(2);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    System.exit(0);
  }

  private void setCloudIotConfigFile(String cloudIotConfigFile) {
    this.cloudIotConfigFile = cloudIotConfigFile;
  }

  private void registerDevices() {
    cloudIotManager = new CloudIotManager(gcpCredFile, cloudIotConfigFile);
    Map<String, LocalDevice> localDevices = getLocalDevices();
    Map<String, CloudDevice> cloudDevices = getCloudDevices();
    if (localDevices.size() != cloudDevices.size()) {
      throw new RuntimeException("Device map size mismatch");
    }
  }

  private Map<String,CloudDevice> getCloudDevices() {
    return new HashMap<>();
  }

  private Map<String,LocalDevice> getLocalDevices() {
    return new HashMap<>();
  }

  private void setDevicesDirectory(String deviceDirectory) {
    this.deviceDirectory = deviceDirectory;
  }

  private void setGcpCredFile(String gcpConfigPath) {
    this.gcpCredFile = gcpConfigPath;
  }
}
