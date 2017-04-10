package io.cdep.cdep.fullfill;

import static io.cdep.cdep.io.IO.infoln;
import static io.cdep.cdep.utils.Invariant.require;

import io.cdep.cdep.utils.ArchiveUtils;
import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Zip up file entries into layout folder. Doesn't record SHA or size since that is done
 * in a subsequent pass.
 */
public class ZipFilesRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final File layout;
  private final File staging;
  private final List<File> zips = new ArrayList<>();
  private int index = 0;

  ZipFilesRewritingVisitor(File outputFolder) {
    this.layout = new File(outputFolder, "layout");
    if (!this.layout.isDirectory()) {
      this.layout.mkdirs();
    }

    this.staging = new File(outputFolder, "staging");
    if (!this.staging.isDirectory()) {
      this.staging.mkdirs();
    }
  }

  File getLayoutFolder() {
    return layout;
  }

  @Override
  protected Archive visitArchive(Archive archive) {
    PathMapping mappings[] = PathMapping.parse(archive.file);
    require(mappings.length > 0,
        "File mapping '%s' did not resolve to any local files", archive.file);
    File layoutZipFile = new File(layout, "archive" + index + ".zip");
    File stagingZipFolder = new File(staging, layoutZipFile.getName());
    stagingZipFolder = new File(stagingZipFolder, "include");
    ++index;

    for (PathMapping mapping : mappings) {
      require(mapping.from.exists(), "File %s didn't exist", mapping.from.getAbsoluteFile());
      File stagingZipFile = new File(stagingZipFolder, mapping.to.getPath());

      // Make the staging zip folder
      stagingZipFile.getParentFile().mkdirs();

      // Copy the single header file to the staging zip folder.
      try {
        infoln("Copying %s to %s", mapping.from.toPath(), stagingZipFile.toPath());
        Files.copy(mapping.from.toPath(), stagingZipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    // Zip that file
    try {
      infoln("Zipping folder %s to %s", stagingZipFolder.getParentFile().toPath(), layoutZipFile.toPath());
      ArchiveUtils.pack(stagingZipFolder.getParentFile().toPath(), layoutZipFile.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    zips.add(layoutZipFile);

    return new Archive(
        layoutZipFile.getName(),
        null,
        null,
        "include"
    );
  }

  public Collection<? extends File> getZips() {
    return zips;
  }
}
