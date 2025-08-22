package com.transformer.examples;

import com.transformer.api.TransformerService;
import com.transformer.api.TransformerServiceImpl;
import com.transformer.api.TransformationResult;
import com.transformer.api.exception.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command-line example demonstrating how to use the Transformer library
 * from the command line with various options and error handling.
 */
public class CommandLineExample {
    
    private static final String USAGE = 
        "Usage: java -cp transformer-1.0.0-jar-with-dependencies.jar CommandLineExample [OPTIONS]\n" +
        "\n" +
        "Options:\n" +
        "  --transform <name>     Apply specific transformation (e.g., AddBrackets)\n" +
        "  --random               Apply random transformation\n" +
        "  --guided               Apply guided transformation (requires --bug-lines)\n" +
        "  --input <file>         Input Java file (required)\n" +
        "  --output <file>        Output Java file (required)\n" +
        "  --attempts <num>       Maximum attempts for random/guided (default: 5)\n" +
        "  --bug-lines <lines>    Comma-separated bug line numbers (for guided mode)\n" +
        "  --list-transforms      List all available transformations\n" +
        "  --help                 Show this help message\n" +
        "\n" +
        "Examples:\n" +
        "  # Apply specific transformation\n" +
        "  java -cp transformer.jar CommandLineExample --transform AddBrackets --input Example.java --output Result.java\n" +
        "\n" +
        "  # Apply random transformation\n" +
        "  java -cp transformer.jar CommandLineExample --random --input Example.java --output Result.java --attempts 10\n" +
        "\n" +
        "  # Apply guided transformation\n" +
        "  java -cp transformer.jar CommandLineExample --guided --input Example.java --output Result.java --bug-lines 15,23,45\n" +
        "\n" +
        "  # List available transformations\n" +
        "  java -cp transformer.jar CommandLineExample --list-transforms\n";
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(USAGE);
            System.exit(1);
        }
        
        try {
            CommandLineOptions options = parseArguments(args);
            
            if (options.showHelp) {
                System.out.println(USAGE);
                return;
            }
            
            TransformerService transformer = new TransformerServiceImpl();
            
            if (options.listTransforms) {
                listAvailableTransformations(transformer);
                return;
            }
            
            // Validate required arguments
            validateRequiredArguments(options);
            
            // Execute transformation based on mode
            TransformationResult result = executeTransformation(transformer, options);
            
            // Display results
            displayResults(result, options);
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Invalid arguments: " + e.getMessage());
            System.err.println("\nUse --help for usage information");
            System.exit(1);
            
        } catch (BugInformationException e) {
            System.err.println("❌ Bug information error: " + e.getMessage());
            System.err.println("Hint: Ensure bug line numbers are positive and within file bounds");
            System.exit(1);
            
        } catch (ParseException e) {
            System.err.println("❌ Parse error: " + e.getMessage());
            System.err.println("Hint: Check that input file contains valid Java syntax");
            System.exit(1);
            
        } catch (IOTransformerException e) {
            System.err.println("❌ I/O error: " + e.getMessage());
            System.err.println("Hint: Check file paths and permissions");
            System.exit(1);
            
        } catch (TransformerException e) {
            System.err.println("❌ Transformation error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static CommandLineOptions parseArguments(String[] args) {
        CommandLineOptions options = new CommandLineOptions();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "--help":
                    options.showHelp = true;
                    break;
                    
                case "--list-transforms":
                    options.listTransforms = true;
                    break;
                    
                case "--transform":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--transform requires a transformation name");
                    }
                    options.mode = TransformationMode.SPECIFIC;
                    options.transformName = args[++i];
                    break;
                    
                case "--random":
                    options.mode = TransformationMode.RANDOM;
                    break;
                    
                case "--guided":
                    options.mode = TransformationMode.GUIDED;
                    break;
                    
                case "--input":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--input requires a file path");
                    }
                    options.inputFile = args[++i];
                    break;
                    
                case "--output":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--output requires a file path");
                    }
                    options.outputFile = args[++i];
                    break;
                    
                case "--attempts":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--attempts requires a number");
                    }
                    try {
                        options.maxAttempts = Integer.parseInt(args[++i]);
                        if (options.maxAttempts <= 0) {
                            throw new IllegalArgumentException("--attempts must be positive");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("--attempts must be a valid number");
                    }
                    break;
                    
                case "--bug-lines":
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--bug-lines requires comma-separated line numbers");
                    }
                    String bugLinesStr = args[++i];
                    try {
                        options.bugLines = Arrays.stream(bugLinesStr.split(","))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                        
                        // Validate bug line numbers
                        for (Integer line : options.bugLines) {
                            if (line <= 0) {
                                throw new IllegalArgumentException("Bug line numbers must be positive");
                            }
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("--bug-lines must contain valid integers");
                    }
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown option: " + arg);
            }
        }
        
        return options;
    }
    
    private static void validateRequiredArguments(CommandLineOptions options) {
        if (options.mode == null) {
            throw new IllegalArgumentException("Must specify transformation mode: --transform, --random, or --guided");
        }
        
        if (options.inputFile == null) {
            throw new IllegalArgumentException("Input file is required (--input)");
        }
        
        if (options.outputFile == null) {
            throw new IllegalArgumentException("Output file is required (--output)");
        }
        
        // Check input file exists
        File inputFile = new File(options.inputFile);
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("Input file does not exist: " + options.inputFile);
        }
        
        if (!inputFile.canRead()) {
            throw new IllegalArgumentException("Cannot read input file: " + options.inputFile);
        }
        
        // Validate mode-specific requirements
        if (options.mode == TransformationMode.SPECIFIC && options.transformName == null) {
            throw new IllegalArgumentException("Specific transformation mode requires --transform <name>");
        }
        
        if (options.mode == TransformationMode.GUIDED && (options.bugLines == null || options.bugLines.isEmpty())) {
            throw new IllegalArgumentException("Guided transformation mode requires --bug-lines");
        }
        
        // Create output directory if it doesn't exist
        File outputFile = new File(options.outputFile);
        File outputDir = outputFile.getParentFile();
        if (outputDir != null && !outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IllegalArgumentException("Cannot create output directory: " + outputDir.getAbsolutePath());
            }
        }
    }
    
    private static void listAvailableTransformations(TransformerService transformer) {
        System.out.println("Available Transformations:");
        System.out.println("=========================");
        
        List<String> transforms = transformer.getAvailableTransforms();
        transforms.sort(String::compareTo);
        
        for (int i = 0; i < transforms.size(); i++) {
            System.out.printf("%2d. %s%n", i + 1, transforms.get(i));
        }
        
        System.out.println("\nTotal: " + transforms.size() + " transformations available");
        System.out.println("\nUsage: --transform <name>");
    }
    
    private static TransformationResult executeTransformation(TransformerService transformer, 
                                                            CommandLineOptions options) 
            throws TransformerException {
        
        System.out.println("🔄 Starting transformation...");
        System.out.println("Input:  " + options.inputFile);
        System.out.println("Output: " + options.outputFile);
        System.out.println("Mode:   " + options.mode);
        
        TransformationResult result;
        
        switch (options.mode) {
            case SPECIFIC:
                System.out.println("Transform: " + options.transformName);
                result = transformer.applyTransform(
                    options.transformName, 
                    options.inputFile, 
                    options.outputFile
                );
                break;
                
            case RANDOM:
                System.out.println("Max attempts: " + options.maxAttempts);
                result = transformer.applyRandomTransform(
                    options.inputFile, 
                    options.outputFile, 
                    options.maxAttempts
                );
                break;
                
            case GUIDED:
                System.out.println("Bug lines: " + options.bugLines);
                System.out.println("Max attempts: " + options.maxAttempts);
                result = transformer.applyGuidedTransform(
                    options.inputFile, 
                    options.outputFile, 
                    options.maxAttempts, 
                    true, 
                    options.bugLines
                );
                break;
                
            default:
                throw new IllegalStateException("Unknown transformation mode: " + options.mode);
        }
        
        return result;
    }
    
    private static void displayResults(TransformationResult result, CommandLineOptions options) {
        System.out.println("\n" + "=".repeat(50));
        
        if (result.isSuccess()) {
            System.out.println("✅ Transformation completed successfully!");
            System.out.println();
            
            System.out.println("Applied transformations:");
            for (String transform : result.getAppliedTransforms()) {
                System.out.println("  • " + transform);
            }
            
            // Display metadata if available
            if (!result.getMetadata().isEmpty()) {
                System.out.println();
                System.out.println("Metadata:");
                result.getMetadata().forEach((key, value) -> 
                    System.out.println("  " + key + ": " + value));
            }
            
            // Verify output file was created
            File outputFile = new File(options.outputFile);
            if (outputFile.exists()) {
                System.out.println();
                System.out.println("Output file created: " + options.outputFile);
                System.out.println("File size: " + outputFile.length() + " bytes");
            }
            
        } else {
            System.out.println("❌ Transformation failed");
            System.out.println();
            System.out.println("Error: " + result.getErrorMessage());
            
            // Provide helpful suggestions based on the mode
            System.out.println();
            System.out.println("Suggestions:");
            
            switch (options.mode) {
                case SPECIFIC:
                    System.out.println("  • Verify the transformation name is correct");
                    System.out.println("  • Use --list-transforms to see available options");
                    System.out.println("  • Check that the input code is compatible with this transformation");
                    break;
                    
                case RANDOM:
                    System.out.println("  • Try increasing --attempts (current: " + options.maxAttempts + ")");
                    System.out.println("  • Verify the input file contains transformable code");
                    System.out.println("  • Try a specific transformation instead");
                    break;
                    
                case GUIDED:
                    System.out.println("  • Verify bug line numbers are within the file bounds");
                    System.out.println("  • Try increasing --attempts (current: " + options.maxAttempts + ")");
                    System.out.println("  • Check that bug lines contain transformable code");
                    break;
            }
        }
        
        System.out.println("=".repeat(50));
    }
    
    // Helper classes
    private static class CommandLineOptions {
        TransformationMode mode;
        String transformName;
        String inputFile;
        String outputFile;
        int maxAttempts = 5;
        List<Integer> bugLines;
        boolean showHelp = false;
        boolean listTransforms = false;
    }
    
    private enum TransformationMode {
        SPECIFIC, RANDOM, GUIDED
    }
}