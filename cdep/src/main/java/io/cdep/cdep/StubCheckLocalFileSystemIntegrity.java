package io.cdep.cdep;

import io.cdep.cdep.ast.finder.ModuleArchiveExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class StubCheckLocalFileSystemIntegrity extends CheckLocalFileSystemIntegrity {
  public StubCheckLocalFileSystemIntegrity(File explodedRoot) {
    super(explodedRoot);
  }

  @Nullable
  @Override
  protected ModuleArchive visitModuleArchiveExpression(@NotNull ModuleArchiveExpression expr) {
    return super.superVisitModuleArchiveExpression(expr);
  }
}
