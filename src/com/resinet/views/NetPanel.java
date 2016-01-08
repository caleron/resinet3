package com.resinet.views;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import com.resinet.*;
import com.resinet.model.*;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;

import javax.swing.*;

public class NetPanel extends JPanel {
    private Resinet3 resinet3;

    //die Kante, die gezeichnet wird, während man die Maus gedrückt hält (beim Kanten erstellen)
    public EdgeLine el;
    public boolean valid = false;

    public MyList drawnNodes;
    public MyList drawnEdges;

    public boolean probability_mode = false;

    public NetPanel(Resinet3 resinet3) {
        this.resinet3 = resinet3;

        drawnEdges = new MyList();
        drawnNodes = new MyList();

        addMouseListener(new MyMouseListener());
        addMouseMotionListener(new MyMouseMotionListener());
    }

    @Override
    public void paint(Graphics g) {
        //g.drawRect(0, 0, 600, 200);
        //System.out.println(netPanel.getHeight());
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D imgGraphics = img.createGraphics();

        imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        imgGraphics.setColor(Color.WHITE);

        imgGraphics.fillRect(0, 0, getWidth(), getHeight());

        MyIterator iterator = drawnNodes.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            imgGraphics.setColor(Color.black);
            NodePoint nodePoint = (NodePoint) iterator.next();
            int x = nodePoint.x;
            int y = nodePoint.y;
            String s = String.valueOf(count);
            if (!nodePoint.k)
                imgGraphics.drawOval(x, y, 20, 20);
            else {
                imgGraphics.fillOval(x, y, 20, 20);
                //Textfarbe weiß, da jetzt Hintergrund schwarz ist
                imgGraphics.setColor(Color.white);
            }
            if (count < 10)
                imgGraphics.drawString(s, x + 6, y + 13);
            else
                imgGraphics.drawString(s, x + 1, y + 13);
            count++;
        }

        imgGraphics.setColor(Color.black);
        iterator = drawnEdges.iterator();
        while (iterator.hasNext()) {
            EdgeLine e = (EdgeLine) iterator.next();
            imgGraphics.drawLine(e.x1, e.y1, e.x2, e.y2);
            String s = String.valueOf(drawnEdges.indexOf(e));
            imgGraphics.drawString(s, e.x0, e.y0);
        }


        if (valid)
            imgGraphics.drawLine(el.x1, el.y1, el.x2, el.y2);

