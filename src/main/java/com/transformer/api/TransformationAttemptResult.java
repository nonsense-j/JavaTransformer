package com.transformer.api;

import java.time.LocalDateTime;

/**
 * Represents the result of a single transformation attempt.
 * Used to track individual attempts within a transformation operation.
 */
public class TransformationAttemptResult {
    
    private boolean success;
    private String transformName;
    private String nodeType;
    private int nodeLineNumber;
    private String errorMessage;
    private long attemptTimeMs;
    private LocalDateTime attemptTime;
    
    public TransformationAttemptResult() {
    }
    
    public TransformationAttemptResult(boolean success, String transformName) {
        this.success = success;
        this.transformName = transformName;
        this.attemptTime = LocalDateTime.now();
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getTransformName() {
        return transformName;
    }
    
    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    
    public int getNodeLineNumber() {
        return nodeLineNumber;
    }
    
    public void setNodeLineNumber(int nodeLineNumber) {
        this.nodeLineNumber = nodeLineNumber;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public long getAttemptTimeMs() {
        return attemptTimeMs;
    }
    
    public void setAttemptTimeMs(long attemptTimeMs) {
        this.attemptTimeMs = attemptTimeMs;
    }
    
    public LocalDateTime getAttemptTime() {
        return attemptTime;
    }
    
    public void setAttemptTime(LocalDateTime attemptTime) {
        this.attemptTime = attemptTime;
    }
    
    /**
     * Builder for creating TransformationAttemptResult instances.
     */
    public static class Builder {
        private TransformationAttemptResult result = new TransformationAttemptResult();
        
        public Builder success(boolean success) {
            result.setSuccess(success);
            return this;
        }
        
        public Builder transformName(String transformName) {
            result.setTransformName(transformName);
            return this;
        }
        
        public Builder nodeType(String nodeType) {
            result.setNodeType(nodeType);
            return this;
        }
        
        public Builder nodeLineNumber(int lineNumber) {
            result.setNodeLineNumber(lineNumber);
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            result.setErrorMessage(errorMessage);
            return this;
        }
        
        public Builder attemptTimeMs(long timeMs) {
            result.setAttemptTimeMs(timeMs);
            return this;
        }
        
        public Builder attemptTime(LocalDateTime time) {
            result.setAttemptTime(time);
            return this;
        }
        
        public TransformationAttemptResult build() {
            if (result.getAttemptTime() == null) {
                result.setAttemptTime(LocalDateTime.now());
            }
            return result;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return String.format("TransformationAttemptResult{success=%s, transform='%s', nodeType='%s', line=%d, time=%dms}", 
                success, transformName, nodeType, nodeLineNumber, attemptTimeMs);
    }
}