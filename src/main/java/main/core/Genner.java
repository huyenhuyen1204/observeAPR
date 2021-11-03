package main.core;

import AST.node.ClassNode;
import AST.node.FolderNode;
import AST.node.MethodNode;
import AST.stm.abst.StatementNode;
import AST.stm.node.OperatorNode;
import AST.stm.token.*;
import main.core.pattern.BasePattern;
import main.core.pattern.InfixPattern;
import main.core.pattern.MethodPattern;
import main.core.pattern.Pattern;
import main.core.token.MethodToken;
import main.core.token.OperatorToken;
import main.core.token.Token;
import main.core.token.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FindingAPI;
import util.ReflectionHelper;

import java.util.*;

public class Genner {
    private static Logger log = LoggerFactory.getLogger(Genner.class);

    private StatementNode targetToken;
    private MethodNode methodNode; // the method where the target token is
    private FolderNode folderNode;
    private String nodeType;
    private List<Pattern> patterns;
    public final static int MAX_DEEP_LEVEL = 1;
    private static HashMap<Integer, List<StatementNode>> nodeMap = new HashMap<>();

    public Genner(StatementNode targetToken, FolderNode folderNode, MethodNode methodNode) {
        this.targetToken = targetToken;
        if (targetToken instanceof MethodInvocationStmNode) {
            this.nodeType = ((MethodInvocationStmNode) targetToken).getMethodType();
        } else {
            this.nodeType = targetToken.getType();
        }
        this.folderNode = folderNode;
        this.methodNode = methodNode;
        this.patterns = new ArrayList<>();
    }

    /**
     * @param targetNode a token candidate
     * @param methodNode the method where the target Node is
     * @param folderNode the folder contains
     * @return list of candidates for the target node
     */
    public static List<PatchCandidate> generateFixCandidates(StatementNode targetNode,
                                                             MethodNode methodNode, FolderNode folderNode) {
        Genner genner = new Genner(targetNode, folderNode, methodNode); // e.g. cfa.method(b,c)
        // 1. generate tokens for Nodes
        log.info("Generate tokens for Nodes");
        genner.genTokens(targetNode); // gen token for each node e.g. cfa -> <Type>, method(b, c) -> method(<int>, <int>)
        // 2. synthesize pattern for the target node
        log.info("Synthesize pattern for the target node");
        List<Pattern> patterns = genner.generatePatterns(targetNode);
        genner.setPatterns(patterns); // <Type>.method(), cfa.anotherMethod(<int.)

        // 3. from synthesized patterns, generate source node candidate
        log.info("Generate candidates");
        genner.generatePatchCandidates();
        log.info("Finishing generate candidates");
        Map<String, PatchCandidate> sourceNodeCandiesMap = new HashMap<>();
        for (Pattern pattern : genner.getPatterns()) {
            for (PatchCandidate candidate : pattern.getPatchCandidates()) {
                candidate.setTargetNode(pattern.getTargetNode());
                sourceNodeCandiesMap.put(candidate.toString(), candidate);
            }
        }

        return new ArrayList<>(sourceNodeCandiesMap.values());
    }

    private void generatePatchCandidates() {
        for (Pattern pattern : patterns) {
            generatePatchCandidate(pattern);
        }
    }


