package io.cdep.cdep.yml.cdepmanifest;

public enum CDepManifestYmlVersion {
  v1, // Had android.abis in array
  v2, // Had top level archive instead of top level interface.headers
  v3, // Changed lib to libs in archives to support OpenSSL
  vlatest
}
