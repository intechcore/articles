package org.jimple.diagnotics;

/**
 * Information about error or warning in Jimple code.
 */
public record Issue(IssueType type, String message, int lineNumber, int lineOffset, String details) {
    public boolean isError() {
        return type == IssueType.ERROR;
    }
}
