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
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
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
            HashMap<ASTNode, SimpleName> varMap = TypeWrapper.getAllVariables(newStatement);
            HashMap<ASTNode, SimpleName> varReplaceMap = new HashMap<>();
            // substitute every variable in newStatement
            HashMap<String, Integer> paramNameCountMap = new HashMap<>();
            ArrayList<SingleVariableDeclaration> paramNameList = new ArrayList<>();
            ArrayList<ASTNode> argVarList = new ArrayList<>();


            for (Map.Entry<ASTNode, SimpleName> entry : varMap.entrySet()) {
                ASTNode varNode = entry.getKey();
                SimpleName varNameNode = entry.getValue();

                String varSimpleName = varNameNode.getIdentifier();
                int var_id = -1;
                String varName = varSimpleName;
                if (paramNameCountMap.containsKey(varSimpleName)) {
                    var_id = paramNameCountMap.get(varSimpleName);
                    varName = varSimpleName + Integer.toString(var_id);
                    paramNameCountMap.put(varSimpleName, var_id + 1);
                } else {
                    paramNameCountMap.put(varSimpleName, 1);
                }

                SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
                IBinding binding = varNameNode.resolveBinding();
                if (binding == null || binding.getKind() != IBinding.VARIABLE) {
                    return false;
                }
                ITypeBinding varTypeBinding = ((IVariableBinding) binding).getType();
                Type varType = convertTypeBindingToASTType(ast, varTypeBinding);
                param.setType(varType);
                param.setName(ast.newSimpleName(varName));
                paramNameList.add(param);
                argVarList.add(ASTNode.copySubtree(ast, varNode));

                varReplaceMap.put(varNode, ast.newSimpleName(varName));
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
            MethodInvocation methodCall = ast.newMethodInvocation();
            methodCall.setExpression(anonymousInstance);
            methodCall.setName(ast.newSimpleName("execute"));
            for (ASTNode argVar : argVarList) {
                methodCall.arguments().add(argVar);
            }
            
            // replace original stmt
            astRewrite.replace(targetNode, methodCall, null);
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
            if (expression instanceof Assignment) {
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