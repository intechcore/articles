package org.jimple.error;

import java.util.Collection;
import java.util.List;

import org.jimple.diagnotics.Issue;

/**
 * Exception in case code contains errors
 */
public class CodeValidateException extends Exception {
    private final List<Issue> issues;

    public CodeValidateException(final Collection<Issue> issues) {
        super("Code validating failed");
        this.issues = issues.stream().toList();
    }

    public List<Issue> getIssues() {
        return issues;
    }
}
