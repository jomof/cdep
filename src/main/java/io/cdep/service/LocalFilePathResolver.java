package io.cdep.service;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.ManifestUtils;
import io.cdep.manifest.Manifest;
import io.cdep.model.Reference;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by jomof on 2/9/17.
 */
class LocalFilePathResolver extends Resolver {

    @Override
    ResolvedManifest resolve(GeneratorEnvironment environment, Reference reference)
        throws IOException {
        String coordinate = reference.compile;
        File local = new File(coordinate);
        if (!local.isFile()) {
            return null;
        }
        String content = new String(Files.readAllBytes(Paths.get(local.getCanonicalPath())));
        Manifest manifest = ManifestUtils.convertStringToManifest(content);

        // Ensure that the manifest coordinate agrees with the url provided
        if (!coordinate.contains(manifest.coordinate.groupId)) {
            throw new RuntimeException(
                String.format("local file name '%s' did not contain groupId '%s'"
                        + "", coordinate,
                    manifest.coordinate.groupId));
        }
        if (!coordinate.contains(manifest.coordinate.artifactId)) {
            throw new RuntimeException(
                String.format("local file name '%s' did not contain artifactId '%s'"
                        + "", coordinate,
                    manifest.coordinate.artifactId));
        }
        if (!coordinate.contains(manifest.coordinate.version)) {
            throw new RuntimeException(
                String.format("local file name '%s' did not contain version '%s'"
                        + "", coordinate,
                    manifest.coordinate.version));
        }

        return new ResolvedManifest(local.getCanonicalFile().toURI().toURL(), manifest);
    }
}
