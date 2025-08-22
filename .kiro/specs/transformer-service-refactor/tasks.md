# Implementation Plan

- [x] 1. Fix RandomLocationStrategy node selection logic





  - Modify `selectCandidateNodes` to select exactly `maxNodes` random nodes (default 5) instead of returning all nodes
  - Add overloaded method with maxNodes parameter
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 2. Add validation to GuidedLocationStrategy





  - Add validation method to check bug=true and non-empty bug lines
  - Throw IllegalArgumentException with clear messages for invalid bug information
  - _Requirements: 2.1, 2.2, 4.1_
-

- [x] 3. Refactor TransformerService API to correct workflow




  - Replace existing methods with two core methods: `applyRandomTransform(inputPath, outputPath, transformName, maxNodeCnt)` and `applyGuidedTransform(inputPath, outputPath, bugInfo, transformName)`
  - When transformName is null/empty, try all available transforms
  - _Requirements: 1.1, 1.5, 2.1, 2.2, 3.1, 3.2, 3.3_




- [ ] 4. Update TransformationEngine to generate multiple mutants

  - Implement correct workflow: select candidate nodes using strategy, then apply all applicable transforms to each node
  - Generate separate mutant for each successful transformation
  - Support specific transform filtering when transformName is provided
  - _Requirements: 1.2, 1.3, 1.4, 3.1, 3.2, 3.4_

- [ ] 4.1 Create new transformation workflow method in TransformationEngine
  - Add `applyTransformsToNodes(TypeWrapper, LocationStrategy, BugInformation, String, int)` method
  - Implement candidate node selection using strategy with proper parameters
  - For each candidate node, find all applicable transforms (or specific transform if provided)
  - Generate separate mutant for each successful transform application
  - _Requirements: 1.2, 1.3, 1.4, 3.1, 3.2_

- [ ] 4.2 Implement mutant generation logic
  - Create TypeWrapper copy for each transformation attempt
  - Apply transform to copied wrapper to generate mutant
  - Track successful transformations and their target nodes
  - Create MutantInfo objects with transform name, target node, and generated code
  - _Requirements: 1.2, 1.3, 3.1, 3.4, 5.4_

- [ ] 4.3 Update transform filtering logic
  - When transformName is null/empty, use all available transforms from registry
  - When transformName is specified, validate it exists and use only that transform
  - Throw appropriate exceptions for invalid transform names
  - _Requirements: 3.1, 3.2, 3.3, 4.1_

- [ ] 5. Update TransformationResult to support multiple mutants

  - Add MutantInfo class to track individual mutant details (transformName, targetNode, mutantCode)
  - Update TransformationResult to include list of generated mutants
  - Maintain backward compatibility with existing result fields
  - _Requirements: 1.4, 3.4, 5.3, 5.4_

- [ ] 5.1 Create MutantInfo class for detailed mutant tracking
  - Add fields: transformName, targetNode, mutantCode, outputFilePath, mutantMetadata
  - Implement proper constructors and getter methods
  - Add toString method for debugging and logging
  - _Requirements: 5.3, 5.4_

- [ ] 5.2 Update TransformationResult to include mutant information
  - Add `List<MutantInfo> mutants` field to track all generated mutants
  - Update existing fields to aggregate information from mutants
  - Maintain backward compatibility with appliedTransforms and transformedCode fields
  - Add convenience methods to access mutant-specific information
  - _Requirements: 1.4, 3.4, 5.3, 5.4_

- [ ] 6. Update TransformerService API to use outputDir parameter

  - Change `outputPath` parameter to `outputDir` in both `applyRandomTransform` and `applyGuidedTransform` methods
  - Update parameter validation to check for valid directory path
  - Update method documentation to reflect directory-based output
  - _Requirements: 6.1, 6.2_

- [ ] 7. Implement mutant file generation with header comments

  - Create utility method to generate mutant header comment in format "// mutant by transform {transformName} from {inputPath}"
  - Implement file naming strategy: `{inputFileName}_mutant_{transformName}_{sequence}.java`
  - Add directory creation logic for output directory
  - Handle file naming conflicts with sequence numbers
  - Save each mutant to separate file with header comment as first line
  - _Requirements: 6.1, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 7.1 Create mutant file writer utility
  - Add method to generate header comment with proper escaping
  - Implement file naming logic with conflict resolution
  - Create directory if it doesn't exist
  - Write mutant code with header comment to file
  - _Requirements: 6.3, 6.4, 6.5, 7.1, 7.2, 7.4_

- [ ] 7.2 Update MutantInfo to include file path information
  - Add `outputFilePath` field to track where each mutant was saved
  - Update constructors to accept file path information
  - Ensure file paths are properly recorded in transformation results
  - _Requirements: 6.1, 6.3, 7.1_

- [ ] 8. Update error handling and validation

  - Implement proper validation for strategy parameters
  - Add descriptive error messages for common failure scenarios
  - Handle partial failures gracefully (some transforms succeed, others fail)
  - Update exception handling to provide better debugging information
  - _Requirements: 2.2, 4.1, 4.2, 4.3, 4.4_

- [ ] 8.1 Implement strategy parameter validation
  - Add validation calls before strategy execution
  - Provide clear error messages for GuidedStrategy validation failures
  - Handle edge cases like null bug information or empty bug lines
  - _Requirements: 2.2, 4.1_

- [ ] 8.2 Enhance error handling for transform application
  - Continue processing other transforms when one fails
  - Record partial results and error information
  - Provide detailed error messages for debugging
  - Handle file I/O errors gracefully
  - _Requirements: 4.2, 4.3, 4.4_

- [ ] 9. Update existing tests for new API

  - Modify existing test cases to use new API methods with outputDir parameter
  - Update test expectations to handle multiple mutants and file generation
  - Test both random and guided strategies with various parameters
  - Add tests for mutant file generation and header comments
  - Test directory creation and file naming conflict resolution
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 5.1, 5.2, 6.1, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3_