# Usage Examples

## Overview

This document provides comprehensive usage examples for the Transformer library, demonstrating various scenarios including bug information integration, batch processing, and real-world use cases.

## Basic Usage Examples

### 1. Simple Transformation

```java
import com.transformer.api.TransformerService;
import com.transformer.api.TransformerServiceImpl;
import com.transformer.api.TransformationResult;

public class BasicTransformationExample {
    public static void main(String[] args) {
        // Create transformer service
        TransformerService transformer = new TransformerServiceImpl();
        
        // Apply a specific transformation
        TransformationResult result = transformer.applyTransform(
            "AddBrackets", 
            "src/main/java/Example.java", 
            "transformed/Example.java"
        );
        
        // Check result
        if (result.isSuccess()) {
            System.out.println("✅ Transformation successful!");
            System.out.println("Applied transformations: " + result.getAppliedTransforms());
            
            // Print metadata
            result.getMetadata().forEach((key, value) -> 
                System.out.println(key + ": " + value));
        } else {
            System.err.println("❌ Transformation failed: " + result.getErrorMessage());
        }
    }
}
```

### 2. Random Location Strategy

```java
public class RandomTransformationExample {
    public static void main(String[] args) {
        TransformerService transformer = new TransformerServiceImpl();
        
        // Apply random transformations with multiple attempts
        TransformationResult result = transformer.applyRandomTransform(
            "src/main/java/ComplexClass.java",
            "transformed/ComplexClass.java",
            10  // maximum attempts
        );
        
        if (result.isSuccess()) {
            System.out.println("Random transformation completed");
            System.out.println("Attempts made: " + result.getMetadata().get("attempts"));
            System.out.println("Transformations applied: " + result.getAppliedTransforms());
        } else {
            System.out.println("No suitable transformations found after 10 attempts");
        }
    }
}
```

## Bug Information Integration Examples

### 3. Static Analysis Integration

```java
import java.util.List;
import java.util.Arrays;

public class StaticAnalysisIntegrationExample {
    
    public static void main(String[] args) {
        // Simulate getting bug information from a static analyzer
        List<Integer> bugLines = runStaticAnalysis("src/main/java/BuggyClass.java");
        
        TransformerService transformer = new TransformerServiceImpl();
        
        if (!bugLines.isEmpty()) {
            System.out.println("Bugs detected at lines: " + bugLines);
            
            // Use guided transformation based on bug locations
            TransformationResult result = transformer.applyGuidedTransform(
                "src/main/java/BuggyClass.java",
                "transformed/BuggyClass.java",
                5,  // max attempts
                true,  // has bugs
                bugLines
            );
            
            if (result.isSuccess()) {
                System.out.println("✅ Guided transformation successful");
                System.out.println("Focused on bug lines: " + bugLines);
                System.out.println("Applied: " + result.getAppliedTransforms());
            } else {
                System.err.println("❌ Guided transformation failed: " + result.getErrorMessage());
            }
        } else {
            System.out.println("No bugs detected, using random strategy");
            
            TransformationResult result = transformer.applyRandomTransform(
                "src/main/java/BuggyClass.java",
                "transformed/BuggyClass.java",
                5
            );
            
            System.out.println("Random transformation result: " + result.isSuccess());
        }
    }
    
    // Simulate static analysis tool integration
    private static List<Integer> runStaticAnalysis(String filePath) {
        // In real implementation, this would call PMD, SpotBugs, etc.
        // For example, integrating with PMD:
        /*
        PMDConfiguration config = new PMDConfiguration();
        config.setInputPaths(filePath);
        config.setRuleSets("rulesets/java/quickstart.xml");
        
        Report report = PMD.processFiles(config);
        return report.getViolations().stream()
            .map(RuleViolation::getBeginLine)
            .collect(Collectors.toList());
        */
        
        // Simulated bug lines for demonstration
        return Arrays.asList(15, 23, 45, 67);
    }
}
```

### 4. PMD Integration Example

