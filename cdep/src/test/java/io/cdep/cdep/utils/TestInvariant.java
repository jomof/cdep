package io.cdep.cdep.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.utils.Invariant.notNull;
import static io.cdep.cdep.utils.Invariant.require;

public class TestInvariant {
  @Test
  public void testRequireFalse() {
    try {
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
  public void testNotNullIsNull() {
    try {
      notNull(null);
      throw new RuntimeException("Expected an exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Invariant violation. Value was null.");
    }
  }

  @Test
  public void testNotNullNotNull() {
    notNull(new Object());
  }

  @Test
  public void testConstructor() {
    new Invariant() {
    };
  }
}
