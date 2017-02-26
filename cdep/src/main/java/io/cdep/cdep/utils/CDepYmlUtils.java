package io.cdep.cdep.utils;

import io.cdep.cdep.yml.cdep.BuildSystem;
import io.cdep.cdep.yml.cdep.CDepYml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CDepYmlUtils {
    public static void checkSanity(CDepYml cdepYml, File configFile) {
        Set<BuildSystem> builders = new HashSet<>();
        for (BuildSystem builder : cdepYml.builders) {
            if (builders.contains(builder)) {
                throw new RuntimeException(String.format("cdep.yml builders contains '%s' more than once", builder));
            }
        }

        if (cdepYml.builders.length == 0) {
            StringBuilder sb = new StringBuilder();
            for (BuildSystem builder : BuildSystem.values()) {
                sb.append(builder.toString());
                sb.append(" ");
            }
            throw new RuntimeException(String.format("Error in '%s'. The 'builders' section is "
                + "missing or empty. Valid values are: %s.", configFile, sb));
        }
    }
}
