package org.jimple.interpreter;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.antlr.v4.runtime.tree.TerminalNode;

public interface JimpleContext {

    boolean hasVar(TerminalNode identifier);

    Object getVarValue(TerminalNode identifier);

    void setVarValue(TerminalNode identifier, Object value);

    void registerVariable(TerminalNode identifier, Object value);

    void registerFunction(FunctionSignature functionSignature, BiFunction<FunctionSignature, List<Object>, Object> handler);

    BiFunction<FunctionSignature, List<Object>, Object> getFunction(FunctionSignature functionSignature);

    List<FunctionInfo> getAllFunctions();

    void pushCallScope(Map<String, Object> variables);

    void popCallScope();

    PrintStream getStdout();

    void pushBlockScope();

    void popBlockScope();
}
