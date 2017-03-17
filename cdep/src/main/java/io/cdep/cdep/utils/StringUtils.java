package io.cdep.cdep.utils;

public class StringUtils {

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String joinOn(String delimiter, String array[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            if (i != 0) {
                sb.append(delimiter);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }
}
