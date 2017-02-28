package io.cdep.cdep.resolver;

import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class TestGithubReleasesCoordinateResolver {
  final private GeneratorEnvironment environment = new GeneratorEnvironment(
      System.out,
      new File("./test-files/TestGithubReleasesCoordinateResolver/working"),
      null);

  @Test
  public void testCompound() throws IOException {
    ResolvedManifest resolved = new GithubReleasesCoordinateResolver()
        .resolve(environment, new SoftNameDependency(
                "com.github.jomof:firebase/database:2.1.3-rev5"),
            false);
    assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
    assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("firebase/database");
    assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("2.1.3-rev5");
    assertThat(resolved.cdepManifestYml.android.archives.length).isEqualTo(3);
  }
}
