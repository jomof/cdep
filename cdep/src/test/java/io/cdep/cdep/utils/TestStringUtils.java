package io.cdep.cdep.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class TestStringUtils {
  @Test
  public void isNumeric() throws Exception {
    assertThat(StringUtils.isNumeric("x")).isFalse();
    assertThat(StringUtils.isNumeric("1")).isTrue();
  }

  @Test
  public void joinOn() throws Exception {
    assertThat(StringUtils.joinOn("+", "1", "2", "3")).isEqualTo("1+2+3");
  }

  @Test
  public void joinOn1() throws Exception {
    assertThat(StringUtils.joinOn("+", new Integer[]{1, 2, 3})).isEqualTo("1+2+3");
  }

  @Test
  public void joinOn2() throws Exception {
    List<String> list = new ArrayList<>();
    list.add("1");
    list.add("3");
    assertThat(StringUtils.joinOn("+", list)).isEqualTo("1+3");
  }

  @Test
  public void joinOnSkipNull() throws Exception {
    assertThat(StringUtils.joinOnSkipNull("+", "1", null, "3")).isEqualTo("1+3");
  }

  @Test
  public void coverConstructor() {
    // Call constructor of tested class to cover that code.
    new CoverConstructor();
  }

  @Test
  public void checkIsNumber() {
    assertThat(StringUtils.isNumeric("1")).isTrue();
  }

  @Test
  public void checkIsNotNumber() {
    assertThat(StringUtils.isNumeric("x")).isFalse();
  }

  private static class CoverConstructor extends StringUtils {

  }
}
