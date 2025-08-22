package com.transformer.core;

import com.transformer.transform.Transform;
import com.transformer.transform.AddArgAssignment;
import com.transformer.transform.AddBrackets;
import com.transformer.transform.AddControlBranch;
import com.transformer.transform.AddGlobalAssignment;
import com.transformer.transform.AddLocalAssignment;
import com.transformer.transform.AddMethodCallToLiteral;
import com.transformer.transform.AddRedundantLiteral;
import com.transformer.transform.AddStaticAssignment;
import com.transformer.transform.AddStaticModifier;
import com.transformer.transform.AnonymousClassWrapper;
import com.transformer.transform.CFWrapperWithDoWhile;
import com.transformer.transform.CFWrapperWithForFalse;
import com.transformer.transform.CFWrapperWithForTrue1;
import com.transformer.transform.CFWrapperWithForTrue2;
import com.transformer.transform.CFWrapperWithIfFalse;
import com.transformer.transform.CFWrapperWithIfTrue;
import com.transformer.transform.CFWrapperWithWhileTrue;
import com.transformer.transform.CompoundExpression1;
import com.transformer.transform.CompoundExpression2;
import com.transformer.transform.CompoundExpression3;
import com.transformer.transform.CompoundExpression4;
import com.transformer.transform.CompoundExpression5;
import com.transformer.transform.EnumClassWrapper;
import com.transformer.transform.LoopConversion1;
import com.transformer.transform.LoopConversion2;
import com.transformer.transform.NestedClassWrapper;
import com.transformer.transform.TransferLocalVarToGlobal;
import com.transformer.transform.TransferLocalVarToStaticGlobal;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry for managing available code transformations.
 * Provides registration, lookup, and filtering functionality for transforms.
 */
public class TransformRegistry {
    
    private final Map<String, Transform> transforms;
    private static TransformRegistry instance;
    
    private TransformRegistry() {
        this.transforms = new HashMap<>();
        registerDefaultTransforms();
    }
    
    /**
     * Get the singleton instance of the TransformRegistry.
     * 
     * @return The TransformRegistry instance
     */
    public static synchronized TransformRegistry getInstance() {
        if (instance == null) {
            instance = new TransformRegistry();
        }
        return instance;
    }
    
    /**
     * Register all default transforms from the transform package.
     */
    private void registerDefaultTransforms() {
        registerTransform(AddArgAssignment.getInstance());
        registerTransform(AddBrackets.getInstance());
        registerTransform(AddControlBranch.getInstance());
        registerTransform(AddGlobalAssignment.getInstance());
        registerTransform(AddLocalAssignment.getInstance());
        registerTransform(AddMethodCallToLiteral.getInstance());
        registerTransform(AddRedundantLiteral.getInstance());
        registerTransform(AddStaticAssignment.getInstance());
        registerTransform(AddStaticModifier.getInstance());
        registerTransform(AnonymousClassWrapper.getInstance());
        registerTransform(CFWrapperWithDoWhile.getInstance());
        registerTransform(CFWrapperWithForFalse.getInstance());
        registerTransform(CFWrapperWithForTrue1.getInstance());
        registerTransform(CFWrapperWithForTrue2.getInstance());
        registerTransform(CFWrapperWithIfFalse.getInstance());
        registerTransform(CFWrapperWithIfTrue.getInstance());
        registerTransform(CFWrapperWithWhileTrue.getInstance());
        registerTransform(CompoundExpression1.getInstance());
        registerTransform(CompoundExpression2.getInstance());
        registerTransform(CompoundExpression3.getInstance());
        registerTransform(CompoundExpression4.getInstance());
        registerTransform(CompoundExpression5.getInstance());
        registerTransform(EnumClassWrapper.getInstance());
        registerTransform(LoopConversion1.getInstance());
        registerTransform(LoopConversion2.getInstance());
        registerTransform(NestedClassWrapper.getInstance());
        registerTransform(TransferLocalVarToGlobal.getInstance());
        registerTransform(TransferLocalVarToStaticGlobal.getInstance());
    }
    
    /**
     * Register a transform in the registry.
     * 
     * @param transform The transform to register
     * @throws IllegalArgumentException if transform is null or name is empty
     */
    public void registerTransform(Transform transform) {
        if (transform == null) {
            throw new IllegalArgumentException("Transform cannot be null");
        }
        
        String name = transform.getIndex();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Transform name cannot be null or empty");
        }
        
