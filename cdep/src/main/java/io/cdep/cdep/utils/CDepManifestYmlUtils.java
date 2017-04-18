/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.yml.cdepmanifest.*;
import io.cdep.cdep.yml.cdepmanifest.v3.V3Reader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.cdep.cdep.utils.Invariant.fail;
import static io.cdep.cdep.utils.Invariant.require;

public class CDepManifestYmlUtils {
  @NotNull
  public static String convertManifestToString(@NotNull CDepManifestYml manifest) {
    return CreateCDepManifestYmlString.serialize(manifest);
  }

  @NotNull
  public static CDepManifestYml convertStringToManifest(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
    CDepManifestYml manifest;
    try {
      // Try to read current version
      manifest = (CDepManifestYml) yaml.load(
          new ByteArrayInputStream(content.getBytes(StandardCharsets
              .UTF_8)));
      if (manifest != null) {
        manifest.sourceVersion = CDepManifestYmlVersion.vlatest;
      }
    } catch (YAMLException e) {
      try {
        manifest = V3Reader.convertStringToManifest(content);
      } catch (YAMLException e2) {
        // If older readers also couldn't read it then throw the original exception.
        throw e;
      }
    }
    require(manifest != null, "Manifest was empty");
    assert manifest != null;
    return manifest;
  }

  public static void checkManifestSanity(@NotNull CDepManifestYml cdepManifestYml) {
    new Checker().visit(cdepManifestYml, CDepManifestYml.class);
  }

  @NotNull
  public static List<HardNameDependency> getTransitiveDependencies(@NotNull CDepManifestYml cdepManifestYml) {
    List<HardNameDependency> dependencies = new ArrayList<>();
    if (cdepManifestYml.dependencies != null) {
      Collections.addAll(dependencies, cdepManifestYml.dependencies);
    }
    return dependencies;
  }

  public static class Checker extends CDepManifestYmlReadonlyVisitor {
    @NotNull
    private final Set<String> filesSeen = new HashSet<>();
    @NotNull
    private final Map<String, AndroidArchive> distinguishableAndroidArchives = new HashMap<>();
    @Nullable
    private Coordinate coordinate = null;
    @Nullable
    private CDepManifestYmlVersion sourceVersion = null;

    @Nullable
    private static String nullToStar(@Nullable String string) {
      if (string == null) {
        return "*";
      }
      return string;
    }

    @Override
    public void visitString(@Nullable String name, @NotNull String node) {
      if (name == null) {
        return;
      }
      if (name.equals("file")) {
        assert sourceVersion != null;
        if (sourceVersion.ordinal() > CDepManifestYmlVersion.v1.ordinal()) {
          require(!filesSeen.contains(node.toLowerCase()),
              "Package '%s' contains multiple references to the same archive file '%s'",
              coordinate,
              node);
        }
        filesSeen.add(node.toLowerCase());
      } else if (name.equals("sha256")) {
        if (node.length() == 0) {
          require(false,
              "Package '%s' contains null or empty sha256",
              coordinate);
        }
      }
    }

    @Override
    public void visitHardNameDependency(@Nullable String name, @NotNull HardNameDependency value) {
      require(value.compile != null, "Package '%s' contains dependency with no 'compile' constant", coordinate);
      require(value.sha256 != null, "Package '%s' contains dependency '%s' with no sha256 constant",
          coordinate,
          value.compile);
      super.visitHardNameDependency(name, value);
    }

    @Override
    public void visitCDepManifestYml(@Nullable String name, @NotNull CDepManifestYml value) {
      coordinate = value.coordinate;
      sourceVersion = value.sourceVersion;
      require(coordinate != null, "Manifest was missing coordinate");
      super.visitCDepManifestYml(name, value);
      require(!filesSeen.isEmpty(), "Package '%s' does not contain any files", coordinate);
    }

    @Override
    public void visitArchive(@Nullable String name, @Nullable Archive value) {
      if (value == null) {
        return;
      }
      require(value.file != null && value.file.length() != 0, "Archive %s is missing file", coordinate);
      require(value.sha256 != null && value.sha256.length() != 0, "Archive %s is missing sha256", coordinate);
      require(value.size != null && value.size != 0, "Archive %s is missing size or it is zero", coordinate);
      require(value.include != null && value.include.length() != 0, "Archive %s is missing include", coordinate);
      super.visitArchive(name, value);
    }

    @Override
    public void visitAndroidArchive(@Nullable String name, @NotNull AndroidArchive value) {
      require(value.file != null && value.file.length() != 0, "Android archive %s is missing file", coordinate);
      require(value.sha256 != null && value.sha256.length() != 0, "Android archive %s is missing sha256", coordinate);
      require(value.size != null && value.size != 0, "Android archive %s is missing size or it is zero", coordinate);

      // Have we seen another Archive that is indistinguishable from this one?
      String key = nullToStar(value.abi) + "-";
      key += nullToStar(value.platform) + "-";
      key += nullToStar(value.runtime) + "-";
      AndroidArchive other = distinguishableAndroidArchives.get(key);

      if (other != null) {
        require(false,
            "Android archive %s file %s is indistinguishable at build time from %s given the information in the manifest",
            coordinate,
            archiveName(value),
            archiveName(other));
      }
      distinguishableAndroidArchives.put(key, value);
      super.visitAndroidArchive(name, value);
    }

