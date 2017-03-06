package io.cdep.cdep.resolver;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.utils.CoordinateUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import java.net.URL;
import org.junit.Test;


public class TestResolver {

  private static Coordinate ADMOB_COORDINATE =
      CoordinateUtils.tryParse("com.github.jomof:firebase/admob:2.1.3-rev7");
  private static String ADMOB_URL =
      "https://github.com/jomof/firebase/releases/download/2.1.3-rev7/cdep-manifest-admob.yml";
  private static CDepManifestYml ADMOB_MANIFEST = CDepManifestYmlUtils.convertStringToManifest(
      "coordinate:\n"
          + "  groupId: com.github.jomof\n"
          + "  artifactId: firebase/admob\n"
          + "  version: 2.1.3-rev7\n"
          + "archive:\n"
          + "  file: firebase-include.zip\n"
          + "  sha256: 51827bab4c5b4f335058ab3c0a93f9fa39ba284d21bd686f27368829ee088815\n"
          + "  size: 93293\n"
          + "dependencies:\n"
          + "  - compile: com.github.jomof:firebase/app:2.1.3-rev7\n"
          + "    sha256: 8292d143db85ec40ddf4d51133571607f4df3796e0477e8678993dcae4acfd03");

  private static Coordinate APP_COORDINATE =
      CoordinateUtils.tryParse("com.github.jomof:firebase/app:2.1.3-rev7");
  private static String APP_URL =
      "https://github.com/jomof/firebase/releases/download/2.1.3-rev7/cdep-manifest-app.yml";
  private static CDepManifestYml APP_MANIFEST = CDepManifestYmlUtils.convertStringToManifest(
      "coordinate:\n"
          + "  groupId: com.github.jomof\n"
          + "  artifactId: firebase/app\n"
          + "  version: 2.1.3-rev7\n"
          + "archive:\n"
          + "  file: firebase-include.zip\n"
          + "  sha256: 51827bab4c5b4f335058ab3c0a93f9fa39ba284d21bd686f27368829ee088815\n"
          + "  size: 93293\n");

  private static CDepManifestYml ADMOB_MISSING_DEPENDENCY_MANIFEST = CDepManifestYmlUtils
      .convertStringToManifest(
          "coordinate:\n"
              + "  groupId: com.github.jomof\n"
              + "  artifactId: firebase/admob\n"
              + "  version: 2.1.3-rev7\n"
              + "archive:\n"
              + "  file: firebase-include.zip\n"
              + "  sha256: 51827bab4c5b4f335058ab3c0a93f9fa39ba284d21bd686f27368829ee088815\n"
              + "  size: 93293\n"
              + "dependencies:\n"
              + "  - compile: com.github.jomof:firebase/app:2.1.3-rev8\n"
              + "    sha256: 8292d143db85ec40ddf4d51133571607f4df3796e0477e8678993dcae4acfd03");

  private static CDepManifestYml ADMOB_BROKEN_DEPENDENCY_MANIFEST = CDepManifestYmlUtils
      .convertStringToManifest(
          "coordinate:\n"
              + "  groupId: com.github.jomof\n"
              + "  artifactId: firebase/admob\n"
              + "  version: 2.1.3-rev7\n"
              + "archive:\n"
              + "  file: firebase-include.zip\n"
              + "  sha256: 51827bab4c5b4f335058ab3c0a93f9fa39ba284d21bd686f27368829ee088815\n"
              + "  size: 93293\n"
              + "dependencies:\n"
              + "  - compile: xxx\n"
              + "    sha256: 8292d143db85ec40ddf4d51133571607f4df3796e0477e8678993dcae4acfd03");


  @Test
  public void testSimpleResolve() throws Exception {
    ManifestProvider provider = mock(ManifestProvider.class);
    when(provider.tryGetManifest(ADMOB_COORDINATE, new URL(ADMOB_URL))).thenReturn(ADMOB_MANIFEST);
    Resolver resolver = new Resolver(provider);
    ResolvedManifest resolved = resolver.resolveAny(
        new SoftNameDependency(ADMOB_COORDINATE.toString()));
    assertThat(resolved).isNotNull();
  }

  @Test
  public void twoResolversMatch() throws Exception {
    ManifestProvider provider = mock(ManifestProvider.class);
    when(provider.tryGetManifest(ADMOB_COORDINATE, new URL(ADMOB_URL))).thenReturn(ADMOB_MANIFEST);
    CoordinateResolver resolvers[] = new CoordinateResolver[]{
        new GithubReleasesCoordinateResolver(),
        new GithubReleasesCoordinateResolver()
    };
    Resolver resolver = new Resolver(provider, resolvers);
    try {
      resolver.resolveAny(
          new SoftNameDependency(ADMOB_COORDINATE.toString()));
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Multiple resolvers matched coordinate: "
          + "com.github.jomof:firebase/admob:2.1.3-rev7");
    }
  }

  @Test
  public void testScopeResolve() throws Exception {
    ManifestProvider provider = mock(ManifestProvider.class);
    when(provider.tryGetManifest(ADMOB_COORDINATE, new URL(ADMOB_URL))).thenReturn(ADMOB_MANIFEST);
    when(provider.tryGetManifest(APP_COORDINATE, new URL(APP_URL))).thenReturn(APP_MANIFEST);
    Resolver resolver = new Resolver(provider);
    ResolutionScope scope = resolver.resolveAll(new SoftNameDependency[]{
        new SoftNameDependency(ADMOB_COORDINATE.toString())});
    assertThat(scope.isResolutionComplete()).isTrue();
  }

  @Test
  public void testScopeUnresolvableDependencyResolve() throws Exception {
    ManifestProvider provider = mock(ManifestProvider.class);
    when(provider.tryGetManifest(ADMOB_COORDINATE, new URL(ADMOB_URL)))
        .thenReturn(ADMOB_MISSING_DEPENDENCY_MANIFEST);
    Resolver resolver = new Resolver(provider);
    try {
      resolver.resolveAll(new SoftNameDependency[]{
          new SoftNameDependency(ADMOB_COORDINATE.toString())});
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Could not resolve 'com.github.jomof:firebase/app:2.1.3-rev8'. "
          + "It doesn't exist.");
    }
  }

  @Test
  public void testScopeMalformedDependencyResolve() throws Exception {
    ManifestProvider provider = mock(ManifestProvider.class);
    when(provider.tryGetManifest(ADMOB_COORDINATE, new URL(ADMOB_URL)))
        .thenReturn(ADMOB_BROKEN_DEPENDENCY_MANIFEST);
    Resolver resolver = new Resolver(provider);
    try {
      resolver.resolveAll(new SoftNameDependency[]{
          new SoftNameDependency(ADMOB_COORDINATE.toString())});
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessage("Could not resolve 'xxx'. It didn't look like a coordinate.");
    }
  }

  @Test
  public void testEmptyScopeResolution() throws Exception {
    ManifestProvider provider = mock(ManifestProvider.class);
    when(provider.tryGetManifest(ADMOB_COORDINATE, new URL(ADMOB_URL)))
        .thenReturn(ADMOB_MISSING_DEPENDENCY_MANIFEST);
    Resolver resolver = new Resolver(provider);
    ResolutionScope scope =
        resolver.resolveAll(new SoftNameDependency[]{});
    assertThat(scope.isResolutionComplete()).isTrue();
    assertThat(scope.getResolutions()).hasSize(0);
  }
}