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
import io.cdep.cdep.yml.cdepmanifest.Android;
import io.cdep.cdep.yml.cdepmanifest.AndroidArchive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;
import io.cdep.cdep.yml.cdepmanifest.iOS;
import io.cdep.cdep.yml.cdepmanifest.iOSArchive;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class CDepManifestYmlUtils {

  public static CDepManifestYml convertStringToManifest(String content) {
    Yaml yaml = new Yaml(new Constructor(CDepManifestYml.class));
    CDepManifestYml dependencyConfig =
        (CDepManifestYml) yaml.load(new ByteArrayInputStream(content.getBytes(
            StandardCharsets.UTF_8)));
    if (dependencyConfig == null) {
      throw new RuntimeException("Manifest was empty");
    }
    return dependencyConfig;
  }

  public static void checkManifestSanity(CDepManifestYml cdepManifestYml) {
    if (cdepManifestYml.coordinate == null) {
      throw new RuntimeException("Manifest was missing coordinate");
    }
    if (cdepManifestYml.coordinate.groupId == null) {
      throw new RuntimeException("Manifest was missing coordinate.groupId");
    }
    if (cdepManifestYml.coordinate.artifactId == null) {
      throw new RuntimeException("Manifest was missing coordinate.artifactId");
    }
    if (cdepManifestYml.coordinate.version == null) {
      throw new RuntimeException("Manifest was missing coordinate.version");
    }

    checkForMalformedCoordinateVersion(cdepManifestYml.coordinate);
    checkForDuplicateOrMissingZipFiles(cdepManifestYml);
    checkAndroid(cdepManifestYml);
    checkiOS(cdepManifestYml);
  }

  private static void checkAndroid(CDepManifestYml cdepManifestYml) {
    if (cdepManifestYml.android == null || cdepManifestYml.android.archives == null) {
      return;
    }

    validateAndroid(cdepManifestYml.coordinate, cdepManifestYml.android);
  }

  private static void checkiOS(CDepManifestYml cdepManifestYml) {
    if (cdepManifestYml.iOS == null || cdepManifestYml.iOS.archives == null) {
      return;
    }

    validateiOS(cdepManifestYml.coordinate, cdepManifestYml.iOS);
  }


  private static void checkForMalformedCoordinateVersion(Coordinate coordinate) {
    String versionDiagnosis = VersionUtils.checkVersion(coordinate.version);
    if (versionDiagnosis == null) {
      return;
    }
    throw new RuntimeException(String.format("Package '%s' has malformed version, %s",
        coordinate, versionDiagnosis));
  }

  private static void validateAndroid(Coordinate coordinate, Android android) {
    if (android.archives == null || android.archives.length == 0) {
      throw new RuntimeException(
          String.format("Package '%s' has missing android.archives", coordinate));
    }
    Set<String> zips = new HashSet<>();
    for (AndroidArchive archive : android.archives) {
      if (archive.lib != null && !archive.lib.endsWith(".a")) {
        // Android NDK team best practice recommendation is to use only static libraries.
        throw new RuntimeException(
            String.format("Package '%s' has non-static android libraryName '%s'",
                coordinate, archive.lib));
      }
      if (archive.runtime != null) {
        switch (archive.runtime) {
          case "c++":
          case "stlport":
          case "gnustl":
            break;
          default:
            throw new RuntimeException(String.format("" +
                    "Package '%s' has unexpected android runtime '%s'. Allowed: c++, stlport, gnustl",
                coordinate, archive.runtime));
        }
      }

      if (archive.file == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing android.archive.file",
                coordinate));
      }

      zips.add(archive.file);

      if (archive.sha256 == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing android.archive.sha256 for '%s'",
                coordinate, archive.file));
      }
      if (archive.size == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing android.archive.size for '%s'",
                coordinate, archive.file));
      }
    }
  }

  private static void validateiOS(Coordinate coordinate, iOS ios) {
    if (ios.archives == null || ios.archives.length == 0) {
      throw new RuntimeException(
          String.format("Package '%s' has missing ios.archives", coordinate));
    }
    Set<String> zips = new HashSet<>();
    for (iOSArchive archive : ios.archives) {
      if (archive.lib != null && !archive.lib.endsWith(".a")) {
        throw new RuntimeException(
            String.format("Package '%s' has non-static iOS libraryName '%s'",
                coordinate, archive.lib));
      }

      if (archive.file == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing ios.archive.file",
                coordinate));
      }

      zips.add(archive.file);

      if (archive.sha256 == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing ios.archive.sha256 for '%s'",
                coordinate, archive.file));
      }
      if (archive.size == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing ios.archive.size for '%s'",
                coordinate, archive.file));
      }
      if (archive.sdk == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing ios.archive.sdk for '%s'",
                coordinate, archive.file));
      }
      if (archive.platform == null) {
        throw new RuntimeException(
            String.format("Package '%s' has missing ios.archive.platform for '%s'",
                coordinate, archive.file));
      }
    }
  }


  private static void checkForDuplicateOrMissingZipFiles(CDepManifestYml cdepManifestYml) {
    Set<String> zips = new HashSet<>();
    if (cdepManifestYml.archive != null) {
      zips.add(cdepManifestYml.archive.file);
    }
    if (cdepManifestYml.android != null && cdepManifestYml.android.archives != null) {
      for (AndroidArchive archive : cdepManifestYml.android.archives) {
        if (zips.contains(archive.file)) {
          throw new RuntimeException(
              String.format("Package '%s' contains multiple references to the same" +
                  " archive file '%s'", cdepManifestYml.coordinate, archive.file));
        }
        zips.add(archive.file);
      }
    }
    if (cdepManifestYml.iOS != null && cdepManifestYml.iOS.archives != null) {
      for (iOSArchive archive : cdepManifestYml.iOS.archives) {
        if (zips.contains(archive.file)) {
          throw new RuntimeException(
              String.format("Package '%s' contains multiple references to the same" +
                  " archive file '%s'", cdepManifestYml.coordinate, archive.file));
        }
        zips.add(archive.file);
      }
    }
    if (zips.isEmpty()) {
      throw new RuntimeException(
          String.format(
              "Package '%s' does not contain any files", cdepManifestYml.coordinate));
    }
  }

  public static List<HardNameDependency> getTransitiveDependencies(
      CDepManifestYml cdepManifestYml) {
    List<HardNameDependency> dependencies = new ArrayList<>();
    if (cdepManifestYml.dependencies != null) {
      for (HardNameDependency dependency : cdepManifestYml.dependencies) {
        dependencies.add(dependency);
      }
    }
    return dependencies;
  }
}