    private List<Pattern> generatePatterns(StatementNode targetNode) {
        List<Pattern> patterns = new ArrayList<>();
        boolean isSameMethodInvo = false; // Qualified same MethodInvocation: Class.Var
        boolean isSameBaseVar = false; //Qualified same base var: Enum.Var
        if (targetNode instanceof QualifiedNameNode) {
            //TODO: edit with Class.Enum.var
            if (ReflectionHelper.isEnum(targetNode.getChildren().get(0).getType())) {
                isSameBaseVar = true;
            } else {
                isSameMethodInvo = true; // eg: that.field
            }
        } else if (targetNode instanceof BaseVariableNode) { //eg: this.field
            if (targetNode.getChildren().size() > 0) {
                if (targetNode.getChildren().get(0) instanceof BaseVariableNode) {
                    isSameMethodInvo = true;
                }
            }
        }
        // 1. if the target token is a base variable
        if (targetNode instanceof BaseVariableNode && !isSameMethodInvo
                || isSameBaseVar
                || targetNode instanceof BooleanNode) {
            List<Token> tokens = targetNode.getToken() == null ? null : Arrays.asList(targetNode.getToken());
            if (tokens != null) {
                for (Token token : tokens) {
                    Change change = new Change();
                    change.setOriginalNode(targetNode);
                    change.setToken(token);
                    token.setOriginalValue(targetNode.toString()); // a basevar

                    BasePattern basePattern = new BasePattern();
                    basePattern.setTargetNode(targetNode);
                    basePattern.getChanges().add(change);
                    patterns.add(basePattern);
                }
            }
        } else if (targetNode instanceof MethodInvocationStmNode) {
            return generateMethodPatterns(targetNode, targetNode);
        } else if (targetNode instanceof ClassInstanceCreationNode) {
            return generateMethodPatterns(targetNode, targetNode);
        } else if (targetNode instanceof InfixExpressionStmNode) {
            return generateInfixPattens(targetNode, targetNode);
        } else if (isSameMethodInvo) {
            return generateQualifierPattern(targetNode, targetNode);
        }
        return patterns;
    }

    private List<Pattern> generateQualifierPattern(StatementNode targetNode, StatementNode currentNode) {
            List<Pattern> patterns = new ArrayList<>();
            // 1. by modifying current node
            List<Token> tokens = currentNode.getToken() == null ? null : Collections.singletonList(currentNode.getToken());
            if (tokens != null) {
                for (Token token : tokens) {
                    Change change = new Change();
                    change.setOriginalNode(currentNode);
                    change.setToken(token);

                    BasePattern basePattern = new BasePattern();
                    basePattern.setTargetNode(targetNode);
                    basePattern.getChanges().add(change);
//                    basePattern.toString();
                    patterns.add(basePattern);
                }
            }

        // 2. by modifying the rest of the method invocation
            if (currentNode.getChildren().size() > 1) {
                for (int i = 1; i < currentNode.getChildren().size(); i++) {
                    Change change = new Change();
                    change.setOriginalNode(currentNode.getChildren().get(0));
                    change.setToken((Token) currentNode.getChildren().get(i));

                    BasePattern basePattern = new BasePattern();
                    basePattern.setTargetNode(targetNode);
                    basePattern.getChanges().add(change);

                    patterns.add(basePattern);
                }
            }

            if (currentNode.getChildren().size() > 0) {
                for (StatementNode child : currentNode.getChildren()) {
                    patterns.addAll(generateQualifierPattern(targetNode, child));
                }
            } else {
//                //child:
//                if (currentNode.getType() != null) {
//                    Token token = new TypeToken(targetNode, Token.Scope.ALL_AFTER);
//                    token.setNodeType(currentNode.getType());
//                    Change change = new Change();
//                    change.setOriginalNode(targetNode);
//                    change.setToken(token);
//
//                    BasePattern basePattern = new BasePattern();
//                    basePattern.setTargetNode(targetNode);
//                    basePattern.getChanges().add(change);
//                    patterns.add(basePattern);
//                }
            }

            return patterns;
    }

