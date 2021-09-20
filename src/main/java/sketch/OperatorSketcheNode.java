package sketch;

import AST.stm.abst.StatementNode;

import java.util.ArrayList;

public class OperatorSketcheNode extends SketchNode {

    public OperatorSketcheNode(StatementNode targetNode, Scope targetScope) {
        if (targetNode.getParent() == null) {
            this.parentType = null;
        } else {
            this.parentType = targetNode.getParent().getType();
        }
        this.target = targetNode;
        this.targetScope = targetScope;
        this.nodeCandidates = new ArrayList<>();
    }
    @Override
    public String toString() {
        return "<" + this.dataType + ">";
    }
}
