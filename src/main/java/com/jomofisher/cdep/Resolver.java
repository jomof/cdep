package com.jomofisher.cdep;

import com.jomofisher.cdep.manifest.Manifest;
import java.io.IOException;

abstract class Resolver {
  final private static Resolver resolvers[] = new Resolver[] {
      new GithubStyleUrlResolver()
  };

  static Manifest resolveAny(String coordinate) throws IOException {
    Manifest resolved = null;
    for (Resolver resolver : resolvers) {
      Manifest attempt = resolver.resolve(coordinate);
      if (attempt != null) {
        if (resolved != null) {
          throw new RuntimeException("Multiple resolvers matched coordinate: " + coordinate);
        }
        resolved = attempt;
      }
    }
    return resolved;
  }

  abstract Manifest resolve(String coordinate) throws IOException;
}
