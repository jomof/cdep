package io.cdep.cdep;

import io.cdep.cdep.ast.finder.ModuleArchiveExpression;

import java.io.File;

public class StubCheckLocalFileSystemIntegrityVisitor extends CheckLocalFileSystemIntegrityVisitor {
  public StubCheckLocalFileSystemIntegrityVisitor(File explodedRoot) {
    super(explodedRoot);
  }

  @Override
  protected ModuleArchive visitModuleArchiveExpression(ModuleArchiveExpression expr) {
    return super.superVisitModuleArchiveExpression(expr);
  }
}
