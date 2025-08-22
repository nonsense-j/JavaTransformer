# Design Document

## Overview

The TargetStrategy feature extends the existing location strategy system in the Transformer library by adding a new strategy that selects AST nodes based on specific line numbers provided by the user. This strategy provides deterministic, precise control over transformation locations, complementing the existing RandomLocationStrategy and GuidedLocationStrategy.

**This implementation includes a refactoring of the entire strategy system** to use optimal method signatures for each strategy type, eliminating the current suboptimal interface design that forces all strategies to use the same generic parameters.

## Architecture

### High-Level Design

The TargetStrategy implementation consists of three main components:

1. **TargetLocationStrategy** - Core strategy implementation that selects nodes based on target line numbers
2. **TransformerService API Extension** - New `applyTargetTransform` method for easy access to target-based transformations
3. **LocationStrategyFactory Integration** - Registration of the new strategy in the factory system

### Component Integration

```
TransformerService.applyTargetTransform()
    ↓
TransformerServiceImpl.applyTargetTransform()
    ↓
TransformationEngine.executeTargetTransformation()
    ↓
TargetLocationStrategy.selectCandidateNodes(wrapper, targetLines)  // Optimal signature
    ↓
TypeWrapper.getAllNodes() + line number filtering
```

### Strategy Method Signatures Refactoring

**Current Problem**: All strategies are forced to implement the same generic interface with parameters they don't need.

**Solution**: Refactor each strategy to use its optimal method signature:
- **RandomLocationStrategy**: `selectCandidateNodes(TypeWrapper wrapper, int randomNodeCnt)`
- **GuidedLocationStrategy**: `selectCandidateNodes(TypeWrapper wrapper, BugInformation bugInfo)`
- **TargetLocationStrategy**: `selectCandidateNodes(TypeWrapper wrapper, List<Integer> targetLines)`

**Benefits**:
- Cleaner code with no unused parameters
- Better performance (no parameter passing overhead)
- More intuitive API for each strategy type
- Easier to understand and maintain

## Components and Interfaces

### LocationStrategy Interface Refactoring

**Location:** `src/main/java/com/transformer/strategy/LocationStrategy.java`

**New Interface Design:**
```java
public interface LocationStrategy {
    String getStrategyName();
    // Remove generic selectCandidateNodes methods - each strategy implements its own optimal signature
}
```

### TargetLocationStrategy

**Location:** `src/main/java/com/transformer/strategy/TargetLocationStrategy.java`

**Responsibilities:**
- Implement LocationStrategy interface with optimal method signature
- Filter AST nodes based on target line numbers
- Validate target line parameters
- Provide strategy identification

**Key Methods:**
```java
public class TargetLocationStrategy implements LocationStrategy {
    private final List<Integer> targetLines;
    
    public TargetLocationStrategy(List<Integer> targetLines);
    
    // Strategy-specific method with optimal signature
    public List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, List<Integer> targetLines);
    
    public String getStrategyName(); // Returns "TARGET_LOCATION"
    public void validateTargetLines(List<Integer> targetLines); // Validates targetLines parameter
}
```

### RandomLocationStrategy Refactoring

**Location:** `src/main/java/com/transformer/strategy/RandomLocationStrategy.java`

**Refactored Methods:**
```java
public class RandomLocationStrategy implements LocationStrategy {
    // Keep existing constructor and fields
    
    // Optimal method signature (remove unused parameters)
    public List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, int randomNodeCnt);
    
    public String getStrategyName(); // Returns "RANDOM_LOCATION"
}
```

### GuidedLocationStrategy Refactoring

**Location:** `src/main/java/com/transformer/strategy/GuidedLocationStrategy.java`

**Refactored Methods:**
```java
public class GuidedLocationStrategy implements LocationStrategy {
    // Keep existing constructor and fields
    
    // Optimal method signature (remove unused parameters)
    public List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, BugInformation bugInfo);
    
    public String getStrategyName(); // Returns "GUIDED_LOCATION"
    public void validateBugInformation(BugInformation bugInfo); // Validates bugInfo parameter
}
```

**Node Selection Logic:**
1. Get all nodes from `wrapper.getAllNodes()`
2. For each node, calculate line number using `wrapper.getCompilationUnit().getLineNumber(node.getStartPosition())`
3. Include node if its line number exists in the targetLines list
4. Return all matching nodes (no randomization or complex filtering)

### TransformerService API Extension

