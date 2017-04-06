package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
public class iOSArchive {

  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public iOSPlatform platform;
  @Nullable
  final public iOSArchitecture architecture;
  @Nullable
  final public String sdk;
  @Nullable
  final public String include;
  @Nullable
  final public String lib;
  @Nullable
  final public String flavor;

  private iOSArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.platform = null;
    this.architecture = null;
    this.sdk = null;
    this.lib = null;
    this.flavor = null;
    this.include = null;
  }
}
