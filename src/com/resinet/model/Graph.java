package com.resinet.model;/* com.resinet.model.Graph.java */

/* Auf den Klassen "com.resinet.model.Node" und "Edge" wird ein Zufallsgraph generiert. Mit einer Zufallszahl
werden die Konnektionsknoten festgelegt. Anschliessend werden die K-Baeume vom com.resinet.model.Graph abgeleitet.
Durch die K-Baeume erhaelt man die Minimalkombinationen.
*/
import java.io.Serializable;
import java.util.ArrayList;

public class Graph implements Serializable {

    public ArrayList<Node> nodeList;
    public ArrayList<Edge> edgeList;


    public Object clone() throws CloneNotSupportedException {
        Graph graph = (Graph) super.clone();
        graph.edgeList = (ArrayList<Edge>) edgeList.clone();
        graph.nodeList = (ArrayList<Node>) nodeList.clone();
        return graph;
    }

    public void delete_Edge(Edge edge) {
        Node left = edge.left_node;
        Node right = edge.right_node;

        left.delete_Edge(edge);
        right.delete_Edge(edge);
        edgeList.remove(edge);
    }


    public void add_Edge(Edge edge) {
        if (!edgeList.contains(edge)) {
            edgeList.add(edge);

            Node left = edge.left_node;
            Node right = edge.right_node;

            left.add_Edge(edge);
            right.add_Edge(edge);
        }
    }

    public ArrayList<Node> getNodelist() {
        return nodeList;
    }

    public ArrayList<Edge> getEdgelist() {
        return edgeList;
    }

    public Graph(ArrayList<Node> nodeList, ArrayList<Edge> edgeList) {
        this.nodeList = nodeList;
        this.edgeList = edgeList;
    }

}











