package com.google.daq.mqtt.registrar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.common.base.Preconditions;
import com.google.daq.mqtt.util.CloudDeviceSettings;
import com.google.daq.mqtt.util.CloudIotManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;

public class LocalDevice {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String RSA256_X509_PEM = "RSA_X509_PEM";
  private static final String RSA_PUBLIC_PEM = "rsa_public.pem";

  private static final String METADATA_JSON = "metadata.json";
  private static final String METADATA_SECTION_KEY = "section";

  private final String deviceId;
  private final Schema schema;
  private final File deviceDir;
  private final Metadata metadata;

  private CloudDeviceSettings settings;

  LocalDevice(File devicesDir, String deviceId, Schema schema) {
    this.deviceId = deviceId;
    this.schema = schema;
    deviceDir = new File(devicesDir, deviceId);
    validateMetadata();
    metadata = readMetadata();
  }

  private void validateMetadata() {
    File targetFile = new File(deviceDir, METADATA_JSON);
    try (InputStream targetStream = new FileInputStream(targetFile)) {
      schema.validate(new JSONObject(new JSONTokener(targetStream)));
    } catch (Exception e) {
      throw new RuntimeException("Against input " + targetFile, e);
    }
  }

  public static boolean deviceDefined(File devicesDir, String deviceName) {
    return new File(new File(devicesDir, deviceName), METADATA_JSON).isFile();
  }

  private Metadata readMetadata() {
    File configFile = new File(deviceDir, METADATA_JSON);
    try {
      return validate(OBJECT_MAPPER.readValue(configFile, Metadata.class));
    } catch (Exception e) {
      throw new RuntimeException("While reading properties file "+ configFile.getAbsolutePath(), e);
    }
  }

  private Metadata validate(Metadata data) {
    Preconditions.checkNotNull(data.system, "Metadata system subsection missing");
    if (data.system.gcp != null) {
      String mode = data.system.gcp.mode;
      String gatewayId = data.system.gcp.gateway_id;
      if (mode == null) {
        Preconditions
            .checkArgument(gatewayId == null, "direct (default) mode gateway_id should be null");
      } else if ("gateway".equals(mode)) {
        Preconditions.checkArgument(gatewayId == null, "gateway mode gateway_id should be null");
      } else if ("proxy".equals(mode)) {
        Preconditions.checkNotNull(gatewayId, "proxy mode needs gateway_id property specified");
      } else {
        throw new RuntimeException("Unknown device mode " + mode);
      }
    }
    return data;
  }

  private List<DeviceCredential> loadCredentials() {
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

  private Map<String, String> loadMetadata() {
    try {
      Map<String, String> metaMap = new HashMap<>();
      metaMap.put(METADATA_SECTION_KEY,
          Preconditions.checkNotNull(metadata.system.location.section,
              "system.location.section not defined"));
      return metaMap;
    } catch (Exception e) {
      throw new RuntimeException("While loading metadata", e);
    }
  }

  CloudDeviceSettings getSettings() {
    try {
      if (settings != null) {
        return settings;
      }

      settings = new CloudDeviceSettings();
      settings.credentials = loadCredentials();
      settings.metadata = loadMetadata();
      return settings;
    } catch (Exception e) {
      throw new RuntimeException("While getting settings for device " + deviceId, e);
    }
  }

  private static class Metadata {
    public PointsetMetadata pointset;
    public SystemMetadata system;
    public String version;
    public String timestamp;
  }

  private static class PointsetMetadata {
    public Map<String, PointMetadata> points;
  }

  private static class SystemMetadata {
    public GcpMetadata gcp;
    public LocationMetadata location;
    public PhysicalTagMetadata physical_tag;
  }

  private static class PointMetadata {
    public String units;
  }

  private static class LocationMetadata {
    public String site_name;
    public String section;
  }

  private static class PhysicalTagMetadata {
    public String inst_guid;
    public String inst_name;
  }

  private static class GcpMetadata {
    public String mode;
    public String gateway_id;
  }
}
