package main.java;

import main.antlr.XQueryLexer;
import main.antlr.XQueryParser;

import java.io.FileInputStream;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Node;


public class XQueryEvaluator {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Input query file must be provided.");
            System.exit(1);
        }

        // Execute input query
        String queryFile = args[0];
        FileInputStream fis = new FileInputStream(queryFile);
        ANTLRInputStream ais = new ANTLRInputStream(fis);
        XQueryLexer lexer = new XQueryLexer(ais);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XQueryParser parser = new XQueryParser(tokens);
        ParseTree tree = parser.ap();
        XQueryEvalVisitor visitor = new XQueryEvalVisitor();
        List<Node> queryResult = visitor.visit(tree);

        // Write query result to xml file
    }
}
