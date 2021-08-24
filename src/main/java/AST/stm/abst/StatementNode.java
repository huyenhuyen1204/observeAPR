package AST.stm.abst;

import AST.stm.token.MethodCalledNode;

import java.util.List;

public class StatementNode {
    protected int line;
    //    protected String keyVar = null;
    protected String statementString;
    protected int startPostion;
    protected int endPostion;
    protected StatementNode parent;
    protected List<StatementNode> children;

    protected int deepLevel  = 0;
    protected String type = null;

//    protected int nodeType;
    public NodeType nodeType;

    protected NodeInstance nodeInstance = NodeInstance.NORMAL;

    public NodeInstance getNodeInstance() {
        return nodeInstance;
    }

    public void setNodeInstance(NodeInstance nodeInstance) {
        this.nodeInstance = nodeInstance;
    }

    public List<StatementNode> getChildren() {
        return this.children;
    }

    public StatementNode getParent() {
        return this.parent;
    }


    public void setParent(StatementNode parent) {
        this.parent = parent;
    }


    public int getStartPostion() {
        return startPostion;
    }

    public void setStartPostion(int startPostion) {
        this.startPostion = startPostion;
    }

    public int getEndPostion() {
        return endPostion;
    }

    public void setEndPostion(int endPostion) {
        this.endPostion = endPostion;
    }


    public int getLine() {
        return this.line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getStatementString() {
        return this.statementString;
    }

    public void setStatementString(String statementString) {
        this.statementString = statementString;
    }

    public void setChildren(StatementNode children) {
        this.children.add(children);
    }

    public int getDeepLevel() {
        return deepLevel;
    }
    public void setDeepLevel(int deepLevel) {
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


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
