package com.resinet.views;

import java.awt.*;
import java.net.URL;

/**
 * Created by Patrick on 27.10.2015.
 */
public class FlagPanel extends Panel {
    private Image dbImage;
    private Graphics dbGraphics;
    //String s = "uk.gif";
    public String s = "com/resinet/img/logo.jpg";

    //int width;
    int width = 20;

    public void paint(Graphics g) {
        Image img;
        URL url = null;
        // URL url = getClass().getResource(s);
        try {

            url = getClass().getResource(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        img = getToolkit().getImage(url);
        //Point p = ch.getLocation();
        //width = (int)(p.x+ch.getSize().width+2);
        //g.drawImage(img, width, -6, this);
        g.drawImage(img, 250, -6, this);
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
    //ueberschreibe die Methode update, um den Bildschirm nicht zu loeschen
}
