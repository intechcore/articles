package org.jimple.interpreter;

import org.jimple.error.CodeValidateException;
import org.jimple.util.IssueUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JimpleInterpreterTest {
    private final JimpleInterpreter interpreter = new JimpleInterpreter();

    @Test
    void testPrintlnNumber(){
        eval("println 123");
    }

    @Test
    void testPrintlnPlusExpression(){
        eval("println 12+34");
    }

    @Test
    void testPrintlnParenthesisExpression(){
        eval("println (1+2)*3");
    }

    @Test
    void testPrintlnId(){
        eval("var x = 1235 println x");
    }

    @Test
    void testFunctionCall(){
        eval("fun sum(a, b) { return a + b } println sum(12, 34)");
    }

    @Test
    void testIfStatement(){
        eval("var b = 1 if (b > 100) println \"high\" else println \"low\"");
    }

    private Object eval(final String expression) {
        try {
            return interpreter.eval(expression);
        } catch (CodeValidateException ex) {
            System.err.println(IssueUtil.issuesToString(ex.getIssues()));
            throw new RuntimeException(ex);
        }
    }
}
