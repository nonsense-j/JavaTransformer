package com.transformer.transform;

import com.transformer.core.TypeWrapper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation that adds static modifier to field declarations.
 */
public class AddStaticModifier extends Transform {

    private static final AddStaticModifier instance = new AddStaticModifier();

    private AddStaticModifier() {}

    public static AddStaticModifier getInstance() {
        return instance;
    }

    @Override
    public boolean run(ASTNode targetNode, TypeWrapper wrapper, ASTNode brotherNode, ASTNode srcNode) {
        AST ast = wrapper.getAst();
        ASTRewrite astRewrite = wrapper.getAstRewrite();
        FieldDeclaration oldFieldDeclaration = (FieldDeclaration) srcNode;
        
        // Check if static modifier already exists
        @SuppressWarnings("unchecked")
        List<ASTNode> modifiers = (List<ASTNode>) oldFieldDeclaration.modifiers();
        for (ASTNode modifier : modifiers) {
            if (modifier instanceof Modifier && ((Modifier) modifier).getKeyword().toString().equals("static")) {
                return false; // Already has static modifier
            }
        }
        
        // Add static modifier
        Modifier staticModifier = ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
        ListRewrite listRewrite = astRewrite.getListRewrite(oldFieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
        listRewrite.insertFirst(staticModifier, null);
        
        // Track the transformation
        wrapper.addAppliedTransform(getIndex(), targetNode);
        
        return true;
    }

    @Override
    public List<ASTNode> check(TypeWrapper wrapper, ASTNode node) {
        List<ASTNode> nodes = new ArrayList<>();
        if (node instanceof FieldDeclaration) {
            // Only add fields that don't already have static modifier
            FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
            @SuppressWarnings("unchecked")
            List<ASTNode> modifiers = (List<ASTNode>) fieldDeclaration.modifiers();
            boolean hasStatic = false;
            for (ASTNode modifier : modifiers) {
                if (modifier instanceof Modifier && ((Modifier) modifier).getKeyword().toString().equals("static")) {
                    hasStatic = true;
                    break;
                }
            }
            if (!hasStatic) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public String getDescription() {
        return "Adds static modifier to field declarations";
    }
}