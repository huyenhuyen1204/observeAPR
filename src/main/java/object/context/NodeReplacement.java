package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;
import object.Algorithm;

public class NodeReplacement extends Context {
//    public  int fixNodeType = -1;
//    public int bugNodeType = -1;

    public String statementNodeFind;


    //NEEDED CONTEXT
    public NodeReplacement(StatementNode bugNode, StatementNode fixNode, Object stmFind, Boolean isSameMethod, Boolean isSameLine) {
        this.bugLine = bugNode.getLine();
        this.bugInstance = bugNode.getNodeInstance();
        this.findSameLine = isSameLine;

        this.bugNode = bugNode.nodeType.toString();
        this.fixNode = fixNode.nodeType.toString();

        this.bugString = bugNode.getStatementString();
        this.fixString = fixNode.getStatementString();

        this.bugType = bugNode.getType();

        if (stmFind != null) {
            if (stmFind instanceof StatementNode) {
                this.fixInstance = ((StatementNode)stmFind).getNodeInstance();
                this.fixString = ((StatementNode)stmFind).getStatementString();
                this.fixType = ((StatementNode)stmFind).getType();
            } else if (stmFind instanceof InitNode) {
                this.fixInstance = NodeInstance.INIT;
                this.fixType = ((InitNode)stmFind).getType();
            }
        }
        if (bugString != null && fixString != null) {
            this.distance = Algorithm.caculatorDistence(bugString, fixString);
        }

        this.findSameMethod = isSameMethod;
    }
     public void setSameMethod (boolean isSame) {
        this.findSameMethod = isSame;
     }

    public void setMethod(String methodbug, String methodfind) {
        this.methodBug = methodbug;
        this.methodFind = methodfind;
    }
}
