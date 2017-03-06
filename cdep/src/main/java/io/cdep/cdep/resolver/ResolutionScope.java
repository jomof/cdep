package io.cdep.cdep.resolver;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.utils.CoordinateUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Records the current state of resolving top-level and transitive dependencies.
 */
public class ResolutionScope {

  final static public Resolution UNPARSEABLE_RESOLUTION = new Resolution();
  final static public Resolution UNRESOLVEABLE_RESOLUTION = new Resolution();
  // Map of dependency edges. Key is dependant and value is dependee.
    final public Map<Coordinate, Coordinate> edges = new HashMap<>();
  // Dependencies that are not yet resolved but where resolution is possible
  final private Map<String, SoftNameDependency> unresolved = new HashMap<>();
  // Dependencies that have been resolved (successfully or unsuccessfully)
  final private Map<String, Resolution> resolved = new HashMap<>();

  /**
   * Construct a fresh resolution scope.
   *
   * @param roots are the top level dependencies from cdep.yml.
   */
    public ResolutionScope(SoftNameDependency[] roots) {
        for (int i = 0; i < roots.length; ++i) {
          addUnresolved(roots[i]);
        }
    }

  /**
   * Add an unresolved dependency to be resolved later.
   *
   * @param softname the name of the unresolved dependency.
   */
  private void addUnresolved(SoftNameDependency softname) {
    if (!resolved.containsKey(softname.compile)) {
      unresolved.put(softname.compile, softname);
    }
  }

  /**
   * Return true if there are no more references to resolve.
   */
  public boolean isResolutionComplete() {
    return unresolved.size() == 0;
  }

  /**
   * Return all remaining unresolved references.
   */
  public Collection<SoftNameDependency> getUnresolvedReferences() {
    return new ArrayList<>(unresolved.values());
  }

  /**
   * Whether the given dependency is resolved or not
   *
   * @param name the name of the dependency.
   * @return true if the dependency has already been resolved.
   */
  private boolean isResolved(String name) {
    return resolved.containsKey(name);
    }

    /**
     * Record the fact that the given dependency has been resolved.
     * @param softname the name that started the resolution.
     * @param resolved the resolved manifest and hard name.
     * @param transitiveDependencies any new dependencies that were discovered during resolution
     */
    public void recordResolved(SoftNameDependency softname, ResolvedManifest resolved,
        List<HardNameDependency> transitiveDependencies) {
      assert !isResolved(resolved.cdepManifestYml.coordinate.toString());

      this.resolved.put(resolved.cdepManifestYml.coordinate.toString(),
          new FoundManifestResolution(resolved));

      unresolved.remove(resolved.cdepManifestYml.coordinate.toString());
      unresolved.remove(softname.compile);

      for (HardNameDependency hardname : transitiveDependencies) {
            Coordinate coordinate = CoordinateUtils.tryParse(hardname.compile);
            if (coordinate == null) {
                this.resolved.put(hardname.compile, UNPARSEABLE_RESOLUTION);
                continue;
            }
            edges.put(resolved.cdepManifestYml.coordinate, coordinate);
            addUnresolved(new SoftNameDependency(coordinate.toString()));
      }
    }

    /**
     * Record fact that a given dependency could not be resolved.
     * @param softname the name of the unresolvable dependency.
     */
    public void recordUnresolvable(SoftNameDependency softname) {
        this.unresolved.remove(softname.compile);
        this.resolved.put(softname.compile, UNRESOLVEABLE_RESOLUTION);
    }

  /**
   * Return the set of resolved names (coordinates or soft names).
   */
  public Collection<String> getResolvedNames() {
    return resolved.keySet();
  }

  /**
   * Return the set of resolved names (coordinates or soft names).
   */
  public Resolution getResolution(String name) {
    return resolved.get(name);
  }

  /**
   * Return the set of resolved names (coordinates or soft names).
   */
  public Collection<Resolution> getResolutions() {
    return resolved.values();
  }

  public static class Resolution {

  }

  /**
   * A resolution that indicates a manifest was found.
   */
  public static class FoundManifestResolution extends Resolution {

    final public ResolvedManifest resolved;

    FoundManifestResolution(ResolvedManifest resolved) {
      this.resolved = resolved;
        }
    }
}
