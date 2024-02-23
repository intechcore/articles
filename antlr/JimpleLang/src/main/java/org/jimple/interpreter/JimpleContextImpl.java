package org.jimple.interpreter;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jimple.lang.JimpleParser;

public class JimpleContextImpl implements JimpleContext {
    // mapping of function handler to function signature
    private final Map<FunctionSignature, BiFunction<FunctionSignature, List<Object>, Object>> functions = new HashMap<>(0);
    // statistics by functions call
    private final Map<FunctionSignature, Integer> functionsCalledCount = new HashMap<>(0);
    private final Deque<FunctionCallScope> callScopes = new ArrayDeque<>(0);
    private final PrintStream stdout;

    public JimpleContextImpl(final PrintStream stdout) {
        this.stdout = stdout;
        // set global frame
        callScopes.add(new FunctionCallScope(new HashMap<>(0), null));
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
    public void setVarValue(final TerminalNode identifier, final Object value) {
        getLastScope().setVarValue(identifier.getText(), value);
    }

    @Override
    public void registerVariable(final TerminalNode identifier, final Object value) {
        getLastScope().registerVariable(identifier.getText(), value);
    }

    @Override
    public void registerFunction(final FunctionSignature funcSignature, final BiFunction<FunctionSignature, List<Object>, Object> handler) {
        if (functions.containsKey(funcSignature)) {
            throw new IllegalStateException("Function already exists: " + funcSignature.name());
        }

        functions.put(funcSignature, (sig, list) -> {
            functionsCalledCount.put(funcSignature, 1 + functionsCalledCount.getOrDefault(funcSignature, 0));
            return handler.apply(sig, list);
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
    public List<FunctionInfo> getAllFunctions() {
        return functions.keySet().stream()
                .map(signature -> new FunctionInfo(signature, functionsCalledCount.getOrDefault(signature, 0)))
                .toList();
    }

    @Override
    public void pushCallScope(final Map<String, Object> variables) {
        callScopes.push(new FunctionCallScope(variables, null));
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
        callScopes.push(new FunctionCallScope(new HashMap<>(0), getLastScope()));
    }

    /**
     * Called when exit current block
     */
    @Override
    public void popBlockScope() {
        Objects.requireNonNull(callScopes.peek(), "last block has invalid parent scope");

        callScopes.pop();
    }

    private FunctionCallScope getLastScope() {
        if (callScopes.isEmpty()) {
            throw new IllegalStateException("Call frame not found");
        }

        return callScopes.peek();
    }
}
