package io.cdep.cdep.fullfill;

import static io.cdep.cdep.io.IO.infoln;
import static io.cdep.cdep.utils.Invariant.require;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.utils.ArchiveUtils;
import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
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
  private String prefix = "";

  ZipFilesRewritingVisitor(File layout, File staging) {
    this.layout = layout;
    this.staging = staging;
  }

  @NotNull
  @Override
  public CDepManifestYml visitCDepManifestYml(@NotNull CDepManifestYml value) {
    prefix = value.coordinate.groupId + "_" + value.coordinate.artifactId + "_" + value.coordinate.version.value;
    prefix = prefix.replace("/", "_");
    prefix = prefix.replace("\\", "_");
    prefix = prefix.replace(":", "_");
    return super.visitCDepManifestYml(value);
  }

  @Override
  protected Archive visitArchive(Archive archive) {
    if (archive == null || archive.file == null) {
      return archive;
    }
    if (archive.file.endsWith(".zip")) {
      return archive;
    }
    PathMapping mappings[] = PathMapping.parse(archive.file);
    require(mappings.length > 0,
        "File mapping '%s' did not resolve to any local files", archive.file);
    File layoutZipFile = getLayoutZipFile();
    File stagingZipFolder = getStagingZipFolder(layoutZipFile, "include");

    copyFilesToStaging(mappings, stagingZipFolder);

    // Zip that file
    zipStagingFilesIntoArchive(layoutZipFile, stagingZipFolder);

    zips.add(layoutZipFile);

    return new Archive(
        layoutZipFile.getName(),
        null,
        null,
        "include"
    );
  }

  @Override
  protected AndroidArchive visitAndroidArchive(AndroidArchive archive) {
    if (archive == null || archive.file == null) {
      return archive;
    }
    if (archive.file.endsWith(".zip")) {
      return archive;
    }
    PathMapping mappings[] = PathMapping.parse(archive.file);
    require(mappings.length > 0,
        "File mapping '%s' did not resolve to any local files", archive.file);
    File layoutZipFile = getLayoutZipFile();
    File stagingZipFolder = getStagingZipFolder(layoutZipFile, archive.abi);

    copyFilesToStaging(mappings, stagingZipFolder);

    // Zip that file
    zipStagingFilesIntoArchive(layoutZipFile, stagingZipFolder);

    zips.add(layoutZipFile);

    return new AndroidArchive(
        layoutZipFile.getName(),
        null,
        null,
        archive.ndk,
        archive.compiler,
        archive.runtime,
        archive.platform,
        archive.builder,
        archive.abi,
        archive.include,
        archive.lib,
        archive.flavor);
  }


  @NotNull
  private File getStagingZipFolder(File layoutZipFile, String folder) {
    File stagingZipFolder = new File(staging, layoutZipFile.getName());
    stagingZipFolder = new File(stagingZipFolder, folder);
    stagingZipFolder.delete();
    return stagingZipFolder;
  }

  @NotNull
  private File getLayoutZipFile() {
    File layoutZipFile = new File(layout, prefix+ "_" + index + ".zip");
    if (layoutZipFile.exists()) {
      layoutZipFile.delete();
    }
    ++index;
    return layoutZipFile;
  }

  private void copyFilesToStaging(PathMapping[] mappings, File stagingZipFolder) {
    for (PathMapping mapping : mappings) {
      require(
          mapping.from.exists(),
          "Could not zip file %s because it didn't exist",
          mapping.from.getAbsoluteFile());
      File stagingZipFile = new File(stagingZipFolder, mapping.to.getPath());

      // Make the staging zip folder
      stagingZipFile.getParentFile().mkdirs();

      // Copy the single header file to the staging zip folder.
      copyFileToStaging(mapping, stagingZipFile);
    }
  }

  private void zipStagingFilesIntoArchive(File layoutZipFile, File stagingZipFolder) {
    try {
      infoln("Zipping folder %s to %s", stagingZipFolder.getParentFile().toPath(),
          layoutZipFile.toPath());
      ArchiveUtils.pack(stagingZipFolder.getParentFile().toPath(), layoutZipFile.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void copyFileToStaging(PathMapping mapping, File stagingZipFile) {
    try {
      infoln("Copying %s to %s", mapping.from.toPath(), stagingZipFile.toPath());
      Files.copy(mapping.from.toPath(), stagingZipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<? extends File> getZips() {
    return zips;
  }
}
