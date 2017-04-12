package io.cdep.cdep.utils;

import ext.org.yaml.snakeyaml.Yaml;
import ext.org.yaml.snakeyaml.constructor.Constructor;
import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdepsha25.CDepSHA256;
import io.cdep.cdep.yml.cdepsha25.HashEntry;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class CDepSHA256Utils {

  @NotNull
  public static CDepSHA256 convertStringToCDepSHA256(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(HashEntry[].class));
    HashEntry[] result = (HashEntry[]) yaml.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    if (result == null) {
      result = new HashEntry[0];
    }
    return new CDepSHA256(result);
  }
}
