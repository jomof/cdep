package io.cdep.cdep.fullfill;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlRewritingVisitor;

import java.util.HashMap;
import java.util.Map;

public class SubstituteStringsRewritingVisitor extends CDepManifestYmlRewritingVisitor {
  final private Map<String, String> variables = new HashMap<>();

  @NotNull
  SubstituteStringsRewritingVisitor replace(String key, String value) {
    variables.put(key, value);
    return this;
  }

  @Nullable
  @Override
  protected String visitString(@Nullable String value) {
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
