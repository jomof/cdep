package io.cdep.cdep.yml;

import io.cdep.annotations.NotNull;
import net.java.quickcheck.Generator;

import static net.java.quickcheck.generator.PrimitiveGenerators.enumValues;
import static net.java.quickcheck.generator.PrimitiveGenerators.strings;

public class ShortFilenameGenerator implements Generator<String> {
  @NotNull
  final public Generator<String> string = strings();
  @NotNull
  final public Generator<Kind> kind = enumValues(Kind.class);

  @Override
  public String next() {
    switch (kind.next()) {
      case zip:
        return "archive.zip";
      case dota:
        return "lib.a";
      case dotso:
        return "lib.so";
      case doth:
        return "header.h";
    }

    return string.next();
  }

  enum Kind {
    random, zip, dota, dotso, doth
  }
}
