package org.jimple.compiler;

import lombok.Getter;

/**
 * Exception throwing during code running.
 */
@Getter
public class CodeExecRuntimeException extends RuntimeException {

    private final String stdOutput;

    public CodeExecRuntimeException(final Throwable cause, final String stdOutput) {
        super((cause instanceof StackOverflowError ? "StackOverflowError" : cause.getMessage()), cause);
        this.stdOutput = stdOutput;
    }
}
