package AST.stm.nodetype;//package AST.stm.nodetype;
//
//import AST.obj.Position;
//import AST.stm.abst.StatementNode;
//import AST.stm.abst.NodeType;
//import AST.stm.token.Token;
//import org.eclipse.jdt.core.dom.ASTNode;
//import util.ASTHelper;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ConstructorInvocationNode extends StatementNode implements Token {
//    private String methodType;
//    private String methodName;
//    private List<StatementNode> agurementTypes = null;
//
//    public ConstructorInvocationNode(String methodName, List<StatementNode> agurementTypes, ASTNode astNode, int line) {
//        super();
//        this.methodName = methodName;
//        this.agurementTypes = new ArrayList<>();
//        if (agurementTypes.size() > 0) {
//            this.agurementTypes.addAll(agurementTypes);
//        }
//        Position position = ASTHelper.getPosition(astNode);
//        this.startPostion = position.getStartPos();
//        this.endPostion = position.getEndPos();
//        this.statementString = astNode.toString();
//        this.line = line;
//        //set child
//        this.children = new ArrayList<>();
////        this.nodeType = astNode.getNodeType();
//        this.nodeType = NodeType.ConstructorInvocationNode;
//    }
//
//
//    public int getStartPostion() {
//        return startPostion;
//    }
//
//    public void setStartPostion(int startPostion) {
//        this.startPostion = startPostion;
//    }
//
//    public void setEndPostion(int endPostion) {
//        this.endPostion = endPostion;
//    }
//
//
//    public String getMethodName() {
//        return methodName;
//    }
//
//    public void setMethodName(String methodName) {
//        this.methodName = methodName;
//    }
//
//    public List<StatementNode> getAguments() {
//        return agurementTypes;
//    }
//
//    public void setAgurementTypes(List<StatementNode> agurementTypes) {
//        this.agurementTypes = agurementTypes;
//    }
//
//    public String getMethodType() {
//        return methodType;
//    }
//
//    public void setMethodType(String methodType) {
//        this.methodType = methodType;
//    }
//
//    @Override
//    public NodeType getObject() {
//       return NodeType.ConstructorInvocationNode;
//    }
//
//    @Override
//    public int getHashCode() {
//        return (methodName + methodType).hashCode();
//    }
//}
