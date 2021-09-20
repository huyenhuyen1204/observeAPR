package sketch;

import AST.stm.abst.StatementNode;
import AST.stm.token.BaseVariableNode;
import AST.stm.token.MethodCalledNode;

import java.util.ArrayList;
import java.util.List;

public class SketchNode extends StatementNode {
    protected String parentType = null;
    protected String dataType; // int, Class, etc.
    protected StatementNode target;
    protected Scope targetScope; // ALL_AFTER or ONLY_CURRENT

    protected String originalValue = null;
    protected List<StatementNode> nodeCandidates = null;

    public SketchNode() {
    }

    public SketchNode(StatementNode target, Scope targetScope) {
        this.target = target;
        this.targetScope = targetScope;
    }

    public static List<BaseVariableNode> makeUpBaseVars() {
        List<BaseVariableNode> baseVariableNodes = new ArrayList<>();
        BaseVariableNode node1 = new BaseVariableNode("var1");
        node1.setStatementString("var1");
        BaseVariableNode node2 = new BaseVariableNode("var2");
        node2.setStatementString("var2");
        BaseVariableNode node3 = new BaseVariableNode("var3");
        node3.setStatementString("var3");
        BaseVariableNode node4 = new BaseVariableNode("var4");
        node4.setStatementString("var4");

        node1.setKeyVar("var1");
        node2.setKeyVar("var2");
        node3.setKeyVar("var3");
        node4.setKeyVar("var4");

        baseVariableNodes.add(node1);
        baseVariableNodes.add(node2);
        baseVariableNodes.add(node3);
        baseVariableNodes.add(node4);
        return baseVariableNodes;
    }

    public static List<MethodCalledNode> makeUpMethodCallNodes() {
        BaseVariableNode node1 = new BaseVariableNode("var1");
        node1.setStatementString("var1");
        node1.setKeyVar("var1");
        BaseVariableNode node2 = new BaseVariableNode("var2");
        node2.setStatementString("var2");
        node2.setKeyVar("var2");
        BaseVariableNode node3 = new BaseVariableNode("var3");
        node3.setStatementString("var3");
        node3.setKeyVar("var3");
        BaseVariableNode node4 = new BaseVariableNode("var4");
        node4.setStatementString("var4");
        node4.setKeyVar("var4");

        List<MethodCalledNode> methodCalledNodes = new ArrayList<>();
        MethodCalledNode m0 = new MethodCalledNode("getName");
        m0.setAgurementTypes(new ArrayList<>());
        MethodCalledNode m1 = new MethodCalledNode("sum");
        List<StatementNode> args = new ArrayList<>();
        args.add(node1);
        args.add(node2);
        m1.setAgurementTypes(args);
        MethodCalledNode m2 = new MethodCalledNode("sub");
        List<StatementNode> args2 = new ArrayList<>();
        args2.add(node3);
        args2.add(node4);
        m2.setAgurementTypes(args2);

        MethodCalledNode m4 = new MethodCalledNode("getAge");
        m4.setAgurementTypes(new ArrayList<>());

        m0.setStatementString(m0.toString());
        m1.setStatementString(m1.toString());
        m2.setStatementString(m2.toString());
        m4.setStatementString(m4.toString());

        methodCalledNodes.add(m0);
        methodCalledNodes.add(m1);
        methodCalledNodes.add(m2);
        methodCalledNodes.add(m4);
        return methodCalledNodes;
    }

    public List<StatementNode> getNodeCandidates() {
        return nodeCandidates;
    }

    public String getParentType() {
        return parentType;
    }

    public String getDataType() {
        return dataType;
    }

    //    public void addParams(List<StatementNode> stmParams) {
//        for (StatementNode param : stmParams) {
//            if (!(param instanceof Token)) {
//                SketchNode paramSketch = new SketchNode(main.object.SketchType.LITERAL, null, "");
//                paramSketch.setDataType(param.getStatementString());
//                this.paramSketches.add(paramSketch);
//            } else {
//                SketchNode paramSketch = new SketchNode(main.object.SketchType.BASE_VAR_SKETCH, null, "");
//                paramSketch.setDataType(param.getType());
//                this.paramSketches.add(paramSketch);
//            }
//        }
//    }

//    public void addParameterParams(List<ParameterNode> stmParams) {
//        for (ParameterNode param : stmParams) {
//            SketchNode paramSketch = new SketchNode(main.object.SketchType.BASE_VAR_SKETCH, null, "");
//            paramSketch.setDataType(param.getType());
//            this.paramSketches.add(paramSketch);
//        }
//    }

//
//    @Override
//    public String toString() {
//        if (this.sketchType.equals(main.object.SketchType.BASE_VAR_SKETCH)) {
//            return "<" + this.dataType + ">";
//        } else if (this.sketchType.equals(main.object.SketchType.METHOD_CALL_SKETCH)) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(this.methodName).append("(");
//            for (int i = 0; i < paramSketches.size(); i++) {
//                SketchNode param = paramSketches.get(i);
//                builder.append(param.toString());
//                if (i < paramSketches.size() - 1) builder.append(",");
//            }
//            builder.append(")");
//            return builder.toString();
//        } else if (this.sketchType.equals(main.object.SketchType.LITERAL)) {
//            return dataType;
//        }
//        return "";
//    }


    public void setNodeCandidates(List<StatementNode> nodeCandidates) {
        this.nodeCandidates = nodeCandidates;
    }

    public StatementNode getTarget() {
        return target;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getType() {
        return dataType;
    }

    public void setTarget(StatementNode target) {
        this.target = target;
    }

    public Scope getTargetScope() {
        return targetScope;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    // assume we ha a method invocation var.methodA().methodB()
    // , and assume the current node is method call node methodA()
    public enum Scope {
        ONLY_CURRENT, // the sketch is a replacement of the current node (methodA())
        ALL_AFTER // the sketch is a replacement of the rest of the method invocation(methodB().methodC())
    }
}
