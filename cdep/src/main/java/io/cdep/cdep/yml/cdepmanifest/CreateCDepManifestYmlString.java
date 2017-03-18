package io.cdep.cdep.yml.cdepmanifest;

import static java.util.regex.Pattern.compile;

import io.cdep.cdep.utils.StringUtils;
import java.util.regex.Pattern;

public class CreateCDepManifestYmlString extends CDepManifestYmlReadonlyVisitor {

  final private Pattern pattern = compile(".*\\n");
  private int indent = 0;
  private int eatIndent = 0;
  private StringBuilder sb = new StringBuilder();

  public static String create(Object node) {
    CreateCDepManifestYmlString thiz = new CreateCDepManifestYmlString();
    thiz.visitPlainOldDataObject(null, node);
    return thiz.sb.toString();
  }

  @Override
  public void visitPlainOldDataObject(String name, Object value) {
    if (name == null) {
      super.visitPlainOldDataObject(name, value);
      return;
    }
    appendIndented("%s:\n", name);
    ++indent;
    super.visitPlainOldDataObject(name, value);
    --indent;
  }

  @Override
  public void visitString(String name, String value) {
    assert value != null;
    assert name != null;
    if (!value.contains("\n")) {
      appendIndented("%s: %s\n", name, value);
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
    return;
  }

  @Override
  public void visitLong(String name, Long node) {
    assert name != null;
    appendIndented("%s: %s\n", name, node);
  }

  @Override
  public void visitStringArray(String name, String[] array) {
    appendIndented("%s: [%s]\n", name, StringUtils.joinOn(", ", array));
  }

  @Override
  public void visitArray(String name, Object[] array, Class<?> elementType) {
    appendIndented("%s:\n", name);
    ++indent;
    for (int i = 0; i < array.length; ++i) {
      appendIndented("- ");
      ++eatIndent;
      visit(array[i], elementType);
      --indent;
    }
    --indent;
  }

  private void append(String format, Object... parms) {
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
