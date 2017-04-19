package io.cdep.cdep.yml;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.Truth;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYmlReadonlyVisitor;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertAbout;

/**
 * A Truth subject for dealing with CDepManifestYm
 */
public class CDepManifestYmlSubject extends Subject<CDepManifestYmlSubject, CDepManifestYml> {
  private static final SubjectFactory<CDepManifestYmlSubject, CDepManifestYml> EMPLOYEE_SUBJECT_FACTORY =
      new SubjectFactory<CDepManifestYmlSubject, CDepManifestYml>() {
        @Override
        public CDepManifestYmlSubject getSubject(FailureStrategy failureStrategy, @Nullable CDepManifestYml target) {
          return new CDepManifestYmlSubject(failureStrategy, target);
        }
      };

  public CDepManifestYmlSubject(FailureStrategy failureStrategy, CDepManifestYml actual) {
    super(failureStrategy, actual);
  }

  public static CDepManifestYmlSubject assertThat(@Nullable CDepManifestYml employee) {
    return assertAbout(EMPLOYEE_SUBJECT_FACTORY).that(employee);
  }

  public void hasCoordinate(Coordinate coordinate) {
    if (!actual().coordinate.equals(coordinate)) {
      fail("Coordinate was not the same", actual().coordinate, coordinate);
    }
  }

  public void hasArchiveNamed(String archive) {
    final Set<String> archives = new HashSet<>();

    // Gather archive names
    (new GatherArchivesYmlReadonlyVisitor(archives)).visitCDepManifestYml(null, actual());

    Truth.assertThat(archives).contains(archive);
  }

  public static class GatherArchivesYmlReadonlyVisitor extends CDepManifestYmlReadonlyVisitor {

    private final Set<String> archives;

    public GatherArchivesYmlReadonlyVisitor(Set<String> archives) {
      this.archives = archives;
    }

    @Override
    public void visitString(String name, String node) {
      if (name == null) {
        return;
      }
      if (name.equals("file")) {
        archives.add(node);
      }
    }
  }
}