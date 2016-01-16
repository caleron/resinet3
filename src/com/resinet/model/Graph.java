package com.resinet.model;/* com.resinet.model.Graph.java */

/* Auf den Klassen "com.resinet.model.Node" und "Edge" wird ein Zufallsgraph generiert. Mit einer Zufallszahl
werden die Konnektionsknoten festgelegt. Anschliessend werden die K-Baeume vom com.resinet.model.Graph abgeleitet.
Durch die K-Baeume erhaelt man die Minimalkombinationen.
*/

import com.resinet.util.MyList;

import java.io.Serializable;

public class Graph implements Serializable {

    public MyList nodeList;
    public MyList edgeList;


    public Object clone() throws CloneNotSupportedException {
        Graph graph = (Graph) super.clone();
        graph.edgeList = (MyList) edgeList.clone();
        graph.nodeList = (MyList) nodeList.clone();
        return graph;
    }

    public void delete_Edge(Edge b) {
        Node left = b.left_node;
        Node right = b.right_node;

        left.delete_Edge(b);
        right.delete_Edge(b);
        edgeList.remove(b);
    }


    public void add_Edge(Edge b) {
        if (!edgeList.contains(b)) {
            edgeList.add(b);

            Node left = b.left_node;
            Node right = b.right_node;

            left.add_Edge(b);
            right.add_Edge(b);
        }
    }

    public MyList getNodelist() {
        return nodeList;
    }

    public MyList getEdgelist() {
        return edgeList;
    }

    public Graph(MyList nodeList, MyList edgeList) {
        this.nodeList = nodeList;
        this.edgeList = edgeList;
    }

}











