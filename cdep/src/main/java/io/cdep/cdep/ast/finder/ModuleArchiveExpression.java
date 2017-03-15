package io.cdep.cdep.ast.finder;

import java.net.URL;

public class ModuleArchiveExpression extends Expression {
  final public URL file; // The zip file.
  final public String sha256;
  final public Long size;
  final public Expression fullIncludePath;
  final public Expression fullLibraryName;

  ModuleArchiveExpression(
      URL file,
      String sha256,
      Long size,
      Expression fullIncludePath,
      Expression fullLibraryName) {
    assert file != null;
    assert sha256 != null;
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.fullIncludePath = fullIncludePath;
    this.fullLibraryName = fullLibraryName;
  }
}
