package io.cdep.cdep.yml;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdepmanifest.LinuxArchive;
import net.java.quickcheck.Generator;

import static net.java.quickcheck.generator.CombinedGenerators.arrays;
import static net.java.quickcheck.generator.PrimitiveGenerators.*;

public class LinuxArchiveGenerator implements Generator<LinuxArchive> {
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

  @Override
  public LinuxArchive next() {
    if (kind.next() == Kind.missing) {
      return null;
    }
    return new LinuxArchive(
        file.next(),
        sha256.next(),
        size.next(),
        libs.next(),
        include.next());
  }

  enum Kind {
    missing, random
  }
}
