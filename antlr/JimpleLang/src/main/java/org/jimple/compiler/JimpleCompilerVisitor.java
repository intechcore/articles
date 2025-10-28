package org.jimple.compiler;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.output.NullPrintStream;
import org.apache.commons.io.output.NullWriter;
import org.jimple.error.CodeCompilationException;
import org.jimple.interpreter.FunctionCallHandler;
import org.jimple.interpreter.FunctionDefinitionVisitor;
import org.jimple.interpreter.FunctionSignature;
import org.jimple.interpreter.JimpleContext;
import org.jimple.interpreter.JimpleContextImpl;
import org.jimple.interpreter.NativeFuncInfo;
import org.jimple.lang.JimpleBaseVisitor;
import org.jimple.lang.JimpleParser;
import org.jimple.util.StringUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import lombok.Getter;

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.RETURN;


/**
 * Implementation of {@link JimpleBaseVisitor} for compiling purpose.
 *
 * @author Ruslan Absaliamov
 */
public class JimpleCompilerVisitor extends JimpleBaseVisitor<CompilationInfo> {
    public static final CompilationInfo VOID = CompilationInfo.VOID;
    public static final String JIMPLE_MAIN_CLASS_NAME = "JimpleAutoGenApp";
    public static final String TYPE_STRING_BUILDER = "java/lang/StringBuilder";
    private final JimpleContext context = new JimpleContextImpl(new NullPrintStream());
    private final PrintWriter codeGenTrace;
    private final Deque<MethodVisitor> methods = new ArrayDeque<>(1);
    @Getter
    private byte[] bytecode;
    private ClassVisitor classVisitor;
    /**
     * If current method one the top skip function call
     */
    private final Deque<MethodVisitor> skipInvokeFunc = new ArrayDeque<>(1);

    public JimpleCompilerVisitor(final PrintWriter codeGenTrace) {
        this.codeGenTrace = codeGenTrace != null ? codeGenTrace : new PrintWriter(new NullWriter());
    }

