package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;
import object.Algorithm;

public class ElementReplacement extends Context {
//    public int NodeType = -1;// eg: MethodInvocation, Simple Name

    public ElementReplacement(StatementNode stmBug,  StatementNode stmFix, Object stmFind, Boolean isSameMethod, Boolean isSameLine) {
//        NodeType = stmBug.getNodeType();
        if (isSameMethod != null) {
            this.findSameMethod = isSameMethod;
        }
        this.bugLine = stmBug.getLine();
        this.findSameLine = isSameLine;
        this.bugInstance = stmBug.getNodeInstance();
        this.bugString = stmBug.getStatementString();
        this.fixString = stmFix.getStatementString();
//        this.statmentBug = stmBug;
        this.bugNode = stmBug.nodeType.toString();
        this.fixNode = stmFix.nodeType.toString();
        this.bugType = stmBug.getType();
        if (stmFind != null) {
            if (stmFind instanceof StatementNode) {
                this.fixInstance = ((StatementNode)stmFind).getNodeInstance();
                this.fixString = ((StatementNode) stmFind).getStatementString();
                this.bugType = ((StatementNode) stmFind).getType();
            } else if (stmFind instanceof InitNode) {
                this.fixInstance = NodeInstance.INIT;
                this.bugType = ((InitNode) stmFind).getType();

            }
        }
        this.distance = Algorithm.caculatorDistence(bugString, fixString);

    }

    public void setMethod(String methodbug, String methodfind) {
        this.methodBug = methodbug;
        this.methodFind = methodfind;
    }

}
