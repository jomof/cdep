package io.cdep.cdep.fullfill;

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

/**
 * Fill in hash values for hard name dependencies.
 */
public class DependencyHashRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final GeneratorEnvironment environment;
  DependencyHashRewritingVisitor(GeneratorEnvironment environment) {
    this.environment = environment;
  }

  @Override
  protected HardNameDependency visitHardNameDependency(HardNameDependency dependency) {
    if (dependency.sha256 == null) {
      try {
        ResolvedManifest resolved = new Resolver(environment).resolveAny(new SoftNameDependency(dependency.compile));
        File manifest = environment.tryGetLocalDownloadedFile(resolved.cdepManifestYml.coordinate, resolved.remote);
        return new HardNameDependency(
            resolved.cdepManifestYml.coordinate.toString(),
            HashUtils.getSHA256OfFile(manifest));
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }
    return super.visitHardNameDependency(dependency);
  }
}
