package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform that adds control branches by wrapping statements in if-else blocks.
 * This transformation creates redundant control flow structures.
 */
public class AddControlBranch extends Transform {

    private static final AddControlBranch singleInstance = new AddControlBranch();
    private static int varCounter;

    public static AddControlBranch getInstance() {
        return singleInstance;
    }

    private AddControlBranch() {
        varCounter = 0;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherNode, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        IfStatement newIfStatement = ast.newIfStatement();
        Block thenBlock = ast.newBlock();
        Block elseBlock = ast.newBlock();
        VariableDeclarationFragment newBoolVdFragment = ast.newVariableDeclarationFragment();
        String varName = String.format("acb%d", varCounter++);
        newBoolVdFragment.setName(ast.newSimpleName(varName));
        newBoolVdFragment.setInitializer(ast.newBooleanLiteral(true));
        VariableDeclarationStatement newBoolVdStatement = ast.newVariableDeclarationStatement(newBoolVdFragment);
        
        // Add final modifier with proper type safety
        @SuppressWarnings("unchecked")
        List<Modifier> modifiers = newBoolVdStatement.modifiers();
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        newBoolVdStatement.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
        
        if(srcNode instanceof VariableDeclarationStatement) {
            VariableDeclarationStatement oldVdStatement = (VariableDeclarationStatement) srcNode;
            @SuppressWarnings("unchecked")
            List<VariableDeclarationFragment> fragments = oldVdStatement.fragments();
            VariableDeclarationFragment oldFragment = fragments.get(0);
            VariableDeclarationFragment newFragment = ast.newVariableDeclarationFragment();
            newFragment.setName(ast.newSimpleName(oldFragment.getName().toString()));
            
            @SuppressWarnings("unchecked")
            List<ASTNode> extraDims = oldFragment.extraDimensions();
            @SuppressWarnings("unchecked")
            List<ASTNode> newExtraDims = newFragment.extraDimensions();
            for(int i = 0; i < extraDims.size(); i++) {
                newExtraDims.add(ASTNode.copySubtree(ast, extraDims.get(i)));
            }
            
            VariableDeclarationStatement newVdStatement = ast.newVariableDeclarationStatement(newFragment);
            newVdStatement.setType((Type) ASTNode.copySubtree(ast, oldVdStatement.getType()));
            Assignment assignment = ast.newAssignment();
            assignment.setLeftHandSide(ast.newSimpleName(oldFragment.getName().toString()));
            Expression e = (Expression) ASTNode.copySubtree(ast, oldFragment.getInitializer());
            assignment.setRightHandSide(e);
            ExpressionStatement thenStatement = ast.newExpressionStatement(assignment);
            ExpressionStatement elseStatement = (ExpressionStatement) ASTNode.copySubtree(ast, thenStatement);
            
            @SuppressWarnings("unchecked")
            List<Statement> thenStatements = thenBlock.statements();
            thenStatements.add(thenStatement);
            @SuppressWarnings("unchecked")
            List<Statement> elseStatements = elseBlock.statements();
            elseStatements.add(elseStatement);
            
            newIfStatement.setThenStatement(thenBlock);
            newIfStatement.setElseStatement(elseBlock);
            newIfStatement.setExpression(ast.newSimpleName(varName));
            ListRewrite listRewrite = astRewrite.getListRewrite(brotherNode.getParent(), Block.STATEMENTS_PROPERTY);
            listRewrite.insertAfter(newVdStatement, srcNode, null);
            listRewrite.insertAfter(newBoolVdStatement, newVdStatement, null);
            listRewrite.insertAfter(newIfStatement, newBoolVdStatement, null);
            listRewrite.remove(srcNode, null);
        } else {
            Statement thenStatement = (Statement) ASTNode.copySubtree(ast, srcNode);
            Statement elseStatement = (Statement) ASTNode.copySubtree(ast, srcNode);
            
            @SuppressWarnings("unchecked")
            List<Statement> thenStatements = thenBlock.statements();
            thenStatements.add(thenStatement);
            @SuppressWarnings("unchecked")
            List<Statement> elseStatements = elseBlock.statements();
            elseStatements.add(elseStatement);
            
            newIfStatement.setExpression(ast.newSimpleName(varName));
            newIfStatement.setThenStatement(thenBlock);
            newIfStatement.setElseStatement(elseBlock);
            
            if(srcNode.getParent() instanceof Block) {
                ListRewrite listRewrite = astRewrite.getListRewrite(srcNode.getParent(), Block.STATEMENTS_PROPERTY);
                listRewrite.insertAfter(newBoolVdStatement, srcNode, null);
                listRewrite.insertAfter(newIfStatement, newBoolVdStatement, null);
                listRewrite.remove(srcNode, null);
            } else {
                Block newBlock = ast.newBlock();
                @SuppressWarnings("unchecked")
                List<Statement> blockStatements = newBlock.statements();
                blockStatements.add(newBoolVdStatement);
                blockStatements.add(newIfStatement);
                astRewrite.replace(srcNode, newBlock, null);
            }
        }
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        if (TypeWrapper.isLiteral(node)) {
            return nodes;
        }
        if (node instanceof FieldDeclaration || node instanceof MethodDeclaration || node instanceof SuperConstructorInvocation ||
                node instanceof EmptyStatement || node instanceof ReturnStatement) {
            return nodes;
        }
        if(node instanceof Statement) {
            if(node instanceof VariableDeclarationStatement) {
               return nodes;
            }
            if(node.getParent().getParent() instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) node.getParent().getParent();
                @SuppressWarnings("unchecked")
                List<Statement> statements = method.getBody().statements();
                if(statements.isEmpty()) {
                    return nodes;
                }
                ASTNode firstStatement = statements.get(0);
                if(method.isConstructor() && node == firstStatement && node instanceof ConstructorInvocation) {
                    return nodes;
                }
                if(method.getName().getIdentifier().equals("finalize")) {
                    return nodes;
                }
            }
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Adds control branches by wrapping statements in if-else blocks";
    }
}