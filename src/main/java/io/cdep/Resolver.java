package io.cdep;

import java.io.IOException;

abstract class Resolver {
  final private static Resolver resolvers[] = new Resolver[] {
      new GithubStyleUrlResolver(),
      new GithubReleasesCoordinateResolver()
  };

  static ResolvedManifest resolveAny(String coordinate) throws IOException {
    ResolvedManifest resolved = null;
    for (Resolver resolver : resolvers) {
      ResolvedManifest attempt = resolver.resolve(coordinate);
      if (attempt != null) {
        if (resolved != null) {
          throw new RuntimeException("Multiple resolvers matched coordinate: " + coordinate);
        }
        resolved = attempt;
      }
    }
    return resolved;
  }

  abstract ResolvedManifest resolve(String coordinate) throws IOException;
}
