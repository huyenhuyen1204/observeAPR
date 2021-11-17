package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;
import AST.stm.token.*;
import object.Algorithm;
import util.FileHelper;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ElementReplacement extends Context {
//    public int NodeType = -1;// eg: MethodInvocation, Simple Name

    public ElementReplacement(StatementNode stmBug, StatementNode stmFix, Object stmFind,
                              Boolean isSameMethod, Boolean isSameLine, Context.Scope scope, String pathBugFile) {
//        NodeType = stmBug.getNodeType();
        if (isSameMethod != null) {
            this.findSameMethod = isSameMethod;
        }
        if (stmBug != null) {
            this.bugLine = stmBug.getLine();
            this.bugInstance = stmBug.getNodeInstance();
            this.bugString = stmBug.toString();
            if (stmBug instanceof MethodCalledNode) {
                this.bugString = stmBug.toString();
            }
            this.bugNode = stmBug.nodeType.toString();
            this.bugType = stmBug.getType();
        } else {
            this.bugLine = -1;
            this.bugInstance = NodeInstance.INFIX;
            this.bugString = "";
            this.bugNode = NodeType.OperatorNode.toString();
            this.bugType = null;
        }

        if (stmFix != null) {
            this.fixString = stmFix.toString();
            if (stmFix instanceof MethodCalledNode) {
                this.fixString = stmFix.toString();
            }
            this.fixNode = stmFix.nodeType.toString();
        } else {
            this.fixString = "";
            this.fixNode = NodeType.OperatorNode.toString();
        }
        this.scope = scope;
        this.findSameLine = isSameLine;

//        if (stmBug instanceof InfixExpressionStmNode) {
//            this.bugString = ((InfixExpressionStmNode) stmBug).getOperator().getOperator();
//        }
//        if (stmFix instanceof InfixExpressionStmNode) {
//            this.fixString = ((InfixExpressionStmNode) stmFix).getOperator().getOperator();
//        }


        if (stmFind != null) {
            if (stmFind instanceof StatementNode) {
                this.fixInstance = ((StatementNode) stmFind).getNodeInstance();
                this.fixString = ((StatementNode) stmFind).toString();
                this.bugType = ((StatementNode) stmFind).getType();
            } else if (stmFind instanceof InitNode) {
                this.fixInstance = NodeInstance.INIT;
                this.bugType = ((InitNode) stmFind).getType();

            }
        }
        this.distance = Algorithm.caculatorDistence(bugString, fixString);
        this.bugNode_fixNode = this.bugNode + "_" + this.fixNode;
        this.context = this.bugNode_fixNode;

        if (stmFix instanceof MethodCalledNode || stmFix instanceof MethodInvocationStmNode
                || stmFix instanceof ClassInstanceCreationNode) {
            String file = FileHelper.readFile(new File(pathBugFile)).replace(" ", "");
            this.find = file.contains(fixString.replace(" ", ""));
        }
        if ((stmBug instanceof MethodCalledNode && stmFix instanceof MethodCalledNode)
                || (stmBug instanceof ClassInstanceCreationNode && stmFix instanceof ClassInstanceCreationNode)) {
            String methodBug = stmBug.toString().replace(" ", "");
            String methodFix = stmFix.toString().replace(" ", "");

            int firstIndex = methodBug.indexOf("(");
            String method1 = methodBug.substring(0, firstIndex);
            String param1 = methodBug.substring(firstIndex + 1, methodBug.length() - 1);

            int firstIndexFix = methodFix.indexOf("(");
            String method2 = methodFix.substring(0, firstIndexFix);
            String param2 = methodFix.substring(firstIndexFix + 1, methodFix.length() - 1);

            int paramSize1 = 0;
            int paramSize2 = 0;
            List<StatementNode> param1List = null;
            List<StatementNode> param2List = null;
            if (stmBug instanceof MethodCalledNode && stmFix instanceof MethodCalledNode) {
                paramSize1 = ((MethodCalledNode) stmBug).getAgurements().size();
                paramSize2 = ((MethodCalledNode) stmFix).getAgurements().size();
                param1List = ((MethodCalledNode) stmBug).getAgurements();
                param2List = ((MethodCalledNode) stmFix).getAgurements();
            } else {
                paramSize1 = ((ClassInstanceCreationNode) stmBug).getArgs().size();
                paramSize2 = ((ClassInstanceCreationNode) stmFix).getArgs().size();
                param1List = ((ClassInstanceCreationNode) stmBug).getArgs();
                param2List = ((ClassInstanceCreationNode) stmFix).getArgs();
            }
            if (method1.equals(method2)) {
                this.context += "&SameMethod";
            } else {
                this.context += "&ChangeMethod";
            }
            if (param1.equals(param2)) {
                this.context += "&SameParam";
            } else {
//                String type = action(param1List, param2List);
                String type = getAction(param1List, param2List);
                if (!type.equals("&CHANGE")) {
                    this.context += "&ChangeParam" + type;
                } else {
                    this.context += "&ChangeParam" + type;
                }

            }
            this.similar_score =  getScore(param1List, param2List);
        }
        this.context +=  (this.findSameMethod != null ? "&" + this.findSameMethod :"") + (this.find != null ? "&" + this.find :"");
//        this.bugNode_fixNode = this.bugNode_fixNode_1;
    }

//    public String action(List<StatementNode> param1, List<StatementNode> param2) {
//        String action = "";
//        if (param2.size() > param1.size()) {
//            if (constain(param2, param1)) {
//                action = "&ADD";
//            } else {
//                action = "&CHANGE";
//            }
//        } else if (param1.size() > param2.size()) {
//            if (constain(param1, param2)) {
//                action = "&REMOVE";
//            } else {
//                action = "&CHANGE";
//            }
//        } else {
//            action = "&CHANGE";
//        }
//        return action;
//    }

    public String getAction(List<StatementNode> param1, List<StatementNode> param2) {
        List<String> list1 = param1.stream().map(StatementNode::toString).collect(Collectors.toList());
        List<String> list2 = param2.stream().map(StatementNode::toString).collect(Collectors.toList());

        int min = Math.min(list2.size(), list1.size());
        int max = Math.max(list2.size(), list1.size());
        List<String> maxList = list1.size() > list2.size() ? list1 : list2;
        List<String> minList = list1.size() > list2.size() ? list2 : list1;
        int same = 0;
        for (int i = 0; i < min; i++) {
            for (int j = 0; j < max; j++) {
                if (min > i) {
                    if (minList.get(i).equals(maxList.get(j))) {
                        same++;
                        i++;
                    }
                } else {
                    break;
                }
            }
        }
        String action = "";
//
//        if (same != min) {
//            action =  "&CHANGE" + (max - same);
//        }
        if (param2.size() > param1.size()) {
            if (min == same) {
                action = "&ADD" + (max - same);
            } else {
                action = "&CHANGE";
            }
        } else if (param1.size() > param2.size()) {
            if (min == same) {
                action = "&REMOVE" + (max - same);
            } else {
                action = "&CHANGE";
            }
        }
        if (same == 0) {
            action = "&CHANGE";
        }
        return action;
    }

    public float getScore(List<StatementNode> param1, List<StatementNode> param2) {
        List<String> list1 = new ArrayList<>();
        for (int i = 0; i < param1.size() - 1; i++) {
            String near = param1.get(i).toString() + param1.get(i+1).toString();
            list1.add(near);
        }
        List<String> list2 = new ArrayList<>();
        for (int i = 0; i < param2.size() - 1; i++) {
            String near = param2.get(i).toString() + param2.get(i+1).toString();
            list2.add(near);
        }
        Collections.sort(list1);
        Collections.sort(list2);
        int  min = Math.min(list2.size(), list1.size());
        int  max = Math.max(list2.size(), list1.size());
        List<String> maxList = list1.size() > list2.size() ? list1 : list2;
        List<String> minList = list1.size() > list2.size() ? list2 : list1;
        int same = 0;
        for (int i = 0; i < min; i++) {
            for (int j = 0; j < max; j++) {
                if (min > i) {
                    if (minList.get(i).equals(maxList.get(j))) {
                        same++;
                        i++;
                    }
                } else {
                    break;
                }
            }
        }
        return (float) (max - same) + (min - same);
    }

    public boolean constain(List<StatementNode> param, List<StatementNode> paramSmaller) {
        List<String> parSmaller = paramSmaller.stream().map(StatementNode::toString)
                .collect(Collectors.toList());
        List<String> pr = param.stream().map(StatementNode::toString)
                .collect(Collectors.toList());
        int same = 0;
        for (String prsml : parSmaller) {
            if (pr.contains(prsml)) {
                same++;
            }
        }
        if (same == paramSmaller.size()) {
            return true;
        }
        return false;
    }


}
