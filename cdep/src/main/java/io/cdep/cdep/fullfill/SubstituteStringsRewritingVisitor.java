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

  @Override
  protected String visitString(String value) {
    if (value == null) {
      return null;
    }
    String result = value;
    for (String key : variables.keySet()) {
      result = result.replace(key, variables.get(key));
    }
    return result;
  }
}
