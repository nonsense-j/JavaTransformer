package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation that adds redundant arithmetic operations to number literals.
 * Transforms "5" into "(1 + 5 - 1)" to create equivalent but more complex expressions.
 */
public class AddRedundantLiteral extends Transform {

    private static AddRedundantLiteral instance = new AddRedundantLiteral();

    private AddRedundantLiteral() {}

    public static AddRedundantLiteral getInstance() {
        return instance;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherStatement, ASTNode sourceStatement) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        Expression newExpression;

        if (targetNode instanceof NumberLiteral) {
            NumberLiteral targetLiteral = (NumberLiteral) targetNode;
            // Skip hexadecimal floating point literals
            if (targetLiteral.getToken().toLowerCase().contains("0x") && targetLiteral.getToken().contains(".")) {
                return false;
            }
            // Create (1 + original - 1) expression
            InfixExpression newLeft = ast.newInfixExpression();
            String value2add;
            if (targetLiteral.getToken().contains(".")) {
                value2add = "1.0";
            } else {
                value2add = "1";
            }
            newLeft.setLeftOperand(ast.newNumberLiteral(value2add));
            newLeft.setOperator(InfixExpression.Operator.PLUS);
            newLeft.setRightOperand((Expression) ASTNode.copySubtree(ast, targetNode));
            
            InfixExpression newRight = ast.newInfixExpression();
            newRight.setLeftOperand(newLeft);
            newRight.setRightOperand(ast.newNumberLiteral(value2add));
            newRight.setOperator(InfixExpression.Operator.MINUS);

            newExpression = newRight;
        } else if (targetNode instanceof StringLiteral) {
            InfixExpression newRight = ast.newInfixExpression();
            // original + ""
            newRight.setLeftOperand((Expression) ASTNode.copySubtree(ast, targetNode));
            StringLiteral sl = ast.newStringLiteral();
            sl.setLiteralValue("");
            newRight.setRightOperand(sl);
            newRight.setOperator(InfixExpression.Operator.PLUS);
            newExpression = newRight;
        } else if (targetNode instanceof BooleanLiteral) {
            InfixExpression newRight = ast.newInfixExpression();
            // original && true
            newRight.setLeftOperand((Expression) ASTNode.copySubtree(ast, targetNode));
            BooleanLiteral bl = ast.newBooleanLiteral(true);
            newRight.setRightOperand(bl);
            newRight.setOperator(InfixExpression.Operator.AND);
            newExpression = newRight;
        } else if (targetNode instanceof CharacterLiteral) {
            InfixExpression newRight = ast.newInfixExpression();
            // original + '\u0000'
            newRight.setLeftOperand((Expression) ASTNode.copySubtree(ast, targetNode));
            CharacterLiteral cl = ast.newCharacterLiteral();
            cl.setCharValue('\u0000');
            newRight.setRightOperand(cl);
            newRight.setOperator(InfixExpression.Operator.PLUS);
            newExpression = newRight;
        } else {
            return false;
        }
        
        ParenthesizedExpression wrappedNewExpression = ast.newParenthesizedExpression();
        wrappedNewExpression.setExpression(newExpression);
        astRewrite.replace(targetNode, wrappedNewExpression, null);
        
        // Track the transformation
        wrapper.addAppliedTransform(getIndex(), targetNode);
        
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode statement) {
        List<ASTNode> nodes = new ArrayList<>();
        List<ASTNode> subNodes = TypeWrapper.getChildrenNodes(statement);
        for (ASTNode node : subNodes) {
            if (TypeWrapper.isLiteral(node)) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Adds redundant arithmetic operations to number literals";
    }
}