    private List<Pattern> generateInfixPattens(StatementNode statementNode, StatementNode targetNode) {
        List<Pattern> patterns = new ArrayList<>();
        if (statementNode instanceof InfixExpressionStmNode) {
            //infix sketch
            Token token = statementNode.getToken();
            if (token != null) {
                InfixPattern infixPattern = new InfixPattern();
                //create changes
                Change change = new Change();
                change.setOriginalNode(((InfixExpressionStmNode) statementNode).getOperator());
                change.setToken(token);
                infixPattern.getChanges().add(change);
                //set target
                infixPattern.setTargetNode(targetToken);
                patterns.add(infixPattern);
            }
            //others
            StatementNode left = ((InfixExpressionStmNode) statementNode).getLeft();
            //if left != instance of infix PatchInfix = add
            if (!(left instanceof InfixExpressionStmNode)) {
                if (left.getToken() != null) {
                    List<Pattern> leftPatterns = generatePatterns(left);
                    List<InfixPattern> infixPatterns = convertToInfixPatterns(leftPatterns);
                    patterns.addAll(infixPatterns);
                }
            } else {
                List<Pattern> infixPatterns = generateInfixPattens(left, targetToken);
                patterns.addAll(infixPatterns);
            }
            StatementNode right = ((InfixExpressionStmNode) statementNode).getRight();
            if (!(right instanceof InfixExpressionStmNode)) {
                if (right.getToken() != null) {
                    List<Pattern> rightPatterns = generatePatterns(right);
                    List<InfixPattern> infixPatterns = convertToInfixPatterns(rightPatterns);
                    patterns.addAll(infixPatterns);
                }
            } else {
                List<Pattern> infixPatterns = generateInfixPattens(right, targetToken);
                patterns.addAll(infixPatterns);
            }
        }
        return patterns;
    }

    private List<InfixPattern> convertToInfixPatterns(List<Pattern> leftPatterns) {
        List<InfixPattern> infixPatterns = new ArrayList<>();
        for (Pattern pattern : leftPatterns) {
            InfixPattern infixPattern = new InfixPattern();
            List<Change> changes = new ArrayList<>(pattern.getChanges());
            infixPattern.setChanges(changes);
            infixPattern.setTargetNode(targetToken);
            infixPatterns.add(infixPattern);
        }
        return infixPatterns;
    }



    private List<Pattern> generateMethodPatterns(StatementNode methodInvo, StatementNode currentNode) {
        List<Pattern> patterns = new ArrayList<>();
        // 1. by modifying current node
        List<Token> tokens = currentNode.getToken() == null ? null : Collections.singletonList(currentNode.getToken());
        if (tokens != null) {
            for (Token token : tokens) {
                Change change = new Change();
                change.setOriginalNode(currentNode);
                change.setToken(token);

                BasePattern basePattern = new BasePattern();
                basePattern.setTargetNode(methodInvo);
                basePattern.getChanges().add(change);

                patterns.add(basePattern);
            }
        }

        // 2. by modifying the rest of the method invocation
        if (currentNode.getChildren().size() > 1) {
            for (int i = 1; i < currentNode.getChildren().size(); i++) {
                Change change = new Change();
                change.setOriginalNode(currentNode.getChildren().get(0));
                change.setToken((Token) currentNode.getChildren().get(i));

                BasePattern basePattern = new BasePattern();
                basePattern.setTargetNode(methodInvo);
                basePattern.getChanges().add(change);

                patterns.add(basePattern);
            }
        }
        //Patterns for params
        if (currentNode instanceof MethodCalledNode
                || currentNode instanceof ClassInstanceCreationNode) { // allowed to change more than one element (e.g. two arguments of a method)
            List<List<Pattern>> arguments = new ArrayList<>();
            List<StatementNode> args = new ArrayList<>();
            if (currentNode instanceof MethodCalledNode) {
                args = ((MethodCalledNode) currentNode).getAgurements();
            } else {
                args = ((ClassInstanceCreationNode) currentNode).getArgs();
            }
            for (StatementNode argument : args) {
                List<Pattern> argPatterns = generatePatterns(argument);
                argPatterns.add(null);
                arguments.add(argPatterns);
            }
            //Synthesis
            List<List<Pattern>> synthesis = new ArrayList<>();
            for (List<Pattern> arg : arguments) {
                if (synthesis.size() == 0) {
                    for (Pattern pattern : arg) {
                        List<Pattern> list = new ArrayList<>();
                        list.add(pattern);
                        synthesis.add(list);
                    }
                } else {
                    List<List<Pattern>> tmpSynthesis = new ArrayList<>();
                    for (Pattern pattern : arg) {
                        for (List<Pattern> synthesizedList : synthesis) {
                            List<Pattern> list = new ArrayList<>(synthesizedList);
                            list.add(pattern);
                            tmpSynthesis.add(list);
                        }
                    }

                    synthesis.clear();
                    synthesis.addAll(tmpSynthesis);
                }
            }

            for (List<Pattern> combination : synthesis) {
                MethodPattern pattern = new MethodPattern();
                pattern.setTargetNode(methodInvo);
                pattern.setChangedMethod(currentNode);

                Map<StatementNode, Pattern> changedArgsMap = new HashMap<>();
                for (Pattern pt : combination) {
                    if (pt != null) {
                        pattern.getChanges().addAll(pt.getChanges());
                        changedArgsMap.put(pt.getTargetNode(), pt);
                    }
                }
                pattern.setChangedArgsMap(changedArgsMap);

                if (pattern.getChanges().size() > 0)
                    patterns.add(pattern);
            }
        }

        if (currentNode.getChildren().size() > 0)
            patterns.addAll(generateMethodPatterns(methodInvo, currentNode.getChildren().get(0)));

        return patterns;

    }

