package org.jimple.interpreter;

import java.util.Map;
import java.util.Objects;

/**
 * Scope of the current function
 *
 * @param variables parameters and local variables
 * @param parent    parent of the current scope
 */
public record FunctionCallScope(Map<String, Object> variables, FunctionCallScope parent) {
    public Object getVarValue(final String id) {
        if (variables.containsKey(id)) {
            return variables.get(id);
        }

        if (parent == null) {
            throw new IllegalStateException("Variable not found: " + id);
        }

        return parent.getVarValue(id);
    }

    public boolean hasVariable(final String id) {
        if (variables.containsKey(id)) {
            return true;
        }

        return parent != null && parent.hasVariable(id);
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
}
