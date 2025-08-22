package com.transformer.api;

/**
 * Exception thrown when I/O operations fail during transformation.
 * This includes file reading, writing, and other file system operations.
 */
public class IOTransformerException extends TransformerException {
    
    private final String filePath;
    private final String operation;
    
    /**
     * Constructs a new IOTransformerException with the specified detail message.
     * 
     * @param message the detail message
     */
    public IOTransformerException(String message) {
        super(message);
        this.filePath = null;
        this.operation = null;
    }
    
    /**
     * Constructs a new IOTransformerException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public IOTransformerException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.operation = null;
    }
    
    /**
     * Constructs a new IOTransformerException with file and operation context.
     * 
     * @param message the detail message
     * @param filePath the path of the file involved in the operation
     * @param operation the type of operation that failed
     */
    public IOTransformerException(String message, String filePath, String operation) {
        super(message);
        this.filePath = filePath;
        this.operation = operation;
    }
    
    /**
     * Constructs a new IOTransformerException with file and operation context and cause.
     * 
     * @param message the detail message
     * @param filePath the path of the file involved in the operation
     * @param operation the type of operation that failed
     * @param cause the cause of the exception
     */
    public IOTransformerException(String message, String filePath, String operation, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
        this.operation = operation;
    }
    
    /**
     * Gets the path of the file involved in the failed operation.
     * 
     * @return the file path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets the type of operation that failed.
     * 
     * @return the operation type, or null if not specified
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Creates an exception for when a file cannot be read.
     * 
     * @param filePath the path of the file
     * @param cause the underlying I/O exception
     * @return IOTransformerException with appropriate message
     */
    public static IOTransformerException fileReadError(String filePath, Throwable cause) {
        return new IOTransformerException(
            String.format("Failed to read file: %s", filePath),
            filePath,
            "read",
            cause
        );
    }
    
    /**
     * Creates an exception for when a file cannot be written.
     * 
     * @param filePath the path of the file
     * @param cause the underlying I/O exception
     * @return IOTransformerException with appropriate message
     */
    public static IOTransformerException fileWriteError(String filePath, Throwable cause) {
        return new IOTransformerException(
            String.format("Failed to write file: %s", filePath),
            filePath,
            "write",
            cause
        );
    }
    
    /**
     * Creates an exception for when a file does not exist.
     * 
     * @param filePath the path of the file
     * @return IOTransformerException with appropriate message
     */
    public static IOTransformerException fileNotFound(String filePath) {
        return new IOTransformerException(
            String.format("File not found: %s", filePath),
            filePath,
            "access"
        );
    }
    
    /**
     * Creates an exception for when a directory cannot be created.
     * 
     * @param dirPath the path of the directory
     * @return IOTransformerException with appropriate message
     */
    public static IOTransformerException directoryCreationFailed(String dirPath) {
        return new IOTransformerException(
            String.format("Failed to create directory: %s", dirPath),
            dirPath,
            "create"
        );
    }
    
    /**
     * Creates an exception for when file permissions are insufficient.
     * 
     * @param filePath the path of the file
     * @param operation the operation that was attempted
     * @return IOTransformerException with appropriate message
     */
    public static IOTransformerException permissionDenied(String filePath, String operation) {
        return new IOTransformerException(
            String.format("Permission denied for %s operation on file: %s", operation, filePath),
            filePath,
            operation
        );
    }
}