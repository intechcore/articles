package org.jimple.compiler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import lombok.Getter;

/**
 * Result of a compilation process. Actually hold bytecode as a byte array.
 */
public class JimpleCompilerResult {
    @Getter
    private final byte[] bytecode;

    public JimpleCompilerResult(final byte[] bytecode) {
        this.bytecode = bytecode;
    }

    public void saveToFile(final String filename) throws IOException {
        FileUtils.writeByteArrayToFile(new File(filename), bytecode);
    }

    public Class<?> loadClass() throws ClassNotFoundException {
        return new ByteArrayClassLoader(bytecode).loadClass(JimpleCompilerVisitor.JIMPLE_MAIN_CLASS_NAME);
    }

    private static class ByteArrayClassLoader extends ClassLoader {
        private final byte[] bytecode;

        public ByteArrayClassLoader(final byte[] bytecode) {
            this.bytecode = bytecode;
        }

        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }

}
