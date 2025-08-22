package com.transformer.strategy;

import com.transformer.api.BugInformation;
import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Location strategy that uses bug information to guide node selection for
 * transformations.
 * This strategy implements Statfier's complete getCandidateNodes() logic
 * including data flow analysis.
 */
public class GuidedLocationStrategy implements LocationStrategy {

    /**
     * Creates a new GuidedLocationStrategy.
     */
    public GuidedLocationStrategy() {
    }

    /**
     * Select candidate AST nodes for transformation using optimal signature.
     * This method uses only the parameters it actually needs.
     * 
     * @param wrapper TypeWrapper containing the parsed AST
     * @param bugInfo Bug information to guide node selection
     * @return List of candidate AST nodes selected based on bug information
     */
    public List<ASTNode> selectCandidateNodes(TypeWrapper wrapper, BugInformation bugInfo) {
        // Validate parameters first
        validateBugInformation(bugInfo);
        
        if (wrapper == null) {
            return new ArrayList<>();
        }

        // Use complete getCandidateNodes() approach from Statfier
        return getCandidateNodes(wrapper, bugInfo.getBugLines());
    }

    /**
     * Complete implementation of Statfier's getCandidateNodes() method including
     * data flow analysis.
     * This method not only finds nodes at bug lines but also analyzes data
     * dependencies.
     * 
     * @param wrapper    TypeWrapper containing the parsed AST
     * @param validLines List of line numbers where bugs were detected (equivalent
     *                   to file2row.get(filePath))
     * @return List of candidate AST nodes including data flow related nodes
     */
    private List<ASTNode> getCandidateNodes(TypeWrapper wrapper, List<Integer> validLines) {
        List<ASTNode> resNodes = new ArrayList<>();

        if (validLines == null) { // no warning in this file
            return resNodes;
        }

        // Step 1: Find nodes at exact bug lines
        if (validLines.size() > 0) {
            for (ASTNode node : wrapper.getAllNodes()) {
                int row = wrapper.getCompilationUnit().getLineNumber(node.getStartPosition());
                if (validLines.contains(row)) {
                    resNodes.add(node);
                }
            }
        }

        // Step 2: Add nodes from prior transformations (if any)
        for (ASTNode priorNode : wrapper.getPriorNodes()) {
            if (priorNode instanceof IfStatement) {
                resNodes.add(((IfStatement) priorNode).getExpression());
            }
            if (priorNode instanceof WhileStatement) {
                resNodes.add(((WhileStatement) priorNode).getExpression());
            }
            if (priorNode instanceof FieldDeclaration) {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((FieldDeclaration) priorNode)
                        .fragments().get(0);
                if (fragment.getInitializer() != null) {
                    resNodes.add(fragment.getInitializer());
                }
            }
        }

        if (resNodes.isEmpty()) {
            return resNodes;
        }

        // Step 3: Add literal expressions in if statements
        List<ASTNode> nodes2add = new ArrayList<>();
        for (ASTNode node : resNodes) {
            Block block = TypeWrapper.getDirectBlockOfStatement(node);
            if (block != null) {
                ASTNode outNode = block.getParent();
                if (outNode instanceof IfStatement && TypeWrapper.isLiteral(((IfStatement) outNode).getExpression())) {
                    nodes2add.add(((IfStatement) outNode).getExpression());
                }
            }
        }
        resNodes.addAll(nodes2add);

        // Step 4: Data flow analysis - find related variables and assignments
        HashSet<String> sources = new HashSet<>();
        List<ASTNode> methodNodes = new ArrayList<>();
        Expression rightExpression = null;

        // Extract variable names from assignments and declarations
        for (ASTNode node : resNodes) {
            // Handle field declarations
            if (node instanceof FieldDeclaration) {
                String fieldName = ((VariableDeclarationFragment) ((FieldDeclaration) node).fragments().get(0))
                        .getName().getFullyQualifiedName();

                // Find method statements that use this field
                for (Map.Entry<String, List<ASTNode>> entry : wrapper.getMethod2statements().entrySet()) {
                    List<ASTNode> statements = entry.getValue();
                    for (ASTNode statement : statements) {
                        if (statement instanceof VariableDeclarationStatement) {
                            break;
                        }
                        if (statement instanceof ExpressionStatement
                                && ((ExpressionStatement) statement).getExpression() instanceof Assignment) {
                            for (ASTNode subNode : TypeWrapper.getChildrenNodes(statement)) {
                                if (subNode instanceof SimpleName
                                        && ((SimpleName) subNode).getIdentifier().equals(fieldName)) {
                                    methodNodes.add(statement);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Extract right-hand side expressions from variable declarations
            if (node instanceof VariableDeclarationStatement) {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement) node)
                        .fragments().get(0);
                rightExpression = fragment.getInitializer();
            }

            // Extract right-hand side expressions from assignments
            if (node instanceof ExpressionStatement
                    && ((ExpressionStatement) node).getExpression() instanceof Assignment) {
                rightExpression = ((Assignment) ((ExpressionStatement) node).getExpression()).getRightHandSide();
            }
        }

        // Step 5: Analyze data dependencies
        if (rightExpression != null) {
            // Extract variable names from simple name expressions
            if (rightExpression instanceof SimpleName) {
                sources.add(((SimpleName) rightExpression).getIdentifier());
            }

            if (rightExpression instanceof Expression) {
                for (ASTNode subNode : TypeWrapper.getChildrenNodes(rightExpression)) {
                    if (subNode instanceof SimpleName) {
                        sources.add(((SimpleName) subNode).getIdentifier());
                    }
                }
            }

            // Extract variable names from constructor calls
            if (rightExpression instanceof ClassInstanceCreation || rightExpression instanceof MethodInvocation) {
                List<Expression> arguments;
                if (rightExpression instanceof ClassInstanceCreation){
                    arguments = ((ClassInstanceCreation) rightExpression).arguments(); 
                }
                else {
                    arguments = ((MethodInvocation) rightExpression).arguments(); 
                }
                for (Expression argument : arguments) {
                    if (argument instanceof MethodInvocation) {
                        Expression argExpr = ((MethodInvocation) argument).getExpression();
                        if (argExpr instanceof SimpleName) {
                            sources.add(((SimpleName) argExpr).getIdentifier());
                        }
                    }
                }
            }

            // Find related statements in the same method
            if (!resNodes.isEmpty()) {
                MethodDeclaration method = TypeWrapper.getDirectMethodOfNode(resNodes.get(0));
                if (method != null) {
                    Block block = method.getBody();
                    if (block != null) {
                        List<Statement> subStatements = TypeWrapper.getAllStatements(block.statements());
                        for (Statement statement : subStatements) {
                            // Find assignments to variables we're tracking
                            if (statement instanceof ExpressionStatement
                                    && ((ExpressionStatement) statement).getExpression() instanceof Assignment) {
                                Assignment assignment = (Assignment) ((ExpressionStatement) statement).getExpression();
                                if (assignment.getLeftHandSide() instanceof SimpleName) {
                                    if (sources.contains(((SimpleName) assignment.getLeftHandSide()).getIdentifier())) {
                                        resNodes.add(statement);
                                    }
                                }
                            }

                            // Find variable declarations for variables we're tracking
                            if (statement instanceof VariableDeclarationStatement) {
                                VariableDeclarationFragment vd = (VariableDeclarationFragment) ((VariableDeclarationStatement) statement)
                                        .fragments().get(0);
                                String varName = vd.getName().getIdentifier();
                                if (sources.contains(varName)) {
                                    resNodes.add(statement);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 6: Filter out unwanted node types
        for (int i = resNodes.size() - 1; i >= 0; i--) {
            ASTNode resNode = resNodes.get(i);
            if (resNode instanceof TypeDeclaration || resNode instanceof Initializer ||
                    resNode instanceof EnumDeclaration || resNode instanceof AnnotationTypeDeclaration) {
                resNodes.remove(i);
            }
        }

        // Step 7: Add method-level nodes
        resNodes.addAll(methodNodes);

        return resNodes;
    }

    @Override
    public String getStrategyName() {
        return "GUIDED_LOCATION";
    }
    
    /**
     * Validate bug information parameters.
     * 
     * @param bugInfo Bug information to validate
     * @throws IllegalArgumentException if parameters are invalid for this strategy
     */
    public void validateBugInformation(BugInformation bugInfo) throws IllegalArgumentException {
        if (bugInfo == null) {
            throw new IllegalArgumentException("GuidedLocationStrategy requires bug information");
        }
        
        if (!bugInfo.hasBugs()) {
            throw new IllegalArgumentException("GuidedLocationStrategy requires bug=true");
        }
        
        if (bugInfo.getBugLines() == null || bugInfo.getBugLines().isEmpty()) {
            throw new IllegalArgumentException("GuidedLocationStrategy requires valid bug lines");
        }
    }

    @Override
    public String toString() {
        return "GuidedLocationStrategy{strategyName=" + getStrategyName() + "}";
    }
}