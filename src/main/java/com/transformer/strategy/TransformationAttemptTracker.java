package com.transformer.strategy;

import com.transformer.transform.Transform;
import org.eclipse.jdt.core.dom.ASTNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tracks multiple transformation attempts for analysis and feedback.
 * Provides statistics and filtering capabilities for transformation attempts.
 */
public class TransformationAttemptTracker {
    
    private final List<TransformationAttempt> attempts;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    
    /**
     * Creates a new transformation attempt tracker.
     */
    public TransformationAttemptTracker() {
        this.attempts = new ArrayList<>();
        this.startTime = LocalDateTime.now();
    }
    
    /**
     * Record a successful transformation attempt.
     * 
     * @param transform Transform that was applied
     * @param targetNode AST node that was transformed
     * @param executionTimeMs Time taken to execute the transformation
     * @param nodeLineNumber Line number of the target node
     */
    public void recordSuccess(Transform transform, ASTNode targetNode, long executionTimeMs, int nodeLineNumber) {
        attempts.add(TransformationAttempt.success(transform, targetNode, executionTimeMs, nodeLineNumber));
    }
    
    /**
     * Record a failed transformation attempt.
     * 
     * @param transform Transform that was attempted
     * @param targetNode AST node that was targeted
     * @param errorMessage Error message describing the failure
     * @param executionTimeMs Time taken before failure
     * @param nodeLineNumber Line number of the target node
     */
    public void recordFailure(Transform transform, ASTNode targetNode, String errorMessage, 
                            long executionTimeMs, int nodeLineNumber) {
        attempts.add(TransformationAttempt.failure(transform, targetNode, errorMessage, executionTimeMs, nodeLineNumber));
    }
    
    /**
     * Record a transformation attempt.
     * 
     * @param attempt TransformationAttempt to record
     */
    public void recordAttempt(TransformationAttempt attempt) {
        if (attempt != null) {
            attempts.add(attempt);
        }
    }
    
    /**
     * Mark the end of the transformation session.
     */
    public void markEnd() {
        this.endTime = LocalDateTime.now();
    }
    
    /**
     * Get all recorded attempts (unmodifiable list).
     * 
     * @return List of all transformation attempts
     */
    public List<TransformationAttempt> getAllAttempts() {
        return Collections.unmodifiableList(attempts);
    }
    
    /**
     * Get only successful attempts.
     * 
     * @return List of successful transformation attempts
     */
    public List<TransformationAttempt> getSuccessfulAttempts() {
        return attempts.stream()
                .filter(TransformationAttempt::isSuccessful)
                .collect(Collectors.toList());
    }
    
    /**
     * Get only failed attempts.
     * 
     * @return List of failed transformation attempts
     */
    public List<TransformationAttempt> getFailedAttempts() {
        return attempts.stream()
                .filter(attempt -> !attempt.isSuccessful())
                .collect(Collectors.toList());
    }
    
