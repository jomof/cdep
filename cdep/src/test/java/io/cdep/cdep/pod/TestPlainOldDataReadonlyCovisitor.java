package io.cdep.cdep.pod;

import org.junit.Test;

public class TestPlainOldDataReadonlyCovisitor {

  @Test
  public void testInteger() {
    checkIdentical(1);
  }

  @Test
  public void testLong() {
    checkIdentical(1L);
  }

  @Test
  public void testString() {
    checkIdentical("x");
  }

  @Test
  public void testLongArray() {
    checkIdentical(new Long[]{1L, 2L});
  }

  @Test
  public void testStringArray() {
    checkIdentical(new String[]{"x", "y"});
  }

  private void checkIdentical(Object value) {
    new PlainOldDataReadonlyCovisitor().covisit(value, value);
  }
}
