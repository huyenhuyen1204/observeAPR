package AST.stm.node;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;
import java.util.List;

public class ClassInstanceCreationNode extends StatementNode {
    private String fullyQualifiedClassName;
    private List<StatementNode> args;

    public ClassInstanceCreationNode(String type, List<StatementNode> args, ASTNode astNode, int line) {
        super();
        this.fullyQualifiedClassName = type;
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.line = line;
        this.statementString = astNode.toString();
        this.args = new ArrayList<>();
        this.args.addAll(args);
        children = new ArrayList<>();
        //set child
//        children = args;
        //set parent
//        for (StatementNode stm : args) {
//            if (stm != null) {
//                stm.setParent(this);
//            }
//        }
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.ClassInstanceCreationNode;
    }


    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public void setFullyQualifiedClassName(String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
    }

    public List<StatementNode> getArgs() {
        return args;
    }

    public void setArgs(List<StatementNode> args) {
        this.args = args;
    }

}
