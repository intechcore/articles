package org.jimple.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.jimple.compiler.samples.FooBar;
import org.jimple.error.CodeCompilationException;
import org.jimple.error.CodeValidateException;
import org.jimple.util.IssueUtil;
import org.jimple.util.TestCase;
import org.jimple.util.TestCaseParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Textifier;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Ruslan Absaliamov
 */
@Slf4j
class JimpleCompilerTest {
    private final TestCaseParser parser = new TestCaseParser();

    @ParameterizedTest
    @ValueSource(strings = {"println", "number", "string", "scopes", "funcs", "while", "if"})
    void testCompileCases(final String fileName) {
        final String resourcePath = "/cases/" + fileName + ".jmpl.case";
        compileResource(resourcePath);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "helloVariable"})
    void testCompileSamples(final String fileName) {
        final String resourcePath = "/samples/" + fileName + ".jimple";
        compileResource(resourcePath);
    }

    @Test
    void showByteCode() throws IOException {
        Textifier.main(new String[]{FooBar.class.getName()});
    }

    @Test
    void showAsmCode() throws IOException {
        ASMifier.main(new String[]{FooBar.class.getName()});
    }

    @SneakyThrows
    private void compileResource(final String resourcePath) {
        final URL resourceUrl = Objects.requireNonNull(getClass().getResource(resourcePath), () -> "Resource not found: " + resourcePath);
        final List<TestCase> cases = parser.parse(resourceUrl);
        final Path dummyPath = Paths.get(resourceUrl.toURI());

        for (final TestCase testCase : cases) {
            final StringWriter logWriter = new StringWriter();
            log.info("-----------------------------------------------");
            final JimpleCompiler compiler = new JimpleCompiler(testCase.jimpleCode(), dummyPath);
            try {
                final JimpleCompilerResult result = compiler.compile(new PrintWriter(logWriter, true));
                final String warningsOutput = compiler.getWarnings().isEmpty() ? "" : IssueUtil.issuesToString(compiler.getWarnings());
                final Class<?> clazz = result.loadClass();
                final String actualStdout = runClass(clazz);
                result.saveToFile("JimpleAutoGenApp.class");
                assertEquals(testCase.expectedOutput(), warningsOutput + actualStdout);
            } catch (final CodeValidateException ex) {
                assertEquals(testCase.expectedOutput(), IssueUtil.issuesToString(ex.getIssues()));
            } catch (final CodeCompilationException | ArithmeticException ex) {
                final String warningsOutput = compiler.getWarnings().isEmpty() ? "" : IssueUtil.issuesToString(compiler.getWarnings());
                assertEquals(testCase.expectedOutput(), warningsOutput + "!ERROR: " + ex.getMessage() + System.lineSeparator());
            } catch (final CodeExecRuntimeException ex) {
                final String warningsOutput = compiler.getWarnings().isEmpty() ? "" : IssueUtil.issuesToString(compiler.getWarnings());
                assertEquals(testCase.expectedOutput(), ex.getStdOutput() + warningsOutput + "!ERROR: " + ex.getMessage() + System.lineSeparator());
            } catch (final Throwable ex) {
                throw ex;
            } finally {
                log.info("Compiled trace:\n{}", logWriter);
                log.info("============================================");
                log.info("Source code:\n{}", testCase.jimpleCode());
            }
        }
    }

    @SneakyThrows
    private static String runClass(final Class<?> clazz) {
        return wrapStdout(() -> {
            final Method mainMethod = clazz.getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[0]);
            return null;
        });
    }

    private static String wrapStdout(final Callable action) throws Throwable {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final PrintStream oldStdout = System.out;
        System.setOut(new PrintStream(stdout));
        try {
            action.call();
            return stdout.toString(StandardCharsets.UTF_8);
        } catch (final InvocationTargetException ex) {
            throw new CodeExecRuntimeException(ex.getTargetException(), stdout.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(oldStdout);
        }
    }
}