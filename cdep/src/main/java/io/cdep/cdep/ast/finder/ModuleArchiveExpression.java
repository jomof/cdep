package io.cdep.cdep.ast.finder;

import java.net.URL;

public class ModuleArchiveExpression extends Expression {
    final public URL file; // The zip file.
    final public String sha256;
    final public Long size;
    final public String libraryName;
    final public Expression fullIncludePath;

  ModuleArchiveExpression(
            URL file,
            String sha256,
            Long size,
            Expression fullIncludePath,
            String libraryName) {
        assert file != null;
        assert sha256 != null;
        this.file = file;
        this.sha256 = sha256;
        this.size = size;
        this.fullIncludePath = fullIncludePath;
        this.libraryName = libraryName;
    }
}
