package com.google.daq.mqtt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.google.api.services.cloudiot.v1.model.PublicKeyCredential;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class CloudIotManager {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String DEVICE_UPDATE_MASK = "blocked,credentials,metadata";
  private static final String PROFILE_KEY = "profile";
  private static final String SCHEMA_KEY = "schema_name";
  private static final int LIST_PAGE_SIZE = 1000;

  private final GcpCreds configuration;
  private final CloudIotConfig cloudIotConfig;

  private final String registryId;

  private CloudIot cloudIotService;
  private String projectPath;
  private CloudIot.Projects.Locations.Registries cloudIotRegistries;
  private Map<String, Device> deviceMap;
  private String schemaName;

  public CloudIotManager(File gcpCred, File iotConfigFile, String schemaName) {
    configuration = readGcpCreds(gcpCred);
    cloudIotConfig = readCloudIotConfig(iotConfigFile);
    registryId = cloudIotConfig.registry_id;
    this.schemaName = schemaName;
    initializeCloudIoT(gcpCred);
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
      return validate(OBJECT_MAPPER.readValue(configFile, CloudIotConfig.class));
    } catch (Exception e) {
      throw new RuntimeException("While reading config file "+ configFile.getAbsolutePath(), e);
    }
  }

  private static CloudIotConfig validate(CloudIotConfig cloudIotConfig) {
    Preconditions.checkNotNull(cloudIotConfig.registry_id, "registry_id not defined");
    Preconditions.checkNotNull(cloudIotConfig.cloud_region, "cloud_region not defined");
    Preconditions.checkNotNull(cloudIotConfig.site_name, "site_name not defined");
    return cloudIotConfig;
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

  public boolean registerDevice(String deviceId, CloudDeviceSettings settings) {
    try {
      Preconditions.checkNotNull(cloudIotService, "CloudIoT service not initialized");
      Preconditions.checkNotNull(deviceMap, "deviceMap not initialized");
      Device device = deviceMap.get(deviceId);
      if (device == null) {
        createDevice(deviceId, settings);
        return true;
      } else {
        updateDevice(deviceId, settings, device);
      }
      return false;
    } catch (Exception e) {
      throw new RuntimeException("While registering device " + deviceId, e);
    }
  }

  public void blockDevice(String deviceId, boolean blocked) {
    try {
      Device device = new Device();
      device.setBlocked(blocked);
      String path = getDevicePath(registryId, deviceId);
      cloudIotRegistries.devices().patch(path, device).setUpdateMask("blocked").execute();
    } catch (Exception e) {
      throw new RuntimeException(String.format("While (un)blocking device %s/%s=%s", registryId, deviceId, blocked), e);
    }
  }

  private Device makeDevice(String deviceId, CloudDeviceSettings settings,
      Device oldDevice) {
    Map<String, String> metadataMap = oldDevice == null ? null : oldDevice.getMetadata();
    if (metadataMap == null) {
      metadataMap = new HashMap<>();
    }
    metadataMap.put(PROFILE_KEY, settings.metadata);
    metadataMap.put(SCHEMA_KEY, schemaName);
    return new Device()
        .setId(deviceId)
        .setCredentials(settings.credentials)
        .setMetadata(metadataMap);
  }

  private void createDevice(String deviceId, CloudDeviceSettings settings) throws IOException {
    try {
      cloudIotRegistries.devices().create(getRegistryPath(registryId),
          makeDevice(deviceId, settings, null)).execute();
    } catch (GoogleJsonResponseException e) {
      throw new RuntimeException("Remote error creating device " + deviceId, e);
    }
  }

  private void updateDevice(String deviceId, CloudDeviceSettings settings,
      Device oldDevice) {
    try {
      Device device = makeDevice(deviceId, settings, oldDevice)
          .setId(null)
          .setNumId(null);
      cloudIotRegistries
          .devices()
          .patch(getDevicePath(registryId, deviceId), device).setUpdateMask(DEVICE_UPDATE_MASK)
          .execute();
    } catch (Exception e) {
      throw new RuntimeException("Remote error patching device " + deviceId, e);
    }
  }

  public static List<DeviceCredential> makeCredentials(String keyFormat, String keyData) {
    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    publicKeyCredential.setFormat(keyFormat);
    publicKeyCredential.setKey(keyData);

    DeviceCredential deviceCredential = new DeviceCredential();
    deviceCredential.setPublicKey(publicKeyCredential);
    return ImmutableList.of(deviceCredential);
  }

  public Map<String, Device> fetchDevices() {
    Preconditions.checkNotNull(cloudIotService, "CloudIoT service not initialized");
    try {
      List<Device> devices = cloudIotRegistries
          .devices()
          .list(getRegistryPath(registryId))
          .setPageSize(LIST_PAGE_SIZE)
          .execute()
          .getDevices();
      if (devices.size() == LIST_PAGE_SIZE) {
        throw new RuntimeException("Returned exact page size, likely not fetched all devices");
      }
      deviceMap = new HashMap<>();
      devices.stream().map(Device::getId)
          .forEach(deviceName -> {
            try {
              System.err.println("Fetching remote device " + deviceName);
              deviceMap.put(deviceName, fetchDevice(deviceName));
            } catch (IOException e) {
              throw new RuntimeException("While fetching device " + deviceName, e);
            }
          });
      return deviceMap;
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

  private Device fetchDevice(String deviceId) throws IOException {
    try {
      return cloudIotRegistries.devices().get(getDevicePath(registryId, deviceId)).execute();
    } catch (GoogleJsonResponseException e) {
      if (e.getDetails().getCode() == 404) {
        return null;
      }
      throw new RuntimeException("Remote error getting device: " + e.getDetails().getMessage());
    }
  }

  public CloudIotConfig getCloudIotConfig() {
    return cloudIotConfig;
  }
}
