package com.resinet.util;

import com.resinet.model.NodePoint;

import java.awt.*;
import java.util.ArrayList;

/**
 * Stellt Funktionen zur Verfügung, die beim Umgang mit Graphen helfen
 */
public class GraphUtil {

    /**
     * Bestimmt das den Graphen umgebende Rechteck
     *
     * @param nodes Die Knotenliste
     * @return Ein Rechteck, das den Graphen umschließt
     */
    public static Rectangle getGraphBounds(ArrayList<NodePoint> nodes) {

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        for (NodePoint drawnNode : nodes) {
            if (drawnNode.getX() < minX) {
                minX = (int) drawnNode.getX();
            }
            if (drawnNode.getMaxX() > maxX) {
                maxX = (int) drawnNode.getMaxX();
            }
            if (drawnNode.getY() < minY) {
                minY = (int) drawnNode.getY();
            }
            if (drawnNode.getMaxY() > maxY) {
                maxY = (int) drawnNode.getMaxY();
            }
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
}
