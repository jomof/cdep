package io.cdep.cdep.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.utils.Invariant.require;

public class TestInvariant {
  @Test
  public void testRequireFalse() {
    try {
      //noinspection ConstantConditions
      require(false);
      throw new RuntimeException("Expected an exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Invariant violation");
    }
  }

  @Test
  public void testRequireTrue() {
    require(true);
  }

  @Test
  public void testNotNullNotNull() {
    new Object();
  }

  @Test
  public void testConstructor() {
    new Invariant() {
    };
  }
}
