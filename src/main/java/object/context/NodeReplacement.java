package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.MethodCalledNode;
import object.Algorithm;

public class NodeReplacement extends Context {
    //    public  int fixNodeType = -1;
//    public int bugNodeType = -1;

    public String statementNodeFind;


    //NEEDED CONTEXT
    public NodeReplacement(StatementNode bugNode, StatementNode fixNode,
                           Object stmFind, Boolean isSameMethod,
                           Boolean isSameLine, Scope scope) {
        this.bugLine = bugNode.getLine();
        this.bugInstance = bugNode.getNodeInstance();
        this.findSameLine = isSameLine;

        this.bugNode = bugNode.nodeType.toString();
        this.fixNode = fixNode.nodeType.toString();
        this.bugString = bugNode.toString();
        this.fixString = fixNode.toString();

        this.bugNode_fixNode = this.bugNode + "_" + this.fixNode;

        if (bugNode instanceof MethodCalledNode) {
            this.bugString = bugNode.toString();
        }

        if (fixNode instanceof MethodCalledNode) {
            this.fixString = fixNode.toString();
        }

        if ((bugNode instanceof MethodCalledNode && fixNode instanceof MethodCalledNode)
         || (bugNode instanceof ClassInstanceCreationNode && fixNode instanceof ClassInstanceCreationNode)) {
            String methodBug = bugNode.toString().replace(" ", "");
            String methodFix = fixNode.toString().replace(" ", "");

            String[] bugs = methodBug.split("\\(");
            String[] fixs = methodFix.split("\\(");
            this.bugNode_fixNode = this.bugNode;
            if (bugs[0].equals(fixs[0])) {
                this.bugNode_fixNode += "+SameMethod";
            } else {
                this.bugNode_fixNode += "+ChangeMethod";
            }
            if (bugs[1].equals(fixs[1])) {
                this.bugNode_fixNode += "+SameParam";
            } else {
                this.bugNode_fixNode += "+ChangeParam";
            }
        }


        this.bugType = bugNode.getType();

        if (stmFind != null) {
            if (stmFind instanceof StatementNode) {
                this.fixInstance = ((StatementNode) stmFind).getNodeInstance();
                this.fixString = ((StatementNode) stmFind).toString();
                this.fixType = ((StatementNode) stmFind).getType();
            } else if (stmFind instanceof InitNode) {
                this.fixInstance = NodeInstance.INIT;
                this.fixType = ((InitNode) stmFind).getType();
            }
        }
        if (bugString != null && fixString != null) {
            this.distance = Algorithm.caculatorDistence(bugString, fixString);
        }
        this.scope = scope;
        this.findSameMethod = isSameMethod;
    }


    public void setSameMethod(boolean isSame) {
        this.findSameMethod = isSame;
    }

    public void setMethod(String methodbug, String methodfind) {
        this.methodBug = methodbug;
        this.methodFind = methodfind;
    }
}
