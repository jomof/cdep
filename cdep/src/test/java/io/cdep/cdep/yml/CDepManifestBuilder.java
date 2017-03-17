package io.cdep.cdep.yml;


import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;

public class CDepManifestBuilder {

  public static HardNameDependency hardname(String compile, String sha256) {
    return new HardNameDependency(compile, sha256);
  }

  public static Archive archive(String file, String sha256, long size) {
    return new Archive(file, sha256, size);
  }
}
