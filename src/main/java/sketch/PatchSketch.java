package sketch;

import AST.node.MethodNode;
import AST.stm.abst.StatementNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.MethodCalledNode;
import util.Genner3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For base var & methodCalled
 */
public class PatchSketch {
    private StatementNode targetNode; // usually a base var or a method invocation
    protected List<Change> changes = new ArrayList<>();
    private StatementNode changedMethod = null; // not null if there is a changed method
    protected Map<StatementNode, PatchSketch> changedArgsMap = null; // not null if changedMethod not null

    private MethodNode methodNode;
    protected List<PatchCandidate> patchCandidates = null;
//    private static Map<String, List<BaseVariableCandidate>> typeToListOfVarMap = new HashMap<>();

    private boolean isParam;

    public PatchSketch() {
        isParam = false;
        this.patchCandidates = new ArrayList<>();
    }

    public void computePatchCandidates(Genner3 genner) {
//        if (patchCandidates == null) {
//        }

        List<SketchNode> sketchNodes = new ArrayList<>();
        // get all sketches in changes
        for (Change change : changes) {
            sketchNodes.add(change.getSketchNode());
        }

        // find all candidates for each sketchNode
        for (SketchNode sketchNode : sketchNodes) {
            genner.generateCandidates(sketchNode);
        }

        // synthesize all the combination of changes
        List<Map<SketchNode, StatementNode>> synthesis = new ArrayList<>();
        for (SketchNode sketchNode : sketchNodes) {
            List<StatementNode> candidates = sketchNode.getNodeCandidates();
            if (candidates == null) {
                System.out.println("Unexpected behaviour: there are no candidates for " + sketchNode.toString());
                return;
            }
            if (synthesis.size() == 0) {
                for (StatementNode candidate : candidates) {
                    if (sketchNode.getOriginalValue() == null
                            || (sketchNode.getOriginalValue() != null && !sketchNode.getOriginalValue()
                            .equals(candidate.getStatementString()))) {
                        Map<SketchNode, StatementNode> newMap = new HashMap<>();
                        newMap.put(sketchNode, candidate);
                        synthesis.add(newMap);
                    }
                }

                if (synthesis.size() == 0) {
                    break;
                } else {
                    continue;
                }
            }

            List<Map<SketchNode, StatementNode>> tmpSynthesis = new ArrayList<>();
            for (Map<SketchNode, StatementNode> synthesizedMap : synthesis) {
                for (StatementNode candidate : candidates) {
                    if (sketchNode.getOriginalValue() == null
                            || (sketchNode.getOriginalValue() != null && !sketchNode.getOriginalValue()
                            .equals(candidate.getStatementString()))) {
                        Map<SketchNode, StatementNode> newMap = new HashMap<>(synthesizedMap);
                        newMap.put(sketchNode, candidate);
                        tmpSynthesis.add(newMap);
                    }
                }
            }

            synthesis.clear();
            if (tmpSynthesis.size() > 0) { // if there is no other candidate for a base var, then tmpSynthesis.size will be zero
                synthesis.addAll(tmpSynthesis);
            } else {
                break; // there is a sketch that can not find any candidate
            }
        }

        for (Map<SketchNode, StatementNode> changesMap : synthesis) {
            PatchCandidate patchCandidate = new PatchCandidate();
            patchCandidate.setChangesMap(changesMap);
            patchCandidate.setPatchSketch(this);
            patchCandidate.computeContent();
            this.patchCandidates.add(patchCandidate);
        }
    }

    public void setTargetNode(StatementNode targetNode) {
        this.targetNode = targetNode;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setMethodNode(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (changedMethod == null) { // the changed element is not a method
            StatementNode originalElement = changes.get(0).getOriginalNode();
            SketchNode sketchNode = changes.get(0).getSketchNode();
            String prefix = originalElement.getPrefix();
            prefix = prefix.equals("") ? prefix : prefix + ".";
            builder.append(prefix).append(sketchNode.toString());

            if (sketchNode.getTargetScope() == SketchNode.Scope.ONLY_CURRENT) { // need to get suffix
                String suffix = originalElement.getSuffix();
                suffix = suffix.equals("") ? suffix : "." + suffix;
                builder.append(suffix);
            }
        } else { // the changed elements is params in a method or ClassInstanceCreationNode
            String prefix = changedMethod.getPrefix();
            prefix = prefix.equals("") ? prefix : prefix + ".";
            builder.append(prefix);
            List<StatementNode> argStatementNodes = null;
            if (changedMethod instanceof ClassInstanceCreationNode) {
                builder.append(((ClassInstanceCreationNode) changedMethod).getName()).append("(");
                argStatementNodes = ((ClassInstanceCreationNode) changedMethod).getArgs();
            } else if (changedMethod instanceof MethodCalledNode) {
                builder.append(((MethodCalledNode) changedMethod).getMethodName()).append("(");
                argStatementNodes = ((MethodCalledNode) changedMethod).getAgurements();
            }
            for (int i = 0; i < argStatementNodes.size(); i++) {
                StatementNode argNode = argStatementNodes.get(i);
                PatchSketch argSketch = changedArgsMap.get(argNode);
                if (argSketch != null) {
                    builder.append(argSketch.toString());
                }
                else {
                    builder.append(argNode.getStatementString());
//                    if (argNode instanceof Token) {
//                        builder.append(argNode.getStatementString());
//                    } else {
//                        builder.append(argNode.getStatementString());
//                    }
                }

                if (i < argStatementNodes.size() - 1)
                    builder.append(",");
            }
            builder.append(")");

            String suffix = changedMethod.getSuffix();
            suffix = suffix.equals("") ? suffix : "." + suffix;
            builder.append(suffix);
        }
        return builder.toString();
    }

    public void setChangedMethod(StatementNode changedMethod) {
        this.changedMethod = changedMethod;
    }

    public void setChangedArgsMap(Map<StatementNode, PatchSketch> changedArgsMap) {
        this.changedArgsMap = changedArgsMap;
    }

    public Map<StatementNode, PatchSketch> getChangedArgsMap() {
        return changedArgsMap;
    }

    public StatementNode getChangedMethod() {
        return changedMethod;
    }

    public StatementNode getTargetNode() {
        return targetNode;
    }

    public List<PatchCandidate> getPatchCandidates() {
        return patchCandidates;
    }

    public boolean isParam() {
        return isParam;
    }

    public void setParam(boolean param) {
        isParam = param;
    }

    public void addCandidate(PatchCandidate patchCandidate) {
        this.patchCandidates.add(patchCandidate);
    }
}
