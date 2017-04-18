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

  @Override
  protected Expression visitGlobalBuildEnvironmentExpression(GlobalBuildEnvironmentExpression expr) {
    return super.visitGlobalBuildEnvironmentExpression(expr);
  }

  @Override
  protected Expression visitModuleExpression(@NotNull ModuleExpression expr) {
    ModuleArchiveExpression archive = expr.archive;
    CxxLanguageFeatures requires[] = archive.requires;
    if (requires.length == 0) {
      return super.visitModuleExpression(expr);
    }

    // Get rid of requires in ModuleArchiveExpression
    archive = new ModuleArchiveExpression(archive.file,
        archive.sha256,
        archive.size,
        archive.include,
        archive.includePath,
        archive.libs,
        archive.libraryPaths,
        new CxxLanguageFeatures[0]);

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
    exprs.add(
        ifSwitch(
            // If build system (CMake or ndk-build) supports compiler feature requirements
            supportsCompilerFeatures(),
            // The build system supports compiler features so request the features from the manifest
            requiresCompilerFeatures(array(features.toArray(new ConstantExpression[requires.length]))),
            // Otherwise, require the lowest compiler standard that supports the listed features
            requireMinimumCxxCompilerStandard(constant(minimumLanguageStandard))));
    exprs.add(archive);

    return new MultiStatementExpression(exprs.toArray(new StatementExpression[exprs.size()]));
  }
}
