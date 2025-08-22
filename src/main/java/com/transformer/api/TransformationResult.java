package com.transformer.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of a transformation operation containing success status,
 * transformed code, applied transformations, and detailed metadata.
 * 
 * This class tracks the complete transformation process including:
 * - Success/failure status
 * - Applied transformation sequence
 * - Error messages and details
 * - Performance and execution metadata
 * - Transformation attempt tracking
 */
public class TransformationResult {
    
    private boolean success;
    private String transformedCode;
    private List<String> appliedTransforms;
    private String errorMessage;
    private List<String> errorMessages;
    private Map<String, Object> metadata;
    
    // Multiple mutants support
    private List<MutantInfo> mutants;
    
    // Enhanced tracking fields
    private int totalAttempts;
    private int successfulAttempts;
    private int failedAttempts;
    private long executionTimeMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<TransformationAttemptResult> attemptResults;
    
    public TransformationResult() {
        this.appliedTransforms = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.attemptResults = new ArrayList<>();
        this.mutants = new ArrayList<>();
        this.startTime = LocalDateTime.now();
    }
    
    public TransformationResult(boolean success) {
        this();
        this.success = success;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getTransformedCode() {
        return transformedCode;
    }
    
    public void setTransformedCode(String transformedCode) {
        this.transformedCode = transformedCode;
    }
    
    public List<String> getAppliedTransforms() {
        return appliedTransforms;
    }
    
    public void setAppliedTransforms(List<String> appliedTransforms) {
        this.appliedTransforms = appliedTransforms;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            addErrorMessage(errorMessage);
        }
    }
    
    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages);
    }
    
    public void addErrorMessage(String errorMessage) {
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            this.errorMessages.add(errorMessage);
        }
    }
    
    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages != null ? new ArrayList<>(errorMessages) : new ArrayList<>();
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    // Multiple mutants support methods
    public List<MutantInfo> getMutants() {
        return new ArrayList<>(mutants);
    }
    
    public void setMutants(List<MutantInfo> mutants) {
        this.mutants = mutants != null ? new ArrayList<>(mutants) : new ArrayList<>();
        // Update appliedTransforms based on mutants
        if (!this.mutants.isEmpty()) {
            this.appliedTransforms = this.mutants.stream()
                .map(MutantInfo::getTransformName)
                .distinct()
                .collect(Collectors.toList());
        }
    }
    
    public void addMutant(MutantInfo mutant) {
        if (mutant != null) {
            this.mutants.add(mutant);
            // Add to applied transforms if not already present
            if (mutant.getTransformName() != null && 
                !this.appliedTransforms.contains(mutant.getTransformName())) {
                this.appliedTransforms.add(mutant.getTransformName());
            }
        }
    }
    
    public int getMutantCount() {
        return mutants.size();
    }
    
    public boolean hasMutants() {
        return !mutants.isEmpty();
    }
    
    // Enhanced tracking getters and setters
    public int getTotalAttempts() {
        return totalAttempts;
    }
    
    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }
    
    public int getSuccessfulAttempts() {
        return successfulAttempts;
    }
    
    public void setSuccessfulAttempts(int successfulAttempts) {
        this.successfulAttempts = successfulAttempts;
    }
    
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        if (this.startTime != null && endTime != null) {
            this.executionTimeMs = java.time.Duration.between(this.startTime, endTime).toMillis();
        }
    }
    
    public List<TransformationAttemptResult> getAttemptResults() {
        return new ArrayList<>(attemptResults);
    }
    
    public void setAttemptResults(List<TransformationAttemptResult> attemptResults) {
        this.attemptResults = attemptResults != null ? new ArrayList<>(attemptResults) : new ArrayList<>();
    }
    
    public void addAttemptResult(TransformationAttemptResult attemptResult) {
        if (attemptResult != null) {
            this.attemptResults.add(attemptResult);
            this.totalAttempts++;
            if (attemptResult.isSuccess()) {
                this.successfulAttempts++;
                // Add to applied transforms if not already present
                if (attemptResult.getTransformName() != null && 
                    !this.appliedTransforms.contains(attemptResult.getTransformName())) {
                    this.appliedTransforms.add(attemptResult.getTransformName());
                }
            } else {
                this.failedAttempts++;
                if (attemptResult.getErrorMessage() != null) {
                    addErrorMessage(attemptResult.getErrorMessage());
                }
            }
        }
    }
    
    /**
     * Marks the transformation as complete and calculates final metrics.
     */
    public void complete() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.executionTimeMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
        
        // Update metadata with final statistics
        addMetadata("totalAttempts", totalAttempts);
        addMetadata("successfulAttempts", successfulAttempts);
        addMetadata("failedAttempts", failedAttempts);
        addMetadata("executionTimeMs", executionTimeMs);
        addMetadata("successRate", totalAttempts > 0 ? (double) successfulAttempts / totalAttempts : 0.0);
        addMetadata("appliedTransformCount", appliedTransforms.size());
        addMetadata("errorCount", errorMessages.size());
        addMetadata("mutantCount", mutants.size());
    }
    
    /**
     * Returns true if any transformations were successfully applied.
     */
    public boolean hasAppliedTransforms() {
        return !appliedTransforms.isEmpty();
    }
    
    /**
     * Returns true if there were any errors during transformation.
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
    
    /**
     * Returns the success rate as a percentage (0.0 to 1.0).
     */
    public double getSuccessRate() {
        return totalAttempts > 0 ? (double) successfulAttempts / totalAttempts : 0.0;
    }
    
    /**
     * Returns a summary of the transformation result.
     */
    public String getSummary() {
        return String.format("TransformationResult{success=%s, attempts=%d/%d, transforms=%d, mutants=%d, errors=%d, time=%dms}", 
                success, successfulAttempts, totalAttempts, appliedTransforms.size(), 
                mutants.size(), errorMessages.size(), executionTimeMs);
    }
    
    /**
     * Builder for creating TransformationResult instances.
     */
    public static class Builder {
        private TransformationResult result = new TransformationResult();
        
        public Builder success(boolean success) {
            result.setSuccess(success);
            return this;
        }
        
        public Builder transformedCode(String code) {
            result.setTransformedCode(code);
            return this;
        }
        
        public Builder appliedTransforms(List<String> transforms) {
            result.setAppliedTransforms(transforms);
            return this;
        }
        
        public Builder errorMessage(String message) {
            result.setErrorMessage(message);
            return this;
        }
        
        public Builder errorMessages(List<String> messages) {
            result.setErrorMessages(messages);
            return this;
        }
        
        public Builder addErrorMessage(String message) {
            result.addErrorMessage(message);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            result.setMetadata(metadata);
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            result.addMetadata(key, value);
            return this;
        }
        
        public Builder totalAttempts(int attempts) {
            result.setTotalAttempts(attempts);
            return this;
        }
        
        public Builder successfulAttempts(int attempts) {
            result.setSuccessfulAttempts(attempts);
            return this;
        }
        
        public Builder failedAttempts(int attempts) {
            result.setFailedAttempts(attempts);
            return this;
        }
        
        public Builder executionTimeMs(long timeMs) {
            result.setExecutionTimeMs(timeMs);
            return this;
        }
        
        public Builder startTime(LocalDateTime startTime) {
            result.setStartTime(startTime);
            return this;
        }
        
        public Builder endTime(LocalDateTime endTime) {
            result.setEndTime(endTime);
            return this;
        }
        
        public Builder attemptResults(List<TransformationAttemptResult> attemptResults) {
            result.setAttemptResults(attemptResults);
            return this;
        }
        
        public Builder addAttemptResult(TransformationAttemptResult attemptResult) {
            result.addAttemptResult(attemptResult);
            return this;
        }
        
        public Builder mutants(List<MutantInfo> mutants) {
            result.setMutants(mutants);
            return this;
        }
        
        public Builder addMutant(MutantInfo mutant) {
            result.addMutant(mutant);
            return this;
        }
        
        public TransformationResult build() {
            // Auto-complete if not already done
            if (result.getEndTime() == null) {
                result.complete();
            }
            return result;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}