package com.resinet.views;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.resinet.*;
import com.resinet.model.*;
import com.resinet.util.MyIterator;

public class NetPanel extends Panel {
    private Renet4 renet4;
    private Image dbImage;
    private Graphics dbGraphics;

    public NetPanel(Renet4 renet4) {
        this.renet4 = renet4;
    }

    @Override
    public void paint(Graphics g) {
        //g.drawRect(0, 0, 600, 200);
        //System.out.println(netPanel.getHeight());
        BufferedImage img = new BufferedImage(625, getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D imgGraphics = img.createGraphics();

        imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        imgGraphics.setColor(Color.WHITE);

        imgGraphics.fillRect(0, 0, 625, getHeight());

        MyIterator iterator = renet4.drawnNodes.iterator();
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
                //Textfarbe weiﬂ, da jetzt Hintergrund schwarz ist
                imgGraphics.setColor(Color.white);
            }
            if (count < 10)
                imgGraphics.drawString(s, x + 6, y + 13);
            else
                imgGraphics.drawString(s, x + 1, y + 13);
            count++;
        }

        imgGraphics.setColor(Color.black);
        iterator = renet4.drawnEdges.iterator();
        while (iterator.hasNext()) {
            EdgeLine e = (EdgeLine) iterator.next();
            imgGraphics.drawLine(e.x1, e.y1, e.x2, e.y2);
            String s = String.valueOf(renet4.drawnEdges.indexOf(e));
            imgGraphics.drawString(s, e.x0, e.y0);
        }


        if (renet4.valid)
            imgGraphics.drawLine(renet4.el.x1, renet4.el.y1, renet4.el.x2, renet4.el.y2);

        g.drawImage(img, 0, 0, this);
    }

    @Override
    public void update(Graphics g) {
        /*if (dbImage == null) {
            dbImage = createImage(this.getSize().width, this.getSize().height);
            dbGraphics = dbImage.getGraphics();
        }
        dbGraphics.setColor(getBackground());
        dbGraphics.fillRect(0, 0, this.getSize().width, this.getSize().height);
        dbGraphics.setColor(getForeground());*/
        //paint(dbGraphics);
        paint(g);
        //g.drawImage(dbImage, 0, 0, this);
    }
    //ueberschreiben der Methode update, um den Bildschirm nicht zu loeschen
}
