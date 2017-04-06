package io.cdep.cdep.utils;

import io.cdep.cdep.ResolvedManifests;
import io.cdep.cdep.generator.GeneratorEnvironment;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class TestEnvironmentUtils {
  private final GeneratorEnvironment environment = new GeneratorEnvironment(System.out, new File("" +
      "./test-files/TestEnvironmentUtils/working"), null, false, false);

  @Test
  public void testAllResolvedManifests() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("sqliteiOS", "'sqliteiOS' does not have archive");
    expected.put("sqliteAndroid", "'sqliteAndroid' does not have archive");
    expected.put("sqliteLinuxMultiple", "'sqliteLinuxMultiple' does not have archive");
    expected.put("sqliteLinux", "'sqliteLinux' does not have archive");
    expected.put("sqlite", "'sqlite' does not have archive");
    expected.put("archiveMissingFile", "'archiveMissingFile' does not have archive.include.file");
    expected.put("singleABI", "'singleABI' does not have archive");
    expected.put("singleABISqlite", "'singleABISqlite' does not have archive");

    boolean unexpectedFailure = false;
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      String key = manifest.name;
      String expectedFailure = expected.get(key);
      try {
        EnvironmentUtils.getPackageLevelIncludeFolder(environment, key, manifest.resolved);
        if (expectedFailure != null) {
          fail("Expected failure");
        }
      } catch (RuntimeException e) {
        if (e.getMessage() == null) {
          throw e;
        }
        if (!e.getMessage().equals(expectedFailure)) {
          e.printStackTrace();
          System.out.printf("expected.put(\"%s\", \"%s\");\n", key, e.getMessage());
          unexpectedFailure = true;
        }
      }
    }
    if (unexpectedFailure) {
      throw new RuntimeException("Unexpected failures. See console.");
    }
  }
}
