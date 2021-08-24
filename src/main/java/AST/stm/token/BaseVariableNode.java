package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import util.ASTHelper;

import java.util.ArrayList;

public class BaseVariableNode extends StatementNode implements Token {
    private String keyVar;

    public BaseVariableNode() {
        super();
    }

    public BaseVariableNode(SimpleName stmNode, String type, int  line) {
        super();
        this.keyVar = stmNode.getIdentifier();
        Position position = ASTHelper.getPosition(stmNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = stmNode.toString();
        this.line = line;
        this.type = type;
        this.children = new ArrayList<>();
//        this.nodeType = stmNode.getNodeType();
        this.nodeType = NodeType.BaseVariableNode;
    }

    public BaseVariableNode(Name stmNode, String type, int  line) {
        super();
        this.keyVar = stmNode.getFullyQualifiedName();
        Position position = ASTHelper.getPosition(stmNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = stmNode.toString();
        this.line = line;
        this.type = type;
        this.children = new ArrayList<>();
//        this.nodeType = stmNode.getNodeType();
        this.nodeType = NodeType.BaseVariableNode;
    }

    public BaseVariableNode(String keyVar, ASTNode astNode, int  line) {
        super();
        this.keyVar = keyVar;
        this.statementString = keyVar;
        this.line = line;
        this.type = null;
        this.children = new ArrayList<>();
//        if (astNode != null) {
////            this.nodeType = astNode.getNodeType();
//        }
        this.nodeType = NodeType.BaseVariableNode;
    }

    public BaseVariableNode(Name stmNode, String varname, String typevar, CompilationUnit cu) {
        super();
        this.keyVar = varname;
        Position position = ASTHelper.getPosition(stmNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = stmNode.toString();
        this.type = typevar;
        this.line = ASTHelper.getLine(stmNode, cu);
//        this.nodeType = stmNode.getNodeType();
        this.children = new ArrayList<>();
        this.nodeType = NodeType.BaseVariableNode;
    }


    public String getKeyVar() {
        return this.keyVar;
    }

    public void setKeyVar(String keyVar) {
        this.keyVar = keyVar;
    }



    @Override
    public NodeType getObject() {
        return NodeType.BaseVariableNode;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getHashCode() {
        if (type != null) {
            return (type +keyVar).hashCode();
        }else  if (keyVar == null) {
            return "null".hashCode();
        } else {
            return keyVar.hashCode();
        }
    }


}
