package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation that creates compound numeric expressions by adding zero to number literals.
 */
public class CompoundExpression3 extends Transform {

    private static CompoundExpression3 instance = new CompoundExpression3();

    private CompoundExpression3() {}

    public static CompoundExpression3 getInstance() {
        return instance;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        List<ASTNode> subNodes = TypeWrapper.getChildrenNodes(node);
        for (ASTNode subNode : subNodes) {
            if (subNode instanceof NumberLiteral) {
                nodes.add(subNode);
            }
        }
        return nodes;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode broNode, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        NumberLiteral targetLiteral = (NumberLiteral) targetNode;
        ParenthesizedExpression newParenthesizedExpression = ast.newParenthesizedExpression();
        InfixExpression newInfixExpression = ast.newInfixExpression();
        newInfixExpression.setLeftOperand(ast.newNumberLiteral("0"));
        newInfixExpression.setRightOperand((NumberLiteral) ASTNode.copySubtree(ast, targetLiteral));
        newInfixExpression.setOperator(InfixExpression.Operator.PLUS);
        newParenthesizedExpression.setExpression(newInfixExpression);
        astRewrite.replace(targetLiteral, newParenthesizedExpression, null);
        
        // Track the transformation
        wrapper.addAppliedTransform(getIndex(), targetNode);
        
        return true;
    }

    @Override
    public String getDescription() {
        return "Creates compound numeric expressions by adding zero to number literals";
    }
}