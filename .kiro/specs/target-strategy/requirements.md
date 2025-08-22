# Requirements Document

## Introduction

This feature adds a new `TargetStrategy` to the Transformer library's location strategy system. The `TargetStrategy` allows users to specify exact line numbers where transformations should be applied, providing precise control over transformation locations. This strategy complements the existing `RandomLocationStrategy` and `GuidedLocationStrategy` by offering deterministic, user-controlled node selection based on line numbers.

## Requirements

### Requirement 1

**User Story:** As a developer using the Transformer library, I want to specify exact line numbers for transformations, so that I can precisely control where code mutations are applied.

#### Acceptance Criteria

1. WHEN I call `applyTargetTransform` with a list of target line numbers THEN the system SHALL select only AST nodes that are located on those specific lines
2. WHEN I provide target lines [5, 10, 15] THEN the system SHALL only consider nodes whose line numbers match exactly 5, 10, or 15
3. WHEN I specify a transform name with target lines THEN the system SHALL apply only that specific transform to the selected nodes
4. WHEN I pass null for transform name with target lines THEN the system SHALL try all available transforms on the selected nodes

### Requirement 2

**User Story:** As a developer, I want the entire strategy system to be refactored with optimal method signatures, so that each strategy uses only the parameters it actually needs without any unused parameter overhead.

#### Acceptance Criteria

1. WHEN the LocationStrategy interface is refactored THEN it SHALL only require `getStrategyName()` method
2. WHEN TargetStrategy is implemented THEN it SHALL provide the optimal method signature `selectCandidateNodes(TypeWrapper wrapper, List<Integer> targetLines)`
3. WHEN RandomLocationStrategy is refactored THEN it SHALL use the optimal signature `selectCandidateNodes(TypeWrapper wrapper, int randomNodeCnt)`
4. WHEN GuidedLocationStrategy is refactored THEN it SHALL use the optimal signature `selectCandidateNodes(TypeWrapper wrapper, BugInformation bugInfo)`
5. WHEN any strategy is used THEN it SHALL return its appropriate strategy name ("TARGET_LOCATION", "RANDOM_LOCATION", "GUIDED_LOCATION")

### Requirement 3

**User Story:** As a developer, I want a new API method for target-based transformations, so that I can easily use the TargetStrategy without dealing with strategy implementation details.

#### Acceptance Criteria

1. WHEN the TransformerService interface is extended THEN it SHALL include a new method `applyTargetTransform(String inputPath, String outputDir, List<Integer> targetLines, String transformName)`
2. WHEN I call applyTargetTransform with valid parameters THEN it SHALL return a TransformationResult with all generated mutants
3. WHEN I call applyTargetTransform with null transformName THEN it SHALL apply all available transforms to nodes on target lines
4. WHEN I call applyTargetTransform with empty targetLines list THEN it SHALL return an empty result without errors
5. WHEN the TransformationEngine processes target transformations THEN it SHALL use the optimal strategy method signature `selectCandidateNodes(TypeWrapper wrapper, List<Integer> targetLines)` for better performance

### Requirement 4

**User Story:** As a developer, I want proper validation for target line parameters, so that I receive clear error messages when providing invalid input.

#### Acceptance Criteria

1. WHEN targetLines parameter is null THEN the system SHALL throw IllegalArgumentException with descriptive message
2. WHEN targetLines contains negative line numbers THEN the system SHALL throw IllegalArgumentException
3. WHEN targetLines contains line numbers beyond the file's line count THEN the system SHALL ignore those lines and process valid ones
4. WHEN inputPath or outputDir parameters are invalid THEN the system SHALL return TransformationResult with success=false and appropriate error message

### Requirement 5

**User Story:** As a developer, I want all strategies to handle edge cases gracefully, so that the system remains stable under various input conditions.

#### Acceptance Criteria

1. WHEN target lines don't match any AST nodes THEN the TargetStrategy SHALL return an empty candidate list without errors
2. WHEN multiple nodes exist on the same target line THEN the TargetStrategy SHALL include all nodes from that line
3. WHEN the AST wrapper is null or contains no nodes THEN all strategies SHALL return an empty candidate list
4. WHEN invalid parameters are passed to any strategy THEN it SHALL throw appropriate validation exceptions with clear error messages

### Requirement 6

**User Story:** As a developer, I want the refactored strategy system to maintain clean separation of concerns, so that node selection logic is independent of transformation application logic.

#### Acceptance Criteria

1. WHEN any strategy selects candidate nodes THEN it SHALL not need to know about available transforms
2. WHEN the TransformationEngine uses strategies THEN it SHALL call the strategy-specific method signatures directly
3. WHEN new strategies are added in the future THEN they SHALL be able to define their own optimal method signatures
4. WHEN the system processes transformations THEN node selection and transformation application SHALL be clearly separated phases