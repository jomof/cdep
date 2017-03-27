package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdep.BuildSystem;
import io.cdep.cdep.yml.cdep.CDepYml;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static io.cdep.cdep.utils.Invariant.fail;
import static io.cdep.cdep.utils.Invariant.require;

abstract public class CDepYmlUtils {
  public static void checkSanity(@org.jetbrains.annotations.NotNull @NotNull CDepYml cdepYml, File configFile) {
    Set<BuildSystem> builders = new HashSet<>();
    for (BuildSystem builder : cdepYml.builders) {
      require(!builders.contains(builder), "%s 'builders' contains '%s' more than once", configFile, builder);
      builders.add(builder);
    }

    if (cdepYml.builders.length == 0) {
      String allowed = StringUtils.joinOn(" ", BuildSystem.values());
      fail("%s 'builders' section is " + "missing or empty. Valid values are: %s.", configFile, allowed);
    }
  }

  @org.jetbrains.annotations.NotNull
  @NotNull
  public static CDepYml fromString(@org.jetbrains.annotations.NotNull @NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(CDepYml.class));
    CDepYml cdepYml = (CDepYml) yaml.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    require(cdepYml != null, "cdep.yml was empty");
    return cdepYml;
  }
}
