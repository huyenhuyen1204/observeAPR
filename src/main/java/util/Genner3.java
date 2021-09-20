package util;

import AST.node.ClassNode;
import AST.node.FolderNode;
import AST.node.MethodNode;
import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.node.OperatorNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.*;
import sketch.*;

import java.util.*;

/**
 * Genner is used for generate source node candidates for a target token of type baseVar or method invocation
 */
public class Genner3 {
    private StatementNode targetToken;
    private String returnType; // the return type of the target token (void, datatype, null, etc.)
    private MethodNode methodNode; // the method where the target token is
    private FolderNode folderNode;
    private List<PatchSketch> patchSketches = new ArrayList<>();
    private static HashMap<Integer, List<StatementNode>> nodeMap = new HashMap<>();

    public final static int MAX_DEEP_LEVEL = 1;

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


    private BooleanNode genBooleanCandidates(BooleanNode booleanNode) {
        List<String> values = booleanCandies.get(BooleanType.BooleanNode.toString());
        for (String value : values) {
            if (!value.equals(booleanNode.getStatementString())) {
                BooleanNode newCandi = new BooleanNode(value);
                return newCandi;
            }
        }
        return null;
    }

    public List<PatchCandidate> genOperatorCandidates(OperatorSketcheNode o, PatchSketch patchSketch) {
        List<PatchCandidate> patchCandidates = new ArrayList<>();
        List<String> values = operatorCandidates.get(o.getDataType());
        for (String value : values) {
            if (!value.equals(o.getOriginalValue())) {
                PatchCandidate patchCandidate = new PatchCandidate();
//                patchCandidate.setChangesMap(changesMap);
                patchCandidate.setPatchSketch(patchSketch);
                patchCandidate.setContent(value);
                patchCandidate.setTargetNode(o.getTarget());
                patchCandidates.add(patchCandidate);
            }
        }
        return patchCandidates;
    }


    // element sketch map is a contain statement node element and its corresponding sketches
    // public HashMap<StatementNode, List<SketchNode>> elementSketchMap = new HashMap<>();


    private Genner3(StatementNode stmNode, FolderNode folderNode, MethodNode methodNode) {
        this.targetToken = stmNode;
        if (stmNode instanceof MethodInvocationStmNode) {
            this.returnType = ((MethodInvocationStmNode) stmNode).getMethodType();
        } else {
            this.returnType = stmNode.getType();
        }
        this.folderNode = folderNode;
        this.methodNode = methodNode;
    }

    /**
     * This method generates source node candidates for the target node (a token candidate).
     *
     * @param targetNode a token candidate
     * @param methodNode the method where the target Node is
     * @param folderNode the folder contains
     * @return list of candidates for the target node
     */
    public static List<PatchCandidate> generateFixCandidates(StatementNode targetNode,
                                                             MethodNode methodNode, FolderNode folderNode) {
        Genner3 genner = new Genner3(targetNode, folderNode, methodNode); // e.g. cfa.method(b,c)
        // 1. generate candidates for each elements
        genner.genSketchesForElements(); // gen sketch node for each element e.g. cfa -> <Type>, method(b, c) -> method(<int>, <int>)
        // 2. synthesize sketches for the target node
        genner.getPatchSketches().addAll(genner.generatePatchSketches(targetNode)); // <Type>.method(), cfa.anotherMethod(<int.)
        // 3. from synthesized sketches, generate source node candidate
        genner.generatePatchCandidates();

        Map<String, PatchCandidate> sourceNodeCandiesMap = new HashMap<>();
        for (PatchSketch patchSketch : genner.getPatchSketches()) {
            for (PatchCandidate candidate : patchSketch.getPatchCandidates()) {
                candidate.setTargetNode(patchSketch.getTargetNode());
                sourceNodeCandiesMap.put(candidate.toString(), candidate);
//                String content = candidate.toString();
//                List<PatchCandidate> candidates = sourceNodeCandiesMap.computeIfAbsent(content, k -> new ArrayList<>());
//                candidates.add(candidate);
            }
        }
        for (PatchCandidate candidate : sourceNodeCandiesMap.values()) {
            System.out.println(candidate.getTargetNode() + " -> " + candidate.getPatchSketch().toString() + " -> " + candidate.toString());
        }

//        List<PatchCandidate> sourceNodeCandidates = new ArrayList<>();

//        for (String content : sourceNodeCandiesMap.keySet()) {
//            sourceNodeCandidates.add(sourceNodeCandiesMap.get(content).get(0));
//        }

        return new ArrayList<>(sourceNodeCandiesMap.values());
    }

