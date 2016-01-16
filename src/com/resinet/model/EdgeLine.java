package com.resinet.model;

import java.awt.geom.Line2D;

/**
 * Stellt eine Kante im Graphen dar
 */
public class EdgeLine extends Line2D.Double {

    public double textPositionX, textPositionY;
    public NodePoint startNode, endNode;

    /**
     * Erzeugt eine Kante
     *
     * @param startX Startkoordinate X
     * @param startY Startkoordinate Y
     * @param endX   Endkoordinate X
     * @param endY   Endkoordinate Y
     */
    public EdgeLine(int startX, int startY, int endX, int endY) {
        super(startX, startY, endX, endY);
        refresh();
    }

    /**
     * Erzeugt eine Kante
     *
     * @param startNode Der Startknoten
     * @param endNode   Der Endknoten
     */
    public EdgeLine(NodePoint startNode, NodePoint endNode) {
        super();
        if (endNode != null) {
            setLine(startNode.x + 10, startNode.y + 10, endNode.x + 10, endNode.y + 10);
        } else {
            setLine(startNode.x + 10, startNode.y + 10, 0, 0);
        }
        this.startNode = startNode;
        this.endNode = endNode;
        refresh();
    }

    /**
     * Setzt nur den Endknoten
     *
     * @param endNode Der Endknoten
     */
    public void setEndNode(NodePoint endNode) {
        this.endNode = endNode;
        x2 = endNode.x + 10;
        y2 = endNode.y + 10;
        refresh();
    }

    /**
     * Setzt die Beschriftungstextposition
     */
    private void refresh() {
        double relativeX = getX2() - getX1();
        double relativeY = getY2() - getY1();
        textPositionX = getX2() - relativeX / 2;
        textPositionY = getY2() - relativeY / 2;
    }
}
