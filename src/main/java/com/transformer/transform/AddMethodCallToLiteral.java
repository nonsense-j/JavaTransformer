package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform that adds method calls to literals by extracting literals into separate methods.
 * This transformation creates helper methods that return literal values.
 */
public class AddMethodCallToLiteral extends Transform {

    private static int literalCounter = 0;

    private static final AddMethodCallToLiteral addMethodCallToLiteral = new AddMethodCallToLiteral();
    private AddMethodCallToLiteral() {}

    public static AddMethodCallToLiteral getInstance() {
        return addMethodCallToLiteral;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode broNode, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        MethodDeclaration newMethod = ast.newMethodDeclaration();
        String newMethodName = "getLiteral" + literalCounter++;
        newMethod.setReturnType2(TypeWrapper.checkLiteralType(ast, (Expression) targetNode));
        newMethod.setName(ast.newSimpleName(newMethodName));
        ReturnStatement returnStatement = ast.newReturnStatement();
        Block newBlock = ast.newBlock();
        
        @SuppressWarnings("unchecked")
        List<Statement> blockStatements = newBlock.statements();
        blockStatements.add(returnStatement);
        newMethod.setBody(newBlock);
        returnStatement.setExpression((Expression) ASTNode.copySubtree(ast, targetNode));
        
        MethodDeclaration directMethod = TypeWrapper.getDirectMethodOfNode(srcNode);
        if(directMethod == null || directMethod.isConstructor()) {
            return false;
        }
        
        boolean hasStatic = false;
        @SuppressWarnings("unchecked")
        List<ASTNode> modifiers = directMethod.modifiers();
        for(ASTNode modifier : modifiers) {
            if(modifier instanceof Modifier) {
                if(((Modifier) modifier).getKeyword().toString().equals("static")) {
                    hasStatic = true;
                }
            }
        }
        
        if(hasStatic) {
            @SuppressWarnings("unchecked")
            List<Modifier> newMethodModifiers = newMethod.modifiers();
            newMethodModifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        }
        
        TypeDeclaration clazz = (TypeDeclaration) directMethod.getParent();
        ListRewrite listRewrite = astRewrite.getListRewrite(clazz, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertFirst(newMethod, null);
        MethodInvocation newMethodInvocation = ast.newMethodInvocation();
        newMethodInvocation.setName(ast.newSimpleName(newMethodName));
        astRewrite.replace(targetNode, newMethodInvocation, null);
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        if (node instanceof FieldDeclaration || node instanceof MethodDeclaration) {
            return nodes;
        }
        List<ASTNode> subNodes = TypeWrapper.getChildrenNodes(node);
        for (int i = subNodes.size() - 1; i >= 0; i--) {
            ASTNode subNode = subNodes.get(i);
            if (TypeWrapper.isLiteral(subNode)) {
                nodes.add(subNode);
            }
        }
        
        // Remove switch case expressions from candidates
        if (node instanceof SwitchStatement) {
            @SuppressWarnings("unchecked")
            List<Statement> statements = ((SwitchStatement) node).statements();
            for (Statement statement : statements) {
                if(statement instanceof SwitchCase) {
                    // Handle deprecated getExpression() method
                    Expression e = null;
                    try {
                        e = ((SwitchCase) statement).getExpression();
                    } catch (Exception ex) {
                        // Handle newer API if available
                        continue;
                    }
                    if (e != null) {
                        for(int i = nodes.size() - 1; i >= 0; i--) {
                            ASTNode subNode = nodes.get(i);
                            if(subNode == e) {
                                nodes.remove(subNode);
                            }
                        }
                    }
                }
            }
        }
        
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Adds method calls to literals by extracting literals into separate methods";
    }
}