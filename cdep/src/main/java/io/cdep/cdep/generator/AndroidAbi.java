package io.cdep.cdep.generator;

/**
 * Enum of valid ABI you can specify for NDK.
 */
public enum AndroidAbi {
  ARMEABI("armeabi"),
  ARMEABI_V7A("armeabi-v7a"),
  ARM64_V8A("arm64-v8a"),
  X86("x86"),
  X86_64("x86_64"),
  MIPS("mips"),
  MIPS64("mips64");

  private final String name;

  private AndroidAbi(String name) {
    this.name = name;
  }

  /**
   * Returns name of the ABI like "armeabi-v7a".
   */
  public String getName() {
    return name;
  }

  /**
   * The names of all ABIs.
   */
  public static String[] getNames() {
    String result[] = new String[AndroidAbi.values().length];
    int i = 0;
    for (AndroidAbi abi : values()) {
      result[i++] = abi.getName();
    }
    return result;
  }
}
