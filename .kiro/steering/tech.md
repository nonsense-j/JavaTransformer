# Technology Stack

## Build System & Dependencies

- **Build Tool**: Maven 3.6+
- **Java Version**: Java 11 (source and target)
- **Packaging**: JAR with dependencies using maven-assembly-plugin

## Core Dependencies

- **Eclipse JDT Core** (3.33.0): AST manipulation and Java source code parsing
- **Jackson Databind** (2.15.1): JSON processing for configuration and data exchange
- **ZT-Exec** (1.12): External process execution for static analyzer integration
- **JUnit** (4.13.2): Unit testing framework

## Common Commands

### Building
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package JAR with dependencies
mvn clean package

# Run main class
mvn exec:java -Dexec.mainClass="com.transformer.config.ConfigurationExample"
```

### Development
```bash
# Generate sources JAR
mvn source:jar

# Generate Javadoc
mvn javadoc:javadoc

# Run specific test class
mvn test -Dtest=TransformerServiceTest

# Skip tests during build
mvn package -DskipTests
```

## Key Technical Patterns

- **Service Layer**: Clean API through TransformerService interface
- **Strategy Pattern**: LocationStrategy implementations for different transformation approaches
- **Factory Pattern**: TransformRegistry for managing available transformations
- **Exception Hierarchy**: Specific exceptions for different error types (BugInformationException, ParseException, etc.)
- **AST Processing**: Eclipse JDT for parsing and rewriting Java source code

## Configuration

- Properties-based configuration via `transformer.properties`
- Configurable timeouts, caching, and transformation settings
- Support for external static analyzer integration parameters