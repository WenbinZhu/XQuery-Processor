package main.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Each independent variable(iterating on doc) has a XQueryTable.
 * Variables referencing this independent variable will be grouped in corresponding XQuery table.
 */
class XQueryTable {
    public int index;

    public List<String> vars;           // variables
    public List<String> ranges;         // range of each variable
    public List<String> conditions;     // conditions only about this table (where $aj eq "John")

    public XQueryTable(int index) {
        this.index = index;
        vars = new ArrayList<>();
        ranges = new ArrayList<>();
        conditions = new ArrayList<>();
    }

    public String toString() {
        String s = "";

        // for clause
        s += "for " + vars.get(0) + " in " + ranges.get(0);
        for (int i = 1; i < vars.size(); ++i) {
            s += ",\n" + vars.get(i) + " in " + ranges.get(i);
        }
        s += "\n";

        // where clause
        if (conditions.size() > 0) {
            s += "where " + conditions.get(0);
            for (int i = 1; i < conditions.size(); ++i) {
                s += ",\n" + conditions.get(i);
            }
            s += "\n";
        }

        // return clause
        s  += "return <tuple>\n";
        for (int i = 0; i < vars.size(); ++i) {
            String var = vars.get(i);
            s += "<" + var.substring(1) + ">{" + var + "}</" + var.substring(1) + ">\n";
        }
        s += "</tuple>";
        return s;
    }
}