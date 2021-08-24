package AST.stm.node;

import AST.obj.Position;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;

public class ArrayInitializerNode extends StatementNode {
    public ArrayInitializerNode(ASTNode astNode, int line) {
        this.line = line;
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = astNode.toString();
        //set child
        this.children = new ArrayList<>();
//        this.nodeType = astNode.getNodeType();
    }
}
