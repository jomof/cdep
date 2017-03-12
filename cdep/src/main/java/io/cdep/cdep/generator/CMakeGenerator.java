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
import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CMakeGenerator {
    final static private String CONFIG_FILE_NAME = "cdep-dependencies-config.cmake";

    final private GeneratorEnvironment environment;
    final private Map<ParameterExpression, String> assignments = new HashMap<>();

    public CMakeGenerator(GeneratorEnvironment environment) {
        this.environment = environment;
    }

    public void generate(FunctionTableExpression table) throws IOException {
        // Generate CMake Find*.cmake files
        StringBuilder sb = new StringBuilder();
        sb.append("# GENERATED FILE. DO NOT EDIT.\n");

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

    private String getCMakePath(File file) {
        return file.toString()
                .replace("\\", "/");
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
            sb.append(String.format("%smessage(FATAL_ERROR \"CDep module '$(%s}' was already defined\")\n", prefix, coordinateVar));
            sb.append(String.format("%sendif(%s)\n", prefix, coordinateVar));
            sb.append(String.format("%sset(%s \"%s\")\n", prefix, coordinateVar, specific.coordinate));
            String appenderFunctionName = getAddDependencyFunctionName(signature.coordinate);
            sb.append("function({appenderFunctionName} target)\n".replace("{appenderFunctionName}", appenderFunctionName));
            sb.append(String.format("  set(CDEP_EXPLODED_ARCHIVE_FOLDER \"%s\")\n\n",
                    getCMakePath(environment.getPackageUnzipFolder(specific.coordinate))));
            sb.append("  # Choose between Anroid NDK Toolchain and CMake Android Toolchain\n" +
                    "  if(DEFINED CMAKE_ANDROID_STL_TYPE)\n" +
                    "    set(CDEP_DETERMINED_ANDROID_RUNTIME ${CMAKE_ANDROID_STL_TYPE})\n" +
                    "    set(CDEP_DETERMINED_ANDROID_ABI ${CMAKE_ANDROID_ARCH_ABI})\n" +
                    "  else()\n" +
                    "    set(CDEP_DETERMINED_ANDROID_RUNTIME ${ANDROID_STL})\n" +
                    "    set(CDEP_DETERMINED_ANDROID_ABI ${ANDROID_ABI})\n" +
                    "  endif()\n\n");
            generateFindAppender(indent + 1, signature, specific.expression, sb);
            sb.append("endfunction({appenderFunctionName})\n".replace("{appenderFunctionName}", appenderFunctionName));
            return;
        } else if (expression instanceof CaseExpression) {
            CaseExpression specific = (CaseExpression) expression;
            StringBuilder varBuilder = new StringBuilder();
            generateAssignments(prefix, signature, specific.var, sb, null);
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
        } else if (expression instanceof IfExpression) {
            IfExpression specific = (IfExpression) expression;
            StringBuilder varBuilder = new StringBuilder();
            generateAssignments(prefix, signature, specific.bool, sb, null);
            generateFindAppender(indent, signature, specific.bool, varBuilder);
            String var = varBuilder.toString();
            sb.append(String.format("%sif(%s)\n", prefix, var));
            generateFindAppender(indent + 1, signature, specific.trueExpression, sb);
            sb.append(String.format("%selse(%s)\n", prefix, var));
            generateFindAppender(indent + 1, signature, specific.falseExpression, sb);
            sb.append(String.format("%sendif(%s)\n", prefix, var));
            return;
        } else if (expression instanceof AssignmentExpression) {
            String identifier = assignments.get(expression);
            if (identifier != null) {
                sb.append(identifier);
                return;
            }
            throw new RuntimeException("Should have called generateAssignments before now");
        } else if (expression instanceof InvokeFunctionExpression) {
            InvokeFunctionExpression specific = (InvokeFunctionExpression) expression;
            String parms[] = new String[specific.parameters.length];
            for (int i = 0; i < specific.parameters.length; ++i) {
                Expression parm = specific.parameters[i];
                StringBuilder parmBuilder = new StringBuilder();
                generateFindAppender(indent + 1, signature, parm, parmBuilder);
                parms[i] = parmBuilder.toString();
            }
            // These are non-assignment function calls.
            if (specific.function == ExternalFunctionExpression.STRING_STARTSWITH) {
                sb.append(String.format("%s MATCHES \"$%s.*\"",
                        parms[0],
                        parms[1]));
            } else {
                throw new RuntimeException(specific.function.method.getName());
            }
            return;
        } else if (expression instanceof ParameterExpression) {
            ParameterExpression specific = (ParameterExpression) expression;
            sb.append(parameterName(signature, specific));
            return;
        } else if (expression instanceof LongExpression) {
            LongExpression specific = (LongExpression) expression;
            sb.append(specific.value.toString());
            return;
        } else if (expression instanceof IntegerExpression) {
            IntegerExpression specific = (IntegerExpression) expression;
            sb.append(specific.value);
            return;
        } else if (expression instanceof StringExpression) {
            StringExpression specific = (StringExpression) expression;
            sb.append(specific.value);
            return;
        } else if (expression instanceof FoundAndroidModuleExpression) {
            FoundAndroidModuleExpression specific = (FoundAndroidModuleExpression) expression;
            for (Coordinate dependency : specific.dependencies) {
                sb.append(String.format("%s%s(${target})\n", prefix, getAddDependencyFunctionName(dependency)));
            }
            for (ModuleArchive archive : specific.archives) {
                if (archive.fullIncludePath != null) {
                    generateAssignments(prefix, signature, archive.fullIncludePath, sb, null);
                }
                File relativeUnzipFolder = environment.getRelativeUnzipFolder(archive.file);
                sb.append(String.format("%sset(CDEP_EXPLODED_PACKAGE_FOLDER \"${CDEP_EXPLODED_ARCHIVE_FOLDER}/%s\")\n",
                        prefix, getCMakePath(relativeUnzipFolder)));
                sb.append(String.format(
                        "%starget_include_directories(${target} PRIVATE \"${CDEP_EXPLODED_PACKAGE_FOLDER}/%s\")\n",
                        prefix,
                        archive.include));

                if (archive.libraryName != null && archive.libraryName.length() > 0) {
                    sb.append(String.format(
                            "%starget_link_libraries(${target} \"${CDEP_EXPLODED_PACKAGE_FOLDER}/lib/${CDEP_DETERMINED_ANDROID_ABI}/%s\")\n",
                            prefix, archive.libraryName));
                }
            }
            return;
        } else if (expression instanceof FoundiOSModuleExpression) {
            FoundiOSModuleExpression specific = (FoundiOSModuleExpression) expression;
            for (Coordinate dependency : specific.dependencies) {
                sb.append(String.format("%s%s(${target})\n", prefix, getAddDependencyFunctionName(dependency)));
            }
            for (ModuleArchive archive : specific.archives) {
                File exploded = environment.getRelativeUnzipFolder(archive.file);
                sb.append(String.format("%starget_include_directories(${target} PRIVATE \"%s\")\n",
                        prefix, getCMakePath(new File(exploded, archive.include))));
                if (archive.libraryName != null && archive.libraryName.length() > 0) {
                    String libFolder = new File(exploded, "lib").toString().replace("\\", "\\\\");
                    sb.append(String.format(
                            "%starget_link_libraries(${target} \"%s/%s\")\n",
                            prefix, libFolder, archive.libraryName));
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
        } else if (expression instanceof AssignmentBlockExpression) {
            AssignmentBlockExpression specific = (AssignmentBlockExpression) expression;
            for (int i = 0; i < specific.assignments.size(); i++) {
                generateAssignments(prefix, signature, specific.assignments.get(i), sb, null);
            }
            generateFindAppender(indent, signature, specific.statement, sb);
            return;
        }

        throw new RuntimeException(expression.getClass().toString());
    }

    private String parameterName(FindModuleExpression signature, ParameterExpression expr) {
        if (expr == signature.targetPlatform) {
            return "${CMAKE_SYSTEM_NAME}";
        }
        if (expr == signature.androidStlType) {
            return "${CDEP_DETERMINED_ANDROID_RUNTIME}";
        }
        if (expr == signature.systemVersion) {
            return "${CMAKE_SYSTEM_VERSION}";
        }
        if (expr == signature.androidTargetAbi) {
            return "${CDEP_DETERMINED_ANDROID_ABI}";
        }
        if (expr == signature.osxSysroot) {
            return "${CMAKE_OSX_SYSROOT}";
        }
        if (expr == signature.cdepExplodedRoot) {
            return getCMakePath(environment.unzippedArchivesFolder);
        }

        throw new RuntimeException(expr.name);
    }

    private Object generateAssignments(String prefix, FindModuleExpression signature, Expression expr, StringBuilder sb, String assignResult) {
        if (expr instanceof AssignmentExpression) {
            AssignmentExpression specific = (AssignmentExpression) expr;
            String identifier = "CDEP_" + specific.name.toUpperCase();
            if (assignments.get(specific) != null) {
                return String.format("${%s}", identifier);
            }
            assignments.put(specific, identifier);
            generateAssignments(prefix, signature, specific.expression, sb, identifier);
            return String.format("${%s}", identifier);
        } else if (expr instanceof InvokeFunctionExpression) {
            InvokeFunctionExpression specific = (InvokeFunctionExpression) expr;
            Object values[] = new Object[specific.parameters.length];
            for (int i = 0; i < specific.parameters.length; ++i) {
                Object value = generateAssignments(prefix, signature, specific.parameters[i], sb, null);
                if (values == null) {
                    throw new RuntimeException(specific.parameters[i].getClass().toString());
                }
                values[i] = value;
            }

            if (specific.function == ExternalFunctionExpression.FILE_GETNAME) {
                if (assignResult == null) {
                    throw new RuntimeException();
                }
                sb.append(String.format("%sget_filename_component(%s %s NAME)\n",
                        prefix,
                        assignResult,
                        values[0]));
                return null;

            } else if (specific.function == ExternalFunctionExpression.STRING_LASTINDEXOF) {
                if (assignResult == null) {
                    throw new RuntimeException();
                }
                sb.append(String.format("%sstring(FIND %s %s %s REVERSE)\n",
                        prefix,
                        values[0],
                        values[1],
                        assignResult));
                return null;
            } else if (specific.function == ExternalFunctionExpression.STRING_SUBSTRING_BEGIN_END) {
                if (assignResult == null) {
                    throw new RuntimeException();
                }
                sb.append(String.format("%sstring(SUBSTRING %s %s %s %s)\n",
                        prefix,
                        values[0],
                        values[1],
                        values[2],
                        assignResult));
                return null;
            } else if (specific.function == ExternalFunctionExpression.STRING_STARTSWITH) {
                if (assignResult != null) {
                    throw new RuntimeException();
                }
                return null;
            } else if (specific.function == ExternalFunctionExpression.FILE_JOIN_SEGMENTS) {
                if (assignResult == null) {
                    throw new RuntimeException();
                }
                String assign = (String) values[0];
                String segments[] = (String[]) values[1];
                for (int i = 0; i < segments.length; ++i) {
                    assign += "/" + segments[i];
                }
                sb.append(String.format("%sset(%s %s)\n",
                        prefix,
                        assignResult,
                        assign));
                return null;
            }
            throw new RuntimeException(specific.function.method.getName());
        } else if (expr instanceof StringExpression) {
            StringExpression specific = (StringExpression) expr;
            String result = "\"" + specific.value + "\"";
            if (assignResult != null) {
                sb.append(String.format("%sset(%s %s)\n",
                        prefix,
                        assignResult,
                        result));
                return null;
            }
            return result;
        } else if (expr instanceof ParameterExpression) {
            ParameterExpression specific = (ParameterExpression) expr;
            assert assignResult == null;
            return parameterName(signature, specific);
        } else if (expr instanceof IntegerExpression) {
            assert assignResult == null;
            IntegerExpression specific = (IntegerExpression) expr;
            return String.format("%s", specific.value);
        } else if (expr instanceof ArrayExpression) {
            ArrayExpression specific = (ArrayExpression) expr;
            Object array[] = new String[specific.elements.length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = generateAssignments(prefix, signature, specific.elements[i], sb, null);
            }
            return array;
        }
        throw new RuntimeException(expr.getClass().toString());
    }

    private String getStringValue(Expression expression) {
        if (expression instanceof StringExpression) {
            return ((StringExpression) expression).value;
        }
        throw new RuntimeException(expression.toString());
    }

    String getAddDependencyFunctionName(Coordinate coordinate) {
        return String.format("add_cdep_%s_dependency", coordinate.artifactId)
                .replace("-", "_")
                .replace("/", "_");
    }
}
