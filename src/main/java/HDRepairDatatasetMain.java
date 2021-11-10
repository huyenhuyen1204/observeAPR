import AST.node.ClassNode;
import AST.node.MethodNode;
import AST.parser.JavaFileParser;
import AST.stm.abst.NodeInstance;
import AST.stm.abst.StatementNode;
import AST.stm.node.AssignmentNode;
import AST.stm.node.ExpressionNode;
import AST.stm.node.IfStmNode;
import AST.stm.nodetype.*;
import AST.stm.token.*;
import object.Behavior;
import object.TokenExist;
import object.context.Context;
import object.context.NodeReplacement;
import object.context.ElementReplacement;
import org.eclipse.jgit.diff.*;
import util.FileHelper;
import util.ReflectionHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//target MEthodINvo, CreatClass -> ReplaceALL
//Qualifier, baseVar -> qualifier, BaseVar -> replaceAll
//TODO: observe NODE REPLACE & create context => Algorithm
//HOW about PARAM???? METHOD INVO or BASEVAR ...
public class HDRepairDatatasetMain {
    private static String pathToResult = "/home/huyenhuyen/obseve/out/hd";

    public static List<Context> contexts;
    private static ClassNode classNodeBug;
    private static ClassNode classNodeFixed;
    private static String pathBugFile;
    private static String classBug = "";
    private static String classFix = "";
    public static List<Integer> counts;
    public static String list;
    public static boolean isSameMethodInvo = false;
    List<ClassNode> classNodeListOld;

    //    //
    public static void main(String[] args) throws Exception {
        String folderBug = "/home/huyenhuyen/Desktop/data-bugfixes/all";
//        getDiff(new File("/home/huyenhuyen/Desktop/data-bugfixes/all/jayway_rest-assured/modifiedFiles/2/old/RequestSpecBuilder.java"),
//                new File("/home/huyenhuyen/Desktop/data-bugfixes/all/jayway_rest-assured/modifiedFiles/2/fix/RequestSpecBuilder.java"));
        contexts = new ArrayList<>();
        counts = new ArrayList<>();
        HDRepairDatatasetMain hdRepairDatatasetMain = new HDRepairDatatasetMain("/home/huyenhuyen/obseve/out/hd");
        List<String> paths = new ArrayList<>();
        parserFolder(new File(folderBug), paths);
        List<String> oldPathAndFixPath = parserFolderDiff(paths);
        hdRepairDatatasetMain.exportDiffTree(oldPathAndFixPath);
        hdRepairDatatasetMain.writeToXlSX(contexts, "HDRepair");
        FileHelper.outputToFile(new File("out/hd/bugs.txt"), hdRepairDatatasetMain.list, false);
        System.out.println(counts.toString());
    }

    HDRepairDatatasetMain(String outputFile) {
        pathToResult = outputFile;
        contexts = new ArrayList<>();
        counts = new ArrayList<>();
    }


    public void writeToXlSX(List<Context> contexts, String fileName) {

        Map<String, Object[]> nodes = new TreeMap<String, Object[]>();
        int idNodes = 0;
        nodes.put(String.valueOf((idNodes++)), new Object[]{
//                "STT",
//                "bugType",
//                "fixType",
                "findSameMethod",
                "bugNode",
                "fixNode",
                "bugNode_fixNode",
                "context",
                "bugInstance",
                "scope",
//                "fixInstance",
                "bugString",
                "fixString",
                "originalBug",
                "originalFix",
//                "statmentBug",
                "pathBugFile",
                "line"});
        for (Context context : contexts) {
//            if (context instanceof ElementReplacement) {
//                elements.put(String.valueOf(idNodes++), new Object[]{
//                        String.valueOf(idNodes++),
//                        context.distance,
//                        context.findSameMethod,
//                        context.methodBug,
//                        context.methodFix,
//                        context.methodFind,
//                        context.bugString,
//                        context.fixString,
//                        context.bugInstance,
//                        ((ElementReplacement) context).fixInstance,
//                        context.statmentBug,
//                        context.pathBugFile});
            nodes.put(String.valueOf(idNodes++), new Object[]{
//                    String.valueOf(idNodes++),
//                    context.bugType,
//                    context.fixType,
                    context.findSameMethod,
                    context.bugNode,
                    context.fixNode,
                    context.bugNode_fixNode + (context.findSameMethod != null ?
                            "&" + context.findSameMethod : "") ,
                    context.bugNode_fixNode + (context.findSameMethod != null ?
                            "&" + context.findSameMethod : "") + "&" + context.bugInstance,
                    context.bugInstance,
                    context.scope.toString(),
//                    context.fixInstance,
                    context.bugString,
                    context.fixString,
                    context.originalBug,
                    context.originalFix,
//                "statmentBug",
                    context.pathBugFile,
                    context.bugLine});
//            }
        }
//        FileHelper.writeToXlsx(elements, "element", pathToResult + "/result_element.xlsx");
        FileHelper.writeToXlsx(nodes, "node replace", pathToResult + File.separator + fileName +".xlsx");
    }

    private void exportDiffTree(List<String> oldPathAndFixPath) throws Exception {
        int count = 0;
        for (String path : oldPathAndFixPath) {
            String[] paths = path.split("\t");
            File old = new File(paths[0]);
            File fix = new File(paths[1]);
            File[] oldJavaFiles = old.listFiles();
            File[] fixJavaFiles = fix.listFiles();
            if (oldJavaFiles.length == fixJavaFiles.length) {
                for (int i = 0; i < oldJavaFiles.length; i++) {
                    if (oldJavaFiles[i] == null) {
//                    System.out.println(oldJavaFiles[i]);
                    } else if (fixJavaFiles[i] == null) {
//                    System.out.println(fixJavaFiles[i]);
                    } else {
                        getDiff(oldJavaFiles[i], fixJavaFiles[i], String.valueOf(count++));
                    }
                }
            } else {
                System.out.println("DIFF");
            }
        }
    }

