# Transformer Library

A standalone Java library for applying code transformations to Java source files, with support for both specific transformation methods and location-based strategies guided by external bug information.

## Overview

The Transformer library extracts and refactors the code transformation functionality from the Statfier project into a clean, standalone JAR library. It provides a simple API for applying Java code transformations using either specific transformation methods or intelligent location-based strategies that can be guided by bug information from external static analysis tools.

## Key Features

- **Standalone JAR**: No dependencies on external static analysis tools
- **Clean API**: Simple, intuitive interface for transformation operations
- **Bug Information Integration**: Guide transformations using external bug detection results
- **Multiple Strategies**: Random and guided location-based transformation strategies
- **Extensible**: Easy to add new transformations and location strategies
- **Robust Error Handling**: Comprehensive exception handling and validation
- **Performance Optimized**: Configurable caching and timeout settings

## Quick Start

### Installation

Add the Transformer JAR to your project:

**Maven:**
```xml
<dependency>
    <groupId>com.transformer</groupId>
    <artifactId>transformer</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'com.transformer:transformer:1.0.0'
```

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
    System.out.println("Applied: " + result.getAppliedTransforms());
} else {
    System.err.println("Failed: " + result.getErrorMessage());
}
```

### Bug Information Integration

```java
// Get bug information from your static analysis tool
List<Integer> bugLines = Arrays.asList(15, 23, 45);

// Apply guided transformation based on bug locations
TransformationResult result = transformer.applyGuidedTransform(
    "input.java", 
    "output.java", 
    5,  // max attempts
    true,  // has bugs
    bugLines
);
```

## Available Transformations

The library includes the following transformations:

- **AddBrackets**: Adds brackets to single-statement control structures
- **LoopConversion1**: Converts for loops to while loops
- **LoopConversion2**: Alternative loop conversion patterns
- **AddGlobalAssignment**: Adds global variable assignments
- **ConditionalBoundary**: Modifies conditional boundary conditions
- **ArithmeticOperatorReplacement**: Replaces arithmetic operators
- And more...

Use `transformer.getAvailableTransforms()` to get the complete list.

## Transformation Strategies

### 1. Specific Transformations
Apply a named transformation to your code:

```java
TransformationResult result = transformer.applyTransform(
    "AddBrackets", "input.java", "output.java"
);
```

### 2. Random Location Strategy
Randomly select transformation locations:

```java
TransformationResult result = transformer.applyRandomTransform(
    "input.java", "output.java", 10  // max attempts
);
```

### 3. Guided Location Strategy
Use bug information to guide transformation selection:

```java
List<Integer> bugLines = Arrays.asList(15, 25, 40);
TransformationResult result = transformer.applyGuidedTransform(
    "input.java", "output.java", 5, true, bugLines
);
```

## Bug Information Parameters

### Format Requirements

- **hasBugs** (boolean): `true` if bugs detected, `false` otherwise
- **bugLines** (List<Integer>): Line numbers where bugs were detected
  - Must be positive integers (1-based line numbers)
  - Required when `hasBugs` is `true` and using guided strategy
  - Should not exceed the actual file length

### Example Integration with PMD

```java
// Run PMD analysis
PMDConfiguration config = new PMDConfiguration();
config.setInputPaths("input.java");
config.setRuleSets("rulesets/java/quickstart.xml");

Report report = PMD.processFiles(config);
List<Integer> bugLines = report.getViolations().stream()
    .map(RuleViolation::getBeginLine)
    .collect(Collectors.toList());

// Use results for guided transformation
if (!bugLines.isEmpty()) {
    TransformationResult result = transformer.applyGuidedTransform(
        "input.java", "output.java", 5, true, bugLines
    );
}
```

## Configuration

Create a `transformer.properties` file for basic settings:

```properties
# Transformation Settings
transform.max.attempts=10
transform.preserve.formatting=true
transform.default.strategy=RANDOM_LOCATION

# AST Processing Settings
ast.parse.timeout=30000
ast.rewrite.timeout=30000

