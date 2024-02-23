package org.jimple.diagnotics;

public enum ValidationType {
    /**
     * Expression returns nothing.
     */
    VOID,

    /**
     * Expression is String
     */
    STRING,

    /**
     * Expression is double
     */
    DOUBLE,

    /**
     * Expression is long
     */
    NUMBER,

    /**
     * Expression is boolean
     */
    BOOL,

    /**
     * Expression contains error and analysing in another context no makes sense.
     */
    SKIP,

    /**
     * When object can be any type. Used only in Check function definition mode.
     */
    ANY,

    /**
     * Tree paet is function definition
     */
    FUNCTION_DEFINITION
}
