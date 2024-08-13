package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* Generates the abstract syntax tree format for the lox compiler. This is not
* part of the Lox language itself, but instead a simple generation tool to enable
* development on the language.
*/
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: GenerateAst <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    /*
    * Define and generate the AST structure from the given components. Output the
    * structure to the outputDir .java file.
    */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // define the general visitor method
        defineVisitor(writer, baseName, types);

        // iterate over the ast types
        for (String type: types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // define the abstract accept method
        writer.println();
        writer.println("abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    /*
    * constructs the visitor interface within the AST class. Defines a visitor class method for each of the
    * AST types that are being constructed.
    */
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }


    /*
    * creates a new type as a class within the AST class
    */
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println();
        writer.println("    static class " + className + " extends " + baseName + " {");

        // constructor for type
        writer.println("        " + className + "(" + fieldList + ") {");

        // store the parameters in the class fields
        String[] fields = fieldList.split(",");
        for (String field: fields) {
            field = field.trim();
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        // close the constructor
        writer.println("        }");

        // define the visitor pattern
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");


        // define the fields
        writer.println();
        for (String field: fields) {
            writer.println("        final " + field + ";");
        }

        writer.print("    }");
        writer.println();

    }
}
