package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.utils.StringUtils;

import static io.cdep.cdep.utils.Invariant.notNull;

public class CreateCDepManifestYmlString extends CDepManifestYmlReadonlyVisitor {

  @NotNull
  private final StringBuilder sb = new StringBuilder();
  private int indent = 0;
  private int eatIndent = 0;

  public static String create(@NotNull Object node) {
    CreateCDepManifestYmlString thiz = new CreateCDepManifestYmlString();
    thiz.visitPlainOldDataObject(null, node);
    return thiz.sb.toString();
  }

  @Override
  public void visitPlainOldDataObject(@Nullable String name, @NotNull Object value) {
    if (name == null) {
      super.visitPlainOldDataObject(null, value);
      return;
    }
    appendIndented("%s:\r\n", name);
    ++indent;
    super.visitPlainOldDataObject(name, value);
    --indent;
  }

  @Override
  public void visitiOSPlatform(String name, @NotNull iOSPlatform value) {
    appendIndented("%s: %s\r\n", notNull(name), value);
  }

  @Override
  public void visitiOSArchitecture(String name, @NotNull iOSArchitecture value) {
    appendIndented("%s: %s\r\n", notNull(name), value);
  }

  @Override
  public void visitString(String name, @NotNull String value) {
    notNull(value);
    notNull(name);
    if (!value.contains("\n")) {
      appendIndented("%s: %s\r\n", name, value);
      return;
    }

    String lines[] = value.split("\\n"); // May contain \r as well, but that's okay
    appendIndented("%s: |\n", name);
    ++indent;
    for (int i = 0; i < lines.length; ++i) {
      appendIndented("%s", lines[i]);
      if (i != lines.length - 1) {
        append("\n");
      } else if (value.endsWith("\n")) {
        // Only want the trailing \n if the source had a trailing \n
        append("\n");
      }
    }
    --indent;
  }

  @Override
  public void visitLong(String name, Long node) {
    appendIndented("%s: %s\r\n", notNull(name), node);
  }

  @Override
  public void visitStringArray(String name, @NotNull String[] array) {
    appendIndented("%s: [%s]\r\n", name, StringUtils.joinOn(", ", array));
  }

  @Override
  public void visitArray(String name, @NotNull Object[] array, @NotNull Class<?>
      elementType) {
    appendIndented("%s:\r\n", name);
    ++indent;
    for (Object anArray : array) {
      appendIndented("- ");
      ++eatIndent;
      visit(anArray, elementType);
      --indent;
    }
    --indent;
  }

  private void append(@NotNull String format, Object... parms) {
    sb.append(String.format(format, parms));
  }

  private void appendIndented(String format, Object... parms) {
    String prefix = new String(new char[indent * 2]).replace('\0', ' ');
    if (eatIndent > 0) {
      prefix = "";
      --eatIndent;
      ++indent;
    }
    sb.append(String.format(prefix + format, parms));
  }
}
