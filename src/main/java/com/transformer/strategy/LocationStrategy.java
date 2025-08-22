package com.transformer.strategy;

/**
 * Strategy interface for selecting candidate AST nodes for transformation.
 * Different implementations provide different approaches for node selection.
 * 
 * Each strategy implementation defines its own optimal selectCandidateNodes method
 * with the specific parameters it needs, eliminating unused parameter overhead.
 */
public interface LocationStrategy {
    
    /**
     * Get the name of this strategy for identification purposes.
     * 
     * @return Strategy name
     */
    String getStrategyName();
}