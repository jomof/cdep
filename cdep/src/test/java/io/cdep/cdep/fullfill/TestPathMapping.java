package io.cdep.cdep.fullfill;

import org.junit.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by jomof on 4/9/2017.
 */
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
        "C:\\Users\\jomof\\projects\\hold\\cdep\\cdep\\..\\third_party\\stb\\tinydir.h " +
        "-> tinydir\\tinydir.h");
    assertThat(mappings).hasLength(1);
    assertThat(mappings[0].from.getName()).isEqualTo("tinydir.h");
    assertThat(mappings[0].from.getParentFile().getName()).isEqualTo("stb");
    assertThat(mappings[0].to.getName()).isEqualTo("tinydir.h");
    assertThat(mappings[0].to.getParentFile().getName()).isEqualTo("tinydir");
    assertThat(mappings[0].to.isAbsolute()).isEqualTo(false);
  }
}