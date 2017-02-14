package io.cdep.generator;

import io.cdep.ast.finder.FoundModuleExpression;
import io.cdep.ast.finder.FunctionTableExpression;
import io.cdep.utils.ArchiveUtils;
import io.cdep.utils.ExpressionUtils;
import io.cdep.utils.HashUtils;
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
