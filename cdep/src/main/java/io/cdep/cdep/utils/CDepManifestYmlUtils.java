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

import io.cdep.cdep.yml.cdepmanifest.Android;
import io.cdep.cdep.yml.cdepmanifest.Archive;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.Coordinate;
import io.cdep.cdep.yml.cdepmanifest.Linux;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

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
    }

    private static void checkAndroid(CDepManifestYml cdepManifestYml) {
        if (cdepManifestYml.android == null) {
            return;
        }
        for (Android android : cdepManifestYml.android) {
            validateAndroid(cdepManifestYml.coordinate, android);
        }
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
        if (android.file == null && (android.archives == null || android.archives.length == 0)) {
            throw new RuntimeException(
                String.format("Package '%s' has missing android.archives", coordinate));
        }
        if (android.file != null && android.sha256 == null) {
            throw new RuntimeException(
                String.format("Package '%s' has missing android.sha256 for '%s'",
                    coordinate, android.file));
        }
        if (android.archives != null) {
            for (Archive archive : android.archives) {
                if (archive.file == null) {
                    throw new RuntimeException(
                        String.format("Package '%s' has missing android.file.file",
                            coordinate));
                }
                if (archive.sha256 == null) {
                    throw new RuntimeException(
                        String.format("Package '%s' has missing android.file.sha256 for: %s",
                            coordinate, archive.file));
                }
                if (archive.size == null) {
                    throw new RuntimeException(
                        String.format("Package '%s' has missing android.file.size for: %s",
                            coordinate, archive.file));
                }
            }
        }
    }

    private static void checkForDuplicateOrMissingZipFiles(CDepManifestYml cdepManifestYml) {
        Set<String> zips = new HashSet<>();
        if (cdepManifestYml.android != null) {
            Set<String> targetZips = new HashSet<>();
            for (Android android : cdepManifestYml.android) {
                if (android.file != null && android.archives != null) {
                    throw new RuntimeException(
                        String.format(
                            "Package '%s' contains both android.file and android.file",
                            cdepManifestYml.coordinate));
                }
                if (android.file != null) {
                    if (targetZips.contains(android.file)) {
                        throw new RuntimeException(
                            String.format(
                                "Package '%s' contains multiple references to the same zip file for android target: %s",
                                cdepManifestYml.coordinate, android.file));
                    }
                    zips.add(android.file);
                    targetZips.add(android.file);
                }
                if (android.archives != null) {
                    for (Archive archive : android.archives) {
                        zips.add(archive.file);
                    }
                }
            }
        }
        if (cdepManifestYml.linux != null) {
            Set<String> targetZips = new HashSet<>();
            for (Linux linux : cdepManifestYml.linux) {
                if (targetZips.contains(linux.file)) {
                    throw new RuntimeException(
                            String.format(
                                    "Package '%s' contains multiple references to the same zip file for linux target: %s",
                                    cdepManifestYml.coordinate, linux.file));
                }
                zips.add(linux.file);
                targetZips.add(linux.file);
            }
        }
        if (zips.isEmpty()) {
            throw new RuntimeException(
                    String.format(
                            "Package '%s' does not contain any files", cdepManifestYml.coordinate));
        }
    }
}