    public static List<String> parserFolderDiff(List<String> paths) {
        List<String> oldPathAndFixPath = new ArrayList<>();
        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                File[] childs = file.listFiles();
                // number modify
                for (File c : childs) {
                    if (c.isDirectory()) {
                        File[] diffs = c.listFiles();
                        String oldPath = "";
                        String fixPath = "";
                        for (File d : diffs) {
                            if (d.isDirectory() && d.getName().equals("old")) {
                                oldPath = d.getAbsolutePath();
                            } else if (d.isDirectory() && d.getName().equals("fix")) {
                                fixPath = d.getAbsolutePath();
                            }
                        }
                        if (!oldPath.equals("") && !fixPath.equals("")) {
                            oldPathAndFixPath.add(oldPath + "\t" + fixPath);
                        }
                    }
                }
            }
        }
        return oldPathAndFixPath;
    }

    private static void parserFolder(File file, List<String> paths) {
        if (file.exists() && file.isDirectory()) {
            if (file.getName().equals("modifiedFiles")) {
                paths.add(file.getAbsolutePath());
            } else {
                File[] files = file.listFiles();
                for (File f : files) {
                    parserFolder(f, paths);
                }
            }
        }
    }

    /**
     * get diff of source code
     *
     * @param buggyFile:      file error of student < 100
     * @param fixedFile:      file pass all test of student = 100
     * @param pathOutBehavior
     * @return
     */
    private static EditList getDiff(File buggyFile, File fixedFile, String pathOutBehavior, String folderName) {
        OutputStream out = new ByteArrayOutputStream();
        EditList diffList = new EditList();
        try {
            RawText rt1 = new RawText(buggyFile);
            RawText rt2 = new RawText(fixedFile);
            diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, rt1, rt2));
            new DiffFormatter(out).format(diffList, rt1, rt2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String edit = "";
        if (diffList != null) {
            for (int i = 0; i < diffList.size(); i++) {
                if (diffList.get(i) != null) {
                    edit += diffList.get(i).toString() + "\n";
                }
            }
        }

//        System.out.println(out);
        // Save diff to File
        if (diffList.size() > 0) {
            String classname = buggyFile.getName().replace(".java", "");
            String pathOutput = pathOutBehavior + File.separator + folderName;
            File fileOut = new File(pathOutput + File.separator + classname + ".txt");
            list += "-------------\n" + out.toString() + "---------------\n";
            FileHelper.createFile(fileOut, out.toString());
            FileHelper.createFile(new File(pathOutput + File.separator + "behavior" + "_" + classname + ".txt"), edit);
        }
        return diffList;
    }

    public void getDiff(File oldFile, File fixFile, String id) throws Exception {
        String old = FileHelper.readFile(oldFile);
        String fix = FileHelper.readFile(fixFile);
        classBug = FileHelper.readFile(oldFile);
        pathBugFile = oldFile.getAbsolutePath();
        classFix = FileHelper.readFile(fixFile);

        EditList diff = getDiff(oldFile, fixFile, "/home/huyenhuyen/obseve/tmp", id);
        List<Behavior> behaviors = new ArrayList<>();
        for (Edit edit : diff) {
            if (edit.getType() == Edit.Type.REPLACE) {
                Behavior behavior = new Behavior(Behavior.Type.REPLACE, edit.getBeginA(), edit.getEndA(),
                        edit.getBeginB(), edit.getEndB());
                behaviors.add(behavior);
            }
        }
        classNodeListOld = JavaFileParser.parse(oldFile, old);
        List<ClassNode> classNodeListFix = JavaFileParser.parse(fixFile, fix);

        if (classNodeListOld.size() == classNodeListFix.size()) {
            for (int c = 0; c < classNodeListFix.size(); c++) {
                ClassNode classNodeBugtmp = classNodeListOld.get(c);
                ClassNode classNodeFixtmp = classNodeListFix.get(c);
                for (Behavior behavior : behaviors) {
                    if (behavior.getBeginA() >= classNodeBugtmp.getStartLine()
                            && behavior.getEndA() <= classNodeBugtmp.getEndLine()
                            && behavior.getBeginB() >= classNodeFixtmp.getStartLine()
                            && behavior.getEndA() <= classNodeFixtmp.getEndLine()) {
                        classNodeBug = classNodeBugtmp;
                        classNodeFixed = classNodeFixtmp;
                        List<StatementNode> statementNodesA = new ArrayList<>();
                        for (int i = behavior.getBeginA(); i <= behavior.getEndA(); i++) {
                            StatementNode stm = classNodeBug.findStatemmentByLine(classNodeBug.getChildren(), i);
                            if (stm != null) {
                                statementNodesA.add(stm);
                            }
                        }
                        List<StatementNode> statementNodesB = new ArrayList<>();

                        for (int i = behavior.getBeginB(); i <= behavior.getEndB(); i++) {
                            StatementNode stm = classNodeFixed.findStatemmentByLine(classNodeFixed.getChildren(), i);
                            if (stm != null) {
                                statementNodesB.add(stm);
                            }
                        }
                        if (statementNodesA.size() == statementNodesB.size()) {
                            if (statementNodesA.size() > 0) {
                                observe(statementNodesA, statementNodesB);
                            }
                            counts.add(statementNodesA.size());
                        } else {
                            //insert
                            System.out.println("ERROR: statementNodesA.size() != statementNodesB.size()");
                        }
                    }
                }
            }
        }
    }

    private void observe(List<StatementNode> statementNodesA, List<StatementNode> statementNodesB) {
        for (int i = 0; i < statementNodesA.size(); i++) {
            StatementNode stmBug = statementNodesA.get(i);
            StatementNode stmFix = statementNodesB.get(i);
            compareStatement(stmBug, stmFix, stmBug, stmFix);
        }
    }

    private List<StatementNode> getChildren(StatementNode methodInvo) {
        List<StatementNode> statementNodes = new ArrayList<>();
        if (methodInvo.getChildren().size() > 0) {
            for (StatementNode stm : methodInvo.getChildren()) {
                statementNodes.add(stm);
                List<StatementNode> statementNodes1 = getChildren(stm);
                statementNodes.addAll(statementNodes1);
            }

        }
        return statementNodes;
    }

    private void compareStatement(StatementNode stmBug, StatementNode stmFix, StatementNode stmBugg, StatementNode stmFixx) {
        boolean add = true;
        if (stmBug.getClass() == stmFix.getClass()) {
            if (stmFix instanceof MethodInvocationStmNode) {
                isSameMethodInvo = false;
                if (stmBug.getChildren().size() > 0 && stmFix.getChildren().size() > 0) {
                    List<Context> elms = compareMethodInvocation(stmBug.getChildren().get(0), stmFix.getChildren().get(0), stmBugg, stmFixx);
                    if (elms.size() > 0 || !isSameMethodInvo) {
                        contexts.addAll(elms);
                    } else if (elms.size() == 0 && !isSameMethodInvo) {
                        NodeReplacement nodeReplacement = setNodeAllAfter(stmBug, stmFix, stmBugg, stmFixx);
                        contexts.add(nodeReplacement);
                    }
                } else {
                    NodeReplacement nodeReplacement = setNodeAllAfter(stmBug, stmFix, stmBugg, stmFixx);
                    contexts.add(nodeReplacement);
                }
                System.out.println(stmBug.toString() + " -> " + stmFix.toString());
            } else if (stmFix instanceof BaseVariableNode) {
                if (((BaseVariableNode) stmBug).getKeyVar() != null && ((BaseVariableNode) stmFix).getKeyVar() != null) {
                    if (!((BaseVariableNode) stmBug).getKeyVar().equals(((BaseVariableNode) stmFix).getKeyVar())) {
                        //find base var  in method, in class
                        findBaseVar(((BaseVariableNode) stmBug), ((BaseVariableNode) stmFix), stmBugg, stmFixx);
                    }
                } else if (((BaseVariableNode) stmBug).getKeyVar() == null &&
                        ((BaseVariableNode) stmFix).getKeyVar() != null) {
                    System.out.println("Chuwa xu ly 1 ===>" + stmBug.getParent().toString()
                            + " => " + stmFix.getParent().toString());
//                    NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, null, null);
//                    contexts.add(nodeReplacement);
                } else if (((BaseVariableNode) stmBug).getKeyVar() != null &&
                        ((BaseVariableNode) stmFix).getKeyVar() == null) {
                    System.out.println("Chua xu lys 2" + stmBug.getParent().toString() + " => " + stmFix);
                }
            } else if (stmFix instanceof QualifiedNameNode) {
                add = false;
                //if (qualifierName != null)
                if (!stmBug.toString().equals(stmFix.toString())) {
                    ElementReplacement elementReplacement = setElementAllAfter(stmBug, stmFix, stmBugg, stmFixx);
                    contexts.add(elementReplacement);
                }
            } else if (stmFix instanceof ExpressionNode) {
                //has children
            } else if (stmFix instanceof AssignmentNode) {
                //has children
            } else if (stmFix instanceof ClassInstanceCreationNode) {
                //TODO:
                add = false;
                setClassInstanceCreationNode(stmBug, stmFix, stmBugg, stmFixx);
            } else if (stmFix instanceof BooleanNode) {
                add = false;
                if (!stmFix.toString().equals(stmBug.toString())) {
                    ElementReplacement elementReplacement = setElementOnlyCurrent(stmBug, stmFix, stmBugg, stmFixx);
                    contexts.add(elementReplacement);
                }
            } else if (stmFix instanceof IfStmNode) {
                System.out.println();
                //has children
//                if (((IfStmNode) stmBug))
            } else if (stmFix instanceof InfixExpressionStmNode) {
                InfixExpressionStmNode infixFix = (InfixExpressionStmNode) stmFix;
                InfixExpressionStmNode infixBug = (InfixExpressionStmNode) stmBug;
                if (!infixFix.getOperator().getOperator()
                        .equals(infixBug.getOperator().getOperator())) {
                    if (infixFix.toString().replace(infixFix.getOperator().getOperator(), "")
                            .equals(infixBug.toString().replace(infixBug.getOperator().getOperator(), ""))) {
                        ElementReplacement elementReplacement = setElementOnlyCurrent(infixFix.getOperator(), infixBug.getOperator(), stmBugg, stmFixx);
                        contexts.add(elementReplacement);
                    }
                }
            } else {
                System.out.println("Chua xu ly");
            }
            if (add) {
                if (stmBug.getChildren().size() > 0 && stmFix.getChildren().size() > 0) {
                    if (stmBug.getChildren().size() == stmFix.getChildren().size())
                        observe(stmBug.getChildren(), stmFix.getChildren());
                }
            }
        } else { //==================== BugNode != FixNode ===============
            if (stmBug instanceof BaseVariableNode) {
                contexts.add(setNodeOnlyCurrent(stmBug, stmFix, stmBugg, stmFixx));
            } else if (stmBug instanceof QualifiedNameNode
                    && stmFix instanceof MethodInvocationStmNode) {
                isSameMethodInvo = false;
                List<Context> changes = compareMethodInvocation(stmBug, stmFix.getChildren().get(0), stmBugg, stmFixx);
                if (isSameMethodInvo) {
                    contexts.addAll(changes);
                }
                add = false;
            } else if (stmFix instanceof QualifiedNameNode
                    && stmBug instanceof MethodInvocationStmNode) {
                isSameMethodInvo = false;
                List<Context> changes = compareMethodInvocation(stmBug.getChildren().get(0), stmFix, stmBugg, stmFixx);
                if (isSameMethodInvo) {
                    contexts.addAll(changes);
                }
                add = false;
            } else if (stmBug instanceof MethodInvocationStmNode) {
                if (stmBug.getChildren().get(0).getChildren() != null) { // > 2 child
                    NodeReplacement nodeReplacement = setNodeAllAfter(stmBug, stmFix, stmBugg, stmFixx);
                    contexts.add(nodeReplacement);
                } else {
                    NodeReplacement nodeReplacement = setNodeOnlyCurrent(stmBug, stmFix, stmBugg, stmFixx);
                    contexts.add(nodeReplacement);
                }
                add = false;
            } else if (stmBug instanceof InfixExpressionStmNode) {
                if (stmBug.toString().replace(((InfixExpressionStmNode) stmBug).getOperator().getOperator(), "")
                        .equals(stmFix.toString())) {
                    ElementReplacement elementReplacement = setElementOnlyCurrent(((InfixExpressionStmNode) stmBug).getOperator(),
                            null, stmBugg, stmFixx);
                    contexts.add(elementReplacement);
                } else {
                    NodeReplacement nodeReplacement = setNodeAllAfter(stmBug, stmFix, stmBugg, stmFixx);
                    contexts.add(nodeReplacement);
                }
            } else if (stmFix instanceof InfixExpressionStmNode) {
                if (stmFix.toString().replace(((InfixExpressionStmNode) stmFix).getOperator().getOperator(), "")
                        .equals(stmBug.toString())) {
                    ElementReplacement elementReplacement = setElementOnlyCurrent(null, ((InfixExpressionStmNode) stmFix).getOperator(), stmBugg, stmFixx);
                    contexts.add(elementReplacement);
                } else {
                    NodeReplacement nodeReplacement = setNodeAllAfter(stmBug, stmFix, stmBugg, stmFixx);
                    contexts.add(nodeReplacement);
                }
            } else if (stmFix instanceof BaseVariableNode) {
                findBaseVar(stmBug, (BaseVariableNode) stmFix, stmBugg, stmFixx);
            } else {
                NodeReplacement nodeReplacement = setNodeAllAfter(stmBug, stmFix, stmBugg, stmFixx);
                contexts.add(nodeReplacement);
            }
            if (add) {
                if (stmBug.getChildren().size() > 0 && stmFix.getChildren().size() > 0) {
                    if (stmBug.getChildren().size() == stmFix.getChildren().size())
                        observe(stmBug.getChildren(), stmFix.getChildren());
                }
            }
        }
    }

    private void setClassInstanceCreationNode(StatementNode bug, StatementNode fixed, StatementNode bugg, StatementNode fixedd) {
        boolean isDiffMethod = false;
        if (!((ClassInstanceCreationNode) bug).getName()
                .equals(((ClassInstanceCreationNode) fixed).getName())) {
            //diffMethod
            isDiffMethod = true;
        } else {
            if (((ClassInstanceCreationNode) bug).getArgs().size() != ((ClassInstanceCreationNode) fixed).getArgs().size()) {
                //diffMethod
                isDiffMethod = true;
            } else if (((ClassInstanceCreationNode) bug).getArgs().size() == ((ClassInstanceCreationNode) fixed).getArgs().size()) {
                isSameMethodInvo = true;
                for (int i = 0; i < ((ClassInstanceCreationNode) bug).getArgs().size(); i++) {
                    compareStatement(((ClassInstanceCreationNode) bug).getArgs().get(i),
                            ((ClassInstanceCreationNode) fixed).getArgs().get(i), bugg, fixedd);
                }
            }
        }
        if (isDiffMethod) {
            ElementReplacement elementReplacement = setElementOnlyCurrent(bug, fixed, bugg, fixedd);
            contexts.add(elementReplacement);
        }
    }

    private NodeReplacement setNodeOnlyCurrent(StatementNode stmBug, StatementNode stmFix,
                                               StatementNode stmBugg, StatementNode stmFixx) {
        if (stmFix instanceof MethodCalledNode) {
            if (stmFix.getParent() instanceof MethodInvocationStmNode) {
                //only one child -> is MethodInvocation
                if (((MethodInvocationStmNode) stmFix.getParent()).getNodes().size()
                == 1) {
                    NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix.getParent(), null, null, null,
                            Context.Scope.ONLY_CURRENT);
                    nodeReplacement.setOriginalString(stmBugg, stmFixx);
                    nodeReplacement.pathBugFile = pathBugFile;
                    return nodeReplacement;
                }
            }
        }

        NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, null, null, null,
                Context.Scope.ONLY_CURRENT);
        nodeReplacement.setOriginalString(stmBugg, stmFixx);
        nodeReplacement.pathBugFile = pathBugFile;

        return nodeReplacement;
    }

    private ElementReplacement setElementOnlyCurrent(StatementNode stmBug, StatementNode stmFix,
                                                     StatementNode stmBugg, StatementNode stmFixx) {
        ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix, null,
                null, null, Context.Scope.ONLY_CURRENT);
        elementReplacement.setOriginalString(stmBugg, stmFixx);
        elementReplacement.pathBugFile = pathBugFile;
        return elementReplacement;
    }

    private NodeReplacement setNodeAllAfter(StatementNode stmBug, StatementNode stmFix,
                                            StatementNode stmBugg, StatementNode stmFixx) {
        NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, null, null, null,
                Context.Scope.ALL_AFTER);
        nodeReplacement.pathBugFile = pathBugFile;
        nodeReplacement.setOriginalString(stmBugg, stmFixx);
        return nodeReplacement;
    }

    private ElementReplacement setElementAllAfter(StatementNode stmBug, StatementNode stmFix,
                                                  StatementNode stmBugg, StatementNode stmFixx) {
        ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix, null,
                null, null, Context.Scope.ALL_AFTER);
        elementReplacement.setOriginalString(stmBugg, stmFixx);
        elementReplacement.pathBugFile = pathBugFile;
        return elementReplacement;
    }

    private void compareStatementOld(StatementNode stmBug, StatementNode stmFix) {
        MethodNode methodNodeBug = classNodeBug.findMethodNodeByStmLine(stmBug.getLine());
        boolean add = true;
        //TOKEN SAME TYPE
        if (stmBug.getClass() == stmFix.getClass()) {
            if (stmFix instanceof MethodInvocationStmNode) {
                System.out.println(stmBug.toString() + " -> " + stmFix.toString());
                //Compare two tree
//                //has children
//                List<StatementNode> childreBug = getChildren(stmBug);
//                List<StatementNode> childreFix = getChildren(stmFix);
//                if (childreBug.size() == childreFix.size()) {
//                    if (((Token)childreBug.get(0)).getHashCode() == ((Token)childreFix.get(0)).getHashCode()) {
//                        add = true;
//                    } else {
//                        NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix,
//                                null, null, null, NodeReplacement.Scope.ALL_AFTER);
//                        nodeReplacement.pathBugFile = pathBugFile;
//                        contexts.add(nodeReplacement);
//                        add = false;
//                    }
//                } else {
//                    NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix,
//                            null, null, null, NodeReplacement.Scope.ALL_AFTER);
//                    nodeReplacement.pathBugFile = pathBugFile;
//                    contexts.add(nodeReplacement);
//                    add = false;
//                }
            } else if (stmFix instanceof BaseVariableNode) {
                add = false;
                if (((BaseVariableNode) stmBug).getKeyVar() != null && ((BaseVariableNode) stmFix).getKeyVar() != null) {
                    if (!((BaseVariableNode) stmBug).getKeyVar().equals(((BaseVariableNode) stmFix).getKeyVar())) {
                        //find base var  in method, in class
                        findBaseVar(((BaseVariableNode) stmBug), ((BaseVariableNode) stmFix), stmBug, stmFix);
                    }
                } else if (((BaseVariableNode) stmBug).getKeyVar() == null &&
                        ((BaseVariableNode) stmFix).getKeyVar() != null) {
                    System.out.println("Chuwa xu ly 1 ===>" + stmBug.getParent().toString()
                            + " => " + stmFix.getParent().toString());
//                    NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, null, null);
//                    contexts.add(nodeReplacement);
                } else if (((BaseVariableNode) stmBug).getKeyVar() != null &&
                        ((BaseVariableNode) stmFix).getKeyVar() == null) {
                    System.out.println("Chua xu lys 2" + stmBug.getParent().toString() + " => " + stmFix);
                }
            } else if (stmFix instanceof MethodCalledNode) {
                MethodCalledNode methodBug = (MethodCalledNode) stmBug;
                MethodCalledNode methodFix = (MethodCalledNode) stmFix;

                if (!methodBug.getMethodName().equals(methodFix.getMethodName())) {
                    //method: diff name => find fixed in source
                    findElement(stmBug, stmFix, methodNodeBug, classNodeBug);
                    // === end here==
                } else {
                    //compare params
                    observeParams(methodBug.getAgurements(), methodFix.getAgurements(), stmBug, methodFix, classNodeBug);
                }
            } else if (stmFix instanceof QualifiedNameNode) {
                add = false;
                //if (qualifierName != null)
                if (!stmBug.toString().equals(stmFix.toString())) {
                    ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix, null,
                            null, null, Context.Scope.ALL_AFTER);
                    contexts.add(elementReplacement);
                    elementReplacement.pathBugFile = pathBugFile;
//                    if (!tokenExist.isSameMethod) {
//                        MethodNode methodNodeFind = classNodeBug.findMethodNodeByStmLine(tokenExist.line);
//                        elementReplacement.methodFind = classBug.substring(methodNodeFind.getStartPosition(), methodNodeFind.getEndPosition());
//                    }
//                    findElement(stmBug, stmFix, methodNodeBug, classNodeBug);
                }
//                QualifiedNameNode qualifiedNameNodeBug = (QualifiedNameNode) stmBug;
//                QualifiedNameNode qualifiedNameNodeFix = (QualifiedNameNode) stmFix;
//                if (!qualifiedNameNodeBug.toString()
//                        .equals(qualifiedNameNodeFix.toString())) {
//                    TokenExist tokenExist = findSameMethodNode(qualifiedNameNodeFix, methodNodeBug, classNodeBug);
//                    ElementReplacement elementReplacement;
//                    if (tokenExist.line != -1 ) {
//                        elementReplacement = new ElementReplacement(stmBug,stmFix, null, tokenExist.isSameMethod);
//                    } else {
//                        //not found
//                        elementReplacement = new ElementReplacement(stmBug, stmFix, null, null);
//                    }
//
//                    elementReplacement.pathBugFile = pathBugFile;
//                    contexts.add(elementReplacement);
//                }
            } else if (stmFix instanceof ExpressionNode) {
                //has children
            } else if (stmFix instanceof AssignmentNode) {
                //has children
            } else if (stmFix instanceof ClassInstanceCreationNode) {
                System.out.println(stmBug.toString() + " -> " + stmFix.toString());
                //compare two tree
//                if (((ClassInstanceCreationNode) stmBug).getFullyQualifiedClassName()
//                        .equals(((ClassInstanceCreationNode) stmFix).getFullyQualifiedClassName())) {
//                    //same name: compare params
//                    observeParams(((ClassInstanceCreationNode) stmBug).getArgs(), ((ClassInstanceCreationNode) stmFix).getArgs(), stmBug, (ClassInstanceCreationNode) stmFix, classNodeBug);
//                } else {
//                    // diff node, diff params (CREATE PARAM)
//
//                    System.out.println("Chua xu ly: diff node, diff params");
//                }
            } else if (stmFix instanceof BooleanNode) {
                add = false;
                if (!stmFix.toString().equals(stmBug.toString())) {
                    ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix, null,
                            null, null, Context.Scope.ONLY_CURRENT);
                    contexts.add(elementReplacement);
                    elementReplacement.pathBugFile = pathBugFile;
//                    findElement(stmBug, stmFix, methodNodeBug, classNodeBug);
//                    TokenExist tokenExist = findSameMethodNode(stmFix, methodNodeBug, classNodeBug);
//                    if (tokenExist != -1)
//                    ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmBug.toString(), stmFix.toString(), null
//                            , stmBug.getNodeInstance(), null, stmFix);
//                    elementReplacement.pathBugFile = pathBugFile;
//                    contexts.add(elementReplacement);
                }
//                System.out.println("Chua xu ly BooleanNode");
            }
