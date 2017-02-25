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
        checkLinux(cdepManifestYml);
    }

    private static void checkAndroid(CDepManifestYml cdepManifestYml) {
        if (cdepManifestYml.android == null) {
            return;
        }
        for (Android android : cdepManifestYml.android) {
            validateAndroid(cdepManifestYml.coordinate, android);
        }
    }

    private static void checkLinux(CDepManifestYml cdepManifestYml) {
        if (cdepManifestYml.linux == null) {
            return;
        }
        for (Linux linux : cdepManifestYml.linux) {
            validateLinux(cdepManifestYml.coordinate, linux);
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
        if (android.archives == null || android.archives.length == 0) {
            throw new RuntimeException(
                String.format("Package '%s' has missing android.archives", coordinate));
        }
        if (android.lib != null && !android.lib.endsWith(".a")) {
            // Android NDK team best practice recommendation is to use only static libraries.
            throw new RuntimeException(
                    String.format("Package '%s' has non-static android lib '%s'",
                        coordinate, android.lib));
        }
        if (android.runtime != null) {
            switch (android.runtime) {
                case "c++":
                case "stlport":
                case "gnustl":
                    break;
                default:
                    throw new RuntimeException(String.format("" +
                            "Package '%s' has unexpected android runtime '%s'. Allowed: c++, stlport, gnustl",
                            coordinate, android.runtime));
            }
        }
        if (android.archives != null) {
            Set<String> zips = new HashSet<>();
            for (Archive archive : android.archives) {
                if (archive.file == null) {
                    throw new RuntimeException(
                            String.format("Package '%s' has missing android.archive.file",
                                    coordinate));
                }
                if (zips.contains(archive.file)) {
                    throw new RuntimeException(String.format("Package '%s' contains multiple references to the same" +
                            " zip file '%s'", coordinate, archive.file));
                }
                zips.add(archive.file);
            }
            for (Archive archive : android.archives) {
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
    }

    private static void validateLinux(Coordinate coordinate, Linux linux) {
        if (linux.archives == null || linux.archives.length == 0) {
            throw new RuntimeException(
                String.format("Package '%s' has missing linux.archives", coordinate));
        }
        if (linux.archives != null) {
            Set<String> zips = new HashSet<>();
            for (Archive archive : linux.archives) {
                if (archive.file == null) {
                    throw new RuntimeException(
                            String.format("Package '%s' has missing linux.archive.file",
                                    coordinate));
                }
                if (zips.contains(archive.file)) {
                    throw new RuntimeException(String.format("Package '%s' contains multiple references to the same" +
                            " zip file '%s'", coordinate, archive.file));
                }
                zips.add(archive.file);
            }
            for (Archive archive : linux.archives) {
                if (archive.sha256 == null) {
                    throw new RuntimeException(
                        String.format("Package '%s' has missing linux.archive.sha256 for '%s'",
                            coordinate, archive.file));
                }
                if (archive.size == null) {
                    throw new RuntimeException(
                        String.format("Package '%s' has missing linux.archive.size for '%s'",
                            coordinate, archive.file));
                }

            }
        }
    }

    private static void checkForDuplicateOrMissingZipFiles(CDepManifestYml cdepManifestYml) {
        Set<String> zips = new HashSet<>();
        if (cdepManifestYml.android != null) {
            for (Android android : cdepManifestYml.android) {
                if (android.archives != null) {
                    for (Archive archive : android.archives) {
                        zips.add(archive.file);
                    }
                }
            }
        }
        if (cdepManifestYml.linux != null) {
            for (Linux linux : cdepManifestYml.linux) {
                if (linux.archives != null) {
                    for (Archive archive : linux.archives) {
                        zips.add(archive.file);
                    }
                }
            }
        }
        if (zips.isEmpty()) {
            throw new RuntimeException(
                    String.format(
                            "Package '%s' does not contain any files", cdepManifestYml.coordinate));
        }
    }
}
