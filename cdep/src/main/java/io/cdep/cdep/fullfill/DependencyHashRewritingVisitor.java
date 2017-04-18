package io.cdep.cdep.fullfill;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.resolver.ResolvedManifest;
import io.cdep.cdep.resolver.Resolver;
import io.cdep.cdep.utils.HashUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static io.cdep.cdep.utils.Invariant.require;

/**
 * Fill in hash values for hard name dependencies.
 */
public class DependencyHashRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final GeneratorEnvironment environment;
  DependencyHashRewritingVisitor(GeneratorEnvironment environment) {
    this.environment = environment;
  }

  @Nullable
  @Override
  protected HardNameDependency visitHardNameDependency(@NotNull HardNameDependency dependency) {
    require(dependency.compile != null, "Dependency had no compile field");
    if (dependency.sha256 == null) {
      try {
        ResolvedManifest resolved = new Resolver(environment).resolveAny(
            new SoftNameDependency(
                dependency.compile));
        require(resolved != null, "Could not resolve dependency %s",
            dependency.compile);
        File manifest = environment.tryGetLocalDownloadedFile(
            resolved.cdepManifestYml.coordinate,
            resolved.remote);
        return new HardNameDependency(
            resolved.cdepManifestYml.coordinate.toString(),
            HashUtils.getSHA256OfFile(manifest));
      } catch (@NotNull IOException | NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }
    return super.visitHardNameDependency(dependency);
  }
}
