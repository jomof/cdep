package io.cdep.cdep.fullfill;

import io.cdep.cdep.utils.HashUtils;
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
      try {
        return new Archive(archive.file, HashUtils.getSHA256OfFile(file), file.length(), archive.include);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return archive;
  }
}
