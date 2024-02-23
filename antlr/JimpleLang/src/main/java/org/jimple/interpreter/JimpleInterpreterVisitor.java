package org.jimple.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jimple.lang.JimpleBaseVisitor;
import org.jimple.lang.JimpleParser;
import org.jimple.util.NumberUtil;

import static org.jimple.interpreter.JimpleInterpreter.VOID;

public class JimpleInterpreterVisitor extends JimpleBaseVisitor<Object> implements FunctionCallHandler {
    public static final String UNSUPPORTED_OPERATOR = "Unsupported operator: ";
    private final JimpleContext context;

    public JimpleInterpreterVisitor(final JimpleContext context) {
        super();
        this.context = context;
    }

    @Override
    public Object visitProgram(final JimpleParser.ProgramContext ctx) {
        // before executing script find all function definitions
        new FunctionDefinitionVisitor(this.context, this).visitProgram(ctx);
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrintln(final JimpleParser.PrintlnContext ctx) {
        final Object result = visit(ctx.expression());
        if (VOID == result) {
            throw new IllegalStateException("println cannot print void type");
        }
        context.getStdout().println(result);
        return VOID;
    }

    @Override
    public Object visitFunctionCall(final JimpleParser.FunctionCallContext ctx) {
        final String name = ctx.IDENTIFIER().getText();
        final List<Object> arguments = ctx.expression().stream().map(this::visit).toList();
        final FunctionSignature funSignature = FunctionSignature.of(name, arguments.size(), ctx);
        final var handler = context.getFunction(funSignature);

        if (handler == null) {
            throw new IllegalStateException("Function not found: " + name);
        }

        return handler.apply(funSignature, arguments);
    }

    @Override
    public Object visitFunctionDefinition(final JimpleParser.FunctionDefinitionContext ctx) {
        return VOID;
    }

    @Override
    public Object visitReturn(final JimpleParser.ReturnContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitVariableDeclaration(final JimpleParser.VariableDeclarationContext ctx) {
        context.registerVariable(ctx.IDENTIFIER(), visit(ctx.expression()));
        return VOID;
    }

    @Override
    public Object visitAssignment(final JimpleParser.AssignmentContext ctx) {
        context.setVarValue(ctx.IDENTIFIER(), visit(ctx.expression()));
        return VOID;
    }

    @Override
    public Object visitStringExpr(final JimpleParser.StringExprContext ctx) {
        return cleanStringLiteral(ctx.STRING_LITERAL().getText());
    }

    @Override
    public Object visitCompExpr(final JimpleParser.CompExprContext ctx) {
        final Object left = shouldBeNumberString(visit(ctx.left));
        final Object right = shouldBeNumberString(visit(ctx.right));

        if (left instanceof Number && right instanceof Number) {
            return evalNumberComparisonOperator((Number) left, (Number) right, ctx.compOperator().op);
        }

        if (left instanceof String && right instanceof String) {
            return evalStringComparisonOperator((String) left, (String) right, ctx.compOperator().op);
        }

        throw new IllegalStateException(String.format("Operator '%s' not supported for this types: %s and %s",
                ctx.compOperator().op.getText(), getTypeName(left), getTypeName(right)));
    }

    @Override
    public Object visitMulDivExpr(final JimpleParser.MulDivExprContext ctx) {
        final Number left = shouldBeNumber(visit(ctx.left));
        final Number right = shouldBeNumber(visit(ctx.right));
        return NumberUtil.evalBinaryOperator(left, right, ctx.op);
    }

    @Override
    public Object visitPlusMinusExpr(final JimpleParser.PlusMinusExprContext ctx) {
        final Object left = shouldBeNumberString(visit(ctx.left));
        final Object right = shouldBeNumberString(visit(ctx.right));

        if (left instanceof Number && right instanceof Number) {
            return NumberUtil.evalBinaryOperator((Number) left, (Number) right, ctx.op);
        }

        if (ctx.op.getType() == JimpleParser.PLUS) {
            // String concat
            return left.toString() + right.toString();
        }

        throw new IllegalStateException(String.format("Operator '%s' not supported for String", ctx.op.getText()));
    }

    @Override
    public Object visitIfStatement(final JimpleParser.IfStatementContext ctx) {
        final Object condition = visit(ctx.expression());
        if (!(condition instanceof Boolean)) {
            throw new IllegalStateException("The \"if\" condition must be of boolean type only. But found: " + getTypeName(condition));
        }

        if (Boolean.TRUE.equals(condition)) {
            visit(ctx.statement());
        } else {
            final JimpleParser.ElseStatementContext elseStatement = ctx.elseStatement();
            if (elseStatement != null) {
                visit(elseStatement);
            }
        }
        return VOID;
    }

    @Override
    public Object visitParenthesisExpr(final JimpleParser.ParenthesisExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitNumExpr(final JimpleParser.NumExprContext ctx) {
        return Long.valueOf(ctx.NUMBER().getText());
    }

    @Override
    public Object visitDoubleExpr(final JimpleParser.DoubleExprContext ctx) {
        return Double.valueOf(ctx.getText());
    }

    @Override
    public Object visitIdExp(final JimpleParser.IdExpContext ctx) {
        return context.getVarValue(ctx.IDENTIFIER());
    }

    @Override
    public Object visitTerminal(final TerminalNode node) {
        if (node.getSymbol().getType() == JimpleParser.EOF) {
            return VOID;
        }

        return super.visitTerminal(node);
    }

    @Override
    public Object handleFunc(final FunctionSignature func, final List<String> parameters, final List<Object> arguments, final JimpleParser.FunctionDefinitionContext ctx) {
        Validate.isTrue(parameters.size() == arguments.size(), "parameters size != arguments size");

        final Map<String, Object> variables = new HashMap<>(parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
            variables.put(parameters.get(i), arguments.get(i));
        }
        // inside function only parameters and local variables are visible
        context.pushCallScope(variables);
        Object lastResult = VOID;

        final List<JimpleParser.StatementContext> statements = ctx.statement();
        for (int i = 0; i < statements.size(); i++) {
            final JimpleParser.StatementContext statement = statements.get(i);
            final Object result = visit(statement);
            if (i == statements.size() - 1) {
                lastResult = result;
            }
        }
        context.popCallScope();

        return lastResult;
    }

    @Override
    public Object visitBlockStatement(final JimpleParser.BlockStatementContext ctx) {
        try {
            context.pushBlockScope();
            return super.visitBlockStatement(ctx);
        } finally {
            context.popBlockScope();
        }
    }

    private Number shouldBeNumber(final Object obj) {
        if (obj instanceof Number) {
            return (Number) obj;
        }

        throw new IllegalStateException("Expected number but found: " + getTypeName(obj));
    }

    private Object shouldBeNumberString(final Object obj) {
        if (obj instanceof Number || obj instanceof String) {
            return obj;
        }

        throw new IllegalStateException("Expected number or string but found: " + getTypeName(obj));
    }

    private static String getTypeName(final Object obj) {
        return obj != null ? obj.getClass().getSimpleName() : "null";
    }

    private Boolean evalStringComparisonOperator(final String leftVal, final String rightVal, final Token operator) {
        final boolean value;

        switch (operator.getType()) {
            case JimpleParser.LESS:
                value = StringUtils.compare(leftVal, rightVal) < 0;
                break;
            case JimpleParser.LESS_OR_EQUAL:
                value = StringUtils.compare(leftVal, rightVal) <= 0;
                break;
            case JimpleParser.GREATER:
                value = StringUtils.compare(leftVal, rightVal) > 0;
                break;
            case JimpleParser.GREATER_OR_EQUAL:
                value = StringUtils.compare(leftVal, rightVal) >= 0;
                break;
            case JimpleParser.EQUAL:
                value = Objects.equals(leftVal, rightVal);
                break;
            case JimpleParser.NOT_EQUAL:
                value = !Objects.equals(leftVal, rightVal);
                break;
            default:
                throw new IllegalStateException(UNSUPPORTED_OPERATOR + operator.getText());
        }

        return value;
    }

    private Boolean evalNumberComparisonOperator(final Number left, final Number right, final Token operator) {
        final boolean value;
        final double leftVal = left.doubleValue();
        final double rightVal = right.doubleValue();

        switch (operator.getType()) {
            case JimpleParser.LESS:
                value = leftVal < rightVal;
                break;
            case JimpleParser.LESS_OR_EQUAL:
                value = leftVal <= rightVal;
                break;
            case JimpleParser.GREATER:
                value = leftVal > rightVal;
                break;
            case JimpleParser.GREATER_OR_EQUAL:
                value = leftVal >= rightVal;
                break;
            case JimpleParser.EQUAL:
                value = Objects.equals(left, right);
                break;
            case JimpleParser.NOT_EQUAL:
                value = !Objects.equals(left, right);
                break;
            default:
                throw new IllegalStateException(UNSUPPORTED_OPERATOR + operator.getText());
        }

        return value;
    }

    private static String cleanStringLiteral(final String literal) {
        return literal.length() > 1 ? literal.substring(1, literal.length() - 1) : literal;
    }
}