    private void generatePatchCandidates() {
        for (PatchSketch patchSketch : patchSketches) {
            generatePatchCandidate(patchSketch);
        }
    }

    public void generatePatchCandidate(PatchSketch patchSketch) {
        if (patchSketch instanceof PatchInfixSketch) {
            System.out.println();
            ((PatchInfixSketch) patchSketch).genCandidates(this);
        } else {
            //save to candidates
            patchSketch.setMethodNode(this.methodNode);
            patchSketch.computePatchCandidates(this);
        }
    }


    private void genSketchesForElements() {
        genSketchesForElements(this.targetToken);
    }

    //
    private void genSketchesForElements(StatementNode element) {
        boolean isSameMethodInvo = false; // Qualified same MethodInvocation: Class.Var
        boolean isSameBaseVar = false; //Qualified same base var: Enum.Var
        if (element instanceof QualifiedNameNode) {
            //TODO: edit with Class.Enum.var
            if (ReflectionHelper.isEnum(element.getChildren().get(0).getType())) {
                isSameBaseVar = true;
            } else {
                isSameMethodInvo = true;
            }
        }
        if (element instanceof MethodInvocationStmNode || isSameMethodInvo) {
            // we generate sketch candidate for the whole method invocation
            // method invocation -> <returnType>

            StatementNode child = element.getChildren().get(0);
            if (child.getChildren().size() > 0) { // when the child has child
                NormalSketchNode sketchNode = new NormalSketchNode(child, SketchNode.Scope.ALL_AFTER);
                sketchNode.setDataType(this.returnType);
                sketchNode.setOriginalValue(element.getStatementString()); // redundant
                element.getChildren().add(sketchNode); // same level of the child node
            }
        } else if (element instanceof BaseVariableNode || isSameBaseVar) {
            // e.g. var node in var.method(b, c).method(d, e)
            // => <varType>.method(b,c).method(d,e)
            // or var.<returnType>
            // 1. var -> <varType>
            // 2. method(b, c).method(d, e) -> <returnType>

            // 1. generate sketch for the current node
            NormalSketchNode sketchNode = null;
            if (!isSameBaseVar) {
                sketchNode = new NormalSketchNode(element, SketchNode.Scope.ALL_AFTER); //qualifier
            } else {
                sketchNode = new NormalSketchNode(element, SketchNode.Scope.ALL_AFTER); //baseVar
            }
            sketchNode.setDataType(element.getType());
            sketchNode.setOriginalValue(element.getStatementString());
            // ===save to sketchMap (candidates)
            List<SketchNode> sketchNodes = new ArrayList<>();
            sketchNodes.add(sketchNode);
            element.setSketchNodes(sketchNodes);

            // 2. generate candidate sketches for the rest of the method invocation (e.g. method(b,c).method(d,e) -> <returnType>)
            // Run when the first child of this node is parent of another node (e.g. method(b,c) is parent of method(d,e))
            if (!isSameBaseVar) {
                StatementNode child = null;
                if (element.getChildren().size() > 0) child = element.getChildren().get(0);
                if (child != null && child.getChildren().size() > 0) { // the child has child
                    NormalSketchNode restSketch = new NormalSketchNode(child, SketchNode.Scope.ALL_AFTER);
                    restSketch.setDataType(this.returnType);
                    String suffix = child.getSuffix();
                    suffix = suffix.equals("") ? suffix : "." + suffix;
                    restSketch.setOriginalValue(child.getStatementString() + suffix);
                    element.getChildren().add(restSketch); // same level of the child node
                }
            }
        } else if (element instanceof MethodCalledNode) {
            // e.g. method1(b,c).method2(b2,c2).method3(b3, c3)
            // => (1) <method1ReturnType>.method2(b2,c2).method3(b3, c3)
            //    (2) method1(b,c).<returnType>
            //    (3) method1(<int>, c).method2(b2,c2).method3(b3, c3)
            //    (3) method1(b, <int>).method2(b2,c2).method3(b3, c3)
            //    (3) method1(<int>, <int>).method2(b2,c2).method3(b3, c3)

            // 1. generate sketch for the current node
            SketchNode normSketch = new NormalSketchNode(element, SketchNode.Scope.ONLY_CURRENT);
            normSketch.setDataType(element.getType());
            normSketch.setOriginalValue(element.toString());
            // ===save to sketchMap (candidates)
            List<SketchNode> sketchNodes = new ArrayList<>();
            sketchNodes.add(normSketch);
            element.setSketchNodes(sketchNodes);

            // 2. generate method sketch for the rest of the method invocation
            StatementNode child = null;
            if (element.getChildren().size() > 0) child = element.getChildren().get(0);
            if (child != null && child.getChildren().size() > 0) { // the child has child
                NormalSketchNode restSketch = new NormalSketchNode(child, SketchNode.Scope.ALL_AFTER);
                restSketch.setDataType(this.returnType);
                String suffix = child.getSuffix();
                suffix = suffix.equals("") ? suffix : "." + suffix;
                restSketch.setOriginalValue(child.toString() + suffix);
                element.getChildren().add(restSketch); // same level of the child node
            }
            // 3. generate sketches for all parameters of the method
            for (StatementNode param : ((MethodCalledNode) element).getAgurements()) {
                Genner3 paramGenner = new Genner3(param, this.folderNode, this.methodNode);
                paramGenner.genSketchesForElements();
            }
        } else if (element instanceof ClassInstanceCreationNode) {
            // 1. generate sketch for the current node
            SketchNode normSketch = new NormalSketchNode(element, SketchNode.Scope.ONLY_CURRENT);
            normSketch.setDataType(element.getType());
            normSketch.setOriginalValue(element.toString());
            // ===save to sketchMap (candidates)
            List<SketchNode> sketchNodes = new ArrayList<>();
            sketchNodes.add(normSketch);
            element.setSketchNodes(sketchNodes);

            // 2. generate method sketch for the rest of the method invocation
            StatementNode child = null;
            if (element.getChildren().size() > 0) child = element.getChildren().get(0);
            if (child != null && child.getChildren().size() > 0) { // the child has child
                NormalSketchNode restSketch = new NormalSketchNode(child, SketchNode.Scope.ALL_AFTER);
                restSketch.setDataType(this.returnType);
                String suffix = child.getSuffix();
                suffix = suffix.equals("") ? suffix : "." + suffix;
                restSketch.setOriginalValue(child.toString() + suffix);
                element.getChildren().add(restSketch); // same level of the child node
            }
            // 3. generate sketches for all parameters of the method
            for (StatementNode param : ((ClassInstanceCreationNode) element).getArgs()) {
                Genner3 paramGenner = new Genner3(param, this.folderNode, this.methodNode);
                paramGenner.genSketchesForElements();
            }
        } else if (element instanceof InfixExpressionStmNode) {
            //1. a/b * c-> <int>
            // <a> / b * c; <
            OperatorSketcheNode operator = generateOperatorSketch((InfixExpressionStmNode) element);
            if (operator != null) {
                element.setSketchNodes(Arrays.asList(operator));
            }
            Genner3 leftNode = new Genner3(((InfixExpressionStmNode) element).getLeft(), this.folderNode, this.methodNode);
            leftNode.genSketchesForElements();
            Genner3 rightNode = new Genner3(((InfixExpressionStmNode) element).getRight(), this.folderNode, this.methodNode);
            rightNode.genSketchesForElements();
        } else if (element instanceof BooleanNode) {
            SketchNode sketchNode = new NormalSketchNode(element, SketchNode.Scope.ONLY_CURRENT);
            sketchNode.setDataType(BooleanType.BooleanNode.toString());
            sketchNode.setOriginalValue(element.getStatementString());
            element.setSketchNodes(Collections.singletonList(sketchNode));
        }

        if (element.getChildren().size() > 0 && !isSameBaseVar)
            genSketchesForElements(element.getChildren().get(0));
    }