```java
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleViolation;
import java.util.stream.Collectors;

public class PMDIntegrationExample {
    
    public static void transformBasedOnPMDResults(String inputFile, String outputFile) {
        try {
            // Run PMD analysis
            List<Integer> bugLines = runPMDAnalysis(inputFile);
            
            TransformerService transformer = new TransformerServiceImpl();
            
            if (!bugLines.isEmpty()) {
                System.out.println("PMD found issues at lines: " + bugLines);
                
                // Apply guided transformations
                TransformationResult result = transformer.applyGuidedTransform(
                    inputFile, outputFile, 7, true, bugLines
                );
                
                if (result.isSuccess()) {
                    System.out.println("✅ PMD-guided transformation completed");
                    System.out.println("Transformations: " + result.getAppliedTransforms());
                    
                    // Optionally, run PMD again to see if issues changed
                    List<Integer> newBugLines = runPMDAnalysis(outputFile);
                    System.out.println("Issues after transformation: " + newBugLines.size());
                } else {
                    System.err.println("❌ Transformation failed: " + result.getErrorMessage());
                }
            } else {
                System.out.println("✅ No PMD issues found - code is clean!");
            }
            
        } catch (Exception e) {
            System.err.println("Error during PMD integration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<Integer> runPMDAnalysis(String filePath) throws Exception {
        PMDConfiguration config = new PMDConfiguration();
        config.setInputPaths(filePath);
        config.setRuleSets("rulesets/java/quickstart.xml");
        config.setReportFormat("text");
        
        Report report = PMD.processFiles(config);
        
        return report.getViolations().stream()
            .map(RuleViolation::getBeginLine)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java PMDIntegrationExample <input.java> <output.java>");
            System.exit(1);
        }
        
        transformBasedOnPMDResults(args[0], args[1]);
    }
}
```

### 5. SpotBugs Integration Example

```java
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Project;

public class SpotBugsIntegrationExample {
    
    public static void transformBasedOnSpotBugsResults(String inputFile, String outputFile) {
        try {
            // Run SpotBugs analysis
            List<Integer> bugLines = runSpotBugsAnalysis(inputFile);
            
            TransformerService transformer = new TransformerServiceImpl();
            
            if (!bugLines.isEmpty()) {
                System.out.println("SpotBugs found issues at lines: " + bugLines);
                
                TransformationResult result = transformer.applyGuidedTransform(
                    inputFile, outputFile, 5, true, bugLines
                );
                
                if (result.isSuccess()) {
                    System.out.println("✅ SpotBugs-guided transformation completed");
                    System.out.println("Applied transformations: " + result.getAppliedTransforms());
                } else {
                    System.err.println("❌ Transformation failed: " + result.getErrorMessage());
                }
            } else {
                System.out.println("✅ No SpotBugs issues found!");
            }
            
        } catch (Exception e) {
            System.err.println("Error during SpotBugs integration: " + e.getMessage());
        }
    }
    
    private static List<Integer> runSpotBugsAnalysis(String filePath) throws Exception {
        // Initialize SpotBugs
        DetectorFactoryCollection.instance();
        
        Project project = new Project();
        project.addFile(filePath);
        
        FindBugs2 findBugs = new FindBugs2();
        findBugs.setProject(project);
        
        // Custom bug reporter to collect line numbers
        List<Integer> bugLines = new ArrayList<>();
        findBugs.setBugReporter(new BugReporter() {
            @Override
            public void reportBug(BugInstance bugInstance) {
                if (bugInstance.getPrimarySourceLineAnnotation() != null) {
                    int line = bugInstance.getPrimarySourceLineAnnotation().getStartLine();
                    if (line > 0) {
                        bugLines.add(line);
                    }
                }
            }
        });
        
        findBugs.execute();
        
        return bugLines.stream().distinct().sorted().collect(Collectors.toList());
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java SpotBugsIntegrationExample <input.java> <output.java>");
            System.exit(1);
        }
        
        transformBasedOnSpotBugsResults(args[0], args[1]);
    }
}
```

## Batch Processing Examples

### 6. Batch File Processing

