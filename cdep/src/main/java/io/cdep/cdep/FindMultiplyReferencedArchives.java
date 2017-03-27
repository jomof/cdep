package io.cdep.cdep;

import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.ModuleArchiveExpression;
import io.cdep.cdep.ast.finder.ModuleExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cdep.cdep.utils.Invariant.require;

/**
 * This checker looks at the SHA256 of files along each dependency chain and ensures that each file
 * is only present at a single level.
 * <p>
 * Also check whether references were unresolved.
 */
public class FindMultiplyReferencedArchives extends ReadonlyVisitor {

  // Map of dependency edges. Key is dependant and value is dependees.
  final public Map<Coordinate, List<Coordinate>> forwardEdges = new HashMap<>();
  // Map of dependency edges. Key is dependee and value is dependants.
  final public Map<Coordinate, List<Coordinate>> backwardEdges = new HashMap<>();
  // Map from module coordinate to the archives that it references
  final public Map<Coordinate, List<ModuleArchiveExpression>> moduleArchives = new HashMap<>();
  private Coordinate currentFindModule = null;

  /**
   * Utility function to add a new edge to an edge map.
   */
  private static void addEdge(Map<Coordinate, List<Coordinate>> edges, Coordinate from, Coordinate to) {
    List<Coordinate> tos = edges.get(from);
    if (tos == null) {
      edges.put(from, new ArrayList<Coordinate>());
      addEdge(edges, from, to);
      return;
    }
    tos.add(to);
  }

  /**
   * Utility function to add a new edge to an edge map.
   */
  private void addModuleArchive(ModuleArchiveExpression archive) {
    require(currentFindModule != null);
    List<ModuleArchiveExpression> tos = moduleArchives.get(currentFindModule);
    if (tos == null) {
      moduleArchives.put(currentFindModule, new ArrayList<ModuleArchiveExpression>());
      addModuleArchive(archive);
      return;
    }
    tos.add(archive);
  }

  @Override
  void visitFunctionTableExpression(FunctionTableExpression expr) {
    super.visitFunctionTableExpression(expr);
    for (Coordinate coordinate : forwardEdges.keySet()) {
      Map<String, Coordinate> shaToPrior = copyArchivesInto(coordinate, null);
      validateForward(coordinate, shaToPrior);
    }
  }

  private void validateForward(Coordinate dependant, Map<String, Coordinate> shaToPrior) {
    for (Coordinate dependee : forwardEdges.get(dependant)) {
      List<ModuleArchiveExpression> dependeeArchives = moduleArchives.get(dependee);
      require(dependeeArchives != null, "Reference %s was not found, needed by %s", dependee, dependant);
      // Have any of the dependee archives been seen before?
      for (ModuleArchiveExpression dependeeArchive : moduleArchives.get(dependee)) {
        Coordinate prior = shaToPrior.get(dependeeArchive.sha256);
        require(prior == null, "Package '%s' depends on '%s' but both packages contain a file:\n  %s\nwith the " +
            "same SHA256. The file should only be in " + "the lowest level package '%s' (sha256:%s)", dependant,
            dependee, dependeeArchive.file, dependee, dependeeArchive.sha256.substring(0, 8));
      }
    }
  }

  /**
   * Produce a map from SHA256 of each archive to the coordinate that references that archive
   * as a dependency.
   */
  Map<String, Coordinate> copyArchivesInto(Coordinate coordinate, Map<String, Coordinate> original) {
    Map<String, Coordinate> copy = new HashMap<>();
    if (original != null) {
      copy.putAll(original);
    }
    for (ModuleArchiveExpression archive : moduleArchives.get(coordinate)) {
      copy.put(archive.sha256, coordinate);
    }
    return copy;
  }

  @Override
  protected void visitFindModuleExpression(FindModuleExpression expr) {
    this.currentFindModule = expr.coordinate;
    super.visitFindModuleExpression(expr);
    this.currentFindModule = null;
  }

  @Override
  protected void visitModuleExpression(ModuleExpression expr) {
    require(currentFindModule != null);
    for (Coordinate coordinate : expr.dependencies) {
      addEdge(forwardEdges, currentFindModule, coordinate);
      addEdge(backwardEdges, coordinate, currentFindModule);
    }
    super.visitModuleExpression(expr);
  }

  @Override
  protected void visitModuleArchiveExpression(ModuleArchiveExpression expr) {
    addModuleArchive(expr);
    super.visitModuleArchiveExpression(expr);
  }
}
