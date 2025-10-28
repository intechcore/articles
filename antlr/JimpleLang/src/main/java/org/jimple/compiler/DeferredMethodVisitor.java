package org.jimple.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jimple.interpreter.FunctionSignature;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import lombok.Getter;

import static java.util.stream.Collectors.joining;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * Record all method visits and then apply them all.
 */
public class DeferredMethodVisitor extends MethodVisitor {
    private final List<Consumer<MethodVisitor>> allCalls = new ArrayList<>(1);
    private final Map<String, String> methodDescriptors = new HashMap<>(1);
    private final List<String> logs = new ArrayList<>(1);
    @Getter
    private final FunctionSignature funSignature;
    @Getter
    private final List<CompilationInfo> arguments;
    @Getter
    private CompilationInfo returnType;

    protected DeferredMethodVisitor(final FunctionSignature funSignature, final List<CompilationInfo> arguments) {
        super(Opcodes.ASM8);
        this.funSignature = funSignature;
        this.arguments = arguments;
    }

    public void apply(final MethodVisitor mv) {
        try {
            allCalls.forEach(allCall -> allCall.accept(mv));
        } catch (final Exception ex) {
            System.err.println("Generating method failed: " + this);
            throw ex;
        }
    }

    public void patchDescriptor(final String methodName, final String oldDescriptor, final String newDescriptor) {
        methodDescriptors.computeIfPresent(methodName + oldDescriptor, (unused1, unused2) -> newDescriptor);
    }

    @Override
    public void visitCode() {
        allCalls.add(MethodVisitor::visitCode);
        logs.add("visitCode()");
    }

    @Override
    public void visitEnd() {
        allCalls.add(MethodVisitor::visitEnd);
        logs.add("visitEnd()");
    }

    @Override
    public void visitParameter(String name, int access) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        throw new IllegalStateException("TODO");
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitInsn(int opcode) {
        allCalls.add(mv -> mv.visitInsn(opcode));
        logs.add(String.format("visitInsn(%s)", getOpcodeName(opcode)));
    }

    private static String getOpcodeName(int opcode) {
        switch (opcode) {
            case INVOKEVIRTUAL:
                return "INVOKEVIRTUAL";
            case RETURN:
                return "RETURN";
            case LRETURN:
                return "LRETURN";
            case LLOAD:
                return "LLOAD";
            case IADD:
                return "IADD";
            case LADD:
                return "LADD";
            case LSUB:
                return "LSUB";
            case LDIV:
                return "LDIV";
            case LMUL:
                return "LMUL";
            case ALOAD:
                return "ALOAD";
            case IFGT:
                return "IFGT";
            case LCMP:
                return "LCMP";
            case ICONST_0:
                return "ICONST_0";
            case ICONST_1:
                return "ICONST_1";
            case GOTO:
                return "GOTO";
            case IRETURN:
                return "IRETURN";
            case GETSTATIC:
                return "GETSTATIC";
            case INVOKESTATIC:
                return "INVOKESTATIC";
            case INVOKESPECIAL:
                return "INVOKESPECIAL";
            case ARETURN:
                return "ARETURN";
            case IFEQ:
                return "IFEQ";
            case NEW:
                return "NEW";
            case DUP:
                return "DUP";
            default:
                throw new IllegalArgumentException("Implement opcode: " + opcode);
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        allCalls.add(mv -> mv.visitVarInsn(opcode, varIndex));
        logs.add(String.format("visitVarInsn(%s, %d)", getOpcodeName(opcode), varIndex));
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        allCalls.add(mv -> mv.visitTypeInsn(opcode, type));
        logs.add(String.format("visitTypeInsn(%s, %s)", getOpcodeName(opcode), type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        allCalls.add(mv -> mv.visitFieldInsn(opcode, owner, name, descriptor));
        logs.add(String.format("visitFieldInsn(%s, %s, %s, %s)", getOpcodeName(opcode), owner, name, descriptor));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        methodDescriptors.put(name + descriptor, descriptor);
        allCalls.add(mv -> {
            final String actualDescriptor = methodDescriptors.get(name + descriptor);
            mv.visitMethodInsn(opcode, owner, name, actualDescriptor, isInterface);
        });
        logs.add(String.format("visitMethodInsn(%s, %s, %s, %s*, %s)", getOpcodeName(opcode), owner, name, descriptor, isInterface));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        allCalls.add(mv -> mv.visitJumpInsn(opcode, label));
        logs.add(String.format("visitJumpInsn(%s, %s)", getOpcodeName(opcode), label));
    }

    @Override
    public void visitLabel(Label label) {
        allCalls.add(mv -> mv.visitLabel(label));
        logs.add(String.format("visitLabel(%s)", label));
    }

    @Override
    public void visitLdcInsn(Object value) {
        allCalls.add(mv -> mv.visitLdcInsn(value));
        if (value instanceof String) {
            logs.add(String.format("visitLdcInsn(\"%s\")", value));
        } else {
            logs.add(String.format("visitLdcInsn(%s)", value));
        }
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        allCalls.add(mv -> mv.visitMaxs(maxStack, maxLocals));
        logs.add(String.format("visitMaxs(%d, %d)", maxStack, maxLocals));
    }

    public String getName() {
        return String.format("%s(%s)", funSignature.name(), arguments.stream().map(CompilationInfo::getTypeName).collect(joining(", ")));
    }

    @Override
    public String toString() {
        if (allCalls.size() != logs.size()) {
            throw new IllegalStateException("Illegal state of logs");
        }

        return getName() + '\n' + String.join("\n", logs);
    }

    public void setReturnType(final CompilationInfo returnType) {
        if (this.returnType == null) {
            this.returnType = returnType;
        } else if (!this.returnType.equals(returnType)) {
            throw new IllegalStateException(String.format("Illegal return type: %s != %s in function %s", returnType, this.returnType, getName()));
        }
    }
}
