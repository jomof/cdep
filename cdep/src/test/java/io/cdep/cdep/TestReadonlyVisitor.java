package io.cdep.cdep;

import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.archive;
import static org.junit.Assert.fail;

public class TestReadonlyVisitor {
  @Test
  public void testNullInclude() throws Exception {
    new ReadonlyVisitor().visit(archive(new URL("https://google.com"), "sha256", 192L, null, null, null, null));
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
        new ReadonlyVisitor().visit(builder.build());
        if (expectedFailure != null) {
          fail("Expected failure");
        }
      } catch (RuntimeException e) {
        if (expectedFailure == null || !e.getClass().equals(RuntimeException.class)) {
          throw e;
        }
        assertThat(e.getMessage()).contains(expectedFailure);
      }
    }
  }
}
