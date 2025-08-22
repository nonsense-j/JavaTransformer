package com.transformer.api;

import java.util.List;

/**
 * Main service interface for Java code transformation operations.
 * Provides two core methods following the correct workflow: random and guided transformations.
 */
public interface TransformerService {
    
    /**
     * Apply random transformation strategy.
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory where mutant files will be saved
     * @param transformName Specific transform to apply (null/empty = try all transforms)
     * @param randomCnt Number of random nodes to select (default: 5)
     * @return TransformationResult with all generated mutants
     */
    TransformationResult applyRandomTransform(String inputPath, String outputDir, int randomCnt, String transformName);
    
    /**
     * Apply random transformation strategy with default maxNodeCnt=5.
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory where mutant files will be saved
     * @param transformName Specific transform to apply (null/empty = try all transforms)
     * @return TransformationResult with all generated mutants
     */
    default TransformationResult applyRandomTransform(String inputPath, String outputDir, String transformName) {
        return applyRandomTransform(inputPath, outputDir, 5, transformName);
    }
    
    /**
     * Apply random transformation strategy with specified randomCnt (try all transforms).
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory where mutant files will be saved
     * @param randomCnt Number of random nodes to select
     * @return TransformationResult with all generated mutants
     */
    default TransformationResult applyRandomTransform(String inputPath, String outputDir, int randomCnt) {
        return applyRandomTransform(inputPath, outputDir, randomCnt, null);
    }
    
    /**
     * Apply guided transformation strategy using bug information.
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory where mutant files will be saved
     * @param bugInfo Bug information (must have bug=true and valid bug lines)
     * @param transformName Specific transform to apply (null/empty = try all transforms)
     * @return TransformationResult with all generated mutants
     */
    TransformationResult applyGuidedTransform(String inputPath, String outputDir, BugInformation bugInfo, String transformName);
    
    /**
     * Apply guided transformation strategy (legacy overload).
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory where mutant files will be saved
     * @param bugInfo Bug information (must have bug=true and valid bug lines)
     * @return TransformationResult with all generated mutants
     */
    default TransformationResult applyGuidedTransform(String inputPath, String outputDir, BugInformation bugInfo) {
        return applyGuidedTransform(inputPath, outputDir, bugInfo, null);
    }
    
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
    
    /**
     * Apply target transformation strategy with all transforms.
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory where mutant files will be saved
     * @param targetLines List of line numbers to target for transformation
     * @return TransformationResult with all generated mutants
     */
    default TransformationResult applyTargetTransform(String inputPath, String outputDir, List<Integer> targetLines) {
        return applyTargetTransform(inputPath, outputDir, targetLines, null);
    }
    
    /**
     * Get list of all available transformation names.
     * 
     * @return List of transformation names that can be used
     */
    List<String> getAvailableTransforms();
    
    /**
     * Validate the current configuration.
     * 
     * @return true if configuration is valid, false otherwise
     */
    boolean validateConfiguration();
}