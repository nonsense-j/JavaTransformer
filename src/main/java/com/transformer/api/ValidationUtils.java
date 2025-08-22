package com.transformer.api;

import java.util.List;

/**
 * Utility class for validating API parameters, especially bug information.
 */
public class ValidationUtils {
    
    private ValidationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates bug information for guided transformations.
     * 
     * @param bugInfo Bug information to validate
     * @throws BugInformationException if validation fails
     */
    public static void validateBugInfoForGuidedTransformation(BugInformation bugInfo) throws BugInformationException {
        if (bugInfo == null) {
            throw BugInformationException.missingBugInfoForGuidedStrategy();
        }
        
        if (!bugInfo.hasBugs()) {
            throw new BugInformationException(
                "Guided transformation requires bug information with hasBugs=true"
            );
        }
        
        if (bugInfo.getBugLines() == null || bugInfo.getBugLines().isEmpty()) {
            throw new BugInformationException(
                "Guided transformation requires at least one bug line number"
            );
        }
        
        validateBugLines(bugInfo.getBugLines());
    }
    
    /**
     * Validates bug information parameters (can be null for non-guided transformations).
     * 
     * @param bugInfo Bug information to validate (can be null)
     * @throws BugInformationException if validation fails
     */
    public static void validateBugInfo(BugInformation bugInfo) throws BugInformationException {
        if (bugInfo == null) {
            return; // Null is acceptable for non-guided transformations
        }
        
        // BugInformation class handles its own validation in constructor
        // Additional validation can be added here if needed
        
        if (bugInfo.hasBugs() && bugInfo.getBugLines() != null) {
            validateBugLines(bugInfo.getBugLines());
        }
    }
    
    /**
     * Validates a list of bug line numbers.
     * 
     * @param bugLines List of bug line numbers to validate
     * @throws BugInformationException if any line number is invalid
     */
    public static void validateBugLines(List<Integer> bugLines) throws BugInformationException {
        if (bugLines == null) {
            return;
        }
        
        for (int i = 0; i < bugLines.size(); i++) {
            Integer lineNumber = bugLines.get(i);
            if (lineNumber == null) {
                throw BugInformationException.invalidBugLines(
                    "null value at index " + i
                );
            }
            if (lineNumber <= 0) {
                throw BugInformationException.invalidBugLines(
                    "non-positive value " + lineNumber + " at index " + i
                );
            }
        }
    }
    
    /**
     * Validates transformation parameters.
     * 
     * @param inputPath Input file path
     * @param outputPath Output file path
     * @param maxAttempts Maximum transformation attempts (if applicable)
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateTransformationParameters(String inputPath, String outputPath, int maxAttempts) {
        if (inputPath == null || inputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Input path cannot be null or empty");
        }
        
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }
        
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("Max attempts cannot be negative, got: " + maxAttempts);
        }
    }
    
    /**
     * Validates transformation parameters without maxAttempts.
     * 
     * @param inputPath Input file path
     * @param outputPath Output file path
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateTransformationParameters(String inputPath, String outputPath) {
        validateTransformationParameters(inputPath, outputPath, 0);
    }
    
    /**
     * Validates transform name parameter.
     * 
     * @param transformName Name of the transformation
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateTransformName(String transformName) {
        if (transformName == null || transformName.trim().isEmpty()) {
            throw new IllegalArgumentException("Transform name cannot be null or empty");
        }
    }
}