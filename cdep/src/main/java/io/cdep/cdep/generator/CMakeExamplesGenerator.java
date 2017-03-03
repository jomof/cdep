package io.cdep.cdep.generator;

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ast.finder.ExampleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class CMakeExamplesGenerator {

    final private GeneratorEnvironment environment;
    final private CMakeGenerator cmake;

    public CMakeExamplesGenerator(GeneratorEnvironment environment) {
        this.environment = environment;
        this.cmake = new CMakeGenerator(environment);
    }

    public void generate(FunctionTableExpression table) throws IOException {
        StringBuilder root = new StringBuilder();
        root.append("cmake_minimum_required(VERSION 3.7)\n");
        for (Coordinate coordinate : table.examples.keySet()) {
            File exampleFolder = getExampleFolder(coordinate);
            ExampleExpression example = table.examples.get(coordinate);
            exampleFolder.mkdirs();
            String artifact = coordinate.artifactId.replace("/", "_");
            String sourceName = artifact + ".cpp";
            File exampleSourceFile = new File(exampleFolder, sourceName);
            environment.out.printf("Generating %s\n", exampleSourceFile);
            FileUtils.writeTextToFile(exampleSourceFile, example.sourceCode);
            File exampleCMakeListsFile = new File(exampleFolder, "CMakeLists.txt");
            String cmakeLists =
                    "cmake_minimum_required(VERSION 3.7)\n" +
                    "project({ARTIFACTID}_example_project)\n" +
                    "include(\"{MODULE}\")\n" +
                    "add_library({ARTIFACTID}_target SHARED {SOURCE})\n" +
                    "{ADDFUNCTION}({ARTIFACTID}_target)\n";
            cmakeLists = cmakeLists
                    .replace("{MODULE}", cmake.getCMakeConfigurationFile().getAbsolutePath())
                    .replace("{ARTIFACTID}", artifact)
                    .replace("{SOURCE}", sourceName)
                    .replace("{ADDFUNCTION}", cmake.getAddDependencyFunctionName(coordinate));
            environment.out.printf("Generating %s\n", exampleCMakeListsFile);
            FileUtils.writeTextToFile(exampleCMakeListsFile, cmakeLists);
            root.append(String.format("add_subdirectory(\"%s\")\r\n",
                    exampleCMakeListsFile.getParentFile().getAbsolutePath()));
        }
        File rootFile = new File(getExampleRootFolder(), "CMakeLists.txt");
        environment.out.printf("Generating %s\n", rootFile);
        FileUtils.writeTextToFile(rootFile, root.toString());

    }

    private File getExampleRootFolder() {
        File file = environment.examplesFolder;
        file = new File(file, "cmake");
        return file;
    }

    private File getExampleFolder(Coordinate coordinate) {
        File file = getExampleRootFolder();
        file = new File(file, coordinate.groupId);
        file = new File(file, coordinate.artifactId);
        file = new File(file, coordinate.version);
        return file;
    }
}