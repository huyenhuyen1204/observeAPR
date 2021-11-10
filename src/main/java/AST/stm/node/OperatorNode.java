package AST.stm.node;

import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;

public class OperatorNode extends StatementNode {
    private  String operator;
    {
        this.nodeType = NodeType.OperatorNode;
    }

    public OperatorNode(String operator, int startPos, int endPos) {
        super();
        this.operator = operator;
        this.statementString =  operator;
        this.startPostion = startPos;
        this.endPostion = endPos;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
