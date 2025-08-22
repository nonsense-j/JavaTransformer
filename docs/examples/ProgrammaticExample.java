package com.transformer.examples;

import com.transformer.api.TransformerService;
import com.transformer.api.TransformerServiceImpl;
import com.transformer.api.TransformationResult;
import com.transformer.api.exception.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Comprehensive programmatic example demonstrating various ways to use
 * the Transformer library API in Java applications.
 */
public class ProgrammaticExample {
    
    private final TransformerService transformer;
    
    public ProgrammaticExample() {
        this.transformer = new TransformerServiceImpl();
    }
    
    public static void main(String[] args) {
        ProgrammaticExample example = new ProgrammaticExample();
        
        try {
            // Create sample input file for demonstration
            example.createSampleInputFile();
            
            System.out.println("=== Transformer Library Programmatic Examples ===\n");
            
            // Example 1: Basic specific transformation
            example.demonstrateSpecificTransformation();
            
            // Example 2: Random transformation with retry logic
            example.demonstrateRandomTransformation();
            
            // Example 3: Guided transformation with bug information
            example.demonstrateGuidedTransformation();
            
            // Example 4: List and explore available transformations
            example.demonstrateTransformationDiscovery();
            
            // Example 5: Error handling patterns
            example.demonstrateErrorHandling();
            
            // Example 6: Batch processing
            example.demonstrateBatchProcessing();
            
            // Example 7: Integration with static analysis
            example.demonstrateStaticAnalysisIntegration();
            
            System.out.println("\n=== All examples completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Example execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 1: Apply a specific transformation to a Java file
     */
    public void demonstrateSpecificTransformation() {
        System.out.println("1. Specific Transformation Example");
        System.out.println("-----------------------------------");
        
        try {
            // Apply AddBrackets transformation
            TransformationResult result = transformer.applyTransform(
                "AddBrackets",                    // transformation name
                "sample_input.java",              // input file
                "output_specific.java"            // output file
            );
            
            if (result.isSuccess()) {
                System.out.println("✅ Transformation successful!");
                System.out.println("Applied transformations: " + result.getAppliedTransforms());
                
                // Access metadata
                result.getMetadata().forEach((key, value) -> 
                    System.out.println("  " + key + ": " + value));
                    
            } else {
                System.out.println("❌ Transformation failed: " + result.getErrorMessage());
            }
            
        } catch (TransformerException e) {
            System.err.println("Error during specific transformation: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Example 2: Apply random transformation with multiple attempts
     */
    public void demonstrateRandomTransformation() {
        System.out.println("2. Random Transformation Example");
        System.out.println("---------------------------------");
        
        try {
            // Apply random transformation with up to 10 attempts
            TransformationResult result = transformer.applyRandomTransform(
                "sample_input.java",              // input file
                "output_random.java",             // output file
                10                                // max attempts
            );
            
            if (result.isSuccess()) {
                System.out.println("✅ Random transformation successful!");
                System.out.println("Applied transformations: " + result.getAppliedTransforms());
                System.out.println("Attempts used: " + result.getMetadata().get("attempts"));
            } else {
                System.out.println("❌ No suitable transformation found after 10 attempts");
                System.out.println("Reason: " + result.getErrorMessage());
            }
            
        } catch (TransformerException e) {
            System.err.println("Error during random transformation: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Example 3: Apply guided transformation using bug information
     */
    public void demonstrateGuidedTransformation() {
        System.out.println("3. Guided Transformation Example");
        System.out.println("---------------------------------");
        
        try {
            // Simulate bug information from static analysis
            List<Integer> bugLines = Arrays.asList(5, 8, 12);
            System.out.println("Bug lines detected: " + bugLines);
            
            // Apply guided transformation
            TransformationResult result = transformer.applyGuidedTransform(
                "sample_input.java",              // input file
                "output_guided.java",             // output file
                5,                                // max attempts
                true,                             // has bugs
                bugLines                          // bug line numbers
            );
            
            if (result.isSuccess()) {
                System.out.println("✅ Guided transformation successful!");
                System.out.println("Applied transformations: " + result.getAppliedTransforms());
                System.out.println("Focused on bug lines: " + bugLines);
                
                // Check if transformation targeted bug areas
                String targetInfo = (String) result.getMetadata().get("target_lines");
                if (targetInfo != null) {
                    System.out.println("Targeted lines: " + targetInfo);
                }
            } else {
                System.out.println("❌ Guided transformation failed: " + result.getErrorMessage());
            }
            
        } catch (BugInformationException e) {
            System.err.println("Bug information error: " + e.getMessage());
        } catch (TransformerException e) {
            System.err.println("Error during guided transformation: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Example 4: Discover and explore available transformations
     */
    public void demonstrateTransformationDiscovery() {
        System.out.println("4. Transformation Discovery Example");
        System.out.println("-----------------------------------");
        
        try {
            // Get all available transformations
            List<String> transforms = transformer.getAvailableTransforms();
            
            System.out.println("Total available transformations: " + transforms.size());
            System.out.println();
            
            // Group transformations by category (simple heuristic)
            List<String> loopTransforms = transforms.stream()
                .filter(t -> t.toLowerCase().contains("loop"))
                .collect(Collectors.toList());
                
            List<String> bracketTransforms = transforms.stream()
                .filter(t -> t.toLowerCase().contains("bracket"))
                .collect(Collectors.toList());
                
            List<String> operatorTransforms = transforms.stream()
                .filter(t -> t.toLowerCase().contains("operator") || t.toLowerCase().contains("arithmetic"))
                .collect(Collectors.toList());
            
            System.out.println("Loop-related transformations:");
            loopTransforms.forEach(t -> System.out.println("  • " + t));
            
            System.out.println("\nBracket-related transformations:");
            bracketTransforms.forEach(t -> System.out.println("  • " + t));
            
            System.out.println("\nOperator-related transformations:");
            operatorTransforms.forEach(t -> System.out.println("  • " + t));
            
            // Test a few transformations to see which ones work with our sample
            System.out.println("\nTesting transformations with sample file:");
            List<String> testTransforms = Arrays.asList("AddBrackets", "LoopConversion1", "AddGlobalAssignment");
            
            for (String transformName : testTransforms) {
                if (transforms.contains(transformName)) {
                    TransformationResult result = transformer.applyTransform(
                        transformName, "sample_input.java", "test_" + transformName + ".java"
                    );
                    
                    System.out.println("  " + transformName + ": " + 
                        (result.isSuccess() ? "✅ Compatible" : "❌ Not applicable"));
                }
            }
            
        } catch (TransformerException e) {
            System.err.println("Error during transformation discovery: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Example 5: Comprehensive error handling patterns
     */
    public void demonstrateErrorHandling() {
        System.out.println("5. Error Handling Example");
        System.out.println("-------------------------");
        
        // Test various error scenarios
        
        // Scenario 1: Invalid transformation name
        System.out.println("Testing invalid transformation name...");
        try {
            TransformationResult result = transformer.applyTransform(
                "NonExistentTransform", "sample_input.java", "output_error1.java"
            );
            System.out.println("Result: " + (result.isSuccess() ? "Success" : "Failed as expected"));
        } catch (TransformerException e) {
            System.out.println("Caught expected error: " + e.getClass().getSimpleName());
        }
        
        // Scenario 2: Invalid file path
        System.out.println("Testing non-existent input file...");
        try {
            TransformationResult result = transformer.applyTransform(
                "AddBrackets", "non_existent_file.java", "output_error2.java"
            );
            System.out.println("Result: " + (result.isSuccess() ? "Success" : "Failed as expected"));
        } catch (IOTransformerException e) {
            System.out.println("Caught expected I/O error: " + e.getMessage());
        } catch (TransformerException e) {
            System.out.println("Caught expected error: " + e.getClass().getSimpleName());
        }
        
        // Scenario 3: Invalid bug information
        System.out.println("Testing invalid bug line numbers...");
        try {
            List<Integer> invalidBugLines = Arrays.asList(-1, 0, 1000000);
            TransformationResult result = transformer.applyGuidedTransform(
                "sample_input.java", "output_error3.java", 3, true, invalidBugLines
            );
            System.out.println("Result: " + (result.isSuccess() ? "Success" : "Failed as expected"));
        } catch (BugInformationException e) {
            System.out.println("Caught expected bug information error: " + e.getMessage());
        } catch (TransformerException e) {
            System.out.println("Caught expected error: " + e.getClass().getSimpleName());
        }
        
        // Scenario 4: Robust error handling pattern
        System.out.println("Demonstrating robust error handling pattern...");
        TransformationResult result = safeTransform("AddBrackets", "sample_input.java", "output_safe.java");
        System.out.println("Safe transformation result: " + 
            (result != null && result.isSuccess() ? "Success" : "Handled gracefully"));
        
        System.out.println();
    }
    
    /**
     * Example 6: Batch processing multiple files
     */
    public void demonstrateBatchProcessing() {
        System.out.println("6. Batch Processing Example");
        System.out.println("---------------------------");
        
        try {
            // Create multiple sample files
            createMultipleSampleFiles();
            
            // Process all Java files in the current directory
            File currentDir = new File(".");
            File[] javaFiles = currentDir.listFiles((dir, name) -> name.endsWith(".java") && name.startsWith("batch_"));
            
            if (javaFiles != null && javaFiles.length > 0) {
                System.out.println("Processing " + javaFiles.length + " files...");
                
                int successCount = 0;
                int failureCount = 0;
                
                for (File javaFile : javaFiles) {
                    String inputPath = javaFile.getName();
                    String outputPath = "processed_" + javaFile.getName();
                    
                    System.out.print("Processing " + inputPath + "... ");
                    
                    TransformationResult result = transformer.applyRandomTransform(inputPath, outputPath, 3);
                    
                    if (result.isSuccess()) {
                        successCount++;
                        System.out.println("✅ " + result.getAppliedTransforms());
                    } else {
                        failureCount++;
                        System.out.println("❌ " + result.getErrorMessage());
                    }
                }
                
                System.out.println("\nBatch processing summary:");
                System.out.println("  Successful: " + successCount);
                System.out.println("  Failed: " + failureCount);
                System.out.println("  Total: " + (successCount + failureCount));
            } else {
                System.out.println("No batch files found to process");
            }
            
        } catch (Exception e) {
            System.err.println("Error during batch processing: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Example 7: Integration with simulated static analysis
     */
    public void demonstrateStaticAnalysisIntegration() {
        System.out.println("7. Static Analysis Integration Example");
        System.out.println("--------------------------------------");
        
        try {
            String inputFile = "sample_input.java";
            
            // Simulate running static analysis
            System.out.println("Running simulated static analysis...");
            List<Integer> bugLines = simulateStaticAnalysis(inputFile);
            
            if (!bugLines.isEmpty()) {
                System.out.println("Static analysis found issues at lines: " + bugLines);
                
                // Apply guided transformation based on analysis results
                TransformationResult result = transformer.applyGuidedTransform(
                    inputFile, "output_analysis_guided.java", 5, true, bugLines
                );
                
                if (result.isSuccess()) {
                    System.out.println("✅ Applied guided transformations: " + result.getAppliedTransforms());
                    
                    // Simulate re-running analysis on transformed code
                    System.out.println("Re-analyzing transformed code...");
                    List<Integer> newBugLines = simulateStaticAnalysis("output_analysis_guided.java");
                    
                    System.out.println("Issues after transformation: " + newBugLines.size());
                    if (newBugLines.size() != bugLines.size()) {
                        System.out.println("⚠️  Issue count changed - potential metamorphic testing opportunity!");
                    }
                } else {
                    System.out.println("❌ Guided transformation failed: " + result.getErrorMessage());
                    
                    // Fallback to random transformation
                    System.out.println("Falling back to random transformation...");
                    TransformationResult fallbackResult = transformer.applyRandomTransform(
                        inputFile, "output_analysis_random.java", 5
                    );
                    
                    if (fallbackResult.isSuccess()) {
                        System.out.println("✅ Fallback successful: " + fallbackResult.getAppliedTransforms());
                    }
                }
            } else {
                System.out.println("✅ No issues found by static analysis - code is clean!");
                
                // Still apply transformation for testing purposes
                TransformationResult result = transformer.applyRandomTransform(
                    inputFile, "output_clean_code.java", 3
                );
                
                if (result.isSuccess()) {
                    System.out.println("Applied transformation to clean code: " + result.getAppliedTransforms());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during static analysis integration: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    // Helper methods
    
    /**
     * Safe transformation with comprehensive error handling
     */
    private TransformationResult safeTransform(String transformName, String inputFile, String outputFile) {
        try {
            return transformer.applyTransform(transformName, inputFile, outputFile);
            
        } catch (BugInformationException e) {
            System.err.println("Bug information error: " + e.getMessage());
            return null;
            
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
            return null;
            
        } catch (IOTransformerException e) {
            System.err.println("I/O error: " + e.getMessage());
            return null;
            
        } catch (TransformerException e) {
            System.err.println("Transformation error: " + e.getMessage());
            return null;
            
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Simulate static analysis by returning some line numbers
     */
    private List<Integer> simulateStaticAnalysis(String filePath) {
        // In a real implementation, this would call PMD, SpotBugs, etc.
        // For demonstration, return some simulated bug lines
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            List<Integer> bugLines = new ArrayList<>();
            
            // Simple heuristic: flag lines with certain patterns
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.contains("=") && !line.contains("==") && !line.contains("!=")) {
                    bugLines.add(i + 1); // 1-based line numbers
                }
            }
            
            return bugLines;
            
        } catch (IOException e) {
            System.err.println("Error reading file for analysis: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Create a sample input file for demonstration
     */
    private void createSampleInputFile() throws IOException {
        String sampleCode = 
            "public class SampleClass {\n" +
            "    private int value;\n" +
            "    \n" +
            "    public void setValue(int newValue) {\n" +
            "        value = newValue;\n" +
            "    }\n" +
            "    \n" +
            "    public void processArray(int[] array) {\n" +
            "        for (int i = 0; i < array.length; i++)\n" +
            "            System.out.println(array[i]);\n" +
            "    }\n" +
            "    \n" +
            "    public int calculate() {\n" +
            "        int result = 10 + 20;\n" +
            "        return result * 2;\n" +
            "    }\n" +
            "}\n";
        
        Files.write(Paths.get("sample_input.java"), sampleCode.getBytes());
    }
    
    /**
     * Create multiple sample files for batch processing
     */
    private void createMultipleSampleFiles() throws IOException {
        String[] sampleCodes = {
            "public class BatchExample1 {\n    public void method() { int x = 1; }\n}\n",
            "public class BatchExample2 {\n    public void loop() { for(int i=0; i<10; i++) System.out.println(i); }\n}\n",
            "public class BatchExample3 {\n    public int calc() { return 5 + 3; }\n}\n"
        };
        
        for (int i = 0; i < sampleCodes.length; i++) {
            Files.write(Paths.get("batch_example" + (i + 1) + ".java"), sampleCodes[i].getBytes());
        }
    }
}