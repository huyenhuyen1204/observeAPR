package AST.parser;

import AST.node.ClassNode;
import org.eclipse.jdt.core.dom.*;
import util.FileService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaFileParser {

    public static List<ClassNode> parse(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(sourceCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        List<ClassNode> classes = new ArrayList<>();
        cu.accept(new ASTVisitor() {
            public boolean visit(TypeDeclaration node) {
                ClassNode classNode = new ClassNode();
                if (node != null) {
                    classNode.setInforFromASTNode(node, cu);
                    classes.add(classNode);
                    return false;
                }
                return true;
            }
        });
        return classes;
    }


    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\Dell\\Desktop\\DebuRepair\\data_test\\81171\\MyTest.java" +
                "");
        String content = FileService.readFileToString(file.getAbsolutePath());
        List<ClassNode> classNodes = JavaFileParser.parse(content);
//        for (ClassNode classNode : classNodes) {
//            for (MethodNode methodNode: classNode.getMethodList()) {
//                methodNode.printInfor();
//            }
//            for (FieldNode fieldNode : classNode.getFieldList()) {
//                fieldNode.printInfor();
//            }
//        }
        System.out.println(classNodes.toString());
    }

}
