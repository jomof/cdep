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

import io.cdep.cdep.ast.finder.FoundModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.yml.cdep.Dependency;
import org.junit.Test;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

public class TestFindModuleFunctionTableBuilder {

    final private GeneratorEnvironment environment = new GeneratorEnvironment(
        System.out,
        new File("./test-files/TestFindModuleFunctionTableBuilder/working"),
        null);

    private static Dependency createReference(String compile) {
        return new Dependency(compile);

    }

    @Test
    public void testSimple() throws Exception {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.61/cdep-manifest.yml"),
            false);
        assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("0.0.61");
        assertThat(resolved.cdepManifestYml.android.length).isEqualTo(4);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        String zip = FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86").archive.getPath();
        assertThat(zip).endsWith("cmakeify-android-cxx_shared-platform-21.zip");
    }

    @Test
    public void testCheckPlatformSwitch() throws Exception {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.61/cdep-manifest.yml"),
            false);
        assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("0.0.61");
        assertThat(resolved.cdepManifestYml.android.length).isEqualTo(4);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86").archive.getPath().contains("platform-21");
        FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "22",
            "c++_shared",
            "x86").archive.getPath().contains("platform-21");
        FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "20",
            "c++_shared",
            "x86").archive.getPath().contains("platform-9");
    }

    @Test
    public void testArchivePathIsFull() throws Exception {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.61/cdep-manifest.yml"),
            false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/cmakeify/releases/download/0.0.61/"
                + "cmakeify-android-cxx_shared-platform-21.zip");
    }

    @Test
    public void testFoundIncludeAndLib() throws Exception {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/sqllite/releases/download/3.16.2-rev11/cdep-manifest.yml"),
            false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include).isEqualTo("include");
        assertThat(found.libraryName).isEqualTo("libsqllite.so");
    }

    @Test
    public void testHeaderOnly() throws Exception {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/boost/releases/download/1.0.63-rev10/cdep-manifest.yml"),
            false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include.toString()).isEqualTo("boost_1_63_0");
        assertThat(found.libraryName).isNull();
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/boost/releases/download/1.0.63-rev10/boost_1_63_0.zip");
    }


    @Test
    public void testHeaderOnlyGitHubCoordinate() throws Exception {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "com.github.jomof:boost:1.0.63-rev10"), false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include).isEqualTo("boost_1_63_0");
        assertThat(found.libraryName).isNull();
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/boost/releases/download/1.0.63-rev10/boost_1_63_0.zip");
    }
}
