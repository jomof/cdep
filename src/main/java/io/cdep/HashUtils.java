package io.cdep;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cryptographic hashing utility functions.
 */
public class HashUtils {

    private static String encodeHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /**
     * Compute the SHA256 of the given local file. This produces the same result as:
     *
     * shasum -a 256 localfile.zip
     *
     * command run from bash.
     */
    static String getSHA256OfFile(File local) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        InputStream in = new FileInputStream(local);
        byte[] block = new byte[4096];
        int length;
        while ((length = in.read(block)) > 0) {
            digest.update(block, 0, length);
        }
        return encodeHex(digest.digest());
    }

}