    private List<PatchSketch> generatePatchSketches(StatementNode statementNode) {
        List<PatchSketch> patchSketches = new ArrayList<>();
        boolean isSameMethodInvo = false; // Qualified same MethodInvocation: Class.Var
        boolean isSameBaseVar = false; //Qualified same base var: Enum.Var
        if (statementNode instanceof QualifiedNameNode) {
            //TODO: edit with Class.Enum.var
            if (ReflectionHelper.isEnum(statementNode.getChildren().get(0).getType())) {
                isSameBaseVar = true;
            } else {
                isSameMethodInvo = true;
            }
        }
        // 1. if the target token is a base variable
        if (statementNode instanceof BaseVariableNode
                || isSameBaseVar
                || statementNode instanceof BooleanNode) {
            List<SketchNode> sketchNodes = statementNode.getSketchNodes();
            if (sketchNodes != null) {
                for (SketchNode sketchNode : sketchNodes) {
                    Change change = new Change();
                    change.setOriginalNode(statementNode);
                    change.setSketchNode(sketchNode);
                    sketchNode.setOriginalValue(statementNode.getStatementString()); // a basevar

                    PatchSketch pSketch = new PatchSketch();
                    pSketch.setTargetNode(statementNode);
                    pSketch.getChanges().add(change);

                    patchSketches.add(pSketch);
                }
            }
        } else if (statementNode instanceof MethodInvocationStmNode || isSameMethodInvo) {
            return generateMethodPatchSketches(statementNode, statementNode);
        } else if (statementNode instanceof ClassInstanceCreationNode) {
            return generateMethodPatchSketches(statementNode, statementNode);
        } else if (statementNode instanceof InfixExpressionStmNode) {
            return generateInfixPatchSketches(statementNode, statementNode);
        }

        return patchSketches;
    }

