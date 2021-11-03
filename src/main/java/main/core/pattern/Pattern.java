package main.core.pattern;

import AST.stm.abst.StatementNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.MethodCalledNode;
import AST.stm.token.MethodInvocationStmNode;
import main.core.Change;
import main.core.Genner;
import main.core.PatchCandidate;
import main.core.token.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pattern {
    protected StatementNode targetNode;
    protected List<Change> changes;
    protected List<PatchCandidate> patchCandidates;
    private float patternLevel = -1f;

    public int getTokenChangedSize() {
        return tokenChangedSize;
    }

    public void setTokenChangedSize(int tokenChangedSize) {
        this.tokenChangedSize = tokenChangedSize;
    }

    protected int tokenChangedSize = 1;

    public Pattern() {
        this.changes = new ArrayList<>();
        this.patchCandidates = new ArrayList<>();
    }

    public float getPatternLevel() {
        int lv = 0;
        if (Float.compare(patternLevel, -1) == 0) {
            if (targetNode instanceof MethodInvocationStmNode
                    && this.getChanges().get(0).getToken().getTargetScope()
                    == Token.Scope.ALL_AFTER && this.getChanges().get(0).getOriginalNode().getStmBugDeepLevel() == 0) {
                this.patternLevel = Float.compare(patternLevel, -1) == 0 ? -0.5f : patternLevel - 0.5f;
            } else {
                for (Change change : changes) {
                    lv += change.getOriginalNode().getStmBugDeepLevel();
                }
                this.patternLevel = ((float) lv / changes.size());
            }
        }
        return this.patternLevel;
    }

    public void setPatternLevel(float patternLevel) {
        this.patternLevel = patternLevel;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    public StatementNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(StatementNode targetNode) {
        this.targetNode = targetNode;
    }

    public void computePatchCandidates(Genner genner) {

        List<Token> tokens = new ArrayList<>();
        // get all tokens in changes
        for (Change change : changes) {
            tokens.add(change.getToken());
        }

        // find all candidates for each sketchNode
        for (Token token : tokens) {
            genner.generateCandidates(token);
        }

        // synthesize all the combination of changes
        List<Map<Token, StatementNode>> synthesis = new ArrayList<>();
        for (Token token : tokens) {
            List<StatementNode> candidates = token.getCandidates();
            if (candidates == null) {
                System.out.println("Unexpected behaviour: there are no candidates for " + token.toString());
                return;
            }
            if (synthesis.size() == 0) {
                for (StatementNode candidate : candidates) {
                    if (token.getOriginalValue() == null
                            || (token.getOriginalValue() != null && !token.getOriginalValue()
                            .equals(candidate.toString()))) {
                        Map<Token, StatementNode> newMap = new HashMap<>();
                        newMap.put(token, candidate);
                        synthesis.add(newMap);
                    }
                }

                if (synthesis.size() == 0) {
                    break;
                } else {
                    continue;
                }
            }

            List<Map<Token, StatementNode>> tmpSynthesis = new ArrayList<>();
            for (Map<Token, StatementNode> synthesizedMap : synthesis) {
                for (StatementNode candidate : candidates) {
                    if (token.getOriginalValue() == null
                            || (token.getOriginalValue() != null && !token.getOriginalValue()
                            .equals(candidate.toString()))) {
                        Map<Token, StatementNode> newMap = new HashMap<>(synthesizedMap);
                        newMap.put(token, candidate);
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

        for (Map<Token, StatementNode> changesMap : synthesis) {
            PatchCandidate patchCandidate = new PatchCandidate();
            patchCandidate.setChangesMap(changesMap);
            patchCandidate.setPattern(this);
            patchCandidate.computeContent(this);
            this.patchCandidates.add(patchCandidate);
        }
    }

    public List<PatchCandidate> getPatchCandidates() {
        return patchCandidates;
    }

    public void setPatchCandidates(List<PatchCandidate> patchCandidates) {
        this.patchCandidates = patchCandidates;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this instanceof BasePattern) { // the changed element is not a method
            StatementNode originalElement = changes.get(0).getOriginalNode();
            Token token = changes.get(0).getToken();
            if (token.getTargetScope() == Token.Scope.ONLY_CURRENT) { // need to get suffix
                String prefix = originalElement.getPrefix();
                prefix = prefix.equals("") ? prefix : prefix + ".";
                builder.append(prefix).append(token.toString());

                String suffix = originalElement.getSuffix();
                suffix = suffix.equals("") ? suffix : "." + suffix;
                builder.append(suffix);
            } else if (token.getTargetScope() == Token.Scope.ALL_AFTER) {
                builder.append(token.toString());
            }
        } else if (this instanceof MethodPattern) { // the changed elements is params in a method or ClassInstanceCreationNode
            StatementNode changedMethod = ((MethodPattern) this).getChangedMethod();
            Map<StatementNode, Pattern> changedArgsMap = ((MethodPattern) this).getChangedArgsMap();
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
                Pattern argPattern = changedArgsMap.get(argNode);
                if (argPattern != null) {
                    builder.append(argPattern.toString());
                } else {
                    builder.append(argNode.toString());
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

}
