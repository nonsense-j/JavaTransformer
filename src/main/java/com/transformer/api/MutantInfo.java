package com.transformer.api;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about a single mutant generated during transformation.
 * Tracks the transform applied, target node, and resulting code.
 */
public class MutantInfo {
    
    private final String transformName;
    private final ASTNode targetNode;
    private final String mutantCode;
    private final String outputFilePath;
    private final Map<String, Object> mutantMetadata;
    
    /**
     * Constructor for MutantInfo.
     * 
     * @param transformName Name of the transform that was applied
     * @param targetNode The AST node that was transformed
     * @param mutantCode The resulting code after transformation
     */
    public MutantInfo(String transformName, ASTNode targetNode, String mutantCode) {
        this(transformName, targetNode, mutantCode, null);
    }
    
    /**
     * Constructor for MutantInfo with output file path.
     * 
     * @param transformName Name of the transform that was applied
     * @param targetNode The AST node that was transformed
     * @param mutantCode The resulting code after transformation
     * @param outputFilePath Path where the mutant file was saved
     */
    public MutantInfo(String transformName, ASTNode targetNode, String mutantCode, String outputFilePath) {
        this.transformName = transformName;
        this.targetNode = targetNode;
        this.mutantCode = mutantCode;
        this.outputFilePath = outputFilePath;
        this.mutantMetadata = new HashMap<>();
        
        // Add basic metadata
        if (targetNode != null) {
            mutantMetadata.put("nodeType", targetNode.getClass().getSimpleName());
            mutantMetadata.put("nodePosition", targetNode.getStartPosition());
            mutantMetadata.put("nodeLength", targetNode.getLength());
        }
        mutantMetadata.put("timestamp", System.currentTimeMillis());
        if (outputFilePath != null) {
            mutantMetadata.put("outputFilePath", outputFilePath);
        }
    }
    
    /**
     * Constructor with additional metadata.
     * 
     * @param transformName Name of the transform that was applied
     * @param targetNode The AST node that was transformed
     * @param mutantCode The resulting code after transformation
     * @param outputFilePath Path where the mutant file was saved
     * @param additionalMetadata Additional metadata to include
     */
    public MutantInfo(String transformName, ASTNode targetNode, String mutantCode, String outputFilePath, Map<String, Object> additionalMetadata) {
        this(transformName, targetNode, mutantCode, outputFilePath);
        if (additionalMetadata != null) {
            this.mutantMetadata.putAll(additionalMetadata);
        }
    }
    
    public String getTransformName() {
        return transformName;
    }
    
    public ASTNode getTargetNode() {
        return targetNode;
    }
    
    public String getMutantCode() {
        return mutantCode;
    }
    
    public String getOutputFilePath() {
        return outputFilePath;
    }
    
    public Map<String, Object> getMutantMetadata() {
        return new HashMap<>(mutantMetadata);
    }
    
    public void addMetadata(String key, Object value) {
        mutantMetadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return mutantMetadata.get(key);
    }
    
    @Override
    public String toString() {
        return String.format("MutantInfo{transform='%s', nodeType='%s', codeLength=%d}", 
                transformName, 
                targetNode != null ? targetNode.getClass().getSimpleName() : "null",
                mutantCode != null ? mutantCode.length() : 0);
    }
}