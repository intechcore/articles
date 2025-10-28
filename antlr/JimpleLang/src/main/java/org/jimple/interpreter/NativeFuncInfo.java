package org.jimple.interpreter;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;
import org.jimple.compiler.CompilationInfo;
import org.jimple.diagnotics.ValidationInfo;
import org.jimple.lang.JimpleParser;

/**
 * Information about native implementation of the function.
 */
public record NativeFuncInfo(String className,
                             String realMethodName,
                             BiFunction<FunctionSignature, List<Object>, Object> interpreterHandler,
                             CompilationInfo returnType,
                             List<CompilationInfo> parameters) {
    public void validateArguments(final List<CompilationInfo> arguments) {
        Validate.isTrue(parameters.size() == arguments.size(), "Arguments and parameters must have same size, but found parameters: %d, arguments: %d", parameters.size(), arguments.size());

        for (int i = 0; i < arguments.size(); i++) {
            if (!parameters.get(i).equals(arguments.get(i))) {
                throw new IllegalArgumentException(String.format("Argument %s does not match parameter %s, index: %d", arguments.get(i).getTypeName(), parameters.get(i).getTypeName(), i));
            }
        }
    }

    public ValidationInfo interpreterReturnType() {
        return switch (returnType.type()) {
            case JimpleParser.STRING_LITERAL -> ValidationInfo.string(null);
            case JimpleParser.NUMBER -> ValidationInfo.number(null);
            case JimpleParser.DOUBLE_NUMBER -> ValidationInfo.doubleVal(null);
            case JimpleParser.BOOLEAN -> ValidationInfo.bool(null);
            case JimpleParser.VOID -> ValidationInfo.VOID;
            default -> throw new UnsupportedOperationException("Unsupported type: " + returnType);
        };
    }
}
