package io.cdep.cdep.fullfill;

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

import static io.cdep.cdep.utils.Invariant.require;

/**
 * Zip up file entries into layout folder. Doesn't record SHA or size since that is done
 * in a subsequent pass.
 */
public class ZipFilesRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final File layout;
  private final File staging;
  private final List<File> zips = new ArrayList<>();

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
    File singleFile = new File(archive.file);
    require(singleFile.exists(), "File %s didn't exist", singleFile.getAbsoluteFile());
    File layoutZipFile = new File(layout, singleFile.getName() + ".zip");
    File stagingZipFolder = new File(staging, layoutZipFile.getName());
    stagingZipFolder = new File(stagingZipFolder, "include");
    File stagingZipFile = new File(stagingZipFolder, singleFile.getName());

    // Make the staging zip folder
    stagingZipFolder.mkdirs();

    // Copy the single header file to the staging zip folder.
    try {
      Files.copy(singleFile.toPath(), stagingZipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Zip that file
    try {
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