package io.cdep.cdep.yml;

import ext.org.yaml.snakeyaml.Yaml;
import ext.org.yaml.snakeyaml.constructor.Constructor;
import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.yml.cdepmanifest.Android;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;

public class TestAndroid {

  @Nullable
  private static Android convertString(@NotNull String content) {
    Yaml yaml = new Yaml(new Constructor(Android.class));
    return (Android) yaml.load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  public void simple() {
    Android android = convertString("");
    assertThat(android).isNull();
  }

  @Test
  public void dependencies() {
    Android android = convertString("dependencies:");
    assertThat(android).isNotNull();
    assertThat(android.dependencies).isNull();
    assertThat(android.archives).isNotNull();
  }

}
