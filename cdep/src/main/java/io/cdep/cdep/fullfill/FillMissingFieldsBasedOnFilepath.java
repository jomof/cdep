package io.cdep.cdep.fullfill;

import io.cdep.annotations.Nullable;
import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;

import java.io.File;

public class FillMissingFieldsBasedOnFilepath extends CDepManifestYmlRewritingVisitor {
  // Note, this list must be in order such that long strings come before shorter prefixes
  // of the same string.
  private final String[] androidABIs = new String[] {
      "arm64-v8a",
      "armeabi-v7a",
      "armeabi",
      "mips64",
      "mips",
      "x86_64",
      "x86"};


  @Nullable
  @Override
  protected AndroidArchive visitAndroidArchive(@Nullable AndroidArchive archive) {
    if (archive == null || archive.file == null) {
      return archive;
    }
    String abi = archive.abi;
    if (abi == null) {
      for (String androidABI : androidABIs) {
        if (archive.file.contains(androidABI)) {
          abi = androidABI;
          break;
        }
      }
    }

    String runtime = archive.runtime;
    if (runtime == null) {
      if (archive.file.contains("c++")) {
        runtime = "c++";
      } else if (archive.file.contains("cxx")) {
        runtime = "c++";
      } else if (archive.file.contains("gnustl")) {
        runtime = "gnustl";
      } else if (archive.file.contains("stlport")) {
        runtime = "stlport";
      }
    }

    String libs[] = archive.libs;
    if (libs == null) {
      File file = new File(archive.file);
      libs = new String[] { file.getName() };
    }

    String platform = archive.platform;
    if (platform == null) {
      File remaining = new File(archive.file);
      while (remaining != null) {
        String segment = remaining.getName();
        if (segment.startsWith("android-")) {
          platform = segment.substring(segment.lastIndexOf("-") + 1);
          break;
        }
        remaining = remaining.getParentFile();
      }
      if (platform == null) {
        // If the platform isn't specified then optimistically choose a very old version for
        // best compatibility.
        platform = "12";
      }
    }
    return new AndroidArchive(
        archive.file,
        archive.sha256,
        archive.size,
        archive.ndk,
        archive.compiler,
        runtime,
        platform,
        archive.builder,
        abi,
        archive.include,
        libs,
        archive.flavor);
  }
}
