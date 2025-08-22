package com.transformer.api;

/**
 * Base exception for all transformer operations.
 * This is the root exception class for the Transformer library.
 */
public class TransformerException extends Exception {
    
    /**
     * Constructs a new TransformerException with the specified detail message.
     * 
     * @param message the detail message
     */
    public TransformerException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new TransformerException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TransformerException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new TransformerException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public TransformerException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructs a new TransformerException with no detail message.
     */
    public TransformerException() {
        super();
    }
}