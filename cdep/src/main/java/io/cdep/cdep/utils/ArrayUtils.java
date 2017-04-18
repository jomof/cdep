package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import java.lang.reflect.Array;

public class ArrayUtils {
  /**
   * Return an empty array if the array is null.
   */
  @NotNull
  public static <T> T[] nullToEmpty(@Nullable T[] array, Class<T> clazz) {
    if (array == null) {
      //noinspection unchecked
      return (T[]) Array.newInstance(clazz, 0);
    }
    return array;
  }
}