//            else if (stmFix instanceof ConstructorInvocationNode) {
//                if (!((ConstructorInvocationNode) stmBug).getMethodName().equals(((ConstructorInvocationNode) stmFix).getMethodName())) {
//                    //find node in Source
//                    System.out.println("Chuwa xu ly: ConstructorInvocationNode");
//                } else {
//                    //compare Params
//                    observeParams(((ConstructorInvocationNode) stmBug).getAguments(),
//                            ((ConstructorInvocationNode) stmFix).getAguments(), stmBug, (ConstructorInvocationNode) stmFix, classNodeBug);
//                }
//            }
            else if (stmFix instanceof IfStmNode) {
                //has children
//                if (((IfStmNode) stmBug))
            } else if (stmFix instanceof InfixExpressionStmNode) {
                //has children
                if (!((InfixExpressionStmNode) stmFix).getOperator()
                        .equals(((InfixExpressionStmNode) stmBug).getOperator())) {
                    ElementReplacement elementReplacement = new ElementReplacement(((InfixExpressionStmNode) stmBug).getOperator()
                            , ((InfixExpressionStmNode) stmFix).getOperator(), null,
                            null, null, Context.Scope.ONLY_CURRENT);
                    contexts.add(elementReplacement);
                    elementReplacement.pathBugFile = pathBugFile;
                }
            } else {
                System.out.println("Quan sat TOKEN REPLACE");
            }
            if (add) {
                if (stmBug.getChildren().size() > 0 && stmFix.getChildren().size() > 0) {
                    if (stmBug.getChildren().size() == stmFix.getChildren().size())
                        observe(stmBug.getChildren(), stmFix.getChildren());
                }
            }
        } else {
            String buggyNode = stmBug.getClass().toString();
            String fixedNode = stmFix.getClass().toString();
            boolean isAdd = true;

            //  =================NODE REPLACEMENT ================
            if (stmBug instanceof InfixExpressionStmNode) {
                List<StatementNode> children = stmBug.getChildren();
                isAdd = false;
                if (children.size() == 1) {
                    compareStatement(children.get(0),
                            stmFix, children.get(0), stmFix);
                }
            } else if (stmFix instanceof InfixExpressionStmNode) {
                List<StatementNode> children = stmFix.getChildren();
                isAdd = false;
                if (children.size() == 1) {
                    compareStatement(stmBug, children.get(0), stmBug, children.get(0));
                } else {
                    isAdd = true;
                }
            } else if ((stmBug instanceof UndefinedNode) || stmFix instanceof UndefinedNode
                    || (stmBug instanceof NumbericNode) || (stmFix instanceof NumbericNode)
                    || (stmBug instanceof StringNode) || (stmFix instanceof StringNode)) {
                isAdd = false;
            } else if (stmFix instanceof MethodInvocationStmNode) {
                //find MethodInvocation
                // found => context around
                // not found => ???  % similar (replace baseVar -> type)
                TokenExist tokenExist = findNodeInClass(stmBug, stmFix, methodNodeBug, classNodeBug);
                if (tokenExist.line != -1) {
                    NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix,
                            tokenExist.statementNode, tokenExist.isSameMethod,
                            tokenExist.isSameLine, Context.Scope.ALL_AFTER);
                    nodeReplacement.methodBug = classBug.substring(methodNodeBug.getStartPosition(),
                            methodNodeBug.getEndPosition());
                    nodeReplacement.pathBugFile = pathBugFile;
                    if (!tokenExist.isSameMethod) {
                        MethodNode methodFind = classNodeBug.findMethodNodeByStmLine(tokenExist.line);
                        nodeReplacement.methodFind = classBug.substring(methodFind.getStartPosition(), methodFind.getEndPosition());
                        nodeReplacement.statementNodeFind = tokenExist.statementNode.toString();
                    }
                    nodeReplacement.pathBugFile = pathBugFile;
                    contexts.add(nodeReplacement);
                    isAdd = false;
                }
                System.out.println("Quan sat MethodInvocationStmNode");
            } else if (stmFix instanceof BaseVariableNode) {
                //find baseVar node
                //=> hard to replace
                findBaseVar(stmBug, (BaseVariableNode) stmFix, stmBug, stmFix);
                isAdd = false;
            } else if (stmFix instanceof BooleanNode) {
                System.out.println(stmBug.toString() + " -> " + stmFix.toString());
//                System.out.println("Quan sat BooleanNode");
            } else if (stmFix instanceof MethodCalledNode) {
                //find in method
//                System.out.println("Quan sat MethodCalledNode");
            } else if (stmFix instanceof QualifiedNameNode) {
                System.out.println(stmBug.toString() + " -> " + stmFix.toString());

                //if == null => find in method
                //else : chua xu ly
                QualifiedNameNode qualifiedFix = (QualifiedNameNode) stmFix;
                TokenExist tokenExist = findNodeInClass(stmBug, qualifiedFix, methodNodeBug, classNodeBug);
                if (tokenExist.line != -1) {
                    NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, tokenExist.statementNode, tokenExist.isSameMethod, tokenExist.isSameLine, Context.Scope.ALL_AFTER);
                    nodeReplacement.methodBug = classBug.substring(methodNodeBug.getStartPosition(),
                            methodNodeBug.getEndPosition());
                    nodeReplacement.findSameMethod = tokenExist.isSameMethod;
                    nodeReplacement.pathBugFile = pathBugFile;
                    if (!tokenExist.isSameMethod) {
                        MethodNode methodFind = classNodeBug.findMethodNodeByStmLine(tokenExist.line);
                        nodeReplacement.methodFind = classBug.substring(methodFind.getStartPosition(), methodFind.getEndPosition());
                        nodeReplacement.statementNodeFind = tokenExist.statementNode.toString();
                    }
                    nodeReplacement.pathBugFile = pathBugFile;
                    contexts.add(nodeReplacement);
                    isAdd = false;
                }
                System.out.println("Quan sat QualifiedNameNode");
            } else if (stmFix instanceof ExpressionNode) {
                System.out.println("Chua xu ly: stmFix instanceof ExpressionNode");
            } else if (stmFix instanceof AssignmentNode) {
                System.out.println("Chua xu ly: stmFix instanceof AssignmentNode");
            } else if (stmFix instanceof ClassInstanceCreationNode) {
                System.out.println("Chua xu ly: stmFix instanceof ClassInstanceCreationStmNode");
            }
