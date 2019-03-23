package com.google.daq.mqtt.util;

public class CloudIotConfig {
  private String projectId;
  public String registry_id;
  public String cloud_region;
  public String site_name;

  public void addProject(String projectId) {
    this.projectId = projectId;
  }

  public String getProjectId() {
    return projectId;
  }
}
