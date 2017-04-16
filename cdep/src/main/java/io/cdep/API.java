package io.cdep;

import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.utils.PlatformUtils;
import io.cdep.cdep.utils.ReflectionUtils;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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

  public static List<String> callCdepVersion(GeneratorEnvironment environment) throws Exception {
    List<String> result = new ArrayList<>();
    result.addAll(callCDep(environment));
    result.add("show");
    result.add("folders");
    return result;
  }

  /**
   * Get a java command-line to call back into CDep.
   */
  private static List<String> callCDep(GeneratorEnvironment environment) throws MalformedURLException {
    List<String> result = new ArrayList<>();
    result.add(getJvmLocation());
    result.add("-classpath");
    String classPath = ReflectionUtils.getLocation(API.class).getAbsolutePath().replace("\\", "/");

    if (!classPath.endsWith(".jar")) {
      // In a test environment need to include SnakeYAML since it isn't part of the unit test environment
      classPath = ReflectionUtils.getLocation(YAMLException.class).getAbsolutePath().replace("\\", "/")
          + ";" + classPath;
    }
    result.add(classPath);
    result.add("io.cdep.CDep");
    result.add("--working-folder");
    result.add( environment.workingFolder.getAbsolutePath().replace("\\", "/"));

    return result;
  }
}
