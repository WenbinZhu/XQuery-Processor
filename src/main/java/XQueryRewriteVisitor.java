package main.java;

import main.antlr.XQueryBaseVisitor;
import main.antlr.XQueryParser;

public class XQueryRewriteVisitor extends XQueryBaseVisitor<String> {
    @Override
    public String visitXqChildren(XQueryParser.XqChildrenContext ctx) {
        return super.visitXqChildren(ctx);
    }

    @Override
    public String visitXqAp(XQueryParser.XqApContext ctx) {
        return super.visitXqAp(ctx);
    }

    @Override
    public String visitXqDescendants(XQueryParser.XqDescendantsContext ctx) {
        return super.visitXqDescendants(ctx);
    }

    @Override
    public String visitXqVariable(XQueryParser.XqVariableContext ctx) {
        return super.visitXqVariable(ctx);
    }

    @Override
    public String visitXqConcat(XQueryParser.XqConcatContext ctx) {
        return super.visitXqConcat(ctx);
    }

    @Override
    public String visitXqEnclosedTags(XQueryParser.XqEnclosedTagsContext ctx) {
        return super.visitXqEnclosedTags(ctx);
    }

    @Override
    public String visitXqParentheses(XQueryParser.XqParenthesesContext ctx) {
        return super.visitXqParentheses(ctx);
    }

    @Override
    public String visitXqFLWR(XQueryParser.XqFLWRContext ctx) {
        return super.visitXqFLWR(ctx);
    }

    @Override
    public String visitVar(XQueryParser.VarContext ctx) {
        return super.visitVar(ctx);
    }

    @Override
    public String visitXqStartTag(XQueryParser.XqStartTagContext ctx) {
        return super.visitXqStartTag(ctx);
    }

    @Override
    public String visitXqEndTag(XQueryParser.XqEndTagContext ctx) {
        return super.visitXqEndTag(ctx);
    }

    @Override
    public String visitForClause(XQueryParser.ForClauseContext ctx) {
        return super.visitForClause(ctx);
    }

    @Override
    public String visitReturnClause(XQueryParser.ReturnClauseContext ctx) {
        return super.visitReturnClause(ctx);
    }

    @Override
    public String visitJoinClause(XQueryParser.JoinClauseContext ctx) {
        return super.visitJoinClause(ctx);
    }

    @Override
    public String visitAttrList(XQueryParser.AttrListContext ctx) {
        return super.visitAttrList(ctx);
    }

    @Override
    public String visitAttrName(XQueryParser.AttrNameContext ctx) {
        return super.visitAttrName(ctx);
    }

    @Override
    public String visitWhereClause(XQueryParser.WhereClauseContext ctx) {
        return super.visitWhereClause(ctx);
    }

    @Override
    public String visitCondEqual(XQueryParser.CondEqualContext ctx) {
        return super.visitCondEqual(ctx);
    }

    @Override
    public String visitCondAnd(XQueryParser.CondAndContext ctx) {
        return super.visitCondAnd(ctx);
    }

    @Override
    public String visitXqString(XQueryParser.XqStringContext ctx) {
        return super.visitXqString(ctx);
    }

    @Override
    public String visitXmlDoc(XQueryParser.XmlDocContext ctx) {
        return super.visitXmlDoc(ctx);
    }
}
