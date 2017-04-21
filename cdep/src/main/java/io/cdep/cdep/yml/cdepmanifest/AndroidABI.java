package io.cdep.cdep.yml.cdepmanifest;

import io.cdep.annotations.NotNull;

@SuppressWarnings("WeakerAccess")
public class AndroidABI {
  final public static AndroidABI X86 = new AndroidABI("x86");
  final public static AndroidABI X86_64 = new AndroidABI("x86_64");
  final public static AndroidABI ARMEABI = new AndroidABI("armeabi");
  final public static AndroidABI ARMEABI_V7A = new AndroidABI("armeabi-v7a");
  final public static AndroidABI ARM64_V8A = new AndroidABI("arm64-v8a");
  final public static AndroidABI MIPS = new AndroidABI("mips");
  final public static AndroidABI MIPS64 = new AndroidABI("mips64");

  public static final AndroidABI EMPTY_ABI = new AndroidABI("");

  @NotNull
  final public String name;

  public AndroidABI(@NotNull String name) {
    this.name = name;
  }

  public static AndroidABI[] values() {
    return new AndroidABI[]{
        X86,
        X86_64,
        ARMEABI,
        ARMEABI_V7A,
        ARM64_V8A,
        MIPS,
        MIPS64,
    };
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof AndroidABI && ((AndroidABI) obj).name.equals(name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }
}
