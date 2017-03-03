package io.cdep.cdep.resolver;

import static com.google.common.truth.Truth.assertThat;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.utils.CoordinateUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.HardNameDependency;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestResolutionScope {

  @Test
  public void testNoRoots() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[0]);
    assertThat(scope.isResolutionComplete()).isTrue();
  }

  @Test
  public void testSimpleResolution() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[]{
        new SoftNameDependency("com.github.jomof:firebase/admob:2.1.3-rev7")
    });
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getUnresolvedReferences()).hasSize(1);
    SoftNameDependency unresolved = scope.getUnresolvedReferences().iterator().next();
    assertThat(unresolved.compile).isEqualTo("com.github.jomof:firebase/admob:2.1.3-rev7");
    Coordinate coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/admob:2.1.3-rev7");
    CDepManifestYml manifest = new CDepManifestYml(coordinate);
    ResolvedManifest resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    scope.recordResolved(unresolved, resolved, new ArrayList<>());
    assertThat(scope.isResolutionComplete()).isTrue();
  }

  @Test
  public void testSimpleUnresolvable() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[]{
        new SoftNameDependency("com.github.jomof:firebase/admob:2.1.3-rev7")
    });
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getUnresolvedReferences()).hasSize(1);
    SoftNameDependency unresolved = scope.getUnresolvedReferences().iterator().next();
    assertThat(unresolved.compile).isEqualTo("com.github.jomof:firebase/admob:2.1.3-rev7");
    scope.recordUnresolvable(new SoftNameDependency("com.github.jomof:firebase/admob:2.1.3-rev7"));
    assertThat(scope.isResolutionComplete()).isTrue();
    assertThat(scope.getResolvedNames()).hasSize(1);
    assertThat(scope.getResolution(scope.getResolvedNames().iterator().next()))
        .isSameAs(ResolutionScope.UNRESOLVEABLE_RESOLUTION);
  }

  @Test
  public void testTransitiveResolution() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[]{
        new SoftNameDependency("com.github.jomof:firebase/admob:2.1.3-rev7")
    });
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getUnresolvedReferences()).hasSize(1);
    SoftNameDependency unresolved = scope.getUnresolvedReferences().iterator().next();
    assertThat(unresolved.compile).isEqualTo("com.github.jomof:firebase/admob:2.1.3-rev7");
    Coordinate coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/admob:2.1.3-rev7");
    CDepManifestYml manifest = new CDepManifestYml(coordinate);
    ResolvedManifest resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    List<HardNameDependency> transitiveDependencies = new ArrayList<>();
    transitiveDependencies.add(new HardNameDependency(
        "com.github.jomof:firebase/app:2.1.3-rev7", "shavalue"));
    scope.recordResolved(unresolved, resolved, transitiveDependencies);
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getResolutions()).hasSize(1);
  }

  @Test
  public void testLocalPathResolution() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[]{
        new SoftNameDependency("/tmp/cdep-manifest.yml")
    });
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getUnresolvedReferences()).hasSize(1);
    SoftNameDependency unresolved = scope.getUnresolvedReferences().iterator().next();
    assertThat(unresolved.compile).isEqualTo("/tmp/cdep-manifest.yml");
    Coordinate coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/admob:2.1.3-rev7");
    CDepManifestYml manifest = new CDepManifestYml(coordinate);
    ResolvedManifest resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    List<HardNameDependency> transitiveDependencies = new ArrayList<>();
    scope.recordResolved(unresolved, resolved, transitiveDependencies);
    assertThat(scope.isResolutionComplete()).isTrue();
    assertThat(scope.getResolutions()).hasSize(1);
  }

  @Test
  public void testTransitiveResolutionWithDependencyAlsoRoot() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[]{
        new SoftNameDependency("com.github.jomof:firebase/admob:2.1.3-rev7"),
        new SoftNameDependency("com.github.jomof:firebase/app:2.1.3-rev7"),
    });
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getUnresolvedReferences()).hasSize(2);
    SoftNameDependency unresolved = scope.getUnresolvedReferences().iterator().next();
    assertThat(unresolved.compile).isEqualTo("com.github.jomof:firebase/admob:2.1.3-rev7");
    Coordinate coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/admob:2.1.3-rev7");
    CDepManifestYml manifest = new CDepManifestYml(coordinate);
    ResolvedManifest resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    List<HardNameDependency> transitiveDependencies = new ArrayList<>();
    transitiveDependencies.add(new HardNameDependency(
        "com.github.jomof:firebase/app:2.1.3-rev7", "shavalue"));
    scope.recordResolved(unresolved, resolved, transitiveDependencies);
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getResolutions()).hasSize(1);
  }

  @Test
  public void testTwoTransitiveReferencesToSameDependency() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[]{
        new SoftNameDependency("com.github.jomof:firebase/admob:2.1.3-rev7"),
        new SoftNameDependency("com.github.jomof:firebase/database:2.1.3-rev7"),
    });
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getUnresolvedReferences()).hasSize(2);
    SoftNameDependency unresolved = scope.getUnresolvedReferences().iterator().next();
    assertThat(unresolved.compile).isEqualTo("com.github.jomof:firebase/admob:2.1.3-rev7");

    Coordinate coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/admob:2.1.3-rev7");
    CDepManifestYml manifest = new CDepManifestYml(coordinate);
    ResolvedManifest resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    List<HardNameDependency> transitiveDependencies = new ArrayList<>();
    transitiveDependencies.add(new HardNameDependency(
        "com.github.jomof:firebase/app:2.1.3-rev7", "shavalue"));
    scope.recordResolved(unresolved, resolved, transitiveDependencies);

    coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/app:2.1.3-rev7");
    manifest = new CDepManifestYml(coordinate);
    resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    transitiveDependencies = new ArrayList<>();
    scope.recordResolved(unresolved, resolved, transitiveDependencies);

    coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/database:2.1.3-rev7");
    manifest = new CDepManifestYml(coordinate);
    resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    transitiveDependencies = new ArrayList<>();
    transitiveDependencies.add(new HardNameDependency(
        "com.github.jomof:firebase/app:2.1.3-rev7", "shavalue"));
    scope.recordResolved(unresolved, resolved, transitiveDependencies);

    assertThat(scope.isResolutionComplete()).isTrue();
    assertThat(scope.getResolutions()).hasSize(3);
  }

  @Test
  public void testUnparsable() throws IOException {
    ResolutionScope scope = new ResolutionScope(new SoftNameDependency[]{
        new SoftNameDependency("com.github.jomof:firebase/admob:2.1.3-rev7")
    });
    assertThat(scope.isResolutionComplete()).isFalse();
    assertThat(scope.getUnresolvedReferences()).hasSize(1);
    SoftNameDependency unresolved = scope.getUnresolvedReferences().iterator().next();
    assertThat(unresolved.compile).isEqualTo("com.github.jomof:firebase/admob:2.1.3-rev7");
    Coordinate coordinate = CoordinateUtils.tryParse("com.github.jomof:firebase/admob:2.1.3-rev7");
    CDepManifestYml manifest = new CDepManifestYml(coordinate);
    ResolvedManifest resolved = new ResolvedManifest(new URL("http://www.google.com"), manifest);
    List<HardNameDependency> transitiveDependencies = new ArrayList<>();
    transitiveDependencies.add(new HardNameDependency(
        "com.github.jomof:firebase/app", "shavalue"));
    scope.recordResolved(unresolved, resolved, transitiveDependencies);
    assertThat(scope.isResolutionComplete()).isTrue();
    assertThat(scope.getResolvedNames()).hasSize(2);
    assertThat(scope.getResolution("com.github.jomof:firebase/app"))
        .isSameAs(ResolutionScope.UNPARSEABLE_RESOLUTION);
  }
}
