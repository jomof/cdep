package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.yml.cdepmanifest.*;

import static io.cdep.cdep.Coordinate.EMPTY_COORDINATE;

/**
 * When Yaml is de-serialized, it may contain null in fields marked @NonNull.
 * Fix those
 */
public class ConvertNullToDefaultRewritingVisitor extends CDepManifestYmlRewritingVisitor {

  @Nullable
  @Override
  protected AndroidArchive visitAndroidArchive(@NotNull AndroidArchive archive) {
    return super.visitAndroidArchive(new AndroidArchive(
        StringUtils.nullToEmpty(archive.file),
        StringUtils.nullToEmpty(archive.sha256),
        LongUtils.nullToZero(archive.size),
        StringUtils.nullToEmpty(archive.ndk),
        StringUtils.nullToEmpty(archive.compiler),
        StringUtils.nullToEmpty(archive.runtime),
        StringUtils.nullToEmpty(archive.platform),
        StringUtils.nullToEmpty(archive.builder),
        StringUtils.nullToEmpty(archive.abi),
        StringUtils.nullToEmpty(archive.include),
        ArrayUtils.nullToEmpty(archive.libs, String.class),
        StringUtils.nullToEmpty(archive.flavor)));
  }

  @NotNull
  @Override
  public CDepManifestYml visitCDepManifestYml(@NotNull CDepManifestYml value) {
    return super.visitCDepManifestYml(new CDepManifestYml(
        value.sourceVersion,
        ObjectUtils.nullToDefault(value.coordinate, EMPTY_COORDINATE),
        ArrayUtils.nullToEmpty(value.dependencies, HardNameDependency.class),
        value.interfaces,
        value.android,
        value.iOS,
        value.linux,
        StringUtils.nullToEmpty(value.example)));
  }

  @Nullable
  @Override
  protected Archive visitArchive(@Nullable Archive archive) {
    if (archive == null) {
      return null;
    }
    return super.visitArchive(new Archive(
        StringUtils.nullToEmpty(archive.file),
        StringUtils.nullToEmpty(archive.sha256),
        LongUtils.nullToZero(archive.size),
        StringUtils.nullToEmpty(archive.include),
        ArrayUtils.nullToEmpty(archive.requires, CxxLanguageFeatures.class)));
  }

  @NotNull
  @Override
  public iOSArchive visitiOSArchive(@NotNull iOSArchive archive) {
    return super.visitiOSArchive(new iOSArchive(
        StringUtils.nullToEmpty(archive.file),
        StringUtils.nullToEmpty(archive.sha256),
        LongUtils.nullToZero(archive.size),
        archive.platform,
        archive.architecture,
        StringUtils.nullToEmpty(archive.sdk),
        StringUtils.nullToEmpty(archive.include),
        ArrayUtils.nullToEmpty(archive.libs, String.class),
        StringUtils.nullToEmpty(archive.flavor)));
  }
}
