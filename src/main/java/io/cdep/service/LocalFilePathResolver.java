package io.cdep.service;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.ManifestUtils;
import io.cdep.manifest.CDepManifestYml;
import io.cdep.model.Dependency;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class LocalFilePathResolver extends Resolver {

    @Override
    ResolvedManifest resolve(GeneratorEnvironment environment, Dependency dependency,
        boolean forceRedownload)
        throws IOException {
        String coordinate = dependency.compile;
        assert coordinate != null;
        File local = new File(coordinate);
        if (!local.isFile()) {
            return null;
        }
        String content = new String(Files.readAllBytes(Paths.get(local.getCanonicalPath())));
        CDepManifestYml cdepManifestYml = ManifestUtils.convertStringToManifest(content);
        ManifestUtils.checkManifestSanity(cdepManifestYml);

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
