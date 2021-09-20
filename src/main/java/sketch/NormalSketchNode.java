package sketch;

import AST.stm.abst.StatementNode;
import AST.stm.token.InfixExpressionStmNode;

import java.util.List;

public class NormalSketchNode extends SketchNode {
    private List<MethodSketchNode> sketchCandidates = null;

    public NormalSketchNode(int deepLevel) {
        this.deepLevel = deepLevel;
    }

    public NormalSketchNode(StatementNode targetNode, Scope targetScope) {
        if (targetNode.getParent() == null) {
            this.parentType = null;
        } else {
            if (!(targetNode.getParent() instanceof InfixExpressionStmNode)) {
                this.parentType = targetNode.getParent().getType();
            }
        }
        this.target = targetNode;
        this.targetScope = targetScope;
    }

//    public void genCandidates() {
//        this.nodeCandidates = new ArrayList<>();
//        this.nodeCandidates.addAll(SketchNode.makeUpBaseVars());
//
//        List<MethodCalledNode> methods = SketchNode.makeUpMethodCallNodes();
//        List<MethodCalledNode> methodsWithoutArgs = new ArrayList<>(methods);
//        methodsWithoutArgs.removeIf(methodCalledNode -> {
//            if (methodCalledNode.getAgurements() == null) {
//                return false;
//            } else {
//                return methodCalledNode.getAgurements().size() > 0;
//            }
//        });
//        List<MethodCalledNode> methodsWithArgs = new ArrayList<>(methods);
//        methodsWithArgs.removeIf(methodCalledNode -> {
//            if (methodCalledNode.getAgurements() == null) {
//                return true;
//            } else {
//                return methodCalledNode.getAgurements().size() == 0;
//            }
//        });
//
//        this.nodeCandidates.addAll(methodsWithoutArgs);
//        if (deepLevel < util.Genner3.MAX_DEEP_LEVEL) {
//            this.sketchCandidates = new ArrayList<>();
//            for (MethodCalledNode methodCall : methodsWithArgs) {
//                MethodSketchNode methodSketch = new MethodSketchNode(methodCall, deepLevel);
//                this.sketchCandidates.add(methodSketch);
//                methodSketch.genCandidates();
//
//                this.nodeCandidates.addAll(methodSketch.getNodeCandidates());
//            }
//        }
        // I. Generate node candidates
//        if (parentType == null) { // can replace the sketch with method invocation
//        } else {
//            // find methods, and baseVars that is dataType and has parent type parentType
//            this.nodeCandidates = new ArrayList<>();
//            this.nodeCandidates.addAll(SketchNode.makeUpBaseVars());
//        }

        // If deepLevel < MAX_DEEP_LV then generate sketch candidates.
        // After that, synthesize node candidates from these sketches
        // if found methods that have argument (statement type) then generate sketch for it;
//        this.sketchCandidates = new ArrayList<>();
        // -> create method sketch nodes, with params are normal sketch node
        // -> genCandiates for params and synthesize

        // II. Synthesize node candidates from sketch candidates

//    }

    public void setSketchCandidates(List<MethodSketchNode> sketchCandidates) {
        this.sketchCandidates = sketchCandidates;
    }

    public List<MethodSketchNode> getSketchCandidates() {
        return sketchCandidates;
    }

    @Override
    public String toString() {
        return "<" + this.dataType + ">";
    }
}
