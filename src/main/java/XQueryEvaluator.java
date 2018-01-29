package main.java;

import main.antlr.XQueryLexer;
import main.antlr.XQueryParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XQueryEvaluator {

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("user.dir"));

        if (args.length != 2) {
            System.out.println("Input query file and output file must be provided.");
            System.exit(1);
        }

        // Execute input query
        String queryFile = args[0];
        String outputFile = args[1];
        FileInputStream fis = new FileInputStream(queryFile);
        ANTLRInputStream ais = new ANTLRInputStream(fis);
        XQueryLexer lexer = new XQueryLexer(ais);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XQueryParser parser = new XQueryParser(tokens);
        ParseTree tree = parser.ap();
        XQueryEvalVisitor visitor = new XQueryEvalVisitor();
        List<Node> queryResult = visitor.visit(tree);

        // Generate output xml object
        System.out.println("Query result: " + queryResult);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Add query result nodes to xml object
        Node resultNode = doc.createElement("Result");
        for (Node node : queryResult) {
            Node copy = doc.importNode(node, true);
            resultNode.appendChild(copy);
        }
        doc.appendChild(resultNode);

        // Generate output xml file
        TransformerFactory tfFactory = TransformerFactory.newInstance();
        Transformer transformer = tfFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new FileOutputStream(outputFile));
        transformer.transform(source, result);
    }
}
