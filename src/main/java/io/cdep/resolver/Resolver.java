package io.cdep.resolver;

import io.cdep.ast.service.ResolvedManifest;
import io.cdep.generator.GeneratorEnvironment;
import io.cdep.yml.cdep.Dependency;
import java.io.IOException;

public abstract class Resolver {

    public abstract ResolvedManifest resolve(
        GeneratorEnvironment environment,
        Dependency dependency,
        boolean forceRedownload) throws IOException;
}
