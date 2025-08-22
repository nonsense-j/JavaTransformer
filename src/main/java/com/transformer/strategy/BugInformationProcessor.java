package com.transformer.strategy;

import com.transformer.api.BugInformation;
import com.transformer.core.TypeWrapper;
import com.transformer.transform.Transform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Processes bug information to guide AST node selection for transformations.
 * Provides methods to find nodes at specific line numbers and within a radius of bug lines.
 */
public class BugInformationProcessor {
    
    private static final int DEFAULT_RADIUS = 3;
    
    /**
     * Select AST nodes near the specified bug lines for guided transformations.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param availableTransforms List of transforms that can be applied
     * @param bugLines List of line numbers where bugs were detected
     * @return List of candidate AST nodes for transformation
     */
    public List<ASTNode> selectNodesNearBugLines(TypeWrapper wrapper, List<Transform> availableTransforms, 
                                                List<Integer> bugLines) {
        return selectNodesNearBugLines(wrapper, availableTransforms, bugLines, DEFAULT_RADIUS);
    }
    
    /**
     * Select AST nodes near the specified bug lines with custom radius.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param availableTransforms List of transforms that can be applied
     * @param bugLines List of line numbers where bugs were detected
     * @param radius Number of lines around each bug line to consider
     * @return List of candidate AST nodes for transformation
     */
    public List<ASTNode> selectNodesNearBugLines(TypeWrapper wrapper, List<Transform> availableTransforms, 
                                                List<Integer> bugLines, int radius) {
        if (bugLines == null || bugLines.isEmpty()) {
            throw new IllegalArgumentException("Bug lines cannot be null or empty for guided selection");
        }
        
        Set<ASTNode> candidateNodes = new HashSet<>();
        
        // Find nodes at exact bug lines first (highest priority)
        for (Integer bugLine : bugLines) {
            List<ASTNode> nodesAtLine = findNodesAtLine(wrapper, bugLine);
            candidateNodes.addAll(nodesAtLine);
        }
        
        // Find nodes within radius of bug lines (lower priority)
        for (Integer bugLine : bugLines) {
            List<ASTNode> nearbyNodes = findNodesNearLine(wrapper, bugLine, radius);
            candidateNodes.addAll(nearbyNodes);
        }
        
        // Filter nodes that can be transformed by available transforms
        return filterTransformableNodes(new ArrayList<>(candidateNodes), availableTransforms, wrapper);
    }
    
    /**
     * Find AST nodes that start at the specified line number.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param lineNumber Line number to search for nodes
     * @return List of AST nodes starting at the specified line
     */
    public List<ASTNode> findNodesAtLine(TypeWrapper wrapper, int lineNumber) {
        List<ASTNode> nodesAtLine = new ArrayList<>();
        CompilationUnit cu = wrapper.getCompilationUnit();
        
        if (cu == null) {
            return nodesAtLine;
        }
        
        // Check all nodes in the AST
        List<ASTNode> allNodes = wrapper.getAllNodes();
        for (ASTNode node : allNodes) {
            int nodeLineNumber = cu.getLineNumber(node.getStartPosition());
            if (nodeLineNumber == lineNumber) {
                nodesAtLine.add(node);
            }
        }
        
        // Also check child nodes for more granular matching
        for (ASTNode node : allNodes) {
            List<ASTNode> childNodes = TypeWrapper.getChildrenNodes(node);
            for (ASTNode childNode : childNodes) {
                int childLineNumber = cu.getLineNumber(childNode.getStartPosition());
                if (childLineNumber == lineNumber && !nodesAtLine.contains(childNode)) {
                    nodesAtLine.add(childNode);
                }
            }
        }
        
        return nodesAtLine;
    }
    
