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
package io.cdep.resolver;

import io.cdep.ast.service.ResolvedManifest;
import io.cdep.generator.GeneratorEnvironment;
import io.cdep.utils.ManifestUtils;
import io.cdep.yml.cdep.Dependency;
import io.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.yml.cdepmanifest.Coordinate;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubStyleUrlResolver extends Resolver {
  final private Pattern pattern = Pattern.compile("^https://(.*)/(.*)/(.*)/releases/download/(.*)/cdep-manifest.yml$");

  @Override
  public ResolvedManifest resolve(GeneratorEnvironment environment,
      Dependency dependency, boolean forceRedownload) throws IOException {
    String coordinate = dependency.compile;
    assert coordinate != null;
    Matcher match = pattern.matcher(coordinate);
    if (match.find()) {

      String baseUrl = match.group(1);
      String segments[] = baseUrl.split("\\.");
      String groupId = "";
      for (int i = 0; i < segments.length; ++i) {
        groupId += segments[segments.length - i - 1];
        groupId += ".";
      }
      String user = match.group(2);
      groupId += user;
      String artifactId = match.group(3);
      String version = match.group(4);

      Coordinate provisionalCoordinate = new Coordinate(groupId, artifactId, version);
      String manifestContent = environment.getLocalDownloadedFileText(
          provisionalCoordinate,
          new URL(coordinate), forceRedownload);
      CDepManifestYml cdepManifestYml = ManifestUtils.convertStringToManifest(manifestContent);

      // Ensure that the manifest coordinate agrees with the url provided
      assert cdepManifestYml.coordinate != null;
      if (!groupId.equals(cdepManifestYml.coordinate.groupId)) {
        throw new RuntimeException(
            String.format("groupId '%s' from manifest did not agree with github url '%s",
                cdepManifestYml.coordinate.groupId,
                coordinate));
      }
      if (!artifactId.equals(cdepManifestYml.coordinate.artifactId)) {
        throw new RuntimeException(
            String.format("artifactId '%s' from manifest did not agree with github url '%s",
                cdepManifestYml.coordinate.artifactId,
                coordinate));
      }
      if (!version.equals(cdepManifestYml.coordinate.version)) {
        throw new RuntimeException(
            String.format("version '%s' from manifest did not agree with github url '%s",
                cdepManifestYml.coordinate.version,
                coordinate));
      }

      return new ResolvedManifest(new URL(coordinate), cdepManifestYml);
    }

    return null;
  }
}
