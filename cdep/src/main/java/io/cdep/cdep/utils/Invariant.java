package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.cdep.cdep.io.IO.infoln;

/**
 * Methods for ensuring state at runtime
 */
abstract public class Invariant {
  private static final List<List<RuntimeException>> requirementFailures = new ArrayList<>();

  @SuppressWarnings("Convert2Diamond")
  public static void pushScope() {
    requirementFailures.add(new ArrayList<RuntimeException>());
  }

  public static List<RuntimeException> popScope() {
    List<RuntimeException> errors = requirementFailures.get(0);
    requirementFailures.remove(0);
    return errors;
  }

  private static void report(@NotNull RuntimeException e) {
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
}
