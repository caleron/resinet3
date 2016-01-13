package com.resinet.views;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import com.resinet.*;
import com.resinet.model.*;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;

import javax.swing.*;

public class NetPanel extends JPanel {
    private Resinet3 resinet3;

    //die Kante, die gezeichnet wird, während man die Maus gedrückt hält (beim Kanten erstellen)
    public EdgeLine draggingLine;
    public boolean lineDragging = false;

    private boolean nodeHovered = false;
    private boolean edgeHovered = false;

    public MyList drawnNodes;
    public MyList drawnEdges;

    public boolean singleReliabilityMode = false;

    private Cursor switchCursor, deleteCursor;


    public NetPanel(Resinet3 resinet3) {
        this.resinet3 = resinet3;

        drawnEdges = new MyList();
        drawnNodes = new MyList();

        addMouseListener(new MyMouseListener());
        addMouseMotionListener(new MyMouseMotionListener());
        addKeyListener(new MyKeyListener());
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        /**
         * Damit Tastendrücke erkannt werden können
         */
        setFocusable(true);

        //Eigene Cursor initialisieren
        Image switchCursorImage = getToolkit().getImage(getClass().getResource("../img/cursor_state_switch.png"));
        Image deleteCursorImage = getToolkit().getImage(getClass().getResource("../img/cursor_delete.png"));

        switchCursor = getToolkit().createCustomCursor(switchCursorImage, new Point(0, 0), "Switch State");
        deleteCursor = getToolkit().createCustomCursor(deleteCursorImage, new Point(0, 0), "Delete Element");
    }

    @Override
    public void paint(Graphics g) {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D imgGraphics = img.createGraphics();
        //Kantenglättung aktivieren
        imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        imgGraphics.setColor(Color.WHITE);
        //Hintergrund zeichnen
        imgGraphics.fillRect(0, 0, getWidth(), getHeight());
        //Knoten zeichnen
        MyIterator iterator = drawnNodes.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            imgGraphics.setColor(Color.black);
            NodePoint nodePoint = (NodePoint) iterator.next();

            if (nodePoint.c_node) {
                imgGraphics.fill(nodePoint);
                //Textfarbe weiß, da jetzt Hintergrund schwarz ist
                imgGraphics.setColor(Color.white);
            } else {
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
        //Kanten zeichnen
        imgGraphics.setColor(Color.black);
        iterator = drawnEdges.iterator();
        while (iterator.hasNext()) {
            EdgeLine edgeLine = (EdgeLine) iterator.next();
            imgGraphics.draw(edgeLine);
            String s = String.valueOf(drawnEdges.indexOf(edgeLine));
            imgGraphics.drawString(s, (float) edgeLine.textPositionX, (float) edgeLine.textPositionY);
        }

        //Linie während des Kantenziehens zeichnen
        if (lineDragging)
            imgGraphics.draw(draggingLine);

        g.drawImage(img, 0, 0, this);
    }

    /**
     * ueberschreiben der Methode update, um den Bildschirm nicht zu löschen
     */
    @Override
    public void update(Graphics g) {
        paint(g);
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
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint currentNode = (NodePoint) it.next();

                if (currentNode.contains(clickX, clickY)) {
                    //Knoten wurde angeklickt
                    nodeClicked = true;

                    if (mouseEvent.isShiftDown()) {
                        //Knoten löschen
                        drawnNodes.remove(currentNode);

                        //anliegende Kanten löschen
                        for (int i = 0; i < drawnEdges.size(); i++) {
                            EdgeLine edl = (EdgeLine) drawnEdges.get(i);
                            if (edl.startNode == currentNode || edl.endNode == currentNode) {
                                drawnEdges.remove(edl);
                                i--;
                            }
                        }
                    } else if (mouseEvent.isControlDown()) {
                        //Knoten zum K-Knoten machen oder umgekehrt
                        currentNode.c_node = !currentNode.c_node;

                    } else if (singleReliabilityMode) {
                        //Nur wenn Einzelwahrscheinlichkeiten angegeben werden können, Dialog anzeigen
                        showInputNodeProbDialog(drawnNodes.indexOf(currentNode));
                    }
                }
            }

            if (!nodeClicked) {
                boolean edgeClicked = false;
                //Wenn kein Knoten angeklickt wurde, auf Kante prüfen
                it = drawnEdges.iterator();
                while (it.hasNext()) {
                    EdgeLine edgeLine = (EdgeLine) it.next();

                    if (edgeLine.ptSegDist(clickX, clickY) < 5) {
                        if (mouseEvent.isShiftDown()) {
                            drawnEdges.remove(edgeLine);
                        } else if (singleReliabilityMode) {
                            showInputEdgeProbDialog(drawnEdges.indexOf(edgeLine));
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
                    for (int i = 0; i < drawnNodes.size(); i++) {
                        NodePoint currentNode = (NodePoint) drawnNodes.get(i);

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
                }
            }
            //neu zeichnen
            repaint();
            raiseGraphChangedEvent();
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
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint np = (NodePoint) it.next();

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
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint currentNode = (NodePoint) it.next();

                if (currentNode.contains(x, y) && draggingLine.startNode != currentNode) {

                    //Prüfen, ob eine Kante mit genau diesen anliegenden Knoten bereits existiert
                    MyIterator it2 = drawnEdges.iterator();
                    while (it2.hasNext()) {
                        EdgeLine currentEdge = (EdgeLine) it2.next();

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

                    raiseGraphChangedEvent();
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
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint nodePoint = (NodePoint) it.next();

                if (nodePoint.contains(x, y)) {
                    nodeHovered = true;
                    edgeHovered = false;
                    setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown(), true);
                    return;
                }
            }

            /**
             * Prüfen, ob der Cursor nahe einer Kante ist (nah ist hier maximal 5px Abstand)
             */
            it = drawnEdges.iterator();
            while (it.hasNext()) {
                EdgeLine edgeLine = (EdgeLine) it.next();

                if (edgeLine.ptSegDist(x, y) < 5) {
                    nodeHovered = false;
                    edgeHovered = true;
                    setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown(), true);
                    return;
                }
            }
            if (nodeHovered || edgeHovered) {
                //Cursor zurücksetzen, falls er auf keinem Element mehr ist
                nodeHovered = false;
                edgeHovered = false;
                setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown(), false);
            }
        }
    }

