package io.cdep.cdep.generator;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.InterpretingVisitor;
import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;
import org.junit.Test;

import java.net.URL;
import java.util.HashSet;

import static com.google.common.truth.Truth.assertThat;

public class TestCxxLanguageStandardRewritingVisitor {

  private static ModuleArchiveExpression makeArchive(CxxLanguageFeatures... requires) throws Exception {
    return new ModuleArchiveExpression(
        new URL("http://google.com/cdep-manifest.yml"),
        "1234",
        2345L,
        null,
        null,
        null,
        null,
        requires);
  }

  @Test
  public void testSimple() throws Exception {
    GlobalBuildEnvironmentExpression globals = new GlobalBuildEnvironmentExpression();
    CxxLanguageStandardRewritingVisitor rewriter = new CxxLanguageStandardRewritingVisitor();
    rewriter.visit(globals);

    ModuleArchiveExpression archive = makeArchive(CxxLanguageFeatures.cxx_alignof);
    ModuleExpression module = new ModuleExpression(archive, new HashSet<Coordinate>());
    Expression result = ((MultiStatementExpression) rewriter.visitModuleExpression(module)).statements[0];

    InterpretingVisitor interpreter = new InterpretingVisitor();
    Object callvalue = interpreter.visit(result);
    assertThat(callvalue.getClass()).isEqualTo(CxxLanguageFeatures[].class);
  }

  @Test
  public void testSimpleStandard17() throws Exception {
    GlobalBuildEnvironmentExpression globals = new GlobalBuildEnvironmentExpression();
    CxxLanguageStandardRewritingVisitor rewriter = new CxxLanguageStandardRewritingVisitor();
    rewriter.visit(globals);

    ModuleArchiveExpression archive = makeArchive(CxxLanguageFeatures.cxx_std_17);
    ModuleExpression module = new ModuleExpression(archive, new HashSet<Coordinate>());
    Expression result = ((MultiStatementExpression) rewriter.visitModuleExpression(module)).statements[0];

    InterpretingVisitor interpreter = new InterpretingVisitor();
    Object callvalue = interpreter.visit(result);
    assertThat(callvalue.getClass()).isEqualTo(CxxLanguageFeatures[].class);
  }

}