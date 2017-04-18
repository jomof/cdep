package io.cdep.cdep.ast.finder;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.yml.cdepmanifest.CxxLanguageFeatures;

import java.net.URL;

import static io.cdep.cdep.utils.Invariant.notNull;
import static io.cdep.cdep.utils.Invariant.require;

public class ModuleArchiveExpression extends StatementExpression {
  @Nullable final public URL file; // The zip file.
  @Nullable final public String sha256;
  @Nullable final public Long size;
  @Nullable final public String include;
  @Nullable final public Expression includePath;
  @NotNull final public String libs[];
  @NotNull final public Expression libraryPaths[];
  @Nullable final public CxxLanguageFeatures requires[];

  public ModuleArchiveExpression(
      URL file,
      String sha256,
      Long size,
      String include, // Like "include"
      Expression fullIncludePath,
      String libs[], // Like "lib/libsqlite.a"
      Expression libraryPaths[],
      CxxLanguageFeatures requires[]) {
    notNull(libs);
    notNull(libraryPaths);
    notNull(requires);
    require(libs.length == libraryPaths.length);
    this.file = notNull(file);
    this.sha256 = sha256;
    this.size = size;
    this.include = include;
    this.includePath = fullIncludePath;
    this.libs = libs;
    this.libraryPaths = libraryPaths;
    this.requires = requires;
  }
}
