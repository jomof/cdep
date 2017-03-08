/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package io.cdep.cdep;

import io.cdep.cdep.ast.finder.FoundAndroidModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.generator.CMakeGenerator;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.resolver.ResolvedManifest;
import io.cdep.cdep.resolver.Resolver;
import io.cdep.cdep.utils.CDepManifestYmlUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import io.cdep.cdep.yml.cdepmanifest.CDepManifestYml;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class TestFindModuleFunctionTableBuilder {

    private final GeneratorEnvironment environment = new GeneratorEnvironment(
            System.out,
            new File("./test-files/TestFindModuleFunctionTableBuilder/working"),
            null,
            false);
    final private Resolver resolver = new Resolver(environment);

    private static SoftNameDependency createReference(String compile) {
        return new SoftNameDependency(compile);

    }

    private static ResolvedManifest getSqllite() throws MalformedURLException {
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
                "    platform: iPhone\n" +
                "  - lib: libsqlite.a\n" +
                "    file: sqlite-ios-platform-simulator.zip\n" +
                "    sha256: 266f16031afd5aef8adf19394fdcf946cb6a28d19a41b7db1ff87487733b91df\n" +
                "    size: 546921\n" +
                "    platform: simulator\n" +
                "  - lib: libsqlite.a\n" +
                "    file: sqlite-ios-platform-simulator64.zip\n" +
                "    sha256: ae9cd54aa94422f482fdc55abbd07c1b673ed9ab48e7eda493325857bbe634ff\n" +
                "    size: 546924\n" +
                "    platform: simulator64\n" +
                "example: |\n" +
                "  #include <sqlite3.h>\n" +
                "  void test() {\n" +
                "    sqlite3_initialize();\n" +
                "  }";
        CDepManifestYml yml = CDepManifestYmlUtils.convertStringToManifest(manifest);
        return new ResolvedManifest(new URL("http://google.com/cdep-manifest.yml"), yml);
    }

    @Test
    public void testSimple() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.81/cdep-manifest.yml"));
        assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("0.0.81");
        assertThat(resolved.cdepManifestYml.android.archives.length).isEqualTo(2);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        String zip = FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86").archives[0].file.getPath();
        assertThat(zip).endsWith("cmakeify-android-platform-21.zip");
    }

    @Test
    public void testiOS() throws Exception {
        ResolvedManifest resolved = getSqllite();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        String zip = FindModuleInterpreter.findiOS(table,
                resolved.cdepManifestYml.coordinate,
                "Darwin",
                "iPhone").archives[0].file.getPath();
        assertThat(zip).endsWith("sqlite-ios-platform-iPhone.zip");

        zip = FindModuleInterpreter.findiOS(table,
                resolved.cdepManifestYml.coordinate,
                "Darwin",
                "simulator").archives[0].file.getPath();
        assertThat(zip).endsWith("sqlite-ios-platform-simulator.zip");

        new CMakeGenerator(environment).generate(table);
    }

    @Test
    public void testiOSUnknownPlatform() throws Exception {
        ResolvedManifest resolved = getSqllite();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        try {
            String zip = FindModuleInterpreter.findiOS(table,
                    resolved.cdepManifestYml.coordinate,
                    "Darwin",
                    "iPad").archives[0].file.getPath();
            fail("Expected exception");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("iOS platform 'iPad' is not supported by module " +
                    "'com.github.jomof:sqlite:0.0.0'. Supported: iPhone simulator simulator64 ");
        }
    }

    @Test
    public void testCheckPlatformSwitch() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.81/cdep-manifest.yml"));
        assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("0.0.81");
        assertThat(resolved.cdepManifestYml.android.archives.length).isEqualTo(2);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86").archives[0].file.getPath().contains("platform-21");
        FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "22",
            "c++_shared",
            "x86").archives[0].file.getPath().contains("platform-21");
        FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "20",
            "c++_shared",
            "x86").archives[0].file.getPath().contains("platform-9");
    }

    @Test
    public void testArchivePathIsFull() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.81/cdep-manifest.yml"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundAndroidModuleExpression found = FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.archives[0].file.toString()).isEqualTo(
            "https://github.com/jomof/cmakeify/releases/download/0.0.81/"
                + "cmakeify-android-platform-21.zip");
    }

    @Test
    public void testFoundIncludeAndLib() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/sqlite/releases/download/3.16.2-rev25/cdep-manifest.yml"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundAndroidModuleExpression found = FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.archives[0].include).isEqualTo("include");
        assertThat(found.archives[0].libraryName).isEqualTo("libsqlite.a");
    }

    @Test
    public void testHeaderOnly() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/boost/releases/download/1.0.63-rev18/cdep-manifest.yml"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundAndroidModuleExpression found = FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.archives[0].include.toString()).isEqualTo("boost_1_63_0");
        assertThat(found.archives[0].libraryName).isNull();
        assertThat(found.archives[0].file.toString()).isEqualTo(
            "https://github.com/jomof/boost/releases/download/1.0.63-rev18/boost_1_63_0.zip");
    }

    @Test
    public void testHeaderOnlyGitHubCoordinate() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "com.github.jomof:boost:1.0.63-rev18"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundAndroidModuleExpression found = FindModuleInterpreter.findAndroid(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.archives[0].include).isEqualTo("boost_1_63_0");
        assertThat(found.archives[0].libraryName).isNull();
        assertThat(found.archives[0].file.toString()).isEqualTo(
            "https://github.com/jomof/boost/releases/download/1.0.63-rev18/boost_1_63_0.zip");
    }
}
