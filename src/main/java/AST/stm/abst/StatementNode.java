package AST.stm.abst;

import AST.node.MethodNode;
import AST.stm.token.BaseVariableNode;
import AST.stm.token.MethodCalledNode;
import AST.stm.token.MethodInvocationStmNode;
import sketch.SketchNode;
import util.ReflectionHelper;

import java.util.List;

public class StatementNode {
    protected int line;
    //    protected String keyVar = null;
    protected String statementString;
    protected int startPostion;
    protected int endPostion;
    protected StatementNode parent;
    protected List<StatementNode> children;
    public boolean isSameMethod;
    protected int deepLevel = 0;
    protected String type = null;
    public String fullNameParent = null;
    public String typeFull = null;
    public String cast = null;

    // for generate candidate
    private String prefix = null;
    private String suffix = null;
    protected List<SketchNode> sketchNodes = null;

//    protected int nodeType;
    public NodeType nodeType;

    protected NodeInstance nodeInstance = NodeInstance.NORMAL;

    // Hoan's methods start
    public String getPrefix() {
        if (prefix == null) {
            if (parent instanceof MethodCalledNode || parent instanceof BaseVariableNode) {
                String parentPrefix = parent.getPrefix();
                if (!parentPrefix.equals(""))
                    prefix = parent.getPrefix() + "." + parent.toString();
                else
                    prefix = parent.toString();
            } else
                prefix = "";
        }
        return prefix;
    }

    public String getSuffix() {
        if (suffix == null) {
            if (children.size() == 0) {
                suffix = "";
            } else {
                String suffixChild = children.get(0).getSuffix();
                if (suffixChild.equals("")) {
                    suffix = children.get(0).toString();
                } else {
                    suffix = children.get(0).toString() + "."
                            + children.get(0).getSuffix();
                }
            }
        }
        return suffix;
    }

    public List<SketchNode> getSketchNodes() {
        return sketchNodes;
    }

    public void setSketchNodes(List<SketchNode> sketchNodes) {
        this.sketchNodes = sketchNodes;
    }

    // Hoan's methods end

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

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getStatementString() {
        return this.statementString;
    }

    public void setStatementString(String statementString) {
        this.statementString = statementString;
    }

    public void setChild(StatementNode child, MethodNode methodNode) {
        String childType = null;
        if (child.getType() == null) {
            if (child instanceof BaseVariableNode) {
                if (this.getType() != null) {
                    childType = ReflectionHelper.findFieldType(this.getType(),
                            ((BaseVariableNode) child).getKeyVar(), methodNode);
                } else {
                    if (this.fullNameParent != null) {
                        childType = ReflectionHelper.findFieldType
                                (this.fullNameParent,
                                        ((BaseVariableNode) child).getKeyVar(), methodNode);
                    } else {
                        System.out.println();
                    }
                }
            } else if (child instanceof MethodCalledNode) {
                childType = ReflectionHelper.findMethodCalledNodeType((MethodCalledNode) child, this);
            }
            if (childType != null) {
                child.setType(childType);
            } else {
//                System.out.println("type == null");
            }
        }
        this.children.add(child);
    }

    public void setFullNameParent(String fullNameParent) {
        this.fullNameParent = fullNameParent;
    }

    public int getDeepLevel() {
        return this.deepLevel;
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
        if (this instanceof MethodInvocationStmNode) {
            ((MethodInvocationStmNode) this).getLastChild(this).setType(type);
            ((MethodInvocationStmNode) this).getNodes().get(((MethodInvocationStmNode) this)
                    .getNodes().size() - 1).setType(type);
        }
    }


}
