package com.google.daq.mqtt.util;

import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.api.services.cloudiot.v1.model.DeviceRegistry;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class CloudIotManager {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final GcpCreds configuration;
  private final CloudIotConfig cloudIotConfig;

  private Map<String, Set<String>> registryDevices = new HashMap<>();
  private CloudIot cloudIotService;
  private String projectPath;
  private CloudIot.Projects.Locations.Registries cloudIotRegistries;

  public CloudIotManager(File gcpCred, File cloudIotConfig) {
    configuration = readGcpCreds(gcpCred);
    this.cloudIotConfig = readCloudIotConfig(cloudIotConfig);
    loadPublicKeyData();
    initializeCloudIoT(gcpCred);
    Set<String> registryList = makeProjectRegistryList();
    System.err.print("Available Registries:\n  ");
    System.err.println(Joiner.on("\n  ").join(registryList));
  }

  private void blockRemainingDevices() {
    for (String registryId : registryDevices.keySet()) {
      for (String deviceId : registryDevices.get(registryId)) {
        updateDevice(registryId, deviceId, true);
      }
    }
  }

  static GcpCreds readGcpCreds(File configFile) {
    try {
      return OBJECT_MAPPER.readValue(configFile, GcpCreds.class);
    } catch (Exception e) {
      throw new RuntimeException("While reading config file "+ configFile.getAbsolutePath(), e);
    }
  }

  static CloudIotConfig readCloudIotConfig(File configFile) {
    try {
      return OBJECT_MAPPER.readValue(configFile, CloudIotConfig.class);
    } catch (Exception e) {
      throw new RuntimeException("While reading config file "+ configFile.getAbsolutePath(), e);
    }
  }

  private void loadPublicKeyData() {
    // Preconditions.checkNotNull(configuration.public_key_file, "Configuration public_key_file");
    // File keyFile = new File(configuration.public_key_file);
    // try {
    //   configuration.public_key_data = IOUtils.toString(new FileInputStream(keyFile), Charset.defaultCharset());
    // } catch (Exception e) {
    //   throw new RuntimeException("Fetching public key file " + keyFile.getAbsolutePath(), e);
    // }
  }

  private Set<String> makeProjectRegistryList() {
    try {
      System.err.println("Listing project registries for " + projectPath);
      List<DeviceRegistry>
          existingRegistries =
          cloudIotRegistries.list(projectPath).execute().getDeviceRegistries();
      Set<String> remainingRegistries = new HashSet<>();
      existingRegistries.forEach(registry -> remainingRegistries.add(registry.getId()));
      return remainingRegistries;
    } catch (Exception e) {
      throw new RuntimeException("While making the Cloud IoT project registries list", e);
    }
  }

  private String getRegistryPath(String registryId) {
    return projectPath + "/registries/" + registryId;
  }

  private String getDevicePath(String registryId, String deviceId) {
    return getRegistryPath(registryId) + "/devices/" + deviceId;
  }

  private GoogleCredential authorizeServiceAccount(File credFile) {
    try (FileInputStream credStream = new FileInputStream(credFile)) {
      return GoogleCredential
          .fromStream(credStream)
          .createScoped(CloudIotScopes.all());
    } catch (Exception e) {
      throw new RuntimeException("While reading cred file " + credFile.getAbsolutePath(), e);
    }
  }

  private void initializeCloudIoT(File gcpCredFile) {
    projectPath = "projects/" + configuration.project_id + "/locations/" + cloudIotConfig.cloud_region;
    try {
      GoogleCredential credential = authorizeServiceAccount(gcpCredFile);
      System.err.println(String.format("Using service account %s/%s",
          credential.getServiceAccountId(), credential.getServiceAccountUser()));
      JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
      HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
      cloudIotService =
          new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
              .setApplicationName("com.google.iot.bos")
              .build();
      cloudIotRegistries = cloudIotService.projects().locations().registries();
      System.err.println("Created service for project " + configuration.project_id);
    } catch (Exception e) {
      throw new RuntimeException("While initializing Cloud IoT project " + projectPath, e);
    }
  }

  private void registerDevice(String registryId, String deviceId) throws Exception {
    Preconditions.checkNotNull(cloudIotService, "CloudIoT service not initialized");
    Device device = getDevice(registryId, deviceId);
    if (device == null) {
      createDevice(registryId, deviceId);
    } else if (!Boolean.FALSE.equals(device.getBlocked())) {
      if (registryDevices.containsKey(registryId)) {
        registryDevices.get(registryId).remove(deviceId);
      }
      updateDevice(registryId, deviceId, false);
    }
  }

  private boolean isDefined(String parameter) {
    return !Strings.isNullOrEmpty(parameter);
  }

  private void updateDevice(String registryId, String deviceId,
      boolean isBlocked) {
    try {
      System.err.println("Update device " + registryId + "/" + deviceId + ", blocked=" + isBlocked);
      Device device = new Device();
      device.setBlocked(isBlocked);
      device.setCredentials(getCredentials());
      String path = getDevicePath(registryId, deviceId);
      cloudIotRegistries.devices().patch(path, device).setUpdateMask("blocked,credentials").execute();
    } catch (Exception e) {
      throw new RuntimeException(String.format("While updating device %s/%s", registryId, deviceId), e);
    }
  }

  private void createDevice(String registryId, String deviceId) throws IOException {
    System.err.println("Create device " + registryId + "/" + deviceId);
    String parent = getRegistryPath(registryId);
    Device device = new Device().setId(deviceId);
    device.setCredentials(getCredentials());
    try {
      cloudIotRegistries.devices().create(parent, device).execute();
    } catch (GoogleJsonResponseException e) {
      throw new RuntimeException("Remote error creating device: " + e.getDetails().getMessage());
    }
  }

  private List<DeviceCredential> getCredentials() {
    // Preconditions.checkNotNull(configuration.public_key_format, "Key format not configured");
    // Preconditions.checkNotNull(configuration.public_key_data, "Key data not configured");
    //
    // PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    // publicKeyCredential.setFormat(configuration.public_key_format);
    // publicKeyCredential.setKey(configuration.public_key_data);
    //
    // DeviceCredential deviceCredential = new DeviceCredential();
    // deviceCredential.setPublicKey(publicKeyCredential);
    DeviceCredential deviceCredential = new DeviceCredential();
    return ImmutableList.of(deviceCredential);
  }

  public List<Device> listDevices(String registryId) {
    Preconditions.checkNotNull(cloudIotService, "CloudIoT service not initialized");
    try {
      List<Device> devices = cloudIotRegistries
          .devices()
          .list(getRegistryPath(registryId))
          .execute()
          .getDevices();
      return devices == null ? ImmutableList.of() : devices;
    } catch (Exception e) {
      throw new RuntimeException("While listing devices for registry " + registryId, e);
    }
  }

  public void removeDevice(String registryId, String deviceId) throws IOException {
    try {
      cloudIotRegistries.devices().delete(getDevicePath(registryId, deviceId)).execute();
    } catch (GoogleJsonResponseException e) {
      if (e.getDetails().getCode() == 404) {
        return;
      }
      throw new RuntimeException("Remote error removing device: " + e.getDetails().getMessage());
    }
  }

  public Device getDevice(String registryId, String deviceId) throws IOException {
    try {
      return cloudIotRegistries.devices().get(getDevicePath(registryId, deviceId)).execute();
    } catch (GoogleJsonResponseException e) {
      if (e.getDetails().getCode() == 404) {
        return null;
      }
      throw new RuntimeException("Remote error getting device: " + e.getDetails().getMessage());
    }
  }

  private Set<String> setupRegistry(String registryId) {
    try {
      Set<String> devices = new HashSet<>();
      devices.addAll(listDevices(registryId).stream().map(Device::getId).collect(toSet()));
      return devices;
    } catch (Exception e) {
      throw new RuntimeException("While listing devices for registry " + registryId, e);
    }
  }

}
