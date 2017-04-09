package io.cdep.cdep.fullfill;

import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CreateCDepManifestYmlString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Fullfill {

  public static List<File> multiple(File templates[], File outputFolder, File sourceFolder, String version) throws IOException {
    List<File> result = new ArrayList<>();
    CDepManifestYml manifests[] = new CDepManifestYml[templates.length];

    // Read all manifest files
    for (int i = 0; i < manifests.length; ++i) {
      String body = FileUtils.readAllText(templates[i]);
      manifests[i] = CDepManifestYmlUtils.convertStringToManifest(body);
    }

    // Replace variables
    SubstituteStringsRewritingVisitor substitutor = new SubstituteStringsRewritingVisitor()
        .replace("${source}", sourceFolder.getAbsolutePath())
        .replace("${version}", version);
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = substitutor.visitCDepManifestYml(manifests[i]);
    }

    // Zip referenced files
    ZipFilesRewritingVisitor zipper = new ZipFilesRewritingVisitor(outputFolder);
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = zipper.visitCDepManifestYml(manifests[i]);
    }
    result.addAll(zipper.getZips());

    // Hash zips
    HashAndSizeRewritingVisitor hasher = new HashAndSizeRewritingVisitor(zipper.getLayoutFolder());
    for (int i = 0; i < manifests.length; ++i) {
      manifests[i] = hasher.visitCDepManifestYml(manifests[i]);
    }

    // Write each of the final manifests to the layout folder
    for (int i = 0; i < manifests.length; ++i) {
      String body = CreateCDepManifestYmlString.create(manifests[i]);
      File output = new File(zipper.getLayoutFolder(), templates[i].getName());
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
