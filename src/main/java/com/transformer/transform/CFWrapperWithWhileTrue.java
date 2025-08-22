package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
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
 * Transform that wraps statements with a while(true) loop and break statement.
 * This creates a while loop that executes exactly once.
 */
public class CFWrapperWithWhileTrue extends Transform {

    private static final CFWrapperWithWhileTrue instance = new CFWrapperWithWhileTrue();

    private CFWrapperWithWhileTrue() {}

    public static CFWrapperWithWhileTrue getInstance() {
        return instance;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brother, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        ASTNode parNode = srcNode.getParent();
        if(parNode instanceof Block) {
            @SuppressWarnings("unchecked")
            List<Statement> statements = ((Block) parNode).statements();
            if(statements.size() == 1) {
                return false;
            }
        }
        Statement newStatement = (Statement) ASTNode.copySubtree(ast, srcNode);
        WhileStatement whileStatement = ast.newWhileStatement();
        whileStatement.setExpression(ast.newBooleanLiteral(true));
        
        // Track prior nodes if wrapper supports it
        if (wrapper.getPriorNodes() != null) {
            wrapper.getPriorNodes().add(whileStatement.getExpression());
        }
        
        BreakStatement breakStatement = ast.newBreakStatement();
        Block block = ast.newBlock();
        
        @SuppressWarnings("unchecked")
        List<Statement> blockStatements = block.statements();
        blockStatements.add(newStatement);
        blockStatements.add(breakStatement);
        whileStatement.setBody(block);
        astRewrite.replace(srcNode, whileStatement, null);
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        if(TypeWrapper.isLiteral(node)) {
            return nodes;
        }
        ASTNode par = node.getParent();
        if (node instanceof Statement && (par instanceof IfStatement || par instanceof WhileStatement ||
                par instanceof DoStatement || par instanceof ForStatement)) {
            return nodes;
        }
        if(node instanceof VariableDeclarationStatement || node instanceof FieldDeclaration ||
                node instanceof MethodDeclaration || node instanceof ReturnStatement || node instanceof SuperConstructorInvocation) {
            return nodes;
        }
        if(!(node instanceof Statement) && !(node instanceof Block)) {
            return nodes;
        }
        if(node.getParent().getParent() instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) node.getParent().getParent();
            @SuppressWarnings("unchecked")
            List<Statement> statements = method.getBody().statements();
            if(method.isConstructor() && !statements.isEmpty() && node == statements.get(0) && node instanceof ConstructorInvocation) {
                return nodes;
            }
        }
        nodes.add(node);
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Wraps statements with a while(true) loop and break statement";
    }
}