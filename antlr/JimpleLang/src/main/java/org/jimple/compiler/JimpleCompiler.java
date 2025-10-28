package org.jimple.compiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jimple.diagnotics.Issue;
import org.jimple.diagnotics.JimpleDiagnosticTool;
import org.jimple.error.CodeValidateException;
import org.jimple.lang.JimpleLexer;
import org.jimple.lang.JimpleParser;

import lombok.Getter;

/**
 * Compiles Jimple-code to executable jar-file.
 *
 * @author Ruslan Absaliamov
 */
@Getter
public class JimpleCompiler {
    private final Path codeFile;
    private final List<Issue> warnings = new ArrayList<>(0);
    private final String input;

    public JimpleCompiler(final Path codeFile) throws IOException {
        this(Files.readString(codeFile), codeFile);
    }

    public JimpleCompiler(final String input, final Path codeFile) {
        this.input = input;
        this.codeFile = codeFile.toAbsolutePath();
    }

    /**
     * Validates and compiles Jimple code.
     */
    public JimpleCompilerResult compile(final PrintWriter codeGenTrace) throws CodeValidateException {
        final JimpleLexer lexer = new JimpleLexer(CharStreams.fromString(input));
        final JimpleParser parser = new JimpleParser(new CommonTokenStream(lexer));

        final List<Issue> issues = JimpleDiagnosticTool.validate(input, null);

        if (issues.stream().anyMatch(Issue::isError)) {
            throw new CodeValidateException(issues);
        }

        if (!issues.isEmpty()) {
            warnings.addAll(issues);
        }

        final JimpleCompilerVisitor compilerVisitor = new JimpleCompilerVisitor(codeGenTrace);
        compilerVisitor.visitProgram(parser.program());
        final byte[] bytecode = compilerVisitor.getBytecode();
        return new JimpleCompilerResult(bytecode);
    }

    public JimpleCompilerResult compile() throws CodeValidateException {
        return compile(null);
    }
}
