package org.jimple.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

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
    @ValueSource(strings = {"println", "number", "string", "scopes"})
    void testAllCases(final String name) throws IOException {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final JimpleInterpreter interpreter = new JimpleInterpreter(stdout);
        final String resourcePath = "/cases/" + name + ".jmpl.case";
        final URL resourceUrl = Objects.requireNonNull(getClass().getResource(resourcePath), () -> "Resource not found: " + resourcePath);
        final List<TestCase> cases = parser.parse(resourceUrl);

        cases.forEach(testCase -> {
            try {
                final Object result = interpreter.eval(testCase.jimpleCode());
                final String actualStdout = stdout.toString(StandardCharsets.UTF_8);

                assertEquals(JimpleInterpreter.VOID, result, "script should return nothing");
                assertEquals(testCase.expectedOutput(), actualStdout);
            } catch (Exception ex) {
                final String errorOutput = "!ERROR: " + ex.getMessage();
                log.error("Script eval failed: {}" + testCase.jimpleCode(), ex);
                assertEquals(testCase.expectedOutput().trim(), errorOutput);
            }
            stdout.reset();
        });
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
}
