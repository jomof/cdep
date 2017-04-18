package io.cdep.cdep;

import io.cdep.cdep.ast.finder.Expression;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.archive;
import static org.junit.Assert.fail;

public class TestRewritingVisitor {
  @Test
  public void testNullInclude() throws Exception {
    new RewritingVisitor().visit(archive(new URL("https://google.com"), "sha256", 192L, null,
        null, new String[0], new Expression[0], new CxxLanguageFeatures[0]));
  }

  @Test
  public void testAllResolvedManifests() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("admob", "Reference com.github.jomof:firebase/app:2.1.3-rev8 was not found");
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      BuildFindModuleFunctionTable builder = new BuildFindModuleFunctionTable();
      builder.addManifest(manifest.resolved);
      String expectedFailure = expected.get(manifest.name);
      try {
        new RewritingVisitor().visit(builder.build());
        if (expectedFailure != null) {
          fail("Expected failure");
        }
      } catch (RuntimeException e) {
        if (expectedFailure == null) {
          throw e;
        }
        assertThat(e.getMessage()).contains(expectedFailure);
      }
    }
  }
}
