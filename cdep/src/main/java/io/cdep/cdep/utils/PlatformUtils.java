package io.cdep.cdep.utils;

/**
 * Created by jomof on 4/16/2017.
 */
public class PlatformUtils {
  public static boolean isWindows() {
    return System.getProperty("os.name").startsWith("Win");
  }
}
