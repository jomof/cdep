package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.Version;
import io.cdep.cdep.pod.PlainOldDataEqualityCovisitor;

@SuppressWarnings("unused")
public class CDepManifestYmlEquality extends PlainOldDataEqualityCovisitor {

  public static boolean areDeeplyIdentical(Object left, Object right) {
    CDepManifestYmlEquality thiz = new CDepManifestYmlEquality();
    thiz.covisit(left, right);
    return thiz.areEqual;
  }

  public void covisitCDepManifestYmlVersion(String name, CDepManifestYmlVersion left, CDepManifestYmlVersion right) {
  }

  public void covisitInterfaces(String name, Interfaces left, Interfaces right) {
    covisitFields(left, right);
  }

  public void covisitCDepManifestYml(String name, CDepManifestYml left, CDepManifestYml right) {
    covisitFields(left, right);
  }

  public void covisitCoordinate(String name, Coordinate left, Coordinate right) {
    covisitFields(left, right);
  }

  public void covisitVersion(String name, Version left, Version right) {
    covisitFields(left, right);
  }

  public void covisitHardNameDependencyArray(String name, HardNameDependency left[], HardNameDependency right[]) {
    covisitArray(name, left, right, HardNameDependency.class);
  }

  public void covisitArchive(String name, Archive left, Archive right) {
    covisitFields(left, right);
  }

  public void covisitAndroid(String name, Android left, Android right) {
    covisitFields(left, right);
  }

  public void covisitAndroidArchiveArray(String name, AndroidArchive left[], AndroidArchive right[]) {
    covisitArray(name, left, right, AndroidArchive.class);
  }

  public void covisitAndroidArchive(String name, AndroidArchive left, AndroidArchive right) {
    covisitFields(left, right);
  }

  public void covisitCxxLanguageFeaturesArray(String name, CxxLanguageFeatures left[], CxxLanguageFeatures right[]) {
    covisitArray(name, left, right, CxxLanguageFeatures.class);
  }

  public void covisitCxxLanguageFeatures(String name, CxxLanguageFeatures left, CxxLanguageFeatures right) {
  }

  public void covisitLinux(String name, Linux left, Linux right) {
    covisitFields(left, right);
  }

  public void covisitLinuxArchiveArray(String name, LinuxArchive left[], LinuxArchive right[]) {
    covisitArray(name, left, right, LinuxArchive.class);
  }

  public void covisitLinuxArchive(String name, LinuxArchive left, LinuxArchive right) {
    covisitFields(left, right);
  }

  public void covisitiOS(String name, iOS left, iOS right) {
    covisitFields(left, right);
  }

  public void covisitiOSArchiveArray(String name, iOSArchive left[], iOSArchive right[]) {
    covisitArray(name, left, right, iOSArchive.class);
  }

  public void covisitiOSArchive(String name, iOSArchive left, iOSArchive right) {
    covisitFields(left, right);
  }

  public void covisitiOSPlatform(String name, iOSPlatform left, iOSPlatform right) {
    checkEquals(left, right);
  }

  public void covisitiOSArchitecture(String name, iOSArchitecture left, iOSArchitecture right) {
    checkEquals(left, right);
  }

  public void covisitHardNameDependency(String name, HardNameDependency left, HardNameDependency right) {
    covisitFields(left, right);
  }
}
