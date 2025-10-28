package org.jimple.diagnotics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullPrintStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jimple.interpreter.FunctionCallHandler;
import org.jimple.interpreter.FunctionDefinitionVisitor;
import org.jimple.interpreter.FunctionSignature;
import org.jimple.interpreter.JimpleContext;
import org.jimple.interpreter.JimpleContextImpl;
import org.jimple.lang.JimpleBaseVisitor;
import org.jimple.lang.JimpleLexer;
import org.jimple.lang.JimpleParser;
import org.jimple.util.NumberUtil;

/**
 * Class for finding semantic errors in Jimple code
 */
public class JimpleDiagnosticTool extends JimpleBaseVisitor<ValidationInfo> implements ANTLRErrorListener, FunctionCallHandler {
    private static final String UNSUPPORTED_OPERATOR = "Unsupported operator: ";
    private final Collection<Issue> issues = new LinkedHashSet<>(0);
    private final List<String> lines;
    private final Path path;
    private final JimpleContext context;
    // true when we check function definition, not ordinal call
    private boolean checkFuncDefinition;

    // set of already checked functions
    private final Set<JimpleParser.FunctionDefinitionContext> checkedFuncs = new HashSet<>(0);

    // currently calling funcstions
    private final Set<JimpleParser.FunctionDefinitionContext> calledFuncs = new HashSet<>(0);

    public JimpleDiagnosticTool(final String input, final Path path) {
        super();
        this.lines = input.lines().toList();
        this.path = path;
        this.context = new JimpleContextImpl(new NullPrintStream());
    }

    /**
     * Validates Jimple source code
     */
    public static List<Issue> validate(final Path path) throws IOException {
        final String input = IOUtils.toString(path.toUri(), StandardCharsets.UTF_8);
        return validate(input, path);
    }

    /**
     * Validates Jimple source code
     */
    public static List<Issue> validate(final String input, final Path path) {
        final JimpleLexer lexer = new JimpleLexer(CharStreams.fromString(input));
        final JimpleParser parser = new JimpleParser(new CommonTokenStream(lexer));
        final JimpleDiagnosticTool diagnosticTool = new JimpleDiagnosticTool(input, path);
        // remove std listeners (e.g. ConsoleErrorListener)
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        // add our listener
        lexer.addErrorListener(diagnosticTool);
        parser.addErrorListener(diagnosticTool);
        diagnosticTool.visitProgram(parser.program());
        return diagnosticTool.getIssues();
    }

    public List<Issue> getIssues() {
        return issues.stream().toList();
    }

    @Override
    public ValidationInfo visitProgram(final JimpleParser.ProgramContext ctx) {
        // before validating script find all function definitions
        new FunctionDefinitionVisitor(this.context, this).visitProgram(ctx);
        final ValidationInfo result = super.visitProgram(ctx);
        checkUnusedFunctions();
        return result;
    }

    private void checkUnusedFunctions() {
        context.getAllFunctions().stream()
                .filter(func -> func.calledCount() <= 0 && func.signature().isNonNative())
                .forEach(info -> {
                    final JimpleParser.FunctionDefinitionContext funDefCtx = (JimpleParser.FunctionDefinitionContext) info.signature().context();
                    addIssue(IssueType.WARNING, funDefCtx.name, "Function declared but not used: " + info.signature().name());
                });
    }

    @Override
    public ValidationInfo visit(final ParseTree tree) {
        if (tree instanceof ParserRuleContext ruleContext && ruleContext.exception != null) {
            // skip validating on syntax error
            return ValidationInfo.SKIP;
        }

        return super.visit(tree);
    }

    @Override
    public ValidationInfo visitPrintln(final JimpleParser.PrintlnContext ctx) {
        final ValidationInfo info = visit(ctx.expression());

        Validate.notNull(info, "unsupported: %s", ctx.expression().getClass().getSimpleName());

        if (info.isSkip()) {
            return ValidationInfo.SKIP;
        }

        if (!info.isPrintable()) {
            addIssue(IssueType.ERROR, ctx.expression().start, "This type cannot be print: " + info.type());
        }
        return ValidationInfo.VOID;
    }

