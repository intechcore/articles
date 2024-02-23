package org.jimple.interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jimple.diagnotics.Issue;

/**
 * Entry point of executing jimple interpreter.
 */
public final class MainApp {
    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("usage: jimple input.jimple");
            System.exit(1);
        }

        final Path path = Paths.get(args[0]);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + path);
            System.exit(1);
        }

        final JimpleInterpreter interpreter = new JimpleInterpreter();

        try {
            final List<Issue> issues = interpreter.validate(path);

            if (!issues.isEmpty()) {
                System.err.println("Found issues: ");
                for (int i = 0; i < issues.size(); i++) {
                    final Issue issue = issues.get(i);
                    System.err.println(String.format("%s (%d:%d): %s", issue.type(), issue.lineNumber(), issue.lineOffset(),
                            issue.message()));
                    if (!issue.details().isEmpty()) {
                        System.err.println(issue.details());
                    }
                }
                System.exit(1);
            }
        } catch (final Exception ex) {
            System.err.println("Code validate failed: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        try {
            interpreter.eval(path, new ArrayList<>(0));
        } catch (final Exception ex) {
            System.err.println("Code execute failed: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
