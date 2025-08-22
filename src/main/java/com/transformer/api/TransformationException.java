package com.transformer.api;

/**
 * Exception thrown when transformation execution errors occur.
 * This includes errors during AST manipulation, transform application,
 * or other transformation-related operations.
 */
public class TransformationException extends TransformerException {
    
    private final String transformName;
    private final String filePath;
    
    /**
     * Constructs a new TransformationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public TransformationException(String message) {
        super(message);
        this.transformName = null;
        this.filePath = null;
    }
    
    /**
     * Constructs a new TransformationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TransformationException(String message, Throwable cause) {
        super(message, cause);
        this.transformName = null;
        this.filePath = null;
    }
    
    /**
     * Constructs a new TransformationException with transform and file context.
     * 
     * @param message the detail message
     * @param transformName the name of the transform that failed
     * @param filePath the path of the file being transformed
     */
    public TransformationException(String message, String transformName, String filePath) {
        super(message);
        this.transformName = transformName;
        this.filePath = filePath;
    }
    
    /**
     * Constructs a new TransformationException with transform and file context and cause.
     * 
     * @param message the detail message
     * @param transformName the name of the transform that failed
     * @param filePath the path of the file being transformed
     * @param cause the cause of the exception
     */
    public TransformationException(String message, String transformName, String filePath, Throwable cause) {
        super(message, cause);
        this.transformName = transformName;
        this.filePath = filePath;
    }
    
    /**
     * Gets the name of the transform that failed.
     * 
     * @return the transform name, or null if not specified
     */
    public String getTransformName() {
        return transformName;
    }
    
    /**
     * Gets the path of the file being transformed when the error occurred.
     * 
     * @return the file path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Creates an exception for when a transform fails to apply to a node.
     * 
     * @param transformName the name of the transform
     * @param nodeType the type of AST node
     * @param filePath the file being transformed
     * @return TransformationException with appropriate message
     */
    public static TransformationException transformApplicationFailed(String transformName, String nodeType, String filePath) {
        return new TransformationException(
            String.format("Failed to apply transform '%s' to node type '%s'", transformName, nodeType),
            transformName,
            filePath
        );
    }
    
    /**
     * Creates an exception for when no suitable nodes are found for transformation.
     * 
     * @param transformName the name of the transform
     * @param filePath the file being transformed
     * @return TransformationException with appropriate message
     */
    public static TransformationException noSuitableNodes(String transformName, String filePath) {
        return new TransformationException(
            String.format("No suitable nodes found for transform '%s'", transformName),
            transformName,
            filePath
        );
    }
    
    /**
     * Creates an exception for when a transform is not found in the registry.
     * 
     * @param transformName the name of the requested transform
     * @return TransformationException with appropriate message
     */
    public static TransformationException transformNotFound(String transformName) {
        return new TransformationException(
            String.format("Transform '%s' not found in registry", transformName),
            transformName,
            null
        );
    }
}