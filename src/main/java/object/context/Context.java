package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.token.MethodCalledNode;

public class Context {

    //is find sameMethod or Not
    public Boolean findSameMethod = null;
    public Boolean findSameLine = null;
    public float distance = -1f; //diff between 2 strings
    public NodeInstance fixInstance;
    public  int bugLine;
    public String methodBug;
    public String methodFind;
    public String methodFix;


    public String bugString;
    public String fixString;

    public String bugType;
    public String fixType;

    public String bugNode;
    public String fixNode;
    public Scope scope;

    public String originalBug;
    public String originalFix;

    public String bugNode_fixNode;
    public float similar_score;
    public String context;

    public Boolean find = null;


    public enum Scope {
        ONLY_CURRENT, ALL_AFTER
    }


    public NodeInstance bugInstance;

    public StatementNode statmentBug;

    public String pathBugFile;

    public String type;

    public void setOriginalString (StatementNode stmbug, StatementNode stmFix) {
        this.originalBug = stmbug.toString();
        this.originalFix = stmFix.toString();
        if (stmbug instanceof MethodCalledNode) {
            originalBug = stmbug.toString();
        }
        if (stmFix instanceof MethodCalledNode) {
            originalFix = stmFix.toString();

        }
    }

}
