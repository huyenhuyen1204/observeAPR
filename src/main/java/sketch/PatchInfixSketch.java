package sketch;

import AST.stm.abst.StatementNode;
import AST.stm.node.OperatorNode;
import AST.stm.token.InfixExpressionStmNode;
import util.Genner3;

import java.util.ArrayList;
import java.util.List;

public class PatchInfixSketch extends PatchSketch {
//    private StatementNode operator;
//    private StatementNode left;
//    private StatementNode right;

    public PatchInfixSketch() {
        this.patchCandidates = new ArrayList<>();
    }


//    public PatchInfixSketch(StatementNode operator, StatementNode left, StatementNode right) {
//        this.operator = operator;
//        this.left = left;
//        this.right = right;
//        this.patchCandidates = new ArrayList<>();
//        this.changedArgsMap = new HashMap<>();
//    }
//    public PatcheInfixSketch(String operator, SketchNode leftNode, SketchNode rightNode) {
//        this.operator = operator;
//        this.leftNode = leftNode;
//        this.rightNode = rightNode;
//    }

//    public StatementNode getOperator() {
//        return operator;
//    }

//    public void genCandidates(util.Genner3 genner3) {
//        if (operator instanceof SketchNode) {
//            List<PatchCandidate> patches =
//                    genner3.generateOperatorCandidates((OperatorSketcheNode) operator, this);
//            this.patchCandidates.addAll(patches);
//        }
//        if (left instanceof SketchNode) {
//            this.computePatchCandidates(genner3);
//        }
//        if (right instanceof SketchNode) {
//            this.computePatchCandidates(genner3);
//        }
//    }

//    public void setOperator(StatementNode operator) {
//        this.operator = operator;
//    }

    //    public SketchNode getLeftNode() {
//        return leftNode;
//    }
//
//    public void setLeftNode(SketchNode leftNode) {
//        this.leftNode = leftNode;
//    }
//
//    public SketchNode getRightNode() {
//        return rightNode;
//    }
//
//    public void setRightNode(SketchNode rightNode) {
//        this.rightNode = rightNode;
//    }


    private String computeString(StatementNode stm) {
        String equation = "";
        if (stm instanceof InfixExpressionStmNode) {
            //left
            StatementNode left = ((InfixExpressionStmNode) stm).getLeft();
            if (left instanceof InfixExpressionStmNode) {
                String leftString = computeString(left);
                equation += leftString;
            } else {
                equation += getString(left);
            }
            //operator
            StatementNode operator = ((InfixExpressionStmNode) stm).getOperator();
            equation += " " + getString(operator) + " ";
            //right
            StatementNode right = ((InfixExpressionStmNode) stm).getRight();
            if (right instanceof InfixExpressionStmNode) {
                String rightString = computeString(right);
                equation += rightString;
            } else {
                equation += getString(right);
            }

        } else {
            equation += stm.getStatementString();
        }
        return equation;
    }

    private String getString(StatementNode node) {
        if (node == changes.get(0).getOriginalNode()) {
            return changes.get(0).getSketchNode().toString();
        } else {
            return node.getStatementString();
        }
    }


    @Override
    public String toString() {
        return computeString(getTargetNode());
    }

    public void genCandidates(Genner3 genner3) {
        if (changes.get(0).getOriginalNode() instanceof OperatorNode) {
            List<PatchCandidate> patches =
                    genner3.genOperatorCandidates((OperatorSketcheNode) changes.get(0).getSketchNode(),
                            this);
            if (patches != null) {
                this.patchCandidates.addAll(patches);
            }
        } else {
            this.computePatchCandidates(genner3);
        }
    }
}
