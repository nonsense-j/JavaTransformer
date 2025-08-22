package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation that splits field declarations with initializers into separate declaration and static initializer blocks.
 */
public class AddGlobalAssignment extends Transform {

    private static final AddGlobalAssignment instance = new AddGlobalAssignment();

    private AddGlobalAssignment() {}

    public static AddGlobalAssignment getInstance() {
        return instance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherStatement, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        TypeDeclaration oldClazz = TypeWrapper.getClassOfNode(srcNode);
        
        if (!(targetNode instanceof FieldDeclaration)) {
            return false;
        }
        
        FieldDeclaration oldFD = (FieldDeclaration) targetNode;
        VariableDeclarationFragment oldVdFragment = (VariableDeclarationFragment) oldFD.fragments().get(0);
        Expression initializer = oldVdFragment.getInitializer();
        
        if (initializer == null) {
            return false;
        }
        
        // Create new field declaration without initializer
        VariableDeclarationFragment newVdFragment = ast.newVariableDeclarationFragment();
        newVdFragment.setName(ast.newSimpleName(oldVdFragment.getName().getIdentifier()));
        
        // Copy extra dimensions if any
        List<Dimension> extraDimensions = oldVdFragment.extraDimensions();
        for (Dimension dimension : extraDimensions) {
            newVdFragment.extraDimensions().add(ASTNode.copySubtree(ast, dimension));
        }
        
        FieldDeclaration newFD = ast.newFieldDeclaration(newVdFragment);
        newFD.setType((Type) ASTNode.copySubtree(ast, oldFD.getType()));
        
        // Copy modifiers
        List<IExtendedModifier> modifiers = oldFD.modifiers();
        for (IExtendedModifier modifier : modifiers) {
            newFD.modifiers().add(ASTNode.copySubtree(ast, (ASTNode) modifier));
        }
        
        // Create assignment statement
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(ast.newSimpleName(oldVdFragment.getName().getIdentifier()));
        assignment.setRightHandSide((Expression) ASTNode.copySubtree(ast, oldVdFragment.getInitializer()));
        assignment.setOperator(Assignment.Operator.ASSIGN);
        ExpressionStatement newAssignment = ast.newExpressionStatement(assignment);
        
        // Create static initializer block
        Initializer newInit = ast.newInitializer();
        newInit.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        Block block = ast.newBlock();
        block.statements().add(newAssignment);
        newInit.setBody(block);
        
        // Replace field and add initializer
        ListRewrite bodyRewrite = astRewrite.getListRewrite(oldClazz, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        astRewrite.replace(oldFD, newFD, null);
        bodyRewrite.insertAfter(newInit, newFD, null);
        
        // Track the transformation
        wrapper.addAppliedTransform(getIndex(), targetNode);
        
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        ASTNode statement = TypeWrapper.getStatementOfNode(node);
        
        if (statement != null && statement instanceof FieldDeclaration) {
            FieldDeclaration fieldDecl = (FieldDeclaration) statement;
            
            // Check if field has static modifier and initializer
            @SuppressWarnings("unchecked")
            List<IExtendedModifier> modifiers = fieldDecl.modifiers();
            boolean hasStatic = false;
            for (IExtendedModifier modifier : modifiers) {
                if (modifier instanceof Modifier && ((Modifier) modifier).getKeyword() == Modifier.ModifierKeyword.STATIC_KEYWORD) {
                    hasStatic = true;
                    break;
                }
            }
            
            if (hasStatic) {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
                if (fragment.getInitializer() != null) {
                    nodes.add(statement);
                }
            }
        }
        
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Splits static field declarations with initializers into separate declaration and static initializer blocks";
    }
}