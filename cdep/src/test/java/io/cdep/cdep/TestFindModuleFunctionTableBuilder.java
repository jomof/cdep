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

import static com.google.common.truth.Truth.assertThat;

import io.cdep.cdep.ast.finder.FoundModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.service.ResolvedManifest;
import io.cdep.cdep.generator.GeneratorEnvironment;
import io.cdep.cdep.resolver.ResolutionScopeResolver;
import io.cdep.cdep.yml.cdep.SoftNameDependency;
import java.io.File;
import org.junit.Test;

public class TestFindModuleFunctionTableBuilder {

    final private ResolutionScopeResolver resolver = new ResolutionScopeResolver(
        new GeneratorEnvironment(
            System.out,
            new File("./test-files/TestFindModuleFunctionTableBuilder/working"),
            null,
            false));

    private static SoftNameDependency createReference(String compile) {
        return new SoftNameDependency(compile);

    }

    @Test
    public void testSimple() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.81/cdep-manifest.yml"),
            false);
        assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("0.0.81");
        assertThat(resolved.cdepManifestYml.android.archives.length).isEqualTo(2);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        String zip = FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86").archives[0].file.getPath();
        assertThat(zip).endsWith("cmakeify-android-platform-21.zip");
    }

    @Test
    public void testCheckPlatformSwitch() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.81/cdep-manifest.yml"),
            false);
        assertThat(resolved.cdepManifestYml.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.cdepManifestYml.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.cdepManifestYml.coordinate.version).isEqualTo("0.0.81");
        assertThat(resolved.cdepManifestYml.android.archives.length).isEqualTo(2);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "21",
            "c++_shared",
            "x86").archives[0].file.getPath().contains("platform-21");
        FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "22",
            "c++_shared",
            "x86").archives[0].file.getPath().contains("platform-21");
        FindModuleInterpreter.find(table,
            resolved.cdepManifestYml.coordinate,
            "Android",
            "20",
            "c++_shared",
            "x86").archives[0].file.getPath().contains("platform-9");
    }

    @Test
    public void testArchivePathIsFull() throws Exception {
        ResolvedManifest resolved = resolver.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/0.0.81/cdep-manifest.yml"),
            false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
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
            "https://github.com/jomof/sqlite/releases/download/3.16.2-rev25/cdep-manifest.yml"),
            false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
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
            "https://github.com/jomof/boost/releases/download/1.0.63-rev18/cdep-manifest.yml"),
            false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
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
            "com.github.jomof:boost:1.0.63-rev18"), false);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
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
