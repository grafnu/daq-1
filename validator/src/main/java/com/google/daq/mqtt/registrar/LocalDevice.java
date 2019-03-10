package com.google.daq.mqtt.registrar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.daq.mqtt.util.CloudIotManager;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class LocalDevice {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String RSA256_X509_PEM = "RSA_X509_PEM";
  private static final String RSA_PUBLIC_PEM = "rsa_public.pem";
  public static final String PROPERTIES_JSON = "properties.json";

  private final String deviceId;
  private final File deviceDir;
  private final Properties properties;

  public LocalDevice(String deviceId, File devicesDir) {
    this.deviceId = deviceId;
    deviceDir = new File(devicesDir, deviceId);
    properties = readProperties();
  }

  private Properties readProperties() {
    File configFile = new File(deviceDir, PROPERTIES_JSON);
    try {
      return validate(OBJECT_MAPPER.readValue(configFile, Properties.class));
    } catch (Exception e) {
      throw new RuntimeException("While reading properties file "+ configFile.getAbsolutePath(), e);
    }
  }

  private Properties validate(Properties properties) {
    String mode = properties.mode;
    if (!("gateway".equals(mode) || "direct".equals(mode) || "proxy".equals(mode))) {
      throw new RuntimeException("Invalid device properties mode " + mode);
    }

    if (mode.equals("proxy") && properties.gateway_id == null) {
      throw new RuntimeException("Missing gateway_id for mode proxy device");
    }
    if (mode.equals("direct") && properties.gateway_id != null) {
      throw new RuntimeException("Unexpected gateway_id for mode direct device");
    }
    
    return properties;
  }

  public List<DeviceCredential> loadCredentials() {
    try {
      File deviceKeyFile = new File(deviceDir, RSA_PUBLIC_PEM);
      if (!deviceKeyFile.exists()) {
        generateNewKey();
      }
      return CloudIotManager.makeCredentials(RSA256_X509_PEM,
          IOUtils.toString(new FileInputStream(deviceKeyFile), Charset.defaultCharset()));
    } catch (Exception e) {
      throw new RuntimeException("While loading credentials for local device " + deviceId, e);
    }
  }

  private void generateNewKey() {
    String absolutePath = deviceDir.getAbsolutePath();
    try {
      System.err.println("Generating device credentials in " + absolutePath);
      int exitCode = Runtime.getRuntime().exec("validator/bin/keygen.sh " + absolutePath).waitFor();
      if (exitCode != 0) {
        throw new RuntimeException("Keygen exit code " + exitCode);
      }
    } catch (Exception e) {
      throw new RuntimeException("While generating new credentials for " + deviceId, e);
    }
  }

  private static class Properties {
    public String mode;
    public String gateway_id;
  }
}