    /**
     * Get attempts that targeted nodes at specific line numbers.
     * 
     * @param lineNumbers Line numbers to filter by
     * @return List of attempts targeting the specified lines
     */
    public List<TransformationAttempt> getAttemptsAtLines(List<Integer> lineNumbers) {
        if (lineNumbers == null || lineNumbers.isEmpty()) {
            return new ArrayList<>();
        }
        
        return attempts.stream()
                .filter(attempt -> lineNumbers.contains(attempt.getNodeLineNumber()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get attempts using a specific transform type.
     * 
     * @param transformClass Transform class to filter by
     * @return List of attempts using the specified transform
     */
    public List<TransformationAttempt> getAttemptsByTransform(Class<? extends Transform> transformClass) {
        return attempts.stream()
                .filter(attempt -> attempt.usedTransform(transformClass))
                .collect(Collectors.toList());
    }
    
    /**
     * Get the total number of attempts.
     * 
     * @return Total number of transformation attempts
     */
    public int getTotalAttempts() {
        return attempts.size();
    }
    
    /**
     * Get the number of successful attempts.
     * 
     * @return Number of successful transformation attempts
     */
    public int getSuccessfulCount() {
        return (int) attempts.stream().filter(TransformationAttempt::isSuccessful).count();
    }
    
    /**
     * Get the number of failed attempts.
     * 
     * @return Number of failed transformation attempts
     */
    public int getFailedCount() {
        return getTotalAttempts() - getSuccessfulCount();
    }
    
    /**
     * Get the success rate as a percentage.
     * 
     * @return Success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        if (attempts.isEmpty()) {
            return 0.0;
        }
        return (double) getSuccessfulCount() / getTotalAttempts() * 100.0;
    }
    
    /**
     * Get the total execution time for all attempts.
     * 
     * @return Total execution time in milliseconds
     */
    public long getTotalExecutionTime() {
        return attempts.stream()
                .mapToLong(TransformationAttempt::getExecutionTimeMs)
                .sum();
    }
    
    /**
     * Get the average execution time per attempt.
     * 
     * @return Average execution time in milliseconds
     */
    public double getAverageExecutionTime() {
        if (attempts.isEmpty()) {
            return 0.0;
        }
        return (double) getTotalExecutionTime() / getTotalAttempts();
    }
    
    /**
     * Get a summary of transform usage.
     * 
     * @return Map of transform names to usage counts
     */
    public Map<String, Long> getTransformUsageSummary() {
        return attempts.stream()
                .collect(Collectors.groupingBy(
                    TransformationAttempt::getTransformName,
                    Collectors.counting()
                ));
    }
    
    /**
     * Get a summary of node type targeting.
     * 
     * @return Map of node types to targeting counts
     */
    public Map<String, Long> getNodeTypeTargetingSummary() {
        return attempts.stream()
                .collect(Collectors.groupingBy(
                    TransformationAttempt::getNodeType,
                    Collectors.counting()
                ));
    }
    
    /**
     * Get attempts grouped by line number.
     * 
     * @return Map of line numbers to lists of attempts
     */
    public Map<Integer, List<TransformationAttempt>> getAttemptsByLine() {
        return attempts.stream()
                .collect(Collectors.groupingBy(TransformationAttempt::getNodeLineNumber));
    }
    
    /**
     * Check if any attempts were successful.
     * 
     * @return true if at least one attempt was successful
     */
    public boolean hasSuccessfulAttempts() {
        return getSuccessfulCount() > 0;
    }
    
    /**
     * Check if all attempts were successful.
     * 
     * @return true if all attempts were successful (or no attempts were made)
     */
    public boolean allAttemptsSuccessful() {
        return attempts.isEmpty() || getFailedCount() == 0;
    }
    
    /**
     * Clear all recorded attempts.
     */
    public void clear() {
        attempts.clear();
    }
    
    /**
     * Get the start time of the tracking session.
     * 
     * @return Start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Get the end time of the tracking session.
     * 
     * @return End time (null if not marked as ended)
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * Check if the tracking session has ended.
     * 
     * @return true if the session has been marked as ended
     */
    public boolean hasEnded() {
        return endTime != null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TransformationAttemptTracker{");
        sb.append("totalAttempts=").append(getTotalAttempts());
        sb.append(", successful=").append(getSuccessfulCount());
        sb.append(", failed=").append(getFailedCount());
        sb.append(", successRate=").append(String.format("%.1f%%", getSuccessRate()));
        sb.append(", totalExecutionTime=").append(getTotalExecutionTime()).append("ms");
        sb.append(", averageExecutionTime=").append(String.format("%.1fms", getAverageExecutionTime()));
        sb.append(", startTime=").append(startTime);
        if (endTime != null) {
            sb.append(", endTime=").append(endTime);
        }
        sb.append('}');
        return sb.toString();
    }
}