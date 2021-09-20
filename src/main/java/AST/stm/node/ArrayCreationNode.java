package AST.stm.node;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;

public class ArrayCreationNode extends StatementNode {
    public ArrayCreationNode(ASTNode astNode, int line, String classfullName) {
        this.line = line;
        setFullNameParent(classfullName);
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = astNode.toString();
        //set child
        this.children = new ArrayList<>();
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.ArrayCreationNode;
    }

}
