package com.jomofisher.cdep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class WebUtils {

  public static String getUrlAsString(String url) throws IOException {
    URL urlObj = new URL(url);
    URLConnection con = urlObj.openConnection();

    con.setDoOutput(true);
    con.connect();

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

    StringBuilder response = new StringBuilder();
    String inputLine;

    String newLine = System.getProperty("line.separator");
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine + newLine);
    }

    in.close();

    return response.toString();
  }

  public static void pingUrl(URL url) throws IOException {
    URLConnection con = url.openConnection();
    con.connect();
    con.getInputStream().read();
  }

  public static void copyUrlToLocalFile(URL url, File localFile) throws IOException {
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
}
