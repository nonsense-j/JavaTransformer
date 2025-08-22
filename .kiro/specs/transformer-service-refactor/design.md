# Design Document

## Overview

This design addresses the logical issues in the current TransformerService implementation by refactoring the transformation application logic to properly implement strategy-based node selection and transform application. The key issue is that the current system doesn't correctly implement the intended behavior where strategies select candidate nodes and then all applicable transforms are applied to each candidate.

## Current Issues

1. **RandomStrategy Logic**: Currently returns all nodes shuffled, but doesn't properly limit to `randomCnt` nodes (default 5)
2. **Transform Application**: Only applies the first applicable transform to each candidate node instead of all applicable transforms
3. **GuidedStrategy Validation**: Doesn't properly validate bug information requirements
4. **Mutant Generation**: Doesn't generate multiple mutants per candidate node as intended
5. **API Design**: Doesn't provide clear methods for "TransformerService" following the correct workflow

## Architecture

### Core Components

#### 1. Enhanced LocationStrategy Interface
- Add `randomCnt` parameter support for RandomLocationStrategy
- Improve validation for GuidedLocationStrategy
- Add method to get strategy-specific parameters

#### 2. Refactored TransformationEngine
- Implement proper candidate node selection with configurable limits
- Apply all applicable transforms to each candidate node
- Generate multiple mutants per transformation session
- Improve error handling and validation

#### 3. Updated TransformerService API
- Add methods with `randomCnt` parameter for RandomStrategy
- Enhance validation for GuidedStrategy requirements
- Support optional transform specification

#### 4. Enhanced Result Tracking
- Track multiple mutants generated from single input
- Record which transforms were applied to which nodes
- Provide detailed metadata about transformation attempts

## Components and Interfaces

### LocationStrategy Enhancements

```java
public interface LocationStrategy {
    // Existing method
    List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, List<Transform> availableTransforms, BugInformation bugInfo);
    
    // New method with node count limit
    List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, List<Transform> availableTransforms, 
                                     BugInformation bugInfo, int maxNodes);
    
    String getStrategyName();
    
    // New method for strategy validation
    void validateParameters(BugInformation bugInfo) throws IllegalArgumentException;
}
```

### RandomLocationStrategy Refactoring

```java
public class RandomLocationStrategy implements LocationStrategy {
    private final Random random;
    
    public RandomLocationStrategy() {
        this.random = new Random();
    }
    
    @Override
    public List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, List<Transform> availableTransforms, 
                                            BugInformation bugInfo, int maxNodes) {
        // Select exactly maxNodes random nodes (default: 5)
        int nodeCount = maxNodes > 0 ? maxNodes : 5;
        List<ASTNode> allNodes = wrapper.getAllNodes();
        
        if (allNodes.size() <= nodeCount) {
            Collections.shuffle(allNodes, random);
            return new ArrayList<>(allNodes);
        }
        
        Collections.shuffle(allNodes, random);
        return allNodes.subList(0, nodeCount);
    }
    
    @Override
    public void validateParameters(BugInformation bugInfo) {
        // RandomStrategy doesn't require specific bug information
        // No validation needed
    }
}
```

### GuidedLocationStrategy Validation

```java
public class GuidedLocationStrategy implements LocationStrategy {
    @Override
    public void validateParameters(BugInformation bugInfo) throws IllegalArgumentException {
        if (bugInfo == null) {
            throw new IllegalArgumentException("GuidedLocationStrategy requires bug information");
        }
        
        if (!bugInfo.hasBugs()) {
            throw new IllegalArgumentException("GuidedLocationStrategy requires bug=true");
        }
        
        if (bugInfo.getBugLines() == null || bugInfo.getBugLines().isEmpty()) {
            throw new IllegalArgumentException("GuidedLocationStrategy requires valid bug lines");
        }
    }
}
```

### Enhanced TransformationEngine

The core logic change is in how we apply transforms to candidate nodes:

```java
public class TransformationEngine {
    /**
     * Apply transforms to candidate nodes following the correct workflow:
     * 1. Use strategy to select candidate nodes (randomCnt for Random, bug-guided for Guided)
     * 2. For each candidate node, try all applicable transforms (or specific transform if specified)
     * 3. Generate mutants for each successful transformation
     */
    private TransformationResult applyTransforms(TypeWrapper wrapper, LocationStrategy strategy, 
                                               BugInformation bugInfo, String specificTransform, int maxNodeCnt) {
        // Validate strategy parameters
        try {
            strategy.validateParameters(bugInfo);
        } catch (IllegalArgumentException e) {
            return TransformationResult.builder()
                .success(false)
                .errorMessage("Strategy validation failed: " + e.getMessage())
                .build();
        }
        
        // Get transforms to apply
        List<Transform> transformsToTry = getTransformsToApply(specificTransform);
        if (transformsToTry.isEmpty()) {
            return TransformationResult.builder()
                .success(false)
                .errorMessage("No transforms available" + (specificTransform != null ? " for: " + specificTransform : ""))
                .build();
        }
        
        // Select candidate nodes using strategy
        List<ASTNode> candidateNodes = strategy.selectCandidateNodes(wrapper, transformsToTry, bugInfo, maxNodeCnt);
        if (candidateNodes.isEmpty()) {
            return TransformationResult.builder()
                .success(false)
                .errorMessage("No candidate nodes found by strategy")
                .build();
        }
        
        List<MutantInfo> mutants = new ArrayList<>();
        
        // For each candidate node, try all applicable transforms
        for (ASTNode candidateNode : candidateNodes) {
            for (Transform transform : transformsToTry) {
                // Check if transform is applicable to this node
                List<ASTNode> applicableNodes = transform.check(wrapper, candidateNode);
                if (applicableNodes != null && !applicableNodes.isEmpty()) {
                    // Create mutant by applying transform
                    TypeWrapper mutantWrapper = wrapper.copy();
                    boolean success = transform.run(candidateNode, mutantWrapper, null, null);
                    
                    if (success) {
                        MutantInfo mutant = new MutantInfo(transform.getName(), candidateNode, mutantWrapper.getCode());
                        mutants.add(mutant);
                    }
                }
            }
        }
        
        return TransformationResult.builder()
            .success(!mutants.isEmpty())
            .mutants(mutants)
            .appliedTransforms(mutants.stream().map(MutantInfo::getTransformName).distinct().collect(Collectors.toList()))
            .build();
    }
    
    private List<Transform> getTransformsToApply(String specificTransform) {
        if (specificTransform != null && !specificTransform.trim().isEmpty()) {
            Transform transform = transformRegistry.getTransform(specificTransform);
            return transform != null ? List.of(transform) : new ArrayList<>();
        }
        return transformRegistry.getAllTransforms(); // Try all transforms when none specified
    }
}
```

