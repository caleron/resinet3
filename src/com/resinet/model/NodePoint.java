package com.resinet.model;

import java.awt.geom.Ellipse2D;

public class NodePoint extends Ellipse2D.Double {
    public boolean c_node = false;

    public NodePoint(double x, double y, boolean c_node) {
        this(x, y);
        this.c_node = c_node;
    }

    public NodePoint(double x, double y) {
        super(x, y, 20, 20);
    }
}
