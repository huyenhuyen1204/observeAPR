package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;
import java.util.List;

public class MethodCalledNode extends StatementNode implements Token {
    private String methodType;
    private String methodName;
    private List<StatementNode> agurementTypes = null;


    public MethodCalledNode(String methodName, List<StatementNode> agurementTypes, ASTNode astNode, int line) {
        super();
        if (this.getParent() == null) {
            BaseVariableNode baseVariableNode = new BaseVariableNode();
            this.setParent(baseVariableNode);
        }
        this.methodName = methodName;
        this.agurementTypes = new ArrayList<>();
        if (agurementTypes.size() > 0) {
            this.agurementTypes.addAll(agurementTypes);
        }
        Position position = ASTHelper.getPosition(astNode);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        this.statementString = astNode.toString();
        this.line = line;
        //set child
        this.children = new ArrayList<>();
//        this.nodeType = astNode.getNodeType();
        this.nodeType = NodeType.MethodCalledNode;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<StatementNode> getAgurements() {
        return agurementTypes;
    }

    public void setAgurementTypes(List<StatementNode> agurementTypes) {
        this.agurementTypes = agurementTypes;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    @Override
    public NodeType getObject() {
        return NodeType.MethodCalledNode;
    }

    @Override
    public String getType() {
        return methodType;
    }

    @Override
    public void setType(String type) {
        this.methodType = type;
    }

    @Override
    public int getHashCode() {
        String params = "";
        if (agurementTypes != null) {
            for (StatementNode param : agurementTypes) {
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
        return (methodName + params).hashCode();
    }

    public void setDeepLevel(int deepLevel) {
        this.deepLevel = deepLevel;
        setDeepLevel(this, deepLevel);
    }
    private void setDeepLevel(StatementNode statementNode, int dl) {
        this.deepLevel = dl;
        if (statementNode instanceof MethodCalledNode) {
            for (StatementNode arg : ((MethodCalledNode) statementNode).getAgurements()) {
                setDeepLevel(arg, dl);
            }
        }
        if (statementNode.getChildren().size() > 0) {
            for (StatementNode child : statementNode.getChildren()) {
                setDeepLevel(child, dl);
            }
        }
    }


}
