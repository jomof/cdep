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

import io.cdep.cdep.ast.finder.FoundAndroidModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.ModuleArchive;
import io.cdep.cdep.utils.ArchiveUtils;
import io.cdep.cdep.utils.ExpressionUtils;
import io.cdep.cdep.utils.HashUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Methods for dealing with GeneratorEnvironment.
 */
public class GeneratorEnvironmentUtils {

    /**
     * Given a function table and generator environment, download all of the files referenced.
     */
    public static void downloadReferencedModules(
        GeneratorEnvironment environment,
        FunctionTableExpression table,
        boolean forceRedownload) throws IOException, NoSuchAlgorithmException {
        List<FoundAndroidModuleExpression> foundModules =
            ExpressionUtils.getAllFoundModuleExpressions(table);

        // Download and unzip any modules.
        for (FoundAndroidModuleExpression foundModule : foundModules) {
            for (ModuleArchive archive : foundModule.archives) {

                File local = environment.tryGetLocalDownloadedFile(
                    foundModule.coordinate, archive.file);
                if (local == null) {
                    throw new RuntimeException(
                        String.format("Resolved archive '%s' didn't exist", archive.file));
                }

                boolean forceUnzip = forceRedownload;
                if (archive.size != local.length()) {
                    // It may have been an interrupted download. Try again.
                    if (!forceRedownload) {
                        forceUnzip = true;
                        local = environment.tryGetLocalDownloadedFile(
                            foundModule.coordinate, archive.file);
                        if (local == null) {
                            throw new RuntimeException(
                                String.format("Resolved archive '%s' didn't exist", archive.file));
                        }
                    }
                    if (archive.size != local.length()) {
                        throw new RuntimeException(String.format(
                            "File size for %s was %s which did not match value %s from the manifest",
                            archive.file,
                            local.length(),
                            archive.size));
                    }
                }

                String localSha256String = HashUtils.getSHA256OfFile(local);
                if (!localSha256String.equals(archive.sha256)) {
                    throw new RuntimeException(String.format(
                        "SHA256 for %s did not match value from manifest", archive.file));
                }

                File unzipFolder = environment.getLocalUnzipFolder(
                    foundModule.coordinate, archive.file);
                if (!unzipFolder.exists() || forceUnzip) {
                    //noinspection ResultOfMethodCallIgnored
                    unzipFolder.mkdirs();
                    environment.out.printf("Exploding %s\n", archive.file);
                    ArchiveUtils.unzip(local, unzipFolder);
                }
            }
        }
    }
}
