package com.resinet.algorithms;/* com.resinet.algorithms.Con_check.java */

import com.resinet.model.Edge;
import com.resinet.model.Graph;
import com.resinet.model.Node;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;

public class Con_check {


    private static void depth_search(Graph g, int i)
    // von Knoten i aus wird eine Tiefensuche durchgefuehrt.
    {
        MyList nd;

        nd = g.nodeList;

        Node node = (Node) nd.get(i);
        node.marked = true;

        // wenn jeder Knoten von Knoten i aus erreichbar ist, ist der com.resinet.model.Graph auch zusammenhaengend
        MyIterator it = node.node_edge.iterator();

        while (it.hasNext()) {
            int n;
            Edge edge = (Edge) it.next();

            if (edge.left_node.node_no != i)
                n = edge.left_node.node_no;
            else
                n = edge.right_node.node_no;
            Node next_node = (Node) nd.get(n);
            if (!next_node.marked)
                depth_search(g, n);

        }
    }

    private static void reset_mark(Graph g) {
        MyIterator it = g.nodeList.iterator();

        while (it.hasNext()) {
            Node node = (Node) it.next();
            node.marked = false;
        }
    }

    public static boolean isConnected(Graph g) {
        return check(g) == -1;
    }

    public static int check(Graph g) {
        MyList nd;
        nd = g.nodeList;

        reset_mark(g);

        depth_search(g, 0);
        //wir fangen immer mit dem ersten Knoten 0 an

        MyIterator it = nd.iterator();
        while (it.hasNext()) {
            Node node = (Node) it.next();
            if (!node.marked) {
                return (nd.indexOf(node));
            }
        }
        return (-1);
    }

}


