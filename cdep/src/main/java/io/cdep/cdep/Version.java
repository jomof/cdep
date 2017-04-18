package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import java.util.Objects;

public class Version {
  public static final Version EMPTY_VERSION = new Version();

  @NotNull
  public final String value;

  public Version(@NotNull String version) {
    this.value = version;
  }

  private Version() {
    this.value = "";
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj != null && obj instanceof Version && Objects.equals(value, obj.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @NotNull
  @Override
  public String toString() {
    return value;
  }
}
