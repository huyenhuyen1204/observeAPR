package AST.stm.node;

import AST.obj.Position;
import AST.stm.abst.NodeInstance;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;

public class IfStmNode extends StatementNode  {
    // children: infix, then, if else, else
    private StatementNode infix;
    public IfStmNode(StatementNode infix, ASTNode astNode, int line) {
        super();
        this.infix = infix;
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.line = line;
        this.statementString = astNode.toString();
        //set child
        this.children = new ArrayList<>();
        if (infix != null) {
            this.infix.setParent(this);
            this.infix.setNodeInstance(NodeInstance.INFIX);
            children.add(infix);
        }
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.IfStmNode;
    }

}
