package io.cdep.cdep.ast.finder;

import io.cdep.annotations.Nullable;

import java.net.URL;

import static io.cdep.cdep.utils.Invariant.notNull;

public class ModuleArchiveExpression extends Expression {
  @Nullable final public URL file; // The zip file.
  @Nullable final public String sha256;
  @Nullable final public Long size;
  @Nullable final public String include;
  @Nullable final public Expression includePath;
  @Nullable final public String library;
  @Nullable final public Expression libraryPath;
  @Nullable final public String requires[];

  ModuleArchiveExpression(
      URL file,
      String sha256,
      Long size,
      String include, // Like "include"
      Expression fullIncludePath,
      String library, // Like "lib/libsqlite.a"
      Expression fullLibraryName,
      String requires[]) {
    this.file = notNull(file);
    this.sha256 = sha256;
    this.size = size;
    this.include = include;
    this.includePath = fullIncludePath;
    this.library = library;
    this.libraryPath = fullLibraryName;
    this.requires = requires;
  }
}
