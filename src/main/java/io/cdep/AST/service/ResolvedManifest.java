package io.cdep.AST.service;

import io.cdep.manifest.Manifest;
import java.net.URL;

public class ResolvedManifest {

    final public URL remote;
    final public Manifest manifest;

    public ResolvedManifest(URL remote, Manifest manifest) {
        this.remote = remote;
        this.manifest = manifest;
    }
}
