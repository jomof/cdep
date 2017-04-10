package io.cdep.cdep.fullfill;

import org.junit.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

public class TestPathMapping {

  @Test
  public void simple() {
    PathMapping mappings[] = PathMapping.parse("myfile.h");
    assertThat(mappings).hasLength(1);
    assertThat(mappings[0].from).isEqualTo(new File("myfile.h"));
    assertThat(mappings[0].to).isEqualTo(new File("myfile.h"));
  }

  @Test
  public void simplePair() {
    PathMapping mappings[] = PathMapping.parse(
        "../third_party/tinydir/tinydir.h " +
        "-> tinydir/tinydir.h");
    assertThat(mappings).hasLength(1);
    assertThat(mappings[0].from.getName()).isEqualTo("tinydir.h");
    assertThat(mappings[0].from.getParentFile().getName()).isEqualTo("tinydir");
    assertThat(mappings[0].to.getName()).isEqualTo("tinydir.h");
    assertThat(mappings[0].to.getParentFile().getName()).isEqualTo("tinydir");
    assertThat(mappings[0].to.isAbsolute()).isEqualTo(false);
  }

  @Test
  public void expandSimple() {
    PathMapping mappings[] = PathMapping.parse("../third_party/vectorial/include/...");
    assertThat(mappings).hasLength(21);
  }
}