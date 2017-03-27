package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.pod.PlainOldDataReadonlyVisitor;

abstract public class CDepManifestYmlReadonlyVisitor extends PlainOldDataReadonlyVisitor {

  public void visitCDepManifestYml(String name, @org.jetbrains.annotations.NotNull @NotNull CDepManifestYml value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitHardNameDependency(String name, @org.jetbrains.annotations.NotNull @NotNull HardNameDependency value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitCoordinate(String name, @org.jetbrains.annotations.NotNull @NotNull Coordinate value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitHardNameDependencyArray(String name, @org.jetbrains.annotations.NotNull @NotNull HardNameDependency array[]) {
    visitArray(name, array, HardNameDependency.class);
  }

  public void visitAndroidArchiveArray(String name, @org.jetbrains.annotations.NotNull @NotNull AndroidArchive array[]) {
    visitArray(name, array, AndroidArchive.class);
  }

  public void visitiOSArchiveArray(String name, @org.jetbrains.annotations.NotNull @NotNull iOSArchive array[]) {
    visitArray(name, array, iOSArchive.class);
  }

  public void visitiOSPlatform(String name, @org.jetbrains.annotations.NotNull @NotNull iOSPlatform value) {
    visitPlainOldDataObject(null, value);
  }

  public void visitiOSArchitecture(String name, @org.jetbrains.annotations.NotNull @NotNull iOSArchitecture value) {
    visitPlainOldDataObject(null, value);
  }

  public void visitArchive(String name, @org.jetbrains.annotations.NotNull @NotNull Archive value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitAndroid(String name, @org.jetbrains.annotations.NotNull @NotNull Android value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitiOS(String name, @org.jetbrains.annotations.NotNull @NotNull iOS value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitAndroidArchive(String name, @org.jetbrains.annotations.NotNull @NotNull AndroidArchive value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitiOSArchive(String name, @org.jetbrains.annotations.NotNull @NotNull iOSArchive value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitLinux(String name, @org.jetbrains.annotations.NotNull @NotNull Linux value) {
    visitPlainOldDataObject(name, value);
  }

  public void visitLinuxArchiveArray(String name, @org.jetbrains.annotations.NotNull @NotNull LinuxArchive array[]) {
    visitArray(name, array, LinuxArchive.class);
  }

  public void visitLinuxArchive(String name, @org.jetbrains.annotations.NotNull @NotNull LinuxArchive value) {
    visitPlainOldDataObject(name, value);
  }
}
