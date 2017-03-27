package io.cdep.cdep.ast.finder;

import org.jetbrains.annotations.Nullable;

import java.net.URL;

import static io.cdep.cdep.utils.Invariant.notNull;

public class ModuleArchiveExpression extends Expression {
  @Nullable
  final public URL file; // The zip file.
  @Nullable
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
    this.file = notNull(file);
    this.sha256 = notNull(sha256);
    this.size = size;
    this.include = include;
    this.includePath = fullIncludePath;
    this.library = library;
    this.libraryPath = fullLibraryName;
  }
}
