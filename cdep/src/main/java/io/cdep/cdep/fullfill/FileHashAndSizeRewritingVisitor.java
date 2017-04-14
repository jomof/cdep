package io.cdep.cdep.fullfill;

import static io.cdep.cdep.utils.Invariant.require;

import io.cdep.cdep.utils.HashUtils;
import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class FileHashAndSizeRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  private final File layoutFolder;

  FileHashAndSizeRewritingVisitor(File layoutFolder) {
    this.layoutFolder = layoutFolder;
  }

  @Override
  protected Archive visitArchive(Archive archive) {
    if (archive.sha256 == null) {
      File file = new File(layoutFolder, archive.file);
      require(
          file.isFile(),
          "Could not hash file %s because it didn't exist",
          archive.file);
      try {
        return new Archive(archive.file, HashUtils.getSHA256OfFile(file), file.length(), archive.include, archive.requires);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return archive;
  }

  @Override
  protected AndroidArchive visitAndroidArchive(AndroidArchive archive) {
    if (archive.sha256 == null) {
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
            archive.lib,
            archive.flavor);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return archive;
  }
}
