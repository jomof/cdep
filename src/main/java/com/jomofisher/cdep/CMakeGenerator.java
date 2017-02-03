package com.jomofisher.cdep;

import com.jomofisher.cdep.AST.AbortExpression;
import com.jomofisher.cdep.AST.CaseExpression;
import com.jomofisher.cdep.AST.Expression;
import com.jomofisher.cdep.AST.FindModuleExpression;
import com.jomofisher.cdep.AST.FoundModuleExpression;
import com.jomofisher.cdep.AST.FunctionTableExpression;
import com.jomofisher.cdep.AST.IfGreaterThanOrEqualExpression;
import com.jomofisher.cdep.AST.LongConstantExpression;
import com.jomofisher.cdep.AST.ParameterExpression;
import com.jomofisher.cdep.AST.StringExpression;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CMakeGenerator {

    final private List<FoundModuleExpression> foundModules;

    CMakeGenerator() {
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

    void generate(GeneratorEnvironment environment, FunctionTableExpression table)
        throws IOException {
        getAllFoundModuleExpressions(table, foundModules);
        for (FoundModuleExpression foundModule : foundModules) {
            File local = environment.getLocalArchiveFilename(
                foundModule.coordinate, foundModule.archive);
            if (!local.exists()) {
                local.getParentFile().mkdirs();
                WebUtils.copyUrlToLocalFile(foundModule.archive, local);
            }
            File unzipFolder = environment.getLocalUnzipFolder(
                foundModule.coordinate, foundModule.archive);
            if (!unzipFolder.exists()) {
                unzipFolder.mkdirs();
                ArchiveUtils.unzip(local, unzipFolder);
            }
        }
    }
}
