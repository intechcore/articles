package org.jimple.interpreter;

import java.util.Objects;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jimple.lang.JimpleParser;

/**
 * Function signature bound to parent context
 *
 * @param name       function name
 * @param paramCount parameter count
 * @param context    context of function definition or call
 */
public record FunctionSignature(String name, int paramCount, ParserRuleContext context) {
    public FunctionSignature {
        Objects.requireNonNull(context, "context");
    }

    /**
     * Returns new instance with specified context
     */
    public FunctionSignature withContext(final ParserRuleContext context) {
        return new FunctionSignature(name, paramCount, context);
    }

    public static FunctionSignature of(final String name, final int paramCount, final JimpleParser.FunctionDefinitionContext funDefCtx) {
        return new FunctionSignature(name, paramCount, funDefCtx);
    }

    public static FunctionSignature of(final String name, final int paramCount, final JimpleParser.FunctionCallContext funCallCtx) {
        return new FunctionSignature(name, paramCount, funCallCtx);
    }

    private ParserRuleContext getNextParent() {
        ParserRuleContext contextParent = context.getParent();
        while (contextParent instanceof JimpleParser.StatementContext) {
            contextParent = contextParent.getParent();
        }
        return contextParent;
    }

    @Override
    public boolean equals(final Object o) {
        final ParserRuleContext parentCtx = getNextParent();
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FunctionSignature that = (FunctionSignature) o;
        return paramCount == that.paramCount && Objects.equals(name, that.name) && Objects.equals(parentCtx, that.getNextParent());
    }

    @Override
    public int hashCode() {
        final ParserRuleContext parentCtx = getNextParent();
        return Objects.hash(name, paramCount, parentCtx);
    }
}
