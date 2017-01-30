package com.jomofisher.cdep;

import com.jomofisher.cdep.manifest.Manifest;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GithubStyleUrlResolver extends Resolver {
  final private Pattern pattern = Pattern.compile("^https://(.*)/(.*)/(.*)/releases/download/(.*)/cdep-manifest.yml$");

  @Override
  ResolvedDependency resolve(String coordinate) throws IOException {

    Matcher match = pattern.matcher(coordinate);
    if (match.find()) {
      String base = match.group(1);
      String user = match.group(2);
      String artifactId = match.group(3);
      String version = match.group(4);

      Coordinate strong = new Coordinate(base + "." + user, artifactId, version);
      String manifestContent = WebUtils.getUrlAsString(coordinate);
      Manifest manifest = ManifestUtils.convertStringToManifest(manifestContent);
      return new ResolvedDependency(strong, manifest);
    }

    //"https://github.com/jomof/cmakeify/releases/tag/alpha-0.0.27";
    return null;
  }
}
