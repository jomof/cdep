package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.cdep.utils.StringUtils;

public class CreateCDepManifestYmlString extends CDepManifestYmlReadonlyVisitor {
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
  public void visitString(String name, String node) {
    assert node != null;
    assert name != null;
    if (node.contains("\n")) {
      String lines[] = node.split("\\r?\\n");
      appendIndented("%s: |\n", name);
      ++indent;
      for (String line : lines) {
        appendIndented("%s\r\n", line);
      }
      --indent;
      return;
    }
    appendIndented("%s: %s\n", name, node);
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
      visitElement(array[i], elementType);
      --indent;
    }
    --indent;
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
