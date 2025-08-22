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
 * Transform that creates compound expressions by wrapping number literals in parentheses with minus zero.
 * This transformation converts "5" to "(5 - 0)".
 */
public class CompoundExpression4 extends Transform {

    private static CompoundExpression4 instance = new CompoundExpression4();

    private CompoundExpression4() {}

    public static CompoundExpression4 getInstance() {
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
        newInfixExpression.setLeftOperand((NumberLiteral) ASTNode.copySubtree(ast, targetLiteral));
        newInfixExpression.setRightOperand(ast.newNumberLiteral("0"));
        newInfixExpression.setOperator(InfixExpression.Operator.MINUS);
        newParenthesizedExpression.setExpression(newInfixExpression);
        astRewrite.replace(targetLiteral, newParenthesizedExpression, null);
        return true;
    }

    @Override
    public String getDescription() {
        return "Creates compound expressions by wrapping number literals in parentheses with minus zero";
    }
}