package com.jomofisher.cdep;

import java.io.IOException;

abstract class Resolver {
  final private static Resolver resolvers[] = new Resolver[] {
      new GithubStyleUrlResolver()
  };

  abstract ResolvedDependency resolve(String coordinate) throws IOException;

  static ResolvedDependency resolveAny(String coordinate) throws IOException {
    ResolvedDependency resolved = null;
    for (Resolver resolver : resolvers) {
      ResolvedDependency attempt = resolver.resolve(coordinate);
      if (attempt != null) {
        if (resolved != null) {
          throw new RuntimeException("Multiple resolvers matched coordinate: " + coordinate);
        }
        resolved = attempt;
      }
    }
    return resolved;
  }
}
