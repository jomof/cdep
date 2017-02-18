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

import io.cdep.cdep.ast.finder.FoundModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
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

    /*
     * Given a function table and generator environment, download all of the files referenced.
     */
    public static void downloadReferencedModules(
        GeneratorEnvironment environment,
        FunctionTableExpression table,
        boolean forceRedownload) throws IOException, NoSuchAlgorithmException {
        List<FoundModuleExpression> foundModules =
            ExpressionUtils.getAllFoundModuleExpressions(table);

        // Download and unzip any modules.
        for (FoundModuleExpression foundModule : foundModules) {
            File local = environment.getLocalDownloadedFile(
                foundModule.coordinate, foundModule.archive, forceRedownload);
            String localSha256String = HashUtils.getSHA256OfFile(local);
            if (!localSha256String.equals(foundModule.archiveSHA256)) {
                throw new RuntimeException(String.format(
                    "SHA256 for %s did not match value from manifest", foundModule.archive));
            }

            File unzipFolder = environment.getLocalUnzipFolder(
                foundModule.coordinate, foundModule.archive);
            if (!unzipFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                unzipFolder.mkdirs();
                environment.out.printf("Exploding %s\n", foundModule.archive);
                ArchiveUtils.unzip(local, unzipFolder);
            }
        }
    }
}
