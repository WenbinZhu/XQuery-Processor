package main.java;

import main.antlr.XQueryBaseVisitor;
import main.antlr.XQueryLexer;
import main.antlr.XQueryParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (args.length != 3 || !(args[2].equals("normal") || args[2].equals("optimized"))) {
            System.out.println("Usage: XQueryEvaluator input_path output_path normal/optimized");
            System.exit(1);
        }

        // Execute input query
        String queryFile = args[0];
        String outputFile = args[1];
        String queryType = args[2];
        List<Node> queryResult = getQueryResult(queryFile, queryType);

        // Generate output xml object
        System.out.println("Query result: " + queryResult);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);

        // Add query result nodes to xml object
        if (queryResult.size() == 1) {
            Node root = doc.importNode(queryResult.get(0), true);
            doc.appendChild(root);
        } else if (queryResult.size() > 0) {
            Node resultNode = doc.createElement("Result");
            for (Node node : queryResult) {
                if (node != null) {
                    Node copy = doc.importNode(node, true);
                    resultNode.appendChild(copy);
                }
            }
            doc.appendChild(resultNode);
        }

        // Generate output xml file
        TransformerFactory tfFactory = TransformerFactory.newInstance();
        Transformer transformer = tfFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new FileOutputStream(outputFile));
        transformer.transform(source, result);
    }

    private static List<Node> getQueryResult(String queryFile, String queryType) throws Exception {
        FileInputStream fis = new FileInputStream(queryFile);
        ANTLRInputStream ais = new ANTLRInputStream(fis);
        XQueryEvalVisitor evalVisitor = new XQueryEvalVisitor();
        XQueryRewriteVisitor rewriteVisitor = new XQueryRewriteVisitor();

        if (queryType.equals("normal")) {
            return getQueryResultFromVisitor(ais, evalVisitor);
        }

        String rewrote = getQueryResultFromVisitor(ais, rewriteVisitor);
        System.out.println("===" + rewrote + "===");
        ais = new ANTLRInputStream(rewrote);

        return getQueryResultFromVisitor(ais, evalVisitor);
    }

    private static <T> T getQueryResultFromVisitor(ANTLRInputStream ais, XQueryBaseVisitor<T> visitor) {
        XQueryLexer lexer = new XQueryLexer(ais);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XQueryParser parser = new XQueryParser(tokens);
        ParseTree tree = parser.xq();

        return visitor.visit(tree);
    }
}
