package io.cdep.cdep.utils;

public class PlatformUtils {
  public static boolean isWindows() {
    return System.getProperty("os.name").startsWith("Win");
  }
}
