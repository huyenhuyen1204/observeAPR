package sketch;

import AST.stm.abst.StatementNode;

public class InfixSketcheNode extends SketchNode{
    private SketchNode infix;
    private SketchNode left;
    private SketchNode right;

    public InfixSketcheNode(SketchNode infix, SketchNode left, SketchNode right) {
        this.infix = infix;
        this.left = left;
        this.right = right;
    }

    public InfixSketcheNode(StatementNode target, Scope targetScope, SketchNode infix, SketchNode left, SketchNode right) {
        super(target, targetScope);
        this.infix = infix;
        this.left = left;
        this.right = right;
    }

    public SketchNode getInfix() {
        return infix;
    }

    public void setInfix(SketchNode infix) {
        this.infix = infix;
    }

    public SketchNode getLeft() {
        return left;
    }

    public void setLeft(SketchNode left) {
        this.left = left;
    }

    public SketchNode getRight() {
        return right;
    }

    public void setRight(SketchNode right) {
        this.right = right;
    }
}
