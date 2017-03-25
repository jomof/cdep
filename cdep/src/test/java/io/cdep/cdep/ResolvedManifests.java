package io.cdep.cdep;

import io.cdep.cdep.resolver.ResolvedManifest;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ResolvedManifests {

  public static ResolvedManifest sqliteLinux() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: sqlite\n" +
        "  version: 0.0.0\n" +
        "linux:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-linux.zip\n" +
        "    sha256: a4fcb715b3b22a29ee774f30795516e46ccc4712351d13030f0f58b36c5b3d9b\n" +
        "    size: 480895\n" +
        "example: |\n" +
        "  #include <sqlite3.h>\n" +
        "  void test() {\n" +
        "    sqlite3_initialize();\n" +
        "  }");
  }

  public static ResolvedManifest sqliteLinuxMultiple() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: sqlite\n" +
        "  version: 0.0.0\n" +
        "linux:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-linux-1.zip\n" +
        "    sha256: a4fcb715b3b22a29ee774f30795516e46ccc4712351d13030f0f58b36c5b3d9b\n" +
        "    size: 480895\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-linux-2.zip\n" +
        "    sha256: a4fcb715b3b22a29ee774f30795516e46ccc4712351d13030f0f58b36c5b3d9b\n" +
        "    size: 480895\n" +
        "example: |\n" +
        "  #include <sqlite3.h>\n" +
        "  void test() {\n" +
        "    sqlite3_initialize();\n" +
        "  }");
  }

  public static ResolvedManifest archiveMissingSize() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: vectorial\n" +
        "  version: 0.0.0\n" +
        "archive:\n" +
        "  file: vectorial.zip\n" +
        "  sha256: 47e72f9898a78024a96e7adc5b29d6ec02313a02087646d69d7797f13840121c\n" +
        "  size: \n" +
        "  include: vectorial-master/include\n" +
        "example: |\n" +
        "  #include <vectorial/simd4f.h>\n" +
        "  void test() {\n" +
        "    float z = simd4f_get_z(simd4f_add(simd4f_create(1,2,3,4), \n" +
        "      simd4f_create(1,2,3,4)));\n" +
        "  }");
  }

  public static ResolvedManifest archiveMissingFile() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: vectorial\n" +
        "  version: 0.0.0\n" +
        "archive:\n" +
        "  file:\n" +
        "  sha256: 47e72f9898a78024a96e7adc5b29d6ec02313a02087646d69d7797f13840121c\n" +
        "  size: 92\n" +
        "  include: vectorial-master/include\n" +
        "example: |\n" +
        "  #include <vectorial/simd4f.h>\n" +
        "  void test() {\n" +
        "    float z = simd4f_get_z(simd4f_add(simd4f_create(1,2,3,4), \n" +
        "      simd4f_create(1,2,3,4)));\n" +
        "  }");
  }

  public static ResolvedManifest archiveMissingSha256() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: vectorial\n" +
        "  version: 0.0.0\n" +
        "archive:\n" +
        "  file: bob.zip\n" +
        "  sha256: \n" +
        "  size: 92\n" +
        "example: |\n" +
        "  #include <vectorial/simd4f.h>\n" +
        "  void test() {\n" +
        "    float z = simd4f_get_z(simd4f_add(simd4f_create(1,2,3,4), \n" +
        "      simd4f_create(1,2,3,4)));\n" +
        "  }");
  }

  public static ResolvedManifest archiveMissingInclude() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: vectorial\n" +
        "  version: 0.0.0\n" +
        "archive:\n" +
        "  file: bob.zip\n" +
        "  sha256: 47e72f9898a78024a96e7adc5b29d6ec02313a02087646d69d7797f13840121c\n" +
        "  size: 92\n" +
        "  include: vectorial-master/include\n" +
        "example: |\n" +
        "  #include <vectorial/simd4f.h>\n" +
        "  void test() {\n" +
        "    float z = simd4f_get_z(simd4f_add(simd4f_create(1,2,3,4), \n" +
        "      simd4f_create(1,2,3,4)));\n" +
        "  }");
  }

  static ResolvedManifest getResolvedManifest(String manifest) throws MalformedURLException {
    CDepManifestYml yml = CDepManifestYmlUtils.convertStringToManifest(manifest);
    return new ResolvedManifest(new URL("http://google.com/cdep-manifest.yml"), yml);
  }

  public static ResolvedManifest archiveOnly() throws MalformedURLException {

    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: vectorial\n" +
        "  version: 0.0.0\n" +
        "archive:\n" +
        "  file: vectorial.zip\n" +
        "  sha256: 47e72f9898a78024a96e7adc5b29d6ec02313a02087646d69d7797f13840121c\n" +
        "  size: 52863\n" +
        "  include: vectorial-master/include\n" +
        "example: |\n" +
        "  #include <vectorial/simd4f.h>\n" +
        "  void test() {\n" +
        "    float z = simd4f_get_z(simd4f_add(simd4f_create(1,2,3,4), \n" +
        "      simd4f_create(1,2,3,4)));\n" +
        "  }");
  }

  public static ResolvedManifest admob() throws MalformedURLException {

    return getResolvedManifest("coordinate:\n"
        + "  groupId: com.github.jomof\n"
        + "  artifactId: firebase/admob\n"
        + "  version: 2.1.3-rev8\n"
        + "dependencies:\n"
        + "  - compile: com.github.jomof:firebase/app:2.1.3-rev8\n"
        + "    sha256: 41ce110b24d2cfa26144b9df1241a4941c57e892f07eb91600103e53650ef0a8\n"
        + "archive:\n"
        + "  file: firebase-include.zip\n"
        + "  sha256: 26e3889c07ad841c5c9ff8b1ad86a575833bec1bb6f15719a527d52ced07a57f\n"
        + "  size: 93293\n"
        + "android:\n"
        + "  archives:\n"
        + "    - file: firebase-android-admob-cpp.zip\n"
        + "      sha256: 34c3cd109199cbccf7ebb1652a5dd66080c27d1448cfa3e6dd5c811aa30e283a\n"
        + "      size: 4990064\n"
        + "      ndk: r10d\n"
        + "      runtime: c++\n"
        + "      platform: 12\n"
        + "      abis: [arm64-v8a, armeabi, armeabi-v7a, mips, mips64, x86, x86_64]\n"
        + "      include: include\n"
        + "      lib: libadmob.a\n"
        + "    - file: firebase-android-admob-gnustl.zip\n"
        + "      sha256: 44cd6682f5f82735d4bf6103139e4179ed6c2b4ceec297fd04b129766e2a2a02\n"
        + "      size: 5294900\n"
        + "      ndk: r10d\n"
        + "      runtime: gnustl\n"
        + "      platform: 12\n"
        + "      abis: [arm64-v8a, armeabi, armeabi-v7a, mips, mips64, x86, x86_64]\n"
        + "      include: include\n"
        + "      lib: libadmob.a\n"
        + "    - file: firebase-android-admob-stlport.zip\n"
        + "      sha256: 0606618e53a06e00cd4c6775f49bd5474bccec68370691b35b12be2f1c89d755\n"
        + "      size: 5154330\n"
        + "      ndk: r10d\n"
        + "      runtime: stlport\n"
        + "      platform: 12\n"
        + "      abis: [arm64-v8a, armeabi, armeabi-v7a, mips, mips64, x86, x86_64]\n"
        + "      include: include\n"
        + "      lib: libadmob.a\n"
        + "example: |\n"
        + "  #include \"firebase/admob.h\"\n"
        + "  #include \"firebase/admob/types.h\"\n"
        + "  #include \"firebase/app.h\"\n"
        + "  #include \"firebase/future.h\"\n"
        + "  \n"
        + "  void test() {\n"
        + "    const char* kAdMobAppID = \"ca-app-pub-XXXXXXXXXXXXXXXX~NNNNNNNNNN\";\n"
        + "    firebase::admob::Initialize(\n"
        + "      *::firebase::App::Create(::firebase::AppOptions(), NULL /* jni_env */ , NULL /* activity */ ), \n"
        + "      kAdMobAppID);\n"
        + "  }\n");
  }

  public static ResolvedManifest sqlite() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: sqlite\n" +
        "  version: 0.0.0\n" +
        "android:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-cxx-platform-12.zip\n" +
        "    sha256: 45a104d61786eaf163b3006aa989922c5c04b8e787073e1cbd60c7895943162c\n" +
        "    size: 2676245\n" +
        "    runtime: c++\n" +
        "    platform: 12\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, x86 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-gnustl-platform-12.zip\n" +
        "    sha256: 5975eff815bd516b5da803f4921774ee38ec7d37fcb046bf2b3e078d920bd775\n" +
        "    size: 2676242\n" +
        "    runtime: gnustl\n" +
        "    platform: 12\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, x86 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-stlport-platform-12.zip\n" +
        "    sha256: b562331de4d7110349ec6ca2c7c888579f2bb56be095afce4671531809b2a894\n" +
        "    size: 2676242\n" +
        "    runtime: stlport\n" +
        "    platform: 12\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, x86 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-cxx-platform-21.zip\n" +
        "    sha256: 54ee95133dbddd4e2d76c572c0f4591aa4d7820f96f52566906f244c05d8bd9c\n" +
        "    size: 4346280\n" +
        "    runtime: c++\n" +
        "    platform: 21\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, arm64-v8a, x86, x86_64 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-gnustl-platform-21.zip\n" +
        "    sha256: da9600b63f03dc9c11ac5b7c234212e16686e0b6874206626bc65f60f230f1af\n" +
        "    size: 4346366\n" +
        "    runtime: gnustl\n" +
        "    platform: 21\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, arm64-v8a, x86, x86_64 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-stlport-platform-21.zip\n" +
        "    sha256: f2876bf59b2624b9adc44fd7758bee15fd0d782ddb6049e9e384e0a2b7a7c03f\n" +
        "    size: 4346303\n" +
        "    runtime: stlport\n" +
        "    platform: 21\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, arm64-v8a, x86, x86_64 ]\n" +
        "iOS:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-iPhone.zip\n" +
        "    sha256: 7126dfb6282a53c16cd648fcfca3bd8c3ac306def1b5bc8cefb3b82b459fca80\n" +
        "    size: 1293737\n" +
        "    platform: iPhoneOS\n" +
        "    sdk: 10.2\n" +
        "    architecture: armv7s\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-simulator.zip\n" +
        "    sha256: 266f16031afd5aef8adf19394fdcf946cb6a28d19a41b7db1ff87487733b91df\n" +
        "    size: 546921\n" +
        "    platform: iPhoneSimulator\n" +
        "    sdk: 10.2\n" +
        "    architecture: i386\n" +
        "example: |\n" +
        "  #include <sqlite3.h>\n" +
        "  void test() {\n" +
        "    sqlite3_initialize();\n" +
        "  }");
  }

  public static ResolvedManifest sqliteiOS() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: sqlite\n" +
        "  version: 3.16.2-rev33\n" +
        "iOS:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-iPhoneOS-architecture-armv7-sdk-9.3.zip\n" +
        "    sha256: c28410af1bcc42e177a141082325efe0a0fa35a4c42fee786229c8f793009253\n" +
        "    size: 514133\n" +
        "    platform: iPhoneOS\n" +
        "    architecture: armv7\n" +
        "    sdk: 9.3\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-iPhoneOS-architecture-armv7s-sdk-9.3.zip\n" +
        "    sha256: 4c213577d7f2a77942b5459b5169e64c6ee2d38c806fb1f44c29fb6b6c7d535d\n" +
        "    size: 514254\n" +
        "    platform: iPhoneOS\n" +
        "    architecture: armv7s\n" +
        "    sdk: 9.3\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-iPhoneOS-architecture-arm64-sdk-9.3.zip\n" +
        "    sha256: f7a2b0c1b8e532615e1e6e1151a4e182bad08cd9ae10a7b9aaa03d55a42f7bab\n" +
        "    size: 529339\n" +
        "    platform: iPhoneOS\n" +
        "    architecture: arm64\n" +
        "    sdk: 9.3\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-iPhoneSimulator-architecture-i386-sdk-9.3.zip\n" +
        "    sha256: 767cfc5379304f67aa27af8dc1b16d2372b65ba2829e22305d8d81858caa05a0\n" +
        "    size: 555306\n" +
        "    platform: iPhoneSimulator\n" +
        "    architecture: i386\n" +
        "    sdk: 9.3\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-iPhoneSimulator-architecture-x86_64-sdk-9.3.zip\n" +
        "    sha256: 7a76243c4ddd006f0105002ea5f6dd1784fb2f3231f793a00a7905661806c1ff\n" +
        "    size: 547071\n" +
        "    platform: iPhoneSimulator\n" +
        "    architecture: x86_64\n" +
        "    sdk: 9.3\n" +
        "example: |\n" +
        "  #include <sqlite3.h>\n" +
        "  void test() {\n" +
        "    sqlite3_initialize();\n" +
        "  }\n");
  }

  public static ResolvedManifest sqliteAndroid() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: sqlite\n" +
        "  version: 3.16.2-rev33\n" +
        "android:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-cxx-platform-12.zip\n" +
        "    sha256: 9604fa0c7fb7635075b31f9231455469c5498c95279840bddf476f98598f7fc9\n" +
        "    size: 2675756\n" +
        "    runtime: c++\n" +
        "    platform: 12\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, x86 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-gnustl-platform-12.zip\n" +
        "    sha256: 794945bb7f1e9e62ba7484e4f889d7d965d583a7a7c3c749a7f953f5ac966ec1\n" +
        "    size: 2675779\n" +
        "    runtime: gnustl\n" +
        "    platform: 12\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, x86 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-stlport-platform-12.zip\n" +
        "    sha256: 3df5b250e8d9429e0e0ee1e8fec571ace23678c05af1cc3ee09b4c94b08e453e\n" +
        "    size: 2675775\n" +
        "    runtime: stlport\n" +
        "    platform: 12\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, x86 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-cxx-platform-21.zip\n" +
        "    sha256: fcc699217930c5bfd0a5fb7240b0f3b24f459034dd1ed43d31245d18c428f0f4\n" +
        "    size: 4345764\n" +
        "    runtime: c++\n" +
        "    platform: 21\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, arm64-v8a, x86, x86_64 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-gnustl-platform-21.zip\n" +
        "    sha256: 5e80c6fe462398f89e7f421e95237e1426996b956fd4b089c32173999065d15e\n" +
        "    size: 4345771\n" +
        "    runtime: gnustl\n" +
        "    platform: 21\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, arm64-v8a, x86, x86_64 ]\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-stlport-platform-21.zip\n" +
        "    sha256: fd6dfe67a3dcf32d3989498a539eb16eb11fd992f1a9459a8c258dfca8279b0a\n" +
        "    size: 4345886\n" +
        "    runtime: stlport\n" +
        "    platform: 21\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, arm64-v8a, x86, x86_64 ]\n" +
        "example: |\n" +
        "  #include <sqlite3.h>\n" +
        "  void test() {\n" +
        "    sqlite3_initialize();\n" +
        "  }\n");
  }

  static ResolvedManifest emptyiOSArchive() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: sqlite\n" +
        "  version: 0.0.0\n" +
        "android:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-android-cxx-platform-12.zip\n" +
        "    sha256: 45a104d61786eaf163b3006aa989922c5c04b8e787073e1cbd60c7895943162c\n" +
        "    size: 2676245\n" +
        "    runtime: c++\n" +
        "    platform: 12\n" +
        "    ndk: r13b\n" +
        "    abis: [ armeabi, armeabi-v7a, x86 ]\n" +
        "iOS:\n" +
        "  archives:\n" +
        "example: |\n" +
        "  #include <sqlite3.h>\n" +
        "  void test() {\n" +
        "    sqlite3_initialize();\n" +
        "  }");
  }

  static ResolvedManifest emptyAndroidArchive() throws MalformedURLException {
    return getResolvedManifest("coordinate:\n" +
        "  groupId: com.github.jomof\n" +
        "  artifactId: sqlite\n" +
        "  version: 0.0.0\n" +
        "iOS:\n" +
        "  archives:\n" +
        "  - lib: libsqlite.a\n" +
        "    file: sqlite-ios-platform-iPhone.zip\n" +
        "    sha256: 45a104d61786eaf163b3006aa989922c5c04b8e787073e1cbd60c7895943162c\n" +
        "    platform: iPhoneOS\n" +
        "    size: 2676245\n" +
        "    sdk: 10.2\n" +
        "    architecture: armv7\n" +
        "android:\n" +
        "  archives:\n" +
        "example: |\n" +
        "  #include <sqlite3.h>\n" +
        "  void test() {\n" +
        "    sqlite3_initialize();\n" +
        "  }");
  }

  static public List<NamedManifest> all() throws InvocationTargetException, IllegalAccessException {
    List<NamedManifest> result = new ArrayList<>();
    for (Method method : ResolvedManifests.class.getMethods()) {
      if (!Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      if (method.getReturnType() != ResolvedManifest.class) {
        continue;
      }
      if (method.getParameterTypes().length != 0) {
        continue;
      }
      ResolvedManifest resolved = (ResolvedManifest) method.invoke(null);
      result.add(new NamedManifest(method.getName(), resolved));
    }
    return result;
  }

  public static class NamedManifest {
    final public String name;
    final public ResolvedManifest resolved;

    NamedManifest(String name, ResolvedManifest resolved) {
      this.name = name;
      this.resolved = resolved;
    }
  }
}
