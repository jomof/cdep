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
import io.cdep.AST.service.ResolvedManifest;
import io.cdep.manifest.Android;
import io.cdep.manifest.Coordinate;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FindModuleFunctionTableBuilder {

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
        manifests.put(resolved.cdepManifestYml.coordinate, resolved);
    }

    public FunctionTableExpression build() throws MalformedURLException, URISyntaxException {
        FunctionTableExpression functionTable = new FunctionTableExpression();
        for (ResolvedManifest resolved : manifests.values()) {
            assert resolved.cdepManifestYml.coordinate != null;
            functionTable.functions.put(resolved.cdepManifestYml.coordinate.toString(),
                buildFindModule(resolved));
        }
        return functionTable;
    }

    private FindModuleExpression buildFindModule(ResolvedManifest resolved)
        throws MalformedURLException, URISyntaxException {

        Map<String, Expression> cases = new HashMap<>();
        String supported = "";
        if (resolved.cdepManifestYml.android != null) {
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
                    resolved.cdepManifestYml.coordinate, supported), targetPlatform));

        return new FindModuleExpression(resolved.cdepManifestYml.coordinate, targetPlatform,
            systemVersion, androidArchAbi, androidStlType, expression);
    }

    private Expression buildAndroidStlTypeCase(
        ResolvedManifest resolved) throws MalformedURLException, URISyntaxException {

        // Gather up the runtime names
        Map<String, List<Android>> stlTypes = new HashMap<>();
        assert resolved.cdepManifestYml.android != null;
        for (Android android : resolved.cdepManifestYml.android) {
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
                    resolved.cdepManifestYml.coordinate));
        }

        Map<String, Expression> cases = new HashMap<>();
        String runtimes = "";
        for (String stlType : stlTypes.keySet()) {
            runtimes += stlType + " ";
            cases.put(stlType, buildAndroidPlatformExpression(
                resolved,
                stlTypes.get(stlType)));
        }

        return new CaseExpression(
            androidStlType,
            cases,
            new AbortExpression(
                String.format("Android runtime '%%s' is not supported by module '%s'. Supported: %s",
                    resolved.cdepManifestYml.coordinate, runtimes), androidStlType));
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
            String.format("Android API level '%%s' is not supported by module '%s'",
                resolved.cdepManifestYml.coordinate), systemVersion);
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
        String include = android.include;
        return new FoundModuleExpression(resolved.cdepManifestYml.coordinate, url,
            android.sha256, include, android.lib);
    }
}
