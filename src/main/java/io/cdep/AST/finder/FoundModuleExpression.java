package io.cdep.AST.finder;

import io.cdep.manifest.Coordinate;
import java.net.URL;

public class FoundModuleExpression extends Expression {

    final public Coordinate coordinate; // Coordinate of the module.
    final public URL archive; // The zip file.
    final public String archiveSHA256;
    final public String include; // The relative path of include files under the zip
    final public String libraryName; // The library name

    public FoundModuleExpression(
            Coordinate coordinate,
        URL archive,
        String archiveSHA256,
            String include,
            String libraryName) {
        this.coordinate = coordinate;
        this.archive = archive;
        this.archiveSHA256 = archiveSHA256;
        this.include = include;
        this.libraryName = libraryName;
    }
}
