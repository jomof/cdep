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
package io.cdep.cdep.generator;


import io.cdep.cdep.Coordinate;
import io.cdep.cdep.resolver.DownloadProvider;
import io.cdep.cdep.resolver.ManifestProvider;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.CDepSHA256Utils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.utils.HashUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepsha25.CDepSHA256;
import io.cdep.cdep.yml.cdepsha25.HashEntry;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.cdep.cdep.utils.Invariant.*;

public class GeneratorEnvironment implements ManifestProvider, DownloadProvider {

  final public PrintStream out;
  final public File downloadFolder;
  final public File unzippedArchivesFolder;
  final public File modulesFolder;
  final public File examplesFolder;
  final public File workingFolder;
  final public Map<String, String> cdepSha256Hashes = new HashMap<>();
  final public boolean ignoreManifestHashes;
  final public boolean forceRedownload;
  final public Set<File> alreadyDownloaded = new HashSet<>();

  public GeneratorEnvironment(PrintStream out, File workingFolder, File userFolder, boolean forceRedownload, boolean
      ignoreManifestHashes) {
    if (userFolder == null) {
      userFolder = new File(System.getProperty("user.home"));
    }
    this.out = out;
    this.workingFolder = workingFolder;
    this.downloadFolder = new File(userFolder, ".cdep/downloads").getAbsoluteFile();
    this.unzippedArchivesFolder = new File(userFolder, ".cdep/exploded").getAbsoluteFile();
    this.modulesFolder = new File(workingFolder, ".cdep/modules").getAbsoluteFile();
    this.examplesFolder = new File(workingFolder, ".cdep/examples").getAbsoluteFile();
    this.ignoreManifestHashes = ignoreManifestHashes;
    this.forceRedownload = forceRedownload;
  }

  private static InputStream tryGetUrlInputStream(URL url) throws IOException {
    URLConnection con = url.openConnection();
    con.connect();
    try {
      return con.getInputStream();
    } catch (FileNotFoundException e) {
      // If the file wasn't found we may want to look for it in other places. Continue by
      // returning null;
      return null;
    }
  }

  private static void copyInputStreamToLocalFile(InputStream input, File localFile) throws IOException {
    byte[] buffer = new byte[4096];
    int n;

    OutputStream output = new FileOutputStream(localFile);
    while ((n = input.read(buffer)) != -1) {
      output.write(buffer, 0, n);
    }
    output.close();
  }

  public File getLocalDownloadFilename(Coordinate coordinate, URL remoteArchive) {
    coordinate = notNull(coordinate);
    remoteArchive = notNull(remoteArchive);
    File local = downloadFolder;
    local = new File(local, coordinate.groupId);
    local = new File(local, coordinate.artifactId);
    local = new File(local, coordinate.version);
    local = new File(local, getUrlBaseName(remoteArchive));
    return local;
  }

  public File tryGetLocalDownloadedFile(Coordinate coordinate, URL remoteArchive) throws IOException {
    File local = getLocalDownloadFilename(notNull(coordinate), notNull(remoteArchive));

    if (local.isFile() && !forceRedownload) {
      return local;
    }

    // Has the file already been downloaded in this session?
    if (alreadyDownloaded.contains(local)) {
      if (forceRedownload) {
        // This ensures that redownload doesn't redownload the same file multiple times
        // Multiples can happen, for example, when two packages depend on the same sub-package.
        return local;
      }
      fail("Tried to download %s twice in the same session", local);
    }

    // Indicate whether download or force redownload
    if (forceRedownload) {
      out.printf("Redownloading %s\n", remoteArchive);
    } else {
      out.printf("Downloading %s\n", remoteArchive);
    }

    // Try to get the content at the remote. If it doesn't exist return null.
    InputStream input = tryGetUrlInputStream(remoteArchive);
    if (input == null) {
      out.printf("  didn't exist, skipping.\n");
      return null;
    }

    //noinspection ResultOfMethodCallIgnored
    local.getParentFile().mkdirs();
    copyInputStreamToLocalFile(input, local);
    alreadyDownloaded.add(local);

    require(local.exists(), "Did not write to %s", local);
    return local;
  }

  public CDepManifestYml tryGetManifest(Coordinate coordinate, URL remoteArchive) throws IOException,
      NoSuchAlgorithmException {
    File file = tryGetLocalDownloadedFile(coordinate, remoteArchive);
    if (file == null) {
      // The remote didn't exist. Return null;
      return null;
    }
    String text = FileUtils.readAllText(file);
    CDepManifestYml cdepManifestYml;
    try {
      cdepManifestYml = CDepManifestYmlUtils.convertStringToManifest(text);
    } catch (YAMLException e) {
      throw new RuntimeException(String.format("Parsing '%s'", coordinate), e);
    }
    if (!ignoreManifestHashes) {
      String sha256 = HashUtils.getSHA256OfFile(file);
      String priorSha256 = this.cdepSha256Hashes.get(cdepManifestYml.coordinate.toString());
      require(priorSha256 == null || priorSha256.equals(sha256), "SHA256 of cdep-manifest.yml for package '%s' does "
          + "not agree with value in cdep.sha256. Something changed.", cdepManifestYml.coordinate);
      this.cdepSha256Hashes.put(cdepManifestYml.coordinate.toString(), sha256);
    }
    return cdepManifestYml;
  }

  public File getLocalUnzipFolder(Coordinate coordinate, URL remoteArchive) {
    File local = unzippedArchivesFolder;
    local = new File(local, coordinate.groupId);
    local = new File(local, coordinate.artifactId);
    local = new File(local, coordinate.version);
    local = new File(local, getUrlBaseName(remoteArchive));
    return local;
  }

  public void readCDepSHA256File() throws IOException {
    File file = new File(workingFolder, "cdep.sha256");
    if (!file.exists()) {
      return;
    }
    String text = FileUtils.readAllText(file);
    CDepSHA256 cdepSha256 = CDepSHA256Utils.convertStringToCDepSHA256(text);
    for (HashEntry entry : cdepSha256.hashes) {
      this.cdepSha256Hashes.put(entry.coordinate, entry.sha256);
    }
  }

  public void writeCDepSHA256File() throws FileNotFoundException {
    File file = new File(workingFolder, "cdep.sha256");
    HashEntry entries[] = new HashEntry[cdepSha256Hashes.size()];
    int i = 0;
    for (String coordinate : cdepSha256Hashes.keySet()) {
      entries[i] = new HashEntry(coordinate, cdepSha256Hashes.get(coordinate));
      ++i;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("# This file is automatically maintained by CDep.\n#\n" + "#     MANUAL EDITS WILL BE LOST ON THE NEXT " +
        "" + "" + "" + "CDEP RUN\n#\n");
    sb.append("# This file contains a list of CDep coordinates along with the SHA256 hash of their\n");
    sb.append("# manifest file. This is to ensure that a manifest hasn't changed since the last\n");
    sb.append("# time CDep ran.\n");
    sb.append("# The recommended best practice is to check this file into source control so that\n");
    sb.append("# anyone else who builds this project is guaranteed to get the same dependencies.\n\n");
    sb.append(new CDepSHA256(entries).toString());
    try (PrintWriter out = new PrintWriter(file)) {
      out.println(sb);
    }
  }

  private String getUrlBaseName(URL url) {
    String urlString = url.getFile();
    return urlString.substring(urlString.lastIndexOf('/') + 1, urlString.length());
  }
}
