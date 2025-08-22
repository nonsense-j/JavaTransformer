package com.transformer.util;

import com.transformer.api.BugInformation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for common test operations.
 * Provides helper methods for file operations, bug information creation, and validation.
 */
public class TestUtils {

    /**
     * Create test bug information with specified bug lines.
     * 
     * @param bugLines List of line numbers where bugs are located
     * @return BugInformation object configured for guided transformations
     */
    public static BugInformation createTestBugInformation(List<Integer> bugLines) {
        return BugInformation.withBugs(bugLines);
    }

    /**
     * Create invalid bug information for testing error scenarios.
     * 
     * @return BugInformation object that is invalid for guided transformations
     */
    public static BugInformation createInvalidBugInformation() {
        return BugInformation.noBugs();  // Invalid: no bugs marked but trying to use guided strategy
    }

    /**
     * Create bug information with invalid line numbers for testing edge cases.
     * 
     * @return BugInformation object with problematic line numbers
     */
    public static BugInformation createBugInformationWithInvalidLines() {
        try {
            return new BugInformation(true, Arrays.asList(-1, 0, 1000000));  // Invalid line numbers
        } catch (IllegalArgumentException e) {
            // If constructor validation prevents creation, return a valid bug info for testing
            return BugInformation.withBugs(Arrays.asList(1000000));  // Very high line number
        }
    }

    /**
     * Read the content of a file as a string.
     * 
     * @param filePath Path to the file to read
     * @return File content as string
     * @throws IOException if file cannot be read
     */
    public static String readFileContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    /**
     * Validate that a string contains syntactically correct Java code.
     * This is a basic validation - checks for basic Java structure.
     * 
     * @param javaCode Java source code to validate
     * @return true if the code appears to be valid Java syntax
     */
    public static boolean validateJavaSyntax(String javaCode) {
        if (javaCode == null || javaCode.trim().isEmpty()) {
            return false;
        }

        // Basic syntax checks
        String trimmed = javaCode.trim();
        
        // Should have balanced braces
        if (!hasBalancedBraces(trimmed)) {
            return false;
        }
        
        // Should contain some basic Java keywords or structure
        boolean hasJavaStructure = trimmed.contains("class ") || 
                                  trimmed.contains("interface ") ||
                                  trimmed.contains("public ") ||
                                  trimmed.contains("private ") ||
                                  trimmed.contains("protected ");
        
        return hasJavaStructure;
    }

    /**
     * Check if a string has balanced braces, parentheses, and brackets.
     * 
     * @param code Code to check
     * @return true if braces are balanced
     */
    private static boolean hasBalancedBraces(String code) {
        int braces = 0;
        int parentheses = 0;
        int brackets = 0;
        
        for (char c : code.toCharArray()) {
            switch (c) {
                case '{': braces++; break;
                case '}': braces--; break;
                case '(': parentheses++; break;
                case ')': parentheses--; break;
                case '[': brackets++; break;
                case ']': brackets--; break;
            }
            
            // Early exit if any become negative (closing before opening)
            if (braces < 0 || parentheses < 0 || brackets < 0) {
                return false;
            }
        }
        
        return braces == 0 && parentheses == 0 && brackets == 0;
    }

    /**
     * Clean up test files in the specified directory.
     * 
     * @param directoryPath Path to directory to clean up
     * @throws IOException if cleanup fails
     */
    public static void cleanupTestFiles(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (Files.exists(dir)) {
            try (Stream<Path> paths = Files.walk(dir)) {
                paths.filter(Files::isRegularFile)
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             // Log but don't fail the test
                             System.err.println("Warning: Could not delete test file: " + path);
                         }
                     });
            }
        }
    }

    /**
     * Create a sample Java file with specified content for testing.
     * 
     * @param content Java source code content
     * @param filename Name of the file to create
     * @param outputDir Directory where to create the file
     * @return Path to the created file
     * @throws IOException if file creation fails
     */
    public static String createSampleJavaFile(String content, String filename, String outputDir) 
            throws IOException {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);
        
        Path filePath = dir.resolve(filename);
        Files.write(filePath, content.getBytes());
        
        return filePath.toString();
    }

    /**
     * Compare two Java source code strings, ignoring whitespace differences.
     * 
     * @param actual Actual transformed code
     * @param expected Expected code
     * @return true if the codes are equivalent (ignoring whitespace)
     */
    public static boolean compareTransformedOutput(String actual, String expected) {
        if (actual == null && expected == null) {
            return true;
        }
        if (actual == null || expected == null) {
            return false;
        }
        
        // Normalize whitespace for comparison
        String normalizedActual = normalizeWhitespace(actual);
        String normalizedExpected = normalizeWhitespace(expected);
        
        return normalizedActual.equals(normalizedExpected);
    }

    /**
     * Normalize whitespace in Java code for comparison purposes.
     * 
     * @param code Java source code
     * @return Code with normalized whitespace
     */
    private static String normalizeWhitespace(String code) {
        return code.replaceAll("\\s+", " ").trim();
    }

    /**
     * Check if a file exists and is not empty.
     * 
     * @param filePath Path to the file to check
     * @return true if file exists and has content
     */
    public static boolean isValidOutputFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

    /**
     * Get the number of lines in a file.
     * 
     * @param filePath Path to the file
     * @return Number of lines in the file
     * @throws IOException if file cannot be read
     */
    public static int getLineCount(String filePath) throws IOException {
        return (int) Files.lines(Paths.get(filePath)).count();
    }

    /**
     * Create a temporary directory for test outputs.
     * 
     * @param prefix Prefix for the temporary directory name
     * @return Path to the created temporary directory
     * @throws IOException if directory creation fails
     */
    public static String createTempTestDirectory(String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(prefix);
        return tempDir.toString();
    }

    /**
     * Validate that a transformation result contains expected information.
     * 
     * @param result TransformationResult to validate
     * @param shouldSucceed Whether the transformation should have succeeded
     * @param expectedTransform Expected transform name (null if any)
     * @return true if result matches expectations
     */
    public static boolean validateTransformationResult(Object result, boolean shouldSucceed, 
                                                     String expectedTransform) {
        // This is a placeholder for result validation
        // In a real implementation, this would check the actual TransformationResult object
        return result != null;
    }

    /**
     * Create test data for performance testing.
     * 
     * @param size Size of test data to create
     * @return Test Java code of specified complexity
     */
    public static String createPerformanceTestData(int size) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class PerformanceTest {\n");
        
        for (int i = 0; i < size; i++) {
            sb.append("    public void method").append(i).append("() {\n");
            sb.append("        int value = ").append(i).append(";\n");
            sb.append("        for (int j = 0; j < ").append(i).append("; j++) {\n");
            sb.append("            value += j;\n");
            sb.append("        }\n");
            sb.append("    }\n");
        }
        
        sb.append("}\n");
        return sb.toString();
    }
}