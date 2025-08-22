package com.transformer.strategy;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Location strategy that selects AST nodes based on specific target line numbers.
 * This strategy provides precise control over transformation locations by filtering
 * nodes to only those that exist on the specified target lines.
 */
public class TargetLocationStrategy implements LocationStrategy {
    
    private final List<Integer> targetLines;
    
    /**
     * Constructor for TargetLocationStrategy.
     * 
     * @param targetLines List of line numbers to target for transformation
     * @throws IllegalArgumentException if targetLines is null or contains negative numbers
     */
    public TargetLocationStrategy(List<Integer> targetLines) {
        this.targetLines = validateAndCopyTargetLines(targetLines);
    }
    
    /**
     * Get the strategy name for identification.
     * 
     * @return Strategy name "TARGET_LOCATION"
     */
    @Override
    public String getStrategyName() {
        return "TARGET_LOCATION";
    }
    
    /**
     * Select candidate nodes based on target line numbers.
     * This method uses the optimal signature specific to target-based selection.
     * 
     * @param wrapper TypeWrapper containing the AST and compilation unit
     * @param targetLines List of line numbers to target
     * @return List of AST nodes that exist on the target lines
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, List<Integer> targetLines) {
        validateParameters(wrapper, targetLines);
        
        List<ASTNode> candidateNodes = new ArrayList<>();
        List<ASTNode> allNodes = wrapper.getAllNodes();
        
        if (allNodes == null || allNodes.isEmpty()) {
            return candidateNodes;
        }
        
        // Filter nodes by target line numbers
        for (ASTNode node : allNodes) {
            int nodeLineNumber = wrapper.getCompilationUnit().getLineNumber(node.getStartPosition());
            
            if (targetLines.contains(nodeLineNumber)) {
                candidateNodes.add(node);
            }
        }
        
        return candidateNodes;
    }
    
    /**
     * Validate target lines parameter and create defensive copy.
     * 
     * @param targetLines List of target line numbers to validate
     * @return Defensive copy of validated target lines
     * @throws IllegalArgumentException if validation fails
     */
    private List<Integer> validateAndCopyTargetLines(List<Integer> targetLines) {
        if (targetLines == null) {
            throw new IllegalArgumentException("Target lines cannot be null");
        }
        
        List<Integer> copy = new ArrayList<>();
        for (Integer lineNumber : targetLines) {
            if (lineNumber == null) {
                throw new IllegalArgumentException("Target lines cannot contain null values");
            }
            if (lineNumber < 1) {
                throw new IllegalArgumentException("Target lines cannot contain negative or zero line numbers: " + lineNumber);
            }
            copy.add(lineNumber);
        }
        
        return copy;
    }
    
    /**
     * Validate parameters for selectCandidateNodes method.
     * 
     * @param wrapper TypeWrapper to validate
     * @param targetLines Target lines to validate
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validateParameters(TypeWrapper wrapper, List<Integer> targetLines) {
        if (wrapper == null) {
            throw new IllegalArgumentException("TypeWrapper cannot be null");
        }
        if (wrapper.getCompilationUnit() == null) {
            throw new IllegalArgumentException("CompilationUnit cannot be null");
        }
        if (targetLines == null) {
            throw new IllegalArgumentException("Target lines cannot be null");
        }
        
        // Validate target lines content
        for (Integer lineNumber : targetLines) {
            if (lineNumber == null) {
                throw new IllegalArgumentException("Target lines cannot contain null values");
            }
            if (lineNumber < 1) {
                throw new IllegalArgumentException("Target lines cannot contain negative or zero line numbers: " + lineNumber);
            }
        }
    }
    
    /**
     * Get the target lines for this strategy instance.
     * 
     * @return Defensive copy of target lines
     */
    public List<Integer> getTargetLines() {
        return new ArrayList<>(this.targetLines);
    }
}