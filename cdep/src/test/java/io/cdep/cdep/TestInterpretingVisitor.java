package io.cdep.cdep;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.archive;

import java.net.URL;
import org.junit.Test;

public class TestInterpretingVisitor {
  @Test
  public void testNullInclude() throws Exception {
    new InterpretingVisitor().visit(
        archive(
            new URL("https://google.com"),
            "sha256",
            192L,
            null,
            null,
            null,
            null));
  }
}
