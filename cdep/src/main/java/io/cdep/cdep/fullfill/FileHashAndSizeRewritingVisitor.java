package io.cdep.cdep.fullfill;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.utils.HashUtils;
import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static io.cdep.cdep.utils.Invariant.require;

public class FileHashAndSizeRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final File layoutFolder;

  FileHashAndSizeRewritingVisitor(File layoutFolder) {
    this.layoutFolder = layoutFolder;
  }

  @Nullable
  @Override
  protected Archive visitArchive(Archive archive) {
    if (archive.sha256 == null) {
      assert archive.file != null;
      File file = new File(layoutFolder, archive.file);
      require(
          file.isFile(),
          "Could not hash file %s because it didn't exist",
          archive.file);
      try {
        return new Archive(
            archive.file,
            HashUtils.getSHA256OfFile(file),
            file.length(),
            archive.include,
            archive.requires);
      } catch (@NotNull NoSuchAlgorithmException | IOException e) {
        throw new RuntimeException(e);
      }
    }
    return archive;
  }

  @Nullable
  @Override
  protected AndroidArchive visitAndroidArchive(@NotNull AndroidArchive archive) {
    if (archive.sha256 == null) {
      assert archive.file != null;
      File file = new File(layoutFolder, archive.file);
      require(
          file.isFile(),
          "Could not hash file %s because it didn't exist",
          archive.file);
      try {
        return new AndroidArchive(
            archive.file,
            HashUtils.getSHA256OfFile(file),
            file.length(),
            archive.ndk,
            archive.compiler,
            archive.runtime,
            archive.platform,
            archive.builder,
            archive.abi,
            archive.include,
            archive.libs,
            archive.flavor);
      } catch (@NotNull NoSuchAlgorithmException | IOException e) {
        throw new RuntimeException(e);
      }
    }
    return archive;
  }
}
