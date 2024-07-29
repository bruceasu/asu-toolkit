//package me.asu.util;
//
//import com.github.javaparser.JavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.BodyDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//
//public class JavaParserExample {
//    public static void main(String[] args) throws IOException {
//        FileInputStream fileInputStream = new FileInputStream("HelloWorld.java"); // Replace "HelloWorld.java" with your Java source file
//        CompilationUnit cu = JavaParser.parse(fileInputStream);
//
//        // Visit and print the methods in the file
//        new MethodVisitor().visit(cu, null);
//    }
//
//    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
//        @Override
//        public void visit(MethodDeclaration n, Void arg) {
//            System.out.println("Method Name: " + n.getName());
//            System.out.println("Parameters: " + n.getParameters());
//            System.out.println("Body: " + n.getBody());
//            super.visit(n, arg);
//        }
//    }
//}