    @Override
    public ValidationInfo visitVariableDeclaration(final JimpleParser.VariableDeclarationContext ctx) {
        context.registerVariable(ctx.IDENTIFIER(), visit(ctx.expression()));
        return ValidationInfo.VOID;
    }

    @Override
    public ValidationInfo visitDoubleExpr(final JimpleParser.DoubleExprContext ctx) {
        return ValidationInfo.doubleVal(Double.valueOf(ctx.getText()));
    }

    @Override
    public ValidationInfo visitStringExpr(final JimpleParser.StringExprContext ctx) {
        return ValidationInfo.string(ctx.getText());
    }

    @Override
    public ValidationInfo visitNumExpr(final JimpleParser.NumExprContext ctx) {
        return ValidationInfo.number(Long.valueOf(ctx.getText()));
    }

    @Override
    public ValidationInfo visitCompExpr(final JimpleParser.CompExprContext ctx) {
        final ValidationInfo left = visit(ctx.left);
        if (left.isSkip()) {
            return ValidationInfo.SKIP;
        }
        final ValidationInfo right = visit(ctx.right);
        if (right.isSkip()) {
            return ValidationInfo.SKIP;
        }

        if (left.isNumeric() && right.isNumeric()) {
            return evalNumberComparisonOperator(left, right, ctx.compOperator().op);
        }

        if (left.isString() && right.isString()) {
            return evalStringComparisonOperator(left, right, ctx.compOperator().op);
        }

        if (left.isAny() || right.isAny()) {
            return ValidationInfo.bool(null);
        }

        operatorNotSupported(ctx.compOperator().op, left, right);

        return ValidationInfo.SKIP;
    }

    @Override
    public ValidationInfo visitMulDivExpr(final JimpleParser.MulDivExprContext ctx) {
        return checkBinaryOperatorCommon(ctx.left, ctx.right, ctx.op);
    }

    @Override
    public ValidationInfo visitPlusMinusExpr(final JimpleParser.PlusMinusExprContext ctx) {
        return checkBinaryOperatorCommon(ctx.left, ctx.right, ctx.op);
    }

    private ValidationInfo checkBinaryOperatorForNumeric(final ValidationInfo left, final ValidationInfo right, final Token op) {
        if (!left.hasValue() || !right.hasValue()) {
            return getCommonNumericType(left, right);
        }

        if (op.getType() == JimpleParser.SLASH && ((Number) right.value()).longValue() == 0) {
            // if we have value of right's part of division expression and it's zero
            addIssue(IssueType.WARNING, op, "Division by zero");
            return getCommonNumericType(left, right);
        }

        final var valResult = NumberUtil.evalBinaryOperator((Number) left.value(), (Number) right.value(), op);

        if (valResult instanceof final Double val) {
            return ValidationInfo.doubleVal(val);
        } else if (valResult instanceof final Long val) {
            return ValidationInfo.number(val);
        }

        throw new IllegalStateException("Unsupported value type: " + valResult.getClass());
    }

    private ValidationInfo checkBinaryOperatorForAny(final ValidationInfo left, final ValidationInfo right, final Token op) {
        if (left.hasValue() && right.hasValue()) {
            final boolean namesAreEqual = left.asString().equals(right.asString());

            if (namesAreEqual) {
                switch (op.getType()) {
                    case JimpleParser.PLUS, JimpleParser.ASTERISK:
                        break;
                    case JimpleParser.MINUS:
                        return ValidationInfo.number(0L);
                    case JimpleParser.SLASH:
                        return ValidationInfo.number(1L);
                    default:
                        throw new IllegalStateException(UNSUPPORTED_OPERATOR + op);
                }
            }
        }

        return ValidationInfo.any(null);
    }

    private static ValidationInfo getCommonNumericType(final ValidationInfo left, final ValidationInfo right) {
        if (left.type() == ValidationType.DOUBLE || right.type() == ValidationType.DOUBLE) {
            return ValidationInfo.doubleVal(null);
        }

        return ValidationInfo.number(null);
    }

