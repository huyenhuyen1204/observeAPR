package main.obj;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;

import java.util.HashMap;
import java.util.Locale;

public class InputFeature {
    public static final String isSameMethod = "isSameMethod";
    public static final String bugNode = "bugNode";
    public static final String fixNode = "fixNode";
    public static final String bugInstance = "bugInstance";
    public static final String fixInstance = "fixInstance";

    public HashMap<String, String> nodeMap(boolean isSameMethod, StatementNode bugNode, Object fixNode) {
        HashMap<String, String> inputParameter;
        inputParameter = new HashMap<>();
        inputParameter.put(InputFeature.isSameMethod, String.valueOf(isSameMethod).toUpperCase(Locale.ROOT));
        inputParameter.put(InputFeature.bugNode, bugNode.nodeType.toString());
        if (fixNode instanceof StatementNode) {
            inputParameter.put(InputFeature.fixNode, ((StatementNode) fixNode).nodeType.toString());
            inputParameter.put(InputFeature.fixInstance, ((StatementNode) fixNode).getNodeInstance().toString());
        } else if (fixNode instanceof InitNode) {
            inputParameter.put(InputFeature.fixNode, NodeType.BaseVariableNode.toString());
            inputParameter.put(InputFeature.fixInstance, NodeInstance.INIT.toString());
        }
        inputParameter.put(InputFeature.bugInstance, bugNode.getNodeInstance().toString());
        return inputParameter;
    }

//    public HashMap<String, String> elementMap(boolean isSameMethod, StatementNode bugNode, Object fixNode) {
//        HashMap<String, String> inputParameter;
//        inputParameter = new HashMap<>();
//        inputParameter.put(InputFeature.isSameMethod, String.valueOf(isSameMethod).toUpperCase(Locale.ROOT));
//        inputParameter.put(InputFeature.bugNode, bugNode.nodeType.toString());
//        inputParameter.put(InputFeature.bugInstance, bugNode.getNodeInstance().toString());
//        if (fixNode instanceof StatementNode) {
//            inputParameter.put(InputFeature.fixInstance, ((StatementNode) fixNode).getNodeInstance().toString());
//        } else if (fixNode instanceof InitNode) {
//            inputParameter.put(InputFeature.fixInstance, NodeInstance.INIT.toString());
//        }
//        return inputParameter;
//    }

}