package io.cdep.cdep.yml;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.Version;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

import java.util.List;

import static net.java.quickcheck.generator.PrimitiveGenerators.enumValues;
import static net.java.quickcheck.generator.PrimitiveGenerators.strings;

public class VersionGenerator  implements Generator<Version> {
    private static List<ResolvedManifests.TestManifest> allManifests = ResolvedManifests.allTestManifest();

  @NotNull
  final public Generator<Kind> kind = enumValues(Kind.class);
  @NotNull
  final public Generator<String> strings = strings();
  @NotNull
  final public Generator<Integer> integers = PrimitiveGenerators.integers();

  @Override
  public Version next() {
    switch(kind.next()) {
      case missing: return Version.EMPTY_VERSION;
      case random:
        return new Version(strings.next());
    }
    return allManifests.get((Math.abs(integers.next()) % allManifests.size()))
        .manifest.cdepManifestYml.coordinate.version;
  }

  enum Kind {
    missing, random, specific
  }
}
