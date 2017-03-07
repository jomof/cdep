package io.cdep.cdep.yml.cdepmanifest;

public class iOSArchive {

  final public String file;
  final public String sha256;
  final public Long size;
  final public String platform = null;
  final public String include;
  final public String lib = null;
  final public String flavor = null;

  private iOSArchive() {
    this.file = null;
    this.sha256 = null;
    this.size = null;
    this.include = "include";
  }
}
