package io.cdep.cdep.yml;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.utils.LongUtils;
import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;
import net.java.quickcheck.Generator;

import static io.cdep.cdep.yml.AndroidArchiveGenerator.Kind.missing;
import static net.java.quickcheck.generator.CombinedGenerators.arrays;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;

public class AndroidArchiveGenerator implements Generator<AndroidArchive> {
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
  final public Generator<String[]> libs = arrays(new ShortFilenameGenerator(), String.class);
  @NotNull
  final public Generator<String> strings = strings();
  @NotNull
  final public Generator<CxxLanguageFeatures[]> requires = arrays(enumValues(CxxLanguageFeatures.class),
      CxxLanguageFeatures.class);

  @Override
  public AndroidArchive next() {
    if (kind.next() == missing) {
      return null;
    }
    return new AndroidArchive(
        file.next(),
        sha256.next(),
        LongUtils.nullToZero(size.next()),
        strings.next(),
        strings.next(),
        strings.next(),
        strings.next(),
        strings.next(),
        strings.next(),
        include.next(),
        libs.next(),
        strings.next()
    );
  }

  enum Kind {
    missing, random
  }
}
