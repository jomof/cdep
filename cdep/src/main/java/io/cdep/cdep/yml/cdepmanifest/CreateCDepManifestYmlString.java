package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;
import io.cdep.annotations.Nullable;
import io.cdep.cdep.utils.StringUtils;

import static io.cdep.cdep.utils.Invariant.notNull;

public class CreateCDepManifestYmlString extends CDepManifestYmlReadonlyVisitor {

  private int indent = 0;
  private int eatIndent = 0;
  @org.jetbrains.annotations.NotNull
  @NotNull
  private StringBuilder sb = new StringBuilder();

  public static String create(@org.jetbrains.annotations.NotNull @NotNull Object node) {
    CreateCDepManifestYmlString thiz = new CreateCDepManifestYmlString();
    thiz.visitPlainOldDataObject(null, node);
    return thiz.sb.toString();
  }

  @Override
  public void visitPlainOldDataObject(@Nullable String name, @org.jetbrains.annotations.NotNull @NotNull Object value) {
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
  public void visitiOSPlatform(String name, iOSPlatform value) {
    appendIndented("%s: %s\r\n", notNull(name), value);
  }

  @Override
  public void visitiOSArchitecture(String name, iOSArchitecture value) {
    appendIndented("%s: %s\r\n", notNull(name), value);
  }

  @Override
  public void visitString(String name, @org.jetbrains.annotations.NotNull @NotNull String value) {
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
  public void visitStringArray(String name, String[] array) {
    appendIndented("%s: [%s]\r\n", name, StringUtils.joinOn(", ", array));
  }

  @Override
  public void visitArray(String name, @org.jetbrains.annotations.NotNull @NotNull Object[] array, @org.jetbrains.annotations.NotNull @NotNull Class<?>
      elementType) {
    appendIndented("%s:\r\n", name);
    ++indent;
    for (int i = 0; i < array.length; ++i) {
      appendIndented("- ");
      ++eatIndent;
      visit(array[i], elementType);
      --indent;
    }
    --indent;
  }

  private void append(@org.jetbrains.annotations.NotNull @NotNull String format, Object... parms) {
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
