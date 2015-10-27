package com.resinet.views;

import com.resinet.*;
import com.resinet.model.Edge;
import com.resinet.model.Graph;
import com.resinet.model.Node;
import com.resinet.util.MyIterator;

import java.awt.*;

/**
 * Created by Patrick on 27.10.2015.
 */
public class GraphPanel extends Panel {
    private Renet4 renet4;
    Graph graph;

    public GraphPanel(Renet4 renet4, Graph graph) {
        this.renet4 = renet4;
        this.graph = graph;
    }

    public void paint(Graphics g) {
        int h = graph.getHighestNodeNr();
        int m = graph.getNodelist().size();
        int[] px = new int[h + 1];
        int[] py = new int[h + 1];

        MyIterator it = graph.getNodelist().iterator();
        while (it.hasNext()) {
            Node node = (Node) it.next();
            int i = node.node_no;
            g.setColor(Color.black);
            int x = node.xposition;
            int y = node.yposition;
            String s = String.valueOf(node.node_no);
            if (!node.c_node)
                g.drawOval(x, y, 20, 20);
            else {
                g.fillOval(x, y, 20, 20);
                g.setColor(Color.white);
            }
            if (i < 10)
                g.drawString(s, x + 6, y + 13);
            else
                g.drawString(s, x + 1, y + 13);
        }
        g.setColor(Color.black);
        int n = graph.getEdgelist().size();
        for (int i = 0; i < n; i++) {
            Edge edge = (Edge) graph.getEdgelist().get(i);
            int x1 = edge.left_node.xposition + 10;
            int y1 = edge.left_node.yposition + 10;
            int x2 = edge.right_node.xposition + 10;
            int y2 = edge.right_node.yposition + 10;
            int x0 = (x1 + x2) / 2;
            int y0 = (y1 + y2) / 2;
            g.drawLine(x1, y1, x2, y2);
            g.drawString(String.valueOf(edge.edge_no), x0, y0);
        }
    }

    public Dimension getPreferredSize() {
        Dimension dimension = new Dimension(Math.round(renet4.graph_width) + 25, Math.round(renet4.graph_height) + 25);
        return dimension;
    }
}
