package io.cdep.cdep.fullfill;

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
  public static List<File> multiple(GeneratorEnvironment environment,
      File templates[], File outputFolder, File sourceFolder, String version) throws IOException {
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

    infoln("Filling in archive details based on file names and paths");
    FillMissingFieldsBasedOnFilepath filler = new FillMissingFieldsBasedOnFilepath();
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = filler.visitCDepManifestYml(manifests[i]);
    }

    infoln("Zipping files referenced directly in manifest");
    ZipFilesRewritingVisitor zipper = new ZipFilesRewritingVisitor(layout, staging);
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = zipper.visitCDepManifestYml(manifests[i]);
    }
    result.addAll(zipper.getZips());

    infoln("Getting hashes and sizes of archives");
    FileHashAndSizeRewritingVisitor hasher = new FileHashAndSizeRewritingVisitor(layout);
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = hasher.visitCDepManifestYml(manifests[i]);
    }

    infoln("Getting hashes of dependencies");
    DependencyHashRewritingVisitor dependencyHasher = new DependencyHashRewritingVisitor(environment);
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = dependencyHasher.visitCDepManifestYml(manifests[i]);
    }

    // Write each of the final manifests to the layout folder
    infoln("Checking sanity of fullfilled manifests");
    for (int i = 0; i < manifests.length; ++i) {
      String body = CreateCDepManifestYmlString.create(manifests[i]);
      File output = new File(layout, templates[i].getName());
      FileUtils.writeTextToFile(output, body);
      result.add(output);
    }

    // Lint the manifests
    for (int i = 0; i < manifests.length; ++i) {
      CDepManifestYmlUtils.checkManifestSanity(manifests[i]);
    }

    return result;
  }
}
