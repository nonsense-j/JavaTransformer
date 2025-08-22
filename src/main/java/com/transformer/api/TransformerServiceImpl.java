package com.transformer.api;

import com.transformer.core.TransformationEngine;
import com.transformer.core.TransformRegistry;

import java.util.List;

/**
 * Implementation of the TransformerService interface.
 * Provides concrete implementations for the two core transformation operations
 * following the correct workflow:
 * 
 * 1. RandomStrategy: Select randomCnt (default 5) random nodes, then try all transforms on each node
 * 2. GuidedStrategy: Select candidate nodes based on bug lines, then try all transforms on each node
 * 3. When specific transform is provided: Only use that transform instead of all transforms
 * 
 * Each successful transformation generates a separate mutant.
 */
public class TransformerServiceImpl implements TransformerService {

    private final TransformationEngine transformationEngine;
    private final TransformRegistry transformRegistry;

    /**
     * Default constructor that initializes with default components.
     */
    public TransformerServiceImpl() {
        this.transformRegistry = TransformRegistry.getInstance();
        this.transformationEngine = new TransformationEngine(transformRegistry);
    }

    /**
     * Constructor with custom transform registry for testing.
     * 
     * @param transformRegistry Custom transform registry
     */
    public TransformerServiceImpl(TransformRegistry transformRegistry) {
        this.transformRegistry = transformRegistry != null ? transformRegistry : TransformRegistry.getInstance();
        this.transformationEngine = new TransformationEngine(this.transformRegistry);
    }

    @Override
    public TransformationResult applyRandomTransform(String inputPath, String outputDir, int randomCnt, String transformName) {
        // Parameter validation
        if (inputPath == null || inputPath.trim().isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Input path cannot be null or empty")
                    .build();
        }

        if (outputDir == null || outputDir.trim().isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Output directory cannot be null or empty")
                    .build();
        }

        if (randomCnt < 0) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Random count cannot be negative")
                    .build();
        }

        // Use default randomCnt of 5 if not specified
        int actualRandomCnt = randomCnt > 0 ? randomCnt : 5;

        // Validate transform name if specified
        if (transformName != null && !transformName.trim().isEmpty()
                && !transformRegistry.hasTransform(transformName.trim())) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Transform not found: " + transformName + ". Available transforms: " +
                            String.join(", ", getAvailableTransforms()))
                    .build();
        }

        // Execute random transformation directly
        return transformationEngine.executeRandomTransformation(
                inputPath.trim(), 
                outputDir.trim(), 
                transformName != null && !transformName.trim().isEmpty() ? transformName.trim() : null,
                actualRandomCnt
        );
    }

    @Override
    public TransformationResult applyGuidedTransform(String inputPath, String outputDir, BugInformation bugInfo,
            String transformName) {
        // Parameter validation
        if (inputPath == null || inputPath.trim().isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Input path cannot be null or empty")
                    .build();
        }

        if (outputDir == null || outputDir.trim().isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Output directory cannot be null or empty")
                    .build();
        }

        // Bug information validation for guided strategy - must have bug=true and buglines
        if (bugInfo == null) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Bug information is required for guided transformations")
                    .build();
        }

        if (!bugInfo.isValidForGuidedTransformation()) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Invalid bug information for guided transformation. " +
                            "Bug information must have hasBugs=true and non-empty bug lines list.")
                    .build();
        }

        // Validate transform name if specified
        if (transformName != null && !transformName.trim().isEmpty()
                && !transformRegistry.hasTransform(transformName.trim())) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Transform not found: " + transformName + ". Available transforms: " +
                            String.join(", ", getAvailableTransforms()))
                    .build();
        }

        // Execute guided transformation directly
        return transformationEngine.executeGuidedTransformation(
                inputPath.trim(), 
                outputDir.trim(), 
                bugInfo,
                transformName != null && !transformName.trim().isEmpty() ? transformName.trim() : null
        );
    }

    @Override
    public TransformationResult applyTargetTransform(String inputPath, String outputDir, List<Integer> targetLines, String transformName) {
        // Parameter validation
        if (inputPath == null || inputPath.trim().isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Input path cannot be null or empty")
                    .build();
        }

        if (outputDir == null || outputDir.trim().isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Output directory cannot be null or empty")
                    .build();
        }

        if (targetLines == null) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Target lines cannot be null")
                    .build();
        }

        // Validate target lines content
        for (Integer lineNumber : targetLines) {
            if (lineNumber == null) {
                return TransformationResult.builder()
                        .success(false)
                        .errorMessage("Target lines cannot contain null values")
                        .build();
            }
            if (lineNumber < 1) {
                return TransformationResult.builder()
                        .success(false)
                        .errorMessage("Target lines cannot contain negative or zero line numbers: " + lineNumber)
                        .build();
            }
        }

        // Validate transform name if specified
        if (transformName != null && !transformName.trim().isEmpty()
                && !transformRegistry.hasTransform(transformName.trim())) {
            return TransformationResult.builder()
                    .success(false)
                    .errorMessage("Transform not found: " + transformName + ". Available transforms: " +
                            String.join(", ", getAvailableTransforms()))
                    .build();
        }

        // Execute target transformation
        return transformationEngine.executeTargetTransformation(
                inputPath.trim(), 
                outputDir.trim(), 
                targetLines,
                transformName != null && !transformName.trim().isEmpty() ? transformName.trim() : null
        );
    }

    @Override
    public List<String> getAvailableTransforms() {
        return transformRegistry.getAvailableTransforms();
    }

    @Override
    public boolean validateConfiguration() {
        // Configuration validation is not needed for core transformation functionality
        return true;
    }
}