package io.cdep.cdep.generator;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.RewritingVisitor;
import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;

import java.util.ArrayList;
import java.util.List;

/**
 * Expand module requires into CMake target_compile_features
 */
public class ConvertRequiresToCMakeTargetCompileFeaturesRewritingVisitor extends RewritingVisitor {
  @Override
  protected Expression visitModuleExpression(@NotNull ModuleExpression expr) {
    ModuleArchiveExpression archive = expr.archive;
    CxxLanguageFeatures requires[] = archive.requires;
    if (requires == null ) {
      return super.visitModuleExpression(expr);
    }

    // Get rid of requires in ModuleArchiveExpression
    archive = new ModuleArchiveExpression(archive.file,
      archive.sha256,
      archive.size,
      archive.include,
      archive.includePath,
      archive.library,
      archive.libraryPath,
      null);

    List<StatementExpression> exprs = new ArrayList<>();
    exprs.add(CMakeInvokeMethod.targetCompileFeatures("${target}", CMakeAccessLevel.PRIVATE, requires));
    exprs.add(archive);

    return new MultiStatementExpression(exprs.toArray(new StatementExpression[exprs.size()]));
  }
}