        g.drawImage(img, 0, 0, this);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }
    //ueberschreiben der Methode update, um den Bildschirm nicht zu loeschen

    private class MyMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            if (!probability_mode) {
                int x1 = evt.getX();
                int y1 = evt.getY();
                int cnt1;
                MyIterator it = drawnNodes.iterator();
                while (it.hasNext()) {
                    NodePoint nps = (NodePoint) it.next();
                    cnt1 = drawnNodes.indexOf(nps);
                    int px = nps.x;
                    int py = nps.y;
                    px = px + 10;
                    py = py + 10;
                    int dx = x1 - px;
                    int dy = y1 - py;
                    if ((dx * dx + dy * dy) <= 100) {

                        if (evt.isShiftDown()) //zum Löschen von Knoten
                        {
                            drawnNodes.remove(nps);
                            for (int i = 0; i < drawnEdges.size(); i++) {
                                EdgeLine edl = (EdgeLine) drawnEdges.get(i);

                                if (edl.node1 == cnt1 || edl.node2 == cnt1) {
                                    drawnEdges.remove(edl);
                                    i = i - 1;
                                } else {
                                    if (edl.node1 > cnt1)
                                        edl.node1 = edl.node1 - 1;
                                    if (edl.node2 > cnt1)
                                        edl.node2 = edl.node2 - 1;
                                }
                            }
                        }

                        //Vorhandene Knoten zu K-Knoten machen
                        if (evt.isControlDown()) {
                            nps.k = !nps.k;

                            drawnNodes.set(cnt1, nps);
                        }
                        repaint();
                        return;
                    }
                    //punkt (x,y) ist in dem Kreis(px, py)
                }
                NodePoint np = new NodePoint();
                if ((x1 % 20) < 10) //Am Raster ausrichten. Kreise haben Durchmesser von 20.
                    np.x = x1 - (x1 % 20) - 10;
                else
                    np.x = x1 + 20 - (x1 % 20) - 10;
                if ((y1 % 20) < 10)
                    np.y = y1 - (y1 % 20) - 10;
                else
                    np.y = y1 + 20 - (y1 % 20) - 10;
                if (evt.isMetaDown())
                    np.k = true;
                drawnNodes.add(np);
                repaint();
            } else {
                int r = 5;
                double dr;
                int cntedge = 0;
                int x3 = evt.getX();
                int y3 = evt.getY();
                MyIterator it = drawnEdges.iterator();
                while (it.hasNext()) {
                    EdgeLine edg = (EdgeLine) it.next();
                    int x1 = edg.x1;
                    int y1 = edg.y1;
                    int x2 = edg.x2;
                    int y2 = edg.y2;
                    int diff_x2x1 = x2 - x1;
                    int diff_y2y1 = y2 - y1;
                    int min_x1x2 = x1;
                    int max_x1x2 = x2;
                    int min_y1y2 = y1;
                    int max_y1y2 = y2;
                    if (x2 < min_x1x2) {
                        min_x1x2 = x2;
                        max_x1x2 = x1;
                    }
                    if (y2 < min_y1y2) {
                        min_y1y2 = y2;
                        max_y1y2 = y1;
                    }

                    if (x1 == x2 && min_y1y2 <= y3 && y3 <= max_y1y2) {
                        dr = Math.abs(x3 - x1);
                        if (dr <= r) {
                            showInputEdgeProbDialog(cntedge);
                            break;
                        }
                    }

                    if (y1 == y2 && min_x1x2 <= x3 && x3 <= max_x1x2) {
                        dr = Math.abs(y3 - y1);
                        if (dr <= r) {
                            showInputEdgeProbDialog(cntedge);
                            break;
                        }
                    }

                    if (x1 != x2 && y1 != y2 && min_x1x2 <= x3 && x3 <= max_x1x2 && min_y1y2 <= y3 && y3 <= max_y1y2) {
                        dr = Math.sqrt(Math.pow(x3 - x1 - ((x3 - x1 + (-x3 * diff_y2y1 + y3 * diff_x2x1 + x1 *
                                diff_y2y1 - y1 * diff_x2x1) / (diff_y2y1 + Math.pow(diff_x2x1, 2) / diff_y2y1)) / diff_x2x1)
                                * diff_x2x1, 2) + Math.pow(y3 - y1 - ((x3 - x1 + (-x3 * diff_y2y1 + y3 * diff_x2x1 + x1
                                * diff_y2y1 - y1 * diff_x2x1) / (diff_y2y1 + Math.pow(diff_x2x1, 2) / diff_y2y1)) / diff_x2x1)
                                * diff_y2y1, 2));
                        if (dr <= r) {
                            showInputEdgeProbDialog(cntedge);
                            break;
                        }
                    }
                    cntedge++;
                }
                repaint();
            }
        }

        private void showInputEdgeProbDialog(int edgeNumber) {
            String str = "Input reliability of Edge " + edgeNumber;
            String res = JOptionPane.showInputDialog(this, str);
            if (res != null && res.length() > 0) {
                resinet3.edgeProbabilityTextFields[edgeNumber].setText(res);
            }
        }

        public void mousePressed(MouseEvent evt) {
            if (evt.isShiftDown() || probability_mode)
                return;
            valid = false;
            int x = evt.getX();
            int y = evt.getY();
            int cnt;
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint np = (NodePoint) it.next();
                int px = np.x;
                int py = np.y;
                px = px + 10;
                py = py + 10;
                int dx = x - px;
                int dy = y - py;
                if ((dx * dx + dy * dy) <= 100) {
                    cnt = drawnNodes.indexOf(np);
                    el = new EdgeLine();
                    el.x1 = px;
                    el.y1 = py;
                    el.node1 = cnt;
                    valid = true;
                    break;
                }
                //punkt (x,y) ist in dem Kreis(px, py)
            }
        }

        public void mouseReleased(MouseEvent evt) {
            if (!valid || evt.isShiftDown() || probability_mode)
                return;

            valid = false;
            int x = evt.getX();
            int y = evt.getY();
            int cnt;
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint np = (NodePoint) it.next();
                int px = np.x;
                int py = np.y;
                px = px + 10;
                py = py + 10;
                int dx = x - px;
                int dy = y - py;
                if ((dx * dx + dy * dy) <= 100 && el.node1 != drawnNodes.indexOf(np)) {
                    cnt = drawnNodes.indexOf(np);
                    el.x2 = px;
                    el.y2 = py;
                    el.node2 = cnt;
                    drawnEdges.add(el);
                    valid = true;

                    int lx = el.x2 - el.x1;
                    int ly = el.y2 - el.y1;
                    el.x0 = el.x2 - lx / 2;
                    el.y0 = el.y2 - ly / 2;

                    break;
                }
                //punkt (x,y) ist in dem Kreis(px, py)
            }
            repaint();
        }
    }

    private class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent evt) {
            if (valid && !probability_mode) {
                int x = evt.getX();
                int y = evt.getY();
                el.x2 = x;
                el.y2 = y;
                repaint();
            }
        }
    }


    public interface GraphChangedListener {
        void graphChanged();
    }

}