### Updated TransformerService API

The API should be updated to support multiple mutant generation with directory output:

```java
public interface TransformerService {
    /**
     * Apply random transformation strategy.
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory for output mutant files
     * @param transformName Specific transform to apply (null/empty = try all transforms)
     * @param maxNodeCnt Maximum number of nodes to select randomly (default: 5)
     * @return TransformationResult with all generated mutants
     */
    TransformationResult applyRandomTransform(String inputPath, String outputDir, String transformName, int maxNodeCnt);
    
    // Overloaded method with default maxNodeCnt=5
    default TransformationResult applyRandomTransform(String inputPath, String outputDir, String transformName) {
        return applyRandomTransform(inputPath, outputDir, transformName, 5);
    }
    
    /**
     * Apply guided transformation strategy using bug information.
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory for output mutant files
     * @param bugInfo Bug information (must have bug=true and valid bug lines)
     * @param transformName Specific transform to apply (null/empty = try all transforms)
     * @return TransformationResult with all generated mutants
     */
    TransformationResult applyGuidedTransform(String inputPath, String outputDir, BugInformation bugInfo, String transformName);
    
    // Utility methods
    List<String> getAvailableTransforms();
    boolean validateConfiguration();
}
```

## Data Models

### Updated TransformationRequest

```java
public class TransformationRequest {
    private String transformName;        // Optional: specific transform to apply (null = all transforms)
    private String inputPath;
    private String outputDir;            // Directory for output mutant files
    private LocationStrategy strategy;   // RANDOM or GUIDED
    private BugInformation bugInfo;      // Required for GUIDED strategy
    private int maxNodeCnt;             // For RandomStrategy node count (default: 5)
    
    // Builder pattern methods
}
```

### Enhanced TransformationResult

```java
public class TransformationResult {
    private boolean success;
    private List<String> appliedTransforms;
    private List<MutantInfo> mutants;           // New: detailed mutant information
    private String transformedCode;             // Backward compatibility - first mutant's code
    private Map<String, Object> metadata;
    private List<String> errorMessages;
    
    public static class MutantInfo {
        private String transformName;
        private ASTNode targetNode;
        private String mutantCode;              // Code with header comment
        private String outputFilePath;          // Path where mutant was saved
        private Map<String, Object> mutantMetadata;
    }
}
```

## Error Handling

### Strategy Validation Errors
- **RandomStrategy**: No specific validation required
- **GuidedStrategy**: Validate bug=true and non-empty bug lines
- **General**: Validate input files exist and are readable

### Transform Application Errors
- Continue with other transforms if one fails
- Record partial results and error information
- Provide detailed error messages for debugging

### File I/O Errors
- Validate input file exists and is readable
- Ensure output directory exists or can be created
- Handle file permission issues gracefully

### Mutant File Generation
- **File Naming**: Use pattern `{inputFileName}_mutant_{transformName}_{sequence}.java`
- **Header Comments**: Add `// mutant by transform {transformName} from {inputPath}` as first line
- **Directory Creation**: Create output directory if it doesn't exist
- **Conflict Resolution**: Handle existing files by appending sequence numbers
- **File Writing**: Save each mutant to separate file with proper encoding

## Testing Strategy

### Unit Tests
- Test RandomLocationStrategy with different randomCnt values
- Test GuidedLocationStrategy validation logic
- Test TransformationEngine mutant generation
- Test error handling scenarios

### Integration Tests
- Test complete workflow with both strategies
- Test with various Java file types and structures
- Test concurrent transformation operations
- Test performance with large files

### Strategy-Specific Tests
- RandomStrategy: Verify exactly randomCnt nodes are selected
- GuidedStrategy: Verify bug line proximity prioritization
- Both: Verify all applicable transforms are attempted

## Implementation Phases

### Phase 1: Strategy Enhancements
1. Add validation methods to LocationStrategy interface
2. Refactor RandomLocationStrategy with proper randomCnt support
3. Enhance GuidedLocationStrategy validation
4. Update strategy factory and related components

### Phase 2: Engine Refactoring
1. Modify TransformationEngine to generate multiple mutants
2. Implement proper candidate node processing
3. Add support for specific transform filtering
4. Enhance result consolidation logic

### Phase 3: API Updates
1. Add new TransformerService methods with randomCnt parameter
2. Update existing methods to use new engine logic
3. Enhance TransformationRequest and TransformationResult classes
4. Update validation and error handling

### Phase 4: Testing and Validation
1. Create comprehensive unit tests for all components
2. Add integration tests for complete workflows
3. Performance testing with various file sizes
4. Validation against existing test cases