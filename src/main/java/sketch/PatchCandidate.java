package sketch;

import AST.obj.CandidateElement;
import AST.stm.abst.StatementNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.MethodCalledNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatchCandidate {

    // changesMap: a sketch node is substituted by the statement source node
    private Map<SketchNode, StatementNode> changesMap = null;
    private PatchSketch patchSketch;
    private String content = null;
    private StatementNode targetNode;
    private List<CandidateElement> candidateElements;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    private float score = -1;
    public Map<SketchNode, StatementNode> getChangesMap() {
        return changesMap;
    }

    public void computeContent() {
        if (content == null) {
            content = computeContent(this.patchSketch);
        }
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String computeContent(PatchSketch patchSketch) { // compute content of a patch according to the "changes" map
        StringBuilder builder = new StringBuilder();
        if (patchSketch.getChangedMethod() == null) { // there is only one changed element, the changed element is not a method
            if (patchSketch instanceof PatchInfixSketch) {
                StatementNode originalElement = patchSketch.getChanges().get(0).getOriginalNode();
                SketchNode sketchNode = patchSketch.getChanges().get(0).getSketchNode();
                StatementNode candidate = changesMap.get(sketchNode);
                String candiString;
                if (candidate instanceof MethodCalledNode) {
                    candiString = candidate.toString();
                } else {
                    candiString = candidate.getStatementString();
                }
                return patchSketch.toString().replace(sketchNode.toString(), candiString);

            } else {
                StatementNode originalElement = patchSketch.getChanges().get(0).getOriginalNode();
                String prefix = originalElement.getPrefix();
                prefix = prefix.equals("") ? prefix : prefix + ".";
                builder.append(prefix);

                SketchNode sketchNode = patchSketch.getChanges().get(0).getSketchNode();
                StatementNode candidate = changesMap.get(sketchNode);
                if (candidate != null) {
                    if (candidate instanceof MethodCalledNode) {
                        builder.append(candidate.toString());
                    } else {
                        builder.append(candidate.getStatementString());
                    }
                }
                if (sketchNode.getTargetScope() == SketchNode.Scope.ONLY_CURRENT) { // need to get suffix
                    String suffix = originalElement.getSuffix();
                    suffix = suffix.equals("") ? suffix : "." + suffix;
                    builder.append(suffix);
                }
            }
        } else { // changed element is a method call node
            String prefix = patchSketch.getChangedMethod().getPrefix();
            prefix = prefix.equals("") ? prefix : prefix + ".";
            builder.append(prefix);
            List<StatementNode> argStatementNodes = new ArrayList<>();
            if (patchSketch.getChangedMethod() instanceof MethodCalledNode) {
                builder.append(((MethodCalledNode) patchSketch.getChangedMethod()).getMethodName()).append("(");
                argStatementNodes = ((MethodCalledNode) patchSketch.getChangedMethod()).getAgurements();

            } else if (patchSketch.getChangedMethod() instanceof ClassInstanceCreationNode) {
                builder.append(((ClassInstanceCreationNode) patchSketch
                        .getChangedMethod()).getName()).append("(");
                argStatementNodes = ((ClassInstanceCreationNode) patchSketch.getChangedMethod()).getArgs();

            }
            for (int i = 0; i < argStatementNodes.size(); i++) {
                StatementNode argNode = argStatementNodes.get(i);
                PatchSketch argSketch = patchSketch.getChangedArgsMap().get(argNode);

                if (argSketch != null) {
                    builder.append(computeContent(argSketch));
                } else {
                    builder.append(argNode.getStatementString());

//                    if (argNode instanceof Token) {
//                        builder.append(argNode.toString());
//                    } else {
//                        builder.append(argNode.getStatementString());
//                    }
                }

                if (i < argStatementNodes.size() - 1)
                    builder.append(",");
            }
            builder.append(")");

            String suffix = patchSketch.getChangedMethod().getSuffix();
            suffix = suffix.equals("") ? suffix : "." + suffix;
            builder.append(suffix);
        }
        calculateScore();
        return builder.toString();
    }

    public void setPatchSketch(PatchSketch patchSketch) {
        this.patchSketch = patchSketch;
    }

    public void setChangesMap(Map<SketchNode, StatementNode> changesMap) {
        this.changesMap = changesMap;
    }

    @Override
    public String toString() {
        return this.content;
    }

    public StatementNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(StatementNode targetNode) {
        this.targetNode = targetNode;
    }

    public PatchSketch getPatchSketch() {
        return patchSketch;
    }

    private void calculateScore () {
        candidateElements = new ArrayList<>();
        for (Change change: patchSketch.getChanges()) {
            StatementNode targetNode = change.getOriginalNode();
            StatementNode sourceNode = changesMap.get(change.getSketchNode());
            CandidateElement candidateElement = new CandidateElement(targetNode, sourceNode);
            score = score == -1? candidateElement.getStatisticalScore(): score*candidateElement.getStatisticalScore();
            candidateElements.add(candidateElement);
        }
    }
}
