package com.resinet.model;

import java.util.ArrayList;

public class GraphWrapper {
    public final ArrayList<NodePoint> nodes;
    public final ArrayList<EdgeLine> edges;

    public GraphWrapper() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public void addNode(NodePoint np) {
        nodes.add(np);
    }

    public void addEdge(NodePoint node1, NodePoint node2) {
        edges.add(new EdgeLine(node1, node2));
    }
}