    private List<PatchSketch> generateInfixPatchSketches(StatementNode statementNode, StatementNode targetToken) {
        List<PatchSketch> patchSketches = new ArrayList<>();
        if (statementNode instanceof InfixExpressionStmNode) {
            //infix sketch
            List<SketchNode> sketchNode = statementNode.getSketchNodes();
            if (sketchNode != null) {
                setInfixPatchSketche(((InfixExpressionStmNode) statementNode).getOperator(),
                        sketchNode.get(0), patchSketches, targetToken);
            }

            //others
            StatementNode left = ((InfixExpressionStmNode) statementNode).getLeft();
            //if left != instance of infix PatchInfix = add
            if (!(left instanceof InfixExpressionStmNode)) {
                if (left.getSketchNodes() != null) {
                    for (SketchNode leftSketch : left.getSketchNodes()) {
                        setInfixPatchSketche(left, leftSketch, patchSketches, targetToken);
                    }
                }
            } else {
                List<PatchSketch> patchInfixSketches = generateInfixPatchSketches(left, targetToken);
                patchSketches.addAll(patchInfixSketches);
            }
            StatementNode right = ((InfixExpressionStmNode) statementNode).getRight();
            if (!(right instanceof InfixExpressionStmNode)) {
                if (right.getSketchNodes() != null) {
                    for (SketchNode rightSketch : right.getSketchNodes()) {
                        setInfixPatchSketche(right, rightSketch, patchSketches, targetToken);
                    }
                }
            } else {
                List<PatchSketch> patchInfixSketches = generateInfixPatchSketches(right, targetToken);
                patchSketches.addAll(patchInfixSketches);
            }

        }
        return patchSketches;
    }

    private void setInfixPatchSketche(StatementNode originNode, SketchNode sketchNode,
                                      List<PatchSketch> patchSketches, StatementNode targetToken) {
        PatchInfixSketch patchSketch = new PatchInfixSketch();
        //create changes
        Change change = new Change();
        change.setOriginalNode(originNode);
        change.setSketchNode(sketchNode);
        patchSketch.getChanges().add(change);
        //set target
        patchSketch.setTargetNode(targetToken);

        if (originNode.getNodeInstance() == NodeInstance.ARGUMENT) {
            patchSketch.setParam(true);
        }
        //add to patch Sketches
        patchSketches.add(patchSketch);
    }

