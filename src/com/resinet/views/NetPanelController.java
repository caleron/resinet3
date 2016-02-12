package com.resinet.views;

import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;
import com.resinet.util.GraphUtil;

import javax.swing.*;
import java.awt.*;

public class NetPanelController {
    private NetPanel netPanel;

    public NetPanelController(NetPanel netPanel) {
        this.netPanel = netPanel;
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
}
