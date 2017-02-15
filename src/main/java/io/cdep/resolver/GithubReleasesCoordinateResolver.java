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

import static java.util.regex.Pattern.compile;

import io.cdep.ast.service.ResolvedManifest;
import io.cdep.generator.GeneratorEnvironment;
import io.cdep.yml.cdep.Dependency;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubReleasesCoordinateResolver extends Resolver {

    final private Pattern pattern = compile("^com\\.github\\.(.*):(.*):(.*)$");
    final private GithubStyleUrlResolver urlResolver = new GithubStyleUrlResolver();

    @Override
    public ResolvedManifest resolve(GeneratorEnvironment environment,
        Dependency dependency, boolean forceRedownload) throws IOException {
        String coordinate = dependency.compile;
        assert coordinate != null;
        Matcher match = pattern.matcher(coordinate);
        if (match.find()) {
            String user = match.group(1);
            String groupId = match.group(2);
            String version = match.group(3);
            String manifest = String.format(
                "https://github.com/%s/%s/releases/download/%s/cdep-manifest.yml",
                user,
                groupId,
                version);
            return urlResolver.resolve(environment, new Dependency(manifest), forceRedownload);
        }
        return null;
    }
}
