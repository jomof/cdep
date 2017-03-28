package io.cdep.cdep;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.archive;
import static org.junit.Assert.fail;

import io.cdep.annotations.NotNull;
import io.cdep.cdep.ast.finder.FunctionTableExpression;
import io.cdep.cdep.ast.finder.ParameterExpression;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class TestInterpretingVisitor {
  @Test
  public void testNullInclude() throws Exception {
    new InterpretingVisitor().visit(archive(new URL("https://google.com"), "sha256", 192L, null, null, null, null));
  }

  @Test
  public void testAllResolvedManifestsLinux() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("archiveMissingFile", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSize", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSha256", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("admob", "Reference com.github.jomof:firebase/app:2.1.3-rev8 was not found, needed by com.github.jomof:firebase/admob:2.1.3-rev8");
    expected.put("sqliteiOS", "Target platform Linux is not supported by com.github.jomof:sqlite:3.16.2-rev33. Supported: Darwin");
    expected.put("sqliteAndroid", "Target platform Linux is not supported by com.github.jomof:sqlite:3.16.2-rev33. Supported: Android");
    expected.put("sqlite", "Target platform Linux is not supported by com.github.jomof:sqlite:0.0.0. Supported: Android Darwin");
    boolean unexpectedFailures = false;
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
      builder.addManifest(manifest.resolved);
      String expectedFailure = expected.get(manifest.name);
      try {
        final FunctionTableExpression function = builder.build();
        new InterpretingVisitor() {
          @Override
          protected Object visitParameterExpression(@NotNull ParameterExpression expr) {
            if (function.globals.cdepExplodedRoot == expr) {
              return "exploded/root";
            }
            if (function.globals.cmakeSystemName == expr) {
              return "Linux";
            }
            if (function.globals.cmakeSystemVersion == expr) {
              return 21;
            }
            if (function.globals.cdepDeterminedAndroidAbi == expr) {
              return "x86";
            }
            if (function.globals.cdepDeterminedAndroidRuntime == expr) {
              return "c++_static";
            }
            if (function.globals.cmakeOsxSysroot == expr) {
              return "/iPhoneOS10.2.sdk";
            }
            if (function.globals.cmakeOsxArchitectures == expr) {
              return "i386";
            }
            return super.visitParameterExpression(expr);
          }
        }.visit(function);
        if (expectedFailure != null) {
          fail("Expected failure for " + manifest.name);
        }
      } catch (RuntimeException e) {
        if (!RuntimeException.class.equals(e.getClass())) {
          throw e;
        }
        if (expectedFailure == null || !expectedFailure.equals(e.getMessage())) {
          unexpectedFailures = true;
          e.printStackTrace();
          System.out.printf("expected.put(\"%s\", \"%s\");\n", manifest.name, e.getMessage());
        }
      }

    }
    if (unexpectedFailures) {
      fail("Unexpected failures. See console.");
    }
  }

  @Test
  public void testAllResolvedManifestsAndroid() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put("sqliteLinux", "Target platform Android is not supported by com.github.jomof:sqlite:0.0.0. Supported: Linux");
    expected.put("sqliteLinuxMultiple", "Target platform Android is not supported by com.github.jomof:sqlite:0.0.0. Supported: Linux");
    expected.put("archiveMissingFile", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("admob", "Reference com.github.jomof:firebase/app:2.1.3-rev8 was not found, needed by com.github.jomof:firebase/admob:2.1.3-rev8");
    expected.put("archiveMissingSize", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSha256", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("sqliteiOS", "Target platform Android is not supported by com.github.jomof:sqlite:3.16.2-rev33. Supported: Darwin");

    boolean unexpectedFailures = false;
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
      builder.addManifest(manifest.resolved);
      String expectedFailure = expected.get(manifest.name);
      try {
        final FunctionTableExpression function = builder.build();
        new InterpretingVisitor() {
          @Override
          protected Object visitParameterExpression(@NotNull ParameterExpression expr) {
            if (function.globals.cdepExplodedRoot == expr) {
              return "exploded/root";
            }
            if (function.globals.cmakeSystemName == expr) {
              return "Android";
            }
            if (function.globals.cmakeSystemVersion == expr) {
              return 21;
            }
            if (function.globals.cdepDeterminedAndroidAbi == expr) {
              return "x86";
            }
            if (function.globals.cdepDeterminedAndroidRuntime == expr) {
              return "c++_static";
            }
            if (function.globals.cmakeOsxSysroot == expr) {
              return "/iPhoneOS10.2.sdk";
            }
            if (function.globals.cmakeOsxArchitectures == expr) {
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
    expected.put("archiveMissingFile", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("sqliteAndroid", "Target platform Darwin is not supported by com.github.jomof:sqlite:3.16.2-rev33. Supported: Android");
    expected.put("sqliteLinux", "Target platform Darwin is not supported by com.github.jomof:sqlite:0.0.0. Supported: Linux");
    expected.put("sqliteLinuxMultiple", "Target platform Darwin is not supported by com.github.jomof:sqlite:0.0.0. Supported: Linux");
    expected.put("admob", "Reference com.github.jomof:firebase/app:2.1.3-rev8 was not found, needed by com.github.jomof:firebase/admob:2.1.3-rev8");
    expected.put("archiveMissingSize", "Archive in http://google.com/cdep-manifest.yml was malformed");
    expected.put("archiveMissingSha256", "Archive in http://google.com/cdep-manifest.yml was malformed");
    boolean unexpectedFailures = false;
    for (ResolvedManifests.NamedManifest manifest : ResolvedManifests.all()) {
      final FindModuleFunctionTableBuilder builder = new FindModuleFunctionTableBuilder();
      builder.addManifest(manifest.resolved);
      String expectedFailure = expected.get(manifest.name);
      try {
        final FunctionTableExpression function = builder.build();
        new InterpretingVisitor() {
          @Override
          protected Object visitParameterExpression(@NotNull ParameterExpression expr) {
            if (function.globals.cdepExplodedRoot == expr) {
              return "exploded/root";
            }
            if (function.globals.cmakeSystemName == expr) {
              return "Darwin";
            }
            if (function.globals.cmakeSystemVersion == expr) {
              return 21;
            }
            if (function.globals.cdepDeterminedAndroidAbi == expr) {
              return "x86";
            }
            if (function.globals.cdepDeterminedAndroidRuntime == expr) {
              return "c++_static";
            }
            if (function.globals.cmakeOsxSysroot == expr) {
              return "/iPhoneOS10.2.sdk";
            }
            if (function.globals.cmakeOsxArchitectures == expr) {
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
