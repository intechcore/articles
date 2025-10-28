package org.jimple.util;

/**
 * @author Ruslan Absaliamov
 */
public final class StringUtil {
    private StringUtil() {
    }

    public static String cleanStringLiteral(final String literal) {
        return literal.length() > 1 ? literal.substring(1, literal.length() - 1) : literal;
    }
}
