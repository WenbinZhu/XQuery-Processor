package main.java;

import java.util.*;

import javafx.util.Pair;


public class JoinPlanner {
    /**
     * A tree node in the binary tree of join plan.
     * It corresponds to either a XQuery table (with tableId >= 0), or an intermediate table.
     */
    class TableNode {
        int tableId;
        TableNode left, right;
        Set<String> vars = new HashSet<>(); // variables in the table
    }

    private List<XQueryTable> tables;
    private List<Pair<String, String>> joinConditions;


    public JoinPlanner(List<XQueryTable> tables, List<Pair<String, String>> joinConditions) {
        this.tables = tables;
        this.joinConditions = removeDuplicateConditions(joinConditions);
    }

    public String joinTables() {
        TableNode root = makeJoinPlan();
        return joinTablesImpl(root);
    }

    /**
     * Remove duplicate join conditions with unio-find set.
     * @param joinConditions
     * @return
     */
    private List<Pair<String, String>> removeDuplicateConditions(List<Pair<String, String>> joinConditions) {
        List<Pair<String, String>> result = new ArrayList<>();

        // 1. Give each variable a unique index.
        Map<String, Integer> var2idx = new HashMap<>();
        int idx = 0;
        for (Pair<String, String> condition : joinConditions) {
            String v1 = condition.getKey(), v2 = condition.getValue();
            if (!var2idx.containsKey(v1)) {
                var2idx.put(v1, idx++);
            }
            if (!var2idx.containsKey(v2)) {
                var2idx.put(v2, idx++);
            }
        }
        // 2. initialize union-find set.
        int[] labels = new int[idx];
        for (int i = 0; i < labels.length; ++i)
            labels[i] = i;

        // 3. Put only unique condition in result.
        for (Pair<String, String> condition : joinConditions) {
            String v1 = condition.getKey(), v2 = condition.getValue();
            int x = var2idx.get(v1), y = var2idx.get(v2);
            if (findLabel(labels, x) != findLabel(labels, y)) {
                unionLables(labels, x, y);
                result.add(new Pair<>(v1, v2));
            }
        }
        return result;
    }

    private int findLabel(int[] labels, int x) {
        if (labels[x] != x) {
            labels[x] = findLabel(labels, labels[x]);
        }
        return labels[x];
    }

    private void unionLables(int[] labels, int x, int y) {
        labels[findLabel(labels, x)] = findLabel(labels, y);
    }

    /**
     * Create the order to join tables (represented as a binary tree).
     * @return root of binary tree
     */
    private TableNode makeJoinPlan() {
        List<TableNode> nodes = new ArrayList<>();
        for (int i = 0; i < tables.size(); ++i) {
            TableNode node = new TableNode();
            node.tableId = i;
            node.vars = new HashSet<>(tables.get(i).vars);
            nodes.add(node);
        }
        TableNode root = new TableNode();
        root.tableId = -1;
        root.left = nodes.get(0);
        root.right = nodes.get(1);

        for (int i = 2; i < nodes.size(); ++i) {
            TableNode temp = new TableNode();
            temp.tableId = -1;
            temp.left = root;
            temp.right = nodes.get(i);
            root = temp;
        }
        return root;
    }

    /**
     * Concatenate final for clause recursively.
     * @param root current root node join plan binary tree
     * @return current string representation of node and its subtree
     */
    private String joinTablesImpl(TableNode root) {
        if (root.tableId >= 0) {
            return tables.get(root.tableId).toString();
        } else {
            // Left and right tables.
            String lhs = joinTablesImpl(root.left);
            String rhs = joinTablesImpl(root.right);

            // Left and right attributes.
            List<String> v1 = new ArrayList<>();
            List<String> v2 = new ArrayList<>();

            List<Pair<String, String>> backup = new ArrayList<>(joinConditions);
            for (Pair<String, String> condition : backup) {
                // For each condition "a1 eq a2", check if a1 and a2 respectively belong to
                // two tables in this step.
                // If true, put the pair in output attributes and remove this condition.
                String a1 = condition.getKey(), a2 = condition.getValue();

                if (root.left.vars.contains(a1) && root.right.vars.contains(a2)) {
                    v1.add(a1.substring(1));
                    v2.add(a2.substring(1));
                    joinConditions.remove(condition);
                } else if (root.left.vars.contains(a2) && root.right.vars.contains(a1)) {
                    v1.add(a2.substring(1));
                    v2.add(a1.substring(1));
                    joinConditions.remove(condition);
                }
            }

            root.vars = new HashSet<>(root.left.vars);
            root.vars.addAll(root.right.vars);

            return "join (\n" + lhs + ",\n" + rhs + ",\n" + v1 + ",\n" + v2 + "\n)";
        }
    }
}
