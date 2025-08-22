package com.transformer.core;

import com.transformer.api.TransformationResult;
import com.transformer.api.BugInformation;
import com.transformer.api.MutantInfo;
import com.transformer.transform.Transform;
import com.transformer.strategy.LocationStrategy;
import com.transformer.strategy.RandomLocationStrategy;
import com.transformer.strategy.GuidedLocationStrategy;
import com.transformer.strategy.TargetLocationStrategy;
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Core transformation engine responsible for executing transformation operations.
 * 
 * Implements the correct workflow:
 * 1. For RandomStrategy: Select randomCnt (default 5) random nodes, then try all transforms on each node
 * 2. For GuidedStrategy: Select candidate nodes based on bug lines, then try all transforms on each node  
 * 3. When specific transform is provided: Only use that transform instead of all transforms
 */
public class TransformationEngine {

    private final TransformRegistry transformRegistry;
    private final ASTProcessor astProcessor;

    /**
     * Constructor for TransformationEngine.
     * 
     * @param transformRegistry Registry containing available transforms
     */
    public TransformationEngine(TransformRegistry transformRegistry) {
        this.transformRegistry = transformRegistry;
        this.astProcessor = new ASTProcessor();
    }

    /**
     * Execute random transformation workflow.
     * 
     * @param inputPath Path to input Java file
     * @param outputPath Path for output file
     * @param transformName Specific transform to apply (null = try all transforms)
     * @param randomCnt Number of random nodes to select (default 5)
     * @return TransformationResult containing all generated mutants
     */
    public TransformationResult executeRandomTransformation(String inputPath, String outputDir, 
            String transformName, int randomCnt) {
        
        try {
            // Parse the input file
            TypeWrapper wrapper = astProcessor.parseJavaFile(inputPath);

            // Create metadata map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("inputPath", inputPath);
            metadata.put("outputDir", outputDir);
            metadata.put("startTime", System.currentTimeMillis());
            metadata.put("strategy", "RANDOM");
            metadata.put("randomCnt", randomCnt);
            metadata.put("specificTransform", transformName);

            // Execute random transformation workflow
            TransformationResult result = executeRandomWorkflow(wrapper, transformName, randomCnt, inputPath, outputDir, metadata);

            // Update final metadata
            if (result.isSuccess()) {
                metadata.put("endTime", System.currentTimeMillis());
                metadata.put("duration", (Long) metadata.get("endTime") - (Long) metadata.get("startTime"));
                result.setMetadata(metadata);
            }

            return result;

        } catch (IOException e) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("Failed to read input file: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("Random transformation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Execute guided transformation workflow.
     * 
     * @param inputPath Path to input Java file
     * @param outputPath Path for output file
     * @param bugInfo Bug information for guided selection
     * @param transformName Specific transform to apply (null = try all transforms)
     * @return TransformationResult containing all generated mutants
     */
    public TransformationResult executeGuidedTransformation(String inputPath, String outputDir, 
            BugInformation bugInfo, String transformName) {
        
        try {
            // Parse the input file
            TypeWrapper wrapper = astProcessor.parseJavaFile(inputPath);

            // Create metadata map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("inputPath", inputPath);
            metadata.put("outputDir", outputDir);
            metadata.put("startTime", System.currentTimeMillis());
            metadata.put("strategy", "GUIDED");
            metadata.put("specificTransform", transformName);

            // Execute guided transformation workflow
            TransformationResult result = executeGuidedWorkflow(wrapper, bugInfo, transformName, inputPath, outputDir, metadata);

            // Update final metadata
            if (result.isSuccess()) {
                metadata.put("endTime", System.currentTimeMillis());
                metadata.put("duration", (Long) metadata.get("endTime") - (Long) metadata.get("startTime"));
                result.setMetadata(metadata);
            }

            return result;

        } catch (IOException e) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("Failed to read input file: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("Guided transformation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Execute target transformation workflow.
     * 
     * @param inputPath Path to input Java file
     * @param outputDir Directory where mutant files will be saved
     * @param targetLines List of line numbers to target for transformation
     * @param transformName Specific transform to apply (null = try all transforms)
     * @return TransformationResult containing all generated mutants
     */
    public TransformationResult executeTargetTransformation(String inputPath, String outputDir, 
            List<Integer> targetLines, String transformName) {
        
        try {
            // Parse the input file
            TypeWrapper wrapper = astProcessor.parseJavaFile(inputPath);

            // Create metadata map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("inputPath", inputPath);
            metadata.put("outputDir", outputDir);
            metadata.put("startTime", System.currentTimeMillis());
            metadata.put("strategy", "TARGET");
            metadata.put("targetLines", targetLines);
            metadata.put("specificTransform", transformName);

            // Execute target transformation workflow
            TransformationResult result = executeTargetWorkflow(wrapper, targetLines, transformName, inputPath, outputDir, metadata);

            // Update final metadata
            if (result.isSuccess()) {
                metadata.put("endTime", System.currentTimeMillis());
                metadata.put("duration", (Long) metadata.get("endTime") - (Long) metadata.get("startTime"));
                result.setMetadata(metadata);
            }

            return result;

        } catch (IOException e) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("Failed to read input file: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("Target transformation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Execute random transformation workflow.
     * 
     * Workflow:
     * 1. Select randomCnt random nodes from all AST nodes
     * 2. For each selected node, try all transforms (or specific transform if provided)
     * 3. Generate separate mutants for each successful transformation
     */
    private TransformationResult executeRandomWorkflow(TypeWrapper wrapper, String specificTransform, 
            int randomCnt, String inputPath, String outputDir, Map<String, Object> metadata) {
        
        // Step 1: Get transforms to apply
        List<Transform> transformsToApply = getTransformsToApply(specificTransform);
        if (transformsToApply.isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("No transforms available" + 
                            (specificTransform != null ? " for: " + specificTransform : ""))
                    .metadata(metadata)
                    .build();
        }

        // Step 2: Select randomCnt random nodes using RandomLocationStrategy
        RandomLocationStrategy randomStrategy = new RandomLocationStrategy();
        List<ASTNode> candidateNodes = randomStrategy.selectCandidateNodes(wrapper, randomCnt);
        if (candidateNodes.isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("No nodes found for random selection")
                    .metadata(metadata)
                    .build();
        }

        metadata.put("candidateNodesFound", candidateNodes.size());
        metadata.put("transformsToTry", transformsToApply.size());

        // Step 3: For each candidate node, try all applicable transforms
        List<MutantInfo> mutants = generateMutants(wrapper, candidateNodes, transformsToApply, inputPath, outputDir);

        metadata.put("mutantsGenerated", mutants.size());
        metadata.put("transformationsApplied", mutants.stream().map(MutantInfo::getTransformName).distinct().count());

        return TransformationResult.builder()
                .success(!mutants.isEmpty())
                .mutants(mutants)
                .metadata(metadata)
                .build();
    }

    /**
     * Execute guided transformation workflow.
     * 
     * Workflow:
     * 1. Validate bug information
     * 2. Select candidate nodes based on bug lines and data flow analysis
     * 3. For each selected node, try all transforms (or specific transform if provided)
     * 4. Generate separate mutants for each successful transformation
     */
    private TransformationResult executeGuidedWorkflow(TypeWrapper wrapper, BugInformation bugInfo, 
            String specificTransform, String inputPath, String outputDir, Map<String, Object> metadata) {
        
        // Step 1: Validate bug information
        if (bugInfo == null || !bugInfo.hasBugs() || bugInfo.getBugLines() == null || bugInfo.getBugLines().isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("GuidedStrategy requires bug=true and valid bug lines")
                    .metadata(metadata)
                    .build();
        }

        // Step 2: Get transforms to apply
        List<Transform> transformsToApply = getTransformsToApply(specificTransform);
        if (transformsToApply.isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("No transforms available" + 
                            (specificTransform != null ? " for: " + specificTransform : ""))
                    .metadata(metadata)
                    .build();
        }

        // Step 3: Select candidate nodes using GuidedLocationStrategy
        GuidedLocationStrategy guidedStrategy = new GuidedLocationStrategy();
        List<ASTNode> candidateNodes = guidedStrategy.selectCandidateNodes(wrapper, bugInfo);
        if (candidateNodes.isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("No candidate nodes found by guided strategy")
                    .metadata(metadata)
                    .build();
        }

        metadata.put("candidateNodesFound", candidateNodes.size());
        metadata.put("transformsToTry", transformsToApply.size());
        metadata.put("bugLines", bugInfo.getBugLines());

        // Step 4: For each candidate node, try all applicable transforms
        List<MutantInfo> mutants = generateMutants(wrapper, candidateNodes, transformsToApply, inputPath, outputDir);

        metadata.put("mutantsGenerated", mutants.size());
        metadata.put("transformationsApplied", mutants.stream().map(MutantInfo::getTransformName).distinct().count());

        return TransformationResult.builder()
                .success(!mutants.isEmpty())
                .mutants(mutants)
                .metadata(metadata)
                .build();
    }

    /**
     * Execute target transformation workflow.
     * 
     * Workflow:
     * 1. Validate target lines
     * 2. Select candidate nodes based on target line numbers
     * 3. For each selected node, try all transforms (or specific transform if provided)
     * 4. Generate separate mutants for each successful transformation
     */
    private TransformationResult executeTargetWorkflow(TypeWrapper wrapper, List<Integer> targetLines, 
            String specificTransform, String inputPath, String outputDir, Map<String, Object> metadata) {
        
        // Step 1: Validate target lines (already validated in service layer, but double-check)
        if (targetLines == null) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("Target lines cannot be null")
                    .metadata(metadata)
                    .build();
        }
        
        // Empty target lines are allowed - they should return success with no mutants
        if (targetLines.isEmpty()) {
            metadata.put("candidateNodesFound", 0);
            metadata.put("transformsToTry", 0);
            metadata.put("mutantsGenerated", 0);
            metadata.put("transformationsApplied", 0);
            
            return TransformationResult.builder()
                    .success(true)
                    .mutants(new ArrayList<>())
                    .metadata(metadata)
                    .build();
        }

        // Step 2: Get transforms to apply
        List<Transform> transformsToApply = getTransformsToApply(specificTransform);
        if (transformsToApply.isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("No transforms available" + 
                            (specificTransform != null ? " for: " + specificTransform : ""))
                    .metadata(metadata)
                    .build();
        }

        // Step 3: Select candidate nodes using TargetLocationStrategy
        TargetLocationStrategy targetStrategy = new TargetLocationStrategy(targetLines);
        List<ASTNode> candidateNodes = targetStrategy.selectCandidateNodes(wrapper, targetLines);
        if (candidateNodes.isEmpty()) {
            return TransformationResult.builder()
                    .success(false)
                    .addErrorMessage("No candidate nodes found on target lines: " + targetLines)
                    .metadata(metadata)
                    .build();
        }

        metadata.put("candidateNodesFound", candidateNodes.size());
        metadata.put("transformsToTry", transformsToApply.size());

        // Step 4: For each candidate node, try all applicable transforms
        List<MutantInfo> mutants = generateMutants(wrapper, candidateNodes, transformsToApply, inputPath, outputDir);

        metadata.put("mutantsGenerated", mutants.size());
        metadata.put("transformationsApplied", mutants.stream().map(MutantInfo::getTransformName).distinct().count());

        return TransformationResult.builder()
                .success(!mutants.isEmpty())
                .mutants(mutants)
                .metadata(metadata)
                .build();
    }

    /**
     * Generate mutants by applying transforms to candidate nodes.
     * 
     * For each candidate node:
     * - Try each transform in the transformsToApply list
     * - If transform is applicable to the node, create a mutant
     * - Each successful transformation generates a separate mutant
     */
    private List<MutantInfo> generateMutants(TypeWrapper wrapper, List<ASTNode> candidateNodes, 
            List<Transform> transformsToApply, String inputPath, String outputDir) {
        
        List<MutantInfo> mutants = new ArrayList<>();

        for (ASTNode candidateNode : candidateNodes) {
            for (Transform transform : transformsToApply) {
                // Check if transform is applicable to this node
                List<ASTNode> applicableNodes = transform.check(wrapper, candidateNode);
                
                if (applicableNodes != null && !applicableNodes.isEmpty()) {
                    // For each applicable node returned by check(), create a mutant
                    for (ASTNode applicableNode : applicableNodes) {
                        try {
                            // Create mutant by applying transform to a copy of the wrapper
                            TypeWrapper mutantWrapper = wrapper.copy();
                            // Node to be transformed
                            int oldLineNumber1 = wrapper.getCompilationUnit().getLineNumber(applicableNode.getStartPosition());
                            int oldColNumber1 = wrapper.getCompilationUnit().getColumnNumber(applicableNode.getStartPosition());
                            ASTNode newTargetNode = mutantWrapper.searchNodeByPosition(applicableNode, oldLineNumber1, oldColNumber1);
                            if (newTargetNode == null) {
                                throw new RuntimeException(wrapper.getFilePath() + ": Old and new ASTWrapper are not matched!");
                            }
                            // source node to extract from report
                            int oldRowNumber2 = wrapper.getCompilationUnit().getLineNumber(candidateNode.getStartPosition());
                            int oldColNumber2 = wrapper.getCompilationUnit().getColumnNumber(candidateNode.getStartPosition());
                            ASTNode newSrcNode = mutantWrapper.searchNodeByPosition(candidateNode, oldRowNumber2, oldColNumber2);
                            if (newSrcNode == null) {
                                throw new RuntimeException(wrapper.getFilePath() + ": Old and new ASTWrapper are not matched!");
                            }
                            boolean success = transform.run(newTargetNode, mutantWrapper, TypeWrapper.getFirstBrotherOfStatement(newSrcNode), newSrcNode);
                            if (success) {
                                // Rewrite the AST to get the final code
                                astProcessor.rewriteAST(mutantWrapper);
                                String mutantCode = mutantWrapper.getCode();
                                
                                // Create mutant file with header comment
                                MutantInfo mutant = com.transformer.util.MutantFileWriter.createMutantWithFile(
                                    transform.getName(), applicableNode, mutantCode, inputPath, outputDir);
                                mutants.add(mutant);
                            }
                        } catch (Exception e) {
                            // Log error but continue with other transformations
                            System.err.println("Failed to generate mutant for transform " + transform.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        return mutants;
    }



    /**
     * Get the list of transforms to apply based on the specific transform name.
     * 
     * @param specificTransform If provided, only return this transform; otherwise return all transforms
     * @return List of transforms to try
     */
    private List<Transform> getTransformsToApply(String specificTransform) {
        if (specificTransform != null && !specificTransform.trim().isEmpty()) {
            Transform transform = transformRegistry.getTransform(specificTransform.trim());
            return transform != null ? List.of(transform) : new ArrayList<>();
        }
        return transformRegistry.getAllTransforms(); // Try all transforms when none specified
    }
}