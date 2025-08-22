# Project Structure

## Maven Standard Layout

```
transformer/
├── pom.xml                    # Maven build configuration
├── README.md                  # Main project documentation
├── API_DOCUMENTATION.md       # Complete API reference
├── USAGE_EXAMPLES.md          # Real-world usage scenarios
├── src/
│   ├── main/java/com/transformer/
│   │   ├── api/               # Public API interfaces and implementations
│   │   ├── core/              # Core transformation engine
│   │   ├── strategy/          # Location selection strategies
│   │   ├── transform/         # Individual transformation implementations
│   │   ├── util/              # Utility classes
│   │   └── Main.java          # Entry point
│   └── test/
│       ├── java/com/transformer/  # Unit tests
│       └── resources/
│           └── sample-java-files/ # Test fixtures
└── target/                    # Maven build output
```

## Package Organization

### `com.transformer.api`
- **TransformerService**: Main service interface
- **TransformerServiceImpl**: Primary implementation
- **TransformationResult**: Result wrapper with metadata
- **Exception classes**: BugInformationException, ParseException, etc.
- **ValidationUtils**: Input validation utilities

### `com.transformer.core`
- **TransformationEngine**: Core transformation orchestration
- **ASTProcessor**: Eclipse JDT AST manipulation
- **TransformRegistry**: Registry of available transformations
- **TypeWrapper**: AST node type utilities

### `com.transformer.strategy`
- **LocationStrategy**: Interface for node selection strategies
- **RandomLocationStrategy**: Random node selection
- **GuidedLocationStrategy**: Bug-information guided selection
- **BugInformationProcessor**: Bug data processing utilities

### `com.transformer.transform`
- **Transform**: Base interface for all transformations
- **30+ transformation classes**: AddBrackets, LoopConversion1, etc.
- Each transformation is self-contained and implements the Transform interface

### `com.transformer.util`
- **ASTUtil**: AST manipulation utilities
- **FileUtil**: File I/O operations
- **ErrorHandler**: Centralized error handling
- **LoopStatement**: Loop-specific utilities

## Naming Conventions

- **Classes**: PascalCase (e.g., `TransformerService`, `AddBrackets`)
- **Methods**: camelCase (e.g., `applyTransform`, `getAvailableTransforms`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_ATTEMPTS`, `DEFAULT_TIMEOUT`)
- **Packages**: lowercase with dots (e.g., `com.transformer.api`)
- **Test classes**: End with `Test` (e.g., `TransformerServiceTest`)

## File Organization Rules

- One public class per file
- File name matches the public class name
- Package-private classes can share files with related public classes
- Test files mirror the main source structure
- Configuration files go in `src/main/resources` or `src/test/resources`