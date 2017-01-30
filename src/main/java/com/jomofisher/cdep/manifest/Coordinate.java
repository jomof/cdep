package com.jomofisher.cdep.manifest;

/**
 * Created by jomof on 1/30/17.
 */
public class Coordinate {

  final public String groupId; // like com.github.jomof
  final public String artifactId; // like cmakeify
  final public String version; // like alpha-0.0.27

  private Coordinate() {
    groupId = null;
    artifactId = null;
    version = null;
  }

  Coordinate(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }
}
