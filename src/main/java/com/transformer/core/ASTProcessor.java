package com.transformer.core;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.text.edits.TextEdit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

/**
 * ASTProcessor handles file parsing and code generation operations.
 * Provides utilities for working with Java source files and AST manipulation.
 */
public class ASTProcessor {

    /**
     * Parse a Java file and create a TypeWrapper for AST manipulation.
     * 
     * @param filePath Path to the Java source file
     * @return TypeWrapper containing the parsed AST
     * @throws IOException if file cannot be read
     */
    public TypeWrapper parseJavaFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        if (!filePath.toLowerCase().endsWith(".java")) {
            throw new IllegalArgumentException("File must be a Java source file (.java): " + filePath);
        }
        
        try {
            return new TypeWrapper(filePath);
        } catch (Exception e) {
            throw new IOException("Failed to parse Java file: " + filePath, e);
        }
    }

    /**
     * Parse Java source code from a string and create a TypeWrapper.
     * 
     * @param fileName Name of the file (for metadata)
     * @param filePath Path where the file would be located (for metadata)
     * @param sourceCode Java source code as string
     * @return TypeWrapper containing the parsed AST
     */
    public TypeWrapper parseJavaSource(String fileName, String filePath, String sourceCode) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        
        if (sourceCode == null) {
            throw new IllegalArgumentException("Source code cannot be null");
        }
        
        try {
            return new TypeWrapper(fileName, filePath, sourceCode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Java source code", e);
        }
    }

    /**
     * Write the transformed code from a TypeWrapper to the specified output file.
     * 
     * @param wrapper TypeWrapper containing the transformed AST
     * @param outputPath Path where the transformed code should be written
     * @return true if write was successful, false otherwise
     */
    public boolean writeTransformedCode(TypeWrapper wrapper, String outputPath) {
        if (wrapper == null) {
            throw new IllegalArgumentException("TypeWrapper cannot be null");
        }
        
        return writeTransformedCodeString(wrapper.getCode(), outputPath);
    }
    
    /**
     * Write a string containing Java code to the specified output file.
     * 
     * @param code Java source code to write
     * @param outputPath Path where the code should be written
     * @return true if write was successful, false otherwise
     */
    public boolean writeTransformedCodeString(String code, String outputPath) {
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }
        
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }
        
        try {
            // Ensure parent directories exist
            Path path = Paths.get(outputPath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Write the code to file
            Files.write(path, code.getBytes(StandardCharsets.UTF_8));
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to write code to: " + outputPath + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Apply AST rewrite operations to update the TypeWrapper's document.
     * This method should be called after making AST modifications.
     * 
     * @param wrapper TypeWrapper to rewrite
     */
    public void rewriteAST(TypeWrapper wrapper) {
        if (wrapper == null) {
            throw new IllegalArgumentException("TypeWrapper cannot be null");
        }
        
        try {
            wrapper.rewriteAST();
        } catch (Exception e) {
            throw new RuntimeException("Failed to rewrite AST", e);
        }
    }

    /**
     * Find an AST node at a specific line and column position.
     * 
     * @param wrapper TypeWrapper containing the AST
     * @param line Line number (1-based)
     * @param column Column number (1-based)
     * @return ASTNode at the specified position, or null if not found
     */
    public ASTNode findNodeByPosition(TypeWrapper wrapper, int line, int column) {
        if (wrapper == null) {
            throw new IllegalArgumentException("TypeWrapper cannot be null");
        }
        
        if (line < 1 || column < 1) {
            throw new IllegalArgumentException("Line and column numbers must be positive");
        }
        
        CompilationUnit cu = wrapper.getCompilationUnit();
        if (cu == null) {
            return null;
        }
        
        // Convert line/column to character position
        int position = cu.getPosition(line - 1, column - 1);
        if (position < 0) {
            return null;
        }
        
        // Find the node at this position
        return findNodeAtPosition(cu, position);
    }

    /**
     * Find nodes at a specific line number.
     * 
     * @param wrapper TypeWrapper containing the AST
     * @param lineNumber Line number (1-based)
     * @return List of ASTNodes that start at or contain the specified line
     */
    public List<ASTNode> findNodesAtLine(TypeWrapper wrapper, int lineNumber) {
        if (wrapper == null) {
            throw new IllegalArgumentException("TypeWrapper cannot be null");
        }
        
        if (lineNumber < 1) {
            throw new IllegalArgumentException("Line number must be positive");
        }
        
        List<ASTNode> nodesAtLine = new ArrayList<>();
        CompilationUnit cu = wrapper.getCompilationUnit();
        
        if (cu == null) {
            return nodesAtLine;
        }
        
        // Get all nodes and check which ones are at the specified line
        List<ASTNode> allNodes = wrapper.getAllNodes();
        for (ASTNode node : allNodes) {
            int nodeStartLine = cu.getLineNumber(node.getStartPosition());
            int nodeEndLine = cu.getLineNumber(node.getStartPosition() + node.getLength() - 1);
            
            // Check if the line falls within the node's range
            if (lineNumber >= nodeStartLine && lineNumber <= nodeEndLine) {
                nodesAtLine.add(node);
            }
        }
        
        return nodesAtLine;
    }

    /**
     * Find nodes within a radius of a specific line number.
     * 
     * @param wrapper TypeWrapper containing the AST
     * @param centerLine Center line number (1-based)
     * @param radius Number of lines above and below to include
     * @return List of ASTNodes within the specified radius
     */
    public List<ASTNode> findNodesNearLine(TypeWrapper wrapper, int centerLine, int radius) {
        if (wrapper == null) {
            throw new IllegalArgumentException("TypeWrapper cannot be null");
        }
        
        if (centerLine < 1 || radius < 0) {
            throw new IllegalArgumentException("Center line must be positive and radius must be non-negative");
        }
        
        List<ASTNode> nearbyNodes = new ArrayList<>();
        CompilationUnit cu = wrapper.getCompilationUnit();
        
        if (cu == null) {
            return nearbyNodes;
        }
        
        int startLine = Math.max(1, centerLine - radius);
        int endLine = centerLine + radius;
        
        // Get all nodes and check which ones fall within the range
        List<ASTNode> allNodes = wrapper.getAllNodes();
        for (ASTNode node : allNodes) {
            int nodeStartLine = cu.getLineNumber(node.getStartPosition());
            int nodeEndLine = cu.getLineNumber(node.getStartPosition() + node.getLength());
            
            // Check if the node overlaps with our target range
            if (!(nodeEndLine < startLine || nodeStartLine > endLine)) {
                nearbyNodes.add(node);
            }
        }
        
        return nearbyNodes;
    }

    /**
     * Validate that a Java source file is syntactically correct.
     * 
     * @param filePath Path to the Java source file
     * @return true if the file is valid Java code, false otherwise
     */
    public boolean validateJavaFile(String filePath) {
        try {
            TypeWrapper wrapper = parseJavaFile(filePath);
            return wrapper.getCompilationUnit() != null && wrapper.getCompilationUnit().getProblems().length == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to find a node at a specific character position.
     */
    private ASTNode findNodeAtPosition(ASTNode root, int position) {
        if (root == null) {
            return null;
        }
        
        int start = root.getStartPosition();
        int end = start + root.getLength();
        
        // Check if position is within this node
        if (position < start || position >= end) {
            return null;
        }
        
        // Check children first (more specific nodes)
        List<?> children = root.structuralPropertiesForType();
        for (Object property : children) {
            if (property instanceof org.eclipse.jdt.core.dom.StructuralPropertyDescriptor) {
                Object value = root.getStructuralProperty((org.eclipse.jdt.core.dom.StructuralPropertyDescriptor) property);
                
                if (value instanceof ASTNode) {
                    ASTNode childResult = findNodeAtPosition((ASTNode) value, position);
                    if (childResult != null) {
                        return childResult;
                    }
                } else if (value instanceof List) {
                    for (Object item : (List<?>) value) {
                        if (item instanceof ASTNode) {
                            ASTNode childResult = findNodeAtPosition((ASTNode) item, position);
                            if (childResult != null) {
                                return childResult;
                            }
                        }
                    }
                }
            }
        }
        
        // If no child contains the position, return this node
        return root;
    }
}