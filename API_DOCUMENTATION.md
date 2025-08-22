# Transformer API Documentation

## Overview

The Transformer library provides a clean API for applying Java code transformations using either specific transformation methods or location-based strategies guided by external bug information. This document provides comprehensive usage examples and parameter documentation.

## Quick Start

### Basic Usage

```java
import com.transformer.api.TransformerService;
import com.transformer.api.TransformerServiceImpl;
import com.transformer.api.TransformationResult;

// Create transformer service
TransformerService transformer = new TransformerServiceImpl();

// Apply a specific transformation
TransformationResult result = transformer.applyTransform(
    "AddBrackets", 
    "input.java", 
    "output.java"
);

if (result.isSuccess()) {
    System.out.println("Transformation applied successfully");
    System.out.println("Applied transforms: " + result.getAppliedTransforms());
} else {
    System.out.println("Transformation failed: " + result.getErrorMessage());
}
```

### Location-Based Transformations

```java
// Random location strategy
TransformationResult randomResult = transformer.applyRandomTransform(
    "input.java", 
    "output.java", 
    5  // max attempts
);

// Guided location strategy with bug information
TransformationResult guidedResult = transformer.applyGuidedTransform(
    "input.java", 
    "output.java", 
    3,  // max attempts
    true,  // has bugs
    Arrays.asList(15, 23, 45)  // bug line numbers
);
```

## API Reference

### TransformerService Interface

The main interface for all transformation operations.

#### Methods

##### applyTransform
```java
TransformationResult applyTransform(String transformName, String inputPath, String outputPath)
```

Applies a specific transformation to a Java file.

**Parameters:**
- `transformName` (String): Name of the transformation to apply
- `inputPath` (String): Path to the input Java file
- `outputPath` (String): Path where the transformed file will be written

**Returns:** `TransformationResult` containing the operation result

**Example:**
```java
TransformationResult result = transformer.applyTransform(
    "LoopConversion1", 
    "src/main/java/Example.java", 
    "transformed/Example.java"
);
```

##### applyRandomTransform
```java
TransformationResult applyRandomTransform(String inputPath, String outputPath, int maxAttempts)
```

Applies transformations using random location selection.

**Parameters:**
- `inputPath` (String): Path to the input Java file
- `outputPath` (String): Path where the transformed file will be written
- `maxAttempts` (int): Maximum number of transformation attempts

**Returns:** `TransformationResult` containing the operation result

**Example:**
```java
TransformationResult result = transformer.applyRandomTransform(
    "src/main/java/Example.java", 
    "transformed/Example.java", 
    10
);
```

##### applyGuidedTransform
```java
TransformationResult applyGuidedTransform(String inputPath, String outputPath, int maxAttempts, 
                                        boolean hasBugs, List<Integer> bugLines)
```

Applies transformations using bug information to guide location selection.

**Parameters:**
- `inputPath` (String): Path to the input Java file
- `outputPath` (String): Path where the transformed file will be written
- `maxAttempts` (int): Maximum number of transformation attempts
- `hasBugs` (boolean): Whether the code has detected bugs
- `bugLines` (List<Integer>): List of line numbers where bugs were detected

**Returns:** `TransformationResult` containing the operation result

**Example:**
```java
List<Integer> bugLines = Arrays.asList(12, 25, 38);
TransformationResult result = transformer.applyGuidedTransform(
    "src/main/java/Example.java", 
    "transformed/Example.java", 
    5,
    true,
    bugLines
);
```

##### getAvailableTransforms
```java
List<String> getAvailableTransforms()
```

Returns a list of all available transformation names.

**Returns:** List of transformation names

**Example:**
```java
List<String> transforms = transformer.getAvailableTransforms();
System.out.println("Available transforms: " + transforms);
```

##### validateConfiguration
```java
boolean validateConfiguration()
```

Validates the current configuration settings.

**Returns:** `true` if configuration is valid, `false` otherwise

**Example:**
```java
if (!transformer.validateConfiguration()) {
    System.err.println("Configuration validation failed");
}
```

### TransformationResult Class

Contains the result of a transformation operation.

#### Properties

- `success` (boolean): Whether the transformation was successful
- `transformedCode` (String): The transformed Java code (if successful)
- `appliedTransforms` (List<String>): List of transformation names that were applied
- `errorMessage` (String): Error message (if transformation failed)
- `metadata` (Map<String, Object>): Additional metadata about the transformation

