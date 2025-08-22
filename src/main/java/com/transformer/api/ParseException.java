package com.transformer.api;

/**
 * Exception thrown when AST parsing errors occur.
 * This includes errors during Java source code parsing,
 * AST creation, or document processing.
 */
public class ParseException extends TransformerException {
    
    private final String filePath;
    private final int lineNumber;
    private final int columnNumber;
    
    /**
     * Constructs a new ParseException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ParseException(String message) {
        super(message);
        this.filePath = null;
        this.lineNumber = -1;
        this.columnNumber = -1;
    }
    
    /**
     * Constructs a new ParseException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.lineNumber = -1;
        this.columnNumber = -1;
    }
    
    /**
     * Constructs a new ParseException with file context.
     * 
     * @param message the detail message
     * @param filePath the path of the file being parsed
     */
    public ParseException(String message, String filePath) {
        super(message);
        this.filePath = filePath;
        this.lineNumber = -1;
        this.columnNumber = -1;
    }
    
    /**
     * Constructs a new ParseException with file and position context.
     * 
     * @param message the detail message
     * @param filePath the path of the file being parsed
     * @param lineNumber the line number where the error occurred
     * @param columnNumber the column number where the error occurred
     */
    public ParseException(String message, String filePath, int lineNumber, int columnNumber) {
        super(message);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    
    /**
     * Constructs a new ParseException with file context and cause.
     * 
     * @param message the detail message
     * @param filePath the path of the file being parsed
     * @param cause the cause of the exception
     */
    public ParseException(String message, String filePath, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
        this.lineNumber = -1;
        this.columnNumber = -1;
    }
    
    /**
     * Gets the path of the file being parsed when the error occurred.
     * 
     * @return the file path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets the line number where the parsing error occurred.
     * 
     * @return the line number, or -1 if not specified
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Gets the column number where the parsing error occurred.
     * 
     * @return the column number, or -1 if not specified
     */
    public int getColumnNumber() {
        return columnNumber;
    }
    
    /**
     * Creates an exception for when a file cannot be read.
     * 
     * @param filePath the path of the file
     * @param cause the underlying I/O exception
     * @return ParseException with appropriate message
     */
    public static ParseException fileReadError(String filePath, Throwable cause) {
        return new ParseException(
            String.format("Failed to read file: %s", filePath),
            filePath,
            cause
        );
    }
    
    /**
     * Creates an exception for when Java source code cannot be parsed.
     * 
     * @param filePath the path of the file
     * @param cause the underlying parsing exception
     * @return ParseException with appropriate message
     */
    public static ParseException syntaxError(String filePath, Throwable cause) {
        return new ParseException(
            String.format("Syntax error in Java file: %s", filePath),
            filePath,
            cause
        );
    }
    
    /**
     * Creates an exception for when AST creation fails.
     * 
     * @param filePath the path of the file
     * @return ParseException with appropriate message
     */
    public static ParseException astCreationFailed(String filePath) {
        return new ParseException(
            String.format("Failed to create AST for file: %s", filePath),
            filePath
        );
    }
}