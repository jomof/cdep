package io.cdep.cdep.yml;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;
import net.java.quickcheck.Generator;

import static net.java.quickcheck.generator.PrimitiveGenerators.strings;

public class HardnameGenerator implements Generator<HardNameDependency> {
  Generator<Coordinate> compileGenerator = new CoordinateGenerator();
  Generator<String> sha256Generator = strings();

  @Override
  public HardNameDependency next() {
    String compile = compileGenerator.next().toString();
    String sha256 = sha256Generator.next();
    return new HardNameDependency(compile, sha256);
  }
}
