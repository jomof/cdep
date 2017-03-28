package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.ast.finder.*;

import java.io.File;

/**
 * Locates every referenced local file and ensures that those files are present in the right
 * place on the local file system.
 */
public class CheckLocalFileSystemIntegrity extends InterpretingVisitor {

  final private File explodedRoot;

  public CheckLocalFileSystemIntegrity(File explodedRoot) {
    this.explodedRoot = explodedRoot;
  }

  @NotNull
  protected ModuleArchive superVisitModuleArchiveExpression(@NotNull ModuleArchiveExpression expr) {
    return super.visitModuleArchiveExpression(expr);
  }


  @NotNull
  @Override
  protected ModuleArchive visitModuleArchiveExpression(@NotNull ModuleArchiveExpression expr) {
    ModuleArchive archive = superVisitModuleArchiveExpression(expr);
    if (archive.fullIncludePath != null) {
      if (!archive.fullIncludePath.getParentFile().isDirectory()) {
        throw new RuntimeException(String.format("Expected '%s' folder to be created but it wasn't.", archive.fullIncludePath.getParentFile()));
      }
      if (!archive.fullIncludePath.isDirectory()) {
        throw new RuntimeException(String.format("Downloaded '%s' did not contain include folder '%s' at it's root.\nLocal path: %s\n" + "If you own this package you can add \"include:\" to the archive entry in cdep-manifest.yml" + " to indicate that there is no include folder.", archive.remote, archive.fullIncludePath.getName(), archive.fullIncludePath));
      }
    }
    if (archive.fullLibraryName != null) {
      if (!archive.fullLibraryName.getParentFile().isDirectory()) {
        throw new RuntimeException(String.format("Expected '%s' folder to be created but it wasn't.", archive.fullLibraryName.getParentFile()));
      }
      if (!archive.fullLibraryName.isFile()) {
        throw new RuntimeException(String.format("Downloaded '%s' did not contain library '%s/%s' at it's root.\nLocal path: %s", archive.remote, archive.fullLibraryName.getParentFile().getName(), archive.fullLibraryName.getName(), archive.fullLibraryName));
      }
    }
    return archive;
  }

  @Override
  protected Object visitParameterExpression(@NotNull ParameterExpression expr) {
    if (expr.name.equals("cdep_exploded_root")) {
      return explodedRoot;
    }
    return super.visitParameterExpression(expr);
  }

  @Nullable
  @Override
  protected Object visitFindModuleExpression(@NotNull FindModuleExpression expr) {
    // Don't visit parameters because we don't need them and don't want to have to bind them
    return visit(expr.expression);
  }

  @Nullable
  protected Object visitIfSwitchExpression(@NotNull IfSwitchExpression expr) {
    for (int i = 0; i < expr.conditions.length; ++i) {
      // Don't visit the condition. Instead, travel down all paths.
      visit(expr.expressions[i]);
    }
    return visit(expr.elseExpression);
  }

  @Nullable
  @Override
  protected Object visitAbortExpression(@NotNull AbortExpression expr) {
    return null;
  }
}
