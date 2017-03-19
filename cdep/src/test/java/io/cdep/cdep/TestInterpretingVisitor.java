package io.cdep.cdep;

import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.ParameterExpression;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.archive;
import static org.junit.Assert.fail;

public class TestInterpretingVisitor {
  @Test
  public void testNullInclude() throws Exception {
    new InterpretingVisitor().visit(
        archive(
            new URL("https://google.com"),
            "sha256",
            192L,
            null,
            null,
            null,
            null));
  }

  @Test
  public void testAllResolvedManifestsAndroid() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("archiveMissingFile", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSize", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSha256", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("admob", "Reference com.github.jomof:firebase/app:2.1.3-rev8 was not found, needed by com.github.jomof:firebase/admob:2.1.3-rev8");
    expected.put("sqliteiOS", "Target platform 'Android' is not supported by module 'com.github.jomof:sqlite:3.16.2-rev33'. Supported: Darwin");
    boolean unexpectedFailures = false;
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
      builder.addManifest(manifest.resolved);
      String expectedFailure = expected.get(manifest.name);
      try {
        FunctionTableExpression function = builder.build();
        new InterpretingVisitor() {
          @Override
          protected Object visitParameterExpression(ParameterExpression expr) {
            if ("cdep_exploded_root".equals(expr.name)) {
              return "exploded/root";
            }
            if ("targetPlatform".equals(expr.name)) {
              return "Android";
            }
            if ("systemVersion".equals(expr.name)) {
              return 21;
            }
            if ("androidArchAbi".equals(expr.name)) {
              return "x86";
            }
            if ("androidStlType".equals(expr.name)) {
              return "c++_static";
            }
            if ("osxSysroot".equals(expr.name)) {
              return "/iPhoneOS10.2.sdk";
            }
            if ("osxArchitectures".equals(expr.name)) {
              return "i386";
            }
            return super.visitParameterExpression(expr);
          }
        }.visit(function);
        if (expectedFailure != null) {
          fail("Expected failure");
        }
      } catch (RuntimeException e) {
        if (expectedFailure == null || !expectedFailure.equals(e.getMessage())) {
          unexpectedFailures = true;
          System.out.printf("expected.put(\"%s\", \"%s\");\n", manifest.name, e.getMessage());
        }
      }

    }
    if (unexpectedFailures) {
      fail("Unexpected failures. See console.");
    }
  }

  @Test
  public void testAllResolvedManifestsiOS() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("admob", "Reference com.github.jomof:firebase/app:2.1.3-rev8 was not found, needed by com.github.jomof:firebase/admob:2.1.3-rev8");
    expected.put("sqliteAndroid", "Target platform 'Darwin' is not supported by module 'com.github.jomof:sqlite:3.16.2-rev33'. Supported: Android");
    expected.put("archiveMissingFile", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSize", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSha256", "Archive in http://google.com/cdep-manifest.yml was malformed");
    boolean unexpectedFailures = false;
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
      builder.addManifest(manifest.resolved);
      String expectedFailure = expected.get(manifest.name);
      try {
        FunctionTableExpression function = builder.build();
        new InterpretingVisitor() {
          @Override
          protected Object visitParameterExpression(ParameterExpression expr) {
            if ("cdep_exploded_root".equals(expr.name)) {
              return "exploded/root";
            }
            if ("targetPlatform".equals(expr.name)) {
              return "Darwin";
            }
            if ("systemVersion".equals(expr.name)) {
              return 21;
            }
            if ("androidArchAbi".equals(expr.name)) {
              return "x86";
            }
            if ("androidStlType".equals(expr.name)) {
              return "c++_static";
            }
            if ("osxSysroot".equals(expr.name)) {
              return "/iPhoneOS10.2.sdk";
            }
            if ("osxArchitectures".equals(expr.name)) {
              return new String[]{"i386"};
            }
            return super.visitParameterExpression(expr);
          }
        }.visit(function);
        if (expectedFailure != null) {
          fail("Expected failure");
        }
      } catch (RuntimeException e) {
        if (expectedFailure == null || !expectedFailure.equals(e.getMessage())) {
          unexpectedFailures = true;
          System.out.printf("expected.put(\"%s\", \"%s\");\n", manifest.name, e.getMessage());
        }
      }
    }
    if (unexpectedFailures) {
      fail("Unexpected failures. See console.");
    }
  }
}
