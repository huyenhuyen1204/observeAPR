package AST.node;

import AST.obj.Position;
import AST.parser.Convert;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ASTHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuong on 3/22/2017.
 */


public class ClassNode extends AbstractableElementNode {
    public static final Logger logger = LoggerFactory.getLogger(ClassNode.class);
    protected boolean isInterface;
    protected String parentClass;
    protected List<String> interfaceList;
    protected String qualifiedName;
    //    protected String type;
    protected int numOfmethod;
    protected int numOfvariable;
    protected int line;
    @JsonIgnore
    protected List<InitNode> initNodes; //to save var & type when init
//    HashMap<Integer, QualifiedNameNode> qualifiers;
//
//    public HashMap<Integer, QualifiedNameNode> getQualifiers() {
//        return qualifiers;
//    }
//
//    public void setQualifiers(HashMap<Integer, QualifiedNameNode> qualifiers) {
//        this.qualifiers = qualifiers;
//    }

    public String getParentClass() {
        return parentClass;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }

    public List<String> getInterfaceList() {
        return interfaceList;
    }

    public void setInterfaceList(List<String> interfaceList) {
        this.interfaceList = interfaceList;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public int getNumOfmethod() {
        return numOfmethod;
    }

    public void setNumOfmethod(int numOfmethod) {
        this.numOfmethod = numOfmethod;
    }

    public int getNumOfvariable() {
        return numOfvariable;
    }

    public void setNumOfvariable(int numOfvariable) {
        this.numOfvariable = numOfvariable;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public List<InitNode> getInitNodes() {
        return initNodes;
    }

    public void setInitNodes(List<InitNode> initNodes) {
        this.initNodes = initNodes;
    }

    public ClassNode() {
        super();
        interfaceList = new ArrayList<>();
        this.initNodes = new ArrayList<>();
//        this.qualifiers = new HashMap<>();
    }


    @JsonProperty("isInterface")
    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    @JsonIgnore
    public List<MethodNode> getMethodList() {
        List<MethodNode> result = new ArrayList<>();
        for (Node child : this.getChildren()) {
            if (child instanceof MethodNode)
                result.add((MethodNode) child);
        }
        return result;
    }

    @JsonIgnore
    public List<FieldNode> getFieldList() {
        List<FieldNode> result = new ArrayList<>();
        for (Node child : this.getChildren()) {
            if (child instanceof FieldNode)
                result.add((FieldNode) child);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ClassNode {" +
                "id=" + this.id +
                "visibility=" + this.getVisibility() +
//                ", type='" + type + '\'' +
                ", name=" + this.name +
                ", isStatic=" + this.isStatic() +
                ", isAbstract=" + this.isAbstract() +
                ", isFinal=" + this.isFinal() +
                ", isInterface=" + isInterface +
                ", parentClass='" + parentClass + '\'' +
                ", interfaceList=" + interfaceList +
                '}';
    }

    public void setInforFromASTNode(TypeDeclaration node, CompilationUnit cu) {
        if (node.isInterface() == true) this.setInterface(true);
        Position position = ASTHelper.getPosition(node);
        this.setStartLine(ASTHelper.getLine(position.getStartPos(), cu));
        this.setEndLine(ASTHelper.getLine(position.getEndPos(), cu));

        //lay ten
        if (node.getName() != null) {
            if (node.getName().getIdentifier() != null) {
                String name = node.getName().getIdentifier();
                this.setName(name);
            }
        }
//        this.setQualifiedName(name);
        PackageDeclaration packageDeclaration = cu.getPackage();
        if (packageDeclaration != null) {
            if (packageDeclaration.getName() != null) {
                if (packageDeclaration.getName().getFullyQualifiedName() != null) {
                    this.setQualifiedName(packageDeclaration.getName().getFullyQualifiedName() + "." + name);
                }
            }
        } else {
            this.setQualifiedName(name);
        }

        //lay visibility
        this.setVisibility(DEFAULT_MODIFIER);
        List modifiers = node.modifiers();
        if (modifiers.size() == 0) {
            this.setVisibility(DEFAULT_MODIFIER);
        } else {
            for (Object o : modifiers) {
                //System.out.println(o.getKeyword().toString());
                if (o instanceof Modifier) {
                    Modifier m = (Modifier) o;
                    if (m.getKeyword() != null) {
                        if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.PUBLIC_KEYWORD.toFlagValue()) {
                            this.setVisibility(PUBLIC_MODIFIER);
                        } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.STATIC_KEYWORD.toFlagValue()) {
                            this.setStatic(true);
                        } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.ABSTRACT_KEYWORD.toFlagValue()) {
                            this.setAbstract(true);
                        } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.FINAL_KEYWORD.toFlagValue()) {
                            this.setFinal(true);
                        } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.PRIVATE_KEYWORD.toFlagValue()) {
                            this.setVisibility(PRIVATE_MODIFIER);
                        } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.PROTECTED_KEYWORD.toFlagValue()) {
                            this.setVisibility(PROTECTED_MODIFIER);
                        } else {
                            this.setVisibility(DEFAULT_MODIFIER);
                        }

                    }
                }
            }
        }

        //lay cac properties
        FieldDeclaration[] fieldList = node.getFields();
        List<Node> fieldNodes = Convert.convertASTListNodeToFieldNode(fieldList, cu, initNodes);

        this.addChildren(fieldNodes, cu);
        //lay cac methods
        MethodDeclaration[] methodList = node.getMethods();
        List<Node> methodNodes = Convert.convertASTListNodeToMethodNode(methodList, cu);
        this.addChildren(methodNodes, cu);

        parserStatements(methodNodes);

        //TODO lay cac class con ben trong
        TypeDeclaration[] classList = node.getTypes();
        List<Node> innerClassNode = Convert.convertASTListNodeToClassNode(classList, cu);
        this.addChildren(innerClassNode, cu);

        //lay superClass
        Type superClassType = node.getSuperclassType();
        if (superClassType != null && superClassType instanceof SimpleType) {
            SimpleType superSimpleClassType = (SimpleType) superClassType;
            if (superSimpleClassType.getName() != null) {
                String fullname = ASTHelper.getFullyQualifiedName(superClassType, cu);
                this.setParentClass(fullname);
            }
        }

        //lay danh sach cac superInterface
        List superInterfaceList = node.superInterfaceTypes();
        if (superInterfaceList.size() > 0) {
            ArrayList<String> interfaceNameList = new ArrayList<String>();
            for (Object o : superInterfaceList) {
                if (o instanceof SimpleType) {
                    SimpleType intefaceType = (SimpleType) o;
                    String fullInterfaceName = ASTHelper.getFullyQualifiedName(intefaceType, cu);
                    interfaceNameList.add(fullInterfaceName);
                }
            }
            this.setInterfaceList(interfaceNameList);
        }

    }

    private void parserStatements(List<Node> methodNodes) {
        for (Node node : methodNodes) {
            MethodNode methodNode = (MethodNode) node;
            methodNode.parserStatements(1, methodNode.getStatements());
        }
    }

    public StatementNode findStatemmentByLine(int line) {
        List<MethodNode> methodNodes = this.getMethodList();
        for (MethodNode methodNode : methodNodes) {
            if (methodNode.getStartLine() <= line && methodNode.getEndLine() >= line) {
                return methodNode.findStatementByLine(line);
            }
        }
        return null;
    }

    public int findIndexTypeVar(String varname) {
        for (int i = 0; i < initNodes.size(); i++) {
            InitNode initNode = initNodes.get(i);

            if (initNode.getVarname().equals(varname)) {
                return i;
            }

        }
        return -1;
    }

    public InitNode findTypeVar(String varname) {
        for (int i = 0; i < initNodes.size(); i++) {
            InitNode initNode = initNodes.get(i);
            if (initNode.getVarname().equals(varname)) {
                return initNodes.get(i);
            }
        }
        return null;
    }

