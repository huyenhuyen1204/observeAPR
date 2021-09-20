package sketch;

import AST.stm.abst.StatementNode;

import java.util.List;

public class Change {
    private StatementNode originalNode;
    private SketchNode sketchNode;

    private List<Change> changes = null;

    public void setOriginalNode(StatementNode originalNode) {
        this.originalNode = originalNode;
    }

    public StatementNode getOriginalNode() {
        return originalNode;
    }

    public void setSketchNode(SketchNode sketchNode) {
        this.sketchNode = sketchNode;
    }

    public SketchNode getSketchNode() {
        return sketchNode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (sketchNode.getTargetScope() == SketchNode.Scope.ONLY_CURRENT) {
            builder.append(originalNode.toString()).append(" -> ");
            builder.append(sketchNode.toString());

        } else if (sketchNode.getTargetScope() == SketchNode.Scope.ALL_AFTER) {
            String suffix = originalNode.getSuffix();
            suffix = suffix.equals("") ? suffix : "." + suffix;
            builder.append(originalNode.toString()).append(suffix);
            builder.append(" -> ").append(sketchNode.toString());
        }
        return builder.toString();
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    public List<Change> getChanges() {
        return changes;
    }
}
