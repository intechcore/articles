package org.jimple.interpreter;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Function signature bound to parent context
 *
 * @param name       function name
 * @param paramCount parameter count
 * @param context    parent context of declaration
 */
public record FunctionSignature(String name, int paramCount, ParserRuleContext context) {
    /**
     * Returns new instance with specified context
     */
    public FunctionSignature withContext(ParserRuleContext context) {
        return new FunctionSignature(name, paramCount, context);
    }
}
