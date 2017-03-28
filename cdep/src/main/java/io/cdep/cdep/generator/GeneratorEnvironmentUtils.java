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

import io.cdep.annotations.NotNull;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ast.finder.Expression;
import io.cdep.cdep.ast.finder.ModuleArchiveExpression;
import io.cdep.cdep.ast.finder.ModuleExpression;
import io.cdep.cdep.utils.ArchiveUtils;
import io.cdep.cdep.utils.HashUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.cdep.cdep.utils.Invariant.notNull;
import static io.cdep.cdep.utils.Invariant.require;

/**
 * Methods for dealing with GeneratorEnvironment.
 */
public class GeneratorEnvironmentUtils {

  /**
   * Given a function table and generator environment, download all of the files referenced.
   */
  public static void downloadReferencedModules(@NotNull GeneratorEnvironment environment,
      @NotNull Map<Coordinate, List<Expression>> foundModules) throws IOException, NoSuchAlgorithmException {

    Set<File> alreadyExploded = new HashSet<>();

    // Download and unzip any modules.
    for (Coordinate coordinate : foundModules.keySet()) {
      List<Expression> foundModuleExpressions = foundModules.get(notNull(coordinate));
      for (Expression foundModule : foundModuleExpressions) {
        ModuleArchiveExpression archive = null;
        if (foundModule instanceof ModuleExpression) {
          ModuleExpression specific = (ModuleExpression) foundModule;
          archive = specific.archive;
        }
        notNull(archive);
        assert archive.file != null;
        File local = environment.tryGetLocalDownloadedFile(coordinate, archive.file);
        require(local != null, "Resolved archive '%s' didn't exist", archive.file);

        boolean forceUnzip = environment.forceRedownload && !alreadyExploded.contains(local);
        if (archive.size != local.length()) {
          // It may have been an interrupted download. Try again.
          if (!environment.forceRedownload) {
            forceUnzip = true;
            local = environment.tryGetLocalDownloadedFile(coordinate, archive.file);
            require(local != null, "Resolved archive '%s' didn't exist", archive.file);
          }
          require(archive.size == local.length(),
              "File size for %s was %s which did not match value %s from the manifest",
              archive.file,
              local.length(),
              archive.size);
        }

        String localSha256String = HashUtils.getSHA256OfFile(local);
        require(localSha256String.equals(archive.sha256), "SHA256 for %s did not match value from manifest", archive.file);

        File unzipFolder = environment.getLocalUnzipFolder(coordinate, archive.file);
        if (!unzipFolder.exists() || forceUnzip) {
          //noinspection ResultOfMethodCallIgnored
          unzipFolder.mkdirs();
          ArchiveUtils.unzip(local, unzipFolder);
          alreadyExploded.add(local);
        }
      }
    }
  }
}
