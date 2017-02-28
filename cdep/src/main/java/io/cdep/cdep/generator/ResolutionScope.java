package io.cdep.cdep.generator;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.utils.CoordinateUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;

import java.util.*;

public class ResolutionScope {
    public static class Resolution {

    }
    public static class FoundManifestResolution extends Resolution {
        final public ResolvedManifest resolved;
        FoundManifestResolution(ResolvedManifest resolved) {
            this.resolved = resolved;
        }
    }
    final static Resolution UNPARSEABLE_RESOLUTION = new Resolution();
    final static Resolution UNRESOLVEABLE_RESOLUTION = new Resolution();
    final public Map<String, SoftNameDependency> unresolved = new HashMap<>();
    final public Map<String, Resolution> resolved = new HashMap<>();
    final public Map<Coordinate, Coordinate> edges = new HashMap<>();

    public ResolutionScope(SoftNameDependency[] roots) {
        for (int i = 0; i < roots.length; ++i) {
            recordUnresolved(roots[i]);
        }
    }

    public void recordUnresolved(SoftNameDependency softname) {
        if (!resolved.containsKey(softname.compile)) {
            unresolved.put(softname.compile, softname);
        }
    }

    public boolean isResolved(String name) {
        return resolved.containsKey(name);
    }

    public void recordResolved(
            String softname,
            ResolvedManifest resolved,
            List<HardNameDependency> transitiveDependencies) {
        assert !isResolved(resolved.cdepManifestYml.coordinate.toString());
        this.resolved.put(resolved.cdepManifestYml.coordinate.toString(),
                new FoundManifestResolution(resolved));
        this.unresolved.remove(resolved.cdepManifestYml.coordinate.toString());
        this.unresolved.remove(softname);

        for (HardNameDependency hardname : transitiveDependencies) {
            if (isResolved(hardname.compile)) {
                continue;
            }
            Coordinate coordinate = CoordinateUtils.tryParse(hardname.compile);
            if (coordinate == null) {
                this.resolved.put(hardname.compile, UNPARSEABLE_RESOLUTION);
                continue;
            }
            edges.put(resolved.cdepManifestYml.coordinate, coordinate);
            recordUnresolved(new SoftNameDependency(coordinate.toString()));
        }
    }

    public void recordUnresolvable(SoftNameDependency softname) {
        this.unresolved.remove(softname.compile);
        this.resolved.put(softname.compile, UNRESOLVEABLE_RESOLUTION);
    }
}
