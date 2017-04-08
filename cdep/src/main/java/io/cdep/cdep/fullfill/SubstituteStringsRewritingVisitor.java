package io.cdep.cdep.fullfill;

import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;

import java.util.HashMap;
import java.util.Map;

public class SubstituteStringsRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  final private Map<String, String> variables = new HashMap<>();

  SubstituteStringsRewritingVisitor replace(String key, String value) {
    variables.put(key, value);
    return this;
  }

}
