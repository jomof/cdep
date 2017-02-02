package com.jomofisher.cdep;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.Test;

public class TestGithubStyleUrlResolver {

  @Test
  public void testSimple() throws IOException {
    ResolvedManifest resolved = new GithubStyleUrlResolver()
        .resolve(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.29/cdep-manifest.yml");
    assertThat(resolved.manifest.coordinate.groupId).isEqualTo("com.github.jomof");
    assertThat(resolved.manifest.coordinate.artifactId).isEqualTo("cmakeify");
    assertThat(resolved.manifest.coordinate.version).isEqualTo("alpha-0.0.29");
    assertThat(resolved.manifest.android.length).isEqualTo(2);
  }
}
