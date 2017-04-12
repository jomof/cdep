package io.cdep.cdep.resolver;

import static com.google.common.truth.Truth.assertThat;

import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import java.io.File;
import org.junit.Test;

public class TestGithubReleasesCoordinateResolver {
  final private GeneratorEnvironment environment = new GeneratorEnvironment(new File(""  +
      "./test-files/TestGithubReleasesCoordinateResolver/working"), null, false, false);

  @Test
  public void testCompound() throws Exception {
    ResolvedManifest resolved = new GithubReleasesCoordinateResolver().resolve(environment, new SoftNameDependency("com.github"
        + ".jomof:firebase/database:2.1.3-rev5"));
    assert resolved != null;
    assert resolved.cdepManifestYml.coordinate != null;
    assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
    assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("firebase/database");
    assertThat(resolved.cdepManifestYml.coordinate.version.value).isEqualTo("2.1.3-rev5");
    assert resolved.cdepManifestYml.android != null;
    assert resolved.cdepManifestYml.android.archives != null;
    assertThat(resolved.cdepManifestYml.android.archives.length).isEqualTo(21);
  }
}
