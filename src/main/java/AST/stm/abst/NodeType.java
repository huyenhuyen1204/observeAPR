package AST.stm.abst;

/**
 * This token can fix Bug
 */
public enum NodeType {
    BaseVariableNode, BooleanNode, MethodCalledNode, QualifiedNameNode,
    ArrayAccessNode,ArrayCreationNode,AssignmentNode, ClassInstanceCreationNode, ExpressionNode,
    IfStmNode, ConstructorInvocationNode, NumbericNode, StringNode, UndefinedNode,
    // just getOperator
    MethodInvocationStmNode, InfixExpressionStmNode
}