    private void genTokens(StatementNode node) {
        boolean isSameMethodInvo = false; // Qualified same MethodInvocation: Class.Var
        boolean isSameBaseVar = false; //Qualified same base var: Enum.Var
        if (node instanceof QualifiedNameNode) {
            //TODO: edit with Class.Enum.var
            if (ReflectionHelper.isEnum(node.getChildren().get(0).getType())) {
                isSameBaseVar = true;
            } else {
                isSameMethodInvo = true;
            }
        }
        if (node instanceof MethodInvocationStmNode || isSameMethodInvo) {
            // we generate Token for the whole method invocation
            // method invocation -> <returnType>

            StatementNode child = node.getChildren().get(0);
            if (child.getChildren().size() > 0) { // when the child has child
                TypeToken typeToken = new TypeToken(child, Token.Scope.ALL_AFTER);
                typeToken.setNodeType(this.nodeType);
                typeToken.setOriginalValue(node.toString()); // redundant
//                node.getChildren().add(typeToken); // same level of the child node
                node.setToken(typeToken);
            }
        } else if (node instanceof BaseVariableNode || isSameBaseVar) {
            // e.g. var node in var.method(b, c).method(d, e)
            // => <varType>.method(b,c).method(d,e)
            // or var.<returnType>
            // 1. var -> <varType>
            // 2. method(b, c).method(d, e) -> <returnType>

            // 1. generate sketch for the current node
            TypeToken typeToken = null;
            if (!isSameBaseVar) {
                typeToken = new TypeToken(node, Token.Scope.ONLY_CURRENT); //baseVar
            } else {
                typeToken = new TypeToken(node, Token.Scope.ALL_AFTER); //qualifier
            }
            typeToken.setNodeType(node.getType());
//            typeToken.setNodeType(node.getType());
            typeToken.setOriginalValue(node.toString());
            // ===save to sketchMap (candidates)
//            List<Token> sketchNodes = new ArrayList<>();
//            sketchNodes.add(sketchNode);
            node.setToken(typeToken);

            // 2. generate candidate sketches for the rest of the method invocation (e.g. method(b,c).method(d,e) -> <returnType>)
            // Run when the first child of this node is parent of another node (e.g. method(b,c) is parent of method(d,e))
            if (!isSameBaseVar) {
                StatementNode child = null;
                if (node.getChildren().size() > 0) child = node.getChildren().get(0);
                if (child != null && child.getChildren().size() > 0) { // the child has child
                    TypeToken rest = new TypeToken(child, Token.Scope.ALL_AFTER);
                    rest.setNodeType(this.nodeType);
                    String suffix = child.getSuffix();
                    suffix = suffix.equals("") ? suffix : "." + suffix;
                    rest.setOriginalValue(child.toString() + suffix);
                    node.getChildren().add(rest); // same level of the child node
                }
            }
        } else if (node instanceof MethodCalledNode) {
            // e.g. method1(b,c).method2(b2,c2).method3(b3, c3)
            // => (1) <method1ReturnType>.method2(b2,c2).method3(b3, c3)
            //    (2) method1(b,c).<returnType>
            //    (3) method1(<int>, c).method2(b2,c2).method3(b3, c3)
            //    (3) method1(b, <int>).method2(b2,c2).method3(b3, c3)
            //    (3) method1(<int>, <int>).method2(b2,c2).method3(b3, c3)

            // 1. generate sketch for the current node
            TypeToken typeToken = new TypeToken(node, Token.Scope.ONLY_CURRENT);
            typeToken.setNodeType(node.getType());
            typeToken.setOriginalValue(node.toString());
            // ===save to sketchMap (candidates)
//            List<Token> sketchNodes = new ArrayList<>();
//            sketchNodes.add(normSketch);
            node.setToken(typeToken);

            // 2. generate method sketch for the rest of the method invocation
            StatementNode child = null;
            if (node.getChildren().size() > 0) child = node.getChildren().get(0);
            if (child != null && child.getChildren().size() > 0) { // the child has child
                Token restSketch = new TypeToken(child, Token.Scope.ALL_AFTER);
                restSketch.setNodeType(this.nodeType);
                String suffix = child.getSuffix();
                suffix = suffix.equals("") ? suffix : "." + suffix;
                restSketch.setOriginalValue(child.toString() + suffix);
                node.getChildren().add(restSketch); // same level of the child node
            }
            // 3. generate sketches for all parameters of the method
            for (StatementNode param : ((MethodCalledNode) node).getAgurements()) {
                //add level
                param.setStmBugDeepLevel(param, node.getStmBugDeepLevel() + 1);

                Genner paramGenner = new Genner(param, this.folderNode, this.methodNode);
                paramGenner.genTokens(param);
            }

        } else if (node instanceof ClassInstanceCreationNode) {
            // 1. generate sketch for the current node
            TypeToken typeToken = new TypeToken(node, Token.Scope.ONLY_CURRENT);
            typeToken.setNodeType(node.getType());
            typeToken.setOriginalValue(node.toString());
            // ===save to sketchMap (candidates)
//            List<Token> sketchNodes = new ArrayList<>();
//            sketchNodes.add(normSketch);
            node.setToken(typeToken);

            // 2. generate method sketch for the rest of the method invocation
            StatementNode child = null;
            if (node.getChildren().size() > 0) child = node.getChildren().get(0);
            if (child != null && child.getChildren().size() > 0) { // the child has child
                TypeToken rest = new TypeToken(child, Token.Scope.ALL_AFTER);
                rest.setNodeType(this.nodeType);
                String suffix = child.getSuffix();
                suffix = suffix.equals("") ? suffix : "." + suffix;
                rest.setOriginalValue(child.toString() + suffix);
                node.getChildren().add(rest); // same level of the child node
            }
            // 3. generate sketches for all parameters of the method
            for (StatementNode param : ((ClassInstanceCreationNode) node).getArgs()) {
                param.setStmBugDeepLevel(param, node.getStmBugDeepLevel() + 1);
                Genner paramGenner = new Genner(param, this.folderNode, this.methodNode);
                paramGenner.genTokens(param);
            }
        } else if (node instanceof InfixExpressionStmNode) {
            //1. a/b * c-> <int>
            // <a> / b * c; <
            OperatorToken operator = generateOperatorToken((InfixExpressionStmNode) node);
            if (operator != null) {
                node.setToken(operator);
            }
            Genner leftNode = new Genner(((InfixExpressionStmNode) node).getLeft(), this.folderNode, this.methodNode);
            if (leftNode != null) {
                leftNode.genTokens(((InfixExpressionStmNode) node).getLeft());
            }
            Genner rightNode = new Genner(((InfixExpressionStmNode) node).getRight(), this.folderNode, this.methodNode);
            if (rightNode != null) {
                rightNode.genTokens(((InfixExpressionStmNode) node).getRight());
            }
        } else if (node instanceof BooleanNode) {
            TypeToken token = new TypeToken(node, Token.Scope.ONLY_CURRENT);
            token.setNodeType(BooleanType.BooleanNode.toString());
            token.setOriginalValue(node.toString());
            node.setToken(token);
        }

        if (node.getChildren().size() > 0 && !isSameBaseVar && !(node instanceof InfixExpressionStmNode)) {
            for (StatementNode child : node.getChildren())
            genTokens(child);
        }

    }

