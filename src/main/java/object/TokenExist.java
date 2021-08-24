package object;

import AST.stm.abst.StatementNode;

public class TokenExist {
    public int line;
    public boolean isSameMethod = false;
    public boolean isSameLine = false;
    public StatementNode statementNode;

    public TokenExist(int line, boolean isSameMethod, boolean  isSameLine,StatementNode statementNode) {
        this.line = line;
        this.isSameMethod = isSameMethod;
        this.statementNode = statementNode;
        this.isSameLine = isSameLine;
    }
}