#### Methods

```java
boolean isSuccess()
String getTransformedCode()
List<String> getAppliedTransforms()
String getErrorMessage()
Map<String, Object> getMetadata()
```

**Example:**
```java
TransformationResult result = transformer.applyTransform("AddBrackets", "input.java", "output.java");

if (result.isSuccess()) {
    System.out.println("Success! Applied: " + result.getAppliedTransforms());
    System.out.println("Metadata: " + result.getMetadata());
} else {
    System.err.println("Failed: " + result.getErrorMessage());
}
```

## Bug Information Parameters

### Overview

Bug information parameters allow you to guide transformations to focus on specific areas of code where bugs have been detected by external static analysis tools.

### Parameter Format

#### hasBugs (boolean)
- `true`: Indicates that bugs have been detected in the code
- `false`: Indicates no bugs detected (guided strategy will behave like random)

#### bugLines (List<Integer>)
- List of line numbers where bugs were detected
- Line numbers are 1-based (first line is line 1)
- Must be provided when `hasBugs` is `true` and using guided strategy
- Can be empty list when `hasBugs` is `false`

### Usage Examples

#### Valid Bug Information
```java
// Single bug line
List<Integer> singleBug = Arrays.asList(15);
transformer.applyGuidedTransform("input.java", "output.java", 3, true, singleBug);

// Multiple bug lines
List<Integer> multipleBugs = Arrays.asList(10, 25, 40, 55);
transformer.applyGuidedTransform("input.java", "output.java", 5, true, multipleBugs);

// No bugs detected
List<Integer> noBugs = new ArrayList<>();
transformer.applyGuidedTransform("input.java", "output.java", 3, false, noBugs);
```

#### Invalid Bug Information
```java
// This will throw BugInformationException
transformer.applyGuidedTransform("input.java", "output.java", 3, true, null);

// This will throw BugInformationException  
transformer.applyGuidedTransform("input.java", "output.java", 3, true, new ArrayList<>());

// This will throw BugInformationException (negative line numbers)
List<Integer> invalidLines = Arrays.asList(-1, 0, 15);
transformer.applyGuidedTransform("input.java", "output.java", 3, true, invalidLines);
```

### How Bug Information Affects Transformation

When using guided location strategy with bug information:

1. **Node Selection**: The system prioritizes AST nodes near the specified bug lines
2. **Search Radius**: Nodes within a 3-line radius of bug lines are considered
3. **Transformation Priority**: Transformations that can be applied to nodes near bug lines are preferred
4. **Fallback Behavior**: If no suitable nodes are found near bug lines, the system falls back to random selection

## Available Transformations

The following transformations are available in the library:

### AddBrackets
Adds brackets to single-statement control structures (if, while, for statements).

**Example:**
```java
// Before
if (condition)
    statement();

// After  
if (condition) {
    statement();
}
```

### LoopConversion1
Converts for loops to while loops where applicable.

**Example:**
```java
// Before
for (int i = 0; i < 10; i++) {
    System.out.println(i);
}

// After
int i = 0;
while (i < 10) {
    System.out.println(i);
    i++;
}
```

### AddGlobalAssignment
Adds global variable assignments in appropriate contexts.

### Additional Transformations
- `LoopConversion2`: Alternative loop conversion patterns
- `ConditionalBoundary`: Modifies conditional boundary conditions
- `ArithmeticOperatorReplacement`: Replaces arithmetic operators
- And more...

Use `getAvailableTransforms()` to get the complete list of available transformations.

## Error Handling

### Exception Types

#### TransformerException
Base exception for all transformer operations.

#### BugInformationException
Thrown when bug information parameters are invalid.

**Common causes:**
- `hasBugs` is `true` but `bugLines` is null or empty
- Bug line numbers are negative or zero
- Bug line numbers exceed file length

#### ConfigurationException
Thrown when configuration is invalid or missing.

#### ParseException
Thrown when Java source code cannot be parsed.

#### IOTransformerException
Thrown when file I/O operations fail.

### Error Handling Examples