    private OperatorToken generateOperatorToken(InfixExpressionStmNode statementNode) {
        OperatorNode operator = statementNode.getOperator();
        for (String operatorType : operatorCandidates.keySet()) {
            List<String> values = operatorCandidates.get(operatorType);
            if (values.contains(((InfixExpressionStmNode) statementNode)
                    .getOperator().getOperator())) {
                OperatorToken operatorToken = new OperatorToken(statementNode.getOperator(),
                        Token.Scope.ONLY_CURRENT);
                operatorToken.setNodeType(operatorType.toString());
                operatorToken.setOriginalValue(operator.getOperator());
                return operatorToken;
            }
        }
        return null;
    }


    private void generatePatchCandidate(Pattern pattern) {
        if (pattern instanceof InfixPattern) {
            ((InfixPattern) pattern).genCandidates(this);
        } else {
            //save to candidates
//            pattern.setMethodNode(this.methodNode);
            pattern.computePatchCandidates(this);
        }
    }

    // ================= GENERATE CANDIDATE ========================
    public void generateCandidates(Token token) {
        if (token instanceof TypeToken) {
            token.setCandidates(new ArrayList<>());
            if (token.getNodeType() != null) {
                //check enum &methodNode find candidates
                if (ReflectionHelper.isEnum(token.getNodeType())) {
                    List<StatementNode> qualifiedNameNodes = ReflectionHelper.findEnum(token.getNodeType());
                    token.getCandidates().addAll(qualifiedNameNodes);
                }
                List<BaseVariableNode> baseVarCandies = new ArrayList<>();
                //find basevar candidates
                if (token.getTargetScope() == Token.Scope.ONLY_CURRENT) {
                    baseVarCandies = findBaseVarSameType(token.getParentType(),
                            token.getNodeType(), this.targetToken.getLine(), this.methodNode);
                } else if (token.getTargetScope() == Token.Scope.ALL_AFTER) {
                    baseVarCandies = findBaseVarSameType(null,
                            token.getNodeType(), this.targetToken.getLine(), this.methodNode);
                }
                token.getCandidates().addAll(baseVarCandies);

                //gen for booleanNode true -> false, false -> true

                if (token.getNodeType().equals(BooleanType.BooleanNode.toString())) {
                    BooleanNode booleanNode = genBooleanCandidates((BooleanNode) token.getTarget());
                    token.getCandidates().add(booleanNode);
                }
            }
            //find method sameType and can access (should rank 2)
            List<StatementNode> methods = findMethodCalledSameType(token.getParentType(),
                    token.getNodeType(), (ClassNode) this.methodNode.getParent());
            List<StatementNode> statementNodes = new ArrayList<>(methods);

            // if Constructor -> find constructor candidates
            if (token.getTarget() instanceof ClassInstanceCreationNode) {
                List<ClassInstanceCreationNode> constructors = ReflectionHelper
                        .findConstructorSameType(token.getNodeType(), ((ClassInstanceCreationNode) token.getTarget()).getArgs().size());
                statementNodes.addAll(constructors);
            }

            if (token.getDeepLevel() < Genner.MAX_DEEP_LEVEL) {
                ((TypeToken) token).setMethodTokens(new ArrayList<>());
                for (StatementNode stmNode : statementNodes) {
                    if (stmNode instanceof MethodCalledNode) {
                        if (((MethodCalledNode) stmNode).getAgurements().size() == 0) {
                            token.getCandidates().add(stmNode);
                        } else {
                            boolean isGen = true;
                            //if same Params & != name
                            if (token.getTarget() instanceof MethodCalledNode) {
                                if (compareParams(stmNode, token.getTarget())) {
                                    MethodCalledNode methodCalledNode = new MethodCalledNode(((MethodCalledNode) stmNode).getMethodName());
                                    methodCalledNode.setAgurementTypes(((MethodCalledNode) token.getTarget()).getAgurements());
                                    methodCalledNode.setStatementString(methodCalledNode.toString());
                                    token.getCandidates().add(methodCalledNode);
                                    isGen = false;
                                }
                            }
                            if (isGen) {
                                MethodToken methodToken = new MethodToken((MethodCalledNode) stmNode, token.getDeepLevel());
                                ((TypeToken) token).getMethodTokens().add(methodToken);
                                generateCandidates(methodToken);
                                token.getCandidates().addAll(methodToken.getCandidates());

                            }
                        }
                    } else if (stmNode instanceof ClassInstanceCreationNode) {
                        if (((ClassInstanceCreationNode) stmNode).getArgs().size() == 0) {
                            token.getCandidates().add(stmNode);
                        } else {

                            MethodToken methodToken = new MethodToken((ClassInstanceCreationNode) stmNode, token.getDeepLevel());
                            ((TypeToken) token).getMethodTokens().add(methodToken);
                            generateCandidates(methodToken);

                            token.getCandidates().addAll(methodToken.getCandidates());
                        }
                    }
                }
            }
        } else if (token instanceof MethodToken) {
            //synthesis params
            for (Token argToken : ((MethodToken) token).getParamTokens()) {
                generateCandidates(argToken);
            }

            token.setCandidates(new ArrayList<>());

            List<List<StatementNode>> synthesis = new ArrayList<>();
            for (Token argToken : ((MethodToken) token).getParamTokens()) {
                if (synthesis.size() == 0) {
                    for (StatementNode arg : argToken.getCandidates()) {
                        List<StatementNode> arguments = new ArrayList<>();
                        arguments.add(arg);
                        synthesis.add(arguments);
                    }
                    continue;
                }

                List<List<StatementNode>> tmpSynthesis = new ArrayList<>();
                for (List<StatementNode> synthesizedArgs : synthesis) {
                    for (StatementNode arg : argToken.getCandidates()) {
                        List<StatementNode> newArgs = new ArrayList<>(synthesizedArgs);
                        newArgs.add(arg);
                        tmpSynthesis.add(newArgs);
                    }
                }

                synthesis.clear();
                synthesis.addAll(tmpSynthesis);
            }

            for (List<StatementNode> args : synthesis) {
                MethodCalledNode methodCalledNode = new MethodCalledNode(
                        ((MethodToken) token).getMethodName());
                methodCalledNode.setAgurementTypes(args);
                methodCalledNode.setStatementString(methodCalledNode.toString());
                token.getCandidates().add(methodCalledNode);
            }
        }
    }


