package com.transformer.core;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import com.transformer.util.LoopStatement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Refactored TypeWrapper class focused on AST manipulation, file I/O, and transformation tracking.
 * Removed bug detection, evaluation, multi-threading, and testing orchestration functionality.
 */
public class TypeWrapper {

    // Core AST components
    private AST ast;
    private ASTRewrite astRewrite;
    private CompilationUnit cu;
    private Document document;
    private ASTParser parser;

    // File information
    private String filePath;
    private String fileName;

    // AST node collections
    private List<ASTNode> allNodes;
    private List<TypeDeclaration> types;
    private HashMap<String, List<ASTNode>> method2statements;
    private HashMap<String, HashSet<String>> method2identifiers;
    private HashMap<String, List<ASTNode>> field2statements;

    // Transformation tracking
    private List<String> appliedTransforms;
    private List<ASTNode> transformedNodes;
    private Map<String, Object> transformationMetadata;
    private List<ASTNode> priorNodes;

    // Compiler options
    public static Map compilerOptions = JavaCore.getOptions();
    static {
        compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
        compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
        compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
    }

    /**
     * Constructor for creating TypeWrapper from file path
     */
    public TypeWrapper(String filePath) {
        this.filePath = filePath;
        File targetFile = new File(filePath);
        this.fileName = targetFile.getName().substring(0, targetFile.getName().length() - 5); // remove .java suffix
        
        try {
            String content = Files.readString(targetFile.toPath(), StandardCharsets.UTF_8);
            this.document = new Document(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
        
        initializeCollections();
        parseToNodes();
    }

    /**
     * Constructor for creating TypeWrapper from content string
     */
    public TypeWrapper(String fileName, String filePath, String content) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.document = new Document(content);
        
        initializeCollections();
        parseToNodes();
    }

    private void initializeCollections() {
        this.appliedTransforms = new ArrayList<>();
        this.transformedNodes = new ArrayList<>();
        this.transformationMetadata = new HashMap<>();
        this.priorNodes = new ArrayList<>();
        this.allNodes = new ArrayList<>();
        this.types = new ArrayList<>();
        this.method2statements = new HashMap<>();
        this.method2identifiers = new HashMap<>();
        this.field2statements = new HashMap<>();
    }

    /**
     * Update AST with new source code
     */
    public void updateAST(String source) {
        this.document = new Document(source);
        parseToNodes();
    }

    /**
     * Rewrite Java code using AST modifications
     */
    public void rewriteAST() {
        TextEdit edits = this.astRewrite.rewriteAST(this.document, null);
        try {
            edits.apply(this.document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to rewrite Java document", e);
        }
        String newCode = this.document.get();
        updateAST(newCode);
    }

    /**
     * Parse document content to AST nodes
     */
    private void parseToNodes() {
        this.parser = ASTParser.newParser(AST.getJLSLatest());
        this.parser.setCompilerOptions(compilerOptions);
        this.parser.setSource(document.get().toCharArray());
        this.cu = (CompilationUnit) parser.createAST(null);
        this.ast = cu.getAST();
        this.astRewrite = ASTRewrite.create(this.ast);
        this.cu.recordModifications();
        
        // Clear existing collections
        this.types.clear();
        this.allNodes.clear();
        this.method2statements.clear();
        this.method2identifiers.clear();
        this.field2statements.clear();
        
        // Extract type declarations
        for (ASTNode node : (List<ASTNode>) this.cu.types()) {
            if (node instanceof TypeDeclaration) {
                this.types.add((TypeDeclaration) node);
            }
        }
        
        // Process each type and extract nodes
        int initializerCount = 0;
        for (TypeDeclaration type : this.types) {
            this.allNodes.add(type);
            List<ASTNode> components = type.bodyDeclarations();
            
            for (ASTNode component : components) {
                this.allNodes.add(component);
                
                if (component instanceof Initializer) {
                    processInitializer((Initializer) component, type, initializerCount++);
                } else if (component instanceof MethodDeclaration) {
                    processMethodDeclaration((MethodDeclaration) component, type);
                } else if (component instanceof FieldDeclaration) {
                    processFieldDeclaration((FieldDeclaration) component, type);
                }
            }
        }
    }

    private void processInitializer(Initializer initializer, TypeDeclaration type, int initializerCount) {
        Block block = initializer.getBody();
        HashSet<String> ids;
        List<ASTNode> statements;
        
        if (block != null && block.statements().size() > 0) {
            ids = getIdentifiers(block);
            statements = getAllStatements(block.statements());
            this.allNodes.addAll(statements);
        } else {
            ids = new HashSet<>();
            statements = new ArrayList<>();
        }
        
        String key = type.getName().toString() + ":Initializer" + initializerCount;
        this.method2identifiers.put(key, ids);
        this.method2statements.put(key, statements);
    }

    private void processMethodDeclaration(MethodDeclaration method, TypeDeclaration type) {
        HashSet<String> ids;
        List<ASTNode> statements;
        Block block = method.getBody();
        
        if (block != null && block.statements().size() > 0) {
            statements = getAllStatements(block.statements());
            this.allNodes.addAll(statements);
            ids = getIdentifiers(block);
        } else {
            statements = new ArrayList<>();
            ids = new HashSet<>();
        }
        
        String key = type.getName().toString() + ":" + createMethodSignature(method);
        this.method2identifiers.put(key, ids);
        this.method2statements.put(key, statements);
    }

    private void processFieldDeclaration(FieldDeclaration field, TypeDeclaration type) {
        // Implementation for processing field declarations
        // get the name of the field
        VariableDeclarationFragment fieldFragment = (VariableDeclarationFragment) field.fragments().get(0);
        SimpleName fieldSimpleName = fieldFragment.getName();
        String fieldName = fieldSimpleName.toString();

        if (field2statements.containsKey(fieldName)) {
            this.field2statements.get(fieldName).add(field);
        }
        else {
            List<ASTNode> statements = new ArrayList<>();
            statements.add(field);
            this.field2statements.put(fieldName, statements);
        }
    }

    /**
     * Write the current AST content to a Java file
     */
    public boolean writeToJavaFile() {
        String code = getCode();
        try {
            File file = new File(this.filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(this.filePath);
            fileWriter.write(code);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to Java file: " + this.filePath, e);
        }
    }

    /**
     * Remove package declaration from the AST
     */
    public void removePackageDeclaration() {
        PackageDeclaration pd = this.cu.getPackage();
        if (pd != null) {
            this.astRewrite.remove(pd, null);
        }
    }

    /**
     * Search for a node by its position in the AST
     */
    public ASTNode searchNodeByPosition(ASTNode oldNode, int oldRowNumber, int oldColNumber) {
        if (oldNode == null) {
            return null;
        }
        
        // First try direct match in allNodes
        for (ASTNode newNode : this.allNodes) {
            int newLineNumber = this.cu.getLineNumber(newNode.getStartPosition());
            int newColNumber = this.cu.getColumnNumber(newNode.getStartPosition());
            if (newLineNumber == oldRowNumber && newColNumber == oldColNumber) {
                if (compareNode(newNode, oldNode)) {
                    return newNode;
                }
            }
        }
        
        // Then try children of allNodes
        for (ASTNode statement : this.allNodes) {
            List<ASTNode> childNodes = getChildrenNodes(statement);
            for (ASTNode childNode : childNodes) {
                if (compareNode(childNode, oldNode)) {
                    int newRowNumber = this.cu.getLineNumber(childNode.getStartPosition());
                    int newColNumber = this.cu.getColumnNumber(childNode.getStartPosition());
                    if (newRowNumber == oldRowNumber && newColNumber == oldColNumber) {
                        return childNode;
                    }
                }
            }
        }
        
        return null;
    }

    // Transformation metadata tracking methods

    /**
     * Add a transformation to the tracking list
     */
    public void addAppliedTransform(String transformName, ASTNode transformedNode) {
        this.appliedTransforms.add(transformName);
        this.transformedNodes.add(transformedNode);
        
        // Update metadata
        this.transformationMetadata.put("lastTransform", transformName);
        this.transformationMetadata.put("transformCount", this.appliedTransforms.size());
        this.transformationMetadata.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    /**
     * Get the sequence of applied transformations
     */
    public List<String> getAppliedTransforms() {
        return new ArrayList<>(this.appliedTransforms);
    }

    /**
     * Get the nodes that were transformed
     */
    public List<ASTNode> getTransformedNodes() {
        return new ArrayList<>(this.transformedNodes);
    }

    /**
     * Get transformation metadata
     */
    public Map<String, Object> getTransformationMetadata() {
        return new HashMap<>(this.transformationMetadata);
    }

    /**
     * Set custom transformation metadata
     */
    public void setTransformationMetadata(String key, Object value) {
        this.transformationMetadata.put(key, value);
    }

    /**
     * Clear transformation tracking data
     */
    public void clearTransformationTracking() {
        this.appliedTransforms.clear();
        this.transformedNodes.clear();
        this.transformationMetadata.clear();
    }
    
    /**
     * Create a copy of this TypeWrapper for mutant generation.
     * The copy shares the same file path but has independent AST and tracking data.
     */
    public TypeWrapper copy() {
        // Create a new TypeWrapper with the same content
        TypeWrapper copy = new TypeWrapper(this.fileName, this.filePath, this.document.get());
        
        // Copy transformation metadata but not applied transforms (each mutant starts fresh)
        copy.transformationMetadata.putAll(this.transformationMetadata);
        
        return copy;
    }

    // Getters
    public String getFileName() {
        return this.fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getCode() {
        return this.document.get();
    }

    public Document getDocument() {
        return this.document;
    }

    public AST getAst() {
        return ast;
    }

    public ASTRewrite getAstRewrite() {
        return astRewrite;
    }

    public CompilationUnit getCompilationUnit() {
        return cu;
    }

    public List<ASTNode> getAllNodes() {
        return new ArrayList<>(this.allNodes);
    }

    public List<TypeDeclaration> getTypes() {
        return new ArrayList<>(this.types);
    }

    public HashMap<String, List<ASTNode>> getMethod2statements() {
        return new HashMap<>(this.method2statements);
    }

    public HashMap<String, HashSet<String>> getMethod2identifiers() {
        return new HashMap<>(this.method2identifiers);
    }

    public HashMap<String, List<ASTNode>> getField2statements() {
        return new HashMap<>(this.field2statements);
    }

    public List<ASTNode> getPriorNodes() {
        return new ArrayList<>(this.priorNodes);
    }

    @Override
    public String toString() {
        return this.filePath;
    }

    // Static utility methods (preserved from original)

    /**
     * Get all children nodes of a given node
     */
    public static List<ASTNode> getChildrenNodes(ASTNode root) {
        ArrayList<ASTNode> nodes = new ArrayList<>();
        if (root == null) {
            return nodes;
        }
        
        ArrayDeque<ASTNode> queue = new ArrayDeque<>();
        queue.add(root);
        
        while (!queue.isEmpty()) {
            ASTNode head = queue.pollFirst();
            List<StructuralPropertyDescriptor> children = (List<StructuralPropertyDescriptor>) head.structuralPropertiesForType();
            
            for (StructuralPropertyDescriptor descriptor : children) {
                Object child = head.getStructuralProperty(descriptor);
                if (child == null) {
                    continue;
                }
                
                if (child instanceof ASTNode) {
                    nodes.add((ASTNode) child);
                    queue.addLast((ASTNode) child);
                } else if (child instanceof List) {
                    List<ASTNode> newChildren = (List<ASTNode>) child;
                    nodes.addAll(newChildren);
                    for (ASTNode node : newChildren) {
                        queue.addLast(node);
                    }
                }
            }
        }
        
        if (nodes.size() == 0) {
            nodes.add(root);
        }
        return nodes;
    }

    /**
     * Get all children nodes from a list of root nodes
     */
    public static List<ASTNode> getChildrenNodes(List<ASTNode> roots) {
        List<ASTNode> nodes = new ArrayList<>();
        for (ASTNode node : roots) {
            nodes.addAll(getChildrenNodes(node));
        }
        return nodes;
    }

    /**
     * Check if a node is a literal
     */
    public static boolean isLiteral(ASTNode astNode) {
        return astNode instanceof StringLiteral || astNode instanceof NumberLiteral
                || astNode instanceof BooleanLiteral || astNode instanceof CharacterLiteral;
    }

    /**
     * Get all statements from a list of statements, including nested ones
     */
    public static List<Statement> getAllStatements(List<Statement> sourceStatements) {
        List<Statement> results = new ArrayList<>();
        if (sourceStatements == null || sourceStatements.size() == 0) {
            return results;
        }
        
        ArrayDeque<Statement> queue = new ArrayDeque<>();
        queue.addAll(sourceStatements);
        
        while (!queue.isEmpty()) {
            Statement head = queue.pollFirst();
            results.add(head);
            
            if (head instanceof IfStatement) {
                queue.addAll(getIfSubStatements((IfStatement) head));
            } else if (head instanceof TryStatement) {
                queue.addAll(((TryStatement) head).getBody().statements());
            } else if (LoopStatement.isLoopStatement(head)) {
                LoopStatement loopStatement = new LoopStatement(head);
                Statement body = loopStatement.getBody();
                if (body instanceof Block) {
                    queue.addAll((List<Statement>) ((Block) body).statements());
                } else {
                    queue.add(body);
                }
            }
        }
        return results;
    }

    /**
     * Get sub-statements from an if statement
     */
    public static ArrayList<Statement> getIfSubStatements(IfStatement target) {
        ArrayList<Statement> results = new ArrayList<>();
        Statement thenStatement = target.getThenStatement();
        Statement elseStatement = target.getElseStatement();
        
        if (thenStatement != null) {
            if (thenStatement instanceof Block) {
                results.addAll(((Block) thenStatement).statements());
            } else {
                results.add(thenStatement);
            }
        }
        
        if (elseStatement != null) {
            if (elseStatement instanceof Block) {
                results.addAll((List<Statement>) ((Block) elseStatement).statements());
            } else {
                results.add(elseStatement);
            }
        }
        return results;
    }

    /**
     * Create method signature string
     */
    public static String createMethodSignature(MethodDeclaration method) {
        StringBuilder signature = new StringBuilder();
        List<ASTNode> parameters = method.parameters();
        signature.append(method.getName().toString());
        
        for (ASTNode parameter : parameters) {
            if (parameter instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration svd = (SingleVariableDeclaration) parameter;
                signature.append(":").append(svd.getType().toString());
            }
        }
        return signature.toString();
    }

    /**
     * Get identifiers from a block
     */
    public static HashSet<String> getIdentifiers(Block block) {
        HashSet<String> identifiers = new HashSet<>();
        for (Statement statement : (List<Statement>) block.statements()) {
            List<ASTNode> subNodes = getChildrenNodes(statement);
            for (ASTNode subNode : subNodes) {
                if (subNode instanceof SimpleName) {
                    identifiers.add(((SimpleName) subNode).getIdentifier());
                }
            }
        }
        return identifiers;
    }

    /**
     * Get variables from ASTNode
     */
    public static HashMap<ASTNode, SimpleName> getAllVariables(ASTNode node) {
        HashMap<ASTNode, SimpleName> varMap = new HashMap<>();
        for (ASTNode childNode : getChildrenNodes(node)) {
            if (childNode instanceof SimpleName) {
                SimpleName simpleName = (SimpleName) childNode;
                IBinding binding = simpleName.resolveBinding();
                if (binding != null && binding.getKind() == IBinding.VARIABLE) {
                    ASTNode parent = simpleName.getParent();
                    if (parent instanceof FieldAccess || parent instanceof QualifiedName) {
                        while (parent.getParent() instanceof FieldAccess || parent.getParent() instanceof QualifiedName) {
                            parent = parent.getParent();
                        }
                        varMap.put(parent, simpleName);
                    } else {
                        varMap.put(simpleName, simpleName);
                    }
                }
            }
        }
        return varMap;
    }

    /**
     * Get the direct method containing a node
     */
    public static MethodDeclaration getDirectMethodOfNode(ASTNode node) {
        if (node == null || node instanceof FieldDeclaration) {
            return null;
        }
        if (node instanceof MethodDeclaration) {
            return (MethodDeclaration) node;
        }
        
        ASTNode parent = node.getParent();
        while (!(parent instanceof MethodDeclaration)) {
            parent = parent.getParent();
            if (parent == null || parent.equals(parent.getParent())) {
                return null;
            }
        }
        return (MethodDeclaration) parent;
    }

    /**
     * Get the class containing a node
     */
    public static TypeDeclaration getClassOfNode(ASTNode node) {
        ASTNode parent = node;
        while (parent != null && !(parent instanceof TypeDeclaration)) {
            parent = parent.getParent();
            if (parent == null || parent.equals(parent.getParent())) {
                return null;
            }
        }
        return (TypeDeclaration) parent;
    }

    /*
     * Get the first brother statement of a statement
     *
     */
    public static ASTNode getFirstBrotherOfStatement(ASTNode statement) {
        if (!(statement instanceof Statement)) {
            return null;
        }
        ASTNode parent = statement.getParent();
        ASTNode currentStatement = statement;
        while (!(parent instanceof Block)) {
            parent = parent.getParent();
            currentStatement = currentStatement.getParent();
            if (parent == null || parent.equals(parent.getParent())) {
                System.err.println("Error in Finding Brother Statement!");
                return null;
            }
        }
        if (!(currentStatement instanceof Statement)) {
            System.err.println("Error: Current Statement cannot be casted to Statement!");
            return null;
        }
        return currentStatement;
    }

    /**
     * Get the direct block containing a statement
     */
    public static Block getDirectBlockOfStatement(ASTNode statement) {
        if (statement instanceof Statement) {
            ASTNode parent = statement.getParent();
            while (!(parent instanceof Block)) {
                parent = parent.getParent();
                if (parent == null || parent.equals(parent.getParent())) {
                    throw new RuntimeException("Error in finding direct block for statement");
                }
            }
            return (Block) parent;
        } else {
            return null;
        }
    }

    /**
     * Check literal type and return corresponding Type
     */
    public static Type checkLiteralType(AST ast, Expression literalExpression) {
        if (literalExpression instanceof NumberLiteral) {
            String token = ((NumberLiteral) literalExpression).getToken();
            if (token.contains(".")) {
                return ast.newPrimitiveType(PrimitiveType.DOUBLE);
            } else {
                if (token.contains("L")) {
                    return ast.newPrimitiveType(PrimitiveType.LONG);
                } else {
                    return ast.newPrimitiveType(PrimitiveType.INT);
                }
            }
        }
        if (literalExpression instanceof StringLiteral) {
            return ast.newSimpleType(ast.newSimpleName("String"));
        }
        if (literalExpression instanceof CharacterLiteral) {
            return ast.newPrimitiveType(PrimitiveType.CHAR);
        }
        if (literalExpression instanceof BooleanLiteral) {
            return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
        }
        return ast.newSimpleType(ast.newSimpleName("Object"));
    }

    /**
     * Get the statement containing a node
     */
    public static ASTNode getStatementOfNode(ASTNode node) {
        if (node == null) {
            return null;
        }
        ASTNode parNode = node;
        while (parNode != null && !(parNode instanceof Statement || parNode instanceof FieldDeclaration)) {
            parNode = parNode.getParent();
        }
        return parNode;
    }

    /**
     * Check if a class has properties that make it suitable for transformation
     */
    @SuppressWarnings("unchecked")
    public static boolean checkClassProperty(TypeDeclaration clazz) {
        if (clazz == null || clazz.isInterface()) {
            return false;
        }
        
        // Check for annotations that indicate test or UI classes
        List<ASTNode> modifiers = (List<ASTNode>) clazz.modifiers();
        for (ASTNode modifier : modifiers) {
            if (modifier instanceof MarkerAnnotation) {
                String name = ((MarkerAnnotation) modifier).getTypeName().getFullyQualifiedName();
                if (name.contains("MainThread")) {
                    return false;
                }
            }
        }
        
        // Check superclass
        if (clazz.getSuperclassType() != null && clazz.getSuperclassType().toString().contains("TestCase")) {
            return false;
        }
        
        // Check interfaces
        if (clazz.superInterfaceTypes() != null) {
            List<ASTNode> interfaces = (List<ASTNode>) clazz.superInterfaceTypes();
            for (ASTNode node : interfaces) {
                if (node.toString().contains("Serializable")) {
                    return false;
                }
            }
        }
        
        // Check imports for test frameworks
        if (clazz.getParent() instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) clazz.getParent();
            @SuppressWarnings("unchecked")
            List<ImportDeclaration> imports = cu.imports();
            for (ImportDeclaration im : imports) {
                String name = im.getName().getFullyQualifiedName();
                if (name.contains("org.junit.jupiter") || name.contains("org.junit")) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Compare two nodes for structural equality (simplified version)
     */
    public static boolean compareNode(ASTNode node1, ASTNode node2) {
        if (node1 == null || node2 == null) {
            return node1 == node2;
        }
        return node1.getClass().equals(node2.getClass()) && 
               node1.toString().equals(node2.toString());
    }
}