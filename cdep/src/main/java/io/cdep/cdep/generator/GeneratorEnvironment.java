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
import io.cdep.cdep.utils.ManifestUtils;
import io.cdep.cdep.yml.cdep.Dependency;
import io.cdep.cdep.yml.cdepmanifest.Coordinate;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public GeneratorEnvironment(
        PrintStream out,
        File workingFolder,
        File userFolder) {
        if (userFolder == null) {
            userFolder = new File(System.getProperty("user.home"));
        }
        this.out = out;
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
        return new String(Files.readAllBytes(
            Paths.get(getLocalDownloadedFile(coordinate, remoteArchive, forceRedownload)
                .getCanonicalPath())));
    }

    public File getLocalUnzipFolder(Coordinate coordinate, URL remoteArchive) {
        File local = unzippedArchivesFolder;
        local = new File(local, coordinate.groupId);
        local = new File(local, coordinate.artifactId);
        local = new File(local, coordinate.version);
        local = new File(local, getUrlBaseName(remoteArchive));
        return local;
    }

    private String getUrlBaseName(URL url) {
        String urlString = url.getFile();
        return urlString.substring(urlString.lastIndexOf('/') + 1, urlString.length());
    }

    public ResolvedManifest resolveAny(
        Dependency dependency,
        boolean forceRedownload) throws IOException {
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
            ManifestUtils.checkManifestSanity(resolved.cdepManifestYml);
            File local = getLocalDownloadFilename(resolved.cdepManifestYml.coordinate,
                resolved.remote);
            if (!local.exists()) {
                // Copy the file local if the resolver didn't
                getLocalDownloadedFile(resolved.cdepManifestYml.coordinate, resolved.remote,
                    forceRedownload);
            }
        }
        return resolved;
    }
}