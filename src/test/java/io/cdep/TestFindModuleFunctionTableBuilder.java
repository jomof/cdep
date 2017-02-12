package io.cdep;

import static com.google.common.truth.Truth.assertThat;

import io.cdep.AST.finder.FoundModuleExpression;
import io.cdep.AST.finder.FunctionTableExpression;
import io.cdep.AST.service.ResolvedManifest;
import io.cdep.model.Reference;
import io.cdep.service.GeneratorEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;

/**
 * Created by jomof on 1/31/17.
 */
public class TestFindModuleFunctionTableBuilder {

    final private GeneratorEnvironment environment = new GeneratorEnvironment(
        System.out,
        new File("./test-files/TestFindModuleFunctionTableBuilder/working"),
        null);

    private static Reference createReference(String compile) {
        return new Reference(compile);

    }

    @Test
    public void testSimple() throws IOException, URISyntaxException {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.59/cdep-manifest.yml"));
        assertThat(resolved.manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.manifest.coordinate.version).isEqualTo("alpha-0.0.59");
        assertThat(resolved.manifest.android.length).isEqualTo(4);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        String zip = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86").archive.getPath();
        assertThat(zip).endsWith("cmakeify-android-cxx_shared-platform-21.zip");
    }

    // Test case where a manifest points to the same zip file multiple times (not allowed)
    @Test
    public void testMultipleZipReferences() throws IOException, URISyntaxException {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.32/cdep-manifest.yml"));
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
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.59/cdep-manifest.yml"));
        assertThat(resolved.manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(resolved.manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(resolved.manifest.coordinate.version).isEqualTo("alpha-0.0.59");
        assertThat(resolved.manifest.android.length).isEqualTo(4);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
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
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.59/cdep-manifest.yml"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.59/"
                + "cmakeify-android-cxx_shared-platform-21.zip");
    }

    @Test
    public void testFoundIncludeAndLib() throws IOException, URISyntaxException {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/sqllite/releases/download/3.16.2-rev11/cdep-manifest.yml"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include).isEqualTo("include");
        assertThat(found.libraryName).isEqualTo("libsqllite.so");
    }

    @Test
    public void testHeaderOnly() throws IOException, URISyntaxException {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "https://github.com/jomof/cdep-boost/releases/download/1.0.63-rev6/cdep-manifest.yml"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include.toString()).isEqualTo("boost_1_63_0/boost");
        assertThat(found.libraryName).isNull();
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/cdep-boost/releases/download/1.0.63-rev6/boost_1_63_0.zip");
    }


    @Test
    public void testHeaderOnlyGitHubCoordinate() throws IOException, URISyntaxException {
        ResolvedManifest resolved = environment.resolveAny(createReference(
            "com.github.jomof:boost:1.0.63-rev9"));

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(resolved);
        FunctionTableExpression table = builder.build();
        FoundModuleExpression found = FindModuleInterpreter.find(table,
            resolved.manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(found.include).isEqualTo("boost_1_63_0");
        assertThat(found.libraryName).isNull();
        assertThat(found.archive.toString()).isEqualTo(
            "https://github.com/jomof/boost/releases/download/1.0.63-rev9/boost_1_63_0.zip");
    }
}
