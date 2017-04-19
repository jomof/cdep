package io.cdep.cdep.yml;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;
import net.java.quickcheck.Generator;

import static io.cdep.cdep.yml.ArchiveGenerator.Kind.missing;
import static net.java.quickcheck.generator.CombinedGenerators.arrays;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;

public class ArchiveGenerator implements Generator<Archive> {
  @NotNull
  final public Generator<Kind> kind = enumValues(Kind.class);
  @NotNull
  final public Generator<String> file = new ShortFilenameGenerator();
  @NotNull
  final public Generator<String> sha256 = strings();
  @NotNull
  final public Generator<Long> size = longs();
  @NotNull
  final public Generator<String> include = strings();
  @NotNull
  final public Generator<CxxLanguageFeatures[]> requires = arrays(enumValues(CxxLanguageFeatures.class),
      CxxLanguageFeatures.class);

  @Override
  public Archive next() {
    if (kind.next() == missing) {
      return null;
    }
    return new Archive(
        file.next(),
        sha256.next(),
        size.next(),
        include.next(),
        requires.next()
    );
  }

  enum Kind {
    missing, random
  }
}