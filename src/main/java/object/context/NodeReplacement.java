package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;
import AST.stm.token.*;
import object.Algorithm;
import util.FileHelper;

import java.io.File;

public class NodeReplacement extends Context {
    //    public  int fixNodeType = -1;
//    public int bugNodeType = -1;

    public String statementNodeFind;


    //NEEDED CONTEXT
    public NodeReplacement(StatementNode bugNode, StatementNode fixNode,
                           Object stmFind, Boolean isSameMethod,
                           Boolean isSameLine, Scope scope, String pathBugFile) {

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

        if (fixNode instanceof MethodCalledNode || fixNode instanceof MethodInvocationStmNode
                || fixNode instanceof ClassInstanceCreationNode) {
            String file = FileHelper.readFile(new File(pathBugFile));
            this.find = file.contains(fixString);
        }
        this.context = this.bugNode_fixNode + (this.findSameMethod != null ? "&" + this.findSameMethod :"") + (this.find != null ? "&" + this.find :"");
//        this.bugNode_fixNode = this.bugNode_fixNode_1;
    }


    public void setSameMethod(boolean isSame) {
        this.findSameMethod = isSame;
    }

    public void setMethod(String methodbug, String methodfind) {
        this.methodBug = methodbug;
        this.methodFind = methodfind;
    }
}
