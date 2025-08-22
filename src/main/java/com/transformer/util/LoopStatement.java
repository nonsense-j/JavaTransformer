package com.transformer.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * Utility class for processing and unifying loop-related statements.
 */
public class LoopStatement {

    private byte tag;
    private Statement loopStatement;
    private Expression expression;
    private Statement body;

    public LoopStatement(ASTNode node) {
        this.tag = -1;
        if (node instanceof ForStatement) {
            this.tag = 0;
            this.loopStatement = (ForStatement) node;
            this.expression = ((ForStatement) node).getExpression();
            this.body = ((ForStatement) this.loopStatement).getBody();
        }
        if (node instanceof WhileStatement) {
            this.tag = 1;
            this.loopStatement = (WhileStatement) node;
            this.expression = ((WhileStatement) node).getExpression();
            this.body = ((WhileStatement) this.loopStatement).getBody();
        }
        if (node instanceof DoStatement) {
            this.tag = 2;
            this.loopStatement = (DoStatement) node;
            this.expression = ((DoStatement) node).getExpression();
            this.body = ((DoStatement) this.loopStatement).getBody();
        }
        if (node instanceof EnhancedForStatement) {
            this.tag = 3;
            this.loopStatement = (EnhancedForStatement) node;
            this.expression = ((EnhancedForStatement) node).getExpression();
            this.body = ((EnhancedForStatement) this.loopStatement).getBody();
        }
        if (tag == -1) {
            throw new IllegalArgumentException("Node is not a loop statement: " + node.getClass().getSimpleName());
        }
    }

    public Expression getExpression() {
        return this.expression;
    }

    public boolean isReachable() {
        if (this.expression instanceof BooleanLiteral && !((BooleanLiteral) this.expression).booleanValue()) {
            return false;
        }
        return true;
    }

    public Statement getBody() {
        return this.body;
    }

    public Statement getLoopStatement() {
        return this.loopStatement;
    }

    public static boolean isLoopStatement(ASTNode node) {
        return node instanceof EnhancedForStatement || node instanceof ForStatement
                || node instanceof WhileStatement || node instanceof DoStatement;
    }

    public boolean isForStatement() {
        return this.loopStatement instanceof ForStatement;
    }

    public boolean isWhileStatement() {
        return this.loopStatement instanceof WhileStatement;
    }

    public boolean isDoStatement() {
        return this.loopStatement instanceof DoStatement;
    }

    public boolean isEnhancedForStatement() {
        return this.loopStatement instanceof EnhancedForStatement;
    }

    @Override
    public String toString() {
        return this.loopStatement.toString();
    }
}