package io.cdep.cdep.utils;

import io.cdep.cdep.yml.cdepsha25.CDepSHA256;
import io.cdep.cdep.yml.cdepsha25.HashEntry;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class CDepSHA256Utils {
    public static CDepSHA256 convertStringToCDepSHA256(String content) {
        Yaml yaml = new Yaml(new Constructor(HashEntry[].class));
        HashEntry[] result =
                (HashEntry[]) yaml.load(new ByteArrayInputStream(content.getBytes(
                        StandardCharsets.UTF_8)));
        assert result != null;
        return new CDepSHA256(result);
    }
}
