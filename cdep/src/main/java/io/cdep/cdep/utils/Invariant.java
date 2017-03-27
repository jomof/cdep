package io.cdep.cdep.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Methods for ensuring state at runtime
 */
abstract public class Invariant {

  public static void fail(@NotNull String format, Object... parameters) {
    throw new RuntimeException(String.format(format, parameters));
  }

  public static void require(boolean check, @NotNull String format, Object... parameters) {
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

  @NotNull
  public static <T> T notNull(@Nullable T obj) {
    if (obj == null) {
      throw new RuntimeException("Invariant violation. Value was null.");
    }
    return obj;
  }

  @NotNull
  public static <T> T[] elementsNotNull(@NotNull T[] array) {
    notNull(array);
    for (T t : array) {
      notNull(t);
    }
    return array;
  }

  @NotNull
  public static <T extends Collection> T elementsNotNull(@NotNull T collection) {
    notNull(collection);
    for (Object t : collection) {
      notNull(t);
    }
    return collection;
  }
}
