package io.cdep.cdep;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;

import java.util.Objects;

public class Version {
  @Nullable
  public final String value;
  public Version(String version) {
    this.value = version;
  }
  public Version() {
    this.value = null;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj != null && obj instanceof Version && Objects.equals(value, obj.toString());
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @NotNull
  @Override
  public String toString() {
    return value;
  }
}
