package com.google.daq.mqtt.registrar;

import com.google.daq.mqtt.util.CloudDevice;
import com.google.daq.mqtt.util.CloudIotManager;
import com.google.daq.mqtt.util.ExceptionMap;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Registrar {

  public static final String CLOUD_IOT_CONFIG_JSON = "cloud_iot_config.json";
  private String gcpCredPath;
  private CloudIotManager cloudIotManager;
  private String siteConfigPath;

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
    this.siteConfigPath = siteConfigPath;
  }

  private void registerDevices() {
    File cloudIotConfig = new File(siteConfigPath, CLOUD_IOT_CONFIG_JSON);
    cloudIotManager = new CloudIotManager(new File(gcpCredPath), cloudIotConfig);
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

  private void setGcpCredPath(String gcpConfigPath) {
    this.gcpCredPath = gcpConfigPath;
  }
}
