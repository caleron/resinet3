package com.resinet.views;

import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;
import com.resinet.util.GraphUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class NetPanel extends JPanel {
    private final GraphChangedListener listener;

    //die Kante, die gezeichnet wird, während man die Maus gedrückt hält (beim Kanten erstellen)
    private EdgeLine draggingLine;
    private boolean lineDragging = false;

    private boolean nodeHovered = false;
    private boolean edgeHovered = false;

    public final ArrayList<NodePoint> drawnNodes;
    public final ArrayList<EdgeLine> drawnEdges;

    public boolean nodeClickable = true;
    public boolean edgeClickable = true;

    private boolean centerGraphOnNextPaint = false;

    private final Cursor switchCursor, deleteCursor;

    private Integer lastPaintWidth = 0, lastPaintHeight = 0;


    public NetPanel(GraphChangedListener listener) {
        this.listener = listener;

        drawnEdges = new ArrayList<>();
        drawnNodes = new ArrayList<>();

        //EventListener setzen
        addMouseListener(new MyMouseListener());
        addMouseMotionListener(new MyMouseMotionListener());
        addKeyListener(new MyKeyListener());

        //Standardcursor setzen
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        /**
         * Damit Tastendrücke erkannt werden können
         */
        setFocusable(true);

        setOpaque(true);

        //Eigene Cursor initialisieren
        Image switchCursorImage = getToolkit().getImage(ClassLoader.getSystemResource("com/resinet/img/cursor_state_switch.png"));
        Image deleteCursorImage = getToolkit().getImage(ClassLoader.getSystemResource("com/resinet/img/cursor_delete.png"));

        switchCursor = getToolkit().createCustomCursor(switchCursorImage, new Point(0, 0), "Switch State");
        deleteCursor = getToolkit().createCustomCursor(deleteCursorImage, new Point(0, 0), "Delete Element");
    }

    /*
    TODO Linie fett zeichnen, wenn man mit der Maus drüber ist
    Dazu Graphics in Graphics2D casten und dann mit setStroke die Linienart festlegen
    Graphics2D g2 = (Graphics2D) g;
    g2.setStroke(new BasicStroke(10));
     */
    @Override
    public void paintComponent(Graphics g) {
        centerGraphIfResized();

        BufferedImage img = new BufferedImage(lastPaintWidth, lastPaintHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D imgGraphics = img.createGraphics();
        //Kantenglättung aktivieren
        imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        imgGraphics.setColor(Color.WHITE);
        //Hintergrund zeichnen
        imgGraphics.fillRect(0, 0, lastPaintWidth, lastPaintHeight);

        //erst Kanten zeichnen, damit danach das Stück im inneren der Knoten überschrieben werden kann
        // und die Kanten demzufolge nur bis zu den Rändern der Knoten gehen
        imgGraphics.setColor(Color.black);

        for (EdgeLine edgeLine : drawnEdges) {
            imgGraphics.draw(edgeLine);
            String s = String.valueOf(drawnEdges.indexOf(edgeLine));
            imgGraphics.drawString(s, (float) edgeLine.textPositionX, (float) edgeLine.textPositionY);
        }

        //Knoten zeichnen
        int count = 0;
        for (NodePoint nodePoint : drawnNodes) {
            imgGraphics.setColor(Color.black);

            if (nodePoint.c_node) {
                imgGraphics.fill(nodePoint);
                //Textfarbe weiß, da jetzt Hintergrund schwarz ist
                imgGraphics.setColor(Color.white);
            } else {
                //Kreis erst weiß ausfüllen, damit die Kanten dadrin überschrieben werden
                imgGraphics.setColor(Color.white);
                imgGraphics.fill(nodePoint);
                imgGraphics.setColor(Color.black);
                imgGraphics.draw(nodePoint);
            }
            //Zahl im Knoten zeichnen
            String s = String.valueOf(count);
            if (count < 10)
                imgGraphics.drawString(s, (float) nodePoint.getX() + 6, (float) nodePoint.getY() + 15);
            else
                imgGraphics.drawString(s, (float) nodePoint.getX() + 3, (float) nodePoint.getY() + 15);
            count++;
        }

        //Linie während des Kantenziehens zeichnen
        if (lineDragging) {
            imgGraphics.setColor(Color.BLACK);
            imgGraphics.draw(draggingLine);
        }

        g.drawImage(img, 0, 0, this);
    }

    private void centerGraphIfResized() {
        //Wenn die Größe des Panels geändert wurde, Graph neu zentrieren
        if (lastPaintWidth != getWidth() || lastPaintHeight != getHeight() || centerGraphOnNextPaint) {
            centerGraphOnNextPaint = false;
            lastPaintHeight = getHeight();
            lastPaintWidth = getWidth();
            Rectangle graphRect = GraphUtil.getGraphBounds(drawnNodes);

            Integer offsetX, offsetY;

            if (graphRect.getX() < 0) {
                offsetX = Math.abs(((int) graphRect.getX()));
            } else {
                offsetX = (int) ((lastPaintWidth - graphRect.getWidth()) / 2 - graphRect.getX());
            }
            if (graphRect.getX() + offsetX < 0) {
                offsetX = -((int) graphRect.getX());
            }

            if (graphRect.getY() < 0) {
                offsetY = Math.abs((int) graphRect.getY());
            } else {
                offsetY = (int) ((lastPaintHeight - graphRect.getHeight()) / 2 - graphRect.getY());
            }
            if (graphRect.getY() + offsetY < 0) {
                offsetY = -((int) graphRect.getY());
            }


            for (NodePoint node : drawnNodes) {
                node.x += offsetX;
                node.y += offsetY;
            }

            drawnEdges.forEach(EdgeLine::refresh);
        }
    }

    /**
     * Setzt die Flag, dass beim nächsten Zeichnen der Graph zentriert wird
     */
    public void centerGraphOnNextPaint() {
        centerGraphOnNextPaint = true;
    }

    /**
     * Setzt den Graph zurück
     */
    public void resetGraph() {
        drawnNodes.clear();
        drawnEdges.clear();
        lineDragging = false;
        draggingLine = null;
        repaint();
    }

    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            requestFocusInWindow();

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

                        //anliegende Kanten löschen
                        for (int i = 0; i < drawnEdges.size(); i++) {
                            EdgeLine edl = drawnEdges.get(i);
                            if (edl.startNode == currentNode || edl.endNode == currentNode) {
                                drawnEdges.remove(edl);
                                listener.graphElementDeleted(false, i);
                                i--;
                            }
                        }
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

                    if (edgeLine.ptSegDist(clickX, clickY) < 5) {
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
            repaint();
        }


        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            if (mouseEvent.isMetaDown()) {
                //Rechtsklick ignorieren
                return;
            }

            lineDragging = false;
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();

            //Prüfen, ob in ein Knoten gedrückt wurde, damit das Ziehen einer Kante gestartet wird
            for (NodePoint np : drawnNodes) {

                if (np.contains(x, y)) {
                    //Es wurde in den Kreis geklickt, also Kantenziehen starten
                    draggingLine = new EdgeLine(np, null);
                    lineDragging = true;
                    break;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            if (!lineDragging || mouseEvent.isMetaDown())
                return;

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
                            repaint();
                            return;
                        }
                    }

                    //Maus wurde in diesem Knoten losgelassen -> als Endknoten der Kante festlegen
                    draggingLine.setEndNode(currentNode);
                    drawnEdges.add(draggingLine);
                    listener.graphElementAdded(false, drawnEdges.size() - 1);

                    repaint();
                    return;
                }
            }
            repaint();
        }
    }

    private class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent evt) {
            if (lineDragging) {
                //Während des Kantenziehens die Endposition der Kante aktualisieren, damit Sie immer unter dem Cursor endet
                int x = evt.getX();
                int y = evt.getY();
                draggingLine.x2 = x;
                draggingLine.y2 = y;
                repaint();
            }
        }

        /**
         * Prüft, ob der Cursor über einem Element liegt und verändert den Cursor entsprechend
         *
         * @param mouseEvent Das MouseEvent
         */
        @Override
        public void mouseMoved(MouseEvent mouseEvent) {
            if (lineDragging)
                return;

            int x = mouseEvent.getX();
            int y = mouseEvent.getY();

            /**
             * Prüfen, ob ein Knoten getroffen wird
             */
            for (NodePoint nodePoint : drawnNodes) {

                if (nodePoint.contains(x, y)) {
                    nodeHovered = true;
                    edgeHovered = false;
                    setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown());
                    return;
                }
            }

            /**
             * Prüfen, ob der Cursor nahe einer Kante ist (nah ist hier maximal 5px Abstand)
             */
            for (EdgeLine edgeLine : drawnEdges) {

                if (edgeLine.ptSegDist(x, y) < 5) {
                    nodeHovered = false;
                    edgeHovered = true;
                    setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown());
                    return;
                }
            }
            if (nodeHovered || edgeHovered) {
                //Cursor zurücksetzen, falls er auf keinem Element mehr ist
                nodeHovered = false;
                edgeHovered = false;
                setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown());
            }
        }
    }

    /**
     * Reagiert auf Tastendrücke von Strg und Shift und verändert den Cursor entsprechend
     */
    private class MyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent keyEvent) {
            setCursorHover(keyEvent.isShiftDown(), keyEvent.isControlDown());
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            setCursorHover(keyEvent.isShiftDown(), keyEvent.isControlDown());
        }
    }

    /**
     * Setzt den Cursor nach angegebenen Parametern
     *
     * @param shiftDown   Ob Shift gedrückt ist
     * @param controlDown Ob Strg gedrückt ist
     */
    private void setCursorHover(boolean shiftDown, boolean controlDown) {
        if (shiftDown && (nodeHovered || edgeHovered)) {
            setCursor(deleteCursor);
        } else if (controlDown && nodeHovered) {
            setCursor(switchCursor);
        } else if (((nodeHovered && nodeClickable) || (edgeHovered && edgeClickable))) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    /*Ermittle kleinste und größte Positionswerte der Knoten.

    int smallest_x_pos = 2000;
    int highest_x_pos = 0;
    int smallest_y_pos = 2000;
    int highest_y_pos = 0;
    np = netPanel.drawnNodes.iterator();
    while (np.hasNext()) {
        NodePoint n = (NodePoint) np.next();
        if (n.x < smallest_x_pos)
            smallest_x_pos = n.x;
        if (n.x > highest_x_pos)
            highest_x_pos = n.x;
        if (n.y < smallest_y_pos)
            smallest_y_pos = n.y;
        if (n.y > highest_y_pos)
            highest_y_pos = n.y;
    }

    graph_width = highest_x_pos - smallest_x_pos + 25;
    graph_height = highest_y_pos - smallest_y_pos + 25;*/


    public interface GraphChangedListener {
        void graphElementAdded(boolean isNode, int number);

        void graphElementDeleted(boolean isNode, int number);

        void graphElementClicked(boolean isNode, int number);
    }

}
