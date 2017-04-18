package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import java.util.Collection;

public class StringUtils {

  public static boolean isNumeric(@NotNull String str) {
    for (char c : str.toCharArray()) {
      if (!Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  }

  public static String joinOn(String delimiter, @NotNull Object array[]) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < array.length; ++i) {
      if (i != 0) {
        sb.append(delimiter);
      }
      sb.append(array[i]);
    }
    return sb.toString();
  }

  public static String joinOn(String delimiter, @NotNull Collection<String> strings) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (String string : strings) {
      if (i != 0) {
        sb.append(delimiter);
      }
      sb.append(string);
      ++i;
    }
    return sb.toString();
  }

  public static String joinOn(String delimiter, @NotNull String ... strings) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (String string : strings) {
      if (i != 0) {
        sb.append(delimiter);
      }
      sb.append(string);
      ++i;
    }
    return sb.toString();
  }

  public static String joinOnSkipNullOrEmpty(String delimiter, @NotNull String ... strings) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (String string : strings) {
      if (string == null || string.isEmpty()) {
        continue;
      }
      if (i != 0) {
        sb.append(delimiter);
      }
      sb.append(string);
      ++i;
    }
    return sb.toString();
  }

  @NotNull
  public static String nullToEmpty(@Nullable String value) {
    if (value == null) {
      return "";
    }
    return value;
  }

  @NotNull
  public static String[] singletonArrayOrEmpty(@Nullable String value) {
    if (value == null) {
      return new String[0];
    }
    return new String[] {value};
  }
}
