package com.transformer.api;

/**
 * Exception thrown when bug information validation fails or
 * when bug information is required but not provided correctly.
 */
public class BugInformationException extends TransformerException {
    
    public BugInformationException(String message) {
        super(message);
    }
    
    public BugInformationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates an exception for missing bug information when using guided strategy.
     * 
     * @return BugInformationException with appropriate message
     */
    public static BugInformationException missingBugInfoForGuidedStrategy() {
        return new BugInformationException(
            "Bug information is required when using guided transformation strategy. " +
            "Please provide BugInformation with hasBugs=true and valid bug line numbers."
        );
    }
    
    /**
     * Creates an exception for invalid bug line numbers.
     * 
     * @param invalidLines Description of invalid line numbers
     * @return BugInformationException with appropriate message
     */
    public static BugInformationException invalidBugLines(String invalidLines) {
        return new BugInformationException(
            "Invalid bug line numbers: " + invalidLines + ". " +
            "Bug line numbers must be positive integers."
        );
    }
    
    /**
     * Creates an exception for inconsistent bug information.
     * 
     * @return BugInformationException with appropriate message
     */
    public static BugInformationException inconsistentBugInfo() {
        return new BugInformationException(
            "Inconsistent bug information: hasBugs is true but no bug lines provided, " +
            "or hasBugs is false but bug lines are provided."
        );
    }
}