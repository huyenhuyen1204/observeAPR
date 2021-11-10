package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeInstance;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import AST.stm.node.OperatorNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.List;

public class InfixExpressionStmNode extends StatementNode implements Token {
    private OperatorNode operator;
    private StatementNode left;
    private StatementNode right;
    private List<StatementNode> extendedOperands;
    {
        this.nodeType = NodeType.InfixExpressionStmNode;
    }

    public InfixExpressionStmNode(String operator, StatementNode left, StatementNode right,
                                  List<StatementNode> extendedOperands, int line,
                                  String stmString, ASTNode astNode, String classfullName) {
        super();
        setFullNameParent(classfullName);
        this.left = left;
        this.right = right;
        setFullNameParent(fullNameParent);

        this.extendedOperands = extendedOperands;
        this.line = line;
        this.statementString = stmString;
        Position pos = ASTHelper.getPosition(astNode);
        int startLeft = pos.getStartPos();
        int endRight = pos.getEndPos();
        this.startPostion = startLeft;
        this.endPostion = endRight;
        if (left != null) {
            children.add(left);
            left.setParent(this);
            left.setNodeInstance(NodeInstance.INFIX);
//            this.startPostion = left.getEndPostion();
        } else {
//            this.startPostion = startLeft;
        }
        if (right != null) {
            children.add(right);
            right.setParent(this);
            right.setNodeInstance(NodeInstance.INFIX);
//            this.endPostion = right.getStartPostion();
        } else {
//            this.endPostion = endRight;
        }
        if (extendedOperands != null) {
//            children.addAll(extendedOperands);
            for (StatementNode stm : extendedOperands) {
                if (stm != null) {
                    children.add(stm);
                    stm.setParent(this);
                }
            }
        }
        this.operator = new OperatorNode(operator, this.startPostion, this.endPostion);
        this.operator.setNodeInstance(NodeInstance.INFIX);
        this.setInstance(NodeInstance.INFIX);
        this.nodeType = NodeType.InfixExpressionStmNode;
    }

//    @Override
//    public int getNodeType() {
//        return this.nodeType;
//    }
//
//    @Override
//    public void setNodeType(int nodeType) {
//        this.nodeType = nodeType;
//    }



    public OperatorNode getOperator() {
        return operator;
    }

    public void setOperator(OperatorNode operator) {
        this.operator = operator;
    }

    public StatementNode getLeft() {
        return left;
    }

    public void setLeft(StatementNode left) {
        this.left = left;
    }

    public StatementNode getRight() {
        return right;
    }

    public void setRight(StatementNode right) {
        this.right = right;
    }

    public List<StatementNode> getExtendedOperands() {
        return extendedOperands;
    }

    public void setExtendedOperands(List<StatementNode> extendedOperands) {
        this.extendedOperands = extendedOperands;
    }



    @Override
    public NodeType getObject() {
        return NodeType.InfixExpressionStmNode;
    }

    @Override
    public String getType() {
        return type;
    }



    @Override
    public int getHashCode() {
        return 0;
    }

}
