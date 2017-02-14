package io.cdep;

import io.cdep.AST.finder.FoundModuleExpression;
import io.cdep.AST.finder.FunctionTableExpression;
import io.cdep.service.GeneratorEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Methods for dealing with GeneratorEnvironment.
 */
public class GeneratorEnvironmentUtils {

    /**
     * Given a function table and generator environment, download all of the files referenced.
     */
    static void downloadReferencedModules(
        GeneratorEnvironment environment,
        FunctionTableExpression table) throws IOException {
        List<FoundModuleExpression> foundModules =
            ExpressionUtils.getAllFoundModuleExpressions(table);

        // Download and unzip any modules.
        for (FoundModuleExpression foundModule : foundModules) {
            File local = environment.getLocalDownloadedFile(
                foundModule.coordinate, foundModule.archive);
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
