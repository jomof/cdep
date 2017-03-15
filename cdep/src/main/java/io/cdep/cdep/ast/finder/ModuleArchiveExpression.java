package io.cdep.cdep.ast.finder;

import java.net.URL;

public class ModuleArchiveExpression extends Expression {
  final public URL file; // The zip file.
  final public String sha256;
  final public Long size;
  final public String include;
  final public Expression includePath;
  final public String library;
  final public Expression libraryPath;

  ModuleArchiveExpression(
      URL file,
      String sha256,
      Long size,
      String include, // Like "include"
      Expression fullIncludePath,
      String library, // Like "lib/libsqlite.a"
      Expression fullLibraryName) {
    assert file != null;
    assert sha256 != null;
    this.file = file;
    this.sha256 = sha256;
    this.size = size;
    this.include = include;
    this.includePath = fullIncludePath;
    this.library = library;
    this.libraryPath = fullLibraryName;
  }
}
