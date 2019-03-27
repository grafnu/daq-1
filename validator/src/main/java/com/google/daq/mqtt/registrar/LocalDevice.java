package com.google.daq.mqtt.registrar;

import static com.google.daq.mqtt.registrar.Registrar.ENVELOPE_JSON;
import static com.google.daq.mqtt.registrar.Registrar.METADATA_JSON;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.common.base.Preconditions;
import com.google.daq.mqtt.util.CloudDeviceSettings;
import com.google.daq.mqtt.util.CloudIotConfig;
import com.google.daq.mqtt.util.CloudIotManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.json.JSONTokener;

public class LocalDevice {

  private static final PrettyPrinter PROPER_PRETTY_PRINTER_POLICY = new ProperPrettyPrinterPolicy();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .enable(SerializationFeature.INDENT_OUTPUT)
      .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
      .enable(Feature.ALLOW_TRAILING_COMMA)
      .enable(Feature.STRICT_DUPLICATE_DETECTION)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setDateFormat(new ISO8601DateFormat())
      .setSerializationInclusion(Include.NON_NULL);

  private static final String RSA256_X509_PEM = "RSA_X509_PEM";
  private static final String RSA_PUBLIC_PEM = "rsa_public.pem";
  private static final String PHYSICAL_TAG_FORMAT = "%s_%s";
  private static final String PHYSICAL_TAG_ERROR = "Physical asset name %s does not match expected %s";

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

  private String metadataHash() {
    try {
      String savedHash = metadata.hash;
      metadata.hash = null;
      String json = metadataString();
      metadata.hash = savedHash;
      return String.format("%08x", Objects.hash(json));
    } catch (Exception e) {
      throw new RuntimeException("Converting object to string", e);
    }
  }

  private File metadataFile() {
    return new File(deviceDir, METADATA_JSON);
  }

  private Metadata validate(Metadata data) {
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
      settings.metadata = metadataString();
      return settings;
    } catch (Exception e) {
      throw new RuntimeException("While getting settings for device " + deviceId, e);
    }
  }

  private String metadataString() {
    try {
      return OBJECT_MAPPER.writeValueAsString(metadata);
    } catch (Exception e) {
      throw new RuntimeException("While converting metadata to string", e);
    }
  }

  public void validate(CloudIotConfig cloudIotConfig) {
    try {
      Envelope envelope = new Envelope();
      envelope.deviceId = deviceId;
      envelope.deviceRegistryId = cloudIotConfig.registry_id;
      // Don't use actual project id because it should be abstracted away.
      envelope.projectId = fakeProjectId();
      envelope.deviceNumId = makeNumId(envelope);
      String envelopeJson = OBJECT_MAPPER.writeValueAsString(envelope);
      schemas.get(ENVELOPE_JSON).validate(new JSONObject(new JSONTokener(envelopeJson)));
    } catch (Exception e) {
      throw new IllegalStateException("Validating envelope " + deviceId, e);
    }
    checkConsistency(cloudIotConfig.site_name);
  }

  private String fakeProjectId() {
    return metadata.system.location.site_name.toLowerCase();
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

  public boolean writeNormlized() {
    File metadataFile = metadataFile();
    try (OutputStream outputStream = new FileOutputStream(metadataFile)) {
      String writeHash = metadataHash();
      boolean update = metadata.hash == null || !metadata.hash.equals(writeHash);
      if (update) {
        metadata.timestamp = new Date();
        metadata.hash = metadataHash();
      }
      // Super annoying, but can't set this on the global static instance.
      JsonGenerator generator = OBJECT_MAPPER.getFactory()
          .createGenerator(outputStream)
          .setPrettyPrinter(PROPER_PRETTY_PRINTER_POLICY);
      OBJECT_MAPPER.writeValue(generator, metadata);
      return update;
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
    public Integer version;
    public Date timestamp;
    public String hash;
  }

  private static class PointsetMetadata {
    public Map<String, PointMetadata> points;
  }

  private static class SystemMetadata {
    public LocationMetadata location;
    public PhysicalTagMetadata physical_tag;
  }

  private static class PointMetadata {
    public String units;
  }

  private static class LocationMetadata {
    public String site_name;
    public String section;
    public Object position;
  }

  private static class PhysicalTagMetadata {
    public AssetMetadata asset;
  }

  private static class AssetMetadata {
    public String guid;
    public String name;
  }

  private static class ProperPrettyPrinterPolicy extends DefaultPrettyPrinter {
    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
      jg.writeRaw(": ");
    }
  }
}
