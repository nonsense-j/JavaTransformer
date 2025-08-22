package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform that transfers local variables to global fields.
 * This transformation moves literal values from local scope to class-level fields.
 */
public class TransferLocalVarToGlobal extends Transform {

    private static int varCounter;
    private static TransferLocalVarToGlobal instance = new TransferLocalVarToGlobal();

    private TransferLocalVarToGlobal() {
        varCounter = 0;
    }

    public static TransferLocalVarToGlobal getInstance() {
        return instance;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherStatement, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        Expression literalNode = (Expression) targetNode;
        TypeDeclaration clazz = TypeWrapper.getClassOfNode(srcNode);
        String newVarName = "localGLOBAL" + varCounter++;
        SimpleName newVar = ast.newSimpleName(newVarName);
        VariableDeclarationFragment newVdFragment = ast.newVariableDeclarationFragment();
        newVdFragment.setName(newVar);
        newVdFragment.setInitializer((Expression) ASTNode.copySubtree(ast, literalNode));
        FieldDeclaration newFieldDeclaration = ast.newFieldDeclaration(newVdFragment);
        newFieldDeclaration.setType(TypeWrapper.checkLiteralType(ast, literalNode));
        
        @SuppressWarnings("unchecked")
        List<Modifier> modifiers = newFieldDeclaration.modifiers();
        modifiers.add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        
        ListRewrite listRewrite = astRewrite.getListRewrite(clazz, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertFirst(newFieldDeclaration, null);
        astRewrite.replace(literalNode, newVar, null);
        return true;
    }

    /**
     * Simple utility to check if a node has static modifier
     */
    private boolean hasStaticModifier(ASTNode node) {
        List<ASTNode> modifiers = null;
        if (node instanceof FieldDeclaration) {
            @SuppressWarnings("unchecked")
            List<ASTNode> fieldModifiers = ((FieldDeclaration) node).modifiers();
            modifiers = fieldModifiers;
        }
        if (node instanceof VariableDeclarationStatement) {
            @SuppressWarnings("unchecked")
            List<ASTNode> varModifiers = ((VariableDeclarationStatement) node).modifiers();
            modifiers = varModifiers;
        }
        if (modifiers == null) {
            return false;
        }
        for (ASTNode modifier : modifiers) {
            if (modifier instanceof Modifier && ((Modifier) modifier).getKeyword().equals(Modifier.ModifierKeyword.STATIC_KEYWORD)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode srcNode) {
        List<ASTNode> nodes = new ArrayList<>();
        TypeDeclaration clazz = TypeWrapper.getClassOfNode(srcNode);
        if(clazz == null || clazz.isInterface()) {
            return nodes;
        }
        MethodDeclaration method = TypeWrapper.getDirectMethodOfNode(srcNode);
        if (method != null) {
            @SuppressWarnings("unchecked")
            List<ASTNode> methodModifiers = method.modifiers();
            for (ASTNode modifier : methodModifiers) {
                if(modifier instanceof Modifier) {
                    if(((Modifier) modifier).getKeyword().toString().contains("static")) {
                        return nodes;
                    }
                }
            }
        } else {
            ASTNode parNode = srcNode.getParent();
            while(parNode != null) {
                if(parNode instanceof Initializer) {
                    return nodes;
                }
                parNode = parNode.getParent();
            }
        }
        if(hasStaticModifier(srcNode)) {
            return nodes;
        }
        List<ASTNode> subNodes = TypeWrapper.getChildrenNodes(srcNode);
        for (int i = 0; i < subNodes.size(); i++) {
            ASTNode subNode = subNodes.get(i);
            if (TypeWrapper.isLiteral(subNode)) {
                nodes.add(subNode);
            }
        }
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Transfers local variables to global fields";
    }
}