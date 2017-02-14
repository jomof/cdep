package io.cdep.service;

import static com.google.common.truth.Truth.assertThat;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.yml.cdep.Dependency;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TestGithubStyleUrlResolver {

  final private GeneratorEnvironment environment = new GeneratorEnvironment(
      System.out,
      new File("./test-files/TestFindModuleFunctionTableBuilder/working"),
      null);
  @Test
  public void testSimple() throws IOException {
    ResolvedManifest resolved = new GithubStyleUrlResolver()
        .resolve(environment, new Dependency(
                "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.29/cdep-manifest.yml"),
            false);
    assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
    assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("cmakeify");
    assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("alpha-0.0.29");
    assertThat(resolved.cdepManifestYml.android.length).isEqualTo(2);
  }
}
