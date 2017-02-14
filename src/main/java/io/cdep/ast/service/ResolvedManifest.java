package io.cdep.ast.service;

import io.cdep.yml.cdepmanifest.CDepManifestYml;
import java.net.URL;

public class ResolvedManifest {

    final public URL remote;
    final public CDepManifestYml cdepManifestYml;

    public ResolvedManifest(URL remote, CDepManifestYml cdepManifestYml) {
        this.remote = remote;
        this.cdepManifestYml = cdepManifestYml;
    }
}
