package io.cdep.cdep.yml;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.yml.cdepmanifest.*;
import net.java.quickcheck.Generator;

import static net.java.quickcheck.generator.CombinedGenerators.arrays;
import static net.java.quickcheck.generator.PrimitiveGenerators.enumValues;
import static net.java.quickcheck.generator.PrimitiveGenerators.strings;

/**
 * Created by jomof on 4/18/2017.
 */
public class CDepManifestYmlGenerator implements Generator<CDepManifestYml> {
  Generator<CDepManifestYmlVersion> versionGenerator = enumValues(CDepManifestYmlVersion.class);
  Generator<HardNameDependency[]> dependenciesGenerator = arrays(new HardnameGenerator(), HardNameDependency.class);
  Generator<String> exampleGenerator = strings();
  Generator<Archive> archiveGenerator = new ArchiveGenerator();

  @Override
  public CDepManifestYml next() {

    return new CDepManifestYml(
        versionGenerator.next(),
        Coordinate.EMPTY_COORDINATE,
        dependenciesGenerator.next(),
        new Interfaces(archiveGenerator.next()),
        null,
        null,
        null,
        exampleGenerator.next());
  }
}
