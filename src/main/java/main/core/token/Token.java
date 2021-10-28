package main.core.token;

import AST.stm.abst.StatementNode;

import java.util.ArrayList;
import java.util.List;

public class Token extends StatementNode {
    protected Scope targetScope; // ALL_AFTER or ONLY_CURRENT
    protected String nodeType;
    protected StatementNode target;

    public StatementNode getTarget() {
        return target;
    }

    public void setTarget(StatementNode target) {
        this.target = target;
    }
    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    protected String parentType;
    protected List<StatementNode> candidates;

    public Token() {
        super();
        this.candidates = new ArrayList<>();
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    protected String originalValue = null;

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public Scope getTargetScope() {
        return targetScope;
    }

    public void setTargetScope(Scope targetScope) {
        this.targetScope = targetScope;
    }

    public List<StatementNode> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<StatementNode> candidates) {
        this.candidates = candidates;
    }

    public enum Scope {
        ALL_AFTER, ONLY_CURRENT
    }
}
