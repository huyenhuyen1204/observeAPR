package AST.node;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.node.*;
import AST.stm.nodetype.InitNode;
import AST.stm.nodetype.NumbericNode;
import AST.stm.nodetype.StringNode;
import AST.stm.nodetype.UndefinedNode;
import AST.stm.token.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ASTHelper;
import util.Checker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuong on 3/22/2017.
 */


public class MethodNode extends AbstractableElementNode {
    public static final Logger logger = LoggerFactory.getLogger(MethodNode.class);
    // protected boolean isAbstract;
    protected String returnType;
    protected List<Node> parameters;
    private String simpleName;
    private boolean isConstructor = false;
    private int startLine;
    private int endLine;
    //    private boolean returnStringOrNumber = true;
    List<InitNode> initNodes;
    List<StatementNode> returnStatements;
    private static CompilationUnit cu;
    @JsonIgnore
    private List statements;
    private List<StatementNode> statementNodes;

    public List getStatements() {
        return statements;
    }

    public void setStatements(List statements) {
        this.statements = statements;
    }

    public MethodNode() {
        this.initNodes = new ArrayList<>();
        this.returnStatements = new ArrayList<>();
        parameters = new ArrayList<>();
        this.statementNodes = new ArrayList<>();
    }

    public List<StatementNode> getStatementNodes() {
        return statementNodes;
    }

    public void setStatementNodes(List<StatementNode> statementNodes) {
        this.statementNodes = statementNodes;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<InitNode> getInitNodes() {
        return initNodes;
    }

    public void setInitNodes(List<InitNode> initNodes) {
        this.initNodes = initNodes;
    }

    public List<StatementNode> getReturnStatements() {
        return returnStatements;
    }

    public void setReturnStatements(List<StatementNode> returnStatements) {
        this.returnStatements = returnStatements;
    }

    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public void setStartLine(int line) {
        this.startLine = line;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }

    @Override
    public void setEndLine(int line) {
        this.endLine = line;
    }

    public List<ParameterNode> getParameters() {
        ArrayList<ParameterNode> list = new ArrayList<>();
        for (Node node : this.getChildren()) {
            if (node instanceof ParameterNode) {
                list.add((ParameterNode) node);
            }
        }
        return list;
    }

    public void setParameters(List list) {
        this.parameters = list;
    }

    public void addParameter(ParameterNode param) {
        if (param != null) parameters.add(param);
    }

    public void addAllParameters(List<Node> params) {
        parameters.addAll(params);
    }

    public String getSimpleName() {
        return this.simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    @JsonProperty("isConstructor")
    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    @Override
    public String toString() {
        return "MethodNode{" +
                "id=" + this.id +
                "visibility=" + this.getVisibility() +
                ", returnType='" + returnType + '\'' +
                ", name='" + name + '\'' +
                ", isStatic=" + this.isStatic() +
                ", isAbstract=" + this.isAbstract() +
                ", isFinal=" + this.isFinal() +
                ", parameters=" + parameters +
                '}';
    }

    public void setInforFromASTNode(MethodDeclaration node, CompilationUnit cu) {
        this.cu = cu;
        if (node.getName() != null) {
            if (node.getName().getIdentifier() != null) {
                this.name = node.getName().getIdentifier();
            }
        }
        List statements = null;
        if (node.getBody() != null) {
            statements = node.getBody().statements();
        }
        this.setStatements(statements);

        //set ten cho phuong thuc
        this.setStartPosition(node.getStartPosition());
        int nodeLength = node.getLength();
        this.setStartLine(this.cu.getLineNumber(node.getStartPosition()));
        this.setStartPosition(node.getStartPosition());
        this.setEndPosition(node.getStartPosition() + nodeLength);
        int endLineNumber = this.cu.getLineNumber(node.getStartPosition() + nodeLength) - 1;
        this.setEndLine(endLineNumber);
        if (node.isConstructor() == false) {
            Type s = node.getReturnType2();
            if (s != null) {
                this.setReturnType(ASTHelper.getFullyQualifiedName(s, this.cu));
            }
        } else {
            this.isConstructor = true;
            this.setReturnType("");
        }

        List visibilityList = node.modifiers();
        if (visibilityList.size() == 0) this.setVisibility(DEFAULT_MODIFIER);
        else {
            for (Object o : visibilityList) {
                if (o instanceof Modifier) {
                    Modifier m = (Modifier) o;
                    if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.PUBLIC_KEYWORD.toFlagValue()) {
                        this.setVisibility(PUBLIC_MODIFIER);
                    } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.PRIVATE_KEYWORD.toFlagValue()) {
                        this.setVisibility(PRIVATE_MODIFIER);
                    } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.PROTECTED_KEYWORD.toFlagValue()) {
                        this.setVisibility(PROTECTED_MODIFIER);
                    } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.STATIC_KEYWORD.toFlagValue()) {
                        this.setStatic(true);
                    } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.FINAL_KEYWORD.toFlagValue()) {
                        this.setFinal(true);
                    } else if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.ABSTRACT_KEYWORD.toFlagValue()) {
                        this.setAbstract(true);
                    } else {
                        this.setVisibility(DEFAULT_MODIFIER);
                    }
                }
            }
        }

        List parameters = node.parameters();
