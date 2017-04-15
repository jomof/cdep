package io.cdep.cdep.generator;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.RewritingVisitor;
import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;

import java.util.ArrayList;
import java.util.List;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.array;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.constant;
import static io.cdep.cdep.ast.finder.ExpressionBuilder.invoke;

/**
 * Expand module requires into an external function.
 */
public class ListRequiresToStatementExpression extends RewritingVisitor {
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
    List<ConstantExpression> features = new ArrayList<>();
    for (int i = 0; i < requires.length; ++i) {
      features.add(constant(requires[i]));
    }

    List<StatementExpression> exprs = new ArrayList<>();
    exprs.add(invoke(
        ExternalFunctionExpression.REQUIRES_COMPILER_FEATURES,
        array(features.toArray(new ConstantExpression[requires.length]))));
    exprs.add(archive);

    return new MultiStatementExpression(exprs.toArray(new StatementExpression[exprs.size()]));
  }
}
