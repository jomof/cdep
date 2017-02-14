package io.cdep.service;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.model.Dependency;
import java.io.IOException;

abstract class Resolver {

    abstract ResolvedManifest resolve(
        GeneratorEnvironment environment,
        Dependency dependency,
        boolean forceRedownload) throws IOException;
}
