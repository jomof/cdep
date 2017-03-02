package io.cdep.cdep.generator;

/**
 * Enum of valid ABI you can specify for NDK.
 */
public enum AndroidAbi {
  ARMEABI("armeabi", "arm"),
  ARMEABI_V7A("armeabi-v7a", "arm"),
  ARM64_V8A("arm64-v8a", "arm64"),
  X86("x86", "x86"),
  X86_64("x86_64", "x86_64");

  private final String name;
  private final String architecture;

  private AndroidAbi(String name, String architecture) {
    this.name = name;
    this.architecture = architecture;

  }

  /**
   * Returns the ABI Enum with the specified name.
   */
  public static AndroidAbi getByName(String name) {
    for (AndroidAbi abi : values()) {
      if (abi.name.equals(name)) {
        return abi;
      }
    }
    throw new RuntimeException(name);
  }

  /**
   * Returns name of the ABI like "armeabi-v7a".
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the CPU architecture like "arm".
   */
  public String getArchitecture() {
    return architecture;
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
