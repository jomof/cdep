package io.cdep.cdep.fullfill;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.utils.ArchiveUtils;
import io.cdep.cdep.utils.StringUtils;
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

import static io.cdep.cdep.io.IO.infoln;
import static io.cdep.cdep.utils.Invariant.require;

/**
 * Zip up file entries into layout folder. Doesn't record SHA or size since that is done
 * in a subsequent pass.
 */
public class ZipFilesRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final File layout;
  private final File staging;
  private final List<File> zips = new ArrayList<>();
  private String prefix = "";

  ZipFilesRewritingVisitor(File layout, File staging) {
    this.layout = layout;
    this.staging = staging;
  }

  @NotNull
  @Override
  public CDepManifestYml visitCDepManifestYml(@NotNull CDepManifestYml value) {
    prefix = value.coordinate.artifactId;
    prefix = prefix.replace("/", "-");
    prefix = prefix.replace("\\", "-");
    prefix = prefix.replace(":", "-");
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
    File layoutZipFile = getLayoutZipFile(prefix, "header");
    File stagingZipFolder = getStagingZipFolder(layoutZipFile, "include");

    copyFilesToStaging(mappings, stagingZipFolder);

    // Zip that file
    zipStagingFilesIntoArchive(layoutZipFile, stagingZipFolder.getParentFile());

    zips.add(layoutZipFile);

    return new Archive(
        layoutZipFile.getName(),
        null,
        null,
        "include",
        archive.requires
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
    File layoutZipFile = getLayoutZipFile(
        prefix,
        archive.ndk,
        archive.runtime,
        archive.platform,
        archive.builder,
        archive.flavor,
        archive.abi);
    File stagingZipFolder = getStagingZipFolder(layoutZipFile, "lib/" + archive.abi);

    copyFilesToStaging(mappings, stagingZipFolder);

    // Zip that file
    zipStagingFilesIntoArchive(layoutZipFile, stagingZipFolder.getParentFile().getParentFile());

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
  private File getLayoutZipFile(String ... keys) {
    String prefix = StringUtils.joinOnSkipNull("-", keys);
    File layoutZipFile = new File(layout, prefix + ".zip");
    if (layoutZipFile.exists()) {
      layoutZipFile.delete();
    }
    return replaceInvalidCharacters(layoutZipFile);
  }

  private File replaceInvalidCharacters(File file) {
    String baseName = file.getName().toString();
    baseName = baseName.replace("/", "-");
    baseName = baseName.replace("\\", "-");
    baseName = baseName.replace(":", "-");
    baseName = baseName.replace("+", "p");
    return new File(file.getParentFile(), baseName);
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
      ArchiveUtils.pack(stagingZipFolder.toPath(), layoutZipFile.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void copyFileToStaging(PathMapping mapping, File stagingZipFile) {
    try {
      Files.copy(mapping.from.toPath(), stagingZipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<? extends File> getZips() {
    return zips;
  }
}
