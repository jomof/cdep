package com.jomofisher.cdep;

import com.jomofisher.cdep.manifest.Manifest;
import java.io.File;

public class ResolvedManifest {

    final public File remote;
    final public Manifest manifest;

    ResolvedManifest(File remote, Manifest manifest) {
        this.remote = remote;
        this.manifest = manifest;
    }
}