    private void setPatchSketche(StatementNode originNode, SketchNode sketchNode,
                                 List<PatchSketch> patchSketches, StatementNode targetNode) {
        PatchSketch patchSketch = new PatchSketch();
        //create changes
        Change change = new Change();
        change.setOriginalNode(originNode);
        change.setSketchNode(sketchNode);
        patchSketch.getChanges().add(change);
        //set target
        patchSketch.setTargetNode(targetNode);

        if (originNode.getNodeInstance() == NodeInstance.ARGUMENT) {
            patchSketch.setParam(true);
        }
        //add to patch Sketches
        patchSketches.add(patchSketch);
    }


    private OperatorSketcheNode generateOperatorSketch(InfixExpressionStmNode statementNode) {
        OperatorNode operator = statementNode.getOperator();
        for (String operatorType : operatorCandidates.keySet()) {
            List<String> values = operatorCandidates.get(operatorType);
            if (values.contains(((InfixExpressionStmNode) statementNode)
                    .getOperator().getOperator())) {
                OperatorSketcheNode sketchNode = new OperatorSketcheNode(statementNode.getOperator(),
                        SketchNode.Scope.ONLY_CURRENT);
                sketchNode.setDataType(operatorType.toString());
                sketchNode.setOriginalValue(operator.getOperator());
                return sketchNode;
            }
        }
        return null;
    }


