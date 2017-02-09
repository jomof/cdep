package io.cdep.service;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.ManifestUtils;
import io.cdep.manifest.Coordinate;
import io.cdep.model.Reference;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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

    public File getLocalDownloadedFile(Coordinate coordinate, URL remoteArchive)
        throws IOException {
        File local = getLocalDownloadFilename(coordinate, remoteArchive);
        if (!local.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            local.getParentFile().mkdirs();
            out.printf("Downloading %s\n", remoteArchive);
            copyUrlToLocalFile(remoteArchive, local);
        }
        return local;
    }

    public String getLocalDownloadedFileText(Coordinate coordinate, URL remoteArchive)
        throws IOException {
        return new String(Files.readAllBytes(
            Paths.get(getLocalDownloadedFile(coordinate, remoteArchive).getCanonicalPath())));
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

    public ResolvedManifest resolveAny(Reference reference) throws IOException {
        ResolvedManifest resolved = null;
        for (Resolver resolver : resolvers) {
            ResolvedManifest attempt = resolver.resolve(this, reference);
            if (attempt != null) {
                if (resolved != null) {
                    throw new RuntimeException("Multiple resolvers matched coordinate:\n"
                        + reference);
                }
                resolved = attempt;
            }
        }
        if (resolved != null) {
            ManifestUtils.checkManifestSanity(resolved.manifest);
            File local = getLocalDownloadFilename(resolved.manifest.coordinate, resolved.remote);
            if (!local.exists()) {
                // Copy the file local if the resolver didn't
                getLocalDownloadedFile(resolved.manifest.coordinate, resolved.remote);
            }
        }
        return resolved;
    }

}
