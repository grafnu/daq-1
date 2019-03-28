package com.google.daq.mqtt.util;

import static com.google.daq.mqtt.util.ConfigUtil.authorizeServiceAccount;
import static com.google.daq.mqtt.util.ConfigUtil.readCloudIotConfig;
import static com.google.daq.mqtt.util.ConfigUtil.readGcpCreds;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.core.ApiFuture;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PubsubMessage.Builder;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PubSubPusher {

  private final String projectId = ServiceOptions.getDefaultProjectId();

  private final GcpCreds configuration;
  private final CloudIotConfig cloudIotConfig;
  private final Publisher publisher;
  private final String registrar_topic;

  public PubSubPusher(File gcpCred, File iotConfigFile) {
    try {
      configuration = readGcpCreds(gcpCred);
      cloudIotConfig = validate(readCloudIotConfig(iotConfigFile));
      registrar_topic = cloudIotConfig.registrar_topic;
      ProjectTopicName topicName =
          ProjectTopicName.of(configuration.project_id, registrar_topic);
      Preconditions.checkState(projectId.equals(configuration.project_id));
      publisher = Publisher.newBuilder(topicName).build();
    } catch (Exception e) {
      throw new RuntimeException("While creating PubSubPublisher", e);
    }
  }

  public String sendMessage(String deviceId, String message) {
    try {
      Builder builder = PubsubMessage.newBuilder().setData(
          ByteString.copyFrom(message, Charset.defaultCharset()));
      builder.putAttributes("deviceId", deviceId);
      builder.putAttributes("registryId", cloudIotConfig.registry_id);
      builder.putAttributes("projectId", configuration.project_id);
      builder.putAttributes("subFolder", "metadata");
      PubsubMessage helloMessage = builder.build();
      ApiFuture<String> publish = publisher.publish(helloMessage);
      return publish.get();
    } catch (Exception e) {
      throw new RuntimeException("While sending to topic " + registrar_topic, e);
    }
  }

  public void shutdown() {
    try {
      publisher.publishAllOutstanding();
      publisher.shutdown();
      System.err.println("Done with PubSubPusher");
    } catch (Exception e) {
      throw new RuntimeException("While shutting down publisher" + registrar_topic, e);
    }
  }

  private CloudIotConfig validate(CloudIotConfig readCloudIotConfig) {
    Preconditions.checkNotNull(readCloudIotConfig.registrar_topic, "registrar_topic not defined");
    return readCloudIotConfig;
  }
}
