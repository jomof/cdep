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
}
