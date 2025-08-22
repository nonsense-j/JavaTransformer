package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation that wraps statements in do-while(false) blocks.
 */
public class CFWrapperWithDoWhile extends Transform {

    private static final CFWrapperWithDoWhile instance = new CFWrapperWithDoWhile();

    private CFWrapperWithDoWhile() {}

    public static CFWrapperWithDoWhile getInstance() {
        return instance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brother, ASTNode sourceStatement) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        DoStatement newDoStatement = ast.newDoStatement();
        newDoStatement.setExpression(ast.newBooleanLiteral(false));
        Statement newStatement = (Statement) ASTNode.copySubtree(ast, sourceStatement);
        Block newMethodBlock = ast.newBlock();
        newMethodBlock.statements().add(newStatement);
        newDoStatement.setBody(newMethodBlock);
        astRewrite.replace(sourceStatement, newDoStatement, null);
        
        // Track the transformation
        wrapper.addAppliedTransform(getIndex(), targetNode);
        
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        
        // Skip literals
        if (TypeWrapper.isLiteral(node)) {
            return nodes;
        }
        
        // Only process statements and blocks
        if (!(node instanceof Statement) && !(node instanceof Block)) {
            return nodes;
        }
        
        ASTNode par = node.getParent();
        
        // Skip statements that are already part of control flow structures
        if (node instanceof Statement && (par instanceof IfStatement || par instanceof WhileStatement ||
                par instanceof DoStatement || par instanceof ForStatement || par instanceof EmptyStatement)) {
            return nodes;
        }
        
        // Skip certain types of statements
        if (node instanceof VariableDeclarationStatement || node instanceof FieldDeclaration ||
                node instanceof MethodDeclaration || node instanceof ReturnStatement || 
                node instanceof SuperConstructorInvocation) {
            return nodes;
        }
        
        // Skip constructor invocations and finalize methods
        if (node.getParent() != null && node.getParent().getParent() instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) node.getParent().getParent();
            if (method.getBody() != null) {
                List<Statement> statements = method.getBody().statements();
                if (method.isConstructor() && !statements.isEmpty() && 
                    node == statements.get(0) && node instanceof ConstructorInvocation) {
                    return nodes;
                }
            }
            if (method.getName().getIdentifier().equals("finalize")) {
                return nodes;
            }
        }
        
        nodes.add(node);
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Wraps statements in do-while(false) blocks";
    }
}