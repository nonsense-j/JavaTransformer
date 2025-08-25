package com.transformer.strategy;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Location strategy that randomly selects candidate nodes for transformation.
 * This strategy selects nodes randomly from all available nodes,
 * following Statfier's TransformByRandomLocation() approach.
 */
public class RandomLocationStrategy implements LocationStrategy {
    
    private final Random random;
    
    /**
     * Creates a new RandomLocationStrategy with a default random seed.
     */
    public RandomLocationStrategy() {
        this.random = new Random();
    }
    
    /**
     * Creates a new RandomLocationStrategy with a specific random seed for reproducible results.
     * 
     * @param seed Random seed for reproducible node selection
     */
    public RandomLocationStrategy(long seed) {
        this.random = new Random(seed);
    }
    
    /**
     * Select candidate AST nodes for transformation using optimal signature.
     * This method uses only the parameters it actually needs.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param randomNodeCnt Number of random nodes to select
     * @return List of randomly selected candidate AST nodes
     */
    public List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, int randomNodeCnt) {
        if (wrapper == null) {
            return new ArrayList<>();
        }
        
        // Get all nodes from the AST
        List<ASTNode> allNodes = wrapper.getAllNodes();
        if (allNodes == null || allNodes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Select exactly randomNodeCnt random nodes (default: 5)
        int nodeCount = randomNodeCnt > 0 ? randomNodeCnt : 5;
        
        if (allNodes.size() <= nodeCount) {
            Collections.shuffle(allNodes, random);
            return new ArrayList<>(allNodes);
        }
        
        Collections.shuffle(allNodes, random);
        return allNodes.subList(0, nodeCount);
    }
    
    /**
     * Select a single random candidate node.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @return Single randomly selected candidate node, or null if none available
     */
    public ASTNode selectSingleCandidateNode(TypeWrapper wrapper) {
        List<ASTNode> candidates = selectCandidateNodes(wrapper, 1);
        return candidates.isEmpty() ? null : candidates.get(0);
    }
    
    /**
     * Get the random instance used by this strategy (for testing purposes).
     * 
     * @return Random instance
     */
    protected Random getRandom() {
        return random;
    }
    
    @Override
    public String getStrategyName() {
        return "RANDOM_LOCATION";
    }
    
    @Override
    public String toString() {
        return "RandomLocationStrategy{strategyName=" + getStrategyName() + "}";
    }
}