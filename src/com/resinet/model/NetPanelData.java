package com.resinet.model;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Verwaltet die Knoten- und Kantenmengen und deren Zustände, die im NetPanel dargestellt werden. Aktionen wie Knoten
 * hinzufügen/löschen können ausgeführt werden. Alle Aktionen werden auch in einem UndoManager registriert und können
 * auch rückgängig gemacht werden.
 */
public class NetPanelData implements Serializable {
    private static final long serialVersionUID = -293719411015421415L;

    private final ArrayList<NodePoint> nodes;
    private final ArrayList<EdgeLine> edges;

    private final UndoManager undoManager;

    public NetPanelData() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        undoManager = new UndoManager();
    }

    /**
     * Fügt einen Knoten an dem angegebenen Punkt hinzu
     *
     * @param x            X-Koordinate
     * @param y            Y-Koordinate
     * @param terminalNode Ob der Knoten ein Terminalknoten ist
     */
    public void addNode(int x, int y, boolean terminalNode) {
        NodePoint newNode = new NodePoint(x, y, terminalNode);
        AddOrRemoveAction action = new AddOrRemoveAction(true, newNode);
        action.redo();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt einen Knoten.
     *
     * @param node Der zu entfernende Knoten
     */
    public void removeNode(NodePoint node) {
        ArrayList<NodePoint> nodeWrapper = new ArrayList<>();
        ArrayList<EdgeLine> removeEdges = new ArrayList<>();
        nodeWrapper.add(node);

        //Anliegende Kanten sammeln
        for (EdgeLine edge : edges) {
            if (node.equals(edge.startNode) || node.equals(edge.endNode)) {
                removeEdges.add(edge);
            }
        }

        AddOrRemoveAction action = new AddOrRemoveAction(false, nodeWrapper, removeEdges);
        action.redo();
        undoManager.addEdit(action);
    }

    /**
     * Fügt eine Kante hinzu.
     *
     * @param startNode Der Startknoten
     * @param endNode   Der Endknoten
     */
    public void addEdge(NodePoint startNode, NodePoint endNode) {
        EdgeLine newEdge = new EdgeLine(startNode, endNode);
        AddOrRemoveAction action = new AddOrRemoveAction(true, newEdge);
        action.redo();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt eine Kante.
     *
     * @param edge Die zu entfernende Kante.
     */
    public void removeEdge(EdgeLine edge) {
        AddOrRemoveAction action = new AddOrRemoveAction(false, edge);
        action.redo();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt eine Menge von Knoten und alle anliegenden Kanten.
     *
     * @param nodes Die Menge von zu entfernenden Knoten
     */
    public void removeNodes(ArrayList<NodePoint> nodes) {
        ArrayList<EdgeLine> removeEdges = new ArrayList<>();

        //Anliegende Kanten sammeln
        for (EdgeLine edge : edges) {
            if (nodes.contains(edge.startNode) || nodes.contains(edge.endNode)) {
                removeEdges.add(edge);
            }
        }

        AddOrRemoveAction action = new AddOrRemoveAction(false, nodes, removeEdges);
        action.redo();
        undoManager.addEdit(action);
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu.
     *
     * @param nodes Die Knotenmenge
     * @param edges Die Kantenmenge
     */
    public void addNodesAndEdges(ArrayList<NodePoint> nodes, ArrayList<EdgeLine> edges) {
        AddOrRemoveAction action = new AddOrRemoveAction(false, nodes, edges);
        action.redo();
        undoManager.addEdit(action);
    }

    /**
     * Bewegt eine Menge von Knoten um die angegebenen Koordinaten
     *
     * @param nodes  Die Menge der zu bewegenden Knoten
     * @param amount Die Dimension, um die die Knoten verschoben werden sollen
     */
    public void moveNodes(ArrayList<NodePoint> nodes, Dimension amount) {
        MoveAction action = new MoveAction(nodes, amount);
        action.redo();
        undoManager.addEdit(action);
    }

    /**
     * Macht die letzte Aktion rückgängig, falls möglich.
     */
    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    /**
     * Macht das letzte Rückgängigmachen rückgängig, falls möglich.
     */
    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }


    public ArrayList<NodePoint> getNodes() {
        return nodes;
    }

    public ArrayList<EdgeLine> getEdges() {
        return edges;
    }

    /**
     * Beschreibt alle Aktionen, bei denen Knoten und/oder Kanten hinzugefügt oder gelöscht werden.
     */
    class AddOrRemoveAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = 7183317665439824767L;
        ArrayList<NodePoint> affectedNodes;
        ArrayList<EdgeLine> affectedEdges;
        boolean isAddAction;

        public AddOrRemoveAction(boolean isAddAction, ArrayList<NodePoint> addedNodes, ArrayList<EdgeLine> addedEdges) {
            this.isAddAction = isAddAction;
            this.affectedNodes = addedNodes;
            this.affectedEdges = addedEdges;
        }

        public AddOrRemoveAction(boolean isAddAction, NodePoint node) {
            this.isAddAction = isAddAction;
            affectedNodes = new ArrayList<>();
            affectedNodes.add(node);
        }

        public AddOrRemoveAction(boolean isAddAction, EdgeLine edge) {
            this.isAddAction = isAddAction;
            affectedEdges = new ArrayList<>();
            affectedEdges.add(edge);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            if (affectedNodes != null) {
                if (isAddAction) {
                    nodes.addAll(affectedNodes);
                } else {
                    nodes.removeAll(affectedNodes);
                }
            }
            if (affectedEdges != null) {
                if (isAddAction) {
                    edges.addAll(affectedEdges);
                } else {
                    edges.removeAll(affectedEdges);
                }
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (affectedNodes != null) {
                if (isAddAction) {
                    nodes.removeAll(affectedNodes);
                } else {
                    nodes.addAll(affectedNodes);
                }
            }
            if (affectedEdges != null) {
                if (isAddAction) {
                    edges.removeAll(affectedEdges);
                } else {
                    edges.addAll(affectedEdges);
                }
            }
        }
    }

    /**
     * Beschreibt eine Aktion, bei der eine Menge von Knoten bewegt wird.
     */
    class MoveAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = 37760421287789359L;

        ArrayList<NodePoint> movedNodes;
        Dimension amount;

        public MoveAction(ArrayList<NodePoint> nodes, Dimension amount) {
            movedNodes = nodes;
            this.amount = amount;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            if (movedNodes != null) {
                for (NodePoint node : movedNodes) {
                    node.x += amount.getWidth();
                    node.y += amount.getHeight();
                }
                edges.forEach(EdgeLine::refresh);
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (movedNodes != null) {
                for (NodePoint node : movedNodes) {
                    node.x -= amount.getWidth();
                    node.y -= amount.getHeight();
                }
                edges.forEach(EdgeLine::refresh);
            }
        }
    }
}
