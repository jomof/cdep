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

import io.cdep.cdep.Coordinate;
import io.cdep.cdep.ast.finder.AbortExpression;
import io.cdep.cdep.ast.finder.CaseExpression;
import io.cdep.cdep.ast.finder.Expression;
import io.cdep.cdep.ast.finder.FindModuleExpression;
import io.cdep.cdep.ast.finder.FoundAndroidModuleExpression;
import io.cdep.cdep.ast.finder.FoundiOSModuleExpression;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.IfGreaterThanOrEqualExpression;
import io.cdep.cdep.ast.finder.LongConstantExpression;
import io.cdep.cdep.ast.finder.ModuleArchive;
import io.cdep.cdep.ast.finder.ParameterExpression;
import io.cdep.cdep.ast.finder.StringExpression;
import io.cdep.cdep.ast.finder.iOSPlatformExpression;
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

        for (FindModuleExpression findFunction : table.findFunctions.values()) {
            generateFindAppender(0, findFunction, findFunction, sb);
        }

        sb.append("\nfunction(add_all_cdep_dependencies target)\n");
        for (FindModuleExpression findFunction : table.findFunctions.values()) {
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

    private void generateFindAppender(
            int indent,
            FindModuleExpression signature,
            Expression expression,
            StringBuilder sb) {

        String prefix = new String(new char[indent * 2]).replace('\0', ' ');

        String upperArtifactID = signature.coordinate.artifactId.toUpperCase()
                .replace("-", "_")
                .replace("/", "_");

        if (expression instanceof FindModuleExpression) {
            FindModuleExpression specific = (FindModuleExpression) expression;
            sb.append("\n###\n");
            sb.append(String.format("### Add dependency for CDep module: %s\n",
                    specific.coordinate.toString()));
            sb.append("###\n");
            String coordinateVar = String.format("%s_CDEP_COORDINATE", upperArtifactID);
            sb.append(String.format("%sif(%s)\n", prefix, coordinateVar));
            sb.append(String.format("%s  message(FATAL_ERROR \"CDep module '$(%s}' was already defined\")\n", prefix, coordinateVar));
            sb.append(String.format("%sendif(%s)\n", prefix, coordinateVar));
            sb.append(String.format("%sSET(%s \"%s\")\n", prefix, coordinateVar, specific.coordinate));
            String appenderFunctionName = getAddDependencyFunctionName(signature.coordinate);
            sb.append("function({appenderFunctionName} target)\n".replace("{appenderFunctionName}", appenderFunctionName));
            generateFindAppender(indent + 1, signature, specific.expression, sb);
            sb.append("endfunction({appenderFunctionName})\n".replace("{appenderFunctionName}", appenderFunctionName));
            return;
        } else if (expression instanceof CaseExpression) {
            CaseExpression specific = (CaseExpression) expression;
            StringBuilder varBuilder = new StringBuilder();
            generateFindAppender(indent, signature, specific.var, varBuilder);
            String var = varBuilder.toString();
            boolean first = true;
            for (Expression matchValueExpression : specific.cases.keySet()) {
                String matchValue = getStringValue(matchValueExpression);
                if (first) {
                    sb.append(String.format("%sif(%s STREQUAL \"%s\")\n", prefix, var, matchValue));
                    first = false;
                } else {
                    sb.append(String.format("%selseif(%s STREQUAL \"%s\")\n", prefix, var, matchValue));
                }
                generateFindAppender(indent + 1, signature, specific.cases.get(matchValueExpression), sb);
            }
            sb.append(String.format("%selse()\n", prefix));
            generateFindAppender(indent + 1, signature, specific.defaultCase, sb);
            sb.append(String.format("%sendif()\n", prefix));

            return;
        } else if (expression instanceof IfGreaterThanOrEqualExpression) {
            IfGreaterThanOrEqualExpression specific = (IfGreaterThanOrEqualExpression) expression;
            StringBuilder varBuilder = new StringBuilder();
            generateFindAppender(indent, signature, specific.value, varBuilder);
            String var = varBuilder.toString();
            StringBuilder compareToBuilder = new StringBuilder();
            generateFindAppender(indent, signature, specific.compareTo, compareToBuilder);
            String compareTo = compareToBuilder.toString();
            sb.append(String.format("%sif((%s GREATER %s) OR (%s EQUAL %s))\n",
                    prefix, var, compareTo, var, compareTo));
            generateFindAppender(indent + 1, signature, specific.trueExpression, sb);
            sb.append(String.format("%selse()\n", prefix));
            generateFindAppender(indent + 1, signature, specific.falseExpression, sb);
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
            if (specific == signature.androidTargetAbi) {
                sb.append("CDEP_DETERMINED_ANDROID_ABI");
                return;
            }
            if (specific == signature.iOSPlatform) {
                sb.append("IOS_PLATFORM");
                return;
            }
            throw new RuntimeException(specific.name);
        } else if (expression instanceof LongConstantExpression) {
            LongConstantExpression specific = (LongConstantExpression) expression;
            sb.append(specific.value.toString());
            return;
        } else if (expression instanceof FoundAndroidModuleExpression) {
            FoundAndroidModuleExpression specific = (FoundAndroidModuleExpression) expression;
            assert specific.coordinate.artifactId != null;
            for (Coordinate dependency : specific.dependencies) {
                sb.append(String.format("%s%s(${target})\n", prefix, getAddDependencyFunctionName(dependency)));
            }
            for (ModuleArchive archive : specific.archives) {
                File exploded = environment
                        .getLocalUnzipFolder(specific.coordinate, archive.file);
                sb.append(String.format("%starget_include_directories(${target} PRIVATE \"%s\")\n",
                        prefix, new File(exploded, archive.include).toString().replace("\\", "\\\\")));
                String libFolder = new File(exploded, "lib").toString().replace("\\", "\\\\");

                if (archive.libraryName != null && archive.libraryName.length() > 0) {
                    sb.append(String.format(
                            "%starget_link_libraries(${target} \"%s%s${CDEP_DETERMINED_ANDROID_ABI}%s%s\")\n",
                            prefix, libFolder, slash, slash, archive.libraryName));
                }
            }
            return;
        } else if (expression instanceof FoundiOSModuleExpression) {
            FoundiOSModuleExpression specific = (FoundiOSModuleExpression) expression;
            assert specific.coordinate.artifactId != null;
            for (Coordinate dependency : specific.dependencies) {
                sb.append(String.format("%s%s(${target})\n", prefix, getAddDependencyFunctionName(dependency)));
            }
            for (ModuleArchive archive : specific.archives) {
                File exploded = environment
                        .getLocalUnzipFolder(specific.coordinate, archive.file);
                sb.append(String.format("%starget_include_directories(${target} PRIVATE \"%s\")\n",
                        prefix, new File(exploded, archive.include).toString().replace("\\", "\\\\")));
                String libFolder = new File(exploded, "lib").toString().replace("\\", "\\\\");

                if (archive.libraryName != null && archive.libraryName.length() > 0) {
                    sb.append(String.format(
                            "%starget_link_libraries(${target} \"%s%s%s\")\n",
                            prefix, libFolder, slash, archive.libraryName));
                }
            }
            return;
        } else if (expression instanceof AbortExpression) {
            AbortExpression specific = (AbortExpression) expression;
            Object parms[] = new String[specific.parameters.length];
            for (int i = 0; i < parms.length; ++i) {
                StringBuilder argBuilder = new StringBuilder();
                generateFindAppender(0, signature, specific.parameters[i], argBuilder);
                parms[i] = String.format("${%s}", argBuilder.toString());
            }
            String message = String.format(specific.message, parms);
            sb.append(String.format("%smessage(FATAL_ERROR \"%s\")\n", prefix, message));
            return;
        }

        throw new RuntimeException(expression.toString());
    }

    private String getStringValue(Expression expression) {
        if (expression instanceof StringExpression) {
            return ((StringExpression) expression).value;
        }
        if (expression instanceof iOSPlatformExpression) {
            iOSPlatformExpression specific = (iOSPlatformExpression) expression;
            switch (specific.platform) {
              case iPhoneOS:
                    return "OS";
              case iPhoneSimulator:
                    return "SIMULATOR";
                default:
                    throw new RuntimeException(specific.toString());
            }
        }
        throw new RuntimeException(expression.toString());
    }

    String getAddDependencyFunctionName(Coordinate coordinate) {
        return String.format("add_cdep_%s_dependency", coordinate.artifactId)
                .replace("-", "_")
                .replace("/", "_");
    }
}
