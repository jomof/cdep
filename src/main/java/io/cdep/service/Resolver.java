package io.cdep.service;

import io.cdep.AST.service.ResolvedManifest;
import io.cdep.model.Reference;
import java.io.IOException;

abstract class Resolver {

  abstract ResolvedManifest resolve(GeneratorEnvironment environment,
      Reference reference, boolean forceRedownload) throws IOException;
}
