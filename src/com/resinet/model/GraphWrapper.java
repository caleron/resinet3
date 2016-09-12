package com.resinet.model;

import java.util.ArrayList;

/**
 * Wrapper für eine Menge von Knoten und eine Menge von Kanten aus dem Graphen.
 */
public class GraphWrapper {
    public final ArrayList<NodePoint> nodes;
    public final ArrayList<EdgeLine> edges;

    public GraphWrapper() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    /**
     * Fügt einen Knoten hinzu.
     *
     * @param np Der neue Knoten
     */
    public void addNode(NodePoint np) {
        nodes.add(np);
    }

    /**
     * Fügt eine Kante hinzu.
     *
     * @param node1 Der Startknoten der Kante
     * @param node2 Der Endknoten der Kante
     */
    public void addEdge(NodePoint node1, NodePoint node2) {
        if (!node1.equals(node2)) {
            edges.add(new EdgeLine(node1, node2));
        }
    }

    /**
     * Prüft, ob eine Kante mit den beiden Endpunkten bereits enthalten ist
     *
     * @param node1 Endpunkt 1
     * @param node2 Endpunkt 2
     * @return true, wenn eine Kante mit den beiden Endpunkten existiert, sonst false
     */
    public boolean hasEdge(NodePoint node1, NodePoint node2) {
        for (EdgeLine edge : edges) {
            if ((edge.startNode.equals(node1) && edge.endNode.equals(node2)
                    || (edge.startNode.equals(node2) && edge.endNode.equals(node1)))) {
                return true;
            }
        }
        return false;
    }
}
