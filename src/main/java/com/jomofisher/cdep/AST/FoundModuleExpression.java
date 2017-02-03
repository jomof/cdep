package com.jomofisher.cdep.AST;

import com.jomofisher.cdep.manifest.Coordinate;
import java.net.URL;

public class FoundModuleExpression extends Expression {

    final public Coordinate coordinate; // Coordinate of the module.
    final public URL archive; // The zip file.
    final public String include; // The relative path of include files under the zip
    final public String lib; // The relative path of lib files under the zip

    public FoundModuleExpression(Coordinate coordinate, URL remote, String include, String lib) {
        this.coordinate = coordinate;
        this.archive = remote;
        this.include = include;
        this.lib = lib;
    }
}
