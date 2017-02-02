package com.jomofisher.cdep;

import static com.google.common.truth.Truth.assertThat;

import com.jomofisher.cdep.AST.FoundModuleExpression;
import com.jomofisher.cdep.AST.FunctionTable;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;

/**
 * Created by jomof on 1/31/17.
 */
public class TestFindModuleFunctionTableBuilder {

    @Test
    public void testSimple() throws IOException, URISyntaxException {
        ResolvedManifest resolved = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.33/cdep-manifest.yml");
        assertThat(resolved.manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.manifest.coordinate.version).isEqualTo("alpha-0.0.33");
        assertThat(resolved.manifest.android.length).isEqualTo(2);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTable table = builder.build();
        String zip = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86").archive.getPath();
        assertThat(zip).endsWith("cmakeify-android-c++_shared.zip");
    }

    // Test case where a manifest points to the same zip file multiple times (not allowed)
    @Test
    public void testMultipleZipReferences() throws IOException, URISyntaxException {
        ResolvedManifest resolved = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.32/cdep-manifest.yml");
        assertThat(resolved.manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.manifest.coordinate.version).isEqualTo("alpha-0.0.32");
        assertThat(resolved.manifest.android.length).isEqualTo(208);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);

        try {
            builder.build();
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("Module 'com.github.jomof:cmakeify:alpha-0.0.32' contains "
                + "multiple references to the same zip file: cmakeify-android-cmake-3.7.1-r13b-4.9"
                + "-platform-9.zip");
            return;
        }
        throw new RuntimeException("Expected an error");
    }

    @Test
    public void testCheckPlatformSwitch() throws IOException, URISyntaxException {
        ResolvedManifest resolved = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.34/cdep-manifest.yml");
        assertThat(resolved.manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.manifest.coordinate.version).isEqualTo("alpha-0.0.34");
        assertThat(resolved.manifest.android.length).isEqualTo(4);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTable table = builder.build();
        FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86").archive.getPath().contains("platform-21");
        FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "22",
            "c++_shared",
            "x86").archive.getPath().contains("platform-21");
        FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "20",
            "c++_shared",
            "x86").archive.getPath().contains("platform-9");
    }

    @Test
    public void testArchivePathIsFull() throws IOException, URISyntaxException {
        ResolvedManifest resolved = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.35/cdep-manifest.yml");

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTable table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.35/"
                + "cmakeify-android-cxx_shared-platform-21.zip");
        WebUtils.pingUrl(found.archive);
    }

    @Test
    public void testFoundIncludeAndLib() throws IOException, URISyntaxException {
        ResolvedManifest resolved = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.35/cdep-manifest.yml");

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTable table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include.toString()).isEqualTo("include");
        assertThat(found.lib.toString()).isEqualTo("lib");
        WebUtils.pingUrl(found.archive);
    }

    @Test
    public void testHeaderOnly() throws IOException, URISyntaxException {
        ResolvedManifest resolved = Resolver.resolveAny(
            "https://github.com/jomof/cdep-boost/releases/download/1.0.63-rev6/cdep-manifest.yml");

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTable table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include.toString()).isEqualTo("boost_1_63_0/boost");
        assertThat(found.lib.toString()).isEqualTo("lib");
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/cdep-boost/releases/download/1.0.63-rev6/boost_1_63_0.zip");
        WebUtils.pingUrl(found.archive);
    }


    @Test
    public void testHeaderOnlyGitHubCoordinate() throws IOException, URISyntaxException {
        ResolvedManifest resolved = Resolver.resolveAny("com.github.jomof:cdep-boost:1.0.63-rev6");

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTable table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include.toString()).isEqualTo("boost_1_63_0/boost");
        assertThat(found.lib.toString()).isEqualTo("lib");
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/cdep-boost/releases/download/1.0.63-rev6/boost_1_63_0.zip");
        WebUtils.pingUrl(found.archive);
    }
}
