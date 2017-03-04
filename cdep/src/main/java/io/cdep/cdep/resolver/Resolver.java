package io.cdep.cdep.resolver;


import static io.cdep.cdep.resolver.ResolutionScope.UNPARSEABLE_RESOLUTION;
import static io.cdep.cdep.resolver.ResolutionScope.UNRESOLVEABLE_RESOLUTION;

import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.resolver.ResolutionScope.Resolution;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Resolve references and groups of references (ResolutionScope)
 */
public class Resolver {

  final private static CoordinateResolver RESOLVERS[] = new CoordinateResolver[]{
      new GithubStyleUrlCoordinateResolver(),
      new GithubReleasesCoordinateResolver(),
      new LocalFilePathCoordinateResolver()
  };

  final private ManifestProvider manifestProvider;
  final private CoordinateResolver resolvers[];

  public Resolver(ManifestProvider manifestProvider) {
    this(manifestProvider, RESOLVERS);
  }

  Resolver(ManifestProvider manifestProvider, CoordinateResolver resolvers[]) {
    this.manifestProvider = manifestProvider;
    this.resolvers = resolvers;
  }

  /**
   * Resilve all of the references contained in the given ResolutionScope
   *
   * @param roots the root References
   */
  public ResolutionScope resolveAll(SoftNameDependency[] roots)
      throws IOException, NoSuchAlgorithmException {
    ResolutionScope scope = new ResolutionScope(roots);
    // Progressively resolve dependencies
    while (!scope.isResolutionComplete()) {
      for (SoftNameDependency softname : scope.getUnresolvedReferences()) {
        ResolvedManifest resolved = resolveAny(softname);
        if (resolved == null) {
          scope.recordUnresolvable(softname);
        } else {
          List<HardNameDependency> transitive =
              CDepManifestYmlUtils.getTransitiveDependencies(resolved.cdepManifestYml);
          scope.recordResolved(softname, resolved, transitive);
        }
      }
    }

    // Throw some exceptions if we didn't resolve something.
    for (String softname : scope.getResolvedNames()) {
      Resolution resolution = scope.getResolution(softname);

      // The resolution was something besides success.
      if (resolution == UNRESOLVEABLE_RESOLUTION) {
        throw new RuntimeException(String.format(
            "Could not resolve '%s'. It doesn't exist.", softname));
      }

      if (resolution == UNPARSEABLE_RESOLUTION) {
        throw new RuntimeException(String.format(
            "Could not resolve '%s'. It didn't look like a coordinate.", softname));
      }
    }
    return scope;
  }

  /**
   * Resolve a single reference. Don't look at transitive references.
   * @param dependency is the reference to resolve.
   * @return the resolved manifest.
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public ResolvedManifest resolveAny(SoftNameDependency dependency)
      throws IOException, NoSuchAlgorithmException {
    ResolvedManifest resolved = null;
    for (CoordinateResolver resolver : resolvers) {
      ResolvedManifest attempt = resolver.resolve(manifestProvider, dependency);
      if (attempt != null) {
        if (resolved != null) {
          throw new RuntimeException("Multiple resolvers matched coordinate: "
              + dependency.compile);
        }
        resolved = attempt;
      }
    }
    if (resolved != null) {
      CDepManifestYmlUtils.checkManifestSanity(resolved.cdepManifestYml);

    }
    return resolved;
  }
}
