package io.cdep.service;

import static java.util.regex.Pattern.compile;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.model.Reference;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GithubReleasesCoordinateResolver extends Resolver {

    final private Pattern pattern = compile("^com\\.github\\.(.*):(.*):(.*)$");
    final private GithubStyleUrlResolver urlResolver = new GithubStyleUrlResolver();

    @Override
    ResolvedManifest resolve(GeneratorEnvironment environment,
        Reference reference) throws IOException {
        String coordinate = reference.compile;
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
            return urlResolver.resolve(environment, new Reference(manifest));
        }
        return null;
    }
}