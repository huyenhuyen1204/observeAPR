package main.core;

import AST.stm.abst.StatementNode;
import AST.stm.node.OperatorNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.MethodCalledNode;
import main.core.pattern.BasePattern;
import main.core.pattern.InfixPattern;
import main.core.pattern.MethodPattern;
import main.core.pattern.Pattern;
import main.core.token.Token;
import main.obj.CandidateElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatchCandidate {

    // changesMap: a sketch node is substituted by the statement source node
    private Map<Token, StatementNode> changesMap = null;
    private Pattern pattern;
    private String content = null;
    private StatementNode targetNode;
    private List<CandidateElement> candidateElements;
    private float score = -1;
    private float patternLevel = -1;
    private float tokenchangedSize = -1;

    public PatchCandidate() {
        changesMap = new HashMap<>();
    }


    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public float getDistanceScore() {
        return distanceScore;
    }

    public void setDistanceScore(float distanceScore) {
        this.distanceScore = distanceScore;
    }

    private float distanceScore = -1;


    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Map<Token, StatementNode> getChangesMap() {
        return changesMap;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String computeContent(Pattern pattern) { // compute content of a patch according to the "changes" map
        calculateScore();
        StringBuilder builder = new StringBuilder();
//        if (pattern.getChangedMethod() == null) { // there is only one changed element, the changed element is not a method
        if (content == null) {
            if (pattern instanceof InfixPattern) {
                Token token = pattern.getChanges().get(0).getToken();
                StatementNode candidate = changesMap.get(token);
                String candiString;
                if (candidate instanceof MethodCalledNode) {
                    candiString = candidate.toString();
                } else {
                    candiString = candidate.getString();
                }
                this.content = pattern.toString().replace(token.toString(), candiString);
                return this.content;
            } else if (pattern instanceof BasePattern) {
                StatementNode originalElement = pattern.getChanges().get(0).getOriginalNode();
                String prefix = originalElement.getPrefix();
                prefix = prefix.equals("") ? prefix : prefix + ".";
                builder.append(prefix);

                Token token = pattern.getChanges().get(0).getToken();
                StatementNode candidate = changesMap.get(token);
                if (candidate != null) {
                    if (candidate instanceof MethodCalledNode) {
                        builder.append(candidate.toString());
                    } else {
                        builder.append(candidate.getStatementString());
                    }
                }
                if (token.getTargetScope() == Token.Scope.ONLY_CURRENT) { // need to get suffix
                    String suffix = originalElement.getSuffix();
                    suffix = suffix.equals("") ? suffix : "." + suffix;
                    builder.append(suffix);
                }
//            }
            } else if (pattern instanceof MethodPattern) { // changed element is a method call node
                MethodPattern methodPattern = (MethodPattern) pattern;
                String prefix = methodPattern.getChangedMethod().getPrefix();
                prefix = prefix.equals("") ? prefix : prefix + ".";
                builder.append(prefix);
                List<StatementNode> argStatementNodes = new ArrayList<>();
                if (methodPattern.getChangedMethod() instanceof MethodCalledNode) {
                    builder.append(((MethodCalledNode) methodPattern.getChangedMethod()).getMethodName()).append("(");
                    argStatementNodes = ((MethodCalledNode) methodPattern.getChangedMethod()).getAgurements();

                } else if (methodPattern.getChangedMethod() instanceof ClassInstanceCreationNode) {
                    builder.append(((ClassInstanceCreationNode) methodPattern
                            .getChangedMethod()).getName()).append("(");
                    argStatementNodes = ((ClassInstanceCreationNode) methodPattern.getChangedMethod()).getArgs();

                }
                for (int i = 0; i < argStatementNodes.size(); i++) {
                    StatementNode argNode = argStatementNodes.get(i);
                    Pattern argPattern = methodPattern.getChangedArgsMap().get(argNode);

                    if (argPattern != null) {
                        builder.append(computeContent(argPattern));
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

                String suffix = methodPattern.getChangedMethod().getSuffix();
                suffix = suffix.equals("") ? suffix : "." + suffix;
                builder.append(suffix);
            }
            content = builder.toString();
        }
        return content;
    }


    public void setChangesMap(Map<Token, StatementNode> changesMap) {
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

    private void calculateScore() {
        candidateElements = new ArrayList<>();
        this.patternLevel = pattern.getPatternLevel();
        this.tokenchangedSize = pattern.getChanges().size();

        if (pattern instanceof InfixPattern) {
            if (!(pattern.getChanges().get(0).getOriginalNode() instanceof OperatorNode)) {
                StatementNode targetNode = pattern.getChanges().get(0).getOriginalNode();
                StatementNode sourceNode = changesMap.get(pattern.getChanges().get(0).getToken());
                CandidateElement candidateElement = new CandidateElement(targetNode, sourceNode);
                candidateElements.add(candidateElement);
                score = score == -1 ? candidateElement.getStatisticalScore() : score + candidateElement.getStatisticalScore();
                distanceScore = distanceScore == -1 ? candidateElement.getDistanceName() : distanceScore + candidateElement.getDistanceName();
            } else {
                score = 1f;
                distanceScore = 7;
            }
        } else if (pattern instanceof BasePattern) {
            StatementNode targetNode = pattern.getChanges().get(0).getOriginalNode();
            StatementNode sourceNode = changesMap.get(pattern.getChanges().get(0).getToken());
            CandidateElement candidateElement = new CandidateElement(targetNode, sourceNode);
            candidateElements.add(candidateElement);
            score = score == -1 ? candidateElement.getStatisticalScore() : score + candidateElement.getStatisticalScore();
            distanceScore = distanceScore == -1 ? candidateElement.getDistanceName() : distanceScore + candidateElement.getDistanceName();
        } else if (pattern instanceof MethodPattern) {
            int paramSize = -1;
            for (Change change : pattern.getChanges()) {
                StatementNode targetNode = change.getOriginalNode();
                StatementNode sourceNode = changesMap.get(change.getToken());
                CandidateElement candidateElement = new CandidateElement(targetNode, sourceNode);
                candidateElements.add(candidateElement);
//                if (targetNode.getNodeInstance() != NodeInstance.ARGUMENT) {
//                    score = score == -1 ? 1 : score + 1;
//                    distanceScore = distanceScore == -1 ? 1 : distanceScore + 1;
//                } else {
                score = score == -1 ? candidateElement.getStatisticalScore() : score + candidateElement.getStatisticalScore();
                distanceScore = distanceScore == -1 ? candidateElement.getDistanceName() : distanceScore + candidateElement.getDistanceName();
                //set level
                this.patternLevel = pattern.getPatternLevel();
                if (targetNode.paramSize != null) {
                    paramSize = targetNode.paramSize;
                }
            }
            if (paramSize != -1) {
                score = (paramSize - pattern.getChanges().size() + score) / paramSize;
            }
        }

    }

    public float getPatternLevel() {
        return patternLevel;
    }

    public void setPatternLevel(float patternLevel) {
        this.patternLevel = patternLevel;
    }

    public float getTokenchangedSize() {
        return tokenchangedSize;
    }

    public void setTokenchangedSize(float tokenchangedSize) {
        this.tokenchangedSize = tokenchangedSize;
    }
//    private void calculateScore() {
//        candidateElements = new ArrayList<>();
//        if (patchSketch.getChanges().size() == 1) {
//            //change element
//            StatementNode targetNode = patchSketch.getChanges().get(0).getOriginalNode();
//            StatementNode sourceNode = changesMap.get(patchSketch.getChanges().get(0).getToken());
//            CandidateElement candidateElement = new CandidateElement(targetNode, sourceNode);
//            candidateElements.add(candidateElement);
//            score = score == -1 ? candidateElement.getStatisticalScore() : score + candidateElement.getStatisticalScore();
//            distanceScore = distanceScore == -1 ? candidateElement.getDistanceName() : distanceScore + candidateElement.getDistanceName();
//        } else if (patchSketch.getChanges().size() > 1) {
//            //change multi params
//            for (Change change : patchSketch.getChanges()) {
//                StatementNode targetNode = change.getOriginalNode();
//                StatementNode sourceNode = changesMap.get(change.getToken());
//                CandidateElement candidateElement = new CandidateElement(targetNode, sourceNode);
//                candidateElements.add(candidateElement);
////                if (targetNode.getNodeInstance() != NodeInstance.ARGUMENT) {
////                    score = score == -1 ? 1 : score + 1;
////                    distanceScore = distanceScore == -1 ? 1 : distanceScore + 1;
////                } else {
//                score = score == -1 ? candidateElement.getStatisticalScore() : score + candidateElement.getStatisticalScore();
//                distanceScore = distanceScore == -1 ? candidateElement.getDistanceName() : distanceScore + candidateElement.getDistanceName();
////                }
//            }
////            score = score / patchSketch.getChanges().get(0).getOriginalNode().paramSize;;
////            distanceScore = distanceScore / patchSketch.getChanges().get(0).getOriginalNode().paramSize;
//            score = score / patchSketch.getChanges().size();
//            distanceScore = distanceScore / patchSketch.getChanges().size();
//        }

//    }
}
