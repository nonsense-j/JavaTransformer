package com.transformer.api;

import com.transformer.strategy.LocationStrategy;

/**
 * Request object for transformation operations.
 * Contains input/output paths, transformation details, strategy configuration, and bug information.
 */
public class TransformationRequest {
    
    private String inputPath;
    private String outputPath;
    private String transformName; // null for location-based transformations
    private LocationStrategy strategy;
    private int maxAttempts;
    private BugInformation bugInfo;
    
    public TransformationRequest() {
    }
    
    public TransformationRequest(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }
    
    public TransformationRequest(String transformName, String inputPath, String outputPath) {
        this.transformName = transformName;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }
    
    public String getInputPath() {
        return inputPath;
    }
    
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public String getTransformName() {
        return transformName;
    }
    
    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }
    
    public LocationStrategy getStrategy() {
        return strategy;
    }
    
    public void setStrategy(LocationStrategy strategy) {
        this.strategy = strategy;
    }
    
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public BugInformation getBugInfo() {
        return bugInfo;
    }
    
    public void setBugInfo(BugInformation bugInfo) {
        this.bugInfo = bugInfo;
    }
    
    /**
     * Builder for creating TransformationRequest instances.
     */
    public static class Builder {
        private TransformationRequest request = new TransformationRequest();
        
        public Builder inputPath(String inputPath) {
            request.setInputPath(inputPath);
            return this;
        }
        
        public Builder outputPath(String outputPath) {
            request.setOutputPath(outputPath);
            return this;
        }
        
        public Builder transformName(String transformName) {
            request.setTransformName(transformName);
            return this;
        }
        
        public Builder strategy(LocationStrategy strategy) {
            request.setStrategy(strategy);
            return this;
        }
        
        public Builder maxAttempts(int maxAttempts) {
            request.setMaxAttempts(maxAttempts);
            return this;
        }
        
        public Builder bugInfo(BugInformation bugInfo) {
            request.setBugInfo(bugInfo);
            return this;
        }
        
        public TransformationRequest build() {
            validateRequest();
            return request;
        }
        
        /**
         * Validates the transformation request for consistency.
         * 
         * @throws IllegalArgumentException if validation fails
         */
        private void validateRequest() {
            if (request.getInputPath() == null || request.getInputPath().trim().isEmpty()) {
                throw new IllegalArgumentException("Input path cannot be null or empty");
            }
            
            if (request.getOutputPath() == null || request.getOutputPath().trim().isEmpty()) {
                throw new IllegalArgumentException("Output path cannot be null or empty");
            }
            
            if (request.getMaxAttempts() < 0) {
                throw new IllegalArgumentException("Max attempts cannot be negative");
            }
            
            // Validate bug information if provided
            if (request.getBugInfo() != null) {
                // Bug information validation is handled by BugInformation class itself
                // Additional validation can be added here if needed
            }
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}