//            else if (stmFix instanceof ConstructorInvocationNode) {
//                System.out.println("Chua xuLy: stmFix instanceof ConstructorInvocationNode");
//            }
            else if (stmFix instanceof IfStmNode) {
                System.out.println("Chua xu ly: stmFix instanceof IfStmNode");
            } else {
                System.out.println("Quan sat else NODE REPLACE");
            }
            if (isAdd) {
                System.out.println("REPLACE: " + buggyNode + " => " + fixedNode);
                NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, null, null, null, Context.Scope.ALL_AFTER);
                nodeReplacement.methodFind = classBug.substring(methodNodeBug.getStartPosition(), methodNodeBug.getEndPosition());
                nodeReplacement.pathBugFile = pathBugFile;
                nodeReplacement.fixInstance = null;
                contexts.add(nodeReplacement);
            }
        }

    }

    private void findElement(StatementNode stmFix, StatementNode stmBug, MethodNode methodNodeBug, ClassNode
            classNodeBug) {
        TokenExist tokenExist = findNodeInClass(stmBug, stmFix, methodNodeBug, classNodeBug);
        if (tokenExist.line != -1) {
            ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix, tokenExist.statementNode,
                    tokenExist.isSameMethod, tokenExist.isSameLine, Context.Scope.ONLY_CURRENT);
            if (!tokenExist.isSameMethod) {
                MethodNode methodNodeFind = classNodeBug.findMethodNodeByStmLine(tokenExist.line);
                elementReplacement.methodFind = classBug.substring(methodNodeFind.getStartPosition(), methodNodeFind.getEndPosition());
            }
            elementReplacement.methodBug = classBug.substring(methodNodeBug.getStartPosition(), methodNodeBug.getEndPosition());
            elementReplacement.pathBugFile = pathBugFile;
            contexts.add(elementReplacement);
        } else {
            // diff name, diff param
            ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix,
                    null, null, null, Context.Scope.ONLY_CURRENT);
            elementReplacement.pathBugFile = pathBugFile;
            contexts.add(elementReplacement);
            //what type? token or not?
        }
    }

    private static boolean isParam(List<StatementNode> argSource, List<StatementNode> argFix) {
        boolean isParams = true;
        for (int i = 0; i < argSource.size(); i++) {
            StatementNode paramSource = argSource.get(i);
            StatementNode paramFixed = argFix.get(i);
            if (!(paramFixed instanceof Token) && !(paramSource instanceof Token)) {
                System.out.println(paramFixed.toString() + "=" + paramSource.toString());
            } else if (!(paramFixed instanceof Token) && (paramSource instanceof Token)) {
                isParams = false;
                break;
            } else if ((paramFixed instanceof Token) && !(paramSource instanceof Token)) {
                isParams = false;
                break;
            } else {
                if ((paramSource).getType() != null && (paramFixed).getType() != null)
                    if (!(paramSource).getType().equals((paramFixed).getType())) {
                        isParams = false;
                        break;
                    }
            }
        }
        return isParams;
    }


    private void findBaseVar(StatementNode stmBug, BaseVariableNode stmFix,
                             StatementNode stmBugg, StatementNode stmFixx) {
        MethodNode methodNode = null;
        ClassNode classNodeBUG = null;
        for (ClassNode classNode : classNodeListOld) {
            methodNode = classNode.findMethodNodeInFile(stmBug.getLine());
            if (methodNode != null) {
                classNodeBUG = classNode;
                break;
            }
        }
        if (methodNode == null) {
            return;
        }
        for (InitNode initNode : methodNode.getInitNodes()) {
            if (initNode.getLine() <= stmFix.getLine()) {
                if (initNode.getVarname().equals(stmFix.getKeyVar())) {
                    if (initNode.getType().equals(stmFix.getType())) {
                        if (stmBug instanceof BaseVariableNode) {
//                            StatementNode statementNode = findNodeInStatement(stmBug, )
                            ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix, initNode, true, null, Context.Scope.ONLY_CURRENT);
                            elementReplacement.methodBug = classBug.substring(methodNode.getStartPosition(),
                                    methodNode.getEndPosition());
                            elementReplacement.pathBugFile = pathBugFile;
                            elementReplacement.setOriginalString(stmBugg, stmFixx);
                            contexts.add(elementReplacement);
                            return;
                        } else {
                            NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, initNode, true, false, Context.Scope.ALL_AFTER);
                            nodeReplacement.methodBug = classBug.substring(methodNode.getStartPosition(), methodNode.getEndPosition());
                            nodeReplacement.methodBug = classBug.substring(methodNode.getStartPosition(),
                                    methodNode.getEndPosition());
                            nodeReplacement.pathBugFile = pathBugFile;
                            nodeReplacement.setOriginalString(stmBugg, stmFixx);
                            contexts.add(nodeReplacement);
                            return;
                        }
                    }
                }
            }
        }
        for (ClassNode classNode : classNodeListOld) {
            for (InitNode initNode : classNode.getInitNodes()) {
                if (initNode.getLine() <= stmFix.getLine()) {
                    if (initNode.getType().equals(stmFix.getType())) {
                        if (initNode.getVarname().equals(stmFix.getKeyVar())) {
                            if (initNode.getType().equals(stmFix.getType())) {
                                if (stmBug instanceof BaseVariableNode) {
                                    ElementReplacement elementReplacement = new ElementReplacement(stmBug, stmFix, initNode, false, null, Context.Scope.ONLY_CURRENT);
                                    elementReplacement.pathBugFile = pathBugFile;
                                    elementReplacement.setOriginalString(stmBugg, stmFixx);
                                    contexts.add(elementReplacement);
                                    return;
                                } else {
                                    NodeReplacement nodeReplacement = new NodeReplacement(stmBug, stmFix, initNode, false, false, Context.Scope.ALL_AFTER);
                                    nodeReplacement.methodBug = classBug.substring(methodNode.getStartPosition(), methodNode.getEndPosition());
                                    nodeReplacement.methodBug = classBug.substring(methodNode.getStartPosition(),
                                            methodNode.getEndPosition());
                                    nodeReplacement.pathBugFile = pathBugFile;
                                    nodeReplacement.setOriginalString(stmBugg, stmFixx);
                                    contexts.add(nodeReplacement);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void observeParams(List<StatementNode> paramsBug, List<StatementNode> paramFix, StatementNode
            bug, StatementNode fix, ClassNode classNode) {
        //compare params
        if (paramsBug.size() == paramFix.size()) {
            observe(paramsBug, paramFix);
        } else {
            // diff node same name, diff params (CREATE PARAM)
            NodeReplacement nodeReplacement = new NodeReplacement(bug, fix, null,
                    null, null, Context.Scope.ONLY_CURRENT);
            nodeReplacement.pathBugFile = pathBugFile;
            contexts.add(nodeReplacement);
//            add = false;
//            MethodNode methodNode = classNode.findMethodNodeByStmLine(stm.getLine());
//            TokenExist tokenExist = findNodeInClass(bug, stm, methodNode, classNode);
//            if (tokenExist.statementNode != null) {
//
//            }
        }
    }

    private TokenExist findNodeInClass(StatementNode bug, StatementNode fixed, MethodNode methodNode, ClassNode
            classNode) {
//        boolean isSame = false
        StatementNode stm = null;
        stm = findNodeInStatement(bug, fixed);
        if (stm != null) {
            return new TokenExist(bug.getLine(), true, true, stm);
        } else {
            //in methodNode
            for (StatementNode statementNode : methodNode.getStatementNodes()) {
                if (statementNode != bug) {
                    stm = findNodeInStatement(statementNode, fixed);
                    if (stm != null) {
                        return new TokenExist(statementNode.getLine(), true, false, stm);
                    }
                }
            }
            //other method
            for (MethodNode method : classNode.getMethodList()) {
                if (method.hashCode() != methodNode.hashCode()) {
                    for (StatementNode statementNode : method.getStatementNodes()) {
//                    if (bug.getLine() != statementNode.getLine()) {
                        stm = findNodeInStatement(statementNode, fixed);
                        if (stm != null) {
                            return new TokenExist(stm.getLine(), false, false, stm);
                        }
//                    }
                    }
                }
            }
        }
        return new TokenExist(-1, false, false, null);
    }

    private StatementNode findNodeInStatement(StatementNode statementNode, StatementNode fixed) {
        if (statementNode != null) {
            if (statementNode.getClass().toString().equals(fixed.getClass().toString())) {
                if (statementNode instanceof ClassInstanceCreationNode) {
                    ClassInstanceCreationNode source = (ClassInstanceCreationNode) statementNode;
                    ClassInstanceCreationNode fixNode = (ClassInstanceCreationNode) fixed;
                    if (((ClassInstanceCreationNode) statementNode).getName()
                            .equals(((ClassInstanceCreationNode) fixed).getName())) {
                        //compare param
                        if (source.getArgs().size() == fixNode.getArgs().size()) {
                            if (isParam(source.getArgs(), fixNode.getArgs())) {
//                                for (int i = 0; i < fixNode.getArgs().size(); i++) {
//                                    compareStatement(source.getArgs().get(i), fixNode.getArgs().get(i));
//                                }
                                return statementNode;
                            }
                        }
                    }
                } else if (statementNode instanceof MethodCalledNode) {
                    MethodCalledNode source = (MethodCalledNode) statementNode;
                    MethodCalledNode fixNode = (MethodCalledNode) fixed;
                    Token parentCandidate = (Token) source.getParent();
                    Token parenttarget = (Token) fixNode.getParent();
                    boolean isEquals = true;
                    if (parenttarget != null && parentCandidate != null) {
                        if (((StatementNode) parenttarget).getType() == null && ((StatementNode) parentCandidate).getType() == null) {
                            isEquals = true;
                        }
                        if (((StatementNode) parenttarget).getType() != null && ((StatementNode) parentCandidate).getType() != null) {
                            isEquals = ((StatementNode) parentCandidate).getType().equals(((StatementNode) parenttarget).getType());
                        }
                    } else if (parenttarget == null && parentCandidate != null) {
                        isEquals = false;
                    } else if (parenttarget != null && parentCandidate == null) {
                        isEquals = false;
                    }
                    if (isEquals) {
                        if (source.getMethodName().equals(fixNode.getMethodName())) {
                            //compare param
                            if (source.getAgurements().size() == fixNode.getAgurements().size()) {
                                if (isParam(source.getAgurements(), fixNode.getAgurements())) {
//                                    for (int i = 0; i < fixNode.getAgurements().size(); i++) {
//                                        compareStatement(source.getAgurements().get(i), fixNode.getAgurements().get(i));
//                                    }
                                    return statementNode;
                                }
                            }
                        }
                    }
                } else if (statementNode instanceof MethodInvocationStmNode) {
                    if (statementNode.getChildren().size() == fixed.getChildren().size()) {
                        for (int i = 0; i < statementNode.getChildren().size(); i++) {
                            boolean isEq = compareMethodInvocation(statementNode.getChildren().get(i), fixed.getChildren().get(i));
                            if (isEq) {
                                return statementNode;
                            }
                        }
                    }
//                    boolean isSame = compareMethodInvocation(statementNode, fixed);
//                    if (isSame) {
//                        return statementNode;
//                    }
                } else if (statementNode instanceof QualifiedNameNode) {
                    QualifiedNameNode fix = (QualifiedNameNode) fixed;
                    QualifiedNameNode find = (QualifiedNameNode) statementNode;
//                    if (fix.getType().equals(find.getType().getType())) {
//                        if (fix.getName().getKeyVar().equals(find.getName().getKeyVar())) {
//                            return statementNode;
//                        }
//                    }
                } else if (statementNode instanceof BooleanNode) {
                    BooleanNode stmFind = (BooleanNode) statementNode;
                    BooleanNode stmFix = (BooleanNode) fixed;
                    if (stmFind.isValue() == stmFix.isValue()) {
                        return stmFind;
                    }
                }
            }
        }
        //get child (except QualifierName)
        if (statementNode.getChildren() != null) {
            if (statementNode.getChildren().size() > 0) {
                for (StatementNode child : statementNode.getChildren()) {
                    StatementNode token = findNodeInStatement(child, fixed);
                    if (token != null) {
                        return token;
                    }
                }
            }
        }
        if (statementNode instanceof MethodCalledNode) {
            if (((MethodCalledNode) statementNode).getAgurements().size() > 0) {
                for (StatementNode child : ((MethodCalledNode) statementNode).getAgurements()) {
                    StatementNode token = findNodeInStatement(child, fixed);
                    if (token != null) {
                        return token;
                    }
                }
            }
        }
        return null;
    }

    private List<Context> compareMethodInvocation(StatementNode bug, StatementNode fixed, StatementNode bugg, StatementNode fixedd) {
        List<Context> elements = new ArrayList<>();
        boolean isChild = true;
        boolean isDiffMethod = false;

        if (bug instanceof BaseVariableNode && fixed instanceof BaseVariableNode) {
            if (!((BaseVariableNode) bug).getKeyVar().equals(((BaseVariableNode) fixed).getKeyVar())) {
                findBaseVar(bug, (BaseVariableNode) fixed, bugg, fixedd);
            } else {
                isSameMethodInvo = true;
            }
        } else if (bug instanceof MethodCalledNode && fixed instanceof MethodCalledNode) {
            if (!((MethodCalledNode) bug).getMethodName()
                    .equals(((MethodCalledNode) fixed).getMethodName())) {
                //diffMethod
                isDiffMethod = true;
            } else {
                if (((MethodCalledNode) bug).getAgurements().size() != ((MethodCalledNode) fixed).getAgurements().size()) {
                    //diffMethod
                    isDiffMethod = true;
                } else if (((MethodCalledNode) bug).getAgurements().size() == ((MethodCalledNode) fixed).getAgurements().size()) {
                    isSameMethodInvo = true;
                    for (int i = 0; i < ((MethodCalledNode) bug).getAgurements().size(); i++) {
                        compareStatement(((MethodCalledNode) bug).getAgurements().get(i),
                                ((MethodCalledNode) fixed).getAgurements().get(i), bugg, fixedd);
                    }
                }
            }
            if (isDiffMethod) {
                if (bug.getChildren().size() > 0
                        && fixed.getChildren().size() > 0) {
                    ElementReplacement elementReplacement = setElementOnlyCurrent(bug, fixed, bugg, fixedd);
                    elements.add(elementReplacement);

                } else if (bug.getChildren().size() > 0 && fixed.getChildren().size() == 0) {
                    ElementReplacement elementReplacement = setElementAllAfter(bug, fixed, bugg, fixedd);
                    elements.add(elementReplacement);
                    isChild = false;
                } else if (bug.getChildren().size() == 0 && fixed.getChildren().size() == 0) {
                    ElementReplacement elementReplacement = setElementOnlyCurrent(bug, fixed, bugg, fixedd);
                    elements.add(elementReplacement);
                    isChild = false;
                } else {
                    System.out.println("NAN: " + bugg.toString() + "->" + fixedd.toString());
                }
            }
        }
        if (isChild) {
            //countinue
            if (bug.getChildren().size() > 0 && fixed.getChildren().size() > 0) {
                List<Context> contexts = compareMethodInvocation(bug.getChildren().get(0), fixed.getChildren().get(0), bugg, fixedd);
                elements.addAll(contexts);
            }
        }

        return elements;
    }

    private List<String> getParamTypes(MethodCalledNode methodCalledNode) {
        List<String> params = new ArrayList<>();
        for (StatementNode type : methodCalledNode.getAgurements()) {
            params.add(type.getType());
        }
        return params;
    }

//    private boolean compareParams(StatementNode stmNode, StatementNode target) {
//        List<String> paramsA = getParamTypes((MethodCalledNode) stmNode);
//        List<String> paramsB = getParamTypes((MethodCalledNode) target);
//        return ReflectionHelper.compareParams(paramsA, paramsB);
//    }

    private boolean compareMethodInvocation(StatementNode bug, StatementNode fixed) {
        boolean isEqual = true;
        if ((bug instanceof BaseVariableNode) && (fixed instanceof BaseVariableNode)) {
            if (((BaseVariableNode) bug).getType() != null && ((BaseVariableNode) fixed).getType() != null) {
                if (((BaseVariableNode) bug).getType() != ((BaseVariableNode) fixed).getType()) {
                    isEqual = false;
                    return isEqual;
                }
            }
        } else if (bug instanceof MethodCalledNode && fixed instanceof MethodCalledNode) {
            if (((MethodCalledNode) bug).getHashCode() != ((MethodCalledNode) fixed).getHashCode()) {
                isEqual = false;
                return isEqual;
            }
        } else {
            return false;
        }
        if (isEqual) {
            if (bug.getChildren() != null && fixed.getChildren() != null) {
                if (bug.getChildren().size() != fixed.getChildren().size()) {
                    isEqual = false;
                    return isEqual;
                } else {
                    for (int i = 0; i < bug.getChildren().size(); i++) {
                        boolean isEq = compareMethodInvocation(bug.getChildren().get(i), fixed.getChildren().get(i));
                        if (!isEq) {
                            return false;
                        }
                    }
                }
            }
        }
        return isEqual;
    }

//    public static void main(String[] args) {
//        String oldFile = "/home/huyenhuyen/Desktop/data-bugfixes/CodRepFormat/Dataset1_3805/old/3805.txt";
//        String fixFile = "/home/huyenhuyen/Desktop/data-bugfixes/CodRepFormat/Dataset1_3805/fix/3805.txt";
//        File fileOld = new File(oldFile);
//        File fileFix = new File(fixFile);
//        contexts = new ArrayList<>();
//        int id = 70;
//        try {
//            HDRepairDatatasetMain hdRepairDatatasetMain = new HDRepairDatatasetMain("/home/huyenhuyen/obseve/out/hd");
//            h.getDiff(fileOld, fileFix, id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    //    public static void main(String[] args) throws Exception {
//                   HDRepairDatatasetMain hdRepairDatatasetMain = new HDRepairDatatasetMain(out);
//        File old = Objects.requireNonNull(new File("/home/huyenhuyen/Desktop/data-bugfixes/CodRepFormat/Dataset1_1961/old/1961.txt"));
//        File fix = Objects.requireNonNull(new File("/home/huyenhuyen/Desktop/data-bugfixes/CodRepFormat/Dataset1_1961/fix/1961.txt"));
//        hdRepairDatatasetMain.getDiff(old, fix, fix.getName());
//    }

}
