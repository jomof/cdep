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
import io.cdep.cdep.utils.ExpressionUtils;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import org.junit.Test;

import java.io.File;

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
        System.out.printf(CreateStringVisitor.convert(table));
        String zip = FindModuleInterpreter.findAndroid(table,
                resolved.cdepManifestYml.coordinate,
                "Android",
                "21",
                "c++_shared",
                "x86").archives[0].file.getPath();
        assertThat(zip).endsWith("cmakeify-android-platform-21.zip");
        new CMakeGenerator(environment).generate(table);
    }

    @Test
    public void testTinyiOS() throws Exception {
        ResolvedManifest resolved = ResolvedManifests.emptyAndroidArchive();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        System.out.printf(CreateStringVisitor.convert(table));
        String zip = FindModuleInterpreter.findiOS(table,
                resolved.cdepManifestYml.coordinate,
                "Darwin",
                new String[]{"armv7s"},
                "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS10.2.sdk")
                .archives[0].file.getPath();
        assertThat(zip).endsWith("sqlite-ios-platform-iPhone.zip");
        new CMakeGenerator(environment).generate(table);
    }

    @Test
    public void testTinyAndroid() throws Exception {
        ResolvedManifest resolved = ResolvedManifests.emptyiOSArchive();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        System.out.printf(CreateStringVisitor.convert(table));
        FoundAndroidModuleExpression found = FindModuleInterpreter.findAndroid(table,
                resolved.cdepManifestYml.coordinate,
                "Android",
                "21",
                "c++_shared",
                "x86");
        assertThat(found.archives[0].file.toString()).contains("sqlite-android-cxx-platform-12.zip");
        new CMakeGenerator(environment).generate(table);
    }

    @Test
    public void testiOS() throws Exception {
        ResolvedManifest resolved = ResolvedManifests.sqlite();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();

        System.out.printf(table.toString());
        String zip = FindModuleInterpreter.findiOS(table,
                resolved.cdepManifestYml.coordinate,
                "Darwin",
                new String[]{"armv7s"},
                "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS10.2.sdk")
                .archives[0].file.getPath();
        assertThat(zip).endsWith("sqlite-ios-platform-iPhone.zip");

        zip = FindModuleInterpreter.findiOS(table,
                resolved.cdepManifestYml.coordinate,
                "Darwin",
                new String[]{"x86"},
                "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator10.2.sdk")
                .archives[0].file.getPath();
        assertThat(zip).endsWith("sqlite-ios-platform-simulator.zip");

        new CMakeGenerator(environment).generate(table);
        ExpressionUtils.getAllFoundModuleExpressions(table);
    }

    @Test
    public void testiOSNonSpecificSDK() throws Exception {
        ResolvedManifest resolved = ResolvedManifests.sqlite();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        String zip = FindModuleInterpreter.findiOS(table,
                resolved.cdepManifestYml.coordinate,
                "Darwin",
                new String[]{"armv7s"},
                "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk")
                .archives[0].file.getPath();
        assertThat(zip).endsWith("sqlite-ios-platform-iPhone.zip");

        zip = FindModuleInterpreter.findiOS(table,
                resolved.cdepManifestYml.coordinate,
                "Darwin",
                new String[]{"x86"},
                "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk")
                .archives[0].file.getPath();
        assertThat(zip).endsWith("sqlite-ios-platform-simulator.zip");

        new CMakeGenerator(environment).generate(table);
        ExpressionUtils.getAllFoundModuleExpressions(table);
    }

    @Test
    public void testiOSUnknownPlatform() throws Exception {
        ResolvedManifest resolved = ResolvedManifests.sqlite();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        try {
            String zip = FindModuleInterpreter.findiOS(table,
                    resolved.cdepManifestYml.coordinate,
                    "Darwin",
                    new String[]{"armv7s"},
                    "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPad10.2.sdk")
                    .archives[0].file.getPath();
            fail("Expected exception");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage(
                    "OSX SDK 'iPad10.2' is not supported by module 'com.github.jomof:sqlite:0.0.0'. Supported: iPhoneOS10.2 iPhoneSimulator10.2 ");
        }
    }

    @Test
    public void testEmptyiOSArchive() throws Exception {
        ResolvedManifest resolved = ResolvedManifests.emptyiOSArchive();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
    }

    @Test
    public void testEmptyAndroidArchive() throws Exception {
        ResolvedManifest resolved = ResolvedManifests.emptyAndroidArchive();
        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
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
        assertThat(FindModuleInterpreter.findAndroid(table,
                resolved.cdepManifestYml.coordinate,
                "Android",
                "21",
                "c++_shared",
                "x86").archives[0].file.getPath())
                .contains("platform-21");
        assertThat(FindModuleInterpreter.findAndroid(table,
                resolved.cdepManifestYml.coordinate,
                "Android",
                "22",
                "c++_shared",
                "x86").archives[0].file.getPath()).contains("platform-21");
        assertThat(FindModuleInterpreter.findAndroid(table,
                resolved.cdepManifestYml.coordinate,
                "Android",
                "20",
                "c++_shared",
                "x86").archives[0].file.getPath()).contains("platform-9");
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
        assertThat(found.archives[0].libraryName).isNull();
        assertThat(found.archives[0].file.toString()).isEqualTo(
                "https://github.com/jomof/boost/releases/download/1.0.63-rev18/boost_1_63_0.zip");
    }
}
