package io.cdep.cdep;

import io.cdep.cdep.ast.finder.*;
import io.cdep.cdep.pod.PlainOldDataFuzzer;

import java.util.function.Function;

import static io.cdep.cdep.ast.finder.ExpressionBuilder.*;

public class InterpretingVisitorTest {

  private Function<Object, Object> op = new Function<Object, Object>() {
    @Override
    public Object apply(Object o) {
      return new InterpretingVisitor().visit((Expression) o);
    }
  };

  PlainOldDataFuzzer getFuzzer() {
    PlainOldDataFuzzer fuzzer = new PlainOldDataFuzzer();
    fuzzer.addTerminal(string("m'string"));
    fuzzer.addTerminal(integer(192));
    fuzzer.addTerminal(nop());
    fuzzer.addTerminal(abort("m'abort"));
    fuzzer.addTerminal(eq(integer(2), integer(3)));
    fuzzer.addParentTypes(AbortExpression.class);
    fuzzer.addParentTypes(ParameterExpression.class);
    fuzzer.addParentTypes(NopExpression.class);
    fuzzer.addExpectedException("Need to bind");
    fuzzer.addExpectedException("m'abort");
    fuzzer.addExpectedException("was not assignable to boolean");
    fuzzer.addExpectedException("Abort: ");
    return fuzzer;
  }

  private void fuzz(Class clazz) throws InstantiationException, IllegalAccessException {
    getFuzzer().fuzz(clazz, op);
  }

  //@Test
  public void fuzzTest() throws Exception {
    fuzz(GlobalBuildEnvironmentExpression.class);
    fuzz(IfSwitchExpression.class);
    fuzz(FunctionTableExpression.class);
    fuzz(FindModuleExpression.class);
  }
}