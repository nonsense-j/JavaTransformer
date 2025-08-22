package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation that splits variable declarations with initializers into separate declaration and assignment statements.
 */
public class AddLocalAssignment extends Transform {

    private static final AddLocalAssignment instance = new AddLocalAssignment();

    private AddLocalAssignment() {}

    public static AddLocalAssignment getInstance() {
        return instance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherStatement, ASTNode sourceStatement) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        VariableDeclarationStatement oldVdStatement = (VariableDeclarationStatement) sourceStatement;
        VariableDeclarationFragment oldFragment = (VariableDeclarationFragment) oldVdStatement.fragments().get(0);
        Expression initializer = oldFragment.getInitializer();
        Expression newInitializer = (Expression) ASTNode.copySubtree(ast, initializer);
        
        // Create new variable declaration without initializer
        VariableDeclarationFragment newVdFragment = ast.newVariableDeclarationFragment();
        newVdFragment.setName(ast.newSimpleName(oldFragment.getName().toString()));
        
        // Copy extra dimensions if any
        List<Dimension> extraDimensions = oldFragment.extraDimensions();
        for (int i = 0; i < extraDimensions.size(); i++) {
            newVdFragment.extraDimensions().add(ASTNode.copySubtree(ast, extraDimensions.get(i)));
        }
        
        VariableDeclarationStatement newVdStatement = ast.newVariableDeclarationStatement(newVdFragment);
        newVdStatement.setType((Type) ASTNode.copySubtree(ast, oldVdStatement.getType()));
        
        // Copy modifiers if any
        List<IExtendedModifier> modifiers = oldVdStatement.modifiers();
        if (!modifiers.isEmpty()) {
            newVdStatement.modifiers().add(ASTNode.copySubtree(ast, (ASTNode) modifiers.get(0)));
        }
        
        // Create assignment statement
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(ast.newSimpleName(oldFragment.getName().toString()));
        assignment.setRightHandSide(newInitializer);
        ExpressionStatement newAssignStatement = ast.newExpressionStatement(assignment);
        
        // Replace the original statement with declaration and assignment
        ListRewrite listRewrite = astRewrite.getListRewrite(sourceStatement.getParent(), Block.STATEMENTS_PROPERTY);
        try {
            listRewrite.insertAfter(newVdStatement, sourceStatement, null);
            listRewrite.insertAfter(newAssignStatement, newVdStatement, null);
            listRewrite.remove(sourceStatement, null);
            
            // Track the transformation
            wrapper.addAppliedTransform(getIndex(), targetNode);
            
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode statement) {
        List<ASTNode> nodes = new ArrayList<>();
        if (statement instanceof VariableDeclarationStatement) {
            VariableDeclarationStatement vdStatement = (VariableDeclarationStatement) statement;
            
            // Skip array types with final modifier to avoid compilation errors
            if (vdStatement.getType() instanceof ArrayType) {
                List<IExtendedModifier> modifiers = vdStatement.modifiers();
                if (modifiers.size() > 0) {
                    IExtendedModifier modifier = modifiers.get(0);
                    if (modifier instanceof Modifier && ((Modifier) modifier).getKeyword().toString().equals("final")) {
                        return nodes; // final byte[] values={0}; -> final byte[] values; values = {0} is wrong.
                    }
                }
            }
            
            VariableDeclarationFragment vdFragment = (VariableDeclarationFragment) vdStatement.fragments().get(0);
            if (vdFragment.getInitializer() != null) {
                nodes.add(statement);
            }
        }
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Splits variable declarations with initializers into separate declaration and assignment statements";
    }
}