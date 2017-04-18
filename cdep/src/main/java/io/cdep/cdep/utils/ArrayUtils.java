package io.cdep.cdep.utils;

import java.lang.reflect.Array;

public class ArrayUtils {
  /**
   * Return an empty array if the array is null.
   */
  public static <T> T[] emptyIfNull(T[] array, Class<T> clazz) {
    if (array == null) {
      //noinspection unchecked
      return (T[]) Array.newInstance(clazz, 0);
    }
    return array;
  }
}
