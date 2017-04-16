package io.cdep;

import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.utils.PlatformUtils;
import io.cdep.cdep.utils.ReflectionUtils;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.net.MalformedURLException;

import static io.cdep.cdep.utils.Invariant.require;

/**
 * Methods meant to be used for calling back from CMake or ndk-build into CDep.
 */
public class API {

  /**
   * Get the location of java.exe that started this process.
   */
  static String getJvmLocation() {
    String java = System.getProperties().getProperty("java.home")
        + File.separator + "bin" + File.separator + "java";
    if (PlatformUtils.isWindows()) {
      java += ".exe";
      java = java.replace("\\", "/");
    }
    File result = new File(java);
    require(result.isFile(), "Expected to find java at %s but didn't", result);
    return java;
  }

  public static String callCdepVersion(GeneratorEnvironment environment) throws Exception {
    String result = callCDep(environment);
    return result + "show folders";
  }

  /**
   * Get a java command-line to call back into CDep.
   */
  private static String callCDep(GeneratorEnvironment environment) throws MalformedURLException {
    StringBuilder sb = new StringBuilder();
    String java = getJvmLocation();
    sb.append("\"" + java + "\"");
    sb.append(" ");
    sb.append("-classpath ");
    String cdepClassPath = ReflectionUtils.getLocation(API.class).getAbsolutePath().replace("\\", "/");
    if (PlatformUtils.isWindows()) {
      sb.append("\"");
    }
    if (!cdepClassPath.endsWith(".jar")) {
      // In a test environment need to include SnakeYAML since it isn't part of the unit test environment
      sb.append(ReflectionUtils.getLocation(YAMLException.class).getAbsolutePath().replace("\\", "/") + ";");
    }
    sb.append(cdepClassPath);
    if (PlatformUtils.isWindows()) {
      sb.append("\"");
    }
    sb.append(" io.cdep.CDep ");
    sb.append("--working-folder \"" + environment.workingFolder.getAbsolutePath().replace("\\", "/") +"\" ");
    return sb.toString();
  }
}
