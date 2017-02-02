package com.jomofisher.cdep;

import com.jomofisher.cdep.manifest.Manifest;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GithubStyleUrlResolver extends Resolver {
  final private Pattern pattern = Pattern.compile("^https://(.*)/(.*)/(.*)/releases/download/(.*)/cdep-manifest.yml$");

  @Override
  ResolvedManifest resolve(String coordinate) throws IOException {

    Matcher match = pattern.matcher(coordinate);
    if (match.find()) {

      String artifactId = match.group(3);
      String version = match.group(4);

      String manifestContent = WebUtils.getUrlAsString(coordinate);
      Manifest manifest = ManifestUtils.convertStringToManifest(manifestContent);

      // Ensure that the manifest coordinate agrees with the url provided
      if (!artifactId.equals(manifest.coordinate.artifactId)) {
        throw new RuntimeException(
            String.format("artifactId '%s' from manifest did not agree with github url '%s",
                manifest.coordinate.artifactId,
                coordinate));
      }
      if (!version.equals(manifest.coordinate.version)) {
        throw new RuntimeException(
            String.format("version '%s' from manifest did not agree with github url '%s",
                manifest.coordinate.version,
                coordinate));
      }

      return new ResolvedManifest(new File(coordinate), manifest);
    }

    return null;
  }
}