    public List<PatchCandidate> genOperatorCandidates(OperatorToken o, Pattern pattern) {
        List<PatchCandidate> patchCandidates = new ArrayList<>();
        List<String> values = operatorCandidates.get(o.getNodeType());
        for (String value : values) {
            if (!value.equals(o.getOriginalValue())) {
                PatchCandidate patchCandidate = new PatchCandidate();
                patchCandidate.setPattern(pattern);
                patchCandidate.getChangesMap().put(o, new OperatorNode(value, o.getStartPostion(), o.getEndPostion()));
                patchCandidate.setTargetNode(o.getTarget());
                patchCandidate.computeContent(pattern);
                patchCandidates.add(patchCandidate);
            }
        }
        return patchCandidates;
    }

    private BooleanNode genBooleanCandidates(BooleanNode booleanNode) {
        List<String> values = booleanCandies.get(BooleanType.BooleanNode.toString());
        for (String value : values) {
            if (!value.equals(booleanNode.toString())) {
                BooleanNode newCandi = new BooleanNode(value);
                return newCandi;
            }
        }
        return null;
    }

    //===================== END Generate Candidate ==========================
    //===================== API ========================

    /**
     * Api find baseVarNodes
     *
     * @param methodNode
     * @return
     */
    private static List<BaseVariableNode> findBaseVarSameType(String parentClass, String type, int line, MethodNode methodNode) {
        return ReflectionHelper.findBaseVarSameType(parentClass,
                type, line, methodNode);
    }

