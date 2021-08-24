package AST.stm.token;

import AST.obj.Position;
import AST.stm.abst.NodeType;
import AST.stm.abst.StatementNode;
import org.eclipse.jdt.core.dom.ASTNode;
import util.ASTHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class  MethodInvocationStmNode extends StatementNode implements Token {
    public static final String type = "MethodInvocation";
    private String methodType;
//    private String keyVar;
    private BaseVariableNode baseVar;
//    private String methodCalled = null; // eg: student.getname() - method called is getname
    //for list method call eg: customerList.get(0).toString()
    // methods called: get, tostring
    private List<StatementNode> methodsCalled = null; //customerList.get(0).toString() -> toString, get

    public MethodInvocationStmNode(ASTNode methodInvocation, int line) {
        this.methodsCalled = new ArrayList<>();
        this.line = line;
        this.statementString = methodInvocation.toString();
        Position position = ASTHelper.getPosition(methodInvocation);
        this.startPostion = position.getStartPos();
        this.endPostion = position.getEndPos();
        //set child
        this.children = new ArrayList<>();
//        this.nodeType = methodInvocation.getNodeType();
        this.nodeType = NodeType.MethodInvocationStmNode;
    }

    public void setChild() {
        Collections.reverse(methodsCalled);
        if (this.methodType == null) {
            if (methodsCalled.get(methodsCalled.size() - 1) instanceof Token) {
                this.setMethodType((methodsCalled.get(methodsCalled.size() - 1)).getType());
            }
        } else {
            if (methodsCalled.get(methodsCalled.size() - 1) instanceof Token) {
                if ((methodsCalled.get(methodsCalled.size() - 1)).getType() == null) {
                    ( methodsCalled.get(methodsCalled.size() - 1)).setType(this.methodType);
                }
            }
        }
        //set Child
         if (baseVar == null) { //is super
             BaseVariableNode baseVariableNode = new BaseVariableNode((String) null, null, this.line);
             this.baseVar = baseVariableNode;
         } else {
            baseVar.setParent(this);
        }
        this.children.add(baseVar);
        StatementNode parentStmNode = baseVar;
        for (StatementNode mt : methodsCalled) {
            //set Child
            if (parentStmNode != null) {
                parentStmNode.setChildren(mt);
            }
            //setParent
            if (mt != null) {
                mt.setParent(parentStmNode);
            }
            parentStmNode = mt;
        }
    }

    public void addMethodCall(MethodCalledNode methodCalledNode) {
        this.methodsCalled.add(methodCalledNode);
        //final methodCall is type Of methodCall
//        this.methodType = ((MethodCalledNode)this.methodsCalled.get(this.getMethodsCalled().size() -1)).getMethodType();
    }

    public List<StatementNode> getMethodsCalled() {
        return methodsCalled;
    }



    public BaseVariableNode getBaseVar() {
        return baseVar;
    }

    public void setBaseVar(BaseVariableNode baseVar) {
        this.baseVar = baseVar;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }


    @Override
    public NodeType getObject() {
        return NodeType.MethodInvocationStmNode;
    }

    @Override
    public String getType() {
        return this.methodType;
    }

    @Override
    public void setType(String type) {
        if (methodType == null) {
            this.methodType = type;
        }
        if (methodsCalled.size() > 0) {
            if (methodsCalled.get(methodsCalled.size() - 1) instanceof Token) {
                if (( methodsCalled.get(methodsCalled.size() - 1)).getType() == null) {
                    ( methodsCalled.get(methodsCalled.size() - 1)).setType(this.methodType);
                }
            }
        }
    }

    @Override
    public int getHashCode() {
        String hash = "";
        if (baseVar != null) {
           hash = baseVar.getType();
        }
        for (StatementNode stm: methodsCalled) {
            if (stm instanceof Token) {
                hash += ((Token) stm).getHashCode();
            }
        }

        return hash.hashCode();
    }



    public void addChild(MethodCalledNode methodCalledNode) {
        this.children.add(methodCalledNode);
    }


}
