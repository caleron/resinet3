package com.resinet.views;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.resinet.*;
import com.resinet.model.*;
import com.resinet.util.MyIterator;

import javax.swing.*;

public class NetPanel extends JPanel {
    private Resinet3 resinet3;

    //die Kante, die gezeichnet wird, während man die Maus gedrückt hält (beim Kanten erstellen)
    public EdgeLine el;
    public boolean valid = false;


    public NetPanel(Resinet3 resinet3) {
        this.resinet3 = resinet3;
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

        MyIterator iterator = resinet3.drawnNodes.iterator();
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
        iterator = resinet3.drawnEdges.iterator();
        while (iterator.hasNext()) {
            EdgeLine e = (EdgeLine) iterator.next();
            imgGraphics.drawLine(e.x1, e.y1, e.x2, e.y2);
            String s = String.valueOf(resinet3.drawnEdges.indexOf(e));
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

    public interface GraphChangedListener {
        void graphChanged();
    }
}
