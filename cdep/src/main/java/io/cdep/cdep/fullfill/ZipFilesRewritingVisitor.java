package io.cdep.cdep.fullfill;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
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

import static io.cdep.cdep.utils.Invariant.require;

/**
 * Zip up file entries into layout folder. Doesn't record SHA or size since that is done
 * in a subsequent pass.
 */
public class ZipFilesRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final File layout;
  private final File staging;
  private final List<File> zips = new ArrayList<>();
  @Nullable
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

  @Nullable
  @Override
  protected Archive visitArchive(@Nullable Archive archive) {
    if (archive == null || archive.file.isEmpty()) {
      return null;
    }
    if (archive.file.endsWith(".zip")) {
      return archive;
    }
    PathMapping mappings[] = PathMapping.parse(archive.file);
    require(mappings.length > 0,
        "File mapping '%s' did not resolve to any local files", archive.file);
    File layoutZipFile = getLayoutZipFile(prefix, "headers");
    File stagingZipFolder = getStagingZipFolder(layoutZipFile, "include");

    copyFilesToStaging(mappings, stagingZipFolder);

    // Zip that file
    zipStagingFilesIntoArchive(layoutZipFile, stagingZipFolder.getParentFile());

    zips.add(layoutZipFile);

    return new Archive(
        layoutZipFile.getName(),
        "",
        0L,
        "include",
        archive.requires
    );
  }

  @Nullable
  @Override
  protected AndroidArchive visitAndroidArchive(@Nullable AndroidArchive archive) {
    if (archive == null || archive.file.isEmpty()) {
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
        "android",
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
        "",
        0L,
        archive.ndk,
        archive.compiler,
        archive.runtime,
        archive.platform,
        archive.builder,
        archive.abi,
        archive.include,
        archive.libs,
        archive.flavor);
  }

  @NotNull
  private File getStagingZipFolder(@NotNull File layoutZipFile, @NotNull String folder) {
    File stagingZipFolder = new File(staging, layoutZipFile.getName());
    stagingZipFolder = new File(stagingZipFolder, folder);
    //noinspection ResultOfMethodCallIgnored
    stagingZipFolder.delete();
    return stagingZipFolder;
  }

  @NotNull
  private File getLayoutZipFile(String ... keys) {
    String prefix = StringUtils.joinOnSkipNullOrEmpty("-", keys);
    File layoutZipFile = new File(layout, prefix + ".zip");
    if (layoutZipFile.exists()) {
      //noinspection ResultOfMethodCallIgnored
      layoutZipFile.delete();
    }
    return replaceInvalidCharacters(layoutZipFile);
  }

  @NotNull
  private File replaceInvalidCharacters(@NotNull File file) {
    String baseName = file.getName();
    baseName = baseName.replace("/", "-");
    baseName = baseName.replace("\\", "-");
    baseName = baseName.replace(":", "-");
    baseName = baseName.replace("+", "p");
    return new File(file.getParentFile(), baseName);
  }

  private void copyFilesToStaging(@NotNull PathMapping[] mappings, File stagingZipFolder) {
    for (PathMapping mapping : mappings) {
      require(
          mapping.from.exists(),
          "Could not zip file %s because it didn't exist",
          mapping.from.getAbsoluteFile());
      File stagingZipFile = new File(stagingZipFolder, mapping.to.getPath());

      // Make the staging zip folder
      //noinspection ResultOfMethodCallIgnored
      stagingZipFile.getParentFile().mkdirs();

      // Copy the single header file to the staging zip folder.
      copyFileToStaging(mapping, stagingZipFile);
    }
  }

  private void zipStagingFilesIntoArchive(@NotNull File layoutZipFile, @NotNull File stagingZipFolder) {
    try {
      ArchiveUtils.pack(stagingZipFolder.toPath(), layoutZipFile.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void copyFileToStaging(@NotNull PathMapping mapping, @NotNull File stagingZipFile) {
    try {
      Files.copy(mapping.from.toPath(), stagingZipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  public Collection<? extends File> getZips() {
    return zips;
  }
}
