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
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.cdep.cdep.utils.Invariant.fail;
import static io.cdep.cdep.utils.Invariant.require;

public class CDepManifestYmlUtils {

  @NotNull
  public static CDepManifestYml convertStringToManifest(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
    CDepManifestYml dependencyConfig = (CDepManifestYml) yaml.load(new ByteArrayInputStream(content.getBytes(StandardCharsets
        .UTF_8)));
    require(dependencyConfig != null, "Manifest was empty");
    return dependencyConfig;
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
    @Nullable
    private Coordinate coordinate = null;

    @Override
    public void visitString(@Nullable String name, @NotNull String node) {
      if (name != null && name.equals("file")) {
        require(!filesSeen.contains(node.toLowerCase()),
            "Package '%s' contains multiple references to the same" + " " + "archive file '%s'",
            coordinate,
            node);
        filesSeen.add(node.toLowerCase());
      }
    }

    @Override
    public void visitCDepManifestYml(@Nullable String name, @NotNull CDepManifestYml value) {
      coordinate = value.coordinate;
      require(coordinate != null, "Manifest was missing coordinate");
      super.visitCDepManifestYml(name, value);
      require(!filesSeen.isEmpty(), "Package '%s' does not contain any files", coordinate);
    }

    @Override
    public void visitArchive(@Nullable String name, @Nullable Archive value) {
      if (value == null) {
        return;
      }
      require(value.file != null && value.file.length() != 0, "Archive is missing file", name);
      require(value.sha256 != null && value.sha256.length() != 0, "Archive is missing sha256", name);
      require(value.size != null && value.size != 0, "Archive is missing size or it is zero", name);
      super.visitArchive(name, value);
    }

    @Override
    public void visitiOS(@Nullable String name, @NotNull iOS value) {
      if (value.archives != null) {
        for (iOSArchive archive : value.archives) {
          require(archive.lib == null || archive.lib.endsWith(".a"),
              "Package '%s' has non-static iOS libraryName " + "'%s'",
              coordinate,
              archive.lib);
          require(archive.file != null, "Package '%s' has missing ios.archive.file", coordinate);
          require(archive.sha256 != null, "Package '%s' has missing ios.archive.sha256 for '%s'", coordinate, archive.file);
          require(archive.size != null, "Package '%s' has missing ios.archive.size for '%s'", coordinate, archive.file);
          require(archive.sdk != null, "Package '%s' has missing ios.archive.sdk for '%s'", coordinate, archive.file);
          require(archive.platform != null, "Package '%s' has missing ios.archive.platform for '%s'", coordinate, archive.file);
        }
      }

      super.visitiOS(name, value);
    }

    @Override
    public void visitLinux(@Nullable String name, @NotNull Linux value) {
      if (value.archives != null) {
        require(value.archives.length <= 1, "Package '%s' has multiple linux archives. Only one is allowed.", coordinate);
      }
      super.visitLinux(name, value);
    }

    @Override
    public void visitAndroid(@Nullable String name, @NotNull Android value) {
      if (value.archives != null) {
        for (AndroidArchive archive : value.archives) {
          require(archive.lib == null || archive.lib.endsWith(".a"),
              "Package '%s' has non-static android " + "libraryName " + "'%s'",
              coordinate,
              archive.lib);
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

          require(archive.file != null, "Package '%s' has missing android.archive.file", coordinate);
          require(archive.sha256 != null, "Package '%s' has missing android.archive.sha256 for '%s'", coordinate, archive.file);
          require(archive.size != null, "Package '%s' has missing android.archive.size for '%s'", coordinate, archive.file);
        }
      }

      super.visitAndroid(name, value);
    }

    @Override
    public void visitCoordinate(@Nullable String name, @NotNull Coordinate value) {
      assert coordinate != null;
      require(coordinate.groupId != null, "Manifest was missing coordinate.groupId");
      require(coordinate.artifactId != null, "Manifest was missing coordinate.artifactId");
      require(coordinate.version != null, "Manifest was missing coordinate.version");

      String versionDiagnosis = VersionUtils.checkVersion(coordinate.version);
      if (versionDiagnosis == null) {
        super.visitCoordinate(name, value);
        return;
      }
      fail("Package '%s' has malformed version, %s", coordinate, versionDiagnosis);
    }
  }
}
