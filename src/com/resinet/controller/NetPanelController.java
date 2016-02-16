package com.resinet.controller;

import com.resinet.model.EdgeLine;
import com.resinet.model.NetPanelData;
import com.resinet.model.NodePoint;
import com.resinet.util.GraphChangedListener;
import com.resinet.util.GraphUtil;
import com.resinet.util.NodeEdgeWrapper;
import com.resinet.views.NetPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class NetPanelController implements MouseListener, MouseMotionListener {
    private final NetPanel netPanel;
    private final GraphChangedListener listener;

    private final NetPanelData netData;

    private boolean cursorInsideSelection;
    private boolean selectedNodesDragging = false;
    private Point selectionDraggingStart;

    private boolean nodeClickable = true;
    private boolean edgeClickable = true;

    private Point currentMousePosition;
    private Shape hoveredElement;

    private boolean selectDragging = false;
    private Point selectStartPoint;
    private boolean nodesSelected = false;
    private Rectangle selectionRectangle;

    //die Kante, die gezeichnet wird, während man die Maus gedrückt hält (beim Kanten erstellen)
    private EdgeLine draggingLine;
    private boolean lineDragging = false;

    private static final int HOVER_DISTANCE = 7;

    public NetPanelController(NetPanel netPanel, GraphChangedListener listener) {
        this.netPanel = netPanel;
        this.listener = listener;

        netData = new NetPanelData();
    }

    /**
     * Wird vom Mainframe-Controller weitergegeben, wenn das NetPanel Fokus hat und dient dazu, Copy&Paste-Aktionen zu
     * behandeln.
     *
     * @param e Das ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action) {
            case "delete":
                removeSelectedNodes();
                break;
            case "undo":
                netData.undo();
                resetSelection();
                break;
            case "redo":
                netData.redo();
                resetSelection();
                break;
            default:
                Action a = netPanel.getActionMap().get(action);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(netPanel, ActionEvent.ACTION_PERFORMED, null));
                }
                break;
        }
        netPanel.repaint();
    }

    /**
     * Zentriert den Graphen, wenn eine Flag dafür gesetzt wurde, etwa nach dem Laden eines Graphen aus einer Datei.
     */
    public void centerGraph() {
        int panelHeight = netPanel.getHeight();
        int panelWidth = netPanel.getWidth();

        //Direktes Vaterelement ist der ViewPort der Scrollpane, und davon das Vaterelement ist die Scrollpane
        Container parent = netPanel.getParent().getParent();
        if (parent instanceof JScrollPane) {
            panelHeight = netPanel.getParent().getHeight();
            panelWidth = netPanel.getParent().getWidth();
        }

        Rectangle graphRect = GraphUtil.getGraphBounds(netData.getNodes());

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

        //Aktion zum verschieben auslösen
        netData.moveAllNodesNoUndo(new Dimension(offsetX, offsetY));
    }

    /**
     * Entfernt die Knoten und deren anliegende Kanten vom Graphen.
     *
     * @param nodes Die zu entfernende Knotenliste
     */
    public void removeNodes(ArrayList<NodePoint> nodes) {
        netData.removeNodes(nodes);
        listener.graphChanged();
        netPanel.repaint();
    }


    /**
     * Setzt den Graph zurück
     */
    public void resetGraph() {
        netData.resetGraph();
        lineDragging = false;
        draggingLine = null;
        resetSelection();
        netPanel.repaint();
    }

    /**
     * Setzt die Auswahl zurück
     */
    public void resetSelection() {
        nodesSelected = false;
        netPanel.selectionAnimationTimer.stop();
        netData.resetSelection();
    }

    /**
     * Entfernt alle ausgewählten Knoten vom Graphen
     */
    private void removeSelectedNodes() {
        if (!nodesSelected)
            return;

        netData.removeSelectedNodes();
        resetSelection();
    }


    /**
     * Erstellt ein Objekt mit den ausgewählten Knoten und den Kanten innerhalb der Knotenmenge. Dabei werden die Knoten
     * und Kanten geklont. Die Referenzen der Kanten werden auf die geklonten Knoten gesetzt.
     *
     * @return NodeEdgeWrapper
     */
    public NodeEdgeWrapper getSelectionCopyData() {
        if (!nodesSelected) {
            return null;
        }
        return netData.getSelectionCopyData();
    }


    /**
     * Fügt Knoten und Kanten in den Graphen ein, welche vorher möglicherweise kopiert/ausgeschnitten wurden. Die Knoten
     * werden automatisch ausgewählt, damit sie verschoben werden können. Zum Einfügen wird ein freier Platz ausgehend
     * von der Mausposition gesucht, falls die eingefügten Knoten mit bestehenden Knoten überlappen.
     *
     * @param nodeEdgeWrapper Das Wrapper-Objekt mit Knoten- und Kantenmenge
     */
    public void pasteNodesAndEdges(NodeEdgeWrapper nodeEdgeWrapper) {
        if (nodeEdgeWrapper == null)
            return;

        //Auswahl zurücksetzen, damit die neuen Knoten nicht automatisch zusätzlich ausgewählt sind
        resetSelection();

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
        netData.addNodesAndEdges(nodes, nodeEdgeWrapper.edges);

        listener.graphChanged();

        //Eingefügte Knoten auswählen
        nodeEdgeWrapper.nodes.forEach((nodePoint -> nodePoint.selected = true));
        selectionRectangle = GraphUtil.getGraphBounds(nodeEdgeWrapper.nodes, 5);
        nodesSelected = true;
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
    private Point searchSafeRectanglePosition(Rectangle rectangle) {
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
        List<NodePoint> drawnNodes = netData.getNodes();

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
    private void revalidateScrollPane() {
        Component parent2 = netPanel.getParent().getParent();
        parent2.revalidate();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        netPanel.requestFocusInWindow();

        if (nodesSelected) {
            resetSelection();
            netPanel.repaint();
            return;
        }
        List<NodePoint> drawnNodes = netData.getNodes();
        List<EdgeLine> drawnEdges = netData.getEdges();

        Integer clickX = mouseEvent.getX();
        Integer clickY = mouseEvent.getY();
        boolean nodeClicked = false;

        //Prüfen, ob der Click einen Knoten getroffen hat
        for (NodePoint currentNode : drawnNodes) {

            if (currentNode.contains(clickX, clickY)) {
                //Knoten wurde angeklickt
                nodeClicked = true;

                if (mouseEvent.isShiftDown() || SwingUtilities.isMiddleMouseButton(mouseEvent)) {
                    //mit Shift geklickt oder mit mittlerer Maustaste
                    //Knoten löschen
                    netData.removeNode(currentNode);

                    int currentNodeIndex = drawnNodes.indexOf(currentNode);
                    listener.graphElementDeleted(true, currentNodeIndex);

                } else if (mouseEvent.isControlDown() || SwingUtilities.isRightMouseButton(mouseEvent)) {
                    //Rechtsklick oder mit Strg
                    //Knoten zum K-Knoten machen oder umgekehrt
                    netData.changeTerminalStatus(currentNode);

                } else if (nodeClickable) {
                    //Event auslösen
                    listener.graphElementClicked(true, drawnNodes.indexOf(currentNode));
                }
                break;
            }
        }

        if (!nodeClicked) {
            boolean edgeClicked = false;
            //Wenn kein Knoten angeklickt wurde, auf Kante prüfen
            for (EdgeLine edgeLine : drawnEdges) {

                if (edgeLine.ptSegDist(clickX, clickY) < HOVER_DISTANCE) {
                    if (mouseEvent.isShiftDown() || SwingUtilities.isMiddleMouseButton(mouseEvent)) {
                        //mit shift oder mit mittlerer Maustaste geklickt --> Kante entfernen
                        netData.removeEdge(edgeLine);

                        int edgeIndex = drawnEdges.indexOf(edgeLine);
                        listener.graphElementDeleted(false, edgeIndex);

                    } else if (edgeClickable) {
                        //Event auslösen
                        listener.graphElementClicked(false, drawnEdges.indexOf(edgeLine));
                    }
                    edgeClicked = true;
                    break;
                }
            }

            if (!edgeClicked) {
                //Wenn kein bestehendes Element angeklickt wurde, neuen Knoten erzeugen
                int x = clickX - 10;
                int y = clickY - 10;

                //isMetaDown() ist beim Rechtsklick true
                boolean c_node = mouseEvent.isMetaDown();

                NodePoint newNode = new NodePoint(x, y, c_node);
                //Prüfen, ob der neue Knoten mit einem anderen überlappt
                for (NodePoint currentNode : drawnNodes) {

                    //Falls er überlappt, den neuen Knoten in die entsprechende Richtung verschieben
                    if (currentNode.getFrame().intersects(newNode.getFrame())) {
                        if (x > currentNode.getX() && y > currentNode.getY()) {
                            newNode = new NodePoint(x + 10, y + 10, c_node);
                        } else if (x > currentNode.getX() && y < currentNode.getY()) {
                            newNode = new NodePoint(x + 10, y - 10, c_node);
                        } else if (x < currentNode.getX() && y > currentNode.getY()) {
                            newNode = new NodePoint(x - 10, y + 10, c_node);
                        } else {
                            newNode = new NodePoint(x - 10, y - 10, c_node);
                        }
                    }
                }
                //neuen Knoten hinzufügen
                netData.addNode(newNode);
                listener.graphElementAdded(true, drawnNodes.size() - 1);
            }
        }
        //neu zeichnen
        netPanel.repaint();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.isMetaDown()) {
            //Rechtsklick ignorieren
            return;
        }
        //Mausposition setzen
        currentMousePosition = mouseEvent.getPoint();

        lineDragging = false;
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();

        //Prüfen, ob die Auswahl verschoben werden soll
        if (cursorInsideSelection) {
            selectedNodesDragging = true;
            selectionDraggingStart = mouseEvent.getPoint();
            hoveredElement = null;
            return;
        }

        //Prüfen, ob in ein Knoten gedrückt wurde, damit das Ziehen einer Kante gestartet wird
        List<NodePoint> drawnNodes = netData.getNodes();
        for (NodePoint node : drawnNodes) {
            if (node.contains(x, y)) {
                //Es wurde in den Kreis geklickt, also Kantenziehen starten
                draggingLine = new EdgeLine(node, null);
                lineDragging = true;
                return;
            }
        }

        //Ansonsten auswählen beginnen
        selectDragging = true;
        //Startpunkt setzen
        selectStartPoint = mouseEvent.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.isMetaDown())
            return;

        List<NodePoint> drawnNodes = netData.getNodes();
        List<EdgeLine> drawnEdges = netData.getEdges();

        if (lineDragging) {
            lineDragging = false;
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();

            //Prüft, ob die Maus innerhalb eines Knotens losgelassen wurde
            for (NodePoint currentNode : drawnNodes) {

                if (currentNode.contains(x, y) && draggingLine.startNode != currentNode) {

                    //Prüfen, ob eine Kante mit genau diesen anliegenden Knoten bereits existiert
                    for (EdgeLine currentEdge : drawnEdges) {

                        //Wenn die Kante existiert, dann abbrechen
                        if (currentEdge.startNode == currentNode && currentEdge.endNode == draggingLine.startNode
                                || currentEdge.startNode == draggingLine.startNode && currentEdge.endNode == currentNode) {
                            netPanel.repaint();
                            return;
                        }
                    }

                    //Maus wurde in diesem Knoten losgelassen -> als Endknoten der Kante festlegen
                    netData.addEdge(draggingLine.startNode, currentNode);
                    listener.graphElementAdded(false, drawnEdges.size() - 1);

                    netPanel.repaint();
                    return;
                }
            }
        }
        //Falls ausgewählt wurde
        if (selectDragging) {
            selectDragging = false;

            //Rechteck mit dem Startpunkt und der aktuellen Position erstellen
            selectionRectangle = new Rectangle(mouseEvent.getPoint());
            selectionRectangle.add(selectStartPoint);

            //Liste mit ausgewählten Knoten
            ArrayList<NodePoint> selectedNodes = new ArrayList<>();

            for (NodePoint node : drawnNodes) {
                if (node.intersects(selectionRectangle)) {
                    node.selected = true;
                    nodesSelected = true;
                    selectedNodes.add(node);
                } else {
                    node.selected = false;
                }
            }

            //Rechteck erstellen, dass mit 5 Pixel Abstand alle ausgewählten Knoten umschließt
            //Falls keine Knoten ausgewählt wurden, hat das Reckteck alle Parameter auf 0
            selectionRectangle = GraphUtil.getGraphBounds(selectedNodes, 5);

            if (nodesSelected) {
                //Timer (neu) starten (falls er bereits läuft)
                netPanel.selectionAnimationTimer.restart();
            }
        }

        if (selectedNodesDragging) {
            selectedNodesDragging = false;

            ArrayList<NodePoint> selectedNodes = new ArrayList<>();

            //Ausgewählte Knoten sammeln
            for (NodePoint nodePoint : drawnNodes) {
                if (nodePoint.selected) {
                    selectedNodes.add(nodePoint);
                }
            }
            Point endPoint = mouseEvent.getPoint();

            Dimension moveAmount = new Dimension(endPoint.x - selectionDraggingStart.x, endPoint.y - selectionDraggingStart.y);

            netData.moveNodesFinal(selectedNodes, moveAmount);
            //Scrollpane aktualisieren
            revalidateScrollPane();
        }
        netPanel.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (lineDragging) {
            //Während des Kantenziehens die Endposition der Kante aktualisieren, damit Sie immer unter dem Cursor endet
            int x = evt.getX();
            int y = evt.getY();
            draggingLine.x2 = x;
            draggingLine.y2 = y;
            netPanel.repaint();
        } else if (selectDragging) {
            //Mausposition setzen und Auswählrechteck neu zeichnen
            currentMousePosition = evt.getPoint();
            netPanel.repaint();
        } else if (selectedNodesDragging) {
            Point newMousePosition = evt.getPoint();

            //Offsets bestimmen
            int offsetX = newMousePosition.x - currentMousePosition.x;
            int offsetY = newMousePosition.y - currentMousePosition.y;

            //Auswahlrechteck verschieben
            selectionRectangle.x += offsetX;
            selectionRectangle.y += offsetY;

            List<NodePoint> drawnNodes = netData.getNodes();
            ArrayList<NodePoint> selectedNodes = new ArrayList<>();

            //Ausgewählte Knoten sammeln
            for (NodePoint nodePoint : drawnNodes) {
                if (nodePoint.selected) {
                    selectedNodes.add(nodePoint);
                }
            }
            netData.moveNodesNotFinal(selectedNodes, new Dimension(offsetX, offsetY));

            //Neue Mausposition setzen
            currentMousePosition = evt.getPoint();
            netPanel.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

        if (lineDragging || selectDragging)
            return;

        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        boolean consumed = false;

        /**
         * Prüfen, ob der Cursor auf einem markierten Bereich ist
         */
        if (nodesSelected && selectionRectangle.contains(x, y)) {
            cursorInsideSelection = true;
            consumed = true;
            hoveredElement = null;
        } else {
            cursorInsideSelection = false;
        }

        List<NodePoint> drawnNodes = netData.getNodes();
        List<EdgeLine> drawnEdges = netData.getEdges();

        /**
         * Prüfen, ob ein Knoten getroffen wird
         */
        if (!consumed) {
            for (NodePoint nodePoint : drawnNodes) {

                if (nodePoint.contains(x, y)) {
                    hoveredElement = nodePoint;
                    netPanel.repaint();
                    consumed = true;
                    break;
                }
            }
        }

        /**
         * Prüfen, ob der Cursor nahe einer Kante ist (nah ist hier maximal 5px Abstand)
         */
        if (!consumed) {
            for (EdgeLine edgeLine : drawnEdges) {

                if (edgeLine.ptSegDist(x, y) < HOVER_DISTANCE) {
                    hoveredElement = edgeLine;
                    netPanel.repaint();
                    consumed = true;
                    break;
                }
            }
        }

        if (!consumed && hoveredElement != null) {
            //Cursor zurücksetzen, falls er auf keinem Element mehr ist
            hoveredElement = null;
            netPanel.repaint();
        }
        netPanel.setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown());
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu.
     *
     * @param nodes Die Knotenmenge
     * @param edges Die Kantenmenge
     */
    public void addNodesAndEdges(List<NodePoint> nodes, List<EdgeLine> edges) {
        netData.addNodesAndEdges(nodes, edges);
    }

    public boolean isCursorInsideSelection() {
        return cursorInsideSelection;
    }

    public List<NodePoint> getNodes() {
        return netData.getNodes();
    }

    public List<EdgeLine> getEdges() {
        return netData.getEdges();
    }

    public boolean isNodeClickable() {
        return nodeClickable;
    }

    public boolean isEdgeClickable() {
        return edgeClickable;
    }

    public void setClickableElements(boolean nodeClickable, boolean edgeClickable) {
        this.nodeClickable = nodeClickable;
        this.edgeClickable = edgeClickable;
    }

    public Point getCurrentMousePosition() {
        return currentMousePosition;
    }

    public Shape getHoveredElement() {
        return hoveredElement;
    }

    public EdgeLine getDraggingLine() {
        return draggingLine;
    }

    public boolean isLineDragging() {
        return lineDragging;
    }

    public boolean isSelectDragging() {
        return selectDragging;
    }

    public Point getSelectStartPoint() {
        return selectStartPoint;
    }

    public boolean isNodesSelected() {
        return nodesSelected;
    }

    public Rectangle getSelectionRectangle() {
        return selectionRectangle;
    }
}
