package io.cdep;

import io.cdep.cdep.utils.PlatformUtils;
import io.cdep.cdep.utils.ReflectionUtils;

import java.io.File;
import java.net.MalformedURLException;

import static io.cdep.cdep.utils.Invariant.require;

/**
 * Methods meant to be used for calling back from CMake or ndk-build into CDep.
 */
public class API {
  static File getAPIJar() throws MalformedURLException {
    return ReflectionUtils.getLocation(API.class);
  }

  public static void downloadArchive(String coordinate, String archive) throws Exception {
    File x = getAPIJar();
    System.out.printf("Downloading %s %s\n", coordinate, archive);
  }

  /**
   * Get the location of java.exe that started this process.
   */
  static File getJvmLocation() {
    String java = System.getProperties().getProperty("java.home")
        + File.separator + "bin" + File.separator + "java";
    if (PlatformUtils.isWindows()) {
      java += ".exe";
    }
    File result = new File(java);
    require(result.isFile(), "Expected to find java at %s but didn't", result);
    return result;
  }

  public static String callCdepVersion() throws Exception {
    String result = callCDep();
    return result + "--version";
  }

  /**
   * Get a java command-line to call back into CDep.
   */
  private static String callCDep() throws MalformedURLException {
    StringBuilder sb = new StringBuilder();
    File java = getJvmLocation();
    sb.append("\"" + java + "\"");
    sb.append(" ");
    sb.append("-classpath ");
    sb.append("\"" + getAPIJar().getAbsolutePath() + "\" ");
    sb.append("io.cdep.CDep ");
    return sb.toString();
  }
}
