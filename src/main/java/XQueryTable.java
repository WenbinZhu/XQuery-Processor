package main.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Each independent variable(iterating on doc) has a XQueryTable.
 * Variables referencing this independent variable will be grouped in corresponding XQuery table.
 */
class XQueryTable {
    public int index;

    public List<String> vars;           // variable names
    public List<String> ranges;         // range of each variable
    public List<String> conditions;     // conditions only for this table (e.g where $aj eq "John")

    public XQueryTable(int index) {
        this.index = index;
        vars = new ArrayList<>();
        ranges = new ArrayList<>();
        conditions = new ArrayList<>();
    }

    public void addVariable(String var, String range) {
        vars.add(var);
        ranges.add(range);
    }

    public void addCondition(String condition) {
        conditions.add(condition);
    }

    /**
     * Convert the XQuery table into string representation.
     * For example, the for loop below contains two variables $b and $tb in same XQuery table.
     *
     * for $b in doc("input")/book,
     *       $tb in $b/title,
     *   ...
     *
     * Then toString() method will return:
     *
     *  for $b in doc("input")/book,
     *      $tb in $b/title
     *  return <tuple> <b> {$b} </b> <tb> {$tb} </tb> </tuple>
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // for clause
        sb.append("for " + vars.get(0) + " in " + ranges.get(0));
        for (int i = 1; i < vars.size(); ++i) {
            sb.append(",\n\t" + vars.get(i) + " in " + ranges.get(i));
        }
        sb.append("\n");

        // where clause
        if (conditions.size() > 0) {
            sb.append("where " + conditions.get(0));
            for (int i = 1; i < conditions.size(); ++i) {
                sb.append(",\n" + conditions.get(i));
            }
            sb.append("\n");
        }

        // return clause
        sb.append("return <tuple>\n");

        for (int i = 0; i < vars.size(); ++i) {
            String varText = vars.get(i).substring(1);  // strip '$'
            sb.append("\t<" + varText + ">{" + vars.get(i) + "}</" + varText + ">");
            if (i < vars.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("</tuple>");
        return sb.toString();
    }
}