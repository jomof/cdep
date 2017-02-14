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
