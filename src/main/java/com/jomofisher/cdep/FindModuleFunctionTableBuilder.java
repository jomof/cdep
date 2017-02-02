package com.jomofisher.cdep;

import com.jomofisher.cdep.AST.AbortExpression;
import com.jomofisher.cdep.AST.CaseExpression;
import com.jomofisher.cdep.AST.Expression;
import com.jomofisher.cdep.AST.FindModuleExpression;
import com.jomofisher.cdep.AST.FunctionTable;
import com.jomofisher.cdep.AST.IfGreaterThanOrEqualExpression;
import com.jomofisher.cdep.AST.LongConstantExpression;
import com.jomofisher.cdep.AST.ParameterExpression;
import com.jomofisher.cdep.AST.StringExpression;
import com.jomofisher.cdep.manifest.Android;
import com.jomofisher.cdep.manifest.Coordinate;
import com.jomofisher.cdep.manifest.Manifest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FindModuleFunctionTableBuilder {

    private final Map<Coordinate, Manifest> manifests = new HashMap<>();
    private final ParameterExpression targetPlatform =
        new ParameterExpression("targetPlatform");
    private final ParameterExpression androidArchAbi =
        new ParameterExpression("androidArchAbi");
    private final ParameterExpression androidStlType =
        new ParameterExpression("androidStlType");
    private final ParameterExpression systemVersion =
        new ParameterExpression("systemVersion");

    public void addManifest(Manifest manifest) {
        manifests.put(manifest.coordinate, manifest);
    }

    public FunctionTable build() {
        FunctionTable functionTable = new FunctionTable();
        for (Manifest manifest : manifests.values()) {
            functionTable.functions.put(manifest.coordinate.toString(),
                buildFindModule(manifest));
        }
        return functionTable;
    }

    private FindModuleExpression buildFindModule(Manifest manifest) {
        checkForDuplicateZipFiles(manifest);

        Map<String, Expression> cases = new HashMap<>();
        String supported = "";
        if (manifest.android != null) {
            supported += "'Android' ";
            cases.put("Android",
                buildAndroidStlTypeCase(manifest.coordinate, manifest.android));
        }
        CaseExpression expression = new CaseExpression(
            targetPlatform,
            cases,
            new AbortExpression(
                String.format("Target platform '%%s' is not supported by module '%s'. "
                        + "Supported: %s",
                    manifest.coordinate, supported), targetPlatform));

        return new FindModuleExpression(manifest.coordinate.toString(), targetPlatform,
            systemVersion, androidArchAbi, androidStlType, expression);
    }

    private Expression buildAndroidStlTypeCase(
        Coordinate coordinate,
        Android[] androidManifests) {
        // Gather up the runtime names
        Map<String, List<Android>> stlTypes = new HashMap<>();
        for (Android android : androidManifests) {
            List<Android> androids = stlTypes.get(android.runtime);
            if (androids == null) {
                androids = new ArrayList<>();
                stlTypes.put(android.runtime, androids);
            }
            androids.add(android);
        }

        Map<String, Expression> cases = new HashMap<>();
        for (String stlType : stlTypes.keySet()) {
            cases.put(stlType, buildAndroidPlatformExpression(
                coordinate,
                stlTypes.get(stlType)));
        }

        return new CaseExpression(
            androidStlType,
            cases,
            new AbortExpression(
                String.format("Runtime '%%s' is not supported by module '%s'",
                    coordinate), androidStlType));
    }

    private void checkForDuplicateZipFiles(Manifest manifest) {
        Set<String> zips = new HashSet<>();
        for (Android android : manifest.android) {
            if (zips.contains(android.file)) {
                throw new RuntimeException(
                    String.format(
                        "Module '%s' contains multiple references to the same zip file: %s",
                        manifest.coordinate, android.file));
            }
            zips.add(android.file);
        }
    }


    private Expression buildAndroidPlatformExpression(
        Coordinate coordinate,
        List<Android> androids) {
        Map<Long, List<Android>> grouped = new HashMap<>();
        for (Android android : androids) {
            Long platform = Long.parseLong(android.platform);
            List<Android> group = grouped.get(platform);
            if (group == null) {
                group = new ArrayList<>();
                grouped.put(platform, group);
            }
            group.add(android);
        }

        List<Long> platforms = new ArrayList<>();
        platforms.addAll(grouped.keySet());
        Collections.sort(platforms);

        Expression prior = new AbortExpression(
            String.format("Platform '%%s' is not supported by module '%s'",
                coordinate), systemVersion);
        for (long platform : platforms) {
            prior = new IfGreaterThanOrEqualExpression(
                systemVersion,
                new LongConstantExpression(platform),
                buildAndroidAbiCase(grouped.get(platform)),
                prior);
        }
        return prior;
    }

    private Expression buildAndroidAbiCase(List<Android> androids) {
        if (androids.size() != 1) {
            throw new RuntimeException(String.format(
                "Expected only one android zip upon reaching ABI level. There were %s.",
                androids.size()));
        }
        return new StringExpression(androids.get(0).file);
    }
}
