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
import io.cdep.cdep.yml.cdep.SoftNameDependency;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class GithubReleasesCoordinateResolver extends Resolver {

    final private Pattern pattern = compile("^com\\.github\\.(.*):(.*):(.*)$");
    final private GithubStyleUrlResolver urlResolver = new GithubStyleUrlResolver();

    @Override
    public ResolvedManifest resolve(GeneratorEnvironment environment,
                                    SoftNameDependency dependency, boolean forceRedownload) throws IOException {
        String coordinate = dependency.compile;
        assert coordinate != null;
        Matcher match = pattern.matcher(coordinate);
        if (match.find()) {
            String user = match.group(1);
            String artifactId = match.group(2);
            String version = match.group(3);
            String subArtifact = "";
            if (artifactId.contains("/")) {
                int pos = artifactId.indexOf("/");
                subArtifact = "-" + artifactId.substring(pos + 1);
                artifactId = artifactId.substring(0, pos);
            }
            String manifest = String.format(
                "https://github.com/%s/%s/releases/download/%s/cdep-manifest%s.yml",
                user,
                artifactId,
                version,
                subArtifact);
            return urlResolver.resolve(environment, new SoftNameDependency(manifest), forceRedownload);
        }
        return null;
    }
}