**Location:** `src/main/java/com/transformer/api/TransformerService.java`

**New Method:**
```java
/**
 * Apply target transformation strategy using specific line numbers.
 * 
 * @param inputPath Path to input Java file
 * @param outputDir Directory where mutant files will be saved
 * @param targetLines List of line numbers to target for transformation
 * @param transformName Specific transform to apply (null/empty = try all transforms)
 * @return TransformationResult with all generated mutants
 */
TransformationResult applyTargetTransform(String inputPath, String outputDir, List<Integer> targetLines, String transformName);
```

**Default Method Overloads:**
```java
// Apply target transformation with all transforms
default TransformationResult applyTargetTransform(String inputPath, String outputDir, List<Integer> targetLines) {
    return applyTargetTransform(inputPath, outputDir, targetLines, null);
}
```

### TransformerServiceImpl Extension

**Location:** `src/main/java/com/transformer/api/TransformerServiceImpl.java`

**Implementation:**
```java
@Override
public TransformationResult applyTargetTransform(String inputPath, String outputDir, List<Integer> targetLines, String transformName) {
    // Parameter validation
    // Delegate to TransformationEngine.executeTargetTransformation()
}
```

### TransformationEngine Refactoring

**Location:** `src/main/java/com/transformer/core/TransformationEngine.java`

**Refactored Methods:**
```java
// Update existing methods to use optimal strategy signatures
public TransformationResult executeRandomTransformation(String inputPath, String outputDir, String transformName, int randomCnt) {
    RandomLocationStrategy strategy = new RandomLocationStrategy();
    List<ASTNode> candidateNodes = strategy.selectCandidateNodes(wrapper, randomCnt);
    // Process candidateNodes
}

public TransformationResult executeGuidedTransformation(String inputPath, String outputDir, BugInformation bugInfo, String transformName) {
    GuidedLocationStrategy strategy = new GuidedLocationStrategy();
    List<ASTNode> candidateNodes = strategy.selectCandidateNodes(wrapper, bugInfo);
    // Process candidateNodes
}

// New method for target transformation
public TransformationResult executeTargetTransformation(String inputPath, String outputDir, List<Integer> targetLines, String transformName) {
    TargetLocationStrategy strategy = new TargetLocationStrategy(targetLines);
    List<ASTNode> candidateNodes = strategy.selectCandidateNodes(wrapper, targetLines);
    // Process candidateNodes
}
```

### LocationStrategyFactory Integration

**Location:** `src/main/java/com/transformer/strategy/LocationStrategyFactory.java`

**Registration:**
```java
static {
    // Existing registrations
    registerStrategy("RANDOM_LOCATION", () -> new RandomLocationStrategy());
    registerStrategy("GUIDED_LOCATION", () -> new GuidedLocationStrategy());
    // New registration - note: requires targetLines parameter, so factory method is preferred
    registerStrategy("TARGET_LOCATION", () -> new TargetLocationStrategy(new ArrayList<>()));
}

// New factory method with proper parameter signature
public static TargetLocationStrategy createTargetStrategy(List<Integer> targetLines) {
    return new TargetLocationStrategy(targetLines);
}
```

**Strategy-Specific Factory Methods:**
Following the pattern of different parameter requirements:
- `createRandomStrategy(long seed)` - for RandomLocationStrategy with seed
- `createGuidedStrategy()` - for GuidedLocationStrategy (no additional params)
- `createTargetStrategy(List<Integer> targetLines)` - for TargetLocationStrategy with target lines

## Data Models

### TargetLocationStrategy State

```java
public class TargetLocationStrategy implements LocationStrategy {
    private final List<Integer> targetLines;  // Immutable list of target line numbers
    
    // Constructor validates and creates defensive copy
    public TargetLocationStrategy(List<Integer> targetLines) {
        this.targetLines = validateAndCopyTargetLines(targetLines);
    }
}
```

### Parameter Validation

**Target Lines Validation:**
- Must not be null
- Must not contain negative numbers
- Empty list is allowed (returns empty candidate list)
- Line numbers beyond file bounds are ignored (no error)

**Integration with Existing Validation:**
- Reuse existing parameter validation patterns from TransformerServiceImpl
- Follow same error handling approach as other strategies

## Error Handling

### Validation Errors

**IllegalArgumentException Cases:**
- `targetLines` parameter is null
- `targetLines` contains negative line numbers
- Standard parameter validation (inputPath, outputDir) follows existing patterns

