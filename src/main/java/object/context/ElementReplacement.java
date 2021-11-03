package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.nodetype.InitNode;
import AST.stm.token.InfixExpressionStmNode;
import AST.stm.token.MethodCalledNode;
import object.Algorithm;

public class ElementReplacement extends Context {
//    public int NodeType = -1;// eg: MethodInvocation, Simple Name

    public ElementReplacement(StatementNode stmBug, StatementNode stmFix, Object stmFind,
                              Boolean isSameMethod, Boolean isSameLine, Context.Scope scope) {
//        NodeType = stmBug.getNodeType();
        if (isSameMethod != null) {
            this.findSameMethod = isSameMethod;
        }
        this.scope = scope;
        this.bugLine = stmBug.getLine();
        this.findSameLine = isSameLine;
        this.bugInstance = stmBug.getNodeInstance();
        this.bugString = stmBug.toString();
        this.fixString = stmFix.toString();
        if (stmBug instanceof InfixExpressionStmNode) {
            this.bugString = ((InfixExpressionStmNode) stmBug).getOperator().getOperator();
        }
        if (stmFix instanceof InfixExpressionStmNode) {
            this.fixString = ((InfixExpressionStmNode) stmFix).getOperator().getOperator();
        }
        if (stmBug instanceof MethodCalledNode) {
            this.bugString = stmBug.toString();
        }
        if (stmFix instanceof MethodCalledNode) {
            this.fixString = stmFix.toString();
        }
//        this.statmentBug = stmBug;
        this.bugNode = stmBug.nodeType.toString();
        this.fixNode = stmFix.nodeType.toString();
        this.bugType = stmBug.getType();
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

    }

    public void setMethod(String methodbug, String methodfind) {
        this.methodBug = methodbug;
        this.methodFind = methodfind;
    }

}
