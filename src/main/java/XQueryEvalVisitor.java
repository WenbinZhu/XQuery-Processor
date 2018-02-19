package main.java;

import main.antlr.XQueryParser;
import main.antlr.XQueryBaseVisitor;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XQueryEvalVisitor extends XQueryBaseVisitor<List<Node>> {

    private Document outputDoc;
    private List<Node> curNodes = new ArrayList<>();
    private Map<String, List<Node>> varMap = new HashMap<>();

    XQueryEvalVisitor() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            outputDoc = docBuilder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public List<Node> visitXqAp(XQueryParser.XqApContext ctx) {
        return visit(ctx.ap());
    }

    @Override
    public List<Node> visitXqChildren(XQueryParser.XqChildrenContext ctx) {
        curNodes = visit(ctx.xq());
        List<Node> result = visit(ctx.rp());
        curNodes = result;

        return result;
    }

    @Override
    public List<Node> visitXqDescendants(XQueryParser.XqDescendantsContext ctx) {
        curNodes = visit(ctx.xq());
        curNodes.addAll(getAllDescendants(curNodes));
        List<Node> nodes = visit(ctx.rp());
        curNodes = nodes;

        return unique(nodes);
    }

    @Override
    public List<Node> visitXqVariable(XQueryParser.XqVariableContext ctx) {
        if (!varMap.containsKey(ctx.getText()))
            return Collections.emptyList();

        return new ArrayList<>(varMap.get(ctx.getText()));
    }

    @Override
    public List<Node> visitVar(XQueryParser.VarContext ctx) {
        return Collections.emptyList();
    }

    @Override
    public List<Node> visitXqLetClause(XQueryParser.XqLetClauseContext ctx) {
        Map<String, List<Node>> backup = new HashMap<>(varMap);

        visit(ctx.letClause());
        List<Node> result = visit(ctx.xq());
        varMap = backup;

        return result;
    }

    @Override
    public List<Node> visitXqConcat(XQueryParser.XqConcatContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> result = visit(ctx.xq(0));
        curNodes = new ArrayList<>(backup);
        result.addAll(visit(ctx.xq(1)));
        curNodes = backup;

        return result;
    }

    @Override
    public List<Node> visitXqEnclosedTags(XQueryParser.XqEnclosedTagsContext ctx) {
        String startTag = ((XQueryParser.XqStartTagContext) ctx.startTag()).WORD().getText();
        String endTag = ((XQueryParser.XqEndTagContext) ctx.endTag()).WORD().getText();

        if (!startTag.equals(endTag)) {
            throw new RuntimeException("Start tag does not match end tag");
        }

        Node resultNode = outputDoc.createElement(startTag);
        for (Node node : visit(ctx.xq())) {
            Node child = outputDoc.importNode(node, true);
            resultNode.appendChild(child);
        }

        List<Node> result = new ArrayList<>();
        result.add(resultNode);

        return result;
    }

    @Override
    public List<Node> visitXqStartTag(XQueryParser.XqStartTagContext ctx) {
        return visit(ctx.WORD());
    }

    @Override
    public List<Node> visitXqEndTag(XQueryParser.XqEndTagContext ctx) {
        return visit(ctx.WORD() );
    }

    @Override
    public List<Node> visitXqParentheses(XQueryParser.XqParenthesesContext ctx) {
        return visit(ctx.xq());
    }

    @Override
    public List<Node> visitXqFLWR(XQueryParser.XqFLWRContext ctx) {
        List<Node> result = new ArrayList<>();
        Map<String, List<Node>> backup = new HashMap<>(varMap);

        visitFLWR(ctx, 0, result);
        varMap = backup;

        return result;
    }

    private void visitFLWR(XQueryParser.XqFLWRContext ctx, int k, List<Node> result) {
        if (k == ctx.forClause().var().size()) {
            if (ctx.letClause() != null) {
                visit(ctx.letClause());
            }
            if (ctx.whereClause() != null && visit(ctx.whereClause()) == null) {
                return;
            }

            result.addAll(visit(ctx.returnClause()));
            return;
        }

        String var = ctx.forClause().var(k).getText();
        List<Node> bind = visit(ctx.forClause().xq(k));

        for (Node node : bind) {
            Map<String, List<Node>> backup = new HashMap<>(varMap);
            List<Node> single = new ArrayList<>();

            single.add(node);
            varMap.put(var, single);
            visitFLWR(ctx, k + 1, result);
            varMap = backup;
        }
    }

    @Override
    public List<Node> visitXqString(XQueryParser.XqStringContext ctx) {
        String text = ctx.getText();
        text = text.substring(1, text.length() - 1);
        Node textNode = outputDoc.createTextNode(text);

        List<Node> result = new ArrayList<>();
        result.add(textNode);

        return result;
    }

    @Override
    public List<Node> visitForClause(XQueryParser.ForClauseContext ctx) {
        return Collections.emptyList();
    }

    @Override
    public List<Node> visitLetClause(XQueryParser.LetClauseContext ctx) {
        List<XQueryParser.VarContext> vars = ctx.var();

        for (int i = 0; i < vars.size(); i++) {
            varMap.put(vars.get(i).getText(), visit(ctx.xq(i)));
        }

        return Collections.emptyList();
    }

    @Override
    public List<Node> visitWhereClause(XQueryParser.WhereClauseContext ctx) {
        return visit(ctx.cond());
    }

    @Override
    public List<Node> visitReturnClause(XQueryParser.ReturnClauseContext ctx) {
        return visit(ctx.xq());
    }

    @Override
    public List<Node> visitCondOr(XQueryParser.CondOrContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = visit(ctx.cond(0));
        curNodes = new ArrayList<>(backup);
        List<Node> nodes1 = visit(ctx.cond(1));
        curNodes = backup;

        if (nodes0 != null || nodes1 != null) {
            return Collections.emptyList();
        }

        return null;
    }

    @Override
    public List<Node> visitCondAnd(XQueryParser.CondAndContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = visit(ctx.cond(0));
        curNodes = new ArrayList<>(backup);
        List<Node> nodes1 = visit(ctx.cond(1));
        curNodes = backup;

        if (nodes0 != null && nodes1 != null) {
            curNodes = Collections.emptyList();
        }

        return curNodes;
    }

    @Override
    public List<Node> visitCondParentheses(XQueryParser.CondParenthesesContext ctx) {
        return visit(ctx.cond());
    }

    @Override
    public List<Node> visitCondEmpty(XQueryParser.CondEmptyContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes = visit(ctx.xq());
        curNodes = backup;

        if (nodes.size() == 0) {
            return Collections.emptyList();
        }

        return null;
    }

    @Override
    public List<Node> visitCondSome(XQueryParser.CondSomeContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        boolean flag = visitCondSomeHelper(ctx, 0);
        curNodes = backup;

        if (flag) {
            return Collections.emptyList();
        }

        return null;
    }

    private boolean visitCondSomeHelper(XQueryParser.CondSomeContext ctx, int k){
        if (k == ctx.var().size()) {
            return visit(ctx.cond()) != null;
        }
        else{
            String key = ctx.var(k).getText();
            List<Node> valueList = visit(ctx.xq(k));

            for (Node node: valueList) {
                HashMap<String, List<Node>> backup = new HashMap<>(varMap);

                LinkedList<Node> value = new LinkedList<>(); value.add(node);
                varMap.put(key, value);
                if (k + 1 <= ctx.var().size()) {
                    if (visitCondSomeHelper(ctx, k + 1)) {
                        varMap = backup;
                        return true;
                    }
                }
                varMap = backup;
            }
        }

        return false;
    }

    @Override
    public List<Node> visitCondIs(XQueryParser.CondIsContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = visit(ctx.xq(0));
        curNodes = new ArrayList<>(backup);
        List<Node> nodes1 = visit(ctx.xq(1));
        curNodes = backup;

        for (Node n0 : nodes0) {
            for (Node n1 : nodes1) {
                if (n0.isSameNode(n1)) {
                    return Collections.emptyList();
                }
            }
        }

        return null;
    }

    @Override
    public List<Node> visitCondNot(XQueryParser.CondNotContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes = visit(ctx.cond());
        curNodes = backup;

        if (nodes == null) {
            return Collections.emptyList();
        }

        return null;
    }

    @Override
    public List<Node> visitCondEqual(XQueryParser.CondEqualContext ctx) {
        List<Node> backup = new ArrayList<>(curNodes);
        List<Node> nodes0 = new LinkedList<>(visit(ctx.xq(0)));
        curNodes = new ArrayList<>(backup);
        List<Node> nodes1 = new LinkedList<>(visit(ctx.xq(1)));
        curNodes = backup;

        for (Node n0 : nodes0) {
            for (Node n1 : nodes1) {
                if (n0.isEqualNode(n1)) {
                    return Collections.emptyList();
                }
            }
        }

        return null;
    }

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
            Document inputDoc = docBuilder.parse(xmlFile);
            result.add(inputDoc);
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
        List<Node> result = getParents(curNodes);
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

        List<Node> result = new ArrayList<>();
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

        List<Node> result = new ArrayList<>();
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

    private List<Node> getParents(List<Node> nodes) {
        List<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            result.add(node.getParentNode());
        }

        return unique(result);
    }
}
