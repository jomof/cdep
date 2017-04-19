package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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

  @NotNull
  public static <T> T[] removeNullElements(@NotNull T[] array, Class<T> clazz) {
    List<T> list = new ArrayList<T>();
    for (int i = 0; i < array.length; ++i) {
      if (array[i] == null) {
        continue;
      }
      list.add(array[i]);
    }
    return list.toArray((T[])Array.newInstance(clazz, list.size()));
  }
}
