package org.jimple.compiler;

import org.jimple.lang.JimpleParser;

/**
 * @author Ruslan Absaliamov
 */
public record CompilationInfo(int type) {
    public static final CompilationInfo VOID = new CompilationInfo(JimpleParser.VOID);

    public static final CompilationInfo BOOLEAN = new CompilationInfo(JimpleParser.BOOLEAN);

    public static final CompilationInfo STRING = new CompilationInfo(JimpleParser.STRING_LITERAL);

    public static final CompilationInfo NUMBER = new CompilationInfo(JimpleParser.NUMBER);

    public static final CompilationInfo DOUBLE = new CompilationInfo(JimpleParser.DOUBLE_NUMBER);


    public boolean isNumber() {
        return type == JimpleParser.NUMBER;
    }

    public boolean isDouble() {
        return type == JimpleParser.DOUBLE_NUMBER;
    }

    public boolean isString() {
        return type == JimpleParser.STRING_LITERAL;
    }

    public boolean isVoid() {
        return type == JimpleParser.VOID;
    }

    public boolean isBoolean() {
        return type == JimpleParser.BOOLEAN;
    }

    public String getTypeName() {
        return switch (type) {
            case JimpleParser.STRING_LITERAL -> "STRING";
            case JimpleParser.NUMBER -> "NUMBER";
            case JimpleParser.DOUBLE_NUMBER -> "DOUBLE";
            case JimpleParser.BOOLEAN -> "BOOLEAN";
            case JimpleParser.VOID -> "VOID";
            default -> throw new UnsupportedOperationException("Unsupported type: " + type);
        };
    }

    @Override
    public String toString() {
        final String typeName = getTypeName();

        return "CompilationInfo{" +
                "type=" + typeName +
                '}';
    }
}
