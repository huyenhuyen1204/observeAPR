package main.core.pattern;

import AST.stm.abst.StatementNode;
import AST.stm.node.OperatorNode;
import AST.stm.token.InfixExpressionStmNode;
import main.core.Genner;
import main.core.PatchCandidate;
import main.core.token.OperatorToken;
import main.core.token.Token;

import java.util.List;

public class InfixPattern extends Pattern {


    public InfixPattern() {
        super();
    }

    //TODO: phan tich String de tong hop (list bien, moi bien co prefix & suffix
    private String computeString(StatementNode stm) {
        String equation = "";
        if (stm instanceof InfixExpressionStmNode) {
            equation += stm.cast + stm.getLparen();
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
            equation += stm.getRparen();
        } else {
            equation += stm.getStatementString();
        }
        return equation;
    }

    private String getString(StatementNode node) {
        String result = "";
        String prefix = node.getPrefix();
        prefix = prefix.equals("") ? prefix : prefix + ".";

        String suffix = node.getSuffix();
        suffix = suffix.equals("") ? suffix : "." + suffix;
        if (node == changes.get(0).getOriginalNode()) {
            if (changes.get(0).getToken().getTargetScope() == Token.Scope.ONLY_CURRENT) {
                result = prefix + changes.get(0).getToken().toString();
                return result + suffix;
            } else if (changes.get(0).getToken().getTargetScope() == Token.Scope.ALL_AFTER) {
                return changes.get(0).getToken().toString();
            }
        } else {
            if (node.getChildren().size() == 0) {
                result = prefix + node.getStatementString();
                return result + suffix;
            } else {
                return getString(node.getChildren().get(0));
            }
        }
        return result;
    }


    @Override
    public String toString() {
        return computeString(getTargetNode());
    }

    public void genCandidates(Genner genner) {
        if (changes.get(0).getOriginalNode() instanceof OperatorNode) {
            List<PatchCandidate> patches =
                    genner.genOperatorCandidates((OperatorToken) changes.get(0).getToken(),
                            this);
            if (patches != null) {
                this.patchCandidates.addAll(patches);
            }
        } else {
            this.computePatchCandidates(genner);
        }
    }
}