    /**
     * API find MethodCalledNodes same type
     *
     * @param classNode
     * @return
     */
    private static List<StatementNode> findMethodCalledSameType(String parentType, String itType, ClassNode classNode) {
        List<StatementNode> statementNodes = new ArrayList<>();
        Integer key;
        if (itType != null) {
            key = itType.hashCode();
        } else {
            key = "null".hashCode();
        }
        if (parentType != null) {
            key = (parentType + itType).hashCode();
        }
        if (!nodeMap.containsKey(key)) {
            HashMap<Integer, List<StatementNode>> listHashMap = parserClass(parentType, itType, classNode);
            nodeMap.putAll(listHashMap);
        }
        //get in list
        if (nodeMap.get(key) != null) {
            for (StatementNode stm : nodeMap.get(key)) {
                if (stm instanceof MethodCalledNode) {
                    statementNodes.add((MethodCalledNode) stm);
                } else if (stm instanceof ClassInstanceCreationNode) {
                    statementNodes.add(stm);
                }
            }
        }

        //get in class
        List<MethodCalledNode> statementNodes1 = ReflectionHelper.findMethodCalledSameType(parentType, itType, classNode);
        statementNodes.addAll(statementNodes1);
        return statementNodes;
    }

    private boolean compareParams(StatementNode stmNode, StatementNode target) {
        List<String> paramsA = getParamTypes((MethodCalledNode) stmNode);
        List<String> paramsB = getParamTypes((MethodCalledNode) target);
        return ReflectionHelper.compareParams(paramsA, paramsB);
    }

