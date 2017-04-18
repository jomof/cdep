package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.Nullable;

public class Interfaces {
  @Nullable
  final public Archive headers;

  public Interfaces() {
    this.headers = null;
  }

  public Interfaces(@Nullable Archive headers) {
    this.headers = headers;
  }
}
