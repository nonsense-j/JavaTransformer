# Requirements Document

## Introduction

This feature focuses on refactoring the TransformerService application logic to implement a proper strategy-based approach for applying code transformations. The current implementation has logical issues in how it applies transforms to candidate nodes. The refactored system should correctly use RandomStrategy and GuidedStrategy to select candidate nodes, then apply all available transforms to each candidate to generate mutants.

## Requirements

### Requirement 1

**User Story:** As a developer using the Transformer library, I want the RandomStrategy to properly select candidate nodes and apply all transforms, so that I can generate multiple mutants from random locations in my code.

#### Acceptance Criteria

1. WHEN RandomStrategy is used THEN the system SHALL randomly select randomCnt nodes (default 5) from all available nodes in the file
2. WHEN candidate nodes are selected THEN the system SHALL attempt to apply all available transforms to each candidate node
3. WHEN a transform is applicable to a candidate node THEN the system SHALL generate a mutant and save it to the output directory
4. WHEN a transform is not applicable to a candidate node THEN the system SHALL skip that transform without error
5. WHEN randomCnt parameter is specified THEN the system SHALL use that value instead of the default 5

### Requirement 2

**User Story:** As a researcher using bug localization, I want the GuidedStrategy to work correctly with bug information, so that I can focus transformations on problematic code areas.

#### Acceptance Criteria

1. WHEN GuidedStrategy is used THEN the system SHALL require bug=True and valid buglines to be provided
2. IF bug=False or buglines are not provided THEN the system SHALL throw an appropriate exception with clear error message
3. WHEN valid bug information is provided THEN the system SHALL select candidate nodes related to code above the bug lines
4. WHEN candidate nodes are selected by GuidedStrategy THEN the system SHALL attempt to apply all available transforms to each candidate and save mutants to the output directory
5. WHEN GuidedStrategy selects nodes THEN the system SHALL prioritize nodes closer to the reported bug lines

### Requirement 3

**User Story:** As a developer, I want to optionally specify a particular transform method, so that I can apply only that specific transformation instead of all available transforms.

#### Acceptance Criteria

1. WHEN a specific transform is specified THEN the system SHALL apply only that transform to the selected candidate nodes
2. WHEN no specific transform is specified THEN the system SHALL attempt to apply all available transforms to each candidate node
3. WHEN a specified transform is not available THEN the system SHALL throw an appropriate exception
4. WHEN a specified transform is not applicable to any candidate nodes THEN the system SHALL return a result indicating no transformations were applied
5. WHEN a specified transform is successfully applied THEN the system SHALL track only that transformation in the result

### Requirement 4

**User Story:** As a user of the transformation system, I want proper error handling and validation, so that I can understand what went wrong when transformations fail.

#### Acceptance Criteria

1. WHEN invalid strategy parameters are provided THEN the system SHALL throw descriptive exceptions
2. WHEN no candidate nodes can be selected THEN the system SHALL return a result indicating no nodes were available
3. WHEN file parsing fails THEN the system SHALL throw a ParseException with details about the parsing error
4. WHEN transformation application fails THEN the system SHALL continue with other transforms and report partial results
5. WHEN all transformations fail THEN the system SHALL return a result with error information and no applied transforms

### Requirement 5

**User Story:** As a developer integrating the transformer, I want consistent API behavior, so that I can reliably use the service in different scenarios.

#### Acceptance Criteria

1. WHEN the same input is provided multiple times THEN RandomStrategy SHALL produce different results due to randomness
2. WHEN the same input and bug information is provided THEN GuidedStrategy SHALL produce consistent candidate node selection
3. WHEN transformation results are returned THEN they SHALL include complete metadata about applied transforms and execution details
4. WHEN multiple mutants are generated THEN each SHALL be properly tracked with its corresponding transform and target node
5. WHEN the API is called concurrently THEN each call SHALL operate independently without interference

### Requirement 6

**User Story:** As a developer using the transformer library, I want multiple mutants to be saved to separate files in a directory, so that I can easily manage and analyze each generated variant.

#### Acceptance Criteria

1. WHEN multiple mutants are generated THEN the system SHALL save each mutant to a separate file in the specified output directory
2. WHEN an output directory is specified THEN the system SHALL create the directory if it doesn't exist
3. WHEN mutant files are created THEN each SHALL be named with a clear pattern indicating the transform used and sequence number
4. WHEN a mutant file is generated THEN it SHALL include a comment header identifying the transform and source file
5. WHEN the output directory already contains files THEN the system SHALL handle naming conflicts appropriately

### Requirement 7

**User Story:** As a researcher analyzing transformed code, I want each mutant to include identifying information, so that I can trace which transformation was applied and from which source file.

#### Acceptance Criteria

1. WHEN a mutant is generated THEN the first line SHALL be a comment in the format "// mutant by transform {transform_name} from {input_path}"
2. WHEN the mutant comment is added THEN it SHALL not interfere with the existing code structure or compilation
3. WHEN multiple mutants are generated from the same source THEN each SHALL have the correct transform name in its comment
4. WHEN the input path contains special characters THEN the comment SHALL properly escape or handle them
5. WHEN the transform name is retrieved THEN it SHALL match the exact name used in the transformation registry