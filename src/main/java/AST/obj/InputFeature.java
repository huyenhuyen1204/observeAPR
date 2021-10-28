package AST.obj;

import AST.stm.abst.StatementNode;

import java.util.HashMap;
import java.util.Locale;

public class InputFeature {
    public static final String isSameMethod = "findSameMethod";
    public static final String bugNode = "bugNode";
    public static final String fixNode = "fixNode";
    public static final String bugInstance = "bugInstance";
    public static final String scope = "scope";
//    public static final String fixInstance = "fixInstance";

    public HashMap<String, String> nodeMap(Boolean isSameMethod, StatementNode bugNode, StatementNode fixNode) {
        HashMap<String, String> inputParameter;
        inputParameter = new HashMap<>();
        String sameMethod = String.valueOf(isSameMethod);
        if (isSameMethod == null) {
            sameMethod = "null";
        }
        inputParameter.put(InputFeature.isSameMethod, sameMethod.toUpperCase(Locale.ROOT));
        inputParameter.put(InputFeature.bugNode, bugNode.nodeType.toString());

        inputParameter.put(InputFeature.scope,
                bugNode.getToken().getTargetScope().toString());

        if (fixNode != null) {
            inputParameter.put(InputFeature.fixNode, fixNode.nodeType.toString());
        }

        inputParameter.put(InputFeature.bugInstance, bugNode.getNodeInstance().toString());
        return inputParameter;
    }


}