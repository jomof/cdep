package io.cdep.cdep.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TestVersionUtils {
  @Test
  public void coverConstructor() {
    // Call constructor of tested class to cover that code.
    new CoverConstructor();
  }

  @Test
  public void simple() {
    assertThat(VersionUtils.checkVersion("1.2.3")).isNull();
  }

  @Test
  public void tweak() {
    assertThat(VersionUtils.checkVersion("1.2.3-rev9")).isNull();
  }

  @Test
  public void majorNotNumber() {
    assertThat(VersionUtils.checkVersion("x.2.3")).isEqualTo("expected major.minor.point[-tweak] but major version 'x' wasn't "
        + "a" + " number");
  }

  @Test
  public void minorNotNumber() {
    assertThat(VersionUtils.checkVersion("1.y.3")).isEqualTo("expected major.minor.point[-tweak] but minor version 'y' wasn't " +
        "a" + " number");
  }

  @Test
  public void pointNotNumber() {
    assertThat(VersionUtils.checkVersion("1.2.z")).isEqualTo("expected major.minor.point[-tweak] but point version 'z' wasn't " +
        "a" + " number");
  }

  @Test
  public void pointWithTweakNotNumber() {
    assertThat(VersionUtils.checkVersion("1.2.1z-rev8")).isEqualTo("expected major.minor.point[-tweak] but point version '1z' "
        + "wasn't a number");
  }

  @Test
  public void noDots() {
    assertThat(VersionUtils.checkVersion("1")).isEqualTo("expected major.minor.point[-tweak] but there were no dots");
  }

  @Test
  public void oneDot() {
    assertThat(VersionUtils.checkVersion("1.2")).isEqualTo("expected major.minor.point[-tweak] but there was only one dot");
  }

  @Test
  public void fourDots() {
    assertThat(VersionUtils.checkVersion("1.2.3.4")).isEqualTo("expected major.minor.point[-tweak] but there were 3 dots");
  }

  private static class CoverConstructor extends VersionUtils {

  }
}
