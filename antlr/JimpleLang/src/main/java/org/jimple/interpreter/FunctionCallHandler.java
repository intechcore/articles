package org.jimple.interpreter;

import java.util.List;

import org.jimple.lang.JimpleParser;

/**
 * Called when function executed
 */
public interface FunctionCallHandler {
    Object handleFunc(FunctionSignature func, List<String> parameters, List<Object> arguments, JimpleParser.FunctionDefinitionContext ctx);

    /**
     * Noop implementation of {@link FunctionCallHandler}
     */
    FunctionCallHandler EMPTY = (func, parameters, arguments, ctx) -> JimpleInterpreter.VOID;
}
