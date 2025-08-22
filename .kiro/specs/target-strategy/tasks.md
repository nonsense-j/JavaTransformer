# Implementation Plan

- [-] 1. Refactor existing LocationStrategy system



  - Refactor LocationStrategy interface to only require getStrategyName() method
  - Update RandomLocationStrategy to use optimal signature: selectCandidateNodes(TypeWrapper wrapper, int randomNodeCnt)
  - Update GuidedLocationStrategy to use optimal signature: selectCandidateNodes(TypeWrapper wrapper, BugInformation bugInfo)
  - Update TransformationEngine to call the refactored strategy methods with correct parameters
  - Update LocationStrategyFactory and all related tests to work with the new signatures
  - _Requirements: 2.1, 2.3, 2.4, 6.1, 6.2, 6.4_

- [x] 2. Add TargetLocationStrategy implementation





  - Create TargetLocationStrategy class with optimal signature: selectCandidateNodes(TypeWrapper wrapper, List<Integer> targetLines)
  - Implement node selection logic that filters nodes by line numbers from targetLines list
  - Add applyTargetTransform method to TransformerService interface and implementation
  - Add executeTargetTransformation method to TransformationEngine
  - Register TargetLocationStrategy in LocationStrategyFactory
  - Create comprehensive tests for the new strategy and API methods
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.2, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4_