    @NotNull
    private String archiveName(@NotNull AndroidArchive value) {
      return value.file == null ? "<unknown>" : value.file;
    }

    @Override
    public void visitiOSArchive(@Nullable String name, @NotNull iOSArchive value) {
      require(value.file != null && value.file.length() != 0, "iOS archive %s is missing file", coordinate);
      require(value.sha256 != null && value.sha256.length() != 0, "iOS archive %s is missing sha256", coordinate);
      require(value.size != null && value.size != 0, "iOS archive %s is missing size or it is zero", coordinate);
      super.visitiOSArchive(name, value);
    }

    @Override
    public void visitLinuxArchive(@Nullable String name, @NotNull LinuxArchive value) {
      require(value.file != null && value.file.length() != 0, "iOS archive %s is missing file", coordinate);
      require(value.sha256 != null && value.sha256.length() != 0, "iOS archive %s is missing sha256", coordinate);
      require(value.size != null && value.size != 0, "iOS archive %s is missing size or it is zero", coordinate);
      super.visitLinuxArchive(name, value);
    }

    @Override
    public void visitiOS(@Nullable String name, @NotNull iOS value) {
      if (value.archives != null) {
        for (iOSArchive archive : value.archives) {
          require(archive.file != null, "Package '%s' has missing ios.archive.file", coordinate);
          require(archive.sha256 != null, "Package '%s' has missing ios.archive.sha256 for '%s'", coordinate, archive.file);
          require(archive.size != null, "Package '%s' has missing ios.archive.size for '%s'", coordinate, archive.file);
          require(archive.sdk != null, "Package '%s' has missing ios.archive.sdk for '%s'", coordinate, archive.file);
          require(archive.platform != null, "Package '%s' has missing ios.archive.platform for '%s'", coordinate, archive.file);
          if (archive.libs == null) {
            continue;
          }
          for (String lib : archive.libs) {
            require(lib.endsWith(".a"),
                "Package '%s' has non-static iOS libraryName '%s'",
                coordinate, lib);
          }
        }
      }
      super.visitiOS(name, value);
    }

    @Override
    public void visitLinux(@Nullable String name, @NotNull Linux linux) {
      if (linux.archives == null) {
        return;
      }
      require(linux.archives.length <= 1, "Package '%s' has multiple linux archives. Only one is allowed.", coordinate);
      for (LinuxArchive archive : linux.archives) {
        assert archive.libs != null;
        for (String lib : archive.libs) {
          require(lib.endsWith(".a"),
              "Package '%s' has non-static android libraryName '%s'",
              coordinate, lib);
        }
      }
      super.visitLinux(name, linux);
    }

    @Override
    public void visitAndroid(@Nullable String name, @NotNull Android value) {
      if (value.archives != null) {
        for (AndroidArchive archive : value.archives) {

          require(archive.file != null, "Package '%s' has missing android.archive.file", coordinate);
          require(archive.sha256 != null, "Package '%s' has missing android.archive.sha256 for '%s'", coordinate, archive.file);
          require(archive.size != null, "Package '%s' has missing android.archive.size for '%s'", coordinate, archive.file);
          if (archive.libs == null) {
            continue;
          }
          for (String lib : archive.libs) {
            require(lib.endsWith(".a"),
                "Package '%s' has non-static android libraryName '%s'",
                coordinate, lib);
            if (archive.runtime != null) {
              switch (archive.runtime) {
                case "c++":
                case "stlport":
                case "gnustl":
                  break;
                default:
                  fail("Package '%s' has unexpected android runtime '%s'. Allowed: c++, stlport, gnustl",
                      coordinate,
                      archive.runtime);
              }
            }
          }
        }
      }

      super.visitAndroid(name, value);
    }

    @Override
    public void visitCoordinate(@Nullable String name, @NotNull Coordinate value) {
      assert coordinate != null;
      require(coordinate.groupId.length() > 0, "Manifest was missing coordinate.groupId");
      require(coordinate.artifactId.length() > 0, "Manifest was missing coordinate.artifactId");
      require(coordinate.version.value.length() > 0, "Manifest was missing coordinate.version");

      String versionDiagnosis = VersionUtils.checkVersion(coordinate.version);
      if (versionDiagnosis == null) {
        super.visitCoordinate(name, value);
        return;
      }
      fail("Package '%s' has malformed version, %s", coordinate, versionDiagnosis);
    }
  }
}
