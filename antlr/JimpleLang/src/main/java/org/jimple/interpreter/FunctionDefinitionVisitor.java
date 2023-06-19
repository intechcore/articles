package org.jimple.interpreter;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.jimple.lang.JimpleBaseVisitor;
import org.jimple.lang.JimpleParser;

import static org.jimple.interpreter.JimpleInterpreter.VOID;

public class FunctionDefinitionVisitor extends JimpleBaseVisitor<Object> {
    private final JimpleContext context;
    private final FunctionCallHandler handler;

    public FunctionDefinitionVisitor(final JimpleContext context, final FunctionCallHandler handler) {
        super();
        this.context = context;
        this.handler = handler;
    }

    @Override
    public Object visitFunctionDefinition(final JimpleParser.FunctionDefinitionContext ctx) {
        final String name = ctx.name.getText();
        final List<String> parameters = ctx.IDENTIFIER().stream().skip(1).map(ParseTree::getText).toList();
        final var funcSig = new FunctionSignature(name, parameters.size(), ctx.getParent().getParent());
        context.registerFunction(funcSig, (func, args) -> handler.handleFunc(func, parameters, args, ctx));
        return VOID;
    }
}
