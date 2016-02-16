package com.resinet.model;

import com.resinet.util.NodeEdgeWrapper;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Verwaltet die Knoten- und Kantenmengen und deren Zustände, die im NetPanel dargestellt werden. Aktionen wie Knoten
 * hinzufügen/löschen können ausgeführt werden. Alle Aktionen werden auch in einem UndoManager registriert und können
 * auch rückgängig gemacht werden.
 * <p>
 * In Form von Unterklassen werden Aktionensklassen definiert. Jede Aktion hat diese drei Methoden:<ul><li>execute():
 * Führt die Aktion erstmalig aus</li><li>undo(): Macht die Aktion rückgängig</li><li>redo(): Wiederholt die Aktion,
 * wenn diese rückgängig gemacht wurde</li></ul> Zusätzlich hält jede Aktion natürlich alle Daten, die für die Methoden
 * benötigt werden.
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
     * Fügt einen Knoten hinzu
     *
     * @param newNode Der neue Knoten
     */
    public void addNode(NodePoint newNode) {
        AddOrRemoveAction action = new AddOrRemoveAction(true, newNode);
        action.execute();
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
        action.execute();
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
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt eine Kante.
     *
     * @param edge Die zu entfernende Kante.
     */
    public void removeEdge(EdgeLine edge) {
        AddOrRemoveAction action = new AddOrRemoveAction(false, edge);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt eine Menge von Knoten und alle anliegenden Kanten.
     *
     * @param removeNodes Die Menge von zu entfernenden Knoten
     */
    public void removeNodes(ArrayList<NodePoint> removeNodes) {
        ArrayList<EdgeLine> removeEdges = new ArrayList<>();

        //Anliegende Kanten sammeln
        for (EdgeLine edge : edges) {
            if (removeNodes.contains(edge.startNode) || removeNodes.contains(edge.endNode)) {
                removeEdges.add(edge);
            }
        }

        AddOrRemoveAction action = new AddOrRemoveAction(false, removeNodes, removeEdges);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Erstellt ein Objekt mit den ausgewählten Knoten und den Kanten innerhalb der Knotenmenge. Dabei werden die Knoten
     * und Kanten geklont. Die Referenzen der Kanten werden auf die geklonten Knoten gesetzt.
     *
     * @return NodeEdgeWrapper
     */
    public NodeEdgeWrapper getSelectionCopyData() {
        //Diese Liste wird zum Entfernen der Knoten benötigt, falls die Aktion "Ausschneiden" ist
        ArrayList<NodePoint> originalSelectedNodes = new ArrayList<>();
        ArrayList<NodePoint> selectedNodes = new ArrayList<>();
        ArrayList<EdgeLine> selectedEdges = new ArrayList<>();

        //Ausgewählte Knoten sammeln
        for (NodePoint nodePoint : nodes) {
            if (nodePoint.selected) {
                selectedNodes.add(nodePoint);
            }
        }

        originalSelectedNodes.addAll(selectedNodes);

        //Anliegende Kanten klonen
        for (EdgeLine edgeLine : edges) {
            if (selectedNodes.contains(edgeLine.endNode) && selectedNodes.contains(edgeLine.startNode)) {
                selectedEdges.add((EdgeLine) edgeLine.clone());
            }
        }

        //Knoten klonen und dabei Referenzen der geklonten Kanten neu setzen
        for (int i = 0; i < selectedNodes.size(); i++) {
            NodePoint nodePoint = selectedNodes.get(i);

            NodePoint newNode = (NodePoint) nodePoint.clone();
            newNode.selected = false;

            //Referenzen neu setzen
            for (EdgeLine edgeLine : selectedEdges) {
                if (edgeLine.endNode.equals(nodePoint)) {
                    edgeLine.endNode = newNode;
                }
                if (edgeLine.startNode.equals(nodePoint)) {
                    edgeLine.startNode = newNode;
                }
            }
            //Aktuellen Knoten durch geklonten ersetzen
            selectedNodes.set(i, newNode);
        }
        return new NodeEdgeWrapper(originalSelectedNodes, selectedNodes, selectedEdges);
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu.
     *
     * @param nodes Die Knotenmenge
     * @param edges Die Kantenmenge
     */
    public void addNodesAndEdges(List<NodePoint> nodes, List<EdgeLine> edges) {
        AddOrRemoveAction action = new AddOrRemoveAction(true, nodes, edges);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Bewegt eine Menge von Knoten um die angegebenen Koordinaten. Diese Methode sollte nicht aufgerufen werden,
     * während das Verschieben noch im Gange ist.
     *
     * @param moveNodes Die Menge der zu bewegenden Knoten
     * @param amount    Die Dimension, um die die Knoten verschoben werden sollen
     */
    public void moveNodesFinal(ArrayList<NodePoint> moveNodes, Dimension amount) {
        MoveAction action = new MoveAction(moveNodes, amount);
        undoManager.addEdit(action);
    }

    /**
     * Bewegt eine Menge von Knoten um die angegebenen Koordinaten, aber fügt diese Aktion nicht in den UndoManager ein.
     * Diese Methode sollte aufgerufen werden, während das Verschieben am Gange ist.
     *
     * @param nodes  Die Menge der zu bewegenden Knoten
     * @param amount Die Dimension, um die die Knoten verschoben werden sollen
     */
    public void moveNodesNotFinal(ArrayList<NodePoint> nodes, Dimension amount) {
        MoveAction action = new MoveAction(nodes, amount);
        action.execute();
    }

    /**
     * Bewegt alle Knoten um die angegebenen Koordinaten. Diese Aktion kann nicht rückgängig gemacht werden.
     *
     * @param amount Die Dimension, um die die Knoten verschoben werden sollen
     */
    public void moveAllNodesNoUndo(Dimension amount) {
        MoveAction action = new MoveAction(nodes, amount);
        action.execute();
    }

    /**
     * Entfernt alle Elemente des Graphen
     */
    public void resetGraph() {
        AddOrRemoveAction action = new AddOrRemoveAction(false, nodes, edges);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt alle ausgewählten Knoten.
     */
    public void removeSelectedNodes() {
        ArrayList<NodePoint> selectedNodes = new ArrayList<>();

        //Ausgewählte Knoten sammeln
        for (NodePoint nodePoint : nodes) {
            if (nodePoint.selected) {
                nodePoint.selected = false;
                selectedNodes.add(nodePoint);
            }
        }
        removeNodes(selectedNodes);
    }

    /**
     * Ändert den Terminalstatus eines Knotens (grafisch gesehen wird zwischen schwarz und weiß gewechselt).
     *
     * @param node Der Knoten, von dem der Terminalstatus geändert werden soll.
     */
    public void changeTerminalStatus(NodePoint node) {
        TerminalChangeAction action = new TerminalChangeAction(node);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Setzt bei allen Knoten den selected-Status auf false.
     */
    public void resetSelection() {
        for (NodePoint node : nodes) {
            node.selected = false;
        }
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

    /**
     * Gibt eine nicht veränderbare Listenrepräsentation der Knotenliste zurück.
     *
     * @return nicht veränderbare Listenrepräsentation der Knotenliste
     */
    public List<NodePoint> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Gibt eine nicht veränderbare Listenrepräsentation der Kantenliste zurück.
     *
     * @return nicht veränderbare Listenrepräsentation der Kantenliste
     */
    public List<EdgeLine> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Beschreibt alle Aktionen, bei denen Knoten und/oder Kanten hinzugefügt oder gelöscht werden.
     */
    class AddOrRemoveAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = 7183317665439824767L;
        List<NodePoint> affectedNodes;
        List<EdgeLine> affectedEdges;
        final boolean isAddAction;

        public AddOrRemoveAction(boolean isAddAction, List<NodePoint> addedNodes, List<EdgeLine> addedEdges) {
            this.isAddAction = isAddAction;
            affectedNodes = new ArrayList<>(addedNodes);
            affectedEdges = new ArrayList<>(addedEdges);
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
            execute();
        }

        public void execute() {
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

        final List<NodePoint> movedNodes;
        final Dimension amount;

        public MoveAction(ArrayList<NodePoint> nodes, Dimension amount) {
            movedNodes = new ArrayList<>(nodes);
            this.amount = amount;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            execute();
        }

        public void execute() {
            if (movedNodes != null) {
                for (NodePoint node : movedNodes) {
                    node.x += amount.getWidth();
                    node.y += amount.getHeight();
                }
                refreshEdges();
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
                refreshEdges();
            }
        }

        private void refreshEdges() {
            for (EdgeLine edgeLine : edges) {
                if (movedNodes.contains(edgeLine.startNode) || movedNodes.contains(edgeLine.endNode)) {
                    edgeLine.refresh();
                }
            }
        }
    }

    /**
     * Beschreibt eine Aktion, bei der der Terminalstatus eines Knotens verändert wird
     */
    static class TerminalChangeAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = 8220691958588757429L;
        final NodePoint node;

        public TerminalChangeAction(NodePoint node) {
            this.node = node;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            execute();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            execute();
        }

        public void execute() {
            node.c_node = !node.c_node;
        }
    }
}
