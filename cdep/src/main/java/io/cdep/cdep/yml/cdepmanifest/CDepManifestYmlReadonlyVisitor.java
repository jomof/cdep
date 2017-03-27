package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.pod.PlainOldDataReadonlyVisitor;
import org.jetbrains.annotations.NotNull;

abstract public class CDepManifestYmlReadonlyVisitor extends PlainOldDataReadonlyVisitor {

  public void visitCDepManifestYml(String name, @NotNull CDepManifestYml value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitHardNameDependency(String name, @NotNull HardNameDependency value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitCoordinate(String name, @NotNull Coordinate value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitHardNameDependencyArray(String name, @NotNull HardNameDependency array[]) {
    visitArray(name, array, HardNameDependency.class);
  }

  public void visitAndroidArchiveArray(String name, @NotNull AndroidArchive array[]) {
    visitArray(name, array, AndroidArchive.class);
  }

  public void visitiOSArchiveArray(String name, @NotNull iOSArchive array[]) {
    visitArray(name, array, iOSArchive.class);
  }

  public void visitiOSPlatform(String name, @NotNull iOSPlatform value) {
    visitPlainOldDataObject(null, value);
  }

  public void visitiOSArchitecture(String name, @NotNull iOSArchitecture value) {
    visitPlainOldDataObject(null, value);
  }

  public void visitArchive(String name, @NotNull Archive value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitAndroid(String name, @NotNull Android value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitiOS(String name, @NotNull iOS value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitAndroidArchive(String name, @NotNull AndroidArchive value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitiOSArchive(String name, @NotNull iOSArchive value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitLinux(String name, @NotNull Linux value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitLinuxArchiveArray(String name, @NotNull LinuxArchive array[]) {
    visitArray(name, array, LinuxArchive.class);
  }

  public void visitLinuxArchive(String name, @NotNull LinuxArchive value) {
    visitPlainOldDataObject(name, value);
  }
}
