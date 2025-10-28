package org.jimple.interpreter;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jimple.compiler.CompilationInfo;

public class JimpleContextImpl implements JimpleContext {
    // mapping of function handler to function signature
    private final Map<FunctionSignature, BiFunction<FunctionSignature, List<Object>, Object>> functions = new HashMap<>(0);
    // statistics by functions call
    private final Map<FunctionSignature, Integer> functionsCalledCount = new HashMap<>(0);
    private final Deque<BlockCallScope> callScopes = new ArrayDeque<>(0);
    private final PrintStream stdout;

    public JimpleContextImpl(final PrintStream stdout) {
        this.stdout = stdout;
        // set global frame
        callScopes.add(new BlockCallScope(new LinkedHashMap<>(0), null, true));
    }

    @Override
    public boolean hasVar(final TerminalNode identifier) {
        return getLastScope().hasVariable(identifier.getText());
    }

    @Override
    public Object getVarValue(final TerminalNode identifier) {
        return getLastScope().getVarValue(identifier.getText());
    }

    @Override
    public int getVarIndex(final TerminalNode identifier) {
        return getLastScope().getVarIndex(identifier.getText());
    }

    @Override
    public CompilationInfo getVarType(final TerminalNode identifier) {
        return getLastScope().getVarType(identifier.getText());
    }

    @Override
    public void setVarValue(final TerminalNode identifier, final Object value) {
        getLastScope().setVarValue(identifier.getText(), value);
    }

    @Override
    public BlockCallScope registerVariable(final TerminalNode identifier, final Object value) {
        return registerVariable(identifier.getText(), value);
    }

    @Override
    public BlockCallScope registerVariable(final String identifier, final Object value) {
        final BlockCallScope lastScope = getLastScope();
        lastScope.registerVariable(identifier, value);
        return lastScope;
    }

    @Override
    public void registerFunction(final FunctionSignature funcSignature, final BiFunction<FunctionSignature, List<Object>, Object> handler) {
        if (functions.containsKey(funcSignature)) {
            throw new IllegalStateException("Function already exists: " + funcSignature.name());
        }

        functions.put(funcSignature, (sig, list) -> {
            functionsCalledCount.put(funcSignature, 1 + functionsCalledCount.getOrDefault(funcSignature, 0));
            return handler.apply(funcSignature, list);
        });
    }

    @Override
    public BiFunction<FunctionSignature, List<Object>, Object> getFunction(final FunctionSignature funcSignature) {
        final var found = functions.get(funcSignature);

        if (found != null) {
            return found;
        }

        // walk up all parents to find function
        ParserRuleContext context = funcSignature.context().getParent();
        while (context != null) {
            final var foundFunc = functions.get(funcSignature.withContext(context));
            if (foundFunc != null) {
                return foundFunc;
            }
            context = context.getParent();
        }

        return null;
    }

    @Override
    public FunctionSignature getFunctionSig(final FunctionSignature funcSignature) {
        if (functions.containsKey(funcSignature)) {
            // we should return original function signature with definition context
            for (final FunctionSignature functionSignature : functions.keySet()) {
                if (functionSignature.equals(funcSignature)) {
                    return functionSignature;
                }
            }
        }

        // walk up all parents to find function
        ParserRuleContext context = funcSignature.context().getParent();
        while (context != null) {
            final FunctionSignature foundSig = funcSignature.withContext(context);
            for (final FunctionSignature functionSignature : functions.keySet()) {
                if (foundSig.equals(functionSignature)) {
                    return functionSignature;
                }
            }
            context = context.getParent();
        }

        return null;
    }

    @Override
    public List<FunctionInfo> getAllFunctions() {
        return functions.keySet().stream()
                .map(signature -> new FunctionInfo(signature, functionsCalledCount.getOrDefault(signature, 0)))
                .toList();
    }

    @Override
    public void pushCallScope(final Map<String, Object> variables) {
        callScopes.push(new BlockCallScope(variables, null, true));
    }

    @Override
    public void popCallScope() {
        if (callScopes.isEmpty()) {
            throw new IllegalStateException("Call frame not found");
        }

        callScopes.pop();
    }

    @Override
    public PrintStream getStdout() {
        return stdout;
    }

    /**
     * Called when enter new block
     */
    @Override
    public void pushBlockScope() {
        callScopes.push(new BlockCallScope(new LinkedHashMap<>(0), getLastScope(), false));
    }

    /**
     * Called when exit current block
     */
    @Override
    public void popBlockScope() {
        Objects.requireNonNull(callScopes.peek(), "last block has invalid parent scope");

        callScopes.pop();
    }

    private BlockCallScope getLastScope() {
        if (callScopes.isEmpty()) {
            throw new IllegalStateException("Call frame not found");
        }

        return callScopes.peek();
    }
}