    private List<PatchSketch> generateMethodPatchSketches(StatementNode methodInvo, StatementNode currentNode) {
        List<PatchSketch> patchSketches = new ArrayList<>();
        // 1. by modifying current node
        List<SketchNode> sketchNodes = currentNode.getSketchNodes();
        if (sketchNodes != null) {
            for (SketchNode sketchNode : sketchNodes) {
                Change change = new Change();
                change.setOriginalNode(currentNode);
                change.setSketchNode(sketchNode);

                PatchSketch pSketch = new PatchSketch();
                pSketch.setTargetNode(methodInvo);
                pSketch.getChanges().add(change);

                patchSketches.add(pSketch);
            }
        }

        // 2. by modifying the rest of the method invocation
        if (currentNode.getChildren().size() > 1) {
            for (int i = 1; i < currentNode.getChildren().size(); i++) {
                Change change = new Change();
                change.setOriginalNode(currentNode.getChildren().get(0));
                change.setSketchNode((SketchNode) currentNode.getChildren().get(i));

                PatchSketch pSketch = new PatchSketch();
                pSketch.setTargetNode(methodInvo);
                pSketch.getChanges().add(change);

                patchSketches.add(pSketch);
            }
        }

        if (currentNode instanceof MethodCalledNode
                || currentNode instanceof ClassInstanceCreationNode) { // allowed to change more than one element (e.g. two arguments of a method)
            List<List<PatchSketch>> arguments = new ArrayList<>();
            List<StatementNode> args = new ArrayList<>();
            if (currentNode instanceof MethodCalledNode) {
                args = ((MethodCalledNode) currentNode).getAgurements();
            } else if (currentNode instanceof ClassInstanceCreationNode) {
                args = ((ClassInstanceCreationNode) currentNode).getArgs();
            }
            for (StatementNode argument : args) {
                List<PatchSketch> sketches = generatePatchSketches(argument);
                sketches.add(null);
                arguments.add(sketches);
            }

            List<List<PatchSketch>> synthesis = new ArrayList<>();
            for (List<PatchSketch> arg : arguments) {
                if (synthesis.size() == 0) {
                    for (PatchSketch patchSketch : arg) {
                        List<PatchSketch> list = new ArrayList<>();
                        list.add(patchSketch);
                        synthesis.add(list);
                    }
                } else {
                    List<List<PatchSketch>> tmpSynthesis = new ArrayList<>();
                    for (PatchSketch patchSketch : arg) {
                        for (List<PatchSketch> synthesizedList : synthesis) {
                            List<PatchSketch> list = new ArrayList<>(synthesizedList);
                            list.add(patchSketch);
                            tmpSynthesis.add(list);
                        }
                    }

                    synthesis.clear();
                    synthesis.addAll(tmpSynthesis);
                }
            }

            for (List<PatchSketch> combination : synthesis) {
                PatchSketch patchSketch = new PatchSketch();
                patchSketch.setTargetNode(methodInvo);
                patchSketch.setChangedMethod(currentNode);

                Map<StatementNode, PatchSketch> changedArgsMap = new HashMap<>();
                for (PatchSketch element : combination) {
                    if (element != null) {
                        patchSketch.getChanges().addAll(element.getChanges());
                        changedArgsMap.put(element.getTargetNode(), element);
                    }
                }
                patchSketch.setChangedArgsMap(changedArgsMap);

                if (patchSketch.getChanges().size() > 0)
                    patchSketches.add(patchSketch);
            }
        }

        if (currentNode.getChildren().size() > 0)
            patchSketches.addAll(generateMethodPatchSketches(methodInvo, currentNode.getChildren().get(0)));

        return patchSketches;
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

    /**
     * Api find baseVarNodes
     *
     * @param methodNode
     * @return
     */
    private static List<BaseVariableNode> findBaseVarSameType(String parentClass, String type, int line, MethodNode methodNode) {
//        if (parentClass != null) {
//            if (targetToken.getParent().getType() != null) {
        return ReflectionHelper.findBaseVarSameType(parentClass,
                type, line, methodNode);
//            }
//        }
//        return ReflectionHelper.findBaseVarSameType(null,
//                type, line, methodNode);
    }

    /**
     * Api find methodInvocationNode
     *
     * @param classNode
     * @return
     */
    private static List<MethodInvocationStmNode> findMethodInvocationSameType(String parentType, String itType, ClassNode classNode) {
        List<MethodInvocationStmNode> statementNodes = new ArrayList<>();
        Integer key = itType.hashCode();

        if (!nodeMap.containsKey(key)) {
            // not exist -> parser
            HashMap<Integer, List<StatementNode>> listHashMap = parserClass(parentType, itType, classNode);
            nodeMap.putAll(listHashMap);
        }
        //get in list
        for (StatementNode stm : nodeMap.get(key)) {
            if (stm instanceof MethodInvocationStmNode) {
                statementNodes.add((MethodInvocationStmNode) stm);
            }
        }
        return statementNodes;
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


    public void generateCandidates(SketchNode sketchNode) {
        if (sketchNode instanceof NormalSketchNode) {
            sketchNode.setNodeCandidates(new ArrayList<>());
            if (sketchNode.getDataType() != null) {
                //check enum & find candidates
                if (ReflectionHelper.isEnum(sketchNode.getDataType())) {
                    List<StatementNode> qualifiedNameNodes = ReflectionHelper.findEnum(sketchNode.getDataType());
                    sketchNode.getNodeCandidates().addAll(qualifiedNameNodes);
                }
                //find basevar candidates
                List<BaseVariableNode> baseVarCandies = findBaseVarSameType(sketchNode.getParentType(),
                        sketchNode.getDataType(), this.targetToken.getLine(), this.methodNode);
                sketchNode.getNodeCandidates().addAll(baseVarCandies);
                //gen for booleanNode true -> false, false -> true
                if (sketchNode.getDataType().equals(BooleanType.BooleanNode.toString())) {
                    BooleanNode booleanNode = genBooleanCandidates((BooleanNode) sketchNode.getTarget());
                    sketchNode.getNodeCandidates().add(booleanNode);
                }
            }
            //find method sameType and can access (should rank 2)
            List<StatementNode> methods = findMethodCalledSameType(sketchNode.getParentType(),
                    sketchNode.getDataType(), (ClassNode) this.methodNode.getParent());
            List<StatementNode> statementNodes = new ArrayList<>(methods);

            // if Constructor -> find constructor candidates
            if (sketchNode.getTarget() instanceof ClassInstanceCreationNode) {
                List<ClassInstanceCreationNode> constructors = ReflectionHelper
                        .findConstructorSameType(sketchNode.getDataType(), ((ClassInstanceCreationNode) sketchNode.getTarget()).getArgs().size());
                statementNodes.addAll(constructors);
            }

            //            List<MethodCalledNode> methodsWithoutArgs = new ArrayList<>(methods);
//            methodsWithoutArgs.removeIf(methodCalledNode -> {
//                if (methodCalledNode.getAgurements() == null) {
//                    return false;
//                } else {
//                    return methodCalledNode.getAgurements().size() > 0;
//                }
//            });
//            List<MethodCalledNode> methodsWithArgs = new ArrayList<>(methods);
//            methodsWithArgs.removeIf(methodCalledNode -> {
//                if (methodCalledNode.getAgurements() == null) {
//                    return true;
//                } else {
//                    return methodCalledNode.getAgurements().size() == 0;
//                }
//            });

//            sketchNode.getNodeCandidates().addAll(methodsWithoutArgs);
            if (sketchNode.getDeepLevel() < Genner3.MAX_DEEP_LEVEL) {
                ((NormalSketchNode) sketchNode).setSketchCandidates(new ArrayList<>());
//                for (MethodCalledNode methodCall : methodsWithArgs) {
                for (StatementNode stmNode : statementNodes) {
                    if (stmNode instanceof MethodCalledNode) {
                        if (((MethodCalledNode) stmNode).getAgurements().size() == 0) {
                            sketchNode.getNodeCandidates().add(stmNode);
                        } else {
                            boolean isGen = true;
                            //if same Params & != name
                            if (sketchNode.getTarget() instanceof MethodCalledNode) {
                                if (compareParams(stmNode, sketchNode.getTarget())) {
                                    MethodCalledNode methodCalledNode = new MethodCalledNode(((MethodCalledNode) stmNode).getMethodName());
                                    methodCalledNode.setAgurementTypes(((MethodCalledNode) sketchNode.getTarget()).getAgurements());
                                    methodCalledNode.setStatementString(methodCalledNode.toString());
                                    sketchNode.getNodeCandidates().add(methodCalledNode);
                                    isGen = false;
                                }
                            }
                            if (isGen) {
                                MethodSketchNode methodSketch = new MethodSketchNode((MethodCalledNode) stmNode, sketchNode.getDeepLevel());
                                ((NormalSketchNode) sketchNode).getSketchCandidates().add(methodSketch);
                                generateCandidates(methodSketch);
                                sketchNode.getNodeCandidates().addAll(methodSketch.getNodeCandidates());

                            }
                        }
                    } else if (stmNode instanceof ClassInstanceCreationNode) {
                        if (((ClassInstanceCreationNode) stmNode).getArgs().size() == 0) {
                            sketchNode.getNodeCandidates().add(stmNode);
                        } else {

                            MethodSketchNode methodSketch = new MethodSketchNode((ClassInstanceCreationNode) stmNode, sketchNode.getDeepLevel());
                            ((NormalSketchNode) sketchNode).getSketchCandidates().add(methodSketch);
                            generateCandidates(methodSketch);

                            sketchNode.getNodeCandidates().addAll(methodSketch.getNodeCandidates());
                        }
                    }
                }
            }
        } else if (sketchNode instanceof MethodSketchNode) {
            //synthesis params
            for (NormalSketchNode argSketch : ((MethodSketchNode) sketchNode).getParamSketches()) {
                generateCandidates(argSketch);
            }
            sketchNode.setNodeCandidates(new ArrayList<>());

            List<List<StatementNode>> synthesis = new ArrayList<>();
            for (NormalSketchNode argSketch : ((MethodSketchNode) sketchNode).getParamSketches()) {
                if (synthesis.size() == 0) {
                    for (StatementNode arg : argSketch.getNodeCandidates()) {
                        List<StatementNode> arguments = new ArrayList<>();
                        arguments.add(arg);
                        synthesis.add(arguments);
                    }
                    continue;
                }

                List<List<StatementNode>> tmpSynthesis = new ArrayList<>();
                for (List<StatementNode> synthesizedArgs : synthesis) {
                    for (StatementNode arg : argSketch.getNodeCandidates()) {
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
                        ((MethodSketchNode) sketchNode).getMethodName());
                methodCalledNode.setAgurementTypes(args);
                methodCalledNode.setStatementString(methodCalledNode.toString());
                sketchNode.getNodeCandidates().add(methodCalledNode);
            }
        }
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


    public List<PatchSketch> getPatchSketches() {
        return patchSketches;
    }

    public void setFolderNode(FolderNode folderNode) {
        this.folderNode = folderNode;
    }

    public FolderNode getFolderNode() {
        return folderNode;
    }

    public void setMethodNode(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }
}
