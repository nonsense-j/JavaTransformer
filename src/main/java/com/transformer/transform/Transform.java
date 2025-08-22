package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * Abstract base class for all code transformations.
 * Simplified interface focused on transformation logic without Statfier-specific dependencies.
 */
public abstract class Transform {

    /**
     * Check if the given node can be transformed by this transformation.
     * 
     * @param wrapper The TypeWrapper containing the AST
     * @param node The AST node to check
     * @return List of nodes that can be transformed (may include the node itself or related nodes)
     */
    public abstract List<ASTNode> check(TypeWrapper wrapper, ASTNode node);

    /**
     * Apply the transformation to the target node.
     * 
     * @param targetNode The node to transform
     * @param wrapper The TypeWrapper containing the AST
     * @param brotherNode Related node (context-dependent, may be null)
     * @param sourceNode Source node for the transformation (context-dependent, may be null)
     * @return true if transformation was successful, false otherwise
     */
    public abstract boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherNode, ASTNode sourceNode);

    /**
     * Get the unique identifier for this transformation.
     * 
     * @return The transformation name/identifier
     */
    public String getIndex() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get the display name for this transformation.
     * 
     * @return The transformation display name
     */
    public String getName() {
        return getIndex();
    }

    /**
     * Get a description of what this transformation does.
     * 
     * @return Description of the transformation
     */
    public String getDescription() {
        return "Code transformation: " + getName();
    }
}