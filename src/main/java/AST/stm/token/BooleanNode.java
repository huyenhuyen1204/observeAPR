package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;

public class BooleanNode extends StatementNode implements Token {
    private boolean value;
    private String keyVar;

    public BooleanNode(boolean value, ASTNode astNode, String stmString, int line, String classfullName) {
        this.value = value;
        setFullNameParent(classfullName);
        this.statementString = stmString;
        this.line = line;
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = astNode.toString();
        this.children = new ArrayList<>();
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.BooleanNode;
    }

    public BooleanNode(String keyVar) {
        this.keyVar = keyVar;
        this.statementString = keyVar;
    }

    public boolean isValue() {
        return value;
    }

//    @Override
//    public void setNodeType(int nodeType) {
//        this.nodeType = nodeType;
//    }

    @Override
    public NodeType getObject() {
        return NodeType.BooleanNode;
    }

    @Override
    public int getHashCode() {
        if (keyVar != null) {
            return keyVar.hashCode();
        } else {
            return String.valueOf(value).hashCode();
        }
    }

}
