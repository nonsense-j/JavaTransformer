package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform that adds static assignment by separating static field declarations from their initializations.
 * This transformation moves static field initialization to static blocks.
 */
public class AddStaticAssignment extends Transform {

    private static final AddStaticAssignment instance = new AddStaticAssignment();

    private AddStaticAssignment() {}

    public static Transform getInstance() {
        return instance;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherStatement, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        FieldDeclaration oldFieldDeclaration = (FieldDeclaration) srcNode;
        
        // Check if field has static modifier
        @SuppressWarnings("unchecked")
        List<ASTNode> modifiers = oldFieldDeclaration.modifiers();
        boolean hasStatic = false;
        for (ASTNode modifier : modifiers) {
            if (modifier instanceof Modifier && ((Modifier) modifier).getKeyword() == Modifier.ModifierKeyword.STATIC_KEYWORD) {
                hasStatic = true;
                break;
            }
        }
        
        if(!hasStatic) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = oldFieldDeclaration.fragments();
        VariableDeclarationFragment oldVdFragment = fragments.get(0);
        FieldDeclaration newFieldDeclaration = (FieldDeclaration) ASTNode.copySubtree(ast, oldFieldDeclaration);
        
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> newFragments = newFieldDeclaration.fragments();
        VariableDeclarationFragment newVdFragment = newFragments.get(0);
        
        if(newVdFragment.getInitializer() != null) {
            astRewrite.replace(newVdFragment.getInitializer(), null, null);
        }
        
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide((SimpleName) ASTNode.copySubtree(ast, oldVdFragment.getName()));
        assignment.setRightHandSide((Expression) ASTNode.copySubtree(ast, oldVdFragment.getInitializer()));
        ExpressionStatement newAssignmentStatement = ast.newExpressionStatement(assignment);
        Block newStaticBlock = ast.newBlock();
        
        @SuppressWarnings("unchecked")
        List<Statement> blockStatements = newStaticBlock.statements();
        blockStatements.add(newAssignmentStatement);
        
        // Create a static initializer instead of just a block
        Initializer staticInitializer = ast.newInitializer();
        staticInitializer.setBody(newStaticBlock);
        @SuppressWarnings("unchecked")
        List<Modifier> initModifiers = staticInitializer.modifiers();
        initModifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        
        TypeDeclaration clazz = (TypeDeclaration) oldFieldDeclaration.getParent();
        @SuppressWarnings("unchecked")
        List<BodyDeclaration> bodyDeclarations = clazz.bodyDeclarations();
        int pos = bodyDeclarations.indexOf(oldFieldDeclaration);
        if(pos == -1) {
            System.out.println("Error: Class and Type Declaration are not matched!");
            return false;
        }
        
        bodyDeclarations.add(pos + 1, newFieldDeclaration);
        bodyDeclarations.add(pos + 2, staticInitializer);
        bodyDeclarations.remove(pos);
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode statement) {
        List<ASTNode> nodes = new ArrayList<>();
        if(statement instanceof FieldDeclaration) {
            nodes.add(statement);
        }
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Adds static assignment by separating static field declarations from their initializations";
    }
}