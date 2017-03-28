package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.Nullable;

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
  final public String abis[];
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
    this.abis = null;
    this.include = "include";
    this.lib = null;
    this.flavor = null;
  }
}
