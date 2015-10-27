package com.resinet.views;

import java.awt.*;
import com.resinet.*;
import com.resinet.model.*;

/**
 * Created by Patrick on 27.10.2015.
 */
public class NetPanel extends Panel {
    private Renet4 renet4;
    private Image dbImage;
    private Graphics dbGraphics;

    public NetPanel(Renet4 renet4) {
        this.renet4 = renet4;
    }

    public void paint(Graphics g) {
        //g.drawRect(0, 0, 600, 200);
        //System.out.println(panel2.getHeight());
        g.drawRect(0, 0, 625, renet4.panel2.getHeight());

        MyIterator it = renet4.nodes.iterator();
        int cnt = 0;
        while (it.hasNext()) {
            g.setColor(Color.black);
            NodePoint np = (NodePoint) it.next();
            int x = np.x;
            int y = np.y;
            String s = String.valueOf(cnt);
            if (!np.k)
                g.drawOval(x, y, 20, 20);
            else {
                g.fillOval(x, y, 20, 20);
                g.setColor(Color.white);
            }
            if (cnt < 10)
                g.drawString(s, x + 6, y + 13);
            else
                g.drawString(s, x + 1, y + 13);
            cnt++;
        }

        g.setColor(Color.black);
        it = renet4.edges.iterator();
        while (it.hasNext()) {
            EdgeLine e = (EdgeLine) it.next();
            g.drawLine(e.x1, e.y1, e.x2, e.y2);
            String s = String.valueOf(renet4.edges.indexOf(e));
            g.drawString(s, e.x0, e.y0);
        }


        if (renet4.valid)
            g.drawLine(renet4.el.x1, renet4.el.y1, renet4.el.x2, renet4.el.y2);
    }

    public void update(Graphics g) {
        if (dbImage == null) {
            dbImage = createImage(this.getSize().width, this.getSize().height);
            dbGraphics = dbImage.getGraphics();
        }
        dbGraphics.setColor(getBackground());
        dbGraphics.fillRect(0, 0, this.getSize().width, this.getSize().height);
        dbGraphics.setColor(getForeground());
        paint(dbGraphics);
        g.drawImage(dbImage, 0, 0, this);
    }
    //ueberschreiben der Methode update, um den Bildschirm nicht zu loeschen
}
