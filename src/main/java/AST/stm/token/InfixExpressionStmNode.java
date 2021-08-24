package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeInstance;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;
import java.util.List;

public class InfixExpressionStmNode extends StatementNode implements Token {
    private String operator;
    private StatementNode left;
    private StatementNode right;
    private List<StatementNode> extendedOperands;


    public InfixExpressionStmNode(String operator, StatementNode left, StatementNode right,
                                  List<StatementNode> extendedOperands, int line, String stmString, ASTNode astNode) {
        this.operator = operator;
        this.left = left;
        this.right = right;

        this.extendedOperands = extendedOperands;
        this.line = line;
        this.statementString = stmString;
        //set child
        this.children = new ArrayList<>();
        Position pos = ASTHelper.getPosition(astNode);
        int startLeft = pos.getStartPos();
        int endRight = pos.getEndPos();
        if (left != null) {
            children.add(left);
            left.setParent(this);
            left.setNodeInstance(NodeInstance.INFIX);
            this.startPostion = left.getEndPostion();
        } else {
            this.startPostion = startLeft;
        }
        if (right != null) {
            children.add(right);
            right.setParent(this);
            right.setNodeInstance(NodeInstance.INFIX);
            this.endPostion = right.getStartPostion();
        } else {
            this.endPostion = endRight;
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
        this.nodeType = NodeType.InfixExpressionStmNode;
//        this.nodeType = astNode.getNodeType();
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



    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
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
        return null;
    }



    @Override
    public int getHashCode() {
        return 0;
    }

}