    /**
     * Find AST nodes within a specified radius of the given line number.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param centerLine Center line number
     * @param radius Number of lines above and below to include
     * @return List of AST nodes within the specified radius
     */
    public List<ASTNode> findNodesNearLine(TypeWrapper wrapper, int centerLine, int radius) {
        List<ASTNode> nearbyNodes = new ArrayList<>();
        CompilationUnit cu = wrapper.getCompilationUnit();
        
        if (cu == null || radius < 0) {
            return nearbyNodes;
        }
        
        int startLine = Math.max(1, centerLine - radius);
        int endLine = centerLine + radius;
        
        // Check all nodes in the AST
        List<ASTNode> allNodes = wrapper.getAllNodes();
        for (ASTNode node : allNodes) {
            int nodeLineNumber = cu.getLineNumber(node.getStartPosition());
            if (nodeLineNumber >= startLine && nodeLineNumber <= endLine) {
                nearbyNodes.add(node);
            }
        }
        
        // Also check child nodes
        for (ASTNode node : allNodes) {
            List<ASTNode> childNodes = TypeWrapper.getChildrenNodes(node);
            for (ASTNode childNode : childNodes) {
                int childLineNumber = cu.getLineNumber(childNode.getStartPosition());
                if (childLineNumber >= startLine && childLineNumber <= endLine && !nearbyNodes.contains(childNode)) {
                    nearbyNodes.add(childNode);
                }
            }
        }
        
        return nearbyNodes;
    }
    
