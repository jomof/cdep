package io.cdep.cdep.yml.cdepsha25;

import io.cdep.annotations.Nullable;

@SuppressWarnings("unused")
public class HashEntry {
  @Nullable
  final public String coordinate;
  @Nullable
  final public String sha256;

  private HashEntry() {
    this.coordinate = null;
    this.sha256 = null;
  }

  public HashEntry(@Nullable String coordinate, @Nullable String sha256) {
    this.coordinate = coordinate;
    this.sha256 = sha256;
  }
}
