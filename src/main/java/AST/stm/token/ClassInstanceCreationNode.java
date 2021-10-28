package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;
import java.util.List;

public class ClassInstanceCreationNode extends StatementNode implements Token {
    private String name;
    private List<StatementNode> args;
    {
        this.nodeType = NodeType.ClassInstanceCreationNode;
    }
    public ClassInstanceCreationNode(String type, List<StatementNode> args,
                                     ASTNode astNode, int line, String classfullName) {
        super();
        setFullNameParent(classfullName);
        this.name = type;
        this.setType(type);
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.line = line;
        this.statementString = astNode.toString();
        this.args = new ArrayList<>();
        this.args.addAll(args);
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

    public ClassInstanceCreationNode (String name, String type) {
        this.children = new ArrayList<>();
        this.args = new ArrayList<>();
        this.setType(type);
        this.name = name;
        this.nodeType = NodeType.ClassInstanceCreationNode;
    }

    public void addParam (StatementNode statementNode) {
        this.args.add(statementNode);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StatementNode> getArgs() {
        return args;
    }

    public void setArgs(List<StatementNode> args) {
        this.args = args;
    }

    @Override
    public String toString () {
        String content = "new " + name + "(";
        for (StatementNode param : args) {
            content += param.getStatementString() + ",";
        }
        content = content.substring(0, content.length() -1);
        return  content + ")";
    }

    @Override
    public NodeType getObject() {
        return NodeType.ClassInstanceCreationNode;
    }

    @Override
    public int getHashCode() {
        String params = "";
        if (args != null) {
            for (StatementNode param : args) {
                if (param instanceof Token) {
                    params += param.getType();
                } else {
                    if (param != null) {
                        params += param.getStatementString();
                    } else {
                        params += "";
                    }
                }
            }
        }
        return (name + params).hashCode();
    }
}
