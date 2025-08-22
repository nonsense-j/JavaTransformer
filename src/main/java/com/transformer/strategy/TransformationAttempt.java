package com.transformer.strategy;

import com.transformer.transform.Transform;
import org.eclipse.jdt.core.dom.ASTNode;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single transformation attempt with tracking information.
 * Contains details about the transform applied, target node, and result.
 */
public class TransformationAttempt {
    
    private final Transform transform;
    private final ASTNode targetNode;
    private final boolean successful;
    private final String errorMessage;
    private final LocalDateTime timestamp;
    private final long executionTimeMs;
    private final int nodeLineNumber;
    private final String nodeType;
    
    /**
     * Creates a successful transformation attempt.
     * 
     * @param transform Transform that was applied
     * @param targetNode AST node that was transformed
     * @param executionTimeMs Time taken to execute the transformation
     * @param nodeLineNumber Line number of the target node
     */
    public TransformationAttempt(Transform transform, ASTNode targetNode, long executionTimeMs, int nodeLineNumber) {
        this.transform = transform;
        this.targetNode = targetNode;
        this.successful = true;
        this.errorMessage = null;
        this.timestamp = LocalDateTime.now();
        this.executionTimeMs = executionTimeMs;
        this.nodeLineNumber = nodeLineNumber;
        this.nodeType = targetNode != null ? targetNode.getClass().getSimpleName() : "Unknown";
    }
    
    /**
     * Creates a failed transformation attempt.
     * 
     * @param transform Transform that was attempted
     * @param targetNode AST node that was targeted
     * @param errorMessage Error message describing the failure
     * @param executionTimeMs Time taken before failure
     * @param nodeLineNumber Line number of the target node
     */
    public TransformationAttempt(Transform transform, ASTNode targetNode, String errorMessage, 
                               long executionTimeMs, int nodeLineNumber) {
        this.transform = transform;
        this.targetNode = targetNode;
        this.successful = false;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
        this.executionTimeMs = executionTimeMs;
        this.nodeLineNumber = nodeLineNumber;
        this.nodeType = targetNode != null ? targetNode.getClass().getSimpleName() : "Unknown";
    }
    
    /**
     * Creates a successful transformation attempt (convenience method).
     * 
     * @param transform Transform that was applied
     * @param targetNode AST node that was transformed
     * @return Successful TransformationAttempt
     */
    public static TransformationAttempt success(Transform transform, ASTNode targetNode, long executionTimeMs, int nodeLineNumber) {
        return new TransformationAttempt(transform, targetNode, executionTimeMs, nodeLineNumber);
    }
    
    /**
     * Creates a failed transformation attempt (convenience method).
     * 
     * @param transform Transform that was attempted
     * @param targetNode AST node that was targeted
     * @param errorMessage Error message describing the failure
     * @return Failed TransformationAttempt
     */
    public static TransformationAttempt failure(Transform transform, ASTNode targetNode, String errorMessage, 
                                              long executionTimeMs, int nodeLineNumber) {
        return new TransformationAttempt(transform, targetNode, errorMessage, executionTimeMs, nodeLineNumber);
    }
    
    public Transform getTransform() {
        return transform;
    }
    
    public ASTNode getTargetNode() {
        return targetNode;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public int getNodeLineNumber() {
        return nodeLineNumber;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    /**
     * Get the name of the transform that was attempted.
     * 
     * @return Transform name or "Unknown" if transform is null
     */
    public String getTransformName() {
        return transform != null ? transform.getClass().getSimpleName() : "Unknown";
    }
    
    /**
     * Check if this attempt was made on a node at the specified line number.
     * 
     * @param lineNumber Line number to check
     * @return true if the target node is at the specified line
     */
    public boolean isAtLine(int lineNumber) {
        return this.nodeLineNumber == lineNumber;
    }
    
    /**
     * Check if this attempt was made using the specified transform type.
     * 
     * @param transformClass Transform class to check
     * @return true if the transform matches the specified class
     */
    public boolean usedTransform(Class<? extends Transform> transformClass) {
        return transform != null && transformClass.isInstance(transform);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TransformationAttempt{");
        sb.append("transform=").append(getTransformName());
        sb.append(", nodeType=").append(nodeType);
        sb.append(", nodeLineNumber=").append(nodeLineNumber);
        sb.append(", successful=").append(successful);
        sb.append(", executionTimeMs=").append(executionTimeMs);
        if (!successful && errorMessage != null) {
            sb.append(", errorMessage='").append(errorMessage).append('\'');
        }
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformationAttempt that = (TransformationAttempt) o;
        return successful == that.successful &&
                executionTimeMs == that.executionTimeMs &&
                nodeLineNumber == that.nodeLineNumber &&
                Objects.equals(transform, that.transform) &&
                Objects.equals(targetNode, that.targetNode) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(nodeType, that.nodeType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transform, targetNode, successful, errorMessage, timestamp, 
                          executionTimeMs, nodeLineNumber, nodeType);
    }
}