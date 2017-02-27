package io.cdep.cdep.yml.cdepmanifest;

public class AndroidArchive {
    final public String file;
    final public String sha256;
    final public Long size;
    final public String ndk = null;
    final public String compiler = null;
    final public String runtime = null;
    final public String platform = null;
    final public String builder = null;
    final public String abis[] = null;
    final public String include;
    final public String lib = null;
    final public String flavor = null;
    private AndroidArchive() {
        this.file = null;
        this.sha256 = null;
        this.size = null;
        this.include = "include";
    }
}