    /**
     * Filter nodes to only include those that can be transformed by available transforms.
     * 
     * @param nodes List of candidate nodes to filter
     * @param availableTransforms List of available transforms
     * @param wrapper TypeWrapper containing the AST (needed for transform checks)
     * @return List of nodes that can be transformed
     */
    public List<ASTNode> filterTransformableNodes(List<ASTNode> nodes, List<Transform> availableTransforms, TypeWrapper wrapper) {
        if (nodes == null || nodes.isEmpty() || availableTransforms == null || availableTransforms.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ASTNode> transformableNodes = new ArrayList<>();
        
        for (ASTNode node : nodes) {
            for (Transform transform : availableTransforms) {
                try {
                    List<ASTNode> checkResult = transform.check(wrapper, node);
                    if (checkResult != null && !checkResult.isEmpty()) {
                        transformableNodes.add(node);
                        break; // Node can be transformed by at least one transform
                    }
                } catch (Exception e) {
                    // If check fails, skip this transform for this node
                    continue;
                }
            }
        }
        
        return transformableNodes;
    }
    
    /**
     * Filter nodes to only include those that can be transformed by available transforms.
     * This is a convenience method that creates a temporary TypeWrapper for checking.
     * 
     * @param nodes List of candidate nodes to filter
     * @param availableTransforms List of available transforms
     * @return List of nodes that can be transformed
     * @deprecated Use filterTransformableNodes(nodes, transforms, wrapper) for better performance
     */
    @Deprecated
    public List<ASTNode> filterTransformableNodes(List<ASTNode> nodes, List<Transform> availableTransforms) {
        if (nodes == null || nodes.isEmpty() || availableTransforms == null || availableTransforms.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ASTNode> transformableNodes = new ArrayList<>();
        
        for (ASTNode node : nodes) {
            for (Transform transform : availableTransforms) {
                try {
                    // Use null wrapper for basic compatibility check
                    // This may not work for all transforms but provides backward compatibility
                    List<ASTNode> checkResult = transform.check(null, node);
                    if (checkResult != null && !checkResult.isEmpty()) {
                        transformableNodes.add(node);
                        break; // Node can be transformed by at least one transform
                    }
                } catch (Exception e) {
                    // If check fails, skip this transform for this node
                    continue;
                }
            }
        }
        
        return transformableNodes;
    }
    
    /**
     * Create a mapping from line numbers to AST nodes for efficient lookup.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @return Map from line numbers to lists of nodes starting at those lines
     */
    public java.util.Map<Integer, List<ASTNode>> createLineToNodeMapping(TypeWrapper wrapper) {
        java.util.Map<Integer, List<ASTNode>> lineToNodes = new java.util.HashMap<>();
        CompilationUnit cu = wrapper.getCompilationUnit();
        
        if (cu == null) {
            return lineToNodes;
        }
        
        List<ASTNode> allNodes = wrapper.getAllNodes();
        
        // Map all primary nodes
        for (ASTNode node : allNodes) {
            int lineNumber = cu.getLineNumber(node.getStartPosition());
            lineToNodes.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(node);
        }
        
        // Map child nodes as well
        for (ASTNode node : allNodes) {
            List<ASTNode> childNodes = TypeWrapper.getChildrenNodes(node);
            for (ASTNode childNode : childNodes) {
                int lineNumber = cu.getLineNumber(childNode.getStartPosition());
                List<ASTNode> nodesAtLine = lineToNodes.computeIfAbsent(lineNumber, k -> new ArrayList<>());
                if (!nodesAtLine.contains(childNode)) {
                    nodesAtLine.add(childNode);
                }
            }
        }
        
        return lineToNodes;
    }
    
    /**
     * Get position information for a node (line and column numbers).
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param node AST node to get position for
     * @return NodePosition containing line and column information
     */
    public NodePosition getNodePosition(TypeWrapper wrapper, ASTNode node) {
        CompilationUnit cu = wrapper.getCompilationUnit();
        if (cu == null || node == null) {
            return new NodePosition(-1, -1);
        }
        
        int startPosition = node.getStartPosition();
        int lineNumber = cu.getLineNumber(startPosition);
        int columnNumber = cu.getColumnNumber(startPosition);
        
        return new NodePosition(lineNumber, columnNumber);
    }
    
    /**
     * Find the closest transformable nodes to the specified bug lines.
     * This method prioritizes nodes closer to bug lines over those further away.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param availableTransforms List of available transforms
     * @param bugLines List of bug line numbers
     * @param maxNodes Maximum number of nodes to return
     * @return List of closest transformable nodes, ordered by proximity to bug lines
     */
    public List<ASTNode> findClosestTransformableNodes(TypeWrapper wrapper, List<Transform> availableTransforms,
                                                      List<Integer> bugLines, int maxNodes) {
        if (bugLines == null || bugLines.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ASTNode> allTransformableNodes = new ArrayList<>();
        
        // Start with radius 0 (exact line matches) and expand outward
        for (int radius = 0; radius <= 10; radius++) {
            for (Integer bugLine : bugLines) {
                List<ASTNode> nodesAtRadius;
                if (radius == 0) {
                    nodesAtRadius = findNodesAtLine(wrapper, bugLine);
                } else {
                    // Find nodes at exactly this radius (not within)
                    List<ASTNode> innerNodes = findNodesNearLine(wrapper, bugLine, radius - 1);
                    List<ASTNode> outerNodes = findNodesNearLine(wrapper, bugLine, radius);
                    nodesAtRadius = outerNodes.stream()
                        .filter(node -> !innerNodes.contains(node))
                        .collect(Collectors.toList());
                }
                
                List<ASTNode> transformableAtRadius = filterTransformableNodes(nodesAtRadius, availableTransforms, wrapper);
                allTransformableNodes.addAll(transformableAtRadius);
                
                if (allTransformableNodes.size() >= maxNodes) {
                    return allTransformableNodes.subList(0, maxNodes);
                }
            }
        }
        
        return allTransformableNodes;
    }
    
    /**
     * Validate bug information for processing.
     * 
     * @param bugInfo Bug information to validate
     * @throws IllegalArgumentException if bug information is invalid
     */
    public void validateBugInformation(BugInformation bugInfo) {
        if (bugInfo == null) {
            throw new IllegalArgumentException("Bug information cannot be null");
        }
        
        if (!bugInfo.isValidForGuidedTransformation()) {
            throw new IllegalArgumentException("Bug information is not valid for guided transformation: " + bugInfo);
        }
        
        for (Integer bugLine : bugInfo.getBugLines()) {
            if (bugLine <= 0) {
                throw new IllegalArgumentException("Bug line numbers must be positive, found: " + bugLine);
            }
        }
    }
    
    /**
     * Simple data class to hold node position information.
     */
    public static class NodePosition {
        private final int lineNumber;
        private final int columnNumber;
        
        public NodePosition(int lineNumber, int columnNumber) {
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public int getColumnNumber() {
            return columnNumber;
        }
        
        @Override
        public String toString() {
            return "NodePosition{line=" + lineNumber + ", column=" + columnNumber + "}";
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodePosition that = (NodePosition) o;
            return lineNumber == that.lineNumber && columnNumber == that.columnNumber;
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(lineNumber, columnNumber);
        }
    }
}