package org.jimple.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jimple.diagnotics.Issue;
import org.jimple.error.CodeValidateException;
import org.jimple.util.IssueUtil;
import org.jimple.util.TestCase;
import org.jimple.util.TestCaseParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JimpleInterpreterCaseTest {
    private final TestCaseParser parser = new TestCaseParser();

    @ParameterizedTest
    @ValueSource(strings = {"println", "number", "string", "scopes", "funcs"})
    void testAllCases(final String name) throws IOException {
        final String resourcePath = "/cases/" + name + ".jmpl.case";
        runResource(resourcePath);
    }

    @ParameterizedTest
    @ValueSource(strings = {"issue1"})
    void testIssueCases(final String name) throws IOException {
        final String resourcePath = "/issues/" + name + ".jimple";
        runResource(resourcePath);
    }

    private void runResource(final String resourcePath) throws IOException {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final JimpleInterpreter interpreter = new JimpleInterpreter(stdout);
        final URL resourceUrl = Objects.requireNonNull(getClass().getResource(resourcePath), () -> "Resource not found: " + resourcePath);
        final List<TestCase> cases = parser.parse(resourceUrl);

        cases.forEach(testCase -> {
            final List<Issue> warnings = new ArrayList<>(0);
            try {
                final Object result = interpreter.eval(testCase.jimpleCode(), warnings);
                final String warningsOutput = warnings.isEmpty() ? "" : IssueUtil.issuesToString(warnings);
                final String actualStdout = stdout.toString(StandardCharsets.UTF_8);

                assertEquals(JimpleInterpreter.VOID, result, "script should return nothing");
                assertEquals(testCase.expectedOutput(), warningsOutput + actualStdout);
            } catch (CodeValidateException ex) {
                assertEquals(testCase.expectedOutput(), IssueUtil.issuesToString(ex.getIssues()));
            } catch (Throwable ex) {
                final String actualStdout = stdout.toString(StandardCharsets.UTF_8);
                final String warningsOutput = warnings.isEmpty() ? "" : IssueUtil.issuesToString(warnings);
                final String errorOutput = actualStdout + warningsOutput + "!ERROR: "
                        + (ex instanceof StackOverflowError ? "StackOverflow" : ex.getMessage());
                log.error("Script eval failed: {}", testCase.jimpleCode(), ex);
                assertEquals(testCase.expectedOutput().trim(), errorOutput);
            }
            stdout.reset();
        });
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
}
