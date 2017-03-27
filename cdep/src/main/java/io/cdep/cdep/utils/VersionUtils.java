package io.cdep.cdep.utils;

import org.jetbrains.annotations.NotNull;

public class VersionUtils {
  static String checkVersion(@NotNull String version) {
        String[] pointSections = version.split("\\.");
        String EXPECTED = "major.minor.point[-tweak]";
        if (pointSections.length == 1) {
            return String.format("expected %s but there were no dots", EXPECTED);
        }
        if (pointSections.length == 2) {
            return String.format("expected %s but there was only one dot", EXPECTED);
        }
        if (pointSections.length > 3) {
            return String.format("expected %s but there were %s dots", EXPECTED, pointSections.length - 1);
        }
        if (!StringUtils.isNumeric(pointSections[0])) {
            return String.format("expected %s but major version '%s' wasn't a number", EXPECTED, pointSections[0]);
        }
        if (!StringUtils.isNumeric(pointSections[1])) {
            return String.format("expected %s but minor version '%s' wasn't a number", EXPECTED, pointSections[1]);
        }
        if (pointSections[2].contains("-")) {
            int dashPosition = pointSections[2].indexOf('-');
            String pointVersion = pointSections[2].substring(0, dashPosition);
            if (!StringUtils.isNumeric(pointVersion)) {
                return String.format("expected %s but point version '%s' wasn't a number", EXPECTED, pointVersion);
            }
        } else {
            if (!StringUtils.isNumeric(pointSections[2])) {
                return String.format("expected %s but point version '%s' wasn't a number", EXPECTED, pointSections[2]);
            }
        }
        return null;
    }
}
