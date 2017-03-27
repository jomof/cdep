package io.cdep.cdep.utils;

import io.cdep.cdep.yml.cdep.CDepYml;
import org.junit.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class TestCDepYmlUtils {

  @Test
  public void testDuplicateGenerator() {
    CDepYml cdep = CDepYmlUtils.fromString("builders: [cmake, cmake]");

    try {
      CDepYmlUtils.checkSanity(cdep, new File("cdep.yml"));
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("cdep.yml 'builders' contains 'cmake' more than once");
    }
  }

  @Test
  public void testNoBuilders() {
    CDepYml cdep = CDepYmlUtils.fromString("builders: []");

    try {
      CDepYmlUtils.checkSanity(cdep, new File("cdep.yml"));
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("cdep.yml 'builders' section is missing or empty. Valid values are: cmake cmakeExamples.");
    }
  }

  @Test
  public void testWorksFine() {
    CDepYml cdep = CDepYmlUtils.fromString("builders: [cmake]");
    CDepYmlUtils.checkSanity(cdep, new File("cdep.yml"));
  }

  @Test
  public void coverConstructor() {
    new CDepYmlUtils() {
    };
  }
}
