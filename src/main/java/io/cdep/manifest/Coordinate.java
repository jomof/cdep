package io.cdep.manifest;

@SuppressWarnings("unused")
public class Coordinate {

  final public String groupId; // like com.github.jomof
  final public String artifactId; // like cmakeify
  final public String version; // like alpha-0.0.27

  private Coordinate() {
    groupId = null;
    artifactId = null;
    version = null;
  }

    public Coordinate(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Coordinate)) {
      return false;
    }
    return toString().equals(obj);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    return groupId + ":" + artifactId + ":" + version;
  }
}
