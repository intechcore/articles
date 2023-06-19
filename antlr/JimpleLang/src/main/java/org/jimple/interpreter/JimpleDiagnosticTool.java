package org.jimple.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.jimple.lang.JimpleBaseVisitor;

/**
 * Class for finding semantic errors in Jimple code
 */
public class JimpleDiagnosticTool extends JimpleBaseVisitor<Object> {
    private final List<ErrorInfo> errors = new ArrayList<>(0);
}

record ErrorInfo(String error) {
}
