package main.java;

import java.util.*;

import javafx.util.Pair;

/**
 * Join plan will be a binary tree.
 * Each node corresponds to a XQuery table or an intermediate table.
 * To make things easier, we have either tableId >= 0 (XQuery table) or left != null && right != null (intermediate table).
 */
class Node {
    int tableId;
    Node left, right;

    Set<String> vars;
}

public class JoinPlanner {
    private List<XQueryTable> tables;
    private Map<String, Integer> var2tableId;
    private List<Pair<String, String>> joinConditions;

    public JoinPlanner(List<XQueryTable> tables, Map<String, Integer> var2tableId, List<Pair<String, String>> joinConditions) {
        this.tables = tables;
        this.var2tableId = var2tableId;
        this.joinConditions = new ArrayList<>(joinConditions);
    }

    public String joinTables() {
        Node root = makePlan();
        return joinTablesImpl(root);
    }


    // makePlan creates a binary tree.
    private Node makePlan() {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < tables.size(); ++i) {
            Node node = new Node();
            node.tableId = i;
            node.vars = new HashSet<>(tables.get(i).vars);
            nodes.add(node);
        }
        Node root = new Node();
        root.tableId = -1;
        root.left = nodes.get(0);
        root.right = nodes.get(1);

        for (int i = 2; i < nodes.size(); ++i) {
            Node temp = new Node();
            temp.tableId = -1;
            temp.left = root;
            temp.right = nodes.get(i);
            root = temp;
        }
        return root;
    }

    private String joinTablesImpl(Node root) {
        if (root.tableId >= 0) {
            System.out.println(root.tableId);
            return "(" + tables.get(root.tableId).toString() + ")";
        } else {
            // left and right join tuples
            String lhs = joinTablesImpl(root.left);
            String rhs = joinTablesImpl(root.right);

            // left and right attributes
            List<String> v1 = new ArrayList<>();
            List<String> v2 = new ArrayList<>();

            List<Pair<String, String>> backup = new ArrayList<>(joinConditions);
            for (Pair<String, String> condition : backup) {
                String a1 = condition.getKey();
                String a2 = condition.getValue();

                if (root.left.vars.contains(a1) && root.right.vars.contains(a2)) {
                    v1.add(a1);
                    v2.add(a2);
                    joinConditions.remove(condition);
                } else if (root.left.vars.contains(a2) && root.right.vars.contains(a1)) {
                    v1.add(a2);
                    v2.add(a1);
                    joinConditions.remove(condition);
                }
            }

            root.vars = new HashSet<>(root.left.vars);
            root.vars.addAll(root.right.vars);

            // find variables to join on
            return "join (\n" + lhs + ",\n" + rhs  + ",\n" + v1 + ",\n" + v2 + "\n)";
        }
    }
}
