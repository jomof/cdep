package io.cdep.cdep;

import io.cdep.cdep.ast.finder.AbortExpression;
import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.IfSwitchExpression;
import io.cdep.cdep.ast.finder.ModuleArchiveExpression;
import io.cdep.cdep.ast.finder.ParameterExpression;
import java.io.File;

/**
 * Locates every referenced local file and ensures that those files are present in the right
 * place on the local file system.
 */
public class CheckLocalFileSystemIntegrityVisitor extends InterpretingVisitor {

  final private File explodedRoot;

  public CheckLocalFileSystemIntegrityVisitor(File explodedRoot) {
    this.explodedRoot = explodedRoot;
  }

  @Override
  protected ModuleArchive visitModuleArchiveExpression(ModuleArchiveExpression expr) {
    ModuleArchive archive = super.visitModuleArchiveExpression(expr);
    if (!archive.fullIncludePath.getParentFile().isDirectory()) {
      throw new RuntimeException(
          String.format("Expected '%s' folder to be created but it wasn't.",
              archive.fullIncludePath.getParentFile()));
    }
    if (!archive.fullIncludePath.isDirectory()) {
      throw new RuntimeException(
          String.format(
              "Downloaded '%s' did not contain include folder '%s' at it's root.\nLocal path: %s",
              archive.remote,
              archive.fullIncludePath.getName(),
              archive.fullIncludePath));
    }
    return archive;
  }

  @Override
  protected Object visitParameterExpression(ParameterExpression expr) {
    if (expr.name.equals("cdep_exploded_root")) {
      return explodedRoot;
    }
    return super.visitParameterExpression(expr);
  }

  @Override
  protected Object visitFindModuleExpression(FindModuleExpression expr) {
    // Don't visit parameters because we don't need them and don't want to have to bind them
    return visit(expr.expression);
  }

  protected Object visitIfSwitchExpression(IfSwitchExpression expr) {
    for (int i = 0; i < expr.conditions.length; ++i) {
      // Don't visit the condition. Instead, travel down all paths.
      visit(expr.expressions[i]);
    }
    return visit(expr.elseExpression);
  }

  @Override
  protected Object visitAbortExpression(AbortExpression expr) {
    return null;
  }
}
