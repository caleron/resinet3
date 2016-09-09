package com.resinet.algorithms;

import com.resinet.model.CalculationParams;
import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;

import java.math.BigDecimal;
import java.util.ArrayList;

public class NetReducer {
    private CalculationParams params;
    private ArrayList<NodePoint> nodes;
    private ArrayList<EdgeLine> edges;

    public static CalculationParams reduce(CalculationParams params) {
        if (params == null)
            return null;

        NetReducer netReducer = new NetReducer(params);

        return netReducer.reduce();
    }

    private NetReducer(CalculationParams params) {
        this.nodes = new ArrayList<>(params.graphNodes);
        this.edges = new ArrayList<>(params.graphEdges);
        this.params = params;

        for (int i = 0; i < nodes.size(); i++) {
            NodePoint node = nodes.get(i);
            if (params.sameReliabilityMode) {
                if (params.differentTerminalNodeReliability && node.c_node) {
                    node.prob = params.terminalNodeValue;
                } else {
                    node.prob = params.nodeValue;
                }
            } else {
                node.prob = params.nodeProbabilities[i];
            }
        }

        for (int i = 0; i < edges.size(); i++) {
            EdgeLine edge = edges.get(i);
            if (params.sameReliabilityMode) {
                edge.prob = params.edgeValue;
            } else {
                edge.prob = params.edgeProbabilities[i];
            }
        }
    }

    private CalculationParams reduce() {
        boolean reduced = true;

        while (reduced) {
            reduced = reduceChain();

            if (reduced)
                continue;

            reduced = reduceParallel();

            if (reduced)
                continue;

            reduced = reduceUseless();
        }

        return buildOutput();
    }

    private boolean reduceChain() {
        ArrayList<EdgeLine> adjacentEdges = new ArrayList<>();
        NodePoint foundNode = null;
        for (NodePoint node : nodes) {
            if (node.c_node) {
                continue;
            }
            adjacentEdges.clear();

            for (EdgeLine edge : edges) {
                if (edge.endNode == node || edge.startNode == node) {
                    adjacentEdges.add(edge);
                }
            }

            if (adjacentEdges.size() == 2) {
                foundNode = node;
                break;
            }
        }
        if (foundNode == null) {
            return false;
        }

        NodePoint otherNode1;
        NodePoint otherNode2;

        if (adjacentEdges.get(0).startNode == foundNode) {
            otherNode1 = adjacentEdges.get(0).endNode;
        } else {
            otherNode1 = adjacentEdges.get(0).startNode;
        }

        if (adjacentEdges.get(1).startNode == foundNode) {
            otherNode2 = adjacentEdges.get(1).endNode;
        } else {
            otherNode2 = adjacentEdges.get(1).startNode;
        }

        nodes.remove(foundNode);
        edges.removeAll(adjacentEdges);


        EdgeLine newEdge = new EdgeLine(otherNode1, otherNode2);
        newEdge.prob = foundNode.prob.multiply(adjacentEdges.get(0).prob).multiply(adjacentEdges.get(1).prob);
        edges.add(newEdge);

        return true;
    }

    private boolean reduceUseless() {
        ArrayList<EdgeLine> adjacentEdges = new ArrayList<>();
        NodePoint foundNode = null;
        for (NodePoint node : nodes) {
            if (node.c_node) {
                continue;
            }
            adjacentEdges.clear();

            for (EdgeLine edge : edges) {
                if (edge.endNode == node || edge.startNode == node) {
                    adjacentEdges.add(edge);
                }
            }

            if (adjacentEdges.size() == 1) {
                foundNode = node;
                break;
            }
        }
        if (foundNode == null) {
            return false;
        }

        nodes.remove(foundNode);
        edges.removeAll(adjacentEdges);
        return true;
    }

    private boolean reduceParallel() {
        EdgeLine foundEdge = null;
        EdgeLine modEdge = null;

        for (EdgeLine edge1 : edges) {
            for (EdgeLine edge : edges) {
                if (edge == edge1) {
                    continue;
                }

                if ((edge.startNode.equals(edge1.startNode) && edge.endNode.equals(edge1.endNode)
                        || (edge.startNode.equals(edge1.endNode) && edge.endNode.equals(edge1.startNode)))) {
                    foundEdge = edge;
                    modEdge = edge1;
                    break;
                }
            }
        }

        if (foundEdge == null) {
            return false;
        }

        edges.remove(foundEdge);

        BigDecimal failProb1 = BigDecimal.ONE.subtract(foundEdge.prob);
        BigDecimal failProb2 = BigDecimal.ONE.subtract(modEdge.prob);

        modEdge.prob = BigDecimal.ONE.subtract(failProb1.multiply(failProb2));
        return true;
    }

    private CalculationParams buildOutput() {
        params.setGraphLists(nodes, edges);
        params.sameReliabilityMode = false;
        params.probabilitiesLoaded = true;

        BigDecimal[] edgeProbabilities = new BigDecimal[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            edgeProbabilities[i] = edges.get(i).prob;
        }

        BigDecimal[] nodeProbabilities = new BigDecimal[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            nodeProbabilities[i] = nodes.get(i).prob;
        }

        params.setSingleReliabilityParams(edgeProbabilities, nodeProbabilities);
        return params;
    }
}