    private ValidationInfo checkBinaryOperatorCommon(final ParseTree leftExp, final ParseTree rightExp, final Token operator) {
        final ValidationInfo left = visit(leftExp);
        if (left.isSkip()) {
            return ValidationInfo.SKIP;
        }
        final ValidationInfo right = visit(rightExp);
        if (right.isSkip()) {
            return ValidationInfo.SKIP;
        }

        if (left.isNumeric() && right.isNumeric()) {
            return checkBinaryOperatorForNumeric(left, right, operator);
        }

        if (operator.getType() == JimpleParser.PLUS && (left.isString() || right.isString())) {
            // String concat
            return ValidationInfo.tryStringConcat(left, right);
        }

        if (checkFuncDefinition && (left.isAny() || right.isAny())) {
            if (left.isAny() && right.isAny()) {
                return checkBinaryOperatorForAny(left, right, operator);
            }

            return ValidationInfo.any(null);
        }

        operatorNotSupported(operator, left, right);

        return ValidationInfo.SKIP;
    }

    private void operatorNotSupported(final Token operator, final ValidationInfo left, final ValidationInfo right) {
        addIssue(IssueType.ERROR, operator, String.format("Operator '%s' not supported for such arguments: '%s' and '%s'",
                operator.getText(), left.type(), right.type()));
    }

    @Override
    public ValidationInfo visitIdExp(final JimpleParser.IdExpContext ctx) {
        if (!context.hasVar(ctx.IDENTIFIER())) {
            addIssue(IssueType.ERROR, ctx.start, "Identifier not found: " + ctx.IDENTIFIER().getText());
            return ValidationInfo.SKIP;
        }

        final Object varValue = context.getVarValue(ctx.IDENTIFIER());
        if (varValue instanceof ValidationInfo valInfo) {
            return valInfo;
        }

        throw new IllegalStateException("Unexpected: " + varValue);
    }

    @Override
    public ValidationInfo visitFunctionDefinition(final JimpleParser.FunctionDefinitionContext ctx) {
        // TODO: check function body
        return ValidationInfo.FUNC_DEFINITION;
    }

    @Override
    public ValidationInfo visitFunctionCall(final JimpleParser.FunctionCallContext ctx) {
        final String functionName = ctx.IDENTIFIER().getText();
        final int argumentsCount = ctx.expression().size();
        final FunctionSignature funSignature = FunctionSignature.of(functionName, argumentsCount, ctx);
        final var handler = context.getFunction(funSignature);

        if (handler == null) {
            addIssue(IssueType.ERROR, ctx.start, "Function with such signature not found: " + functionName);
            return ValidationInfo.SKIP;
        }

        final List<ValidationInfo> arguments = ctx.expression().stream().map(this::visit).toList();
        return (ValidationInfo) handler.apply(funSignature, arguments.stream().map(Object.class::cast).toList());
    }

    @Override
    public ValidationInfo visitIfStatement(final JimpleParser.IfStatementContext ctx) {
        // calc expression in "if" condition
        final ValidationInfo condition = visit(ctx.expression());
        if (condition.isSkip()) {
            return ValidationInfo.SKIP;
        }

        if (!condition.isBool()) {
            addIssue(IssueType.WARNING, ctx.expression().start, "The \"if\" condition must be of boolean type only. But found: " + condition.type());
        }

        if (checkFuncDefinition) {
            visit(ctx.statement());
            // as it's just function definition check, check else statement as well
            final JimpleParser.ElseStatementContext elseStatement = ctx.elseStatement();
            if (elseStatement != null) {
                visit(elseStatement);
            }
            return ValidationInfo.VOID;
        }

        // it's not check function definition, it's checking of certain function call
        if (condition.isBool() && condition.hasValue()) {
            // TODO: check when return used
            if (condition.asBoolean()) {
                visit(ctx.statement());
            } else {
                final JimpleParser.ElseStatementContext elseStatement = ctx.elseStatement();
                if (elseStatement != null) {
                    visit(elseStatement);
                }
            }
        }

        return ValidationInfo.VOID;
    }

    @Override
    public ValidationInfo visitParenthesisExpr(final JimpleParser.ParenthesisExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        final int offset = charPositionInLine + 1;
        issues.add(new Issue(IssueType.ERROR, msg, line, offset, makeDetails(line, offset)));
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        // skip
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        // skip
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        // skip
    }

    private void addIssue(final IssueType issueType, final Token token, String message) {
        final int line = token.getLine();
        final int offset = token.getCharPositionInLine() + 1;
        issues.add(new Issue(issueType, message, line, offset, makeDetails(line, offset)));
    }

