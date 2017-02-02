package com.jomofisher.cdep.AST;

import java.io.File;

public class FoundModuleExpression extends Expression {

    final public File archive; // The zip file.

    public FoundModuleExpression(File remote) {
        this.archive = remote;

    }

}
