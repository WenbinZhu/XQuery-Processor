package main.java;

import main.antlr.XQueryParser;
import org.w3c.dom.Node;
import java.util.List;

import main.antlr.XQueryBaseVisitor;

public class XQueryEvalVisitor extends XQueryBaseVisitor<List<Node>> {
    @Override
    public List<Node> visitApChildren(XQueryParser.ApChildrenContext ctx) {
        return super.visitApChildren(ctx);
    }

    @Override
    public List<Node> visitApDescendants(XQueryParser.ApDescendantsContext ctx) {
        return super.visitApDescendants(ctx);
    }

    @Override
    public List<Node> visitDescendants(XQueryParser.DescendantsContext ctx) {
        return super.visitDescendants(ctx);
    }

    @Override
    public List<Node> visitTagName(XQueryParser.TagNameContext ctx) {
        return super.visitTagName(ctx);
    }

    @Override
    public List<Node> visitParent(XQueryParser.ParentContext ctx) {
        return super.visitParent(ctx);
    }

    @Override
    public List<Node> visitAttribute(XQueryParser.AttributeContext ctx) {
        return super.visitAttribute(ctx);
    }

    @Override
    public List<Node> visitRpChildren(XQueryParser.RpChildrenContext ctx) {
        return super.visitRpChildren(ctx);
    }

    @Override
    public List<Node> visitRpParentheses(XQueryParser.RpParenthesesContext ctx) {
        return super.visitRpParentheses(ctx);
    }

    @Override
    public List<Node> visitText(XQueryParser.TextContext ctx) {
        return super.visitText(ctx);
    }

    @Override
    public List<Node> visitChildren(XQueryParser.ChildrenContext ctx) {
        return super.visitChildren(ctx);
    }

    @Override
    public List<Node> visitRpConcat(XQueryParser.RpConcatContext ctx) {
        return super.visitRpConcat(ctx);
    }

    @Override
    public List<Node> visitRpDescendants(XQueryParser.RpDescendantsContext ctx) {
        return super.visitRpDescendants(ctx);
    }

    @Override
    public List<Node> visitRpFilter(XQueryParser.RpFilterContext ctx) {
        return super.visitRpFilter(ctx);
    }

    @Override
    public List<Node> visitFilterEqual(XQueryParser.FilterEqualContext ctx) {
        return super.visitFilterEqual(ctx);
    }

    @Override
    public List<Node> visitFilterNot(XQueryParser.FilterNotContext ctx) {
        return super.visitFilterNot(ctx);
    }

    @Override
    public List<Node> visitFilterOr(XQueryParser.FilterOrContext ctx) {
        return super.visitFilterOr(ctx);
    }

    @Override
    public List<Node> visitFilterAnd(XQueryParser.FilterAndContext ctx) {
        return super.visitFilterAnd(ctx);
    }

    @Override
    public List<Node> visitFilterRp(XQueryParser.FilterRpContext ctx) {
        return super.visitFilterRp(ctx);
    }

    @Override
    public List<Node> visitFilterParentheses(XQueryParser.FilterParenthesesContext ctx) {
        return super.visitFilterParentheses(ctx);
    }

    @Override
    public List<Node> visitFilterIs(XQueryParser.FilterIsContext ctx) {
        return super.visitFilterIs(ctx);
    }
}
