package com.resinet.views;

import java.awt.*;

/**
 * Created by Patrick on 27.10.2015.
 */
public class LinePanel extends Panel {
    int width, height, type, edge; //type 0: von oben rechts nach unten links, type1: von oben links nach unten rechts

    public LinePanel(int width, int height, int type, int edge) {
        this.width = width;
        this.height = height;
        this.type = type;
        this.edge = edge;
    }

    public void paint(Graphics g) {
        if (type == 0) {
            g.setColor(Color.blue);
            int x0 = width / 2 - 10;
            int y0 = height / 2;
            String s = "" + edge + "i";
            g.drawLine(width, 0, 0, height);
            g.drawString(s, x0, y0);
        }
        if (type == 1) {
            g.setColor(Color.red);
            int x0 = width / 2 - 10;
            int y0 = height / 2;
            String s = "" + edge + "d";
            g.drawLine(0, 0, width, height);
            g.drawString(s, x0, y0);
        }
    }
}
