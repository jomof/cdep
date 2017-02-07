package io.cdep;

import static java.util.regex.Pattern.compile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubReleasesCoordinateResolver extends Resolver {

    final private Pattern pattern = compile("^com\\.github\\.(.*):(.*):(.*)$");
    final private GithubStyleUrlResolver urlResolver = new GithubStyleUrlResolver();

    @Override
    ResolvedManifest resolve(String coordinate) throws IOException {
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
            return urlResolver.resolve(manifest);
        }
        return null;
    }
}
