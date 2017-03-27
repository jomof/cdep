package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.Coordinate;

import static io.cdep.cdep.yml.cdepmanifest.CDepManifestBuilder.*;
import static io.cdep.cdep.yml.cdepmanifest.CDepManifestBuilder.iOS;

/**
 * Semantic merge of two manifests
 */
public class MergeCDepManifestYmls extends CDepManifestYmlEquality {

  @Nullable
  private Object returnValue = null;

  @Nullable
  public static CDepManifestYml merge(CDepManifestYml left, CDepManifestYml right) {
    MergeCDepManifestYmls thiz = new MergeCDepManifestYmls();
    thiz.covisit(left, right);
    if (!thiz.areEqual) {
      throw new RuntimeException(String.format("Manifests were different at %s", thiz.firstDifference));
    }
    return (CDepManifestYml) thiz.returnValue;
  }

  @Override
  public void covisit(String name, Object left, Object right, @NotNull Class<?> type) {
    returnValue = null;
    super.covisit(name, left, right, type);
  }

  @Override
  public void covisitCDepManifestYml(String name, @Nullable CDepManifestYml left, @Nullable CDepManifestYml right) {
    if (left == null && right == null) {
      returnValue = null;
      return;
    }
    if (left == null) {
      returnValue = right;
      return;
    }
    if (right == null) {
      returnValue = left;
      return;
    }
    covisit("coordinate", left.coordinate, right.coordinate, Coordinate.class);
    covisitHardNameDependencyArray("dependencies", left.dependencies, right.dependencies);
    covisit("archive", left.archive, right.archive, Archive.class);
    covisit("example", left.example, right.example, String.class);
    covisit("linux", left.linux, right.linux, Object.class);
    covisit("iOS", left.iOS, right.iOS, iOS.class);
    iOS ios = (iOS) returnValue;
    covisit("android", left.android, right.android, Android.class);
    Android android = (Android) returnValue;
    covisit("linux", left.linux, right.linux, Linux.class);
    Linux linux = (Linux) returnValue;

    returnValue = new CDepManifestYml(left.coordinate, left.dependencies, left.archive, android,
        ios, linux, left.example);
  }

  @Override
  public void covisitAndroid(String name, @Nullable Android left, @Nullable Android right) {
    if (left == null && right == null) {
      returnValue = null;
      return;
    }
    if (left == null) {
      returnValue = right;
      return;
    }
    if (right == null) {
      returnValue = left;
      return;
    }
    covisitHardNameDependencyArray("dependencies", left.dependencies, right.dependencies);
    covisitAndroidArchiveArray("archive", left.archives, right.archives);
    AndroidArchive archives[] = (AndroidArchive[]) returnValue;
    returnValue = android(left.dependencies, archives);
  }

  @Override
  public void covisitiOS(String name, @Nullable iOS left, @Nullable iOS right) {
    if (left == null && right == null) {
      returnValue = null;
      return;
    }
    if (left == null) {
      returnValue = right;
      return;
    }
    if (right == null) {
      returnValue = left;
      return;
    }
    covisitHardNameDependencyArray("dependencies", left.dependencies, right.dependencies);
    covisitiOSArchiveArray("archive", left.archives, right.archives);
    iOSArchive archives[] = (iOSArchive[]) returnValue;
    returnValue = iOS(left.dependencies, archives);
  }

  @Override
  public void covisitLinux(String name, @Nullable Linux left, @Nullable Linux right) {
    if (left == null && right == null) {
      returnValue = null;
      return;
    }
    if (left == null) {
      returnValue = right;
      return;
    }
    if (right == null) {
      returnValue = left;
      return;
    }
    covisitLinuxArchiveArray("archive", left.archives, right.archives);
    LinuxArchive archives[] = (LinuxArchive[]) returnValue;
    returnValue = linux(archives);
  }

  @Override
  public void covisitAndroidArchiveArray(String name, @Nullable AndroidArchive[] left, @Nullable AndroidArchive[] right) {
    if (left == null) {
      returnValue = right;
      return;
    }
    if (right == null) {
      returnValue = left;
      return;
    }
    AndroidArchive result[] = new AndroidArchive[left.length + right.length];
    int j = 0;
    for (int i = 0; i < left.length; ++i, ++j) {
      result[j] = left[i];
    }
    for (int i = 0; i < right.length; ++i, ++j) {
      result[j] = right[i];
    }
    returnValue = result;
  }

  @Override
  public void covisitiOSArchiveArray(String name, @Nullable iOSArchive[] left, @Nullable iOSArchive[] right) {
    if (left == null) {
      returnValue = right;
      return;
    }
    if (right == null) {
      returnValue = left;
      return;
    }
    iOSArchive result[] = new iOSArchive[left.length + right.length];
    int j = 0;
    for (int i = 0; i < left.length; ++i, ++j) {
      result[j] = left[i];
    }
    for (int i = 0; i < right.length; ++i, ++j) {
      result[j] = right[i];
    }
    returnValue = result;
  }

  @Override
  public void covisitLinuxArchiveArray(String name, @Nullable LinuxArchive[] left, @Nullable LinuxArchive[] right) {
    if (left == null) {
      returnValue = right;
      return;
    }
    if (right == null) {
      returnValue = left;
      return;
    }
    LinuxArchive result[] = new LinuxArchive[left.length + right.length];
    int j = 0;
    for (int i = 0; i < left.length; ++i, ++j) {
      result[j] = left[i];
    }
    for (int i = 0; i < right.length; ++i, ++j) {
      result[j] = right[i];
    }
    returnValue = result;
  }
}