//    public MethodNode findMethodNode(String methodName, List<String> params) {
//        List<MethodNode> methodNodes = new ArrayList<>();
//        for (MethodNode methodNode : getMethodList()) {
//            if (methodName.equals(methodNode.getName())) {
//                if (compareParams(methodNode.getParameters(), params))
//                    methodNodes.add(methodNode);
//            }
//        }
//        if (methodNodes.size() == 1) {
//            return methodNodes.get(0);
//        } else if (methodNodes.size() > 1) {
//            logger.error("THIS IS ERR");
//            return null;
//        }
//        return null;
//    }

    public List<MethodNode> findMethodNode(String methodName, List<String> params) {
        List<MethodNode> methodNodes = new ArrayList<>();
        for (MethodNode methodNode : getMethodList()) {
            if (methodName.equals(methodNode.getName())) {
                if (compareParams(methodNode.getParameters(), params))
                    methodNodes.add(methodNode);
            }
        }
//        if (methodNodes.size() == 1) {
//            return methodNodes.get(0);
//        } else if (methodNodes.size() > 1) {
//            logger.error("THIS IS ERR");
//            return null;
//        }
        return methodNodes;
    }

    public MethodNode findMethodNodeByStmLine(String methodName, int line) {
        for (MethodNode methodNode : getMethodList()) {
            if (methodName.equals(methodNode.getName())) {
                if (line <= methodNode.getEndLine()
                        && line >= methodNode.getStartLine())
                    return methodNode;
            }
        }
        return null;
    }

    public MethodNode findMethodNodeByStmLine(int line) {
        for (MethodNode methodNode : getMethodList()) {
            if (line <= methodNode.getEndLine()
                    && line >= methodNode.getStartLine())
                return methodNode;
        }
        return null;
    }

    private boolean compareParams(List<ParameterNode> parameterNodes, List<String> params) {
        boolean result = true;
        if (parameterNodes.size() == params.size()) {
            if (params.size() == 0) {
                return true;
            }
            for (int i = 0; i < parameterNodes.size(); i++) {
                if (params.get(i) != null) {
                    if (!params.get(i).equals(parameterNodes.get(i).getType())) {
                        result = false;
                        break;
                    }
                }
            }
//            for (ParameterNode parameterNode : parameterNodes) {
//                for (String param : params) {
//                    if (param != null) {
//                        if (!param.equals(parameterNode.getType())) {
//                            result = false;
//                            break;
//                        }
//                    }
//                }
//            }
            return result;
        } else {
            return false;
        }
    }


}