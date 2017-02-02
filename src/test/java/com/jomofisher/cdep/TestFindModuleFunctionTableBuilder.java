package com.jomofisher.cdep;

import static com.google.common.truth.Truth.assertThat;

import com.jomofisher.cdep.AST.FunctionTable;
import com.jomofisher.cdep.manifest.Manifest;
import java.io.IOException;
import org.junit.Test;

/**
 * Created by jomof on 1/31/17.
 */
public class TestFindModuleFunctionTableBuilder {

    @Test
    public void testSimple() throws IOException {
        Manifest manifest = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.33/cdep-manifest.yml");
        assertThat(manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(manifest.coordinate.version).isEqualTo("alpha-0.0.33");
        assertThat(manifest.android.length).isEqualTo(2);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(manifest);
        FunctionTable table = builder.build();
        String zip = FindModuleInterpreter.getZip(table,
            manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86");
        assertThat(zip).isEqualTo("cmakeify-android-c++_shared.zip");
    }

    // Test case where a manifest points to the same zip file multiple times (not allowed)
    @Test
    public void testMultipleZipReferences() throws IOException {
        Manifest manifest = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.32/cdep-manifest.yml");
        assertThat(manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(manifest.coordinate.version).isEqualTo("alpha-0.0.32");
        assertThat(manifest.android.length).isEqualTo(208);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(manifest);

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
    public void testCheckPlatformSwitch() throws IOException {
        Manifest manifest = Resolver.resolveAny(
            "https://github.com/jomof/cmakeify/releases/download/alpha-0.0.34/cdep-manifest.yml");
        assertThat(manifest.coordinate.groupId).isEqualTo("com.github.jomof");
        assertThat(manifest.coordinate.artifactId).isEqualTo("cmakeify");
        assertThat(manifest.coordinate.version).isEqualTo("alpha-0.0.34");
        assertThat(manifest.android.length).isEqualTo(4);

        FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
        builder.addManifest(manifest);
        FunctionTable table = builder.build();
        FindModuleInterpreter.getZip(table,
            manifest.coordinate.toString(),
            "Android",
            "21",
            "c++_shared",
            "x86").contains("platform-21");
        FindModuleInterpreter.getZip(table,
            manifest.coordinate.toString(),
            "Android",
            "22",
            "c++_shared",
            "x86").contains("platform-21");
        FindModuleInterpreter.getZip(table,
            manifest.coordinate.toString(),
            "Android",
            "20",
            "c++_shared",
            "x86").contains("platform-9");
    }

}
