package main.java;

import main.antlr.XQueryParser;
import main.antlr.XQueryBaseVisitor;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XQueryEvalVisitor extends XQueryBaseVisitor<List<Node>> {

    private List<Node> curNodes = new ArrayList<>();

    @Override
    public List<Node> visitApChildren(XQueryParser.ApChildrenContext ctx) {
        visit(ctx.doc());
        return visit(ctx.rp());
    }

    @Override
    public List<Node> visitApDescendants(XQueryParser.ApDescendantsContext ctx) {
        visit(ctx.doc());
        curNodes.addAll(getAllDescendants(curNodes));
        return visit(ctx.rp());
    }

    @Override
    public List<Node> visitXmlDoc(XQueryParser.XmlDocContext ctx) {
        List<Node> result = new ArrayList<>();
        String resource = "src/main/resources";
        String fname = ctx.fname().getText();
        File xmlFile = Paths.get(resource, fname).toAbsolutePath().toFile();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);
            result.add(doc);
            curNodes = result;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return result;
    }

    @Override
    public List<Node> visitFileName(XQueryParser.FileNameContext ctx) {
        return super.visitFileName(ctx);
    }

    @Override
    public List<Node> visitCurrent(XQueryParser.CurrentContext ctx) {
        return new ArrayList<>(curNodes);
    }

    @Override
    public List<Node> visitTagName(XQueryParser.TagNameContext ctx) {
        String tagName = ctx.WORD().getText();
        List<Node> result = new ArrayList<>();

        for (Node node : curNodes) {
            NodeList Children = node.getChildNodes();
            for (int i = 0; i < Children.getLength(); i++) {
                Node child = Children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName))
                    result.add(child);
            }
        }
        curNodes = result;

        return result;
    }

    @Override
    public List<Node> visitParent(XQueryParser.ParentContext ctx) {
        List<Node> result = getParent(curNodes);
        curNodes = result;
        return result;
    }

    @Override
    public List<Node> visitAttribute(XQueryParser.AttributeContext ctx) {
        List<Node> result = new ArrayList<>();
        for (Node node : curNodes) {
            NamedNodeMap nodeMap = node.getAttributes();

            if (nodeMap != null && nodeMap.getNamedItem(ctx.WORD().getText()) != null) {
                result.add(node);
            }
        }
        curNodes = result;
        return result;
    }

    @Override
    public List<Node> visitRpChildren(XQueryParser.RpChildrenContext ctx) {
        visit(ctx.rp(0));
        List<Node> result = visit(ctx.rp(1));
        curNodes = result;

        return result;
    }

    @Override
    public List<Node> visitRpParentheses(XQueryParser.RpParenthesesContext ctx) {
        return visit(ctx.rp());
    }

    @Override
    public List<Node> visitText(XQueryParser.TextContext ctx) {
        List<Node> result = new ArrayList<>();
        for (Node node : curNodes) {
            NodeList children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.TEXT_NODE)
                    result.add(children.item(i));
            }
        }
        curNodes = result;
        return result;
    }

    @Override
    public List<Node> visitChildren(XQueryParser.ChildrenContext ctx) {
        List<Node> result = new ArrayList<>();
        for (Node node : curNodes) {
            for (int i = 0; i < node.getChildNodes().getLength(); ++i) {
                result.add(node.getChildNodes().item(i));
            }
        }
        curNodes = result;
        return result;
    }

    @Override
    public List<Node> visitRpConcat(XQueryParser.RpConcatContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> result = visit(ctx.rp(0));
        curNodes = backup;
        result.addAll(visit(ctx.rp(1)));
        result = unique(result);
        curNodes = result;
        return result;
    }

    @Override
    public List<Node> visitRpDescendants(XQueryParser.RpDescendantsContext ctx) {
        visit(ctx.rp(0));
        curNodes.addAll(getAllDescendants(curNodes));
        List<Node> nodes = visit(ctx.rp(1));
        curNodes = nodes;

        return unique(nodes);
    }

    @Override
    public List<Node> visitRpFilter(XQueryParser.RpFilterContext ctx) {
        List<Node> rpNodes = visit(ctx.rp());
        List<Node> result = new ArrayList<>();

        for (Node node : rpNodes) {
            List<Node> single = new ArrayList<>();
            single.add(node);
            curNodes = single;

            if (!visit(ctx.filter()).isEmpty())
                result.add(node);
        }

        curNodes = result;

        return result;
    }

    @Override
    public List<Node> visitFilterEqual(XQueryParser.FilterEqualContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = visit(ctx.rp(0));
        curNodes = backup;
        List<Node> nodes1 = visit(ctx.rp(1));

        List<Node> result =  new ArrayList<>();
        for (Node i : nodes0) {
            for (Node j : nodes1) {
                if (i.isEqualNode(j)) {
                    result.add(i);
                }
            }
        }
        curNodes = result;
        return result;
    }

    @Override
    public List<Node> visitFilterNot(XQueryParser.FilterNotContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = visit(ctx.filter());
        curNodes = backup;
        curNodes.removeAll(nodes0);
        return new ArrayList<>(curNodes);
    }

    @Override
    public List<Node> visitFilterOr(XQueryParser.FilterOrContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> result = visit(ctx.filter(0));
        curNodes = backup;
        result.addAll(visit(ctx.filter(1)));

        result = unique(result);
        curNodes = result;

        return result;
    }

    @Override
    public List<Node> visitFilterAnd(XQueryParser.FilterAndContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = visit(ctx.filter(0));
        curNodes = backup;
        List<Node> nodes1 = visit(ctx.filter(1));

        List<Node> result = new ArrayList<>();
        for (Node node : nodes0) {
            if (nodes1.contains(node)) {
                result.add(node);
            }
        }
        curNodes = result;
        return result;
    }

    @Override
    public List<Node> visitFilterRp(XQueryParser.FilterRpContext ctx) {
        List<Node> result = visit(ctx.rp());
        curNodes = result;

        return result;
    }

    @Override
    public List<Node> visitFilterParentheses(XQueryParser.FilterParenthesesContext ctx) {
        return visit(ctx.filter());
    }

    @Override
    public List<Node> visitFilterIs(XQueryParser.FilterIsContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = visit(ctx.rp(0));
        curNodes = backup;
        List<Node> nodes1 = visit(ctx.rp(1));

        List<Node> result =  new ArrayList<>();
        for (Node i : nodes0) {
            for (Node j : nodes1) {
                if (i.isSameNode(j)) {
                    result.add(i);
                }
            }
        }
        curNodes = result;
        return result;
    }

    private List<Node> unique(List<Node> nodes) {
        Set<Node> nodeSet = new HashSet<>();
        List<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            if (!nodeSet.contains(node)) {
                nodeSet.add(node);
                result.add(node);
            }
        }

        return result;
    }

    private List<Node> getAllDescendants(List<Node> nodes) {
        List<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                List<Node> child = new ArrayList<>();
                child.add(children.item(i));
                result.add(children.item(i));
                result.addAll(getAllDescendants(child));
            }
        }

        return unique(result);
    }

    private List<Node> getParent(List<Node> nodes) {
        List<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            result.add(node.getParentNode());
        }

        return unique(result);
    }
}
