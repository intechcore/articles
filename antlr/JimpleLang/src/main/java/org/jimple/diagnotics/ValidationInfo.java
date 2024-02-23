package org.jimple.diagnotics;

public record ValidationInfo(ValidationType type, Object value) {

    public boolean isPrintable() {
        return type == ValidationType.STRING || type == ValidationType.DOUBLE || type == ValidationType.NUMBER || type == ValidationType.BOOL || type == ValidationType.ANY;
    }

    public boolean isSkip() {
        return type == ValidationType.SKIP;
    }

    /**
     * Returns true if value of {@link Number}.
     */
    public boolean isNumeric() {
        return type == ValidationType.NUMBER || type == ValidationType.DOUBLE;
    }

    public double numericAsDouble() {
        if (!isNumeric()) {
            throw new IllegalStateException("Expected numeric value");
        }

        if (!hasValue()) {
            throw new IllegalStateException("Expected certain value");
        }

        if (value instanceof Double dbl) {
            return dbl;
        } else if (value instanceof Long lng) {
            return lng.doubleValue();
        } else {
            throw new IllegalStateException("Unsupported value type: " + value);
        }
    }

    public String asString() {
        if (!hasValue()) {
            throw new IllegalStateException("Expected String value");
        }

        return value.toString();
    }

    public boolean asBoolean() {
        return (Boolean) value;
    }

    public boolean isString() {
        return type == ValidationType.STRING;
    }

    public boolean isBool() {
        return type == ValidationType.BOOL;
    }

    public boolean isAny() {
        return type == ValidationType.ANY;
    }

    public boolean hasValue() {
        return value != null;
    }

    public static final ValidationInfo VOID = new ValidationInfo(ValidationType.VOID, null);

    public static final ValidationInfo SKIP = new ValidationInfo(ValidationType.SKIP, null);

    public static final ValidationInfo FUNC_DEFINITION = new ValidationInfo(ValidationType.FUNCTION_DEFINITION, null);

    public static ValidationInfo string(String value) {
        return new ValidationInfo(ValidationType.STRING, value);
    }

    public static ValidationInfo number(Long value) {
        return new ValidationInfo(ValidationType.NUMBER, value);
    }

    public static ValidationInfo doubleVal(Double value) {
        return new ValidationInfo(ValidationType.DOUBLE, value);
    }

    public static ValidationInfo bool(Boolean value) {
        return new ValidationInfo(ValidationType.BOOL, value);
    }

    /**
     * Creates {@link ValidationInfo} of type {@link ValidationType#ANY}.
     *
     * @param name name of the variable
     */
    public static ValidationInfo any(String name) {
        return new ValidationInfo(ValidationType.ANY, name);
    }

    public static ValidationInfo tryStringConcat(ValidationInfo value1, ValidationInfo value2) {
        final Object str1 = value1.value;
        final Object str2 = value2.value;

        return str1 != null && str2 != null ? string(str1.toString() + str2) : string(null);
    }
}
