package io.cdep.cdep.utils;

import java.util.Collection;

/**
 * Methods for ensuring state at runtime
 */
abstract public class Invariant {

  public static void fail(String format, Object... parameters) {
    throw new RuntimeException(String.format(format, parameters));
  }

  public static void require(boolean check, String format, Object... parameters) {
    if (check) {
      return;
    }
    throw new RuntimeException(String.format(format, parameters));
  }

  public static void require(boolean check) {
    if (!check) {
      throw new RuntimeException("Invariant violation");
    }
  }

  public static <T> T notNull(T obj) {
    if (obj == null) {
      throw new RuntimeException("Invariant violation. Value was null.");
    }
    return obj;
  }

  public static <T> T[] elementsNotNull(T[] array) {
    notNull(array);
    for (T t : array) {
      notNull(t);
    }
    return array;
  }

  public static <T extends Collection> T elementsNotNull(T collection) {
    notNull(collection);
    for (Object t : collection) {
      notNull(t);
    }
    return collection;
  }
}
