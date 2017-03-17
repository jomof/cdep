package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.utils.StringUtils;

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
  public void visitCoordinate(String name, Coordinate node) {
    append("%s:\n", name);
    ++indent;
    super.visitCoordinate(name, node);
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
  public void visitObject(String name, Object value) {
    appendIndented("%s:\n", name);
    ++indent;
    super.visitObject(name, value);
    --indent;
  }

  @Override
  public void visitArchive(String name, Archive value) {
    appendIndented("%s:\n", name);
    ++indent;
    super.visitObject(name, value);
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

    if (name == null) {
      // Likely array element
      appendIndented("%s\n", node);
      return;
    }
    appendIndented("%s: %s\n", name, node);
  }

  @Override
  public void visitStringArray(String name, String[] array) {
    if (array == null) {
      return;
    }
    appendIndented("%s: [%s]\n", name, StringUtils.joinOn(", ", array));
  }

  @Override
  public void visitArray(Object[] array, Class<?> elementType) {
    if (array == null) {
      return;
    }

    ++indent;
    for (int i = 0; i < array.length; ++i) {
      appendIndented("- ");
      ++eatIndent;
      visitElement(array[i], elementType);
      --indent;
    }
    --indent;
  }

  @Override
  public void visitHardNameDependencyArray(String name, HardNameDependency[] array) {
    if (array == null) {
      return;
    }
    appendIndented("%s:\n", name);
    super.visitHardNameDependencyArray(name, array);
  }

  @Override
  public void visitAndroidArchiveArray(String name, AndroidArchive[] array) {
    if (array == null) {
      return;
    }
    appendIndented("%s:\n", name);
    super.visitAndroidArchiveArray(name, array);
  }

  @Override
  public void visitiOSArchiveArray(String name, iOSArchive[] array) {
    if (array == null) {
      return;
    }
    appendIndented("%s:\n", name);
    super.visitiOSArchiveArray(name, array);
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
