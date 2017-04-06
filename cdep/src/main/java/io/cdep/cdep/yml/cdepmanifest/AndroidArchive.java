package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.Nullable;

import static io.cdep.cdep.utils.Invariant.require;

@SuppressWarnings("unused")
public class AndroidArchive {
  @Nullable
  final public String file;
  @Nullable
  final public String sha256;
  @Nullable
  final public Long size;
  @Nullable
  final public String ndk;
  @Nullable
  final public String compiler;
  @Nullable
  final public String runtime;
  @Nullable
  final public String platform;
  @Nullable
  final public String builder;
  @Nullable
  final public String abi;
  @Nullable
  final public String include;
  @Nullable
  final public String lib;
  @Nullable
  final public String flavor;

  private AndroidArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.ndk = null;
    this.compiler = null;
    this.runtime = null;
    this.platform = null;
    this.builder = null;
    this.abi = null;
    this.include = null;
    this.lib = null;
    this.flavor = null;
  }

  public AndroidArchive(String file,
      String sha256,
      Long size,
      String ndk,
      String compiler,
      String runtime,
      String platform,
      String builder,
      String abi,
      String include,
      String lib,
      String flavor) {
    require(abi != null);
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.ndk = ndk;
    this.compiler = compiler;
    this.runtime = runtime;
    this.platform = platform;
    this.builder = builder;
    this.abi = abi;
    this.include = include;
    this.lib = lib;
    this.flavor = flavor;
    require(this.abi != null);
  }
}
