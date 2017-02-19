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
package io.cdep.cdep.resolver;

import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdep.Dependency;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFilePathResolver extends Resolver {

    @Override
    public ResolvedManifest resolve(GeneratorEnvironment environment, Dependency dependency,
        boolean forceRedownload)
        throws IOException {
        String coordinate = dependency.compile;
        assert coordinate != null;
        File local = new File(coordinate);
        if (!local.isFile()) {
            return null;
        }
        String content = new String(Files.readAllBytes(Paths.get(local.getCanonicalPath())));
        CDepManifestYml cdepManifestYml = CDepManifestYmlUtils.convertStringToManifest(content);
        CDepManifestYmlUtils.checkManifestSanity(cdepManifestYml);

        if (dependency.enforceSourceUrlMatchesManifest == null
            || dependency.enforceSourceUrlMatchesManifest) {
            // Ensure that the manifest coordinate agrees with the url provided
            assert cdepManifestYml.coordinate != null;
            assert cdepManifestYml.coordinate.groupId != null;
            if (!coordinate.contains(cdepManifestYml.coordinate.groupId)) {
                throw new RuntimeException(
                    String.format("local file name '%s' did not contain groupId '%s'"
                            + "", coordinate,
                        cdepManifestYml.coordinate.groupId));
            }
            assert cdepManifestYml.coordinate.artifactId != null;
            if (!coordinate.contains(cdepManifestYml.coordinate.artifactId)) {
                throw new RuntimeException(
                    String.format("local file name '%s' did not contain artifactId '%s'"
                            + "", coordinate,
                        cdepManifestYml.coordinate.artifactId));
            }
            assert cdepManifestYml.coordinate.version != null;
            if (!coordinate.contains(cdepManifestYml.coordinate.version)) {
                throw new RuntimeException(
                    String.format("local file name '%s' did not contain version '%s'"
                            + "", coordinate,
                        cdepManifestYml.coordinate.version));
            }
        }

        return new ResolvedManifest(local.getCanonicalFile().toURI().toURL(), cdepManifestYml);
    }
}