    /**
     * Reagiert auf Tastendrücke von Strg und Shift und verändert den Cursor entsprechend
     */
    public class MyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent keyEvent) {
            setCursorHover(keyEvent.isShiftDown(), keyEvent.isControlDown(), false);
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            setCursorHover(keyEvent.isShiftDown(), keyEvent.isControlDown(), false);
        }
    }

    /**
     * Setzt den Cursor nach angegebenen Parametern
     *
     * @param shiftDown   Ob Shift gedrückt ist
     * @param controlDown Ob Strg gedrückt ist
     * @param hovered     Ob der Cursor über einem Element liegt
     */
    private void setCursorHover(boolean shiftDown, boolean controlDown, boolean hovered) {
        if (shiftDown && (nodeHovered || edgeHovered)) {
            setCursor(deleteCursor);
        } else if (controlDown && nodeHovered) {
            setCursor(switchCursor);
        } else if (singleReliabilityMode && hovered) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    /**
     * Zeigt einen Dialog an, um die Intaktwahrscheinlichkeit einer Kante zu setzen
     *
     * @param edgeNumber Die Kantennummer
     */
    private void showInputEdgeProbDialog(int edgeNumber) {
        String str = "Input reliability of Edge " + edgeNumber;
        String res = JOptionPane.showInputDialog(this, str);
        if (res != null && res.length() > 0) {
            resinet3.edgeProbabilityBoxes.get(edgeNumber).setText(res);
        }
    }

    /**
     * Zeigt einen Dialog an, um die Intaktwahrscheinlichkeit eines Knotens zu setzen
     *
     * @param nodeNumber Die Knotennummer
     */
    private void showInputNodeProbDialog(int nodeNumber) {
        String str = "Input reliability of Node " + nodeNumber;
        String res = JOptionPane.showInputDialog(this, str);
        if (res != null && res.length() > 0) {
            resinet3.nodeProbabilityBoxes.get(nodeNumber).setText(res);
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

    /**
     * Setzt den Komponentenwahrscheinlichkeitsmodus fest
     *
     * @param sameReliabilityMode Ob gleiche Intaktwahrscheinlichkeiten für alle Kanten/Knoten gelten
     */
    public void setReliabilityMode(boolean sameReliabilityMode) {
        singleReliabilityMode = !sameReliabilityMode;
    }

    /**
     * Wird aufgerufen, wenn der Graph verändert wird
     */
    private void raiseGraphChangedEvent() {
        resinet3.graphChanged();
    }

    public interface GraphChangedListener {
        void graphChanged();
    }

}
