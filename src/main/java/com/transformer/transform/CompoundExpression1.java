package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation that creates compound boolean expressions from boolean literals.
 */
public class CompoundExpression1 extends Transform {

    private static final CompoundExpression1 instance = new CompoundExpression1();

    private CompoundExpression1() {}

    public static CompoundExpression1 getInstance() {
        return instance;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brother, ASTNode oldStatement) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        BooleanLiteral targetLiteral = (BooleanLiteral) targetNode;
        ParenthesizedExpression newParenthesizedExpression = ast.newParenthesizedExpression();
        InfixExpression newInfixExpression = ast.newInfixExpression();
        newInfixExpression.setLeftOperand((BooleanLiteral) ASTNode.copySubtree(ast, targetLiteral));
        
        if (targetLiteral.booleanValue()) {
            newInfixExpression.setRightOperand(ast.newBooleanLiteral(false));
            newInfixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
        } else {
            newInfixExpression.setRightOperand(ast.newBooleanLiteral(true));
            newInfixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
        }
        
        newParenthesizedExpression.setExpression(newInfixExpression);
        astRewrite.replace(targetLiteral, newParenthesizedExpression, null);
        
        // Track the transformation
        wrapper.addAppliedTransform(getIndex(), targetNode);
        
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode statement) {
        List<ASTNode> nodes = new ArrayList<>();
        List<ASTNode> subNodes = TypeWrapper.getChildrenNodes(statement);
        for (ASTNode subNode : subNodes) {
            if (subNode instanceof BooleanLiteral) {
                nodes.add(subNode);
            }
        }
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Creates compound boolean expressions from boolean literals";
    }
}