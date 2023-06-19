package org.jimple.interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        try {
            new JimpleInterpreter().eval(path);
        } catch (final Exception ex) {
            System.err.println("Code execute failed: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
