package main.java;

import main.antlr.XQueryBaseVisitor;
import main.antlr.XQueryParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

public class XQueryRewriteVisitor extends XQueryBaseVisitor<String> {

    // All XQuery tables.
    private List<XQueryTable> tables = new ArrayList<>();

    // Map from variable name to index of corresponding XQuery tables.
    private Map<String, Integer> var2tableId = new HashMap<>();

    // "eq" relationship between different XQuery tables.
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

            // 1. Find the id of XQuery table this variable belongs to.
            int id = -1;
            if (range.startsWith("doc")) {
                // create a new XQuery Table for new independent variable
                id = tables.size();
                tables.add(new XQueryTable(id));
            } else {
                // find existent XQuery table by the independent variable it references
                String baseVar = range.split("/")[0];
                id = var2tableId.get(baseVar);
            }

            // 2. Insert variable in corresponding XQuery table and update map from variable to table id.
            XQueryTable table = tables.get(id);
            table.addVariable(var, range);
            var2tableId.put(var, id);
        }
        return "";
    }


    @Override
    public String visitReturnClause(XQueryParser.ReturnClauseContext ctx) {
        String query = "";

        // for clause
        String forClause = "for $tuple in ";
        JoinPlanner jp = new JoinPlanner(tables, joinConditions);
        forClause += jp.joinTables();

        // return clause
        String returnClause = ctx.getText();
        returnClause = returnClause.replace("return", "return\n");
        returnClause = returnClause.replaceAll("\\$([A-Za-z0-9_]+)", "\\$tuple/$1/*");
        returnClause = returnClause.replaceAll(",", ",\n");

        query += forClause + "\n" + returnClause;
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
        // Get left and right parameters in expression "lhs eq rhs".
        String lhs = ctx.xq(0).getText();
        String rhs = ctx.xq(1).getText();

        if (lhs.startsWith("$") && rhs.startsWith("$") && !var2tableId.get(lhs).equals(var2tableId.get(rhs))) {
            // Case 1: parameters are two variables from two different tables (join between tables).
            joinConditions.add(new Pair<>(lhs, rhs));
        }
        else {
            // Case 2: either both parameters are variables from same table or one is a constant.
            if (lhs.startsWith("$")) {
                int id = var2tableId.get(lhs);
                tables.get(id).addCondition(lhs + " eq " + rhs);
            } else if (rhs.startsWith("$")) {
                int id = var2tableId.get(rhs);
                tables.get(id).addCondition(rhs + " eq " + lhs);
            }
        }
        return "";
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
