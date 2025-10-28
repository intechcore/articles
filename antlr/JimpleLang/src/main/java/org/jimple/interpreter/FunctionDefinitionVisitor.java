package org.jimple.interpreter;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.jimple.compiler.CompilationInfo;
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
        final var funcSig = FunctionSignature.of(name, parameters.size(), ctx);
        context.registerFunction(funcSig, (func, args) -> handler.handleFunc(func, parameters, args, ctx));
        return VOID;
    }

    @Override
    public Object visitProgram(final JimpleParser.ProgramContext ctx) {
        // list of native functions
        final NativeFuncInfo nowNativeHandler = new NativeFuncInfo(System.class.getName().replace('.', '/'), "currentTimeMillis", this::handleNow, CompilationInfo.NUMBER, List.of());
        context.registerFunction(FunctionSignature.ofNative("now", 0, ctx, nowNativeHandler), (func, args) -> handler.handleFunc(func, List.of(), args, null));

        return super.visitProgram(ctx);
    }

    private Object handleNow(final FunctionSignature func, final List<Object> args) {
        if (!args.isEmpty()) {
            throw new IllegalStateException("In function '" + func.name() + "' expected empty arguments but found: " + args);
        }

        return System.currentTimeMillis();
    }
}
