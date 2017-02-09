package io.cdep;

import io.cdep.AST.finder.AbortExpression;
import io.cdep.AST.finder.CaseExpression;
import io.cdep.AST.finder.Expression;
import io.cdep.AST.finder.FindModuleExpression;
import io.cdep.AST.finder.FoundModuleExpression;
import io.cdep.AST.finder.FunctionTableExpression;
import io.cdep.AST.finder.IfGreaterThanOrEqualExpression;
import io.cdep.AST.finder.LongConstantExpression;
import io.cdep.AST.finder.ParameterExpression;
import io.cdep.AST.finder.StringExpression;
import io.cdep.service.GeneratorEnvironment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CMakeGenerator {

    final private GeneratorEnvironment environment;
    final private List<FoundModuleExpression> foundModules;

    CMakeGenerator(GeneratorEnvironment environment) {
        this.environment = environment;
        this.foundModules = new ArrayList<>();
    }

    private static void getAllFoundModuleExpressions(
            Expression expression, List<FoundModuleExpression> foundModules) {
        if (expression instanceof CaseExpression) {
            CaseExpression caseExpression = (CaseExpression) expression;
            getAllFoundModuleExpressions(caseExpression.var, foundModules);
            for (String caseValue : caseExpression.cases.keySet()) {
                getAllFoundModuleExpressions(caseExpression.cases.get(caseValue), foundModules);
            }
            getAllFoundModuleExpressions(caseExpression.defaultCase, foundModules);
            return;
        } else if (expression instanceof ParameterExpression) {
            return;
        } else if (expression instanceof AbortExpression) {
            return;
        } else if (expression instanceof IfGreaterThanOrEqualExpression) {
            IfGreaterThanOrEqualExpression ifexpr = (IfGreaterThanOrEqualExpression) expression;
            getAllFoundModuleExpressions(ifexpr.value, foundModules);
            getAllFoundModuleExpressions(ifexpr.compareTo, foundModules);
            getAllFoundModuleExpressions(ifexpr.trueExpression, foundModules);
            getAllFoundModuleExpressions(ifexpr.falseExpression, foundModules);
            return;
        } else if (expression instanceof LongConstantExpression) {
            return;
        } else if (expression instanceof StringExpression) {
            return;
        } else if (expression instanceof FoundModuleExpression) {
            foundModules.add((FoundModuleExpression) expression);
            return;
        } else if (expression instanceof FunctionTableExpression) {
            FunctionTableExpression table = (FunctionTableExpression) expression;
            for (FindModuleExpression function : table.functions.values()) {
                getAllFoundModuleExpressions(function, foundModules);
            }
            return;
        } else if (expression instanceof FindModuleExpression) {
            FindModuleExpression findModule = (FindModuleExpression) expression;
            getAllFoundModuleExpressions(findModule.expression, foundModules);
            return;
        }
        throw new RuntimeException(expression.toString());
    }

    void generate(FunctionTableExpression table)
        throws IOException {
        getAllFoundModuleExpressions(table, foundModules);

        // Download and unzip any modules.
        for (FoundModuleExpression foundModule : foundModules) {
            File local = environment.getLocalDownloadedFile(
                foundModule.coordinate, foundModule.archive);
            File unzipFolder = environment.getLocalUnzipFolder(
                foundModule.coordinate, foundModule.archive);
            if (!unzipFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                unzipFolder.mkdirs();
                environment.out.printf("Exploding %s\n", foundModule.archive);
                ArchiveUtils.unzip(local, unzipFolder);
            }
        }

        // Generate CMake Find*.cmake files
        for (FindModuleExpression findFunction : table.functions.values()) {
            StringBuilder sb = new StringBuilder();
            generateFinderExpression(0, findFunction, findFunction, sb);
            // TODO: If two artifact IDs conflict then generate a Find*.cmake that emits an error
            // telling user to pick one.
            File shortFile = new File(environment.modulesFolder,
                String.format("Find%s.cmake", findFunction.coordinate.artifactId));
            writeTextToFile(shortFile, sb.toString());

        }
    }

    private void writeTextToFile(File file, String body) throws IOException {
        environment.out.printf("Generating %s\n", file);
        BufferedWriter writer = null;
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(body);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void generateFinderExpression(
        int indent,
        FindModuleExpression signature,
        Expression expression,
        StringBuilder sb) {
        String prefix = new String(new char[indent * 2]).replace('\0', ' ');
        if (expression instanceof FindModuleExpression) {
            FindModuleExpression specific = (FindModuleExpression) expression;
            sb.append("# GENERATED FILE. DO NOT EDIT.\n");
            sb.append(String.format("# FindModule for CDep module: %s\n",
                specific.coordinate.toString()));
            sb.append("\n" +
                    "# Choose between Anroid NDK Toolchain and CMake Android Toolchain\n" +
                    "if(DEFINED CMAKE_ANDROID_STL_TYPE)\n" +
                    "  set(CDEP_DETERMINED_ANDROID_RUNTIME ${CMAKE_ANDROID_STL_TYPE})\n" +
                    "  set(CDEP_DETERMINED_ANDROID_ABI ${CMAKE_ANDROID_ARCH_ABI})\n" +
                    "else()\n" +
                    "  set(CDEP_DETERMINED_ANDROID_RUNTIME ${ANDROID_STL})\n" +
                    "  set(CDEP_DETERMINED_ANDROID_ABI ${ANDROID_ABI})\n" +
                    "endif()\n\n");
            generateFinderExpression(indent, signature, specific.expression, sb);
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
            String simpleName = specific.coordinate.artifactId.toUpperCase().replace("-", "_");
            File exploded = environment.getLocalUnzipFolder(specific.coordinate, specific.archive);
            sb.append(String.format("%sset(%s_FOUND true)\n", prefix, simpleName));
            sb.append(String.format("%sset(%s_INCLUDE_DIRS \"%s\")\n", prefix, simpleName,
                new File(exploded, specific.include).toString().replace("\\", "\\\\")));
            String libFolder = new File(exploded, specific.lib).toString().replace("\\", "\\\\");
            sb.append(String.format("%sfile(GLOB %s_LIBRARIES \"%s/${CDEP_DETERMINED_ANDROID_ABI}/lib*.*\")\n",
                    prefix,
                    simpleName,
                    libFolder));
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
}
