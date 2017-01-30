package com.jomofisher.cdep;

import static com.google.common.truth.Truth.assertThat;

import com.jomofisher.cdep.manifest.Manifest;
import java.io.IOException;
import org.junit.Test;

public class TestGithubStyleUrlResolver {

  @Test
  public void testSimple() throws IOException {
    Manifest manifest = new GithubStyleUrlResolver()
        .resolve(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.29/cdep-manifest.yml");
    assertThat(manifest.coordinate.groupId).isEqualTo("com.github.jomof");
    assertThat(manifest.coordinate.artifactId).isEqualTo("cmakeify");
    assertThat(manifest.coordinate.version).isEqualTo("alpha-0.0.29");
    assertThat(manifest.android.length).isEqualTo(2);
  }
}
