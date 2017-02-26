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
package io.cdep.cdep.generator;

import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.Coordinate;
import io.cdep.cdep.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class CMakeGenerator {
    final static private String CONFIG_FILE_NAME = "cdep-dependencies-config.cmake";

    final private GeneratorEnvironment environment;
    final private String slash;

    public CMakeGenerator(GeneratorEnvironment environment) {
        this.environment = environment;
        this.slash = File.separator.replace("\\", "\\\\");
    }

    public void generate(FunctionTableExpression table) throws IOException {
        // Generate CMake Find*.cmake files
        StringBuilder sb = new StringBuilder();
        sb.append("# GENERATED FILE. DO NOT EDIT.\n");
        sb.append("\n" +
            "# Choose between Anroid NDK Toolchain and CMake Android Toolchain\n" +
            "if(DEFINED CMAKE_ANDROID_STL_TYPE)\n" +
            "  set(CDEP_DETERMINED_ANDROID_RUNTIME ${CMAKE_ANDROID_STL_TYPE})\n" +
            "  set(CDEP_DETERMINED_ANDROID_ABI ${CMAKE_ANDROID_ARCH_ABI})\n" +
            "else()\n" +
            "  set(CDEP_DETERMINED_ANDROID_RUNTIME ${ANDROID_STL})\n" +
            "  set(CDEP_DETERMINED_ANDROID_ABI ${ANDROID_ABI})\n" +
            "endif()\n");

        for (FindModuleExpression findFunction : table.functions.values()) {
            generateFinderExpression(0, findFunction, findFunction, sb);
            // TODO: If two artifact IDs conflict then generate a Find*.cmake that emits an error
        }

        sb.append("\nfunction(add_all_cdep_dependencies target)\n");
        for (FindModuleExpression findFunction : table.functions.values()) {
            String function = getAddDependencyFunctionName(findFunction.coordinate);
            sb.append(String.format("  %s(${target})\n", function));
        }
        sb.append("endfunction(add_all_cdep_dependencies)\n");
        File file = getCMakeConfigurationFile();
        environment.out.printf("Generating %s\n", file);
        FileUtils.writeTextToFile(file, sb.toString());
    }

    File getCMakeConfigurationFile() {
        return new File(environment.modulesFolder, CONFIG_FILE_NAME);
    }

    private void generateFinderExpression(
        int indent,
        FindModuleExpression signature,
        Expression expression,
        StringBuilder sb) {

        String prefix = new String(new char[indent * 2]).replace('\0', ' ');

        String upperArtifactID = signature.coordinate.artifactId.toUpperCase().replace("-", "_");

        if (expression instanceof FindModuleExpression) {
            FindModuleExpression specific = (FindModuleExpression) expression;
            sb.append("\n###\n");
            sb.append(String.format("### FindModule for CDep module: %s\n",
                specific.coordinate.toString()));
            sb.append("###\n");

            generateFinderExpression(indent, signature, specific.expression, sb);
            String functionName = getAddDependencyFunctionName(specific.coordinate);
            sb.append(String.format("\nfunction(%s target)\n",functionName));
            sb.append(String.format("   target_include_directories(${target} PRIVATE ${%s_INCLUDE_DIRS})\n",
                    upperArtifactID));
            sb.append(String.format("   target_link_libraries(${target} ${%s_LIBRARIES})\n",upperArtifactID));
            sb.append(String.format("   if(%s_SHARED_LIBRARIES AND CMAKE_LIBRARY_OUTPUT_DIRECTORY)\n",upperArtifactID));
            sb.append(String.format("     add_custom_command(TARGET ${target} " +
                    "POST_BUILD COMMAND ${CMAKE_COMMAND} -E " +
                    "copy ${%s_SHARED_LIBRARIES} ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})\n",upperArtifactID));
            sb.append(String.format("   endif(%s_SHARED_LIBRARIES AND CMAKE_LIBRARY_OUTPUT_DIRECTORY)\n",
                    upperArtifactID));
            sb.append(String.format("endfunction(%s)\n",functionName));
            return;
        } else if (expression instanceof CaseExpression) {
            CaseExpression specific = (CaseExpression) expression;
            StringBuilder varBuilder = new StringBuilder();
            generateFinderExpression(indent, signature, specific.var, varBuilder);
            String var = varBuilder.toString();
            boolean first = true;
            for (String matchValue : specific.cases.keySet()) {
                if (first) {
                    sb.append(String.format("%sif(%s STREQUAL \"%s\")\n", prefix, var, matchValue));
                    first = false;
                } else {
                    sb.append(String.format("%selseif(%s STREQUAL \"%s\")\n", prefix, var, matchValue));
                }
                generateFinderExpression(indent + 1, signature, specific.cases.get(matchValue), sb);
            }
            sb.append(String.format("%selse()\n", prefix));
            generateFinderExpression(indent + 1, signature, specific.defaultCase, sb);
            sb.append(String.format("%sendif()\n", prefix));

            return;
        } else if (expression instanceof IfGreaterThanOrEqualExpression) {
            IfGreaterThanOrEqualExpression specific = (IfGreaterThanOrEqualExpression) expression;
            StringBuilder varBuilder = new StringBuilder();
            generateFinderExpression(indent, signature, specific.value, varBuilder);
            String var = varBuilder.toString();
            StringBuilder compareToBuilder = new StringBuilder();
            generateFinderExpression(indent, signature, specific.compareTo, compareToBuilder);
            String compareTo = compareToBuilder.toString();
            sb.append(String.format("%sif((%s GREATER %s) OR (%s EQUAL %s))\n",
                prefix, var, compareTo, var, compareTo));
            generateFinderExpression(indent + 1, signature, specific.trueExpression, sb);
            sb.append(String.format("%selse()\n", prefix));
            generateFinderExpression(indent + 1, signature, specific.falseExpression, sb);
            sb.append(String.format("%sendif()\n", prefix));
            return;
        } else if (expression instanceof ParameterExpression) {
            ParameterExpression specific = (ParameterExpression) expression;
            if (specific == signature.targetPlatform) {
                sb.append("CMAKE_SYSTEM_NAME");
                return;
            }
            if (specific == signature.androidStlType) {
                sb.append("CDEP_DETERMINED_ANDROID_RUNTIME");
                return;
            }
            if (specific == signature.systemVersion) {
                sb.append("CMAKE_SYSTEM_VERSION");
                return;
            }
            throw new RuntimeException(specific.name);
        } else if (expression instanceof LongConstantExpression) {
            LongConstantExpression specific = (LongConstantExpression) expression;
            sb.append(specific.value.toString());
            return;
        } else if (expression instanceof FoundModuleExpression) {
            FoundModuleExpression specific = (FoundModuleExpression) expression;
            assert specific.coordinate.artifactId != null;
            for (ModuleArchive archive : specific.archives) {
                File exploded = environment
                    .getLocalUnzipFolder(specific.coordinate, archive.file);
                sb.append(String.format("%sset(%s_FOUND true)\n", prefix, upperArtifactID));
                sb.append(String.format("%sset(%s_INCLUDE_DIRS \"%s\")\n", prefix, upperArtifactID,
                    new File(exploded, specific.include).toString().replace("\\", "\\\\")));
                String libFolder = new File(exploded, "lib").toString().replace("\\", "\\\\");

                if (specific.libraryName != null && specific.libraryName.length() > 0) {
                    sb.append(String
                        .format("%sset(%s_LIBRARIES \"%s%s${CDEP_DETERMINED_ANDROID_ABI}%s%s\")\n",
                            prefix, upperArtifactID, libFolder, slash, slash,
                            specific.libraryName));
                    if (specific.libraryName.endsWith(".so")) {
                        sb.append(String.format(
                            "%sset(%s_SHARED_LIBRARIES \"%s%s${CDEP_DETERMINED_ANDROID_ABI}%s%s\")\n",
                            prefix, upperArtifactID, libFolder, slash, slash,
                            specific.libraryName));
                    }
                }
            }
            return;
        } else if (expression instanceof AbortExpression) {
            AbortExpression specific = (AbortExpression) expression;
            Object parms[] = new String[specific.parameters.length];
            for (int i = 0; i < parms.length; ++i) {
                StringBuilder argBuilder = new StringBuilder();
                generateFinderExpression(0, signature, specific.parameters[i], argBuilder);
                parms[i] = String.format("${%s}", argBuilder.toString());
            }
            String message = String.format(specific.message, parms);
            sb.append(String.format("%smessage(FATAL_ERROR \"%s\")\n", prefix, message));
            return;
        }

        throw new RuntimeException(expression.toString());
    }

    String getAddDependencyFunctionName(Coordinate coordinate) {
        return String.format("add_cdep_%s_dependency", coordinate.artifactId)
                .replace("-", "_")
                .replace("/", "_");
    }
}
