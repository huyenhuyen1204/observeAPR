package AST.stm.nodetype;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;

public class NumbericNode extends StatementNode {
    public static final String type = "NumberLiteral";
    private Object numberic;

    public NumbericNode(Object numberic, ASTNode astNode) {
        this.numberic = numberic;
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = astNode.toString();
        this.children = new ArrayList<>();
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.NumbericNode;
    }

}
