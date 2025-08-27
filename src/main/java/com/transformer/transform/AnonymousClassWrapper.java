package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Transformation that wraps methods and fields in anonymous class declarations.
 */
public class AnonymousClassWrapper extends Transform {

    private static int varCounter = 0;
    private static AnonymousClassWrapper instance = new AnonymousClassWrapper();

    public static AnonymousClassWrapper getInstance() {
        return instance;
    }

    private AnonymousClassWrapper() {}

    @Override
    @SuppressWarnings("unchecked")
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brother, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        AnonymousClassDeclaration anonymousClassDecl = ast.newAnonymousClassDeclaration();

        if (targetNode instanceof Statement) {
            // original_stmt -> new Object() { void execute() { original_stmt; } }.execute();
            Statement newStatement = (Statement) ASTNode.copySubtree(ast, targetNode);
            LinkedHashSet<ASTNode> varSet = TypeWrapper.getAllVariables(newStatement);
            HashMap<ASTNode, SimpleName> varReplaceMap = new HashMap<>();
            ArrayList<SingleVariableDeclaration> paramNameList = new ArrayList<>();
            ArrayList<ASTNode> argVarList = new ArrayList<>();

            for (ASTNode varNode : varSet) {
                String varStr = varNode.toString();
                // clear empty characters and get the transformed name like this.var -> thisVar, org.exp.Class.var -> orgExpClassVar
                varStr = varStr.replaceAll("\\s+", "");
                if (varStr.isEmpty()) {
                    continue;
                }
                
                // replace all non-word characters(including _) with . 
                varStr = varStr.replaceAll("[^a-zA-Z0-9]+", ".");
                String[] subVarStrs = varStr.split("\\.");
                if (subVarStrs.length < 1)
                    continue;
                String newVarStr = "";
                for (int i = 0; i < subVarStrs.length; i++) {
                    String subVarStr = subVarStrs[i];
                    if (i == 0)
                        newVarStr += subVarStr.toLowerCase();
                    else
                        newVarStr += subVarStr.substring(0, 1).toUpperCase() + subVarStr.substring(1).toLowerCase();
                }
                SimpleName newVarNameNode = ast.newSimpleName(newVarStr);
                varReplaceMap.put(varNode, newVarNameNode);

                SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
                param.setType(ast.newSimpleType(ast.newSimpleName("Object")));
                param.setName(ast.newSimpleName(newVarStr));
                paramNameList.add(param);

                argVarList.add(ASTNode.copySubtree(ast, varNode));
            }

            // create execute method
            MethodDeclaration executeMethod = ast.newMethodDeclaration();
            executeMethod.setName(ast.newSimpleName("execute"));
            executeMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
            for (SingleVariableDeclaration param : paramNameList) {
                executeMethod.parameters().add(ASTNode.copySubtree(ast, param));
            }
            Block methodBody = ast.newBlock();
            methodBody.statements().add(newStatement);
            executeMethod.setBody(methodBody);
            
            // add method to anonymousClassDecl
            anonymousClassDecl.bodyDeclarations().add(executeMethod);

            // create methodCall with obj instance
            ClassInstanceCreation anonymousInstance = ast.newClassInstanceCreation();
            anonymousInstance.setAnonymousClassDeclaration(anonymousClassDecl);
            anonymousInstance.setType(ast.newSimpleType(ast.newSimpleName("Object")));
            MethodInvocation methodCall = ast.newMethodInvocation();
            methodCall.setExpression(anonymousInstance);
            methodCall.setName(ast.newSimpleName("execute"));
            for (ASTNode argVar : argVarList) {
                methodCall.arguments().add(argVar);
            }
            ExpressionStatement methodCallStmt = ast.newExpressionStatement(methodCall);
            
            // replace original stmt
            astRewrite.replace(targetNode, methodCallStmt, null);
            for  (Map.Entry<ASTNode, SimpleName> entry : varReplaceMap.entrySet()) {
                ASTNode varNode = entry.getKey();
                SimpleName varNameNode = entry.getValue();
                astRewrite.replace(varNode, varNameNode, null);
            }
            
            // Track the transformation
            wrapper.addAppliedTransform(getIndex(), targetNode);
            return true;

        } else if (targetNode instanceof Expression) {
            // {type} xxx = origianlExpr  -> {type} xxx = new Object() { {type} execute({type} x) { return x; } }.execute(x);
            Type exprType;
            if (srcNode instanceof VariableDeclarationStatement) {
                VariableDeclarationStatement vdStatement = (VariableDeclarationStatement) srcNode;
                exprType = vdStatement.getType();
            } else if (srcNode instanceof FieldDeclaration) {
                FieldDeclaration fd = (FieldDeclaration) srcNode;
                exprType = fd.getType();
            } else {
                return false;
            }
            Expression originalExpression = (Expression) targetNode;
            
            // create execute method
            MethodDeclaration executeMethod = ast.newMethodDeclaration();
            executeMethod.setName(ast.newSimpleName("execute"));
            Type returnType = (Type) ASTNode.copySubtree(ast, exprType);
            executeMethod.setReturnType2(returnType);
            SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
            Type paramType = (Type) ASTNode.copySubtree(ast, exprType);
            param.setType(paramType);
            param.setName(ast.newSimpleName("x"));
            executeMethod.parameters().add(param);
            Block methodBody = ast.newBlock();
            ReturnStatement returnStmt = ast.newReturnStatement();
            returnStmt.setExpression(ast.newSimpleName("x"));
            methodBody.statements().add(returnStmt);
            executeMethod.setBody(methodBody);

            // add method to anonymousClassDecl
            anonymousClassDecl.bodyDeclarations().add(executeMethod);

            // create methodCall with obj instance
            ClassInstanceCreation anonymousInstance = ast.newClassInstanceCreation();
            anonymousInstance.setAnonymousClassDeclaration(anonymousClassDecl);
            anonymousInstance.setType(ast.newSimpleType(ast.newSimpleName("Object")));
            MethodInvocation methodCall = ast.newMethodInvocation();
            methodCall.setExpression(anonymousInstance);
            methodCall.setName(ast.newSimpleName("execute"));
            methodCall.arguments().add(
                (Expression) ASTNode.copySubtree(ast, originalExpression)
            );
            
            // replace original expr
            astRewrite.replace(targetNode, methodCall, null);
            return true; 
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        TypeDeclaration clazz = TypeWrapper.getClassOfNode(node);
        
        if (!TypeWrapper.checkClassProperty(clazz)) {
            return nodes;
        }

        ASTNode statement = TypeWrapper.getStatementOfNode(node);
        if (statement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) statement).getExpression();
            if (!(expression instanceof Assignment)) {
                nodes.add(statement);
            }
        } else if (statement instanceof VariableDeclarationStatement) {
            VariableDeclarationStatement vdStatement = (VariableDeclarationStatement) statement;
            VariableDeclarationFragment vdFragment = (VariableDeclarationFragment) vdStatement.fragments().get(0);
            Expression initExpr = vdFragment.getInitializer();
            if (initExpr != null) {
                nodes.add(initExpr);
            }
        } else if (statement instanceof FieldDeclaration) {
            FieldDeclaration fd = (FieldDeclaration) statement;
            VariableDeclarationFragment fdFragment = (VariableDeclarationFragment) fd.fragments().get(0);
            Expression initExpr = fdFragment.getInitializer();
            if (initExpr != null) {
                nodes.add(initExpr);
            }
        }
        
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Wraps methods and fields in anonymous class declarations";
    }

    private Type convertTypeBindingToASTType(AST ast, ITypeBinding typeBinding) {
        if (typeBinding.isPrimitive()) {
            return ast.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName()));
        }else if (typeBinding.isArray()) {
            Type componentType = convertTypeBindingToASTType(ast, typeBinding.getElementType());
            ArrayType arrayType = ast.newArrayType(componentType, typeBinding.getDimensions());
            return arrayType;
        }
        
        return ast.newSimpleType(ast.newSimpleName("Object"));
    }
}