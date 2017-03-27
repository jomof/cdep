package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

public class iOSArchive {

  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public iOSPlatform platform = null;
  @Nullable
  final public iOSArchitecture architecture = null;
  @Nullable
  final public String sdk = null;
  @org.jetbrains.annotations.NotNull
  @NotNull
  final public String include;
  @Nullable
  final public String lib = null;
  @Nullable
  final public String flavor = null;

  private iOSArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.include = "include";
  }
}