    private String makeDetails(final int lineNum, final int offset) {
        if (lineNum > 0 && lineNum <= lines.size()) {
            final String line = this.lines.get(lineNum - 1);
            final StringBuilder sb = new StringBuilder(line.length() * 2);
            if (path != null) {
                sb.append(path.toAbsolutePath());
                sb.append(':');
                sb.append(lineNum);
                sb.append(':');
                sb.append(offset);
                sb.append(System.lineSeparator());
            }
            sb.append(line);
            sb.append(System.lineSeparator());
            if (offset > 0) {
                sb.append(StringUtils.repeat(' ', offset - 1));
            }
            sb.append('^');
            return sb.toString();
        }

        return "";
    }

    /**
     * Handler for each function call.
     */
    @Override
    public Object handleFunc(final FunctionSignature func, final List<String> parameters, final List<Object> arguments, final JimpleParser.FunctionDefinitionContext ctx) {
        if (func.nativeInfo() != null) {
            return func.nativeInfo().interpreterReturnType();
        }

        // TODO: use another strategy to check func definition may be in visitFunctionDefinition
        if (!checkedFuncs.contains(ctx)) {
            // if function is not checked yet, check it only once
            checkedFuncs.add(ctx);
            final ValidationInfo result;
            if (checkFuncDefinition) {
                // if we already in checkFuncDefinition mode
                result = handleFuncInternal(parameters, arguments, ctx);
            } else {
                // when definition checked push List<Any> as arguments, because real arguments can be different
                final List<Object> args = parameters.stream()
                        .map(param -> ValidationInfo.any(String.format("%s@%s%08X", param, func.name(), ctx.hashCode())))
                        .map(Object.class::cast)
                        .toList();
                checkFuncDefinition = true;
                result = handleFuncInternal(parameters, args, ctx);
                checkFuncDefinition = false;
            }

            if (result.isSkip()) {
                return ValidationInfo.SKIP;
            }
        }

        // trace call with real arguments
        return handleFuncInternal(parameters, arguments, ctx);
    }

    @Override
    public ValidationInfo visitBlockStatement(final JimpleParser.BlockStatementContext ctx) {
        try {
            context.pushBlockScope();
            return super.visitBlockStatement(ctx);
        } finally {
            context.popBlockScope();
        }
    }

    private ValidationInfo handleFuncInternal(final List<String> parameters, final List<Object> arguments, final JimpleParser.FunctionDefinitionContext ctx) {
        Validate.isTrue(parameters.size() == arguments.size(), "parameters size != arguments size");

        if (calledFuncs.contains(ctx)) {
            addIssue(IssueType.WARNING, ctx.name, String.format("Recursive call of function '%s' can lead to StackOverflow", ctx.name.getText()));
            return ValidationInfo.SKIP;
        }
        calledFuncs.add(ctx);
        final Map<String, Object> variables = new LinkedHashMap<>(parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
            variables.put(parameters.get(i), arguments.get(i));
        }
        // inside function only parameters and local variables are visible
        context.pushCallScope(variables);
        ValidationInfo lastResult = ValidationInfo.VOID;

        final List<JimpleParser.StatementContext> statements = ctx.statement();
        for (int i = 0; i < statements.size(); i++) {
            final JimpleParser.StatementContext statement = statements.get(i);
            final ValidationInfo result = visit(statement);
            // TODO: do we need continue if SKIP occurs?
            // TODO: check when return used
            if (i == statements.size() - 1) {
                lastResult = result;
            }
        }
        context.popCallScope();
        calledFuncs.remove(ctx);

        return lastResult;
    }

    private ValidationInfo evalStringComparisonOperator(final ValidationInfo left, final ValidationInfo right, final Token operator) {
        final boolean value;

        final String leftVal = left.asString();
        final String rightVal = right.asString();
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

        return ValidationInfo.bool(value);
    }

    private ValidationInfo evalNumberComparisonOperator(final ValidationInfo left, final ValidationInfo right, final Token operator) {
        if (!left.hasValue() || !right.hasValue()) {
            return ValidationInfo.bool(null);
        }

        final boolean value;
        final double leftVal = left.numericAsDouble();
        final double rightVal = right.numericAsDouble();

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

        return ValidationInfo.bool(value);
    }
}
