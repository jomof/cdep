package io.cdep.cdep.ast.finder;

import io.cdep.cdep.yml.cdepmanifest.iOSPlatform;

public class iOSPlatformExpression extends Expression {
    final public iOSPlatform platform;

    public iOSPlatformExpression(iOSPlatform platform) {
        this.platform = platform;
    }

    @Override
    public int hashCode() {
        return platform.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof iOSPlatformExpression)) {
            return false;
        }
        return platform.equals(((iOSPlatformExpression) obj).platform);
    }

    @Override
    public String toString() {
        return platform.toString();
    }
}
