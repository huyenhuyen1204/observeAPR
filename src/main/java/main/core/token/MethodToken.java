package main.core.token;

import AST.stm.abst.StatementNode;
import AST.stm.token.ClassInstanceCreationNode;
import AST.stm.token.MethodCalledNode;
import AST.stm.token.MethodInvocationStmNode;

import java.util.ArrayList;
import java.util.List;

public class MethodToken extends Token {
    private String methodName;
    private List<Token> paramTokens = new ArrayList<>();

    public List<Token> getParamTokens() {
        return paramTokens;
    }

    public void setParamTokens(List<Token> paramTokens) {
        this.paramTokens = paramTokens;
    }


    public MethodToken(MethodCalledNode methodCalledNode, int deepLevel) {
        super();
        this.methodName = methodCalledNode.getMethodName();
        this.deepLevel = deepLevel;
        for (StatementNode statementNode : methodCalledNode.getAgurements()) {
            TypeToken argSketch = new TypeToken(this.deepLevel + 1);
            if (statementNode instanceof MethodInvocationStmNode) {
                argSketch.setNodeType(((MethodInvocationStmNode) statementNode).getMethodType());
            } else {
                argSketch.setNodeType(statementNode.getType());
            }
            this.paramTokens.add(argSketch);
        }
    }

    public MethodToken(ClassInstanceCreationNode classInstanceCreationNode, int deepLevel) {
        super();
        this.methodName = "new " + classInstanceCreationNode.getName() ;
        this.deepLevel = deepLevel;
        for (StatementNode statementNode : classInstanceCreationNode.getArgs()) {
            TypeToken argToken = new TypeToken(this.deepLevel + 1);
            if (statementNode instanceof MethodInvocationStmNode) {
                argToken.setNodeType(((MethodInvocationStmNode) statementNode).getMethodType());
            } else {
                argToken.setNodeType(statementNode.getType());
            }
            this.paramTokens.add(argToken);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(methodName).append("(");
        for (int i = 0; i < paramTokens.size(); i++) {
            Token param = paramTokens.get(i);
            builder.append(param.toString());
            if (i < paramTokens.size() - 1) builder.append(",");
        }
        builder.append(")");
        return builder.toString();
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

}
