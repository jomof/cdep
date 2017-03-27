package io.cdep.cdep.pod;


import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TestPlainOldDataEqualityCovisitor {
  @Test
  public void testInteger() {
    checkIdentical(1);
  }

  @Test
  public void testLong() {
    checkIdentical(1L);
  }

  @Test
  public void testLongLong() {
    checkNotIdentical(1L, 2L);
  }

  @Test
  public void testString() {
    checkIdentical("x");
  }

  @Test
  public void testStringString() {
    checkNotIdentical("x", "y");
  }

  @Test
  public void testLongArray() {
    checkIdentical(new Long[]{1L, 2L});
  }

  @Test
  public void testStringArray() {
    checkIdentical(new String[]{"x", "y"});
  }

  @Test
  public void testStringArrayStringArray() {
    checkNotIdentical(new String[]{"x", "y"}, new String[]{"x", "z"});
  }

  @Test
  public void testStringArrayStringArrayIdentical() {
    checkIdentical(new String[]{"x", "y"}, new String[]{"x", "y"});
  }

  private void checkIdentical(Object value) {
    assertThat(PlainOldDataEqualityCovisitor.areDeeplyIdentical(value, value)).isTrue();
  }

  private void checkIdentical(Object left, Object right) {
    assertThat(PlainOldDataEqualityCovisitor.areDeeplyIdentical(left, right)).isTrue();
  }

  private void checkNotIdentical(Object left, Object right) {
    assertThat(PlainOldDataEqualityCovisitor.areDeeplyIdentical(left, right)).isFalse();
  }
}
