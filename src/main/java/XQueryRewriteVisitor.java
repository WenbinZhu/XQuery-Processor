package main.java;

import main.antlr.XQueryBaseVisitor;
import main.antlr.XQueryParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;

public class XQueryRewriteVisitor extends XQueryBaseVisitor<String> {

    // collect XQuery tables in for clause
    private List<XQueryTable> tables = new ArrayList<>();

    // map from variable name to XQuery table index
    private Map<String, Integer> var2tableId = new HashMap<>();

    // record "eq" between different tables
    private List<Pair<String, String>> joinConditions = new ArrayList<>();

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
        visit(ctx.forClause());
        if (ctx.whereClause() != null) {
            visit(ctx.whereClause());
        }
        return visit(ctx.returnClause());
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
        for (int i = 0; i < ctx.var().size(); ++i) {
            String var = ctx.var().get(i).getText();
            String range = ctx.xq().get(i).getText();

            int id = -1;
            if (range.startsWith("doc")) {
                // create a new XQuery Table
                id = tables.size();
                tables.add(new XQueryTable(id));
            } else {
                // find existent XQuery table
                String base = range.split("/")[0];
                id = var2tableId.get(base);
            }

            // insert variable in tables and var2tableId
            XQueryTable table = tables.get(id);
            table.vars.add(var);
            table.ranges.add(range);
            var2tableId.put(var, id);
        }
        return "";
    }



    @Override
    public String visitReturnClause(XQueryParser.ReturnClauseContext ctx) {
        //
        // TODO(handle independent tables): 处理没有join的table
        //

        String query = "";

        String forString = "for $tuple in ";
        JoinPlanner jp = new JoinPlanner(tables, var2tableId, joinConditions);
        forString += jp.joinTables();

        // TODO(handle independent tables): 处理没有join的table中的变量
        String returnString = ctx.getText();
        returnString = returnString.replace("return", "return\n");
        returnString = returnString.replaceAll("\\$([A-Za-z0-9_]+)", "\\$tuple/$1/*");
        returnString = returnString.replaceAll(",", ",\n");

        query += forString + "\n" + returnString;
        return query;
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
        visit(ctx.cond());
        return "";
    }

    @Override
    public String visitCondEqual(XQueryParser.CondEqualContext ctx) {
        String lhs = ctx.xq(0).getText();
        String rhs = ctx.xq(1).getText();

        if (lhs.startsWith("$") && rhs.startsWith("$") && var2tableId.get(lhs) != var2tableId.get(rhs)) {
            // two variables are from two different tables
            joinConditions.add(new Pair<>(lhs, rhs));
        }
        // either two variables are in same table or one is constant
        else if (lhs.startsWith("$")) {
            int id = var2tableId.get(lhs);
            tables.get(id).conditions.add(lhs + " eq " + rhs);
        } else if (rhs.startsWith("$")) {
            int id = var2tableId.get(rhs);
            tables.get(id).conditions.add(rhs + " eq " + lhs);
        }
        return super.visitCondEqual(ctx);
    }

    @Override
    public String visitCondAnd(XQueryParser.CondAndContext ctx) {
        visit(ctx.cond(0));
        visit(ctx.cond(1));
        return "";
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
