package io.cdep.cdep.yml.cdepmanifest;

/**
 * Semantic merge of two manifests
 */
public class MergeCDepManifestYmls extends CDepManifestYmlEquality {

  private Object returnValue = null;

  public static CDepManifestYml merge(CDepManifestYml left, CDepManifestYml right) {
    MergeCDepManifestYmls thiz = new MergeCDepManifestYmls();
    thiz.covisit(left, right);
    if (!thiz.areEqual) {
      throw new RuntimeException(
          String.format("Manifests were different at %s", thiz.firstDifference));
    }
    return (CDepManifestYml) thiz.returnValue;
  }

  @Override
  public void covisitCDepManifestYml(String name, CDepManifestYml left, CDepManifestYml right) {
    super.covisitCDepManifestYml(name, left, right);
    returnValue = left;
  }

  @Override
  public void covisitAndroidArchiveArray(String name, AndroidArchive[] left,
      AndroidArchive[] right) {
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
  public void covisitiOSArchiveArray(String name, iOSArchive[] left, iOSArchive[] right) {
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
}
