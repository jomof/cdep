package io.cdep.cdep.generator;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.RewritingVisitor;
import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;

import java.util.ArrayList;
import java.util.List;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.*;

/**
 * Expand module requires into logic for setting the compiler standard that should be used.
 */
public class CxxLanguageStandardRewritingVisitor extends RewritingVisitor {
  private GlobalBuildEnvironmentExpression globals;

  @Override
  protected Expression visitGlobalBuildEnvironmentExpression(GlobalBuildEnvironmentExpression expr) {
    this.globals = expr;
    return super.visitGlobalBuildEnvironmentExpression(expr);
  }

  @Override
  protected Expression visitModuleExpression(@NotNull ModuleExpression expr) {
    ModuleArchiveExpression archive = expr.archive;
    CxxLanguageFeatures requires[] = archive.requires;
    if (requires == null || requires.length == 0) {
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

    // Make constant expression of requires
    List<ConstantExpression> features = new ArrayList<>();
    for (CxxLanguageFeatures require : requires) {
      features.add(constant(require));
    }

    // Find the minimum language standard to support all the requirements
    int minimumLanguageStandard = 0;
    for (CxxLanguageFeatures require : requires) {
      minimumLanguageStandard = Math.max(
          minimumLanguageStandard,
          require.standard);
    }

    List<StatementExpression> exprs = new ArrayList<>();
    InvokeFunctionExpression invokeRequires = invoke(
        ExternalFunctionExpression.REQUIRES_COMPILER_FEATURES,
        array(features.toArray(new ConstantExpression[requires.length])));
    if (minimumLanguageStandard == 0) {
      // No point in setting the default.
      exprs.add(invokeRequires);
    } else {
      /*
        Construct an if statement that looks like this:

          if (compiler supports feature requirements)
            requireCompilerFeatures(a, b, c)
          else if (
            not defined current compiler standard
            or current compiler standard < required standard)
            setGlobalCompilerStandard(required standard)
          else
            nop

       */

      exprs.add(
          ifSwitch(
              new Expression[]{
                  // If build system supports compiler feature requirements
                  invoke(ExternalFunctionExpression.SUPPORTS_REQUIRES_COMPILER_FEATURES),
                  // If the current global compiler standard is less than required
                   or(not(defined(globals.buildSystemCxxCompilerStandard)),
                       lt(globals.buildSystemCxxCompilerStandard, minimumLanguageStandard))},

              new Expression[]{
                  // The compiler supports compiler features so request the features from the manifest
                  invoke(
                      ExternalFunctionExpression.REQUIRES_COMPILER_FEATURES,
                      array(features.toArray(new ConstantExpression[requires.length]))),
                  new ParameterAssignmentExpression(
                      globals.buildSystemCxxCompilerStandard, constant(minimumLanguageStandard))},
              nop()));
    }

    exprs.add(archive);

    return new MultiStatementExpression(exprs.toArray(new StatementExpression[exprs.size()]));
  }
}
