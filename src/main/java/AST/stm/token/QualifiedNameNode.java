package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;

/**
 * eg: that.toolTipText
 * with 'that' : org.jfree.chart.annotations.AbstractXYAnnotation
 *      toolTipText: String in class of 'that'
 */
public class QualifiedNameNode extends StatementNode implements Token {
    private BaseVariableNode qualifier;
    private BaseVariableNode name;
    public QualifiedNameNode(BaseVariableNode qualifier, BaseVariableNode name, ASTNode astNode, int line)  {
        this.qualifier = qualifier;
        this.name = name;
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = astNode.toString();
        this.line = line;
        //add child
        this.children = new ArrayList<>();
//        this.children.add(name);
//        this.children.add(qualifier);
        //add parent
        if (name != null) {
            name.setParent(this);
        }
        if (qualifier != null) {
            qualifier.setParent(this);
        }
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.QualifiedNameNode;
    }


    public BaseVariableNode getQualifier() {
        return qualifier;
    }

    public void setQualifier(BaseVariableNode qualifier) {
        this.qualifier = qualifier;
    }

    public BaseVariableNode getName() {
        return name;
    }

    public void setName(BaseVariableNode name) {
        this.name = name;
    }


    @Override
    public NodeType getObject() {
        return null;
    }

    public void setType(String type) {
        this.type = type;
        this.name.setType(type);
    }

    @Override
    public int getHashCode() {
        return (qualifier.getType() + name.getKeyVar()).hashCode();
    }

}
