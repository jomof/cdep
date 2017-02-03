package com.jomofisher.cdep;

import com.jomofisher.cdep.manifest.Coordinate;
import java.io.File;
import java.net.URL;

public class GeneratorEnvironment {

    public File downloadFolder;
    public File unzippedArchivesFolder;
    public File modulesFolder;

    GeneratorEnvironment(
        File downloadFolder,
        File unzippedArchivesFolder,
        File modulesFolder) {
        this.downloadFolder = downloadFolder;
        this.unzippedArchivesFolder = unzippedArchivesFolder;
        this.modulesFolder = modulesFolder;
    }

    File getLocalArchiveFilename(Coordinate coordinate, URL remoteArchive) {
        File local = downloadFolder;
        local = new File(local, coordinate.groupId);
        local = new File(local, coordinate.artifactId);
        local = new File(local, coordinate.version);
        local = new File(local, getUrlBaseName(remoteArchive));
        return local;
    }

    File getLocalUnzipFolder(Coordinate coordinate, URL remoteArchive) {
        File local = unzippedArchivesFolder;
        local = new File(local, coordinate.groupId);
        local = new File(local, coordinate.artifactId);
        local = new File(local, coordinate.version);
        local = new File(local, getUrlBaseName(remoteArchive));
        return local;
    }

    String getUrlBaseName(URL url) {
        String urlString = url.getFile();
        return urlString.toString().substring(urlString.lastIndexOf('/') + 1, urlString.length());
    }
}
