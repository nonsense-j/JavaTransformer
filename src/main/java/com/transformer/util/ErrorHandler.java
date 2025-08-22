package com.transformer.util;

import com.transformer.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for consistent error handling throughout the Transformer system.
 * Provides methods for wrapping exceptions, validating inputs, and creating
 * standardized error messages.
 */
public class ErrorHandler {
    
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());
    
    /**
     * Validates that a file path exists and is readable.
     * 
     * @param filePath the file path to validate
     * @throws IOTransformerException if the file doesn't exist or isn't readable
     */
    public static void validateInputFile(String filePath) throws IOTransformerException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IOTransformerException("Input file path cannot be null or empty");
        }
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw IOTransformerException.fileNotFound(filePath);
        }
        
        if (!Files.isReadable(path)) {
            throw IOTransformerException.permissionDenied(filePath, "read");
        }
        
        if (!Files.isRegularFile(path)) {
            throw new IOTransformerException(
                String.format("Path is not a regular file: %s", filePath),
                filePath,
                "access"
            );
        }
    }
    
    /**
     * Validates that an output file path is writable.
     * Creates parent directories if they don't exist.
     * 
     * @param filePath the output file path to validate
     * @throws IOTransformerException if the path is not writable
     */
    public static void validateOutputFile(String filePath) throws IOTransformerException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IOTransformerException("Output file path cannot be null or empty");
        }
        
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();
        
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw IOTransformerException.directoryCreationFailed(parentDir.toString());
            }
        }
        
        if (Files.exists(path) && !Files.isWritable(path)) {
            throw IOTransformerException.permissionDenied(filePath, "write");
        }
    }
    
    /**
     * Validates bug information parameters.
     * 
     * @param bugInfo the bug information to validate
     * @throws BugInformationException if validation fails
     */
    public static void validateBugInformation(BugInformation bugInfo) throws BugInformationException {
        if (bugInfo == null) {
            return; // null is allowed for non-guided strategies
        }
        
        if (bugInfo.hasBugs() && (bugInfo.getBugLines() == null || bugInfo.getBugLines().isEmpty())) {
            throw BugInformationException.inconsistentBugInfo();
        }
        
        if (!bugInfo.hasBugs() && bugInfo.getBugLines() != null && !bugInfo.getBugLines().isEmpty()) {
            throw BugInformationException.inconsistentBugInfo();
        }
        
        if (bugInfo.getBugLines() != null) {
            for (Integer lineNumber : bugInfo.getBugLines()) {
                if (lineNumber == null || lineNumber <= 0) {
                    throw BugInformationException.invalidBugLines(
                        "Line numbers must be positive integers, found: " + lineNumber
                    );
                }
            }
        }
    }
    
    /**
     * Validates transformation parameters.
     * 
     * @param transformName the transform name (can be null for location-based)
     * @param inputPath the input file path
     * @param outputPath the output file path
     * @param maxAttempts the maximum number of attempts (for location-based)
     * @throws TransformerException if validation fails
     */
    public static void validateTransformationParameters(String transformName, String inputPath, 
                                                       String outputPath, Integer maxAttempts) 
                                                       throws TransformerException {
        validateInputFile(inputPath);
        validateOutputFile(outputPath);
        
        if (maxAttempts != null && maxAttempts <= 0) {
            throw new TransformationException(
                "Maximum attempts must be a positive integer, got: " + maxAttempts
            );
        }
        
        if (transformName != null && transformName.trim().isEmpty()) {
            throw new TransformationException("Transform name cannot be empty");
        }
    }
    
    /**
     * Wraps an IOException in the appropriate transformer exception.
     * 
     * @param e the IOException to wrap
     * @param filePath the file path involved in the operation
     * @param operation the operation that failed
     * @return IOTransformerException wrapping the original exception
     */
    public static IOTransformerException wrapIOException(IOException e, String filePath, String operation) {
        logger.log(Level.WARNING, String.format("I/O error during %s operation on file %s", operation, filePath), e);
        
        if (e instanceof java.nio.file.NoSuchFileException) {
            return IOTransformerException.fileNotFound(filePath);
        } else if (e instanceof java.nio.file.AccessDeniedException) {
            return IOTransformerException.permissionDenied(filePath, operation);
        } else {
            return new IOTransformerException(
                String.format("I/O error during %s operation: %s", operation, e.getMessage()),
                filePath,
                operation,
                e
            );
        }
    }
    
    /**
     * Wraps a parsing exception in a ParseException.
     * 
     * @param e the original exception
     * @param filePath the file being parsed
     * @return ParseException wrapping the original exception
     */
    public static ParseException wrapParseException(Exception e, String filePath) {
        logger.log(Level.WARNING, String.format("Parse error in file %s", filePath), e);
        
        if (e.getMessage() != null && e.getMessage().contains("syntax")) {
            return ParseException.syntaxError(filePath, e);
        } else {
            return new ParseException(
                String.format("Failed to parse file: %s", e.getMessage()),
                filePath,
                e
            );
        }
    }
    
    /**
     * Creates a detailed error message for transformation failures.
     * 
     * @param transformName the name of the transform
     * @param filePath the file being transformed
     * @param nodeType the type of AST node
     * @param cause the underlying cause
     * @return formatted error message
     */
    public static String createTransformationErrorMessage(String transformName, String filePath, 
                                                         String nodeType, Throwable cause) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Transformation failed - Transform: %s, File: %s", 
                                    transformName, filePath));
        
        if (nodeType != null) {
            message.append(String.format(", Node Type: %s", nodeType));
        }
        
        if (cause != null) {
            message.append(String.format(", Cause: %s", cause.getMessage()));
        }
        
        return message.toString();
    }
    
    /**
     * Logs an error and returns a standardized error result.
     * 
     * @param message the error message
     * @param cause the underlying cause (optional)
     * @return TransformationResult indicating failure
     */
    public static TransformationResult createErrorResult(String message, Throwable cause) {
        logger.log(Level.SEVERE, message, cause);
        
        TransformationResult result = new TransformationResult();
        result.setSuccess(false);
        result.setErrorMessage(message);
        
        if (cause != null) {
            result.addMetadata("exception_type", cause.getClass().getSimpleName());
            result.addMetadata("exception_message", cause.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validates that a list of parameters is not null or empty.
     * 
     * @param items the list to validate
     * @param parameterName the name of the parameter for error messages
     * @throws TransformerException if the list is null or empty
     */
    public static void validateNotEmpty(List<?> items, String parameterName) throws TransformerException {
        if (items == null || items.isEmpty()) {
            throw new TransformerException(
                String.format("Parameter '%s' cannot be null or empty", parameterName)
            );
        }
    }
    
    /**
     * Validates that a string parameter is not null or empty.
     * 
     * @param value the string to validate
     * @param parameterName the name of the parameter for error messages
     * @throws TransformerException if the string is null or empty
     */
    public static void validateNotEmpty(String value, String parameterName) throws TransformerException {
        if (value == null || value.trim().isEmpty()) {
            throw new TransformerException(
                String.format("Parameter '%s' cannot be null or empty", parameterName)
            );
        }
    }
}