    @Override
    public CompilationInfo visitProgram(final JimpleParser.ProgramContext ctx) {
        // before compilation find all function definitions
        new FunctionDefinitionVisitor(this.context, FunctionCallHandler.EMPTY).visitProgram(ctx);

        // compilation steps
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        final TraceClassVisitor tcv = new TraceClassVisitor(cw, codeGenTrace);
        final ClassVisitor cv = new CheckClassAdapter(tcv);
        classVisitor = cv;

        try {
            cv.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, JIMPLE_MAIN_CLASS_NAME, null, "java/lang/Object", null);

            // Define a constructor
            final MethodVisitor constructor = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitEnd();

            // Create the main method
            final MethodVisitor mainMethod = cv.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            mainMethod.visitCode();
            // by default body of script compiled into main method and variable args could be used in the script
            context.registerVariable("args", CompilationInfo.STRING);
            // set mainMethod as current (put on the top)
            methods.push(mainMethod);
            super.visitProgram(ctx);
            methods.pop();
            mainMethod.visitInsn(Opcodes.RETURN);
            mainMethod.visitEnd();

            cv.visitEnd();
        } catch (final Exception ex) {
            // flash trace to codeGenTrace
            tcv.visitEnd();
            throw ex;
        }
        bytecode = cw.toByteArray();
        classVisitor = null;
        return VOID;
    }

    @Override
    public CompilationInfo visit(final ParseTree tree) {
        final CompilationInfo result = super.visit(tree);
        if (result == null) {
            throw new IllegalStateException("Context not implemented: " + tree.getClass().getName());
        }
        return result;
    }

    @Override
    protected CompilationInfo aggregateResult(final CompilationInfo aggregate, final CompilationInfo nextResult) {
        if (nextResult == null) {
            throw new IllegalStateException("nextResult is null");
        }

        return super.aggregateResult(aggregate, nextResult);
    }

    @Override
    public CompilationInfo visitTerminal(final TerminalNode node) {
        if (node.getSymbol().getType() == Token.EOF) {
            return VOID;
        }

        throw new IllegalStateException("Terminal should not be called: '"
                + node.getText() + "' in parent context: '" + node.getParent().getText() + "' (" + node.getParent().getClass().getSimpleName() + ')');
    }

    @Override
    public CompilationInfo visitPrintln(final JimpleParser.PrintlnContext ctx) {
        final MethodVisitor method = getCurrentMethod();
        method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        CompilationInfo expressionType = visit(ctx.expression());
        if (expressionType.isVoid()) {
            // TODO: throw new CodeCompilationException("Type not expected: " + compInfo.getTypeName());
            // println void, probably it's case of recursive function call, just println "VOID"
            // at runtime program will fail with StackOverflowException
            getCurrentMethod().visitLdcInsn("VOID");
            expressionType = CompilationInfo.STRING;
        }
        method.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", makeFuncDescriptor(expressionType, "V"), false);
        return VOID;
    }

    @Override
    public CompilationInfo visitBlockStatement(final JimpleParser.BlockStatementContext ctx) {
        try {
            context.pushBlockScope();
            final List<JimpleParser.StatementContext> statements = ctx.statement();
            for (final JimpleParser.StatementContext statement : statements) {
                visit(statement);
            }
            return CompilationInfo.VOID;
        } finally {
            context.popBlockScope();
        }
    }

    @Override
    public CompilationInfo visitStringExpr(final JimpleParser.StringExprContext ctx) {
        final String value = StringUtil.cleanStringLiteral(ctx.STRING_LITERAL().getText());
        // LDC "some literal string"
        getCurrentMethod().visitLdcInsn(value);
        return CompilationInfo.STRING;
    }

    @Override
    public CompilationInfo visitNumExpr(final JimpleParser.NumExprContext ctx) {
        final long value = Long.parseLong(ctx.NUMBER().getText());
       /* if (value == 0) {
            // LCONST_0
            getCurrentMethod().visitLdcInsn(Opcodes.LCONST_0);
        } else if (value == 1) {
            // LCONST_1
            getCurrentMethod().visitLdcInsn(Opcodes.LCONST_1);
        } else*/
        {
            // LDC long_value
            getCurrentMethod().visitLdcInsn(value);
        }
        return CompilationInfo.NUMBER;
    }

    @Override
    public CompilationInfo visitDoubleExpr(final JimpleParser.DoubleExprContext ctx) {
        final Double value = Double.valueOf(ctx.getText());
        getCurrentMethod().visitLdcInsn(value);
        return CompilationInfo.DOUBLE;
    }

    @Override
    public CompilationInfo visitBooleanExpr(final JimpleParser.BooleanExprContext ctx) {
        if ("true".equals(ctx.BOOLEAN().getText())) {
            getCurrentMethod().visitInsn(ICONST_1);
        } else {
            getCurrentMethod().visitInsn(ICONST_0);
        }
        return CompilationInfo.BOOLEAN;
    }

    @Override
    public CompilationInfo visitIfStatement(final JimpleParser.IfStatementContext ctx) {
        final CompilationInfo value = visit(ctx.expression());
        if (!value.isBoolean()) {
            throw new CodeCompilationException("The \"if\" condition must be of boolean type only. But found: " + value.getTypeName());
        }

        final MethodVisitor method = getCurrentMethod();
        final Label labelFalse = new Label();
        final Label labelExit = new Label();
        final boolean hasElse = ctx.elseStatement() != null;
        // if the value is false, then go to the else statement or exit if there is no else
        method.visitJumpInsn(IFEQ, hasElse ? labelFalse : labelExit);

        // true
        visit(ctx.statement());

        if (hasElse) {
            method.visitJumpInsn(GOTO, labelExit);
            // false
            method.visitLabel(labelFalse);
            visit(ctx.elseStatement());
        }
        method.visitLabel(labelExit);

        return CompilationInfo.VOID;
    }

    @Override
    public CompilationInfo visitElseStatement(JimpleParser.ElseStatementContext ctx) {
        return visit(ctx.statement());
    }

    @Override
    public CompilationInfo visitWhileStatement(final JimpleParser.WhileStatementContext ctx) {
        final MethodVisitor method = getCurrentMethod();
        final Label loopCheck = new Label();
        final Label loopEnd = new Label();

        // check boolean statement
        method.visitLabel(loopCheck);
        visit(ctx.expression());
        method.visitJumpInsn(IFEQ, loopEnd);

        // body
        visit(ctx.statement());

        // Go back to loopCheck
        method.visitJumpInsn(GOTO, loopCheck);

        method.visitLabel(loopEnd);

        return CompilationInfo.VOID;
    }

    @Override
    public CompilationInfo visitParenthesisExpr(final JimpleParser.ParenthesisExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public CompilationInfo visitReturn(final JimpleParser.ReturnContext ctx) {
        final CompilationInfo result = visit(ctx.expression());
        final MethodVisitor currentMethod = getCurrentMethod();
        if (currentMethod instanceof DeferredMethodVisitor deferredMethod) {
            deferredMethod.setReturnType(result);
        }
        currentMethod.visitInsn(getReturnOpcodeByType(result));
        return result;
    }

    @Override
    public CompilationInfo visitReturnVoid(final JimpleParser.ReturnVoidContext ctx) {
        final MethodVisitor currentMethod = getCurrentMethod();
        if (currentMethod instanceof DeferredMethodVisitor deferredMethod) {
            deferredMethod.setReturnType(CompilationInfo.VOID);
        }
        currentMethod.visitInsn(getReturnOpcodeByType(CompilationInfo.VOID));
        return CompilationInfo.VOID;
    }

    @Override
    public CompilationInfo visitPlusMinusExpr(final JimpleParser.PlusMinusExprContext ctx) {
        final MethodVisitor method = getCurrentMethod();
        final int typeLeft = getExpressionType(ctx.left);
        final int typeRight = getExpressionType(ctx.right);

        // check if plus is string concat operator
        if (ctx.op.getType() == JimpleParser.PLUS && (isStaticString(typeLeft) || isStaticString(typeRight))) {
            method.visitTypeInsn(NEW, TYPE_STRING_BUILDER);
            method.visitInsn(DUP);
            method.visitMethodInsn(INVOKESPECIAL, TYPE_STRING_BUILDER, "<init>", "()V", false);
            final CompilationInfo left = visit(ctx.left);
            final String typeDescrLeft = makeFuncDescriptor(left, "Ljava/lang/StringBuilder;");
            method.visitMethodInsn(INVOKEVIRTUAL, TYPE_STRING_BUILDER, "append", typeDescrLeft, false);
            final CompilationInfo right = visit(ctx.right);
            final String typeDescrRight = makeFuncDescriptor(right, "Ljava/lang/StringBuilder;");
            method.visitMethodInsn(INVOKEVIRTUAL, TYPE_STRING_BUILDER, "append", typeDescrRight, false);
            method.visitMethodInsn(INVOKEVIRTUAL, TYPE_STRING_BUILDER, "toString", "()Ljava/lang/String;", false);
            return CompilationInfo.STRING;
        }

        final int commonType = commonType(typeLeft, typeRight);
        switch (ctx.op.getType()) {
            case JimpleParser.PLUS:
                visit(ctx.left);
                convertAfterFirstArg(typeLeft, typeRight);
                visit(ctx.right);
                convertAfterSecondArg(typeLeft, typeRight);
                method.visitInsn(getAddOpcode(commonType));
                return new CompilationInfo(commonType);
            case JimpleParser.MINUS:
                visit(ctx.left);
                convertAfterFirstArg(typeLeft, typeRight);
                visit(ctx.right);
                convertAfterSecondArg(typeLeft, typeRight);
                method.visitInsn(getSubOpcode(commonType));
                return new CompilationInfo(commonType);
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + ctx.op.getText());
        }

        // TODO: return final type info of expression
    }

    @Override
    public CompilationInfo visitMulDivExpr(final JimpleParser.MulDivExprContext ctx) {
        final MethodVisitor method = getCurrentMethod();
        final int typeLeft = getExpressionType(ctx.left);
        final int typeRight = getExpressionType(ctx.right);

        final int commonType = commonType(typeLeft, typeRight);
        switch (ctx.op.getType()) {
            case JimpleParser.ASTERISK:
                visit(ctx.left);
                convertAfterFirstArg(typeLeft, typeRight);
                visit(ctx.right);
                convertAfterSecondArg(typeLeft, typeRight);
                method.visitInsn(getMulOpcode(commonType));
                return new CompilationInfo(commonType);
            case JimpleParser.SLASH:
                visit(ctx.left);
                convertAfterFirstArg(typeLeft, typeRight);
                visit(ctx.right);
                convertAfterSecondArg(typeLeft, typeRight);
                method.visitInsn(getDivOpcode(commonType));
                return new CompilationInfo(commonType);
            case JimpleParser.MOD:
                visit(ctx.left);
                convertAfterFirstArg(typeLeft, typeRight);
                visit(ctx.right);
                convertAfterSecondArg(typeLeft, typeRight);
                method.visitInsn(getModOpcode(commonType));
                return new CompilationInfo(commonType);
            default:
                throw new UnsupportedOperationException("TODO operator: " + ctx.op.getText());
        }
    }

    @Override
    public CompilationInfo visitFunctionCall(final JimpleParser.FunctionCallContext ctx) {
        final String name = ctx.IDENTIFIER().getText();
        final List<CompilationInfo> arguments;
        if (isSkipFunctionCall()) {
            arguments = ctx.expression().stream().map(this::getExpressionTypeInfo).toList();
        } else {
            arguments = ctx.expression().stream().map(this::visit).toList();
        }
        final MethodVisitor currentMethod = getCurrentMethod();
        final FunctionSignature funSignature = context.getFunctionSig(FunctionSignature.of(name, arguments.size(), ctx));
        final String argsDescriptor = makeFuncArgsDescriptor(arguments);

        if (funSignature.isNative()) {
            // when native function just call it
            final NativeFuncInfo nativeFuncInfo = funSignature.nativeInfo();
            nativeFuncInfo.validateArguments(arguments);
            final CompilationInfo funcReturnType = nativeFuncInfo.returnType();
            if (!isSkipFunctionCall()) {
                final String descriptor = argsDescriptor + getAsmType(funcReturnType);
                currentMethod.visitMethodInsn(INVOKESTATIC, nativeFuncInfo.className(), nativeFuncInfo.realMethodName(), descriptor, false);
            }
            return funcReturnType;
        }

        if (!funSignature.methods().containsKey(argsDescriptor)) {
            generateNewMethod(funSignature, argsDescriptor, arguments);
        }
        CompilationInfo funcReturnType = funSignature.methods().get(argsDescriptor);
        if (currentMethod instanceof DeferredMethodVisitor deferredMethod && deferredMethod.getReturnType() != funcReturnType) {
            if (deferredMethod.getFunSignature().equals(funSignature) && deferredMethod.getArguments().equals(arguments)) {
                // recursive function call, set type from already visited 'return'
                funcReturnType = deferredMethod.getReturnType();
            }
        }

        if (!isSkipFunctionCall()) {
            if (funcReturnType == null) {
                // we cannot find out the type of the function, probably recursive function call
                funcReturnType = CompilationInfo.VOID;
            }

            final String descriptor = argsDescriptor + getAsmType(funcReturnType);
            // invoke method
            currentMethod.visitMethodInsn(INVOKESTATIC, JIMPLE_MAIN_CLASS_NAME, name, descriptor, false);
            // check if function call it's just a statement
            if (ctx.getParent() instanceof JimpleParser.StatementContext && !funcReturnType.isVoid()) {
                // if result of the function is not used we should to discard one or two values from the stack
                if (funcReturnType.isDouble() || funcReturnType.isNumber()) {
                    currentMethod.visitInsn(POP2);
                } else {
                    currentMethod.visitInsn(POP);
                }
            }
        }

        return funcReturnType;
    }

    private boolean isSkipFunctionCall() {
        return skipInvokeFunc.peek() == getCurrentMethod();
    }

    @Override
    public CompilationInfo visitFuncCallExpr(final JimpleParser.FuncCallExprContext ctx) {
        return super.visitFuncCallExpr(ctx);
    }

    @Override
    public CompilationInfo visitFunctionDefinition(final JimpleParser.FunctionDefinitionContext ctx) {
        // skip function definition as we must generate separate method for different arguments
        return VOID;
    }

    @Override
    public CompilationInfo visitIdExp(final JimpleParser.IdExpContext ctx) {
        // ALOAD varIndex
        final TerminalNode identifier = ctx.IDENTIFIER();
        final CompilationInfo varType = context.getVarType(identifier);
        getCurrentMethod().visitVarInsn(getLoadByType(varType), context.getVarIndex(identifier));
        return varType;
    }

    @Override
    public CompilationInfo visitVariableDeclaration(final JimpleParser.VariableDeclarationContext ctx) {
        final TerminalNode identifier = ctx.IDENTIFIER();
        context.registerVariable(identifier, visit(ctx.expression()));
        final MethodVisitor method = getCurrentMethod();
        // XSTORE varIndex
        method.visitVarInsn(storeOpcode(ctx.expression()), context.getVarIndex(identifier));
        return VOID;
    }

    @Override
    public CompilationInfo visitAssignment(final JimpleParser.AssignmentContext ctx) {
        final CompilationInfo info = visit(ctx.expression());
        getCurrentMethod().visitVarInsn(getStoreByType(info), context.getVarIndex(ctx.IDENTIFIER()));

        return VOID;
    }

    @Override
    public CompilationInfo visitCompExpr(final JimpleParser.CompExprContext ctx) {
        final MethodVisitor method = getCurrentMethod();
        final int leftType = getExpressionType(ctx.left);
        final int rightType = getExpressionType(ctx.right);
        final int operator = ctx.compOperator().op.getType();
        if (leftType == JimpleParser.STRING_LITERAL && rightType == JimpleParser.STRING_LITERAL) {
            // "s1" > "s2" equivalent to "s1".compareTo("s2") > 0
            visit(ctx.left);
            visit(ctx.right);
            // Call "s1".compareTo("s2")
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "compareTo", makeFuncDescriptor(CompilationInfo.STRING, "I"), false);
            return visitCmpInternal(method, operator);
        }
        visit(ctx.left);
        visit(ctx.right);
        // l1 > l2, l2 < l1, ...
        method.visitInsn(Opcodes.LCMP);
        return visitCmpInternal(method, operator);
    }

    /**
     * Generates something like "([Ljava/lang/String;)V"
     */
    private String makeFuncDescriptor(final List<CompilationInfo> arguments, final String resultType) {
        return makeFuncArgsDescriptor(arguments) + resultType;
    }

    private static CompilationInfo visitCmpInternal(final MethodVisitor method, final int operator) {
        // all used labels
        final Label labelTrue = new Label();
        final Label labelExit = new Label();

        if (operator == JimpleParser.GREATER) {
            // if (ret > 0) jump to labelTrue
            method.visitJumpInsn(IFGT, labelTrue);
        } else if (operator == JimpleParser.LESS) {
            // if (ret < 0) jump to labelTrue
            method.visitJumpInsn(IFLT, labelTrue);
        } else if (operator == JimpleParser.EQUAL) {
            // if (ret == 0) jump to labelTrue
            method.visitJumpInsn(IFEQ, labelTrue);
        } else if (operator == JimpleParser.NOT_EQUAL) {
            // if (ret != 0) jump to labelTrue
            method.visitJumpInsn(IFNE, labelTrue);
        } else if (operator == JimpleParser.GREATER_OR_EQUAL) {
            // if (ret >= 0) jump to labelTrue
            method.visitJumpInsn(IFGE, labelTrue);
        } else if (operator == JimpleParser.LESS_OR_EQUAL) {
            // if (ret <= 0) jump to labelTrue
            method.visitJumpInsn(IFLE, labelTrue);
        }
        // return false/0
        method.visitInsn(ICONST_0);
        method.visitJumpInsn(GOTO, labelExit);

        // return true/1
        method.visitLabel(labelTrue);
        method.visitInsn(ICONST_1);

        method.visitLabel(labelExit);
        return CompilationInfo.BOOLEAN;
    }

    private String makeFuncDescriptor(final CompilationInfo compInfo, final String resultType) {
        return makeFuncDescriptor(Collections.singletonList(compInfo), resultType);
    }

    private static String makeFuncArgsDescriptor(final List<CompilationInfo> arguments) {
        final StringBuilder sb = new StringBuilder("(");
        for (final CompilationInfo argument : arguments) {
            sb.append(getAsmType(argument));
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Equivalent to {@link org.objectweb.asm.Type#getType(Class)}.
     */
    private static String getAsmType(final CompilationInfo compInfo) {
        switch (compInfo.type()) {
            case JimpleParser.NUMBER -> {
                return "J";
            }
            case JimpleParser.DOUBLE_NUMBER -> {
                return "D";
            }
            case JimpleParser.BOOLEAN -> {
                return "Z";
            }
            case JimpleParser.VOID -> {
                return "V";
            }
            case JimpleParser.STRING_LITERAL -> {
                return "Ljava/lang/String;";
            }
        }

        throw new UnsupportedOperationException("TODO: " + compInfo);
    }

    private void convertAfterSecondArg(final int typeLeft, final int typeRight) {
        if (typeLeft == JimpleParser.DOUBLE_NUMBER && typeRight == JimpleParser.NUMBER) {
            // long -> double converting
            getCurrentMethod().visitInsn(L2D);
        }

        if (typeLeft != JimpleParser.DOUBLE_NUMBER && typeLeft != JimpleParser.NUMBER) {
            throw new UnsupportedOperationException("Unsupported type: " + typeLeft);
        }
        if (typeRight != JimpleParser.DOUBLE_NUMBER && typeRight != JimpleParser.NUMBER) {
            throw new UnsupportedOperationException("Unsupported type: " + typeRight);
        }
    }

    private void convertAfterFirstArg(final int typeLeft, final int typeRight) {
        if (typeLeft == JimpleParser.NUMBER && typeRight == JimpleParser.DOUBLE_NUMBER) {
            // long -> double converting
            getCurrentMethod().visitInsn(L2D);
        }

        if (typeLeft != JimpleParser.DOUBLE_NUMBER && typeLeft != JimpleParser.NUMBER) {
            throw new UnsupportedOperationException("Unsupported type: " + typeLeft);
        }
        if (typeRight != JimpleParser.DOUBLE_NUMBER && typeRight != JimpleParser.NUMBER) {
            throw new UnsupportedOperationException("Unsupported type: " + typeRight);
        }
    }

    private static boolean isStaticString(final int type) {
        return type == JimpleParser.STRING_LITERAL;
    }

    /**
     * Generates new method and calculate it's return type.
     *
     * @param funSignature   func signature
     * @param argsDescriptor description for all arguments
     * @param arguments      actual arguments
     */
    private void generateNewMethod(final FunctionSignature funSignature, final String argsDescriptor, final List<CompilationInfo> arguments) {
        funSignature.methods().put(argsDescriptor, VOID);
        final JimpleParser.FunctionDefinitionContext funcDefinition = (JimpleParser.FunctionDefinitionContext) funSignature.context();
        final DeferredMethodVisitor deferredMethodVisitor = new DeferredMethodVisitor(funSignature, arguments);
        deferredMethodVisitor.visitCode();
        methods.push(deferredMethodVisitor);
        final Map<String, Object> args = new LinkedHashMap<>(arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            args.put(funcDefinition.IDENTIFIER(i + 1).getText(), arguments.get(i));
        }
        context.pushCallScope(args);
        final List<JimpleParser.StatementContext> statements = funcDefinition.statement();
        for (final JimpleParser.StatementContext statement : statements) {
            visit(statement);
        }
        methods.pop();
        final CompilationInfo funcReturnType = Optional.ofNullable(deferredMethodVisitor.getReturnType()).orElse(CompilationInfo.VOID);
        if (funcReturnType.isVoid()) {
            deferredMethodVisitor.visitInsn(RETURN);
        }
        deferredMethodVisitor.visitEnd();
        context.popCallScope();

        // apply all collected visits
        // now we know func's return type, and we can create method description
        final String descriptor = argsDescriptor + getAsmType(funcReturnType);
        final MethodVisitor method = classVisitor.visitMethod(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, funSignature.name(), descriptor, null, null);
        // replace a real type of recursive function call
        deferredMethodVisitor.patchDescriptor(funSignature.name(), argsDescriptor + getAsmType(CompilationInfo.VOID), descriptor);
        deferredMethodVisitor.apply(method);
        // update function return type
        funSignature.methods().put(argsDescriptor, funcReturnType);
    }

    private static int getReturnOpcodeByType(final CompilationInfo info) {
        switch (info.type()) {
            case JimpleParser.VOID:
                return Opcodes.RETURN;
            case JimpleParser.DOUBLE_NUMBER:
                return Opcodes.DRETURN;
            case JimpleParser.NUMBER:
                return Opcodes.LRETURN;
            case JimpleParser.STRING_LITERAL:
                return Opcodes.ARETURN;
            case JimpleParser.BOOLEAN:
                return Opcodes.IRETURN;
            default:
                throw new UnsupportedOperationException("TODO: type - " + info);
        }
    }

    private int storeOpcode(final JimpleParser.ExpressionContext expression) {
        return storeOpcode(getExpressionType(expression));
    }

    private int storeOpcode(final int type) {
        switch (type) {
            case JimpleParser.DOUBLE_NUMBER -> {
                return Opcodes.DSTORE;
            }
            case JimpleParser.STRING_LITERAL -> {
                return Opcodes.ASTORE;
            }
            case JimpleParser.NUMBER -> {
                return Opcodes.LSTORE;
            }
            default -> throw new IllegalStateException("Unexpected expression type: " + type);
        }
    }

    private static int getLoadByType(final CompilationInfo info) {
        switch (info.type()) {
            case JimpleParser.DOUBLE_NUMBER:
                return Opcodes.DLOAD;
            case JimpleParser.NUMBER:
                return Opcodes.LLOAD;
            case JimpleParser.STRING_LITERAL:
                return Opcodes.ALOAD;
            default:
                throw new UnsupportedOperationException("TODO: type - " + info);
        }
    }

    private int getStoreByType(final CompilationInfo info) {
        return storeOpcode(info.type());
    }

    private static int getAddOpcode(final int commonType) {
        final int type = commonType;

        switch (type) {
            case JimpleParser.DOUBLE_NUMBER -> {
                return Opcodes.DADD;
            }
            case JimpleParser.NUMBER -> {
                return Opcodes.LADD;
            }
            default -> throw new IllegalStateException("Unexpected expression type: " + type);
        }
    }

    private int getSubOpcode(final int commonType) {
        final int type = commonType;

        switch (type) {
            case JimpleParser.DOUBLE_NUMBER -> {
                return Opcodes.DSUB;
            }
            case JimpleParser.NUMBER -> {
                return Opcodes.LSUB;
            }
            default -> throw new IllegalStateException("Unexpected expression type: " + type);
        }
    }

    private int getDivOpcode(final int commonType) {
        final int type = commonType;

        switch (type) {
            case JimpleParser.DOUBLE_NUMBER -> {
                return Opcodes.DDIV;
            }
            case JimpleParser.NUMBER -> {
                return Opcodes.LDIV;
            }
            default -> throw new IllegalStateException("Unexpected expression type: " + type);
        }
    }

    private int getModOpcode(final int commonType) {
        final int type = commonType;

        switch (type) {
            case JimpleParser.DOUBLE_NUMBER -> {
                return Opcodes.DREM;
            }
            case JimpleParser.NUMBER -> {
                return Opcodes.LREM;
            }
            default -> throw new IllegalStateException("Unexpected expression type: " + type);
        }
    }

    private static int getMulOpcode(final int commonType) {
        final int type = commonType;

        switch (type) {
            case JimpleParser.DOUBLE_NUMBER -> {
                return Opcodes.DMUL;
            }
            case JimpleParser.NUMBER -> {
                return Opcodes.LMUL;
            }
            default -> throw new IllegalStateException("Unexpected expression type: " + type);
        }
    }


    private static int commonType(final int typeLeft, final int typeRight) {
        final int type;

        switch (typeLeft) {
            case JimpleParser.DOUBLE_NUMBER -> {
                switch (typeRight) {
                    case JimpleParser.NUMBER, JimpleParser.DOUBLE_NUMBER -> {
                        type = JimpleParser.DOUBLE_NUMBER;
                    }
                    default -> throw new IllegalStateException("Unexpected expression type: " + typeRight);
                }
            }
            case JimpleParser.NUMBER -> {
                switch (typeRight) {
                    case JimpleParser.DOUBLE_NUMBER -> {
                        type = JimpleParser.DOUBLE_NUMBER;
                    }
                    case JimpleParser.NUMBER -> {
                        type = JimpleParser.NUMBER;
                    }
                    default -> throw new IllegalStateException("Unexpected expression type: " + typeRight);
                }
            }
            default -> throw new IllegalStateException("Unexpected expression type: " + typeRight);
        }
        return type;
    }

    private int getExpressionType(final JimpleParser.ExpressionContext expression) {
        if (expression instanceof JimpleParser.DoubleExprContext) {
            return JimpleParser.DOUBLE_NUMBER;
        } else if (expression instanceof JimpleParser.StringExprContext) {
            return JimpleParser.STRING_LITERAL;
        } else if (expression instanceof JimpleParser.NumExprContext) {
            return JimpleParser.NUMBER;
        } else if (expression instanceof JimpleParser.ParenthesisExprContext innerExpression) {
            return getExpressionType(innerExpression.expression());
        } else if (expression instanceof final JimpleParser.IdExpContext idContext) {
            return context.getVarType(idContext.IDENTIFIER()).type();
        } else if (expression instanceof final JimpleParser.MulDivExprContext mulDivExp) {
            return commonType(getExpressionType(mulDivExp.left), getExpressionType(mulDivExp.right));
        } else if (expression instanceof final JimpleParser.PlusMinusExprContext plusMinusExp) {
            return commonType(getExpressionType(plusMinusExp.left), getExpressionType(plusMinusExp.right));
        } else if (expression instanceof JimpleParser.FuncCallExprContext) {
            // to find out func return type we need to generate it, but without invoke the function!
            this.skipInvokeFunc.push(getCurrentMethod());
            final CompilationInfo funcType = visit(expression);
            this.skipInvokeFunc.pop();
            return funcType.type();
        }

        throw new UnsupportedOperationException("TODO handle expression: '" + expression.getText() + "' (" + expression.getClass().getName() + ')');
    }

    private CompilationInfo getExpressionTypeInfo(final JimpleParser.ExpressionContext expression) {
        return new CompilationInfo(getExpressionType(expression));
    }

    private MethodVisitor getCurrentMethod() {
        return Objects.requireNonNull(methods.peek(), "No current method was found");
    }
}
