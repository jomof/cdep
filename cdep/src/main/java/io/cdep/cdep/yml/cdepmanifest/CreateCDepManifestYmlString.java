package io.cdep.cdep.yml.cdepmanifest;

public class CreateCDepManifestYmlString extends CDepManifestYmlReadonlyVisitor {
  private int indent = 0;
  private int eatIndent = 0;
  private StringBuilder sb = new StringBuilder();

  public static String create(Object node) {
    CreateCDepManifestYmlString thiz = new CreateCDepManifestYmlString();
    thiz.visit(node);
    return thiz.sb.toString();
  }

  @Override
  public void visitAndroid(String name, Android android) {
    appendIndented("%s:\n", name);
    ++indent;
    super.visitAndroid(name, android);
    --indent;
  }

  @Override
  public void visitiOS(String name, iOS value) {
    appendIndented("%s:\n", name);
    ++indent;
    super.visitiOS(name, value);
    --indent;
  }

  @Override
  public void visitString(String name, String node) {
    if (node == null) {
      return;
    }
    if (name == null) {
      // Likely array element
      appendIndented("%s\n", node);
      return;
    }
    appendIndented("%s: %s\n", name, node);
  }

  @Override
  public void visitLong(String name, Long node) {

    if (name == null) {
      // Likely array element
      appendIndented("%s\n", node);
      return;
    }
    appendIndented("%s: %s\n", name, node);
  }

  @Override
  public void visitArray(Object[] array, Class<?> elementType) {
    if (array == null) {
      return;
    }
    for (int i = 0; i < array.length; ++i) {
      if (i == 0) {
        appendIndented("- ");
        ++eatIndent;
        ++indent;
      }

      visitElement(array[i], elementType);
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
    }
    sb.append(String.format(prefix + format, parms));
  }
}
