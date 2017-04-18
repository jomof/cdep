package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

/**
 * Methods for dealing with Objects.
 */
public class ObjectUtils {
  @NotNull
  public static <T> T nullToDefault(@Nullable T value, @NotNull T $default) {
    if (value == null) {
      return $default;
    }
    return value;
  }
}
