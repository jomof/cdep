package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.ast.finder.ModuleArchiveExpression;

import java.io.File;

public class StubCheckLocalFileSystemIntegrity extends CheckLocalFileSystemIntegrity {
  public StubCheckLocalFileSystemIntegrity(File explodedRoot) {
    super(explodedRoot);
  }

  @Nullable
  @Override
  protected ModuleArchive visitModuleArchiveExpression(@org.jetbrains.annotations.NotNull @NotNull ModuleArchiveExpression expr) {
    return super.superVisitModuleArchiveExpression(expr);
  }
}
