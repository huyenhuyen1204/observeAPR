package AST.stm.nodetype;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;

public class StringNode extends StatementNode {
    public static final String type = "StringLiteral";
    private String value;
    private String keyVar;
    public StringNode(int line, String keyVar, String value, String stmString, ASTNode astNode) {
        super();
        this.line = line;
        this.keyVar = keyVar;
        this.statementString = stmString;
        this.value = value;
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = astNode.toString();
        this.children = new ArrayList<>();
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.StringNode;
    }

}
