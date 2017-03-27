package io.cdep.cdep.utils;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.yml.cdepsha25.CDepSHA256;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TestCDepSHA256Utils {
    @Test
    public void coverConstructor() {
        // Call constructor of tested class to cover that code.
        new CoverConstructor();
    }

    @Test
    public void testSimple() {
        CDepSHA256 hashes = create(
                "- coordinate: com.github.jomof:boost:0.1.2-rev2\n" +
                "  sha256: abcd");

        assertThat(hashes.hashes).hasLength(1);
    }

    @Test
    public void testMultiple() {
        CDepSHA256 hashes = create(
                "- coordinate: com.github.jomof:boost:0.1.2-rev2\n" +
                "  sha256: abcd\n" +
                "- coordinate: com.github.jomof:boost:0.1.2-rev3\n" +
                "  sha256: abcd\n");

        assertThat(hashes.hashes).hasLength(2);
    }


  @NotNull
  private CDepSHA256 create(@NotNull String content) {
        CDepSHA256 hashes = CDepSHA256Utils.convertStringToCDepSHA256(content);
        return CDepSHA256Utils.convertStringToCDepSHA256(hashes.toString());

    }

    private static class CoverConstructor extends CDepSHA256Utils {

    }
}
