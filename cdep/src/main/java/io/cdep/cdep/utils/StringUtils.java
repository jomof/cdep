package io.cdep.cdep.utils;

import org.jetbrains.annotations.NotNull;

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
}
