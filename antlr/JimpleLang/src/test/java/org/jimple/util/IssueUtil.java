package org.jimple.util;

import java.util.Collection;
import java.util.stream.Collectors;

import org.jimple.diagnotics.Issue;

public final class IssueUtil {
    public static String issuesToString(final Collection<Issue> issues) {
        return issues.stream()
                .map(issue -> String.format("%s: %s%s%s%s", issue.type(), issue.message(), System.lineSeparator(), issue.details(), System.lineSeparator()))
                .collect(Collectors.joining());
    }
}
