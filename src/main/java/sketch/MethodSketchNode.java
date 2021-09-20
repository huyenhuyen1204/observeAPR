package sketch;

import AST.stm.abst.StatementNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.MethodCalledNode;
import AST.stm.token.MethodInvocationStmNode;

import java.util.ArrayList;
import java.util.List;

public class MethodSketchNode extends SketchNode {
    private String methodName;
    private List<NormalSketchNode> paramSketches = new ArrayList<>();

    public MethodSketchNode(MethodCalledNode methodCalledNode, int deepLevel) {
        this.methodName = methodCalledNode.getMethodName();
        this.deepLevel = deepLevel;
        for (StatementNode statementNode : methodCalledNode.getAgurements()) {
            NormalSketchNode argSketch = new NormalSketchNode(this.deepLevel + 1);
            if (statementNode instanceof MethodInvocationStmNode) {
                argSketch.setDataType(((MethodInvocationStmNode) statementNode).getMethodType());
            } else {
                argSketch.setDataType(statementNode.getType());
            }
            this.paramSketches.add(argSketch);
        }
    }

    public MethodSketchNode(ClassInstanceCreationNode classInstanceCreationNode, int deepLevel) {
        this.methodName = "new " + classInstanceCreationNode.getName() ;
        this.deepLevel = deepLevel;
        for (StatementNode statementNode : classInstanceCreationNode.getArgs()) {
            NormalSketchNode argSketch = new NormalSketchNode(this.deepLevel + 1);
            if (statementNode instanceof MethodInvocationStmNode) {
                argSketch.setDataType(((MethodInvocationStmNode) statementNode).getMethodType());
            } else {
                argSketch.setDataType(statementNode.getType());
            }
            this.paramSketches.add(argSketch);
        }
    }

//    public void genCandidates() {
//        for (NormalSketchNode argSketch : paramSketches) {
//            argSketch.genCandidates();
//        }
//        this.nodeCandidates = new ArrayList<>();
//
//        List<List<StatementNode>> synthesis = new ArrayList<>();
//        for (NormalSketchNode argSketch : paramSketches) {
//            if (synthesis.size() == 0) {
//                for (StatementNode arg : argSketch.getNodeCandidates()) {
//                    List<StatementNode> arguments = new ArrayList<>();
//                    arguments.add(arg);
//                    synthesis.add(arguments);
//                }
//                continue;
//            }
//
//            List<List<StatementNode>> tmpSynthesis = new ArrayList<>();
//            for (List<StatementNode> synthesizedArgs : synthesis) {
//                for (StatementNode arg : argSketch.getNodeCandidates()) {
//                    List<StatementNode> newArgs = new ArrayList<>(synthesizedArgs);
//                    newArgs.add(arg);
//                    tmpSynthesis.add(newArgs);
//                }
//            }
//
//            synthesis.clear();
//            synthesis.addAll(tmpSynthesis);
//        }
//
//        for (List<StatementNode> args : synthesis) {
//            MethodCalledNode methodCalledNode = new MethodCalledNode(methodName);
//            methodCalledNode.setAgurementTypes(args);
//            methodCalledNode.setStatementString(methodCalledNode.toString());
//            this.nodeCandidates.add(methodCalledNode);
//        }
//    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(methodName).append("(");
        for (int i = 0; i < paramSketches.size(); i++) {
            SketchNode param = paramSketches.get(i);
            builder.append(param.toString());
            if (i < paramSketches.size() - 1) builder.append(",");
        }
        builder.append(")");
        return builder.toString();
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<NormalSketchNode> getParamSketches() {
        return paramSketches;
    }
}
