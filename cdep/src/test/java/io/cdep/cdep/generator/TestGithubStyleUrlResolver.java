/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package io.cdep.cdep.generator;

import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.resolver.GithubStyleUrlResolver;
import io.cdep.cdep.yml.cdep.Dependency;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

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
