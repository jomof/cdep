package io.cdep.cdep.yml.cdepmanifest;

public class LinuxArchive {
  final public String file;
  final public String sha256;
  final public Long size;
  final public String lib;
  final public String include;

  LinuxArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.lib = null;
    this.include = "include";
  }

  public LinuxArchive(String file, String sha256, Long size, String lib, String include) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.lib = lib;
    this.include = include;
  }
}
