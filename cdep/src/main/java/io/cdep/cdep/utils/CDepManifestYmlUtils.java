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

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.yml.cdepmanifest.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.cdep.cdep.utils.Invariant.fail;
import static io.cdep.cdep.utils.Invariant.require;

public class CDepManifestYmlUtils {

  public static CDepManifestYml convertStringToManifest(String content) {
    Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
    CDepManifestYml dependencyConfig = (CDepManifestYml) yaml.load(new ByteArrayInputStream(content.getBytes
        (StandardCharsets.UTF_8)));
    require(dependencyConfig != null, "Manifest was empty");
    return dependencyConfig;
  }

  public static void checkManifestSanity(CDepManifestYml cdepManifestYml) {
    new Checker().visit(cdepManifestYml, CDepManifestYml.class);
  }

  public static List<HardNameDependency> getTransitiveDependencies(CDepManifestYml cdepManifestYml) {
    List<HardNameDependency> dependencies = new ArrayList<>();
    if (cdepManifestYml.dependencies != null) {
      for (HardNameDependency dependency : cdepManifestYml.dependencies) {
        dependencies.add(dependency);
      }
    }
    return dependencies;
  }

  public static class Checker extends CDepManifestYmlReadonlyVisitor {
    private Coordinate coordinate = null;
    private Set<String> filesSeen = new HashSet<>();

    @Override
    public void visitString(String name, String node) {
      if (name != null && name.equals("file")) {
        if (filesSeen.contains(node.toLowerCase())) {
          throw new RuntimeException(String.format("Package '%s' contains multiple references to the same" + " " +
              "archive file '%s'", coordinate, node));
        }
        filesSeen.add(node.toLowerCase());
      }
    }

    @Override
    public void visitCDepManifestYml(String name, CDepManifestYml value) {
      coordinate = value.coordinate;
      if (coordinate == null) {
        throw new RuntimeException("Manifest was missing coordinate");
      }
      super.visitCDepManifestYml(name, value);
      if (filesSeen.isEmpty()) {
        throw new RuntimeException(String.format("Package '%s' does not contain any files", coordinate));
      }
    }

    @Override
    public void visitArchive(String name, Archive value) {
      if (value == null) {
        return;
      }
      if (value.file == null || value.file.length() == 0) {
        throw new RuntimeException(String.format("Archive is missing file", name));
      }
      if (value.sha256 == null || value.sha256.length() == 0) {
        throw new RuntimeException(String.format("Archive is missing sha256", name));
      }
      if (value.size == null || value.size == 0) {
        throw new RuntimeException(String.format("Archive is missing size or it is zero", name));
      }
      super.visitArchive(name, value);
    }

    @Override
    public void visitiOS(String name, iOS value) {
      if (value.archives != null) {
        Set<String> zips = new HashSet<>();
        for (iOSArchive archive : value.archives) {
          if (archive.lib != null && !archive.lib.endsWith(".a")) {
            throw new RuntimeException(String.format("Package '%s' has non-static iOS libraryName '%s'", coordinate,
                archive.lib));
          }

          if (archive.file == null) {
            throw new RuntimeException(String.format("Package '%s' has missing ios.archive.file", coordinate));
          }

          zips.add(archive.file);

          if (archive.sha256 == null) {
            throw new RuntimeException(String.format("Package '%s' has missing ios.archive.sha256 for '%s'",
                coordinate, archive.file));
          }
          if (archive.size == null) {
            throw new RuntimeException(String.format("Package '%s' has missing ios.archive.size for '%s'",
                coordinate, archive.file));
          }
          if (archive.sdk == null) {
            throw new RuntimeException(String.format("Package '%s' has missing ios.archive.sdk for '%s'", coordinate,
                archive.file));
          }
          if (archive.platform == null) {
            throw new RuntimeException(String.format("Package '%s' has missing ios.archive.platform for '%s'",
                coordinate, archive.file));
          }
        }
      }

      super.visitiOS(name, value);
    }

    @Override
    public void visitLinux(String name, Linux value) {
      if (value.archives != null) {
        if (value.archives.length > 1) {
          throw new RuntimeException(String.format("Package '%s' has multiple linux archives. Only one is allowed.",
              coordinate));
        }
      }
      super.visitLinux(name, value);
    }

    @Override
    public void visitAndroid(String name, Android value) {
      if (value.archives != null) {
        Set<String> zips = new HashSet<>();
        for (AndroidArchive archive : value.archives) {
          require(archive.lib == null || archive.lib.endsWith(".a"), "Package '%s' has non-static android " +
              "libraryName '%s'", coordinate, archive.lib);
          if (archive.runtime != null) {
            switch (archive.runtime) {
              case "c++":
              case "stlport":
              case "gnustl":
                break;
              default:
                fail("Package '%s' has unexpected android runtime '%s'. Allowed: c++, stlport, gnustl", coordinate,
                    archive.runtime);
            }
          }

          require(archive.file != null, "Package '%s' has missing android.archive.file", coordinate);
          zips.add(archive.file);

          require(archive.sha256 != null, "Package '%s' has missing android.archive.sha256 for '%s'", coordinate,
              archive.file);
          require(archive.size != null, "Package '%s' has missing android.archive.size for '%s'", coordinate, archive
              .file);
        }
      }

      super.visitAndroid(name, value);
    }

    @Override
    public void visitCoordinate(String name, Coordinate value) {
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
