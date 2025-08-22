package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Transformation that wraps methods and fields in enum class declarations.
 */
public class EnumClassWrapper extends Transform {

    private static int enumCounter = 0;
    private static EnumClassWrapper instance = new EnumClassWrapper();

    public static EnumClassWrapper getInstance() {
        return instance;
    }

    private EnumClassWrapper() {}

    @Override
    @SuppressWarnings("unchecked")
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherNode, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        TypeDeclaration clazz = TypeWrapper.getClassOfNode(srcNode);
        List<IExtendedModifier> classModifiers = clazz.modifiers();
        
        // Collect static field names
        List<String> staticFieldNames = new ArrayList<>();
        for (FieldDeclaration fd : clazz.getFields()) {
            List<IExtendedModifier> modifiers = fd.modifiers();
            for (IExtendedModifier modifier : modifiers) {
                if (modifier instanceof Modifier && ((Modifier) modifier).isStatic()) {
                    staticFieldNames.add(((VariableDeclarationFragment) fd.fragments().get(0)).getName().getFullyQualifiedName());
                }
            }
        }
        
        MethodDeclaration oldMethod = TypeWrapper.getDirectMethodOfNode(srcNode);
        EnumConstantDeclaration enumConstant = ast.newEnumConstantDeclaration();
        enumConstant.setName(ast.newSimpleName("RED"));
        EnumDeclaration enumClass = ast.newEnumDeclaration();
        
        // Copy class modifiers
        for (IExtendedModifier classModifier : classModifiers) {
            if (classModifier instanceof Modifier) {
                enumClass.modifiers().add(ASTNode.copySubtree(ast, (ASTNode) classModifier));
            }
        }
        
        enumClass.setName(ast.newSimpleName("enumClass_" + enumCounter++));
        enumClass.enumConstants().add(enumConstant);
        ListRewrite listRewrite = astRewrite.getListRewrite(enumClass, enumClass.getBodyDeclarationsProperty());
        
