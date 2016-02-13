package com.resinet.views;

import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;
import com.resinet.util.GraphChangedListener;
import com.resinet.util.GraphUtil;
import com.resinet.util.NetPanelTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class NetPanel extends JPanel {
    private final GraphChangedListener listener;
    public final NetPanelController controller;

    //die Kante, die gezeichnet wird, während man die Maus gedrückt hält (beim Kanten erstellen)
    EdgeLine draggingLine;
    boolean lineDragging = false;

    boolean selectDragging = false;
    Point selectStartPoint;
    Timer selectionAnimationTimer;
    private float selectionAnimationPhase = 0;
    boolean nodesSelected = false;
    Rectangle selectionRectangle;

    Point currentMousePosition;

    Shape hoveredElement;

    public boolean nodeClickable = true;
    public boolean edgeClickable = true;

    private boolean centerGraphOnNextPaint = false;

    private final Cursor switchCursor, deleteCursor;

    static final int HOVER_DISTANCE = 7;

    public NetPanel(GraphChangedListener listener) {
        this.listener = listener;

        controller = new NetPanelController(this, listener);

        //EventListener setzen
        addMouseListener(controller);
        addMouseMotionListener(controller);
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

        selectionAnimationTimer = new Timer(30, (e) -> {
            //modulo, damit es nicht nach langer zeit zu einer Exception kommen kann
            selectionAnimationPhase = (selectionAnimationPhase + 0.5f) % 4;
            repaint();
        });

        //Aktionen für Kopieren, Ausschneiden und Einfügen registrieren
        ActionMap actionMap = this.getActionMap();
        actionMap.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        actionMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        actionMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

        //Handler für kopieren und einfügen
        setTransferHandler(new NetPanelTransferHandler());
    }

    @Override
    public void paintComponent(Graphics g) {
        ArrayList<NodePoint> drawnNodes = controller.getNodes();
        ArrayList<EdgeLine> drawnEdges = controller.getEdges();
        if (centerGraphOnNextPaint) {
            centerGraphOnNextPaint = false;
            controller.centerGraph();
        }

        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D imgGraphics = img.createGraphics();
        //Kantenglättung aktivieren
        imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        imgGraphics.setColor(Color.WHITE);
        //Hintergrund zeichnen
        imgGraphics.fillRect(0, 0, getWidth(), getHeight());

        //erst Kanten zeichnen, damit danach das Stück im inneren der Knoten überschrieben werden kann
        //und die Kanten demzufolge nur bis zu den Rändern der Knoten gehen
        imgGraphics.setColor(Color.black);

        for (EdgeLine edgeLine : drawnEdges) {
            if (edgeLine.equals(hoveredElement)) {
                imgGraphics.setStroke(new BasicStroke(2));
                imgGraphics.draw(edgeLine);
                imgGraphics.setStroke(new BasicStroke(1));
            } else {
                imgGraphics.draw(edgeLine);
            }

            String s = String.valueOf(drawnEdges.indexOf(edgeLine));
            imgGraphics.drawString(s, (float) edgeLine.textPositionX, (float) edgeLine.textPositionY);
        }

        //Knoten zeichnen
        int count = 0;
        for (NodePoint nodePoint : drawnNodes) {
            imgGraphics.setColor(Color.black);

            drawNode(imgGraphics, nodePoint);

            //Zahl im Knoten zeichnen
            String s = String.valueOf(count);
            if (count < 10)
                imgGraphics.drawString(s, (float) nodePoint.getX() + 6, (float) nodePoint.getY() + 15);
            else
                imgGraphics.drawString(s, (float) nodePoint.getX() + 3, (float) nodePoint.getY() + 15);
            count++;
        }

        imgGraphics.setColor(Color.BLACK);

        //Linie während des Kantenziehens nur zeichnen, wenn die Maus bewegt wurde, also auch ein zweiter Punkt gesetzt wurde
        if (lineDragging && draggingLine.x2 > 0 && draggingLine.y2 > 0) {
            imgGraphics.draw(draggingLine);
        }

        //Kasten zum auswählen zeichnen
        if (selectDragging) {
            //gestrichelte Linie
            imgGraphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1, 2}, 0));
            Rectangle selectRect = new Rectangle(selectStartPoint);
            selectRect.add(currentMousePosition);
            imgGraphics.draw(selectRect);
        }

        //Kasten um die ausgewählten Knoten zeichnen, wenn nicht gerade neu ausgewählt wird
        if (nodesSelected && !selectDragging) {
            //animiert gestrichelte Linie
            imgGraphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, selectionAnimationPhase));
            imgGraphics.draw(selectionRectangle);
        }

        g.drawImage(img, 0, 0, this);
    }

    /**
     * Zeichnet ein Knoten. Der Knoten wird hervorgehoben, wenn die Maus sich darüber findet
     *
     * @param imgGraphics die Zielgraphik
     * @param nodePoint   Der zu zeichnende Knoten
     */
    private void drawNode(Graphics2D imgGraphics, NodePoint nodePoint) {
        imgGraphics.setColor(Color.black);
        if (nodePoint.equals(hoveredElement)) {

            //wenn die Maus darüber ist, fett zeichnen
            if (nodePoint.c_node) {
                //oder wenns ein Terminalknoten ist, leicht größer füllen
                imgGraphics.fill(nodePoint.grow());
            } else {
                //Kreis erst weiß ausfüllen, damit die Kanten dadrin überschrieben werden
                imgGraphics.setColor(Color.white);
                imgGraphics.fill(nodePoint);
                imgGraphics.setColor(Color.black);

                imgGraphics.setStroke(new BasicStroke(2));
                imgGraphics.draw(nodePoint);
            }
        } else {
            if (nodePoint.c_node) {
                if (nodePoint.selected) {
                    //animierte umrandung, falls ausgewählt
                    //kleineren schwarzen Knoten ausgefüllt zeichnen
                    imgGraphics.fill(nodePoint.shrink());

                    imgGraphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, selectionAnimationPhase));
                    imgGraphics.draw(nodePoint);
                } else {
                    //schwarz ausfüllen
                    imgGraphics.fill(nodePoint);
                }
            } else {
                //Kreis erst weiß ausfüllen, damit die Kanten dadrin überschrieben werden
                imgGraphics.setColor(Color.white);
                imgGraphics.fill(nodePoint);
                imgGraphics.setColor(Color.black);

                //Falls der Knoten ausgewählt ist, gestrichelte Umrandung zeichnen
                if (nodePoint.selected) {
                    imgGraphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, selectionAnimationPhase));
                }

                imgGraphics.draw(nodePoint);
            }
        }

        if (nodePoint.c_node) {
            //Textfarbe weiß, da jetzt Hintergrund schwarz ist
            imgGraphics.setColor(Color.white);
        }
        imgGraphics.setStroke(new BasicStroke(1));
    }


    /**
     * Setzt die Flag, dass beim nächsten Zeichnen der Graph zentriert wird
     */
    public void centerGraphOnNextPaint() {
        centerGraphOnNextPaint = true;
    }

    /**
     * Setzt die bevorzugte Größe auf die vom Graphen eingenommene Fläche inklusive Offsets + 10 Pixel
     *
     * @return Bevorzugte Größe
     */
    @Override
    public Dimension getPreferredSize() {
        Rectangle graphRect = GraphUtil.getGraphBounds(controller.getNodes());

        return new Dimension((int) (graphRect.getX() + graphRect.getWidth()) + 10,
                (int) (graphRect.getY() + graphRect.getHeight()) + 10);
    }

    /**
     * Setzt die Auswahl zurück
     */
    public void resetSelection() {
        ArrayList<NodePoint> drawnNodes = controller.getNodes();

        nodesSelected = false;
        selectionAnimationTimer.stop();
        for (NodePoint node : drawnNodes) {
            node.selected = false;
        }
    }

    /**
     * Setzt den Graph zurück
     */
    public void resetGraph() {
        controller.resetGraph();
    }

    /**
     * Wird vom Mainframe-Controller weitergegeben, wenn das NetPanel Fokus hat und dient dazu, Copy&Paste-Aktionen zu
     * behandeln.
     *
     * @param e Das ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        controller.actionPerformed(e);
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
    void setCursorHover(boolean shiftDown, boolean controlDown) {
        if (controller.isCursorInsideSelection()) {
            setCursor(new Cursor(Cursor.MOVE_CURSOR));
        } else if (shiftDown && hoveredElement != null) {
            setCursor(deleteCursor);
        } else if (controlDown && hoveredElement instanceof NodePoint) {
            setCursor(switchCursor);
        } else if (((hoveredElement instanceof NodePoint && nodeClickable) || (hoveredElement instanceof EdgeLine && edgeClickable))) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    public ArrayList<NodePoint> getNodes() {
        return controller.getNodes();
    }

    public ArrayList<EdgeLine> getEdges() {
        return controller.getEdges();
    }

}
