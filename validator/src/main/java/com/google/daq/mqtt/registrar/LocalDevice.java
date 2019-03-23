package com.google.daq.mqtt.registrar;

import static com.google.daq.mqtt.registrar.Registrar.ENVELOPE_JSON;
import static com.google.daq.mqtt.registrar.Registrar.METADATA_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.common.base.Preconditions;
import com.google.daq.mqtt.util.CloudDeviceSettings;
import com.google.daq.mqtt.util.CloudIotConfig;
import com.google.daq.mqtt.util.CloudIotManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;

public class LocalDevice {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String RSA256_X509_PEM = "RSA_X509_PEM";
  private static final String RSA_PUBLIC_PEM = "rsa_public.pem";
  public static final String PHYSICAL_TAG_FORMAT = "%s_%s";
  public static final String PHYSICAL_TAG_ERROR = "Physical asset name %s does not match expected %s";

  private final String deviceId;
  private final Map<String, Schema> schemas;
  private final File deviceDir;
  private final Metadata metadata;

  private CloudDeviceSettings settings;

  LocalDevice(File devicesDir, String deviceId, Map<String, Schema> schemas) {
    this.deviceId = deviceId;
    this.schemas = schemas;
    deviceDir = metadataFile(devicesDir, deviceId);
    validateMetadata();
    metadata = readMetadata();
  }

  private void validateMetadata() {
    File metadataFile = metadataFile(deviceDir, METADATA_JSON);
    try (InputStream targetStream = new FileInputStream(metadataFile)) {
      schemas.get(METADATA_JSON).validate(new JSONObject(new JSONTokener(targetStream)));
    } catch (Exception e) {
      throw new RuntimeException("Processing input " + metadataFile, e);
    }
  }

  static boolean deviceExists(File devicesDir, String deviceName) {
    return new File(metadataFile(devicesDir, deviceName), METADATA_JSON).isFile();
  }

  private static File metadataFile(File devicesDir, String deviceName) {
    return new File(devicesDir, deviceName);
  }

  private Metadata readMetadata() {
    File metadataFile = metadataFile();
    try {
      return validate(OBJECT_MAPPER.readValue(metadataFile, Metadata.class));
    } catch (Exception e) {
      throw new RuntimeException("While reading "+ metadataFile.getAbsolutePath(), e);
    }
  }

  private File metadataFile() {
    return new File(deviceDir, METADATA_JSON);
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

  CloudDeviceSettings getSettings() {
    try {
      if (settings != null) {
        return settings;
      }

      settings = new CloudDeviceSettings();
      settings.credentials = loadCredentials();
      return settings;
    } catch (Exception e) {
      throw new RuntimeException("While getting settings for device " + deviceId, e);
    }
  }

  public void validate(CloudIotConfig cloudIotConfig) {
    try {
      Envelope envelope = new Envelope();
      envelope.deviceId = deviceId;
      envelope.deviceRegistryId = cloudIotConfig.registry_id;
      envelope.projectId = cloudIotConfig.getProjectId();
      envelope.deviceNumId = makeNumId(envelope);
      String envelopeJson = OBJECT_MAPPER.writeValueAsString(envelope);
      schemas.get(ENVELOPE_JSON).validate(new JSONObject(new JSONTokener(envelopeJson)));
    } catch (Exception e) {
      throw new IllegalStateException("Validating envelope " + deviceId, e);
    }
    checkConsistency(cloudIotConfig.site_name);
  }

  private void checkConsistency(String expected_site_name) {
    String siteName = metadata.system.location.site_name;
    String desiredTag = String.format(PHYSICAL_TAG_FORMAT, siteName, deviceId);
    String assetName = metadata.system.physical_tag.asset.name;
    Preconditions.checkState(desiredTag.equals(assetName),
        String.format(PHYSICAL_TAG_ERROR, assetName, desiredTag));
    Preconditions.checkState(expected_site_name.equals(siteName));
  }

  private String makeNumId(Envelope envelope) {
    int hash = Objects.hash(deviceId, envelope.deviceRegistryId, envelope.projectId);
    return Integer.toString(hash < 0 ? -hash : hash);
  }

  public void writeNormlized() {
    File metadataFile = metadataFile();
    try {
      OBJECT_MAPPER.writeValue(metadataFile, metadata);
    } catch (Exception e) {
      throw new RuntimeException("While writing "+ metadataFile.getAbsolutePath(), e);
    }
  }

  private static class Envelope {
    public String deviceId;
    public String deviceNumId;
    public String deviceRegistryId;
    public String projectId;
    public final String subFolder = "metadata";
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
    public PositionMetadata position;
  }

  private static class PositionMetadata {
    public Double x;
    public Double y;
  }

  private static class PhysicalTagMetadata {
    public AssetMetadata asset;
  }

  private static class AssetMetadata {
    public String guid;
    public String name;
  }

  private static class GcpMetadata {
    public String mode;
    public String gateway_id;
  }
}
