package io.cdep.cdep.fullfill;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.BuildFindModuleFunctionTable;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.generator.GeneratorEnvironmentUtils;
import io.cdep.cdep.resolver.ResolutionScope;
import io.cdep.cdep.resolver.ResolvedManifest;
import io.cdep.cdep.resolver.Resolver;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlEquality;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static io.cdep.cdep.io.IO.infoln;

public class Fullfill {

  /**
   * Returns a list of manifest files with missing fields filled in.
   */
  @NotNull
  public static List<File> multiple(
      GeneratorEnvironment environment,
      @NotNull File templates[],
      File outputFolder,
      @NotNull File sourceFolder,
      String version) throws IOException, NoSuchAlgorithmException {
    List<File> result = new ArrayList<>();
    CDepManifestYml manifests[] = new CDepManifestYml[templates.length];

    File layout = new File(outputFolder, "layout");
    if (!layout.isDirectory()) {
      //noinspection ResultOfMethodCallIgnored
      layout.mkdirs();
    }

    File staging = new File(outputFolder, "staging");
    if (!staging.isDirectory()) {
      //noinspection ResultOfMethodCallIgnored
      staging.mkdirs();
    }

    // Read all manifest files
    for (int i = 0; i < manifests.length; ++i) {
      String body = FileUtils.readAllText(templates[i]);
      manifests[i] = CDepManifestYmlUtils.convertStringToManifest(body);
    }

    // Replace variables
    SubstituteStringsRewritingVisitor substitutor = new SubstituteStringsRewritingVisitor()
        .replace("${source}", sourceFolder.getAbsolutePath())
        .replace("${layout}", layout.getAbsolutePath())
        .replace("${version}", version);
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = substitutor.visitCDepManifestYml(manifests[i]);
    }

    // Build function table along the way to prove function table can be built from the resulting
    // manifests.
    Resolver resolver = new Resolver(environment);
    ResolutionScope scope = new ResolutionScope();

    infoln("Fullfilling %s manifests", templates.length);
    for (int i = 0; i < manifests.length; ++i) {
      FillMissingFieldsBasedOnFilepath filler = new FillMissingFieldsBasedOnFilepath();
      Coordinate coordinate = manifests[i].coordinate;
      infoln("  guessing archive details from path names in %s", coordinate);
      manifests[i] = filler.visitCDepManifestYml(manifests[i]);

      ZipFilesRewritingVisitor zipper = new ZipFilesRewritingVisitor(layout, staging);
      infoln("  zipping files references in %s", coordinate);
      manifests[i] = zipper.visitCDepManifestYml(manifests[i]);
      result.addAll(zipper.getZips());

      FileHashAndSizeRewritingVisitor hasher = new FileHashAndSizeRewritingVisitor(layout);
      infoln("  computing hashes and file sizes of archives in %s", coordinate);
      manifests[i] = hasher.visitCDepManifestYml(manifests[i]);

      DependencyHashRewritingVisitor dependencyHasher =
          new DependencyHashRewritingVisitor(environment);
      infoln("  hashing dependencies in %s", coordinate);
      manifests[i] = dependencyHasher.visitCDepManifestYml(manifests[i]);

      File output = new File(layout, templates[i].getName());
      infoln("  writing manifest file %s", new File(".")
          .toURI().relativize(output.toURI()).getPath());
      String body = CDepManifestYmlUtils.convertManifestToString(manifests[i]);
      FileUtils.writeTextToFile(output, body);
      result.add(output);

      // Attempt to read the manifest that was written to disk.
      CDepManifestYml readFromDisk = CDepManifestYmlUtils.convertStringToManifest(
          FileUtils.readAllText(output));
      CDepManifestYmlEquality.areDeeplyIdentical(manifests[i], readFromDisk);

      infoln("  checking sanity of result %s", coordinate);
      CDepManifestYmlUtils.checkManifestSanity(manifests[i]);

      // Find any transitive dependencies that we may need to build the function table.
      SoftNameDependency softname = new SoftNameDependency(coordinate.toString());
      scope.addUnresolved(softname);
      scope.recordResolved(
          softname,
          new ResolvedManifest(output.toURI().toURL(), manifests[i]),
          CDepManifestYmlUtils.getTransitiveDependencies(manifests[i]));
    }

    infoln("  checking consistency of all manifests");
    // Resolve all remaining dependencies. This happens if the fullfilled manifests have
    // their own dependencies.
    resolver.resolveAll(scope);

    // Attempt to build a function table for the combination of manifests.
    BuildFindModuleFunctionTable table = new BuildFindModuleFunctionTable();
    GeneratorEnvironmentUtils.addAllResolvedToTable(table, scope);
    table.build();


    return result;
  }
}
