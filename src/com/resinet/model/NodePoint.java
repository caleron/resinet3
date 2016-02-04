package com.resinet.model;

import java.awt.geom.Ellipse2D;

public class NodePoint extends Ellipse2D.Double {
    public boolean c_node = false;

    public NodePoint(double x, double y, boolean c_node) {
        this(x, y);
        this.c_node = c_node;
    }

    private NodePoint(double x, double y) {
        super(x, y, 20, 20);
    }

    public NodePoint grow() {
        NodePoint np = new NodePoint(x - 1, y - 1);
        np.width += 2;
        np.height += 2;
        return np;
    }
}
