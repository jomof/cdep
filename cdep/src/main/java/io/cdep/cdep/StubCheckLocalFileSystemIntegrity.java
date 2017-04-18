package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.ast.finder.ModuleArchiveExpression;

import java.io.File;

@SuppressWarnings("unused")
public class StubCheckLocalFileSystemIntegrity extends CheckLocalFileSystemIntegrity {
  public StubCheckLocalFileSystemIntegrity(File explodedRoot) {
    super(explodedRoot);
  }

  @NotNull
  @Override
  protected ModuleArchive visitModuleArchiveExpression(@NotNull ModuleArchiveExpression expr) {
    return super.superVisitModuleArchiveExpression(expr);
  }
}
