package io.cdep.cdep.yml.cdepmanifest;

public class Archive {
    final public String file;
    final public String sha256;
    final public Long size;
    private Archive() {
        this.file = null;
        this.sha256 = null;
        this.size = null;
    }

  public Archive(String file, String sha256, long size) {
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
  }
}
