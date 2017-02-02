package com.jomofisher.cdep;

import com.jomofisher.cdep.AST.AbortExpression;
import com.jomofisher.cdep.AST.CaseExpression;
import com.jomofisher.cdep.AST.Expression;
import com.jomofisher.cdep.AST.FindModuleExpression;
import com.jomofisher.cdep.AST.FoundModuleExpression;
import com.jomofisher.cdep.AST.FunctionTable;
import com.jomofisher.cdep.AST.IfGreaterThanOrEqualExpression;
import com.jomofisher.cdep.AST.LongConstantExpression;
import com.jomofisher.cdep.AST.ParameterExpression;
import com.jomofisher.cdep.manifest.Android;
import com.jomofisher.cdep.manifest.Coordinate;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FindModuleFunctionTableBuilder {

    private final Map<Coordinate, ResolvedManifest> manifests = new HashMap<>();
    private final ParameterExpression targetPlatform =
        new ParameterExpression("targetPlatform");
    private final ParameterExpression androidArchAbi =
        new ParameterExpression("androidArchAbi");
    private final ParameterExpression androidStlType =
        new ParameterExpression("androidStlType");
    private final ParameterExpression systemVersion =
        new ParameterExpression("systemVersion");

    public void addManifest(ResolvedManifest resolved) {
        manifests.put(resolved.manifest.coordinate, resolved);
    }

    public FunctionTable build() throws MalformedURLException, URISyntaxException {
        FunctionTable functionTable = new FunctionTable();
        for (ResolvedManifest resolved : manifests.values()) {
            functionTable.functions.put(resolved.manifest.coordinate.toString(),
                buildFindModule(resolved));
        }
        return functionTable;
    }

    private FindModuleExpression buildFindModule(ResolvedManifest resolved)
        throws MalformedURLException, URISyntaxException {
        checkForDuplicateZipFiles(resolved);

        Map<String, Expression> cases = new HashMap<>();
        String supported = "";
        if (resolved.manifest.android != null) {
            supported += "'Android' ";
            cases.put("Android",
                buildAndroidStlTypeCase(resolved));
        }
        CaseExpression expression = new CaseExpression(
            targetPlatform,
            cases,
            new AbortExpression(
                String.format("Target platform '%%s' is not supported by module '%s'. "
                        + "Supported: %s",
                    resolved.manifest.coordinate, supported), targetPlatform));

        return new FindModuleExpression(resolved.manifest.coordinate.toString(), targetPlatform,
            systemVersion, androidArchAbi, androidStlType, expression);
    }

    private Expression buildAndroidStlTypeCase(
        ResolvedManifest resolved) throws MalformedURLException, URISyntaxException {

        // Gather up the runtime names
        Map<String, List<Android>> stlTypes = new HashMap<>();
        for (Android android : resolved.manifest.android) {
            List<Android> androids = stlTypes.get(android.runtime);
            if (androids == null) {
                androids = new ArrayList<>();
                stlTypes.put(android.runtime, androids);
            }
            androids.add(android);
        }

        List<Android> noRuntimeAndroids = stlTypes.get(null);
        if (noRuntimeAndroids != null) {
            if (stlTypes.size() == 1) {
                // If there are no runtimes, then skip the runtime check. This is likely a
                // header-only module.
                return buildAndroidPlatformExpression(resolved, noRuntimeAndroids);
            }
            // There are some android sub modules with runtime and some without
            return new AbortExpression(
                String.format("Runtime is on some android submodules but not other in module '%s'",
                    resolved.manifest.coordinate));
        }

        Map<String, Expression> cases = new HashMap<>();
        for (String stlType : stlTypes.keySet()) {
            cases.put(stlType, buildAndroidPlatformExpression(
                resolved,
                stlTypes.get(stlType)));
        }

        return new CaseExpression(
            androidStlType,
            cases,
            new AbortExpression(
                String.format("Runtime '%%s' is not supported by module '%s'",
                    resolved.manifest.coordinate), androidStlType));
    }

    private void checkForDuplicateZipFiles(ResolvedManifest resolved) {
        Set<String> zips = new HashSet<>();
        for (Android android : resolved.manifest.android) {
            if (zips.contains(android.file)) {
                throw new RuntimeException(
                    String.format(
                        "Module '%s' contains multiple references to the same zip file: %s",
                        resolved.manifest.coordinate, android.file));
            }
            zips.add(android.file);
        }
    }


    private Expression buildAndroidPlatformExpression(
        ResolvedManifest resolved,
        List<Android> androids) throws MalformedURLException, URISyntaxException {

        // If there's only one android left and it doesn't have a platform then this is
        // a header-only module.
        if (androids.size() == 1 && androids.get(0).platform == null) {
            return returnOnly(resolved, androids);
        }


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
                resolved.manifest.coordinate), systemVersion);
        for (long platform : platforms) {
            prior = new IfGreaterThanOrEqualExpression(
                systemVersion,
                new LongConstantExpression(platform),
                returnOnly(resolved, grouped.get(platform)),
                prior);
        }
        return prior;
    }

    private Expression returnOnly(ResolvedManifest resolved, List<Android> androids)
        throws URISyntaxException, MalformedURLException {
        if (androids.size() != 1) {
            throw new RuntimeException(String.format(
                "Expected only one android zip upon reaching ABI level. There were %s.",
                androids.size()));
        }
        Android android = androids.get(0);
        URL url = resolved.remote.toURI()
            .resolve(".")
            .resolve(android.file)
            .toURL();
        return new FoundModuleExpression(url, android.include, android.lib);
    }
}