# Logging Settings
logging.level=INFO
logging.file=transformer.log
```

See [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md) for complete configuration options.

## Error Handling

The library provides comprehensive error handling:

```java
try {
    TransformationResult result = transformer.applyGuidedTransform(
        "input.java", "output.java", 5, true, bugLines
    );
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

## Documentation

- **[API Documentation](API_DOCUMENTATION.md)**: Complete API reference with examples
- **[Configuration Guide](CONFIGURATION_GUIDE.md)**: Configuration options and templates
- **[Usage Examples](USAGE_EXAMPLES.md)**: Real-world usage scenarios and integration examples
- **[Troubleshooting Guide](TROUBLESHOOTING_GUIDE.md)**: Common issues and solutions

## Requirements

- **Java 11** or higher
- **Maven 3.6+** (for building from source)

## Dependencies

The library has minimal dependencies:
- Eclipse JDT Core (for AST manipulation)
- Jackson (optional, for JSON processing)

## Building from Source

```bash
git clone <repository-url>
cd transformer
mvn clean package
```

This creates `target/transformer-1.0.0.jar` with all dependencies included.

## Integration Examples

### Spring Boot Integration

```java
@Service
public class CodeTransformationService {
    private final TransformerService transformer = new TransformerServiceImpl();
    
    public TransformationResult transformCode(String input, String output, 
                                            List<Integer> bugLines) {
        if (bugLines != null && !bugLines.isEmpty()) {
            return transformer.applyGuidedTransform(input, output, 5, true, bugLines);
        } else {
            return transformer.applyRandomTransform(input, output, 5);
        }
    }
}
```

### Command Line Usage

```java
public class TransformerCLI {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar transformer.jar <input> <output> <transform>");
            System.exit(1);
        }
        
        TransformerService transformer = new TransformerServiceImpl();
        TransformationResult result = transformer.applyTransform(args[2], args[0], args[1]);
        
        if (result.isSuccess()) {
            System.out.println("Success: " + result.getAppliedTransforms());
        } else {
            System.err.println("Failed: " + result.getErrorMessage());
            System.exit(1);
        }
    }
}
```

## Use Cases

### 1. Metamorphic Testing
Generate multiple versions of code to test static analysis tools:

```java
// Generate 5 different versions for metamorphic testing
for (int i = 0; i < 5; i++) {
    TransformationResult result = transformer.applyRandomTransform(
        "original.java", "version_" + i + ".java", 3
    );
}
```

### 2. Bug Localization Testing
Focus transformations on areas where bugs were detected:

```java
List<Integer> bugLines = getStaticAnalysisBugs("code.java");
TransformationResult result = transformer.applyGuidedTransform(
    "code.java", "transformed.java", 5, true, bugLines
);
```

### 3. Code Mutation Testing
Apply systematic transformations for testing purposes:

```java
List<String> transforms = transformer.getAvailableTransforms();
for (String transform : transforms) {
    TransformationResult result = transformer.applyTransform(
        transform, "original.java", "mutant_" + transform + ".java"
    );
}
```

## Performance Considerations

- **Caching**: Enable AST caching for better performance with multiple files
- **Timeouts**: Configure appropriate timeouts for large files
- **Memory**: Use appropriate JVM heap size for large-scale processing
- **Parallel Processing**: Process multiple files concurrently for better throughput

Example high-performance configuration:

```properties
performance.enable.ast.cache=true
performance.ast.cache.size=200
transform.preserve.formatting=false
ast.parse.timeout=15000
```

## Best Practices

1. **Validate Input**: Always check file existence and permissions before transformation
2. **Handle Errors**: Use comprehensive error handling for robust applications
3. **Validate Bug Information**: Ensure bug line numbers are valid and within file bounds
4. **Use Appropriate Strategies**: Choose guided strategy when bug information is available
5. **Monitor Performance**: Configure timeouts and caching based on your use case
6. **Test Transformations**: Validate transformed code compiles and behaves correctly

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues, questions, or contributions:

1. Check the [Troubleshooting Guide](TROUBLESHOOTING_GUIDE.md)
2. Review existing issues in the repository
3. Create a new issue with detailed information
4. Include code examples and error messages

## Changelog

### Version 1.0.0
- Initial release
- Core transformation functionality
- Bug information integration
- Location-based strategies
- Comprehensive documentation
- Configuration management
- Error handling and validation