//        parserAruments(parameters, this.getStartLine());
        List<Node> paraNodes = new ArrayList<>();
        for (Object o : parameters) {
            ParameterNode paraNode = new ParameterNode();
            if (o instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration temp = (SingleVariableDeclaration) o;
                if (temp.getType() != null) {
                    if (temp.getType() instanceof SimpleType) {
                        SimpleType temp2 = (SimpleType) temp.getType();
                        if (temp2.getName() != null) {
                            if (temp2.getName().getFullyQualifiedName() != null) {
                                paraNode.setType(ASTHelper.getFullyQualifiedName(temp2, cu));
                            }
                            if (temp.getName().getIdentifier() != null) {
                                paraNode.setName(temp.getName().getIdentifier());
                            }
                        }
                        List modifiers = temp.modifiers();
                        for (Object m : modifiers) {
                            if (m instanceof Modifier) {
                                Modifier n = (Modifier) m;
                                if (n.getKeyword().toFlagValue() == Modifier.ModifierKeyword.FINAL_KEYWORD.toFlagValue()) {
                                    paraNode.setFinal(true);
                                }
                            }
                        }
                        paraNodes.add(paraNode);
                    } else if (temp.getType() instanceof PrimitiveType) {
                        PrimitiveType primitiveType = (PrimitiveType) temp.getType();
                        if (primitiveType.getPrimitiveTypeCode() != null) {
                            paraNode.setType(ASTHelper.getFullyQualifiedName(primitiveType, cu));
                        }
                        if (temp.getName() != null) {
                            if (temp.getName().getIdentifier() != null) {
                                paraNode.setName(temp.getName().getIdentifier());
                            }
                        }
                        List<Modifier> modifiers = temp.modifiers();
//                        for (Modifier m : modifiers) {
//                            if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.FINAL_KEYWORD.toFlagValue()) {
//                                paraNode.setFinal(true);
//                            }
//                        }
                        paraNodes.add(paraNode);
                    } else if (temp.getType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) temp.getType();
                        paraNode.setType(ASTHelper.getFullyQualifiedName(parameterizedType, cu));
                        if (temp.getName() != null) {
                            if (temp.getName().getIdentifier() != null) {
                                paraNode.setName(temp.getName().getIdentifier());
                            }
                        }
//                        List<Modifier> modifiers = temp.modifiers();
//                        for (Modifier m : modifiers) {
//                            if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.FINAL_KEYWORD.toFlagValue()) {
//                                paraNode.setFinal(true);
//                            }
//                        }
                        paraNodes.add(paraNode);
                    } else if (temp.getType() instanceof ArrayType) {
                        ArrayType arrayType = (ArrayType) temp.getType();
                        paraNode.setType(ASTHelper.getFullyQualifiedName(arrayType, cu));
                        if (temp.getName() != null) {
                            if (temp.getName().getIdentifier() != null) {
                                paraNode.setName(temp.getName().getIdentifier());
                            }
                        }
//                        List<Modifier> modifiers = temp.modifiers();

//                        for (Modifier m : modifiers) {
//                            if (m.getKeyword().toFlagValue() == Modifier.ModifierKeyword.FINAL_KEYWORD.toFlagValue()) {
//                                paraNode.setFinal(true);
//                            }
//                        }
                        paraNodes.add(paraNode);
                    }
                }
                InitNode initNode = new InitNode(1, paraNode.getName(), paraNode.getType(), paraNode.getStartLine());
                initNodes.add(initNode);
            }
        }
        this.addChildren(paraNodes, cu);
        this.addAllParameters(paraNodes);
    }

    //===================== By Huyen: parser statement=============================

    /**
     * parser and get information of statement
     *
     * @param statements
     */
    public List<StatementNode> parserStatements(int level, List statements) {
        List<StatementNode> statementNodeList = new ArrayList<>();
        if (statements != null) {
            for (Object obj : statements) {
                int line = ASTHelper.getLine((ASTNode) obj, cu);
                StatementNode statementNode = null;
                if (obj instanceof Statement) {
                    statementNode = parserStatement(obj, level, line);
                } else if (obj instanceof Block) {
                    List<StatementNode> statementNodes = parserStatements(level + 1, ((Block) obj).statements());
                    statementNodeList.addAll(statementNodes);
                }
                if (statementNode != null) {
                    this.statementNodes.add(statementNode);
                    statementNodeList.add(statementNode);
                }
            }
        }
        return statementNodeList;
    }

    private StatementNode parserStatement(Object obj, int level, int line) {
        StatementNode statementNode = null;
        final int typeNode = ((ASTNode) obj).getNodeType();
        if (Checker.isBreakStatement(typeNode)) {
            // BreakStatement
        } else if (Checker.isConstructorInvocation(typeNode)) {
            // ConstructorInvocation
            List<StatementNode> args = parserObjects(((ConstructorInvocation) obj).arguments(), line);
            setArgumentInstance(args);
//            ConstructorInvocationNode construct = new ConstructorInvocationNode(this.getName(), args, (ASTNode) obj, line);
            MethodCalledNode methodCalledNode = new MethodCalledNode("this", args, (ASTNode) obj, line);
            statementNode = methodCalledNode;
        } else if (Checker.isContinueStatement(typeNode)) {
            // ContinueStatement
        } else if (Checker.isExpressionStatement(typeNode)) {
            // ExpressionStatement
            statementNode = parserExpression(((ExpressionStatement) obj).getExpression(), line);
        } else if (Checker.isReturnStatement(typeNode)) {
            // ReturnStatement
            statementNode = parserExpression(((ReturnStatement) obj).getExpression(), line);

            if (statementNode instanceof Token) {
                (statementNode).setType(this.getReturnType());
            }
            if (statementNode != null) {
                statementNode.setNodeInstance(NodeInstance.RETURN);
            }
        } else if (Checker.isSuperConstructorInvocation(typeNode)) {
            // SuperConstructorInvocation
            //Not used
            MethodInvocationStmNode methodInvocationStmNode = new MethodInvocationStmNode((ASTNode) obj, line);
            BaseVariableNode baseVariableNode = new BaseVariableNode((String) null, null, line);
            methodInvocationStmNode.setBaseVar(baseVariableNode);
            List<StatementNode> args = parserObjects(((SuperConstructorInvocation) obj).arguments(), line);
            setArgumentInstance(args);
            MethodCalledNode methodCalledNode = new MethodCalledNode("super", args, (ASTNode) obj, line);
            methodInvocationStmNode.addMethodCall(methodCalledNode);
            methodInvocationStmNode.addChild(methodCalledNode);
            statementNode = methodInvocationStmNode;
        } else if (Checker.isSwitchCase(typeNode)) {
            // SwitchCase
            //not use
//            logger.info("Chua xu ly isSwitchCase");
        } else if (Checker.isThrowStatement(typeNode)) {
            // ThrowStatement
            //Not used
        } else if (Checker.isTypeDeclarationStatement(typeNode)) {
            // TypeDeclarationStatement
            // parsed - da parser classes
//            logger.error("Chua xu ly isTypeDeclarationStatement");
        } else if (Checker.isIfStatement(typeNode)) {
            //isIfStatement
            statementNode = parserIfStatementInfo((IfStatement) obj, level, line);
        } else if (Checker.isVariableDeclarationStatement(typeNode)) {
            // VariableDeclarationStatement
            VariableDeclarationStatement variable = (VariableDeclarationStatement) obj;
            List<StatementNode> statementNodes = parserVariableDeclarationInfo(level, variable, ASTHelper.getLine((ASTNode) obj, cu));
            this.statementNodes.addAll(statementNodes);
        } else if (Checker.isTryStatement(typeNode)) {
            //isTryStatement
            parserTryInfo((TryStatement) obj, level, line);
        } else if (Checker.isEnhancedForStatement(typeNode)) {
            //isEnhancedForStatement
            parserEnhancedForInfo((EnhancedForStatement) obj, line, level);
        } else if (Checker.isSwitchStatement(typeNode)) {
            //isSwitchStatement
            parserSwitchStatementInfo((SwitchStatement) obj, level);
        } else if (Checker.isForStatement(typeNode)) {
            //isForStatement
            parserForInfo((ForStatement) obj, line, level);
        } else if (Checker.isWhileStatement(typeNode)) {
            //isWhileStatement
            if (((WhileStatement) obj).getBody() instanceof Block) {
                parserStatements(level, ((Block) ((WhileStatement) obj).getBody()).statements());
            } else if (((WhileStatement) obj).getBody() instanceof Statement) {
                statementNode = parserStatement(((WhileStatement) obj).getBody(), level, line);
            } else {
                logger.error("Chua xu ly");
            }
        } else if (Checker.isDoStatement(typeNode)) {
            //isDoStatement
            // not use expression do
            if (((DoStatement) obj).getBody() instanceof Block) {
                Block block = (Block) ((DoStatement) obj).getBody();
                parserStatements(level + 1, block.statements());
            } else {
                Statement stm = ((DoStatement) obj).getBody();
                statementNode = parserStatement(stm, level + 1, line);
            }
        } else if (Checker.isSynchronizedStatement(typeNode)) {
            //NOT parser expression
            if (((SynchronizedStatement) obj).getBody() instanceof Block) {
                Block block = ((SynchronizedStatement) obj).getBody();
                parserStatements(level, block.statements());
            } else {
                statementNode = parserStatement(((SynchronizedStatement) obj).getBody(), level, line);
            }
        } else if (Checker.isLabeledStatement(typeNode)) {
            if (((LabeledStatement) obj).getBody() instanceof Block) {
                parserStatements(level, (((Block) ((LabeledStatement) obj).getBody()).statements()));
            } else if (((LabeledStatement) obj).getBody() instanceof Statement) {
                statementNode = parserStatement(((LabeledStatement) obj).getBody(), level, line);
            } else {
                logger.error("Chua xu ly ((LabeledStatement) obj).getBody() != Statement");
            }
        } else if (Checker.isBlock(typeNode)) {
            parserStatements(level + 1, ((Block)obj).statements());
        } else {
            //ELSE
            if (!(obj instanceof ThrowStatement) && !(obj instanceof EmptyStatement)) {
                logger.error("Chưa xử lý:parserStatements `" + obj.toString() + "`");
            }
        }
        return statementNode;
    }

    /**
     * parser Argurement, if (argurement instanceof MethodInvocation), need to countinue parser
     * and save in List<Object> is list argurements of methodInvocation.
     *
     * @param objects
     * @param line
     * @return
     */
    private List<StatementNode> parserObjects(List objects, int line) {
        List<StatementNode> stmNodes = new ArrayList<>();
        if (objects.size() > 0) {
            for (Object obj : objects) {
                StatementNode statementNode = null;
                if (obj instanceof Expression) {
                    statementNode = parserExpression((Expression) obj, line);
                } else {
                    logger.error("Chua xu ly");
                }
                if (statementNode == null) {
                    statementNode = new UndefinedNode(obj.toString(), (ASTNode) obj, line);
                }
                stmNodes.add(statementNode);
            }
        }
        return stmNodes;
    }

    private void parserTryInfo(TryStatement tryStm, int level, int line) {
        if (tryStm.getBody() != null) {
            if (tryStm.getBody() instanceof Block) {
                List statements = ((Block) tryStm.getBody()).statements();
                parserStatements(level + 1, statements);
                List<CatchClause> catchClauses = tryStm.catchClauses();
                for (CatchClause catchCl : catchClauses) {
                    List statementss = catchCl.getBody().statements();
                    parserStatements(level + 1, statementss);
                }
            } else {
                logger.error("Chưa xử lý:parserTryInfo ");
            }
        }
    }

    private void parserSwitchStatementInfo(SwitchStatement switchStatement, int level) {
        List statements = switchStatement.statements();
        parserStatements(level + 1, statements);
//        for (Object stm : statements) {
//            Statement stmConvert = (Statement) stm;
//            int line.txt = cu.getLineNumber(stmConvert.getStartPosition());
//            if (stm instanceof ReturnStatement) {
//                StatementNode statementNode = parserExpression(((ReturnStatement) stm).getExpression(), line.txt);
//                if (statementNode instanceof Token) {
//                    ((Token) statementNode).setType(this.getReturnType());
//                }
//            }
//        }
    }

    private void parserEnhancedForInfo(EnhancedForStatement stm, int line, int level) {
        if (stm.getParameter() != null) {
            InitNode initNode = new InitNode(level + 1, stm.getParameter().getName().getIdentifier(),
                    ASTHelper.getFullyQualifiedName(stm.getParameter().getType(), cu), line);
            this.initNodes.add(initNode);
        } else {
            logger.error("Chưa xử lý:parserEnhancedForInfo: " + stm.getParameter());
        }
        if (stm.getBody() != null) {
            if (stm.getBody() instanceof Block) {
                List statements = ((Block) stm.getBody()).statements();
                parserStatements(level + 1, statements);
            } else {
                StatementNode statementNode = parserStatement(stm.getBody(), level + 1, line);
                if (statementNode != null) {
                    this.statementNodes.add(statementNode);
                }
//                logger.error("Chưa xử lý:parserEnhancedForInfo ");
            }
        }
    }

    private void parserForInfo(ForStatement stm, int line, int level) {
        List initializers = stm.initializers();
        // step1 parser inits;
        for (Object objInit : initializers) {
            if (objInit instanceof VariableDeclarationStatement) {
                parserVariableDeclarationInfo(level + 1, (VariableDeclarationStatement) objInit, line);
            } else if (objInit instanceof VariableDeclarationExpression) {
                parserVariableDeclarationInfo(level + 1, (VariableDeclarationExpression) objInit, line);
            } else if (objInit instanceof Assignment) {
                StatementNode statementNode = parserAssignmentStm((Expression) objInit, line);
                if (statementNode != null) {
                    this.statementNodes.add(statementNode);
                }
            } else if (objInit instanceof PostfixExpression) {
                //not use
            } else {
                logger.info("Chua xy ly:parserForInfo");
            }
        }
        //TODO: step2: need to parser "optionalConditionExpression (i <n ) of forStatement
        //step 3: parser Block;
        if (stm.getBody() != null) {
            if (stm.getBody() instanceof Block) {
                List statements = ((Block) stm.getBody()).statements();
                parserStatements(level + 1, statements);
            } else {
                StatementNode statementNode = parserStatement(stm.getBody(), level + 1, line);
                if (statementNode != null) {
                    this.statementNodes.add(statementNode);
                }
//                logger.error("Chưa xử lý:parserEnhancedForInfo ");
            }
        }
    }

    private StatementNode parserIfStatementInfo(IfStatement ifStatement, int level, int line) {
        //DK
        StatementNode conditional = parserExpression(ifStatement.getExpression(), line);
        IfStmNode ifStmNode = new IfStmNode(conditional, ifStatement.getExpression(), line);
        if (ifStmNode != null) {
            this.statementNodes.add(ifStmNode);
        }
        if (ifStatement.getThenStatement() != null) {
            if (ifStatement.getThenStatement() instanceof Block) {
                List statements = ((Block) ifStatement.getThenStatement()).statements();
                parserStatements(level + 1, statements);
            } else if (ifStatement.getThenStatement() instanceof Statement) {
                List stms = new ArrayList();
                stms.add(ifStatement.getThenStatement());
                parserStatements(level + 1, stms);
            } else {
                logger.error("Chưa xử lý:parserEnhancedForInfo ");
            }
        }
        if (ifStatement.getElseStatement() != null) {
            if (ifStatement.getElseStatement() instanceof Block) {
                List statements = ((Block) ifStatement.getElseStatement()).statements();
                parserStatements(level + 1, statements);
            } else if (ifStatement.getElseStatement() instanceof Statement) {
                List stms = new ArrayList();
                stms.add(ifStatement.getElseStatement());
                parserStatements(level + 1, stms);
            } else {
                logger.error("Chưa xử lý:parserEnhancedForInfo ");
            }
        }
        return null;
    }


    private List<StatementNode> parserVariableDeclarationInfo(int level, VariableDeclarationStatement variableDeclarationStatement, int line) {
        List<VariableDeclarationFragment> astNodes = variableDeclarationStatement.fragments();
        List<StatementNode> statementNodes = new ArrayList<>();
        for (VariableDeclarationFragment astNode : astNodes) {
            String type = ASTHelper.getFullyQualifiedName(variableDeclarationStatement.getType(), cu);
            InitNode initNode = new InitNode(level, astNode.getName().getIdentifier(),
                    type, line);
            initNodes.add(initNode);

            BaseVariableNode baseVariableNode = new BaseVariableNode(astNode.getName(),
                    initNode.getType(), line);
            StatementNode statementNode = parserExpression(astNode.getInitializer(), line);
            if (statementNode instanceof Token) {
                (statementNode).setType(baseVariableNode.getType());
            }
            baseVariableNode.setNodeInstance(NodeInstance.LEFT_VAR_DECLARATION);
            if (statementNode != null) {
                statementNode.setNodeInstance(NodeInstance.RIGHT_VAR_DECLARATION);
            }
            ExpressionNode expression = new ExpressionNode(baseVariableNode, statementNode, type, astNode, line);
            statementNodes.add(expression);
        }
        return statementNodes;
    }

    private void parserVariableDeclarationInfo(int level, VariableDeclarationExpression variableDeclarationStatement, int line) {
        List<VariableDeclarationFragment> astNodes = variableDeclarationStatement.fragments();
        for (VariableDeclarationFragment astNode : astNodes) {
            InitNode initNode = new InitNode(level, astNode.getName().getIdentifier(),
                    ASTHelper.getFullyQualifiedName(variableDeclarationStatement.getType(), cu), line);
            initNodes.add(initNode);
        }
    }


    private AssignmentNode parserAssignmentStm(Expression expression, int line) {

        Assignment asm = (Assignment) expression;
        StatementNode leftNode = parserExpression(asm.getLeftHandSide(), line);
        if (leftNode != null) {
            leftNode.setNodeInstance(NodeInstance.LEFT_ASSIGNMENT);
        }
        StatementNode rightNode = parserExpression(asm.getRightHandSide(), line);
        if (rightNode != null) {
            rightNode.setNodeInstance(NodeInstance.RIGHT_ASSIGNMENT);
            setType(leftNode, rightNode);
        }
        return new AssignmentNode(asm.getOperator().toString(), leftNode, rightNode, line, asm);
    }

    private void setType(StatementNode leftNode, StatementNode rightNode) {
        if (leftNode instanceof Token && rightNode instanceof Token) {
            if ((rightNode).getType() == null) {
                ( rightNode).setType((leftNode).getType());
            }
        }
    }


    private StatementNode parserExpression(Expression expression, int line) {
        StatementNode statementNode = null;
        if (expression != null) {
            final int typeNode = expression.getNodeType();
            if (Checker.isMethodInvocation(typeNode)
                    || Checker.isSuperMethodInvocation(typeNode)) {
                MethodInvocationStmNode methodInvocationStmNode = new MethodInvocationStmNode(expression, line);
                parserInvoStm(methodInvocationStmNode, expression, line);
                methodInvocationStmNode.setChild();
                statementNode = methodInvocationStmNode;
            } else {
                statementNode = parserExpressionWithoutMethodInvo(expression, line);
            }
        }
        return statementNode;
    }

    private StatementNode parserExpressionWithoutMethodInvo(Expression expression, int line) {
        StatementNode statementNode = null;
        if (expression != null) {
            final int typeNode = expression.getNodeType();
            if (Checker.isArrayAccess(typeNode)) {
                // ArrayAccess
                StatementNode arrayExp = parserExpression(((ArrayAccess) expression).getArray(), line);
                StatementNode indexExp = parserExpression(((ArrayAccess) expression).getIndex(), line);
                ArrayAccessNode arrayAccessNode = new ArrayAccessNode(arrayExp, indexExp, expression, line);
                statementNode = arrayAccessNode;
//                logger.info("isArrayAccess");
            } else if (Checker.isArrayCreation(typeNode)) {
                // ArrayCreation
                //TODO: chua xu ly
                statementNode = new ArrayCreationNode(expression, line);
                if (((ArrayCreation) expression).getInitializer() != null) {
                    StatementNode child = parserExpression(((ArrayCreation) expression).getInitializer(), line);
                    statementNode.setChildren(child);
                }
//                logger.info("isArrayCreation");
            } else if (Checker.isArrayInitializer(typeNode)) {
                // ArrayInitializer
                statementNode = new ArrayInitializerNode(expression, line);
//                logger.info("isArrayInitializer");
            } else if (Checker.isAssignment(typeNode)) {
                // Assignment
                statementNode = parserAssignmentStm(expression, line);
            } else if (Checker.isCastExpression(typeNode)) {
                // CastExpression
                statementNode = parserExpression(((CastExpression) expression).getExpression(), line);
                String type = ASTHelper.getFullyQualifiedName(((CastExpression) expression).getType(), cu);
                if (statementNode instanceof Token) {
                    (statementNode).setType(type);
                }
            } else if (Checker.isClassInstanceCreation(typeNode)) {
                // ClassInstanceCreation
                String type = ASTHelper.getFullyQualifiedName(((ClassInstanceCreation) expression).getType(), cu);
                List<StatementNode> args = parserObjects(((ClassInstanceCreation) expression).arguments(), line);
                setArgumentInstance(args);
                statementNode = new ClassInstanceCreationNode(type, args, (ASTNode) expression, line);
            } else if (Checker.isConditionalExpression(typeNode)) {
                // ConditionalExpression
                ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
                statementNode = parserConditionalExpression(conditionalExpression, line);
                //                InfixExpressionStmNode infixExpressionStmNode = new InfixExpressionStmNode(conditionalExpression)
//                logger.info("isConditionalExpression");
            } else if (Checker.isConstructorInvocation(typeNode)) {
                // ConstructorInvocation
                //not use
                logger.info("isConstructorInvocation");
            } else if (Checker.isFieldAccess(typeNode)) {
                // FieldAccess
                FieldAccess fieldAccess = (FieldAccess) expression;
                StatementNode firstSide = parserExpression(fieldAccess.getExpression(), line);
                StatementNode lastSide = parserExpression(fieldAccess.getName(), line);
                if (firstSide != null) {
                    firstSide.setChildren(lastSide);
                    statementNode = firstSide;
                } else {
                    statementNode = lastSide;
                }
//
//                if (fieldAccess.getExpression() instanceof FieldAccess) {
//                    //to qualifierName
//                    // QualifiedName
//                    InitNode initNode = findTypeVar(((FieldAccess) fieldAccess.getExpression()).getName().getIdentifier(), line.txt);
//                    String type;
//                    if (initNode == null) {
//                        type = ASTHelper.getFullyQualifiedTypeName(((FieldAccess) fieldAccess.getExpression()).getName().getIdentifier(), cu);
//                    } else {
//                        type = initNode.getType();
//                    }
//                    BaseVariableNode qualifier = new BaseVariableNode(((FieldAccess) fieldAccess.getExpression()).getName(), type, line.txt);
//                    BaseVariableNode name = new BaseVariableNode(fieldAccess.getName().getIdentifier(), fieldAccess.getName(), line.txt);
//                    statementNode = new QualifiedNameNode(qualifier, name, expression, line.txt);
//                } else if (fieldAccess.getExpression() instanceof ThisExpression) {
//                    InitNode initNode = ((ClassNode) this.getParent()).findTypeVar(fieldAccess.getName().getIdentifier());
//                    if (initNode != null) {
//                        statementNode = new BaseVariableNode(fieldAccess, initNode.getVarname(), initNode.getType(), cu);
//                    } else {
////                    initNodes.add()
//                        //in super Class => ??????
//                        //TODO: xu ly
//                        StatementNode stm = parserExpression(fieldAccess.getExpression(), line.txt);
//                        if (stm != null) {
//                            StatementNode child = parserExpression(((FieldAccess) expression).getName(), line.txt);
//                            stm.setChildren(child);
//                            statementNode = stm;
//                        }
//                        String type = ASTHelper.getFullyQualifiedTypeName(fieldAccess.getName().getIdentifier(), cu);
//                        logger.info("initNode == null" + expression.getParent().toString());
//                    }
//                } else if (fieldAccess.getExpression() instanceof MethodInvocation) {
//                    MethodInvocationStmNode methodInvocationStmNode = (MethodInvocationStmNode) parserExpression(fieldAccess.getExpression(), line.txt);
//                    BaseVariableNode baseVariableNode = (BaseVariableNode) parserExpression(fieldAccess.getName(), line.txt);
//                    methodInvocationStmNode.setChildren(baseVariableNode);
//                    statementNode = methodInvocationStmNode;
//                } else {
//                    StatementNode stm = parserExpression(fieldAccess.getExpression(), line.txt);
//                    stm.setChildren(stm);
//                    statementNode = stm;
////                    logger.error("Chua xu ly Field Access");
//                }

            } else if (Checker.isThisExpression(typeNode)) {
                if (((ThisExpression)expression).getQualifier() != null) {
                    InitNode initNode = ((ClassNode) this.getParent()).findTypeVar(((ThisExpression) expression).getQualifier().getFullyQualifiedName());
                    if (initNode != null) {
                        statementNode = new BaseVariableNode(((ThisExpression) expression).getQualifier(), initNode.getVarname(), initNode.getType(), cu);
                    } else {
//                    initNodes.add()
                        //in super Class => ??????
                        StatementNode stm = parserExpression(((ThisExpression) expression).getQualifier(), line);
                        if (stm != null) {
                            StatementNode child = parserExpression(((ThisExpression) expression).getQualifier(), line);
                            stm.setChildren(child);
                            statementNode = stm;
                        }
//                    String type = ASTHelper.getFullyQualifiedTypeName(fieldAccess.getName().getIdentifier(), cu);
//                    logger.info("initNode == null" + expression.getParent().toString());
                    }
                }
            } else if (Checker.isInfixExpression(typeNode)) {
                // InfixExpression
                InfixExpression infixEx = (InfixExpression) expression;
                List<StatementNode> objects = parserObjects(infixEx.extendedOperands(), line);
                StatementNode left = parserExpression(infixEx.getLeftOperand(), line);
                StatementNode right = parserExpression(infixEx.getRightOperand(), line);
                statementNode = new InfixExpressionStmNode(infixEx.getOperator().toString(),
                        left, right, objects, line, expression.toString(), infixEx);
            } else if (Checker.isMethodInvocation(typeNode)) {
                // MethodInvocation
                //not use
                logger.info("isMethodInvocation");
            } else if (Checker.isParenthesizedExpression(typeNode)) {
                // ParenthesizedExpression
                statementNode = parserExpression(((ParenthesizedExpression) expression).getExpression(), line);
            } else if (Checker.isPostfixExpression(typeNode)) {
                // PostfixExpression
                StatementNode leftNode = parserExpression(((PostfixExpression) expression).getOperand(), line);
                InfixExpressionStmNode infixExpressionStmNode = new InfixExpressionStmNode(((PostfixExpression) expression).getOperator().toString(),
                        leftNode, null, null, line, expression.toString(), expression);
//                logger.info("isPostfixExpression");
            } else if (Checker.isPrefixExpression(typeNode)) {
                // PrefixExpression
                PrefixExpression preFix = (PrefixExpression) expression;
                StatementNode exp = parserExpression(((PrefixExpression) expression).getOperand(), line);
                statementNode = new InfixExpressionStmNode(preFix.getOperator().toString(), null,
                        exp, null, line, preFix.toString(), expression);
            } else if (Checker.isQualifiedName(typeNode)) {
                // QualifiedName
                InitNode initNode = findTypeVar(((QualifiedName) expression).getQualifier().getFullyQualifiedName(), line);
                String type;
                if (initNode == null) {
                    type = ASTHelper.getFullyQualifiedTypeName(((QualifiedName) expression).getQualifier().getFullyQualifiedName(), cu);
                } else {
                    type = initNode.getType();
                }
                BaseVariableNode qualifier = new BaseVariableNode(((QualifiedName) expression).getQualifier(), type, line);
                BaseVariableNode name = new BaseVariableNode(((QualifiedName) expression).getName(), null, line);
                statementNode = new QualifiedNameNode(qualifier, name, expression, line);
            } else if (Checker.isSuperFieldAccess(typeNode)) {
                // SuperFieldAccess
                logger.info("isSuperFieldAccess");
            } else if (Checker.isSuperMethodInvocation(typeNode)) {
                // SuperMethodInvocation
                //Not use
                logger.info("isSuperMethodInvocation");
            } else if (Checker.isVariableDeclarationExpression(typeNode)) {
                // VariableDeclarationExpression
                logger.info("isVariableDeclarationExpression");
            } else if (Checker.isVariableDeclarationFragment(typeNode)) {
                // VariableDeclarationFragment FIXME: this node is not an expression node.
                logger.info("isVariableDeclarationFragment");
            } else if (Checker.isInstanceofExpression(typeNode)) {
                // InstanceofExpression
                // Not use
//                logger.info("isInstanceofExpression");
            } else if (Checker.isLambdaExpression(typeNode)) {
                // LambdaExpression
//                logger.info("isLambdaExpression");
                LambdaExpression lambdaExpression = (LambdaExpression) expression;
                if (lambdaExpression.getBody() instanceof Block) {
                    parserStatements(-1, ((Block) lambdaExpression.getBody()).statements());
                } else if (lambdaExpression.getBody() instanceof Statement) {
                    statementNode = parserStatement(lambdaExpression.getBody(), -1, line);
                }
            } else if (Checker.isSimpleName(typeNode)) {
                //isSimpleName
                InitNode initNode = findTypeVar(((SimpleName) expression).getIdentifier(), line);
                if (initNode != null) {
                    statementNode = new BaseVariableNode((SimpleName) expression, initNode.getType(), line);
                } else {
                    String type = ASTHelper.getFullyQualifiedTypeName(((SimpleName) expression).getIdentifier(), cu);
                    statementNode = new BaseVariableNode((SimpleName) expression, type, line);
                }
            } else if (Checker.isStringLiteral(typeNode)) {
                //isStringLiteral
                String value = ((StringLiteral) expression).getEscapedValue();
                statementNode = new StringNode(line, null, value, expression.toString(), expression);
            } else if (Checker.isCharacterLiteral(typeNode)) {
                //isCharacterLiteral
                String value = ((CharacterLiteral) expression).getEscapedValue();
                statementNode = new StringNode(line, null, value, expression.toString(), expression);
            } else if (Checker.isNumberLiteral(typeNode)) {
                //isNumberLiteral
                statementNode = new NumbericNode(expression, expression);
            } else if (Checker.isBooleanLiteral(typeNode)) {
                //isBooleanLiteral
                statementNode = new BooleanNode(((BooleanLiteral) expression).booleanValue(), expression, expression.toString(), line);

            } else {
                //ELSE
                statementNode = new UndefinedNode(expression.getClass().getName(), (ASTNode) expression, line);
                if (!Checker.isThisExpression(expression.getNodeType())
                        && !Checker.isNullLiteral(expression.getNodeType())
                        && !Checker.isTypeLiteral(typeNode)) {
                    logger.error("Chưa xử lý:parserExpressionWithoutMethodInvo " + expression.toString());
                }

            }
        }
        return statementNode;
    }

    private void setArgumentInstance(List<StatementNode> args) {
        for (StatementNode statementNode : args) {
            statementNode.setNodeInstance(NodeInstance.ARGUMENT);
        }
    }

    private StatementNode parserConditionalExpression(ConditionalExpression conditionalExpression, int line) {
        StatementNode inFix = parserExpression(conditionalExpression.getExpression(), line);
        StatementNode then = parserExpression(conditionalExpression.getThenExpression(), line);
        StatementNode elseNode = parserExpression(conditionalExpression.getElseExpression(), line);
        StatementNode condition = null;
        if (inFix instanceof InfixExpressionStmNode) {
            condition = inFix;
        } else {
            condition = new InfixExpressionStmNode("", null, inFix, null, line,
                    conditionalExpression.getExpression().toString(), conditionalExpression.getExpression());
        }
        //TODO: noticed a line.txt has multi values
        if (then != null) {
            this.statementNodes.add(then);
        }
        if (elseNode != null) {
            this.statementNodes.add(elseNode);
        }
        IfStmNode ifStmNode = new IfStmNode(condition, conditionalExpression, line);
        return ifStmNode;
    }

    public void parserInvoStm(MethodInvocationStmNode node, Expression expression, int line) {
        if (expression != null) {
            final int typeNode = expression.getNodeType();
            if (Checker.isMethodInvocation(typeNode)
                    || Checker.isSuperMethodInvocation(typeNode)) {
                List<StatementNode> argTypes = new ArrayList<>();
                if (Checker.isMethodInvocation(typeNode)) {
                    argTypes = parserObjects(((MethodInvocation) expression).arguments(), line);
                    setArgumentInstance(argTypes);
                    int endPos = expression.getStartPosition() + expression.getLength();
                    MethodCalledNode methodCalledNode = new MethodCalledNode(((MethodInvocation) expression).getName().getIdentifier(), argTypes, ((MethodInvocation) expression).getName(), line);
                    methodCalledNode.setEndPostion(endPos);
                    node.addMethodCall(methodCalledNode);
                    parserInvoStm(node, ((MethodInvocation) expression).getExpression(), line);
                } else if (Checker.isSuperMethodInvocation(typeNode)) {
                    BaseVariableNode baseVariableNode = new BaseVariableNode("super", expression, line);
                    argTypes = parserObjects(((SuperMethodInvocation) expression).arguments(), line);
                    setArgumentInstance(argTypes);
                    node.setBaseVar(baseVariableNode);
                    MethodCalledNode methodCalledNode = new MethodCalledNode(((SuperMethodInvocation) expression).getName().getIdentifier(), argTypes, ((SuperMethodInvocation) expression).getName(), line);
                    node.addMethodCall(methodCalledNode);
                }
            } else {
                StatementNode statementNode = parserExpressionWithoutMethodInvo(expression, line);
                if (statementNode instanceof BaseVariableNode) {
                    node.setBaseVar((BaseVariableNode) statementNode);
                } else {
                    node.setBaseVar(null);
                }
            }
        }

    }

    /**
     * find index of InitNode in initNodes (initNode has type var)
     *
     * @param varname
     * @param line
     * @return
     */
    public int findIndexTypeVar(String varname, int line) {
        for (int i = 0; i < initNodes.size(); i++) {
            InitNode initNode = initNodes.get(i);
            if (initNode.getVarname().equals(varname)) {
                if (line <= this.getEndLine() &&
                        line >= this.getStartLine()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public InitNode findTypeVar(String varname, int line) {
        for (InitNode init : initNodes) {
            if (init.getVarname() != null) {
                if (init.getVarname().equals(varname)) {
                    if (line <= this.getEndLine() &&
                            line >= this.getStartLine()) {
                        return init;
                    }
//                    else {
//                        logger.error("Chưa xử lý findTypeVar1");
//                    }
                }
            }
        }
        ClassNode classNode = (ClassNode) this.getParent();
        List<InitNode> initNodes = classNode.getInitNodes();
        for (InitNode init : initNodes) {
            if (init.getVarname().equals(varname)) {
                return init;
            }
        }
//        String parent = classNode.getParentClass();
        return null;
    }

    public StatementNode findStatementByLine(int line) {
        for (StatementNode stm : statementNodes) {
            if (stm.getLine() == line) {
                return stm;
            }
        }
        return null;
    }

//    public void addTypeForMethodInvocations(FolderNode folderNode, ClassNode classNode, MethodNode methodNode) {
//        for (StatementNode statementNode : methodNode.getStatementNodes()) {
//            addTypeMethodInvo(statementNode, classNode, folderNode);
//        }
//    }

    public void addTypeForMethodInvocations(FolderNode folderNode, ClassNode classNode, MethodNode methodNode) {
        for (StatementNode statementNode : methodNode.getStatementNodes()) {
            if (statementNode != null) {
                addTypeMethodInvo(statementNode, classNode, folderNode);
            } else {
                logger.error("");
            }
        }
    }

    private static void addTypeMethodInvo(StatementNode statementNode, ClassNode classNode, FolderNode folderNode) {
        if (statementNode instanceof MethodInvocationStmNode) {
            MethodInvocationStmNode methodInvocationStmNode = (MethodInvocationStmNode) statementNode;
            resolveMethodInvocation(methodInvocationStmNode, folderNode, classNode);
        } else if (statementNode instanceof QualifiedNameNode) {
            ClassNode clazz = folderNode.findClassByQualifiedName(((QualifiedNameNode) statementNode).getQualifier().getType());
            String fieldName = ((QualifiedNameNode) statementNode).getName().getKeyVar();
            String fieldType = ASTHelper.findFieldType(folderNode, clazz, fieldName);
            ((QualifiedNameNode) statementNode).getName().setType(fieldType);
        } else {
            if (statementNode.getChildren() != null) {
                for (StatementNode stm : statementNode.getChildren()) {
                    if (stm != null) {
                        addTypeMethodInvo(stm, classNode, folderNode);
                    }
                }
            }
        }
    }

    private static String resolveMethodInvocation(MethodInvocationStmNode methodInvo, FolderNode folderNode, ClassNode classNode) {
        String baseVarType = null;
        if (methodInvo.getMethodType() == null) {
            List<StatementNode> methodCalledNodes = methodInvo.getMethodsCalled();
            if (methodInvo.getBaseVar() != null) {
                baseVarType = methodInvo.getBaseVar().getType();
            } else {
                baseVarType = classNode.getQualifiedName();
            }
            for (StatementNode node : methodCalledNodes) {
                if (node instanceof MethodCalledNode) {
                    baseVarType = resolveMethodCalledNode((MethodCalledNode) node, baseVarType, folderNode, classNode);
                } else if (node instanceof BaseVariableNode) {
                    baseVarType = ( node).getType();
                } else if (node instanceof QualifiedNameNode) {
                    ClassNode clazz = folderNode.findClassByQualifiedName(((QualifiedNameNode) node).getQualifier().getType());
                    InitNode initNode = clazz.findTypeVar(((QualifiedNameNode) node).getName().getKeyVar());
                    baseVarType = initNode.getType();
                    ((QualifiedNameNode) node).getName().setType(initNode.getType());
                } else {
                    logger.error("Chua xu ly !(node instanceof MethodCalled)");
                }
            }
            methodInvo.setMethodType(baseVarType);
        } else {
            return methodInvo.getMethodType();
        }
        return baseVarType;
    }

    private static String resolveMethodCalledNode(MethodCalledNode node, String baseVarType, FolderNode folderNode, ClassNode classNode) {
        List<String> typeParams = new ArrayList<>();
        List<StatementNode> params = node.getAgurements();
        if (params != null) {
            for (StatementNode param : params) {
                if (param instanceof BaseVariableNode) {
                    typeParams.add(((BaseVariableNode) param).getType());
                } else if (param instanceof MethodInvocationStmNode) {
                    String type = resolveMethodInvocation((MethodInvocationStmNode) param, folderNode, classNode);
                    typeParams.add(type);
                } else if (param instanceof QualifiedNameNode) {
                    typeParams.add(((QualifiedNameNode) param).getType());
                } else {
                    typeParams.add(null);
                }
            }
        }
        ClassNode classnode = folderNode.findClassByQualifiedName(baseVarType);
        String type = ASTHelper.findMethodType(folderNode, classnode, node, typeParams);
        node.setMethodType(type);
        return type;
    }

    //    private static void addTypeForList(List<StatementNode> statementNodes, ClassNode classNode, FolderNode folderNode) {
//        for (StatementNode statementNode : statementNodes) {
//            MethodCalledNode methodCalledNode = (MethodCalledNode) methodCall;
//            List<StatementNode> statementNodes = methodCalledNode.getAgurementTypes();
//
//            MethodNode methodNode = classOfVar.findMethodNode(methodCalledNode.getMethodName(), ((MethodCalledNode) methodCall).getAgurementTypes());
////                ((MethodCalledNode)methodInvocationStmNode.getMethodsCalled().get(i)).setMethodType(methodNode.getReturnType());
////                classOfVar = folderNode.findClassByQualifiedName(methodNode.getReturnType());
////                i++;
//        }
//    }


    public void printInfor() {
        System.out.println("Method name: " + name + "   Type: " + returnType + "  Visibility: " + this.getVisibility() + " startline:" + this.getStartLine() + " ENDLINE: " + this.endLine);
    }
}
