package com.resinet.algorithms;/* com.resinet.algorithms.Con_check.java */

import com.resinet.model.Edge;
import com.resinet.model.Graph;
import com.resinet.model.Node;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;

public class Con_check {
    /*Vom Knoten "node" aus wird eine Tiefensuche durchgefuehrt. Der Rueckgabewert ist true, 
    falls in der Menge der erreichbaren Knoten mindestens ein Konnektionsknoten enthalten ist. 
    boolean c_node_reached prueft, ob ein Konnektionsknoten erreicht wurde.*/
    private static boolean depth_search(Graph g, Node node) {
        node.marked = true;
        //boolean any_other_c_node = false;
        boolean c_node_reached = false;
        // wenn jeder Knoten von Knoten "node" aus erreichbar ist, ist der com.resinet.model.Graph auch zusammenhaengend
        MyIterator it = node.node_edge.iterator();
        while (it.hasNext()) {
            Node next = null;
            Edge edge = (Edge) it.next();
            if (!edge.marked) //Um eine Kante bei der Tiefensuche auslassen zu koennen.
            {
                try {
                    if (!edge.left_node.equals(node))
                        next = edge.left_node;
                    else
                        next = edge.right_node;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (edge == null)
                        System.out.println("edge is null");
                    else
                        System.out.println("left_node is null " + edge.right_node.node_no);
                }
                if (!next.marked) {
                    if (!c_node_reached)
                        c_node_reached = depth_search(g, next);
                    else
                        depth_search(g, next);
                }
            }
        }
        //|| any_other_c_node==true)
        return c_node_reached || node.c_node;
    }


    private static void depth_search(Graph g, int i)
    // von Knoten i aus wird eine Tiefensuche durchgefuehrt.
    {
        int j;
        MyList nd;

        nd = g.nodeList;

        Node node = (Node) nd.get(i);
        node.marked = true;

        // wenn jeder Knoten von Knoten i aus erreichbar ist, ist der com.resinet.model.Graph auch zusammenhaengend
        MyIterator it = node.node_edge.iterator();

        while (it.hasNext()) {
            int n = 0;
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

    private static void reset_mark(Graph g, boolean without_edges) {
        MyIterator it = g.getNodelist().iterator();

        while (it.hasNext()) {
            Node node = (Node) it.next();
            node.marked = false;
        }

        if (!without_edges) {
            it = g.getEdgelist().iterator();
            while (it.hasNext()) {
                Edge edge = (Edge) it.next();
                edge.marked = false;
            }
        }

    }

    private static void reset_mark(Graph g) {
        MyIterator it = g.nodeList.iterator();

        while (it.hasNext()) {
            Node node = (Node) it.next();
            node.marked = false;
        }
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

    /* Fuehrt Tiefensuche durch und ignoriert dabei die Kante e sowie zu e korrespondierende Mehrfachkanten. (2.3.) (10.3.)
    Rueckgabewert -1: com.resinet.model.Graph wuerde nach dem Entfernen von e zusammenhaengend bleiben (Bizusammenhang).
    Rueckgabewert 1: com.resinet.model.Graph wuerde nach dem Entfernen von e unzusammenhaengend, und der right-Teil enthaelt keine Konnektionsknoten.
    Rueckgabewert 2: com.resinet.model.Graph wuerde nach dem Entfernen von e unzusammenhaengend, und der left-Teil enthaelt keine Konnektionsknoten.
    Rueckgabewert 0: com.resinet.model.Graph wuerde nach dem Entfernen von e unzusammenhaengend, der right-Teil und der left-Teil enthalten Konnektionsknoten.*/
    public static int check(Graph g, Edge e) {
        MyList nd;
        boolean right_part_contains_c_node;
        boolean left_part_contains_c_node;
        int edge_cnt = 0;
        nd = g.getNodelist();
        reset_mark(g, false);
        Node n1 = e.right_node;
        Node n2 = e.left_node;

	/*Durchlaufe alle Kanten des "rechten" Knoten.*/
        MyIterator it = n1.node_edge.iterator();//Iterator ueber Kanten von n1
        while (it.hasNext()) {
            Edge e1 = (Edge) it.next(); //naechste Kante
        /*Falls die Kante in der Liste der Kanten des "linken" Knoten ist,
        markiere sie. Das ist mindestens bei einer Kante (e) der Fall. Bei Mehrfachkanten werden demnach mehrere Kanten ignoriert.*/
            if (n2.node_edge.contains(e1)) {
                e1.marked = true; //Kante soll bei der Tiefensuche ausgelassen werden.
                edge_cnt = edge_cnt + 1;
            }
        }

        right_part_contains_c_node = depth_search(g, n1);//, false);

        if (n2.marked) //Der Knoten wurde erreicht.
            return (-1);
        if (!right_part_contains_c_node)
            return 1;
        reset_mark(g, true);
        left_part_contains_c_node = depth_search(g, n2);//, false);
        if (!left_part_contains_c_node)
            return 2;
        if (edge_cnt > 1)
            return (-1);
        else
            return 0;
    }
}


