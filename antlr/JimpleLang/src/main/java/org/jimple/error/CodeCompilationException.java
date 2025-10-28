package org.jimple.error;

import java.io.Serial;

/**
 * Exception when compilation error occurs.
 */
public class CodeCompilationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5817056785188508294L;

    public CodeCompilationException() {
    }

    public CodeCompilationException(final String message) {
        super(message);
    }
}
