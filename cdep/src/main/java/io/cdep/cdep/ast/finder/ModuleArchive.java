package io.cdep.cdep.ast.finder;

import java.net.URL;

public class ModuleArchive {
    final public URL file; // The zip file.
    final public String sha256;
    final public Long size;
    public ModuleArchive(URL file, String sha256, Long size) {
        assert file != null;
        assert sha256 != null;
        this.file = file;
        this.sha256 = sha256;
        this.size = size;
    }
}
