package io.cdep.cdep;

import io.cdep.cdep.resolver.ResolvedManifest;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;

import java.net.MalformedURLException;
import java.net.URL;


public class ResolvedManifests {

  static ResolvedManifest sqlite() throws MalformedURLException {
    String manifest = "coordinate:\n" +
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
        "  }";
    CDepManifestYml yml = CDepManifestYmlUtils.convertStringToManifest(manifest);
    CDepManifestYmlUtils.checkManifestSanity(yml);
    return new ResolvedManifest(new URL("http://google.com/cdep-manifest.yml"), yml);
  }

  static ResolvedManifest emptyiOSArchive() throws MalformedURLException {
    String manifest = "coordinate:\n" +
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
        "  }";
    CDepManifestYml yml = CDepManifestYmlUtils.convertStringToManifest(manifest);
    CDepManifestYmlUtils.checkManifestSanity(yml);
    return new ResolvedManifest(new URL("http://google.com/cdep-manifest.yml"), yml);
  }

  static ResolvedManifest emptyAndroidArchive() throws MalformedURLException {
    String manifest = "coordinate:\n" +
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
        "  }";
    CDepManifestYml yml = CDepManifestYmlUtils.convertStringToManifest(manifest);
    CDepManifestYmlUtils.checkManifestSanity(yml);
    return new ResolvedManifest(new URL("http://google.com/cdep-manifest.yml"), yml);
  }
}
