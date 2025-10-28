package org.jimple.util;

import org.antlr.v4.runtime.Token;
import org.jimple.lang.JimpleParser;

public final class NumberUtil {
    public static final String UNSUPPORTED_OPERATOR = "Unsupported operator: ";

    public static Number evalBinaryOperator(final Number left, final Number right, final Token operator) {
        if (left instanceof Long && right instanceof Long) {
            return evalLongOperator((Long) left, (Long) right, operator);
        } else {
            return evalDoubleOperator(left.doubleValue(), right.doubleValue(), operator);
        }
    }


    private static Long evalLongOperator(final Long left, final Long right, final Token operator) {
        switch (operator.getType()) {
            case JimpleParser.PLUS -> {
                return left + right;
            }
            case JimpleParser.MINUS -> {
                return left - right;
            }
            case JimpleParser.SLASH -> {
                return left / right;
            }
            case JimpleParser.ASTERISK -> {
                return left * right;
            }
            case JimpleParser.MOD -> {
                return left % right;
            }
            default -> throw new IllegalStateException(UNSUPPORTED_OPERATOR + operator);
        }
    }

    private static Double evalDoubleOperator(final Double left, final Double right, final Token operator) {
        switch (operator.getType()) {
            case JimpleParser.PLUS -> {
                return left + right;
            }
            case JimpleParser.MINUS -> {
                return left - right;
            }
            case JimpleParser.SLASH -> {
                return left / right;
            }
            case JimpleParser.ASTERISK -> {
                return left * right;
            }
            default -> throw new IllegalStateException(UNSUPPORTED_OPERATOR + operator);
        }
    }
}
