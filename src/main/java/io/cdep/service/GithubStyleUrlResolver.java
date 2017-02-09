package io.cdep.service;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.ManifestUtils;
import io.cdep.manifest.Coordinate;
import io.cdep.manifest.Manifest;
import io.cdep.model.Reference;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GithubStyleUrlResolver extends Resolver {
  final private Pattern pattern = Pattern.compile("^https://(.*)/(.*)/(.*)/releases/download/(.*)/cdep-manifest.yml$");

  @Override
  ResolvedManifest resolve(GeneratorEnvironment environment,
      Reference reference) throws IOException {
    String coordinate = reference.compile;
    Matcher match = pattern.matcher(coordinate);
    if (match.find()) {

      String baseUrl = match.group(1);
      String segments[] = baseUrl.split("\\.");
      String groupId = "";
      for (int i = 0; i < segments.length; ++i) {
        groupId += segments[segments.length - i - 1];
        groupId += ".";
      }
      String user = match.group(2);
      groupId += user;
      String artifactId = match.group(3);
      String version = match.group(4);

      Coordinate provisionalCoordinate = new Coordinate(groupId, artifactId, version);
      String manifestContent = environment.getLocalDownloadedFileText(
          provisionalCoordinate,
          new URL(coordinate));
      Manifest manifest = ManifestUtils.convertStringToManifest(manifestContent);

      // Ensure that the manifest coordinate agrees with the url provided
      if (!groupId.equals(manifest.coordinate.groupId)) {
        throw new RuntimeException(
            String.format("groupId '%s' from manifest did not agree with github url '%s",
                manifest.coordinate.groupId,
                coordinate));
      }
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

      return new ResolvedManifest(new URL(coordinate), manifest);
    }

    return null;
  }
}