    private List<String> getParamTypes(MethodCalledNode methodCalledNode) {
        List<String> params = new ArrayList<>();
        for (StatementNode type : methodCalledNode.getAgurements()) {
            params.add(type.getType());
        }
        return params;
    }

    private static HashMap<Integer, List<StatementNode>> parserClass(String parentType, String itType, ClassNode classNode) {
        HashMap<Integer, List<StatementNode>> map = new HashMap<>();
        for (MethodNode methodNode : classNode.getMethodList()) {
            for (StatementNode statementNode : methodNode.getStatementNodes()) {
                HashMap<Integer, List<StatementNode>> listHashMap = FindingAPI
                        .parseFile(statementNode, parentType, itType);
                for (Integer key : listHashMap.keySet()) {
                    if (map.containsKey(key)) {
                        FindingAPI.addStmNode(map.get(key), listHashMap.get(key));
                    } else {
                        map.put(key, listHashMap.get(key));
                    }
                }
            }
        }
        return map;
    }

    //=================== End API =========================

    public StatementNode getTargetToken() {
        return targetToken;
    }

    public void setTargetToken(StatementNode targetToken) {
        this.targetToken = targetToken;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public void setMethodNode(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public FolderNode getFolderNode() {
        return folderNode;
    }

    public void setFolderNode(FolderNode folderNode) {
        this.folderNode = folderNode;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    private enum OperatorType {
        Relational_Operator,  // >, >=, <, <=
        Conditional_Operator, // &&, ||
        Equality_Operator, // ==, !=
        Pre_Operator // "!", ""
    }

    private enum BooleanType {
        BooleanNode
    }

    private static final Map<String, List<String>> operatorCandidates = new HashMap<String, List<String>>() {
        {
            List<String> infix1 = Arrays.asList(">", ">=", "<", "<=");
            put(OperatorType.Relational_Operator.toString(), infix1);

            List<String> infix2 = Arrays.asList("!", "");
            put(OperatorType.Pre_Operator.toString(), infix2);

            List<String> infix3 = Arrays.asList("==", "!=");
            put(OperatorType.Equality_Operator.toString(), infix3);

            List<String> infix4 = Arrays.asList("&&", "||");
            put(OperatorType.Conditional_Operator.toString(), infix4);
        }
    };

    private static final Map<String, List<String>> booleanCandies = new HashMap<String, List<String>>() {
        {
            {
                List<String> infix4 = Arrays.asList("true", "false");
                put(BooleanType.BooleanNode.toString(), infix4);
            }
        }
    };




}
