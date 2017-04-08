package io.cdep.cdep.fullfill;

import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static io.cdep.cdep.utils.Invariant.require;

/**
 * Zip up file entries and add sha256, size, and include
 */
public class ZipFilesRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final File outputFolder;
  private final File layout;
  private final File staging;


  ZipFilesRewritingVisitor(File outputFolder) {
    this.outputFolder = outputFolder;
    this.layout = new File(outputFolder, "layout");
    if (!this.layout.isDirectory()) {
      this.layout.mkdirs();
    }

    this.staging = new File(outputFolder, "staging");
    if (!this.staging.isDirectory()) {
      this.staging.mkdirs();
    }
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

    return new Archive(
        layoutZipFile.getName(),
        "shamalamadingdong",
        102L,
        "include"
    );
  }
}
