package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.cdep.cdep.io.IO.infoln;

/**
 * Methods for ensuring state at runtime
 */
abstract public class Invariant {
  private static List<List<RuntimeException>> requirementFailures = new ArrayList<>();

  public static void pushScope() {
    requirementFailures.add(new ArrayList<RuntimeException>());
  }

  public static List<RuntimeException> popScope() {
    List<RuntimeException> errors = requirementFailures.get(0);
    requirementFailures.remove(0);
    return errors;
  }

  private static void report(RuntimeException e) {
    if (requirementFailures.size() == 0) {
      throw e;
    }
    infoln("  FAILURE: %s", e.getMessage());
    requirementFailures.get(0).add(e);
  }

  public static void fail(@NotNull String format, Object... parameters) {
    report(new RuntimeException(String.format(format, parameters)));
  }

  public static void require(boolean check, @NotNull String format, Object... parameters) {
    if (check) {
      return;
    }
    report(new RuntimeException(String.format(format, parameters)));
  }

  public static void require(boolean check) {
    if (!check) {
      report(new RuntimeException("Invariant violation"));
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