**Graceful Handling:**
- Line numbers beyond file bounds are silently ignored
- Empty target lines list returns empty candidate list
- No matching nodes returns empty candidate list

### Error Response Format

Follow existing TransformationResult error pattern:
```java
return TransformationResult.builder()
    .success(false)
    .addErrorMessage("Target lines cannot be null")
    .build();
```

## Testing Strategy

### Unit Tests

**TargetLocationStrategyTest:**
- Test node selection with various target line combinations
- Test validation of target line parameters
- Test edge cases (empty lists, out-of-bounds lines)
- Test integration with TypeWrapper and AST parsing

**TransformerServiceImplTest:**
- Test new applyTargetTransform method
- Test parameter validation
- Test integration with TransformationEngine
- Test error handling scenarios

**LocationStrategyFactoryTest:**
- Test registration of TARGET_LOCATION strategy
- Test createTargetStrategy factory method
- Test strategy name recognition

### Integration Tests

**TransformerServiceIntegrationTest:**
- Test end-to-end target transformation workflow
- Test with real Java files and various target line scenarios
- Test interaction with different transform types
- Test output file generation and mutant creation

### Test Data Requirements

**Sample Java Files:**
- Simple class with methods on known line numbers
- Complex class with nested structures
- Files with various AST node types on target lines

**Test Scenarios:**
- Single target line with one node
- Multiple target lines with multiple nodes
- Target lines with no matching nodes
- Target lines beyond file bounds
- Empty target lines list

## Implementation Notes

### Strategy System Refactoring Rationale

**Current Problems:**
1. All strategies forced to implement generic interface with unused parameters
2. `availableTransforms` parameter not needed for node selection
3. Poor separation of concerns between node selection and transformation application
4. Confusing API with parameters that are ignored

**Refactoring Solution:**
1. **Simplified Interface**: LocationStrategy only requires `getStrategyName()`
2. **Optimal Signatures**: Each strategy implements its own optimal `selectCandidateNodes` method
3. **Clean Separation**: Node selection logic separated from transformation application
4. **Type Safety**: Each strategy validates only the parameters it actually uses

**Strategy-Specific Signatures:**
- **GuidedLocationStrategy**: `selectCandidateNodes(TypeWrapper wrapper, BugInformation bugInfo)`
- **RandomLocationStrategy**: `selectCandidateNodes(TypeWrapper wrapper, int randomNodeCnt)`  
- **TargetLocationStrategy**: `selectCandidateNodes(TypeWrapper wrapper, List<Integer> targetLines)`

### Line Number Calculation

Use existing pattern from GuidedLocationStrategy:
```java
int row = wrapper.getCompilationUnit().getLineNumber(node.getStartPosition());
```

### Node Selection Approach

Unlike RandomLocationStrategy (which limits to maxNodes) and GuidedLocationStrategy (which performs complex analysis), TargetLocationStrategy uses a simple approach:
1. Get all nodes from TypeWrapper
2. Filter by line number match
3. Return all matching nodes (no artificial limits)

### Strategy Name Convention

Follow existing naming pattern:
- RandomLocationStrategy → "RANDOM_LOCATION"
- GuidedLocationStrategy → "GUIDED_LOCATION"  
- TargetLocationStrategy → "TARGET_LOCATION"

### Immutability Design

TargetLocationStrategy should be immutable after construction:
- targetLines field is final
- Constructor creates defensive copy of input list
- No methods modify internal state

This design ensures thread safety and prevents accidental modification of strategy parameters during transformation execution.

### Benefits of Refactoring

1. **Performance**: No unused parameter passing overhead
2. **Clarity**: Each method signature clearly shows what parameters are needed
3. **Maintainability**: Easier to understand and modify each strategy
4. **Type Safety**: Compile-time validation of correct parameter usage
5. **Extensibility**: Easy to add new strategies with their own optimal signatures

### Migration Impact

**Files to Update:**
- `LocationStrategy.java` - Simplify interface
- `RandomLocationStrategy.java` - Remove unused parameters from selectCandidateNodes
- `GuidedLocationStrategy.java` - Remove unused parameters from selectCandidateNodes  
- `TargetLocationStrategy.java` - New implementation with optimal signature
- `TransformationEngine.java` - Update all strategy usage to use optimal signatures
- `LocationStrategyFactory.java` - Update factory methods if needed
- All test files - Update to use new method signatures