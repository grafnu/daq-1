package com.google.daq.mqtt.registrar;

import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.daq.mqtt.util.CloudIotManager;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.rmi.server.ExportException;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class LocalDevice {

  private static final String RSA256_X509_PEM = "RSA_X509_PEM";
  private static final String RSA_PUBLIC_PEM = "rsa_public.pem";

  private final String deviceId;
  private final File deviceDir;

  public LocalDevice(String deviceId, File devicesDir) {
    this.deviceId = deviceId;
    deviceDir = new File(devicesDir, deviceId);
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
}
