package io.cdep.cdep.resolver;

import io.cdep.annotations.Nullable;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public interface ManifestProvider {
  @Nullable
  CDepManifestYml tryGetManifest(Coordinate coordinate, URL remoteArchive) throws IOException, NoSuchAlgorithmException;
}
