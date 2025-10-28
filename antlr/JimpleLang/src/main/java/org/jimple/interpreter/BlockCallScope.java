package org.jimple.interpreter;

import org.jimple.compiler.CompilationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scope of function or block.
 *
 * @param variables  parameters and local variables
 * @param parent     parent of the current scope
 * @param indexBase  base value for variable index
 * @param isFunction current scope is function, otherwise is simple block
 */
public record BlockCallScope(Map<String, Object> variables, BlockCallScope parent, int indexBase, boolean isFunction) {

    public BlockCallScope(Map<String, Object> variables, BlockCallScope parent, boolean isFunction) {
        this(variables, parent, isFunction ? 0 : parent.getVarSlots(), isFunction);
    }

    public static final String VARIABLE_NOT_FOUND = "Variable not found: %s";

    public Object getVarValue(final String id) {
        if (variables.containsKey(id)) {
            return variables.get(id);
        }

        return getBlockParentOrThrow(VARIABLE_NOT_FOUND, id).getVarValue(id);
    }

    /**
     * Returns index of variable by id.
     */
    public int getVarIndex(final String id) {
        final List<String> names = new ArrayList<>(variables.keySet());
        final int index = names.indexOf(id);
        if (index >= 0) {
            // double and number/long use two slots of stack
            int slots = 0;
            for (int i = 0; i < index; i++) {
                final String name = names.get(i);
                final CompilationInfo info = (CompilationInfo) variables.get(name);
                slots += slotSize(info);
            }
            return indexBase + slots;
        }

        return getBlockParentOrThrow(VARIABLE_NOT_FOUND, id).getVarIndex(id);
    }

    public CompilationInfo getVarType(final String id) {
        final Object object = variables.get(id);
        if (object != null) {
            return ((CompilationInfo) object);
        }

        return getBlockParentOrThrow(VARIABLE_NOT_FOUND, id).getVarType(id);
    }

    public boolean hasVariable(final String id) {
        if (variables.containsKey(id)) {
            return true;
        }

        return parent != null && !isFunction && parent.hasVariable(id);
    }

    public void setVarValue(final String id, final Object value) {
        if (variables.containsKey(id)) {
            variables.put(id, value);
            return;
        }

        if (parent == null) {
            throw new IllegalStateException("Variable not found: " + id);
        }

        parent.setVarValue(id, value);
    }

    public void registerVariable(final String id, final Object value) {
        // check parents scopes as well
        if (hasVariable(id)) {
            throw new IllegalStateException("Variable already exists: " + id);
        }
        variables.put(id, value);
    }

    private int getVarSlots() {
        int slots = 0;

        for (final Object object : variables.values()) {
            if (object instanceof CompilationInfo info) {
                slots += slotSize(info);
            }
        }

        if (!isFunction && parent != null) {
            // for block, we should summarize parents variable slots
            slots += parent.getVarSlots();
        }

        return slots;
    }

    private int slotSize(final CompilationInfo info) {
        if (info.isDouble() || info.isNumber()) {
            return 2;
        } else {
            return  1;
        }
    }

    private BlockCallScope getBlockParentOrThrow(final String format, final Object... args) {
        if (parent == null || isFunction) {
            throw new IllegalStateException(String.format(format, args));
        }
        return parent;
    }

    public int getParameterAndVariableCount() {
        return variables.size();
    }
}
