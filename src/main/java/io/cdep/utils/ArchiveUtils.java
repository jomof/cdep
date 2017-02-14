package io.cdep.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveUtils {

    /**
     * Unzip the given archive.
     */
    public static void unzip(File localArchive, File localUnzipFolder) throws IOException {
        ZipFile zipFile = new ZipFile(localArchive.getPath());
        Enumeration<?> enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();

            String name = zipEntry.getName();
//            long size = zipEntry.getSize();
//            long compressedSize = zipEntry.getCompressedSize();
//            System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n",
//                name, size, compressedSize);

            File file = new File(localUnzipFolder, name);
            if (name.endsWith("/")) {
                //noinspection ResultOfMethodCallIgnored
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }

            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();

        }
        zipFile.close();

    }
}
