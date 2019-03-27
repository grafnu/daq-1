package com.google.daq.mqtt.util;

import static com.google.daq.mqtt.util.ConfigUtil.authorizeServiceAccount;
import static com.google.daq.mqtt.util.ConfigUtil.readCloudIotConfig;
import static com.google.daq.mqtt.util.ConfigUtil.readGcpCreds;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.base.Preconditions;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PubSubPusher {

  private final String projectId = ServiceOptions.getDefaultProjectId();

  private final GcpCreds configuration;
  private final CloudIotConfig cloudIotConfig;

  public PubSubPusher(File gcpCred, File iotConfigFile) {
    try {
      configuration = readGcpCreds(gcpCred);
      cloudIotConfig = validate(readCloudIotConfig(iotConfigFile));
      ProjectTopicName topicName =
          ProjectTopicName.of(configuration.project_id, cloudIotConfig.registrar_topic);
      Credentials credentials = getProjectCredentials();
      Preconditions.checkState(projectId.equals(configuration.project_id));
      GoogleCredential credential = authorizeServiceAccount(gcpCred);
      System.err.println(String.format("Using service account %s/%s",
          credential.getServiceAccountId(), credential.getServiceAccountUser()));
      Publisher publisher = Publisher.newBuilder(topicName).build();
      PubsubMessage helloMessage = PubsubMessage.newBuilder().build();
      publisher.publish(helloMessage);
      publisher.publishAllOutstanding();
      publisher.shutdown();
    } catch (Exception e) {
      throw new RuntimeException("While creating PubSubPublisher", e);
    }
  }

  private Credentials getProjectCredentials() throws IOException {
    File credentialFile = new File(System.getenv(ServiceOptions.CREDENTIAL_ENV_NAME));
    if (!credentialFile.exists()) {
      throw new RuntimeException(String.format("Missing file %s from %s",
          credentialFile.getAbsolutePath(), ServiceOptions.CREDENTIAL_ENV_NAME));
    }
    try (FileInputStream serviceAccount = new FileInputStream(credentialFile)) {
      return GoogleCredentials.fromStream(serviceAccount);
    }
  }


  private CloudIotConfig validate(CloudIotConfig readCloudIotConfig) {
    Preconditions.checkNotNull(readCloudIotConfig.registrar_topic, "registrar_topic not defined");
    return readCloudIotConfig;
  }
}
