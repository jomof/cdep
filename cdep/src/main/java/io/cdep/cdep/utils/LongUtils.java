package io.cdep.cdep.utils;

import io.cdep.annotations.Nullable;

public class LongUtils {
  public static long nullToZero(@Nullable Long value) {
    if (value == null) {
      return 0L;
    }
    return value;
  }
}
