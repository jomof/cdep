package io.cdep;

import io.cdep.manifest.Manifest;

import java.net.URL;

public class ResolvedManifest {

    final public URL remote;
    final public Manifest manifest;

    ResolvedManifest(URL remote, Manifest manifest) {
        this.remote = remote;
        this.manifest = manifest;
    }
}