        if (oldMethod != null) {
            if (oldMethod.isConstructor()) {
                return false;
            }
            
            TypeDeclaration type = TypeWrapper.getClassOfNode(srcNode);
            String methodKey = type.getName().toString() + ":" + TypeWrapper.createMethodSignature(oldMethod);
            for (Map.Entry<String, HashSet<String>> entry : wrapper.getMethod2identifiers().entrySet()) {
                if (!entry.getKey().equals(methodKey) && entry.getValue().contains(oldMethod.getName().getIdentifier())) {
                    return false; // Method name is used elsewhere
                }
            }
            
            // Check if method only uses static fields
            if (oldMethod.getBody() != null) {
                List<Statement> statements = oldMethod.getBody().statements();
                for (Statement statement : statements) {
                    List<ASTNode> nodes = TypeWrapper.getChildrenNodes(statement);
                    for (ASTNode node : nodes) {
                        if (node instanceof SimpleName) {
                            if (!staticFieldNames.contains(((SimpleName) node).getFullyQualifiedName())) {
                                return false; // Uses non-static fields
                            }
                        }
                    }
                }
            }
            
            // Copy variable declaration statements to enum class
            List<VariableDeclarationStatement> validVDStatements = new ArrayList<>();
            List<ASTNode> bodyDeclarations = clazz.bodyDeclarations();
            for (ASTNode node : bodyDeclarations) {
                if (node instanceof VariableDeclarationStatement) {
                    validVDStatements.add((VariableDeclarationStatement) node);
                }
            }
            
            MethodDeclaration newMethod = (MethodDeclaration) ASTNode.copySubtree(ast, oldMethod);
            for (VariableDeclarationStatement vdStatement : validVDStatements) {
                listRewrite.insertLast(vdStatement, null);
            }
            listRewrite.insertLast(newMethod, null);
            astRewrite.replace(oldMethod, enumClass, null);
            
            // Track the transformation
            wrapper.addAppliedTransform(getIndex(), targetNode);
            return true;
        } else {
            if (srcNode instanceof FieldDeclaration) {
                String varName = ((VariableDeclarationFragment) (((FieldDeclaration) srcNode).fragments().get(0))).getName().getIdentifier();
                for (Map.Entry<String, HashSet<String>> entry : wrapper.getMethod2identifiers().entrySet()) {
                    if (entry.getValue().contains(varName)) {
                        return false; // Variable name is used elsewhere
                    }
                }
                
                FieldDeclaration newStatement = (FieldDeclaration) ASTNode.copySubtree(ast, srcNode);
                listRewrite.insertLast(newStatement, null);
                astRewrite.replace(srcNode, enumClass, null);
                
                // Track the transformation
                wrapper.addAppliedTransform(getIndex(), targetNode);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        TypeDeclaration clazz = TypeWrapper.getClassOfNode(node);
        
        if (!TypeWrapper.checkClassProperty(clazz)) {
            return nodes;
        }
        
        MethodDeclaration method = TypeWrapper.getDirectMethodOfNode(node);
        if (method == null) {
            ASTNode statement = TypeWrapper.getStatementOfNode(node);
            if (statement instanceof FieldDeclaration) {
                FieldDeclaration fieldDecl = (FieldDeclaration) statement;
                
                // Skip logger fields
                if (fieldDecl.getType().toString().toLowerCase().contains("logger")) {
                    return nodes;
                }
                
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
                String varName = fragment.getName().getIdentifier();
                
                // Skip serialVersionUID
                if (varName.equals("serialVersionUID")) {
                    return nodes;
                }
                
                // Skip if there's a method with the same name
                for (MethodDeclaration methodDeclaration : clazz.getMethods()) {
                    if (methodDeclaration.getName().getIdentifier().equals(varName)) {
                        return nodes;
                    }
                }
                
                // Skip if field uses 'this'
                List<ASTNode> subNodes = TypeWrapper.getChildrenNodes(node);
                for (ASTNode subNode : subNodes) {
                    if (subNode instanceof ThisExpression) {
                        return nodes;
                    }
                }
                
                nodes.add(node);
            }
            return nodes;
        }
        
        String methodName = method.getName().getIdentifier();
        
        // Skip constructors with same name as class
        if (!method.isConstructor() && methodName.equals(clazz.getName().getIdentifier())) {
            return nodes;
        }
        
        // Skip common methods
        if (methodName.equals("equals") || methodName.equals("hashCode") || methodName.equals("toString") || 
            methodName.equals("clone") || methodName.equals("compareTo")) {
            return nodes;
        }
        
        // Skip test methods and getters/setters
        if (methodName.toLowerCase().startsWith("test") || methodName.startsWith("get") || methodName.startsWith("set")) {
            return nodes;
        }
        
        // Skip final classes
        List<IExtendedModifier> classModifiers = clazz.modifiers();
        for (IExtendedModifier classModifier : classModifiers) {
            if (classModifier instanceof Modifier && ((Modifier) classModifier).getKeyword().toString().contains("final")) {
                return nodes;
            }
        }
        
        // Check for inheritance issues
        List<ASTNode> bodyDeclarations = clazz.bodyDeclarations();
        for (ASTNode component : bodyDeclarations) {
            if (component instanceof TypeDeclaration) {
                Type parentType = ((TypeDeclaration) component).getSuperclassType();
                if (parentType instanceof SimpleType && ((SimpleType) parentType).getName().toString().equals(clazz.getName().getIdentifier())) {
                    return nodes;
                }
            }
        }
        
        // Check if method uses 'this'
        List<ASTNode> subNodes = TypeWrapper.getChildrenNodes(method);
        boolean hasThis = false;
        for (ASTNode subNode : subNodes) {
            if (subNode instanceof ThisExpression) {
                hasThis = true;
                break;
            }
        }
        if (hasThis) {
            return nodes;
        }
        
        // Check modifiers and annotations
        List<IExtendedModifier> methodModifiers = method.modifiers();
        boolean isOverride = false;
        for (IExtendedModifier modifier : methodModifiers) {
            if (modifier instanceof MarkerAnnotation) {
                String name = ((MarkerAnnotation) modifier).getTypeName().getFullyQualifiedName();
                if (name.contains("Override")) {
                    isOverride = true;
                }
                if (name.contains("Test") || name.contains("UiThread")) {
                    return nodes;
                }
            }
            if (modifier instanceof Modifier) {
                String modifierName = ((Modifier) modifier).getKeyword().toString();
                if (modifierName.contains("abstract") || modifierName.contains("synchronized")) {
                    return nodes;
                }
            }
        }
        
        if (!isOverride) {
            nodes.add(node);
        }
        
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Wraps methods and fields in enum class declarations";
    }
}