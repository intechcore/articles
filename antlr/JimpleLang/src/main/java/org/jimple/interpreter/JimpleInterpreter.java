package org.jimple.interpreter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.jimple.diagnotics.Issue;
import org.jimple.diagnotics.JimpleDiagnosticTool;
import org.jimple.error.CodeValidateException;
import org.jimple.lang.JimpleLexer;
import org.jimple.lang.JimpleParser;

public class JimpleInterpreter {
    public static final Object VOID = new VoidObject();

    private final PrintStream stdout;

    public JimpleInterpreter(final OutputStream stdoutStream) {
        this.stdout = new PrintStream(stdoutStream, true, StandardCharsets.UTF_8);
    }

    public JimpleInterpreter(final PrintStream stdout) {
        this.stdout = stdout;
    }

    public JimpleInterpreter() {
        this(System.out);
    }

    /**
     * Execute jimple source code
     *
     * @return last value of expression or return statement
     */
    public Object eval(final String input) throws CodeValidateException {
        return eval(input, null, new ArrayList<>(0));
    }

    /**
     * Execute jimple source code
     *
     * @return last value of expression or return statement
     */
    public Object eval(final String input, final List<Issue> warnings) throws CodeValidateException {
        return eval(input, null, warnings);
    }

    /**
     * Execute jimple source code
     *
     * @return last value of expression or return statement
     */
    public Object eval(final String input, final Path path, final List<Issue> warnings) throws CodeValidateException {
        final JimpleLexer lexer = new JimpleLexer(CharStreams.fromString(input));
        final JimpleParser parser = new JimpleParser(new CommonTokenStream(lexer));

        final List<Issue> issues = JimpleDiagnosticTool.validate(input, path);

        if (issues.stream().anyMatch(Issue::isError)) {
            throw new CodeValidateException(issues);
        }

        if (!issues.isEmpty()) {
            warnings.addAll(issues);
        }

        final JimpleInterpreterVisitor interpreterVisitor = new JimpleInterpreterVisitor(new JimpleContextImpl(stdout));
        return interpreterVisitor.visitProgram(parser.program());
    }

    public Object eval(final Path path, List<Issue> warnings) throws IOException, CodeValidateException {
        return eval(path, StandardCharsets.UTF_8, warnings);
    }

    public Object eval(final Path path, final Charset charset, final List<Issue> warnings) throws IOException, CodeValidateException {
        return eval(IOUtils.toString(path.toUri(), charset), path, warnings);
    }


    private static class VoidObject {
        @Override
        public String toString() {
            return "VOID";
        }
    }
}