```java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class BatchProcessingExample {
    
    public static void processBatchFiles(String inputDir, String outputDir) {
        TransformerService transformer = new TransformerServiceImpl();
        
        try (Stream<Path> paths = Files.walk(Paths.get(inputDir))) {
            List<Path> javaFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toList());
            
            System.out.println("Found " + javaFiles.size() + " Java files to process");
            
            int successCount = 0;
            int failureCount = 0;
            
            for (Path javaFile : javaFiles) {
                String inputPath = javaFile.toString();
                String outputPath = outputDir + "/" + javaFile.getFileName().toString();
                
                System.out.println("Processing: " + inputPath);
                
                // Apply random transformation to each file
                TransformationResult result = transformer.applyRandomTransform(
                    inputPath, outputPath, 5
                );
                
                if (result.isSuccess()) {
                    successCount++;
                    System.out.println("  ✅ Success: " + result.getAppliedTransforms());
                } else {
                    failureCount++;
                    System.out.println("  ❌ Failed: " + result.getErrorMessage());
                }
            }
            
            System.out.println("\nBatch processing completed:");
            System.out.println("  Successful: " + successCount);
            System.out.println("  Failed: " + failureCount);
            
        } catch (Exception e) {
            System.err.println("Error during batch processing: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java BatchProcessingExample <inputDir> <outputDir>");
            System.exit(1);
        }
        
        // Ensure output directory exists
        new File(args[1]).mkdirs();
        
        processBatchFiles(args[0], args[1]);
    }
}
```

### 7. Parallel Batch Processing

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelBatchProcessingExample {
    
    public static void processFilesInParallel(List<String> inputFiles, String outputDir) {
        TransformerService transformer = new TransformerServiceImpl();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            List<CompletableFuture<TransformationResult>> futures = inputFiles.stream()
                .map(inputFile -> CompletableFuture.supplyAsync(() -> {
                    String fileName = Paths.get(inputFile).getFileName().toString();
                    String outputFile = outputDir + "/" + fileName;
                    
                    System.out.println("Processing " + inputFile + " on thread " + 
                                     Thread.currentThread().getName());
                    
                    return transformer.applyRandomTransform(inputFile, outputFile, 5);
                }, executor))
                .collect(Collectors.toList());
            
            // Wait for all transformations to complete
            List<TransformationResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            // Analyze results
            long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
            long failureCount = results.size() - successCount;
            
            System.out.println("\nParallel processing completed:");
            System.out.println("  Total files: " + results.size());
            System.out.println("  Successful: " + successCount);
            System.out.println("  Failed: " + failureCount);
            
            // Print detailed results
            for (int i = 0; i < inputFiles.size(); i++) {
                TransformationResult result = results.get(i);
                System.out.println(inputFiles.get(i) + ": " + 
                    (result.isSuccess() ? "✅ " + result.getAppliedTransforms() : 
                                         "❌ " + result.getErrorMessage()));
            }
            
        } finally {
            executor.shutdown();
        }
    }
    
    public static void main(String[] args) throws Exception {
        // Find all Java files in a directory
        List<String> javaFiles = Files.walk(Paths.get("src/main/java"))
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".java"))
            .map(Path::toString)
            .collect(Collectors.toList());
        
        processFilesInParallel(javaFiles, "transformed");
    }
}
```

## Advanced Usage Examples

### 8. Custom Transformation Pipeline

```java
public class TransformationPipelineExample {
    
    private final TransformerService transformer;
    private final List<String> transformationSequence;
    
    public TransformationPipelineExample() {
        this.transformer = new TransformerServiceImpl();
        this.transformationSequence = Arrays.asList(
            "AddBrackets",
            "LoopConversion1", 
            "AddGlobalAssignment"
        );
    }
    