```java
try {
    TransformationResult result = transformer.applyGuidedTransform(
        "input.java", "output.java", 3, true, Arrays.asList(15, 25)
    );
    
    if (!result.isSuccess()) {
        System.err.println("Transformation failed: " + result.getErrorMessage());
    }
    
} catch (BugInformationException e) {
    System.err.println("Invalid bug information: " + e.getMessage());
} catch (ParseException e) {
    System.err.println("Failed to parse Java file: " + e.getMessage());
} catch (IOTransformerException e) {
    System.err.println("File I/O error: " + e.getMessage());
} catch (TransformerException e) {
    System.err.println("Transformation error: " + e.getMessage());
}
```

## Best Practices

### 1. Validate Input Parameters
```java
// Always validate file paths exist
File inputFile = new File("input.java");
if (!inputFile.exists()) {
    throw new IllegalArgumentException("Input file does not exist");
}

// Validate bug line numbers are reasonable
List<Integer> bugLines = Arrays.asList(15, 25);
if (bugLines.stream().anyMatch(line -> line <= 0)) {
    throw new IllegalArgumentException("Bug line numbers must be positive");
}
```

### 2. Handle Transformation Results Properly
```java
TransformationResult result = transformer.applyTransform("AddBrackets", "input.java", "output.java");

if (result.isSuccess()) {
    // Check what transformations were actually applied
    if (result.getAppliedTransforms().isEmpty()) {
        System.out.println("No transformations were applicable");
    } else {
        System.out.println("Applied: " + result.getAppliedTransforms());
    }
} else {
    // Log the error for debugging
    System.err.println("Transformation failed: " + result.getErrorMessage());
}
```

### 3. Use Appropriate Max Attempts
```java
// For testing purposes, use higher attempts
int testingAttempts = 10;

// For production use, use lower attempts to avoid performance issues
int productionAttempts = 3;

TransformationResult result = transformer.applyRandomTransform(
    "input.java", "output.java", productionAttempts
);
```

### 4. Leverage Bug Information Effectively
```java
// Get bug information from your static analysis tool
List<Integer> bugLines = getStaticAnalysisBugLines("input.java");

if (!bugLines.isEmpty()) {
    // Use guided strategy when bugs are detected
    TransformationResult result = transformer.applyGuidedTransform(
        "input.java", "output.java", 5, true, bugLines
    );
} else {
    // Use random strategy when no bugs detected
    TransformationResult result = transformer.applyRandomTransform(
        "input.java", "output.java", 5
    );
}
```

## Integration Examples

### Maven Integration

Add the Transformer JAR to your Maven project:

```xml
<dependency>
    <groupId>com.transformer</groupId>
    <artifactId>transformer</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Spring Boot Integration

```java
@Service
public class CodeTransformationService {
    
    private final TransformerService transformer;
    
    public CodeTransformationService() {
        this.transformer = new TransformerServiceImpl();
    }
    
    public TransformationResult transformCode(String inputPath, String outputPath, 
                                            List<Integer> bugLines) {
        try {
            if (bugLines != null && !bugLines.isEmpty()) {
                return transformer.applyGuidedTransform(inputPath, outputPath, 5, true, bugLines);
            } else {
                return transformer.applyRandomTransform(inputPath, outputPath, 5);
            }
        } catch (TransformerException e) {
            // Handle exception appropriately
            throw new RuntimeException("Code transformation failed", e);
        }
    }
}
```

### Command Line Integration

```java
public class TransformerCLI {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java TransformerCLI <input> <output> <transform> [bugLines...]");
            System.exit(1);
        }
        
        String input = args[0];
        String output = args[1];
        String transformName = args[2];
        
        TransformerService transformer = new TransformerServiceImpl();
        
        try {
            TransformationResult result;
            
            if (args.length > 3) {
                // Parse bug lines from command line
                List<Integer> bugLines = new ArrayList<>();
                for (int i = 3; i < args.length; i++) {
                    bugLines.add(Integer.parseInt(args[i]));
                }
                
                result = transformer.applyGuidedTransform(input, output, 5, true, bugLines);
            } else {
                result = transformer.applyTransform(transformName, input, output);
            }
            
            if (result.isSuccess()) {
                System.out.println("Transformation successful");
                System.out.println("Applied: " + result.getAppliedTransforms());
            } else {
                System.err.println("Transformation failed: " + result.getErrorMessage());
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
```