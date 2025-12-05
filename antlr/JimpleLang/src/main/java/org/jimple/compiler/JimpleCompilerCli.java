package org.jimple.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.FilenameUtils;
import org.jimple.diagnotics.Issue;
import org.jimple.error.CodeValidateException;
import org.jimple.util.IssueUtil;

/**
 * Creates executable jar-file from {@link JimpleCompilerResult}.
 *
 * @author Ruslan Absaliamov
 */
public class JimpleCompilerCli {
    private static final int EXIT_INVALID_ARGS = 1;
    private static final int EXIT_FILE_NOT_FOUND = 2;
    private static final int EXIT_COMPILATION_ERROR = 5;

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.out.println("Jimple Compiler v1.2");
            System.out.println("Usage: jimplec <path>");
            System.exit(EXIT_INVALID_ARGS);
        }

        try {
            final Path filePath = Path.of(args[0]).toAbsolutePath();
            if (Files.notExists(filePath)) {
                System.out.println("File not found: " + filePath);
                System.exit(EXIT_FILE_NOT_FOUND);
            }

            final String jimpleCode = Files.readString(filePath);
            System.out.println("Compiling " + filePath + "...");
            final JimpleCompiler compiler = new JimpleCompiler(jimpleCode, filePath);
            final JimpleCompilerResult result = compiler.compile();
            final List<Issue> warnings = compiler.getWarnings();
            if (!warnings.isEmpty()) {
                // print warnings
                System.out.println(IssueUtil.issuesToString(warnings));
            }
            // make an executable jar-file
            final Path jarFile = createExecutableJarFile(result, filePath);
            System.out.println("Compilation completed successfully to " + jarFile);
        } catch (final CodeValidateException ex) {
            System.err.println(IssueUtil.issuesToString(ex.getIssues()));
            System.exit(EXIT_COMPILATION_ERROR);
        } catch (final Exception ex) {
            ex.printStackTrace();
            System.err.println("Jimple compiler error: " + ex.getMessage());
            System.exit(EXIT_COMPILATION_ERROR);
        }
    }

    private static Path createExecutableJarFile(final JimpleCompilerResult result, final Path filePath) throws IOException {
        // === Output paths ===
        final Path srcDir = filePath.getParent();
        final Path targetDir = srcDir.resolve("target");
        Files.createDirectories(targetDir);

        final String className = JimpleCompilerVisitor.JIMPLE_MAIN_CLASS_NAME;
        final Path classFilePath = targetDir.resolve(className + ".class");
        final String fileName = FilenameUtils.removeExtension(filePath.getFileName().toString());
        final Path jarFilePath = targetDir.resolve(fileName + ".jar");

        // === Write class file ===
        Files.write(classFilePath, result.getBytecode());

        // === Create manifest ===
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, className);

        // === Create the JAR file ===
        try (final JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFilePath), manifest)) {
            final JarEntry entry = new JarEntry(className + ".class");
            jos.putNextEntry(entry);
            jos.write(result.getBytecode());
            jos.closeEntry();
        }

        return jarFilePath;
    }
}