    public void runTransformationPipeline(String inputFile, String outputDir, 
                                        List<Integer> bugLines) {
        String currentInput = inputFile;
        List<String> allAppliedTransforms = new ArrayList<>();
        
        for (int i = 0; i < transformationSequence.size(); i++) {
            String transformName = transformationSequence.get(i);
            String currentOutput = outputDir + "/step" + (i + 1) + "_" + 
                                 Paths.get(inputFile).getFileName().toString();
            
            System.out.println("Step " + (i + 1) + ": Applying " + transformName);
            
            TransformationResult result;
            
            if (bugLines != null && !bugLines.isEmpty()) {
                // Use guided transformation for first step, then random
                if (i == 0) {
                    result = transformer.applyGuidedTransform(
                        currentInput, currentOutput, 3, true, bugLines
                    );
                } else {
                    result = transformer.applyRandomTransform(currentInput, currentOutput, 3);
                }
            } else {
                result = transformer.applyTransform(transformName, currentInput, currentOutput);
            }
            
            if (result.isSuccess()) {
                System.out.println("  ✅ Applied: " + result.getAppliedTransforms());
                allAppliedTransforms.addAll(result.getAppliedTransforms());
                currentInput = currentOutput; // Use output as input for next step
            } else {
                System.out.println("  ❌ Failed: " + result.getErrorMessage());
                break; // Stop pipeline on failure
            }
        }
        
        System.out.println("\nPipeline completed. Total transformations applied: " + 
                          allAppliedTransforms);
    }
    
    public static void main(String[] args) {
        TransformationPipelineExample pipeline = new TransformationPipelineExample();
        
        // Example with bug information
        List<Integer> bugLines = Arrays.asList(15, 30, 45);
        pipeline.runTransformationPipeline(
            "src/main/java/Example.java", 
            "pipeline-output", 
            bugLines
        );
    }
}
```

### 9. Metamorphic Testing Example

```java
public class MetamorphicTestingExample {
    
    public static void performMetamorphicTesting(String originalFile, 
                                               List<Integer> bugLines) {
        TransformerService transformer = new TransformerServiceImpl();
        
        // Generate multiple transformed versions
        List<String> transformedFiles = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            String outputFile = "metamorphic/version_" + i + ".java";
            
            TransformationResult result;
            if (bugLines != null && !bugLines.isEmpty()) {
                result = transformer.applyGuidedTransform(
                    originalFile, outputFile, 3, true, bugLines
                );
            } else {
                result = transformer.applyRandomTransform(originalFile, outputFile, 3);
            }
            
            if (result.isSuccess()) {
                transformedFiles.add(outputFile);
                System.out.println("Generated version " + i + ": " + 
                                 result.getAppliedTransforms());
            }
        }
        
        // Now run static analysis on all versions
        System.out.println("\nRunning metamorphic testing...");
        
        // Analyze original file
        List<String> originalIssues = runStaticAnalysisForTesting(originalFile);
        System.out.println("Original file issues: " + originalIssues.size());
        
        // Analyze transformed versions
        for (String transformedFile : transformedFiles) {
            List<String> transformedIssues = runStaticAnalysisForTesting(transformedFile);
            
            // Compare results - this is where you'd implement your metamorphic relations
            if (transformedIssues.size() != originalIssues.size()) {
                System.out.println("⚠️  Potential bug detected in static analyzer!");
                System.out.println("  Original issues: " + originalIssues.size());
                System.out.println("  Transformed issues: " + transformedIssues.size());
                System.out.println("  File: " + transformedFile);
            } else {
                System.out.println("✅ Consistent results for " + transformedFile);
            }
        }
    }
    
    private static List<String> runStaticAnalysisForTesting(String filePath) {
        // Simulate running static analysis and returning issue descriptions
        // In real implementation, this would call your static analyzer
        return Arrays.asList("Unused variable", "Missing null check");
    }
    
    public static void main(String[] args) {
        List<Integer> bugLines = Arrays.asList(20, 35, 50);
        performMetamorphicTesting("src/main/java/TestSubject.java", bugLines);
    }
}
```

### 10. Configuration-Driven Processing

```java
public class ConfigurationDrivenExample {
    
    public static class ProcessingConfig {
        private String inputDirectory;
        private String outputDirectory;
        private List<String> transformations;
        private int maxAttempts;
        private boolean useBugInformation;
        private String staticAnalyzer;
        
