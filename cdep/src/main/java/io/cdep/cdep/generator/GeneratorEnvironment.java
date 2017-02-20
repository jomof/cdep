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

import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.resolver.GithubReleasesCoordinateResolver;
import io.cdep.cdep.resolver.GithubStyleUrlResolver;
import io.cdep.cdep.resolver.LocalFilePathResolver;
import io.cdep.cdep.resolver.Resolver;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.CDepSHA256Utils;
import io.cdep.cdep.utils.FileUtils;
import io.cdep.cdep.utils.HashUtils;
import io.cdep.cdep.yml.Coordinate;
import io.cdep.cdep.yml.cdep.Dependency;
import io.cdep.cdep.yml.cdepsha25.CDepSHA256;
import io.cdep.cdep.yml.cdepsha25.HashEntry;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class GeneratorEnvironment {

    final private static Resolver resolvers[] = new Resolver[]{
            new GithubStyleUrlResolver(),
            new GithubReleasesCoordinateResolver(),
            new LocalFilePathResolver()
    };

    final public PrintStream out;
    final public File downloadFolder;
    final public File unzippedArchivesFolder;
    final public File modulesFolder;
    final public File workingFolder;
    final public Map<String, String> cdepSha256Hashes = new HashMap<>();

    public GeneratorEnvironment(
            PrintStream out,
            File workingFolder,
            File userFolder) {
        if (userFolder == null) {
            userFolder = new File(System.getProperty("user.home"));
        }
        this.out = out;
        this.workingFolder = workingFolder;
        this.downloadFolder = new File(userFolder, ".cdep/downloads").getAbsoluteFile();
        this.unzippedArchivesFolder = new File(userFolder, ".cdep/exploded").getAbsoluteFile();
        this.modulesFolder = new File(workingFolder, ".cdep/modules").getAbsoluteFile();
    }

    private static void copyUrlToLocalFile(URL url, File localFile) throws IOException {
        URLConnection con = url.openConnection();
        con.connect();
        InputStream input = con.getInputStream();
        byte[] buffer = new byte[4096];
        int n;

        OutputStream output = new FileOutputStream(localFile);
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        output.close();
    }

    public File getLocalDownloadFilename(Coordinate coordinate, URL remoteArchive) {
        File local = downloadFolder;
        local = new File(local, coordinate.groupId);
        local = new File(local, coordinate.artifactId);
        local = new File(local, coordinate.version);
        local = new File(local, getUrlBaseName(remoteArchive));
        return local;
    }

    public File getLocalDownloadedFile(
            Coordinate coordinate,
            URL remoteArchive,
            boolean forceRedownload)
            throws IOException {
        File local = getLocalDownloadFilename(coordinate, remoteArchive);
        if (!local.isFile()) {
            out.printf("Downloading %s\n", remoteArchive);
        } else if (forceRedownload) {
            out.printf("Redownloading %s\n", remoteArchive);
        } else {
            return local;
        }
        //noinspection ResultOfMethodCallIgnored
        local.getParentFile().mkdirs();
        copyUrlToLocalFile(remoteArchive, local);
        return local;
    }

    public String getLocalDownloadedFileText(
            Coordinate coordinate,
            URL remoteArchive,
            boolean forceRedownload)
            throws IOException {
        File file = getLocalDownloadedFile(coordinate, remoteArchive, forceRedownload);
        return FileUtils.readAllText(file);
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
        sb.append(
                "# This file is automatically maintained by CDep.\n#\n" +
                        "#     MANUAL EDITS WILL BE LOST ON THE NEXT CDEP RUN\n#\n");
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

    public ResolvedManifest resolveAny(
            Dependency dependency,
            boolean forceRedownload) throws IOException, NoSuchAlgorithmException {
        ResolvedManifest resolved = null;
        for (Resolver resolver : resolvers) {
            ResolvedManifest attempt = resolver.resolve(this, dependency, forceRedownload);
            if (attempt != null) {
                if (resolved != null) {
                    throw new RuntimeException("Multiple resolvers matched coordinate:\n"
                            + dependency);
                }
                resolved = attempt;
            }
        }
        if (resolved != null) {
            CDepManifestYmlUtils.checkManifestSanity(resolved.cdepManifestYml);
            File local = getLocalDownloadFilename(resolved.cdepManifestYml.coordinate,
                    resolved.remote);
            if (!local.exists()) {
                // Copy the file local if the resolver didn't
                getLocalDownloadedFile(resolved.cdepManifestYml.coordinate, resolved.remote,
                        forceRedownload);
            }
            String sha256 = HashUtils.getSHA256OfFile(local);
            String priorSha256 = this.cdepSha256Hashes.get(resolved.cdepManifestYml.coordinate.toString());
            if (priorSha256 != null && !priorSha256.equals(sha256)) {
                throw new RuntimeException(String.format(
                        "SHA256 of cdep-manifest.yml for package '%s' does not agree with value in " +
                                "cdep.sha256. Something changed.",
                        resolved.cdepManifestYml.coordinate));
            }
            this.cdepSha256Hashes.put(resolved.cdepManifestYml.coordinate.toString(), sha256);
        }
        return resolved;
    }
}
