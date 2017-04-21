package io.cdep.cdep.yml;

import net.java.quickcheck.Generator;

import static net.java.quickcheck.generator.PrimitiveGenerators.integers;
import static net.java.quickcheck.generator.PrimitiveGenerators.strings;

public class AbiGenerator implements Generator<String> {
  Generator<String> strings = strings();
  Generator<Integer> integers = integers();

  @Override
  public String next() {
    switch (Math.abs(integers.next()) % 3) {
      case 0:
        return "x86_64";
      case 1:
        return "mips";
      default:
        return strings.next();
    }
  }
}