        // Getters and setters...
    }
    
    public static void processWithConfiguration(ProcessingConfig config) {
        TransformerService transformer = new TransformerServiceImpl();
        
        try (Stream<Path> paths = Files.walk(Paths.get(config.getInputDirectory()))) {
            List<Path> javaFiles = paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toList());
            
            for (Path javaFile : javaFiles) {
                String inputPath = javaFile.toString();
                String outputPath = config.getOutputDirectory() + "/" + 
                                  javaFile.getFileName().toString();
                
                List<Integer> bugLines = null;
                if (config.isUseBugInformation()) {
                    bugLines = getBugLinesFromAnalyzer(inputPath, config.getStaticAnalyzer());
                }
                
                TransformationResult result = null;
                
                if (config.getTransformations() != null && !config.getTransformations().isEmpty()) {
                    // Apply specific transformations
                    for (String transformName : config.getTransformations()) {
                        result = transformer.applyTransform(transformName, inputPath, outputPath);
                        if (result.isSuccess()) break;
                    }
                } else if (bugLines != null && !bugLines.isEmpty()) {
                    // Use guided strategy
                    result = transformer.applyGuidedTransform(
                        inputPath, outputPath, config.getMaxAttempts(), true, bugLines
                    );
                } else {
                    // Use random strategy
                    result = transformer.applyRandomTransform(
                        inputPath, outputPath, config.getMaxAttempts()
                    );
                }
                
                if (result != null && result.isSuccess()) {
                    System.out.println("✅ " + inputPath + ": " + result.getAppliedTransforms());
                } else {
                    System.out.println("❌ " + inputPath + ": " + 
                                     (result != null ? result.getErrorMessage() : "No result"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during configuration-driven processing: " + e.getMessage());
        }
    }
    
    private static List<Integer> getBugLinesFromAnalyzer(String filePath, String analyzer) {
        // Implement integration with specified static analyzer
        switch (analyzer.toLowerCase()) {
            case "pmd":
                return runPMDAnalysis(filePath);
            case "spotbugs":
                return runSpotBugsAnalysis(filePath);
            case "checkstyle":
                return runCheckStyleAnalysis(filePath);
            default:
                return new ArrayList<>();
        }
    }
    
    // Implement analyzer-specific methods...
    private static List<Integer> runPMDAnalysis(String filePath) { /* ... */ return new ArrayList<>(); }
    private static List<Integer> runSpotBugsAnalysis(String filePath) { /* ... */ return new ArrayList<>(); }
    private static List<Integer> runCheckStyleAnalysis(String filePath) { /* ... */ return new ArrayList<>(); }
    
    public static void main(String[] args) {
        ProcessingConfig config = new ProcessingConfig();
        config.setInputDirectory("src/main/java");
        config.setOutputDirectory("transformed");
        config.setMaxAttempts(5);
        config.setUseBugInformation(true);
        config.setStaticAnalyzer("pmd");
        
        processWithConfiguration(config);
    }
}
```

## Error Handling Examples

### 11. Comprehensive Error Handling

```java
public class ErrorHandlingExample {
    
    public static void robustTransformation(String inputFile, String outputFile, 
                                          List<Integer> bugLines) {
        TransformerService transformer = new TransformerServiceImpl();
        
        try {
            // Validate inputs first
            validateInputs(inputFile, outputFile, bugLines);
            
            // Attempt transformation with retry logic
            TransformationResult result = attemptTransformationWithRetry(
                transformer, inputFile, outputFile, bugLines, 3
            );
            
            if (result.isSuccess()) {
                System.out.println("✅ Transformation successful");
                System.out.println("Applied: " + result.getAppliedTransforms());
                
                // Validate output
                validateTransformedCode(outputFile);
                
            } else {
                System.err.println("❌ All transformation attempts failed");
                System.err.println("Last error: " + result.getErrorMessage());
                
                // Attempt fallback strategy
                attemptFallbackTransformation(transformer, inputFile, outputFile);
            }
            
        } catch (BugInformationException e) {
            System.err.println("❌ Bug information error: " + e.getMessage());
            System.err.println("Suggestion: Check bug line numbers and ensure they're positive");
            
        } catch (ParseException e) {
            System.err.println("❌ Parse error: " + e.getMessage());
            System.err.println("Suggestion: Check input file for syntax errors");
            
        } catch (IOTransformerException e) {
            System.err.println("❌ I/O error: " + e.getMessage());
            System.err.println("Suggestion: Check file paths and permissions");
            
        } catch (ConfigurationException e) {
            System.err.println("❌ Configuration error: " + e.getMessage());
            System.err.println("Suggestion: Check transformer.properties file");
            
        } catch (TransformerException e) {
            System.err.println("❌ Transformation error: " + e.getMessage());
            e.printStackTrace();
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void validateInputs(String inputFile, String outputFile, 
                                     List<Integer> bugLines) throws IllegalArgumentException {
        if (inputFile == null || !new File(inputFile).exists()) {
            throw new IllegalArgumentException("Input file does not exist: " + inputFile);
        }
        
        if (outputFile == null || outputFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Output file path cannot be empty");
        }
        
        if (bugLines != null) {
            for (Integer line : bugLines) {
                if (line == null || line <= 0) {
                    throw new IllegalArgumentException("Invalid bug line number: " + line);
                }
            }
        }
    }
    
    private static TransformationResult attemptTransformationWithRetry(
            TransformerService transformer, String inputFile, String outputFile,
            List<Integer> bugLines, int maxRetries) throws TransformerException {
        
        TransformationResult lastResult = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            System.out.println("Transformation attempt " + attempt + "/" + maxRetries);
            
            try {
                if (bugLines != null && !bugLines.isEmpty()) {
                    lastResult = transformer.applyGuidedTransform(
                        inputFile, outputFile, 3, true, bugLines
                    );
                } else {
                    lastResult = transformer.applyRandomTransform(inputFile, outputFile, 3);
                }
                
                if (lastResult.isSuccess()) {
                    System.out.println("✅ Attempt " + attempt + " succeeded");
                    return lastResult;
                }
                
                System.out.println("❌ Attempt " + attempt + " failed: " + lastResult.getErrorMessage());
                
                // Wait before retry
                if (attempt < maxRetries) {
                    Thread.sleep(1000);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TransformerException("Transformation interrupted", e);
            }
        }
        
        return lastResult;
    }
    
    private static void attemptFallbackTransformation(TransformerService transformer,
                                                    String inputFile, String outputFile) {
        System.out.println("Attempting fallback transformation...");
        
        try {
            // Try with a simple, reliable transformation
            TransformationResult fallbackResult = transformer.applyTransform(
                "AddBrackets", inputFile, outputFile
            );
            
            if (fallbackResult.isSuccess()) {
                System.out.println("✅ Fallback transformation succeeded");
            } else {
                System.out.println("❌ Fallback transformation also failed");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Fallback transformation error: " + e.getMessage());
        }
    }
    
    private static void validateTransformedCode(String outputFile) {
        try {
            // Basic syntax validation
            String code = Files.readString(Paths.get(outputFile));
            
            // Check for basic syntax issues
            if (code.contains(";;") || code.contains("{{") || code.contains("}}")) {
                System.out.println("⚠️  Warning: Potential syntax issues detected in output");
            }
            
            // Try to compile (optional)
            // JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            // int result = compiler.run(null, null, null, outputFile);
            // if (result != 0) {
            //     System.out.println("⚠️  Warning: Transformed code may not compile");
            // }
            
        } catch (Exception e) {
            System.out.println("⚠️  Warning: Could not validate transformed code: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        List<Integer> bugLines = Arrays.asList(15, 25, 35);
        robustTransformation("src/main/java/Example.java", "transformed/Example.java", bugLines);
    }
}
```

These examples demonstrate various real-world usage scenarios for the Transformer library, including integration with static analysis tools, batch processing, error handling, and advanced transformation pipelines. Each example includes proper error handling and demonstrates best practices for using the library effectively.