        transforms.put(name, transform);
    }
    
    /**
     * Get a transform by name.
     * 
     * @param name The name of the transform
     * @return The transform instance, or null if not found
     */
    public Transform getTransform(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return transforms.get(name);
    }
    
    /**
     * Get all available transform names.
     * 
     * @return List of transform names
     */
    public List<String> getAvailableTransforms() {
        return new ArrayList<>(transforms.keySet());
    }
    
    /**
     * Get all registered transform instances.
     * 
     * @return List of all transforms
     */
    public List<Transform> getAllTransforms() {
        return new ArrayList<>(transforms.values());
    }
    
    /**
     * Check if a transform is registered.
     * 
     * @param name The name of the transform
     * @return true if the transform is registered, false otherwise
     */
    public boolean hasTransform(String name) {
        return name != null && transforms.containsKey(name);
    }
    
    /**
     * Get the number of registered transforms.
     * 
     * @return The count of registered transforms
     */
    public int getTransformCount() {
        return transforms.size();
    }
    
    /**
     * Filter transforms that can handle a specific node type.
     * This method checks which transforms can potentially work with the given node
     * by calling their check method.
     * 
     * @param wrapper The TypeWrapper containing the AST
     * @param node The AST node to filter transforms for
     * @return List of transforms that can handle the node type
     */
    public List<Transform> getTransformsForNode(TypeWrapper wrapper, ASTNode node) {
        if (wrapper == null || node == null) {
            return new ArrayList<>();
        }
        
        return transforms.values().stream()
            .filter(transform -> {
                try {
                    List<ASTNode> candidateNodes = transform.check(wrapper, node);
                    return candidateNodes != null && !candidateNodes.isEmpty();
                } catch (Exception e) {
                    // If check method throws an exception, this transform can't handle the node
                    return false;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter transforms by node type class.
     * This is a simpler filtering method that checks transforms based on the node's class type.
     * 
     * @param nodeType The class type of the AST node
     * @return List of transform names that typically work with this node type
     */
    public List<String> getTransformNamesForNodeType(Class<? extends ASTNode> nodeType) {
        if (nodeType == null) {
            return new ArrayList<>();
        }
        
        // This is a heuristic-based approach - in practice, you might want to maintain
        // a mapping of node types to transforms for better performance
        List<String> compatibleTransforms = new ArrayList<>();
        
        // Add logic based on common patterns observed in the transforms
        String nodeTypeName = nodeType.getSimpleName();
        
        // Expression-related transforms
        if (nodeTypeName.contains("Expression") || nodeTypeName.contains("Assignment")) {
            compatibleTransforms.add("AddBrackets");
            compatibleTransforms.add("CompoundExpression1");
            compatibleTransforms.add("CompoundExpression2");
            compatibleTransforms.add("CompoundExpression3");
            compatibleTransforms.add("AddRedundantLiteral");
        }
        
        // Loop-related transforms
        if (nodeTypeName.contains("For") || nodeTypeName.contains("While") || nodeTypeName.contains("Loop")) {
            compatibleTransforms.add("LoopConversion1");
            compatibleTransforms.add("CFWrapperWithDoWhile");
        }
        
        // Statement-related transforms
        if (nodeTypeName.contains("Statement")) {
            compatibleTransforms.add("CFWrapperWithIfTrue");
            compatibleTransforms.add("CFWrapperWithIfFalse");
            compatibleTransforms.add("AddLocalAssignment");
        }
        
        // Declaration-related transforms
        if (nodeTypeName.contains("Declaration")) {
            compatibleTransforms.add("AddStaticModifier");
            compatibleTransforms.add("AddGlobalAssignment");
        }
        
        // Class-related transforms
        if (nodeTypeName.contains("Type") || nodeTypeName.contains("Class")) {
            compatibleTransforms.add("AnonymousClassWrapper");
            compatibleTransforms.add("NestedClassWrapper");
            compatibleTransforms.add("EnumClassWrapper");
        }
        
        // Filter to only return transforms that are actually registered
        return compatibleTransforms.stream()
            .filter(this::hasTransform)
            .collect(Collectors.toList());
    }
    
    /**
     * Clear all registered transforms.
     * Mainly used for testing purposes.
     */
    public void clear() {
        transforms.clear();
    }
    
    /**
     * Reset the registry to its default state with all default transforms.
     */
    public void reset() {
        clear();
        registerDefaultTransforms();
    }
}