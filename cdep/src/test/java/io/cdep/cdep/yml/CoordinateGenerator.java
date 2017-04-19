package io.cdep.cdep.yml;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.Version;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

import java.util.List;

import static net.java.quickcheck.generator.PrimitiveGenerators.enumValues;
import static net.java.quickcheck.generator.PrimitiveGenerators.strings;

public class CoordinateGenerator implements Generator<Coordinate> {
  private static List<ResolvedManifests.TestManifest> allManifests = ResolvedManifests.allTestManifest();

  @NotNull
  final public Generator<Kind> kind = enumValues(Kind.class);
  @NotNull
  final public Generator<String> strings = strings();
  @NotNull
  final public Generator<Integer> integers = PrimitiveGenerators.integers();
  @NotNull
  final public Generator<Version> versions = new VersionGenerator();

  @Override
  public Coordinate next() {
    switch (kind.next()) {
      case missing:
        return Coordinate.EMPTY_COORDINATE;
      case random:
        return new Coordinate(strings.next(), strings.next(), versions.next());
    }
    return allManifests.get((Math.abs(integers.next()) % allManifests.size())).manifest.cdepManifestYml.coordinate;
  }

  enum Kind {
    missing, random, specific,
  }
}
