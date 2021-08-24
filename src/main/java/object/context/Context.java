package object.context;

import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;

public class Context {

    //is find sameMethod or Not
    public Boolean findSameMethod = false;
    public Boolean findSameLine = false;
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


    public NodeInstance bugInstance;

    public StatementNode statmentBug;

    public String pathBugFile;

    public String type;

}
