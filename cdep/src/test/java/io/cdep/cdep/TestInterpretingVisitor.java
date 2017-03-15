package io.cdep.cdep;

import org.junit.Test;

import java.net.URL;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.archive;

public class TestInterpretingVisitor {
  @Test
  public void testNullInclude() throws Exception {
    new InterpretingVisitor().visit(
        archive(
            new URL("https://google.com"),
            "sha256",
            192L,
            null,
            null));
  }
}
