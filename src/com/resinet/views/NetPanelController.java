package com.resinet.views;

import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;
import com.resinet.util.GraphChangedListener;
import com.resinet.util.GraphUtil;
import com.resinet.util.NodeEdgeWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class NetPanelController implements MouseListener, MouseMotionListener {
    private NetPanel netPanel;
    private GraphChangedListener listener;

    private final ArrayList<NodePoint> drawnNodes;
    private final ArrayList<EdgeLine> drawnEdges;

    private boolean cursorInsideSelection;
    private boolean selectedNodesDragging = false;

    private boolean nodeClickable = true;
    private boolean edgeClickable = true;

    public NetPanelController(NetPanel netPanel, GraphChangedListener listener) {
        this.netPanel = netPanel;
        this.listener = listener;

        drawnEdges = new ArrayList<>();
        drawnNodes = new ArrayList<>();
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

        Rectangle graphRect = GraphUtil.getGraphBounds(drawnNodes);

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
        for (NodePoint node : drawnNodes) {
            node.x += offsetX;
            node.y += offsetY;
        }

        //Kantenpositionen verschieben
        drawnEdges.forEach(EdgeLine::refresh);
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
        drawnNodes.removeAll(nodes);
        listener.graphChanged();
        netPanel.repaint();
    }


    /**
     * Setzt den Graph zurück
     */
    public void resetGraph() {
        drawnNodes.clear();
        drawnEdges.clear();
        netPanel.lineDragging = false;
        netPanel.draggingLine = null;
        netPanel.resetSelection();
        netPanel.repaint();
    }

    /**
     * Entfernt alle ausgewählten Knoten vom Graphen
     */
    public void removeSelectedNodes() {
        if (!netPanel.nodesSelected)
            return;

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

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        netPanel.requestFocusInWindow();

        if (netPanel.nodesSelected) {
            netPanel.resetSelection();
            netPanel.repaint();
            return;
        }

        Integer clickX = mouseEvent.getX();
        Integer clickY = mouseEvent.getY();
        boolean nodeClicked = false;

        //Prüfen, ob der Click einen Knoten getroffen hat
        for (NodePoint currentNode : drawnNodes) {

            if (currentNode.contains(clickX, clickY)) {
                //Knoten wurde angeklickt
                nodeClicked = true;

                if (mouseEvent.isShiftDown()) {
                    //Knoten löschen
                    int currentNodeIndex = drawnNodes.indexOf(currentNode);
                    drawnNodes.remove(currentNode);
                    listener.graphElementDeleted(true, currentNodeIndex);

                    //Anliegende Kanten löschen
                    removeAdjacentEdges(currentNode);

                } else if (mouseEvent.isControlDown()) {
                    //Knoten zum K-Knoten machen oder umgekehrt
                    currentNode.c_node = !currentNode.c_node;

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

                if (edgeLine.ptSegDist(clickX, clickY) < NetPanel.HOVER_DISTANCE) {
                    if (mouseEvent.isShiftDown()) {
                        int edgeIndex = drawnEdges.indexOf(edgeLine);
                        drawnEdges.remove(edgeLine);
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
                drawnNodes.add(newNode);
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
        netPanel.currentMousePosition = mouseEvent.getPoint();

        netPanel.lineDragging = false;
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();

        //Prüfen, ob die Auswahl verschoben werden soll
        if (cursorInsideSelection) {
            selectedNodesDragging = true;
            netPanel.hoveredElement = null;
            return;
        }

        //Prüfen, ob in ein Knoten gedrückt wurde, damit das Ziehen einer Kante gestartet wird
        for (NodePoint np : drawnNodes) {

            if (np.contains(x, y)) {
                //Es wurde in den Kreis geklickt, also Kantenziehen starten
                netPanel.draggingLine = new EdgeLine(np, null);
                netPanel.lineDragging = true;
                return;
            }
        }

        //Ansonsten auswählen beginnen
        netPanel.selectDragging = true;
        //Startpunkt setzen
        netPanel.selectStartPoint = mouseEvent.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.isMetaDown())
            return;

        if (netPanel.lineDragging) {
            netPanel.lineDragging = false;
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();

            //Prüft, ob die Maus innerhalb eines Knotens losgelassen wurde
            for (NodePoint currentNode : drawnNodes) {

                if (currentNode.contains(x, y) && netPanel.draggingLine.startNode != currentNode) {

                    //Prüfen, ob eine Kante mit genau diesen anliegenden Knoten bereits existiert
                    for (EdgeLine currentEdge : drawnEdges) {

                        //Wenn die Kante existiert, dann abbrechen
                        if (currentEdge.startNode == currentNode && currentEdge.endNode == netPanel.draggingLine.startNode
                                || currentEdge.startNode == netPanel.draggingLine.startNode && currentEdge.endNode == currentNode) {
                            netPanel.repaint();
                            return;
                        }
                    }

                    //Maus wurde in diesem Knoten losgelassen -> als Endknoten der Kante festlegen
                    netPanel.draggingLine.setEndNode(currentNode);
                    drawnEdges.add(netPanel.draggingLine);
                    listener.graphElementAdded(false, drawnEdges.size() - 1);

                    netPanel.repaint();
                    return;
                }
            }
        }
        //Falls ausgewählt wurde
        if (netPanel.selectDragging) {
            netPanel.selectDragging = false;

            //Rechteck mit dem Startpunkt und der aktuellen Position erstellen
            netPanel.selectionRectangle = new Rectangle(mouseEvent.getPoint());
            netPanel.selectionRectangle.add(netPanel.selectStartPoint);

            //Liste mit ausgewählten Knoten
            ArrayList<NodePoint> selectedNodes = new ArrayList<>();

            for (NodePoint node : drawnNodes) {
                if (node.intersects(netPanel.selectionRectangle)) {
                    node.selected = true;
                    netPanel.nodesSelected = true;
                    selectedNodes.add(node);
                } else {
                    node.selected = false;
                }
            }

            //Rechteck erstellen, dass mit 5 Pixel Abstand alle ausgewählten Knoten umschließt
            //Falls keine Knoten ausgewählt wurden, hat das Reckteck alle Parameter auf 0
            netPanel.selectionRectangle = GraphUtil.getGraphBounds(selectedNodes, 5);

            if (netPanel.nodesSelected) {
                //Timer (neu) starten (falls er bereits läuft)
                netPanel.selectionAnimationTimer.restart();
            }
        }

        if (selectedNodesDragging) {
            selectedNodesDragging = false;

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
        if (netPanel.lineDragging) {
            //Während des Kantenziehens die Endposition der Kante aktualisieren, damit Sie immer unter dem Cursor endet
            int x = evt.getX();
            int y = evt.getY();
            netPanel.draggingLine.x2 = x;
            netPanel.draggingLine.y2 = y;
            netPanel.repaint();
        } else if (netPanel.selectDragging) {
            //Mausposition setzen und Auswählrechteck neu zeichnen
            netPanel.currentMousePosition = evt.getPoint();
            netPanel.repaint();
        } else if (selectedNodesDragging) {
            Point newMousePosition = evt.getPoint();

            //Offsets bestimmen
            int offsetX = newMousePosition.x - netPanel.currentMousePosition.x;
            int offsetY = newMousePosition.y - netPanel.currentMousePosition.y;

            //Auswahlrechteck verschieben
            netPanel.selectionRectangle.x += offsetX;
            netPanel.selectionRectangle.y += offsetY;

            //Ausgewählte Knoten verschieben
            for (NodePoint nodePoint : drawnNodes) {
                if (nodePoint.selected) {
                    nodePoint.x += offsetX;
                    nodePoint.y += offsetY;
                }
            }

            //Anliegende Kanten verschieben
            for (EdgeLine edgeLine : drawnEdges) {
                if (edgeLine.startNode.selected || edgeLine.endNode.selected) {
                    edgeLine.refresh();
                }
            }
            //Neue Mausposition setzen
            netPanel.currentMousePosition = evt.getPoint();
            netPanel.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

        if (netPanel.lineDragging || netPanel.selectDragging)
            return;

        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        boolean consumed = false;

        /**
         * Prüfen, ob der Cursor auf einem markierten Bereich ist
         */
        if (netPanel.nodesSelected && netPanel.selectionRectangle.contains(x, y)) {
            cursorInsideSelection = true;
            consumed = true;
            netPanel.hoveredElement = null;
        } else {
            cursorInsideSelection = false;
        }

        /**
         * Prüfen, ob ein Knoten getroffen wird
         */
        if (!consumed) {
            for (NodePoint nodePoint : drawnNodes) {

                if (nodePoint.contains(x, y)) {
                    netPanel.hoveredElement = nodePoint;
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

                if (edgeLine.ptSegDist(x, y) < NetPanel.HOVER_DISTANCE) {
                    netPanel.hoveredElement = edgeLine;
                    netPanel.repaint();
                    consumed = true;
                    break;
                }
            }
        }

        if (!consumed && netPanel.hoveredElement != null) {
            //Cursor zurücksetzen, falls er auf keinem Element mehr ist
            netPanel.hoveredElement = null;
            netPanel.repaint();
        }
        netPanel.setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown());
    }

    public boolean isCursorInsideSelection() {
        return cursorInsideSelection;
    }

    public ArrayList<NodePoint> getNodes() {
        return drawnNodes;
    }

    public ArrayList<EdgeLine> getEdges() {
        return drawnEdges;
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
}
