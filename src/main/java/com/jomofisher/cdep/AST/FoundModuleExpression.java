package com.jomofisher.cdep.AST;

import java.net.URL;

public class FoundModuleExpression extends Expression {

    final public URL archive; // The zip file.
    final public String include; // The relative path of include files under the zip
    final public String lib; // The relative path of lib files under the zip

    public FoundModuleExpression(URL remote, String include, String lib) {
        this.archive = remote;
        this.include = include;
        this.lib = lib;
    }
}
