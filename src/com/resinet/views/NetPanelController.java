package com.resinet.views;

import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;
import com.resinet.util.GraphChangedListener;
import com.resinet.util.GraphUtil;
import com.resinet.util.NodeEdgeWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class NetPanelController {
    private NetPanel netPanel;
    private GraphChangedListener listener;

    public NetPanelController(NetPanel netPanel, GraphChangedListener listener) {
        this.netPanel = netPanel;
        this.listener = listener;
    }

    /**
     * Wird vom Mainframe-Controller weitergegeben, wenn das NetPanel Fokus hat und dient dazu, Copy&Paste-Aktionen zu
     * behandeln.
     *
     * @param e Das ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("delete")) {
            removeSelectedNodes();
        } else {
            Action a = netPanel.getActionMap().get(action);
            if (a != null) {
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    /**
     * Zentriert den Graphen, wenn eine Flag dafür gesetzt wurde, etwa nach dem Laden eines Graphen aus einer Datei.
     */
    void centerGraph() {
        int panelHeight = netPanel.getHeight();
        int panelWidth = netPanel.getWidth();

        //Direktes Vaterelement ist der ViewPort der Scrollpane, und davon das Vaterelement ist die Scrollpane
        Container parent = netPanel.getParent().getParent();
        if (parent instanceof JScrollPane) {
            panelHeight = netPanel.getParent().getHeight();
            panelWidth = netPanel.getParent().getWidth();
        }

        Rectangle graphRect = GraphUtil.getGraphBounds(netPanel.drawnNodes);

        Integer offsetX, offsetY;

        if (graphRect.getX() < 0) {
            offsetX = Math.abs(((int) graphRect.getX()));
        } else {
            offsetX = (int) ((panelWidth - graphRect.getWidth()) / 2 - graphRect.getX());
        }
        if (graphRect.getX() + offsetX < 0) {
            offsetX = -((int) graphRect.getX());
        }

        if (graphRect.getY() < 0) {
            offsetY = Math.abs((int) graphRect.getY());
        } else {
            offsetY = (int) ((panelHeight - graphRect.getHeight()) / 2 - graphRect.getY());
        }
        if (graphRect.getY() + offsetY < 0) {
            offsetY = -((int) graphRect.getY());
        }

        //Knoten verschieben
        for (NodePoint node : netPanel.drawnNodes) {
            node.x += offsetX;
            node.y += offsetY;
        }

        //Kantenpositionen verschieben
        netPanel.drawnEdges.forEach(EdgeLine::refresh);
    }


    /**
     * Löscht die an einen Knoten anliegenden Kanten
     *
     * @param node Der Knoten
     */
    void removeAdjacentEdges(NodePoint node) {
        ArrayList<NodePoint> nodes = new ArrayList<>();
        nodes.add(node);
        removeAdjacentEdges(nodes);
    }

    /**
     * Löscht die an einer Knotenmenge anliegenden Kanten
     *
     * @param nodes Die Knotenmenge
     */
    void removeAdjacentEdges(ArrayList<NodePoint> nodes) {
        ArrayList<EdgeLine> drawnEdges = netPanel.drawnEdges;

        for (int i = 0; i < drawnEdges.size(); i++) {
            EdgeLine edl = drawnEdges.get(i);
            if (nodes.contains(edl.startNode) || nodes.contains(edl.endNode)) {
                drawnEdges.remove(edl);
                listener.graphElementDeleted(false, i);
                i--;
            }
        }
    }


    /**
     * Entfernt die Knoten und deren anliegende Kanten vom Graphen.
     *
     * @param nodes Die zu entfernende Knotenliste
     */
    public void removeNodes(ArrayList<NodePoint> nodes) {
        removeAdjacentEdges(nodes);
        netPanel.drawnNodes.removeAll(nodes);
        listener.graphChanged();
        netPanel.repaint();
    }


    /**
     * Entfernt alle ausgewählten Knoten vom Graphen
     */
    public void removeSelectedNodes() {
        if (!netPanel.nodesSelected)
            return;

        ArrayList<NodePoint> drawnNodes = netPanel.drawnNodes;
        ArrayList<NodePoint> selectedNodes = new ArrayList<>();

        //Ausgewählte Knoten sammeln
        for (NodePoint nodePoint : drawnNodes) {
            if (nodePoint.selected) {
                selectedNodes.add(nodePoint);
            }
        }
        netPanel.resetSelection();
        removeNodes(selectedNodes);
    }


    /**
     * Erstellt ein Objekt mit den ausgewählten Knoten und den Kanten innerhalb der Knotenmenge. Dabei werden die Knoten
     * und Kanten geklont. Die Referenzen der Kanten werden auf die geklonten Knoten gesetzt.
     *
     * @return NodeEdgeWrapper
     */
    public NodeEdgeWrapper getSelectionCopyData() {
        ArrayList<NodePoint> drawnNodes = netPanel.drawnNodes;
        ArrayList<EdgeLine> drawnEdges = netPanel.drawnEdges;

        if (!netPanel.nodesSelected) {
            return null;
        }

        //Diese Liste wird zum Entfernen der Knoten benötigt, falls die Aktion "Ausschneiden" ist
        ArrayList<NodePoint> originalSelectedNodes = new ArrayList<>();
        ArrayList<NodePoint> selectedNodes = new ArrayList<>();
        ArrayList<EdgeLine> selectedEdges = new ArrayList<>();

        //Ausgewählte Knoten sammeln
        for (NodePoint nodePoint : drawnNodes) {
            if (nodePoint.selected) {
                selectedNodes.add(nodePoint);
            }
        }

        originalSelectedNodes.addAll(selectedNodes);

        //Anliegende Kanten klonen
        for (EdgeLine edgeLine : drawnEdges) {
            if (selectedNodes.contains(edgeLine.endNode) && selectedNodes.contains(edgeLine.startNode)) {
                selectedEdges.add((EdgeLine) edgeLine.clone());
            }
        }

        //Knoten klonen und dabei Referenzen der geklonten Kanten neu setzen
        for (int i = 0; i < selectedNodes.size(); i++) {
            NodePoint nodePoint = drawnNodes.get(i);

            NodePoint newNode = (NodePoint) nodePoint.clone();
            newNode.selected = false;
            //Aktuellen Knoten durch geklonten ersetzen
            selectedNodes.set(i, newNode);

            //Referenzen neu setzen
            for (EdgeLine edgeLine : selectedEdges) {
                if (edgeLine.endNode.equals(nodePoint)) {
                    edgeLine.endNode = newNode;
                }
                if (edgeLine.startNode.equals(nodePoint)) {
                    edgeLine.startNode = newNode;
                }
            }
        }
        return new NodeEdgeWrapper(originalSelectedNodes, selectedNodes, selectedEdges);
    }


    /**
     * Fügt Knoten und Kanten in den Graphen ein, welche vorher möglicherweise kopiert/ausgeschnitten wurden. Die Knoten
     * werden automatisch ausgewählt, damit sie verschoben werden können. Zum Einfügen wird ein freier Platz ausgehend
     * von der Mausposition gesucht, falls die eingefügten Knoten mit bestehenden Knoten überlappen.
     *
     * @param nodeEdgeWrapper Das Wrapper-Objekt mit Knoten- und Kantenmenge
     */
    public void pasteNodesAndEdges(NodeEdgeWrapper nodeEdgeWrapper) {
        ArrayList<NodePoint> drawnNodes = netPanel.drawnNodes;
        ArrayList<EdgeLine> drawnEdges = netPanel.drawnEdges;
        //Auswahl zurücksetzen, damit die neuen Knoten nicht automatisch zusätzlich ausgewählt sind
        netPanel.resetSelection();

        ArrayList<NodePoint> nodes = nodeEdgeWrapper.nodes;

        //Freie Stelle für neue Knoten suchen
        Rectangle pasteRectangle = GraphUtil.getGraphBounds(nodeEdgeWrapper.nodes);
        Point originalLocation = pasteRectangle.getLocation();

        //Wenn das Rechteck um die eingefügten Knoten andere Knoten kreuzt, neue Position suchen
        if (intersectsNode(pasteRectangle)) {
            //Mausposition testen
            Point mousePosition = netPanel.getMousePosition();
            //Mausposition kann null sein, etwa wenn die Maus nicht über dem NetPanel ist
            if (mousePosition != null) {
                pasteRectangle.setLocation(mousePosition);
            }

            if (intersectsNode(pasteRectangle)) {
                //Falls an der Mausposition nicht möglich, sicheres Rechteck suchen

                Point whiteRectanglePosition = searchSafeRectanglePosition(pasteRectangle);
                if (whiteRectanglePosition != null) {
                    pasteRectangle.setLocation(whiteRectanglePosition);
                } else {
                    //Alle Methoden erfolglos, Rechteck außerhalb der Zeichenfläche verschieben, damit die Zeichenfläche vergrößert wird.
                    pasteRectangle.x = netPanel.getWidth();
                }
            }
        }

        //Knoten wie das Rechteck verschieben
        for (NodePoint nodePoint : nodes) {
            nodePoint.x += pasteRectangle.getX() - originalLocation.getX();
            nodePoint.y += pasteRectangle.getY() - originalLocation.getY();
        }

        //Kantenkoordinaten neu setzen
        nodeEdgeWrapper.edges.forEach(EdgeLine::refresh);

        //Neue Knoten und Kanten hinzufügen
        drawnNodes.addAll(nodes);
        drawnEdges.addAll(nodeEdgeWrapper.edges);

        listener.graphChanged();

        //Eingefügte Knoten auswählen
        nodeEdgeWrapper.nodes.forEach((nodePoint -> nodePoint.selected = true));
        netPanel.selectionRectangle = GraphUtil.getGraphBounds(nodeEdgeWrapper.nodes, 5);
        netPanel.nodesSelected = true;
        netPanel.selectionAnimationTimer.restart();

        //Scrollbar refreshen
        revalidateScrollPane();
        netPanel.repaint();
    }


    /**
     * Sucht eine Position für das Rechteck, an der es mit keinem Knoten überlappt.
     *
     * @param rectangle Das Rechteck
     * @return neue Position oder null
     */
    public Point searchSafeRectanglePosition(Rectangle rectangle) {
        double xRange = netPanel.getWidth() - rectangle.getWidth();
        double yRange = netPanel.getHeight() - rectangle.getHeight();

        for (int x = 0; x < xRange; x += 20) {
            for (int y = 0; y < yRange; y += 20) {
                rectangle.setLocation(x, y);

                if (!intersectsNode(rectangle)) {
                    return rectangle.getLocation();
                }
            }
        }
        return null;
    }

    /**
     * Prüft, ob das Rechteck einen Knoten schneidet.
     *
     * @param pasteRectangle Das zu überprüfende Rechteck
     * @return Boolean
     */
    private boolean intersectsNode(Rectangle pasteRectangle) {
        ArrayList<NodePoint> drawnNodes = netPanel.drawnNodes;

        for (NodePoint nodePoint : drawnNodes) {
            if (nodePoint.intersects(pasteRectangle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lässt die Scrollpane revalidieren, damit etwa die Scrollbars an die veränderte Größe des Graphen angepasst
     * werden.
     */
    void revalidateScrollPane() {
        Component parent2 = netPanel.getParent().getParent();
        parent2.revalidate();
    }
}
