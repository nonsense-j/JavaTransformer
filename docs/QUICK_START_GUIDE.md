# Quick Start Guide: JAR Compilation and Usage

## Overview

This guide provides streamlined instructions for building the Transformer library JAR with all dependencies and using it both programmatically and via command line.

## Prerequisites

- **Java 11** or higher
- **Maven 3.6+**
- Git (for cloning the repository)

## Building the JAR

### 1. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd transformer

# Build JAR with all dependencies
mvn clean package
```

This creates two JAR files in the `target/` directory:
- `transformer-1.0.0.jar` - Basic JAR without dependencies
- `transformer-1.0.0-jar-with-dependencies.jar` - Complete JAR with all dependencies (recommended)

### 2. Verify Build

```bash
# Check that the JAR was created successfully
ls -la target/*.jar

# Verify JAR contents
jar tf target/transformer-1.0.0-jar-with-dependencies.jar | head -10
```

### 3. Quick Test

```bash
# Test the JAR by running a simple command
java -cp target/transformer-1.0.0-jar-with-dependencies.jar com.transformer.api.TransformerServiceImpl
```

## Command Line Usage

### Basic Transformation

```bash
# Apply a specific transformation
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main \
  --transform AddBrackets \
  --input src/main/java/Example.java \
  --output transformed/Example.java
```

### Random Transformation

```bash
# Apply random transformation with multiple attempts
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main \
  --random \
  --input src/main/java/Example.java \
  --output transformed/Example.java \
  --attempts 5
```

### Guided Transformation with Bug Information

```bash
# Apply guided transformation using bug line numbers
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main \
  --guided \
  --input src/main/java/Example.java \
  --output transformed/Example.java \
  --bug-lines 15,23,45 \
  --attempts 3
```

### List Available Transformations

```bash
# Show all available transformations
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main --list-transforms
```

### Help and Usage

```bash
# Display usage information
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main --help
```

## Programmatic API Usage

### Basic Setup

```java
import com.transformer.api.TransformerService;
import com.transformer.api.TransformerServiceImpl;
import com.transformer.api.TransformationResult;

// Create transformer service instance
TransformerService transformer = new TransformerServiceImpl();
```

### Apply Specific Transformation

```java
// Apply a named transformation
TransformationResult result = transformer.applyTransform(
    "AddBrackets",                    // transformation name
    "src/main/java/Example.java",     // input file
    "transformed/Example.java"        // output file
);

// Check results
if (result.isSuccess()) {
    System.out.println("Success! Applied: " + result.getAppliedTransforms());
} else {
    System.err.println("Failed: " + result.getErrorMessage());
}
```

### Apply Random Transformation

```java
// Apply random transformation with retry attempts
TransformationResult result = transformer.applyRandomTransform(
    "src/main/java/Example.java",     // input file
    "transformed/Example.java",       // output file
    5                                 // max attempts
);

if (result.isSuccess()) {
    System.out.println("Random transformation applied: " + result.getAppliedTransforms());
    System.out.println("Attempts used: " + result.getMetadata().get("attempts"));
}
```

### Apply Guided Transformation

```java
import java.util.Arrays;
import java.util.List;

// Define bug line numbers (from static analysis tools)
List<Integer> bugLines = Arrays.asList(15, 23, 45);

// Apply guided transformation
TransformationResult result = transformer.applyGuidedTransform(
    "src/main/java/Example.java",     // input file
    "transformed/Example.java",       // output file
    3,                                // max attempts
    true,                             // has bugs
    bugLines                          // bug line numbers
);

if (result.isSuccess()) {
    System.out.println("Guided transformation successful!");
    System.out.println("Focused on lines: " + bugLines);
    System.out.println("Applied: " + result.getAppliedTransforms());
}
```

### Get Available Transformations

```java
// List all available transformations
List<String> transforms = transformer.getAvailableTransforms();
System.out.println("Available transformations:");
transforms.forEach(System.out::println);
```

### Error Handling

```java
import com.transformer.api.exception.*;

try {
    TransformationResult result = transformer.applyTransform(
        "AddBrackets", "input.java", "output.java"
    );
    
    if (result.isSuccess()) {
        System.out.println("Success: " + result.getAppliedTransforms());
    } else {
        System.err.println("Transformation failed: " + result.getErrorMessage());
    }
    
} catch (BugInformationException e) {
    System.err.println("Invalid bug information: " + e.getMessage());
} catch (ParseException e) {
    System.err.println("Failed to parse Java file: " + e.getMessage());
} catch (IOTransformerException e) {
    System.err.println("File I/O error: " + e.getMessage());
} catch (TransformerException e) {
    System.err.println("General transformation error: " + e.getMessage());
}
```

## Integration Examples

### Maven Project Integration

Add the JAR to your Maven project:

```xml
<dependency>
    <groupId>com.transformer</groupId>
    <artifactId>transformer</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/transformer-1.0.0-jar-with-dependencies.jar</systemPath>
</dependency>
```

### Gradle Project Integration

```gradle
dependencies {
    implementation files('lib/transformer-1.0.0-jar-with-dependencies.jar')
}
```

### Spring Boot Integration

```java
@Service
public class CodeTransformationService {
    
    private final TransformerService transformer = new TransformerServiceImpl();
    
    public TransformationResult transformCode(String inputFile, String outputFile, 
                                            List<Integer> bugLines) {
        if (bugLines != null && !bugLines.isEmpty()) {
            return transformer.applyGuidedTransform(inputFile, outputFile, 5, true, bugLines);
        } else {
            return transformer.applyRandomTransform(inputFile, outputFile, 5);
        }
    }
}
```

## Troubleshooting

### Build Issues

**Problem**: `mvn clean package` fails with dependency errors
```
Solution: Ensure you have Java 11+ and Maven 3.6+
Check: java -version && mvn -version
```

**Problem**: Out of memory during build
```
Solution: Increase Maven memory
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"
mvn clean package
```

**Problem**: Tests fail during build
```
Solution: Skip tests if needed for JAR creation
mvn clean package -DskipTests
```

### Runtime Issues

**Problem**: `ClassNotFoundException` when running JAR
```
Solution: Use the jar-with-dependencies version
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main
```

**Problem**: `NoSuchMethodError` or compatibility issues
```
Solution: Verify Java version compatibility
java -version  # Should be Java 11+
```

**Problem**: File not found errors
```
Solution: Use absolute paths or verify working directory
java -cp transformer.jar com.transformer.Main --input /full/path/to/input.java
```

### Transformation Issues

**Problem**: No transformations applied (empty result)
```
Solution: 
1. Verify input file is valid Java code
2. Increase max attempts for random/guided strategies
3. Try different transformation names
4. Check file permissions
```

**Problem**: Parse errors on valid Java files
```
Solution:
1. Ensure file uses Java 11 compatible syntax
2. Check file encoding (should be UTF-8)
3. Verify file is not corrupted
```

**Problem**: Bug information errors
```
Solution:
1. Ensure bug line numbers are positive integers
2. Verify line numbers don't exceed file length
3. Use 1-based line numbering (not 0-based)
```

### Performance Issues

**Problem**: Slow transformation performance
```
Solution:
1. Reduce max attempts for random/guided strategies
2. Use specific transformations when possible
3. Process files in smaller batches
4. Increase JVM heap size: java -Xmx2g -cp transformer.jar
```

## Validation Instructions

### Verify JAR Build

```bash
# 1. Check JAR file exists and has reasonable size
ls -lh target/transformer-1.0.0-jar-with-dependencies.jar

# 2. Verify JAR contains main classes
jar tf target/transformer-1.0.0-jar-with-dependencies.jar | grep "com/transformer/api/TransformerService"

# 3. Test basic functionality
echo 'public class Test { public void method() { int x = 1; } }' > test.java
java -cp target/transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main \
  --transform AddBrackets --input test.java --output test_transformed.java
```

### Verify Transformations

```bash
# 1. List available transformations
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main --list-transforms

# 2. Test specific transformation
java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main \
  --transform AddBrackets --input test.java --output result.java

# 3. Verify output file was created and is valid Java
javac result.java  # Should compile without errors
```

### Verify API Usage

Create a test program:

```java
// TestTransformer.java
import com.transformer.api.*;
import java.util.Arrays;

public class TestTransformer {
    public static void main(String[] args) {
        TransformerService transformer = new TransformerServiceImpl();
        
        // Test 1: List transformations
        System.out.println("Available transformations: " + 
                          transformer.getAvailableTransforms().size());
        
        // Test 2: Apply transformation
        TransformationResult result = transformer.applyTransform(
            "AddBrackets", "test.java", "output.java"
        );
        
        System.out.println("Transformation success: " + result.isSuccess());
        
        // Test 3: Guided transformation
        result = transformer.applyGuidedTransform(
            "test.java", "guided_output.java", 3, true, Arrays.asList(1, 2)
        );
        
        System.out.println("Guided transformation success: " + result.isSuccess());
    }
}
```

Compile and run:

```bash
# Compile test program
javac -cp transformer-1.0.0-jar-with-dependencies.jar TestTransformer.java

# Run test program
java -cp .:transformer-1.0.0-jar-with-dependencies.jar TestTransformer
```

## Advanced Usage

### Batch Processing Script

Create a shell script for batch processing:

```bash
#!/bin/bash
# batch_transform.sh

INPUT_DIR="$1"
OUTPUT_DIR="$2"
TRANSFORM="$3"

if [ $# -ne 3 ]; then
    echo "Usage: $0 <input_dir> <output_dir> <transform_name>"
    exit 1
fi

mkdir -p "$OUTPUT_DIR"

for java_file in "$INPUT_DIR"/*.java; do
    if [ -f "$java_file" ]; then
        filename=$(basename "$java_file")
        echo "Processing: $filename"
        
        java -cp transformer-1.0.0-jar-with-dependencies.jar com.transformer.Main \
            --transform "$TRANSFORM" \
            --input "$java_file" \
            --output "$OUTPUT_DIR/$filename"
    fi
done

echo "Batch processing completed"
```

Usage:
```bash
chmod +x batch_transform.sh
./batch_transform.sh src/main/java transformed AddBrackets
```

### Configuration File Usage

Create `transformer.properties`:

```properties
# Default transformation settings
transform.max.attempts=10
transform.preserve.formatting=true
transform.default.strategy=RANDOM_LOCATION

# Performance settings
ast.parse.timeout=30000
ast.rewrite.timeout=30000

# Logging
logging.level=INFO
```

Use with:
```bash
java -Dtransformer.config=transformer.properties \
     -cp transformer-1.0.0-jar-with-dependencies.jar \
     com.transformer.Main --input test.java --output result.java
```

## Next Steps

After successfully building and testing the JAR:

1. **Integration**: Integrate with your static analysis pipeline
2. **Automation**: Create scripts for batch processing
3. **Testing**: Set up metamorphic testing workflows
4. **Monitoring**: Add logging and monitoring for production use

For more detailed examples and advanced usage patterns, see:
- [API Documentation](../API_DOCUMENTATION.md)
- [Usage Examples](../USAGE_EXAMPLES.md)
- [Configuration Guide](CONFIGURATION_GUIDE.md)