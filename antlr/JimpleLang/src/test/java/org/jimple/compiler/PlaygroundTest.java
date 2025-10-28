package org.jimple.compiler;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.objectweb.asm.Opcodes.*;

class PlaygroundTest {
    private static final String CLASS_NAME = "Playground";

    @Test
    void testGenMethod() throws IOException {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final StringWriter strWriter = new StringWriter();
        final PrintWriter codeGenTrace = new PrintWriter(strWriter);
        final TraceClassVisitor tcv = new TraceClassVisitor(cw, codeGenTrace);
        final CheckClassAdapter cv = new CheckClassAdapter(tcv);
        cv.visit(Opcodes.V1_8, ACC_PUBLIC, CLASS_NAME, null, "java/lang/Object", null);

        // define field
        cv.visitField(Opcodes.ACC_PROTECTED, "f", "I", null, null);

        // Define a constructor
        final MethodVisitor constructor = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        // Create the main method
        final MethodVisitor mainMethod = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mainMethod.visitCode();
        mainMethod.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mainMethod.visitLdcInsn("Hello World");
        mainMethod.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mainMethod.visitInsn(RETURN);
        mainMethod.visitMaxs(1, 1);
        mainMethod.visitEnd();

        // create getter
        final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getF", "()I", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, "f", "I");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cv.visitEnd();
        final byte[] bytecode = cw.toByteArray();
        final JimpleCompilerResult result = new JimpleCompilerResult(bytecode);
        result.saveToFile(CLASS_NAME + ".class");

        System.out.println("----------------------------------------");
        System.out.println(strWriter);
    }

    @Test
    void testCheckClassAdapter() throws IOException {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final CheckClassAdapter cv = new CheckClassAdapter(cw);
        cv.visit(Opcodes.V1_8, ACC_PUBLIC, CLASS_NAME, null, "java/lang/Object", null);

        // Define a constructor
        final MethodVisitor constructor = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitEnd();

        // Create the main method
        final MethodVisitor method = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        method.visitCode();
        method.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        method.visitLdcInsn(12L);
        method.visitLdcInsn("33");
        method.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "max", "(JJ)J", false);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
        method.visitInsn(RETURN);

        final IllegalArgumentException ex = assertThrowsExactly(IllegalArgumentException.class, method::visitEnd);
        assertEquals("Error at instruction 3: Argument 2: expected J, but found R main([Ljava/lang/String;)V", ex.getMessage().substring(0, ex.getMessage().indexOf('\n')).trim());
    }

    @Test
    void generateHelloWorld() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        cw.visit(V17, ACC_PUBLIC, "HelloWorld", null, "java/lang/Object", null);

        // Generate default constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // Generate public static void main(String[] args)
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        // System.out
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // "Hello, World!" string
        mv.visitLdcInsn("Hello, World!");

        // println call
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();

        // Write the generated class to a file
        try (FileOutputStream fos = new FileOutputStream("HelloWorld.class")) {
            fos.write(cw.toByteArray());
        }
    }
}
