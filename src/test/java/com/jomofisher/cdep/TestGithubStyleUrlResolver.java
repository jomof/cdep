package com.jomofisher.cdep;

import java.io.IOException;
import org.junit.Test;

/**
 * Created by jomof on 1/30/17.
 */
public class TestGithubStyleUrlResolver {

  @Test
  public void testSimple() throws IOException {
    ResolvedDependency dependency = new GithubStyleUrlResolver()
        .resolve("https://github.com/jomof/cmakeify/releases/download/alpha-0.0.27/cdep-manifest.yml");
  }
}
