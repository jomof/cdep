package com.jomofisher.cdep.AST;

import java.net.URL;

public class FoundModuleExpression extends Expression {

    final public URL archive; // The zip file.

    public FoundModuleExpression(URL remote) {
        this.archive = remote;

    }

}
