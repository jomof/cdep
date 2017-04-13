package io.cdep.cdep.fullfill;

import static io.cdep.cdep.io.IO.info;
import static io.cdep.cdep.io.IO.infoln;

import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CreateCDepManifestYmlString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Fullfill {

  /**
   * Returns a list of manifest files.
   */
  public static List<File> multiple(
      GeneratorEnvironment environment,
      File templates[],
      File outputFolder,
      File sourceFolder,
      String version) throws IOException {
    List<File> result = new ArrayList<>();
    CDepManifestYml manifests[] = new CDepManifestYml[templates.length];

    File layout = new File(outputFolder, "layout");
    if (!layout.isDirectory()) {
      layout.mkdirs();
    }

    File staging = new File(outputFolder, "staging");
    if (!staging.isDirectory()) {
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

    infoln("Fullfilling %s manifests", templates.length);
    FillMissingFieldsBasedOnFilepath filler = new FillMissingFieldsBasedOnFilepath();
    for (int i = 0; i < manifests.length; ++i) {
      infoln("  guessing archive details from path names in %s", manifests[i].coordinate);
      manifests[i] = filler.visitCDepManifestYml(manifests[i]);

      ZipFilesRewritingVisitor zipper = new ZipFilesRewritingVisitor(layout, staging);
      infoln("  zipping files references in %s", manifests[i].coordinate);
      manifests[i] = zipper.visitCDepManifestYml(manifests[i]);
      result.addAll(zipper.getZips());

      FileHashAndSizeRewritingVisitor hasher = new FileHashAndSizeRewritingVisitor(layout);
      infoln("  computing hashes and file sizes of archives in %s", manifests[i].coordinate);
      manifests[i] = hasher.visitCDepManifestYml(manifests[i]);

      DependencyHashRewritingVisitor dependencyHasher =
          new DependencyHashRewritingVisitor(environment);
      infoln("  hashing dependencies in %s", manifests[i].coordinate);
      manifests[i] = dependencyHasher.visitCDepManifestYml(manifests[i]);

      File output = new File(layout, templates[i].getName());
      infoln("  writing manifest file %s", new File(".")
          .toURI().relativize(output.toURI()).getPath());
      String body = CreateCDepManifestYmlString.create(manifests[i]);
      FileUtils.writeTextToFile(output, body);
      result.add(output);

      infoln("  checking sanity of result %s", manifests[i].coordinate);
      CDepManifestYmlUtils.checkManifestSanity(manifests[i]);
    }

    return result;
  }
}
