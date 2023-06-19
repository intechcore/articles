package org.jimple.interpreter;

import org.junit.jupiter.api.Test;

class JimpleInterpreterTest {
    private final JimpleInterpreter interpreter = new JimpleInterpreter();

    @Test
    void testPrintlnNumber() {
        interpreter.eval("println 123");
    }

    @Test
    void testPrintlnPlusExpression() {
        interpreter.eval("println 12+34");
    }

    @Test
    void testPrintlnParenthesisExpression() {
        interpreter.eval("println (1+2)*3");
    }

    @Test
    void testPrintlnId() {
        interpreter.eval("var x = 1235 println x");
    }

    @Test
    void testFunctionCall() {
        interpreter.eval("fun sum(a, b) { return a + b } println sum(12, 34)");
    }

    @Test
    void testIfStatement() {
        interpreter.eval("var b = 1 if (b > 100) println \"high\" else println \"low\"");
    }
}
