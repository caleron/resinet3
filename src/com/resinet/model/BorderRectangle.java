package com.resinet.model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Diese Klasse stellt ein Reckteck dar, das überprüfen kann, ob ein Punkt nahe einer der Eckpunkte oder Kanten liegt.
 */
public class BorderRectangle extends Rectangle2D.Double {
    private static final long serialVersionUID = 2401721394399277623L;
    private boolean renewBounds = true;
    private Line2D leftBorder, topBorder, rightBorder, bottomBorder;
    private Point2D topLeft, topRight, bottomLeft, bottomRight;

    /**
     * Erzeugt ein Rechteck mit den angegebenen Parametern.
     *
     * @param x      X-koordinate der oberen linken Ecke
     * @param y      Y-Koordinate der oberen linken Ecke
     * @param width  Breite des Rechtecks
     * @param height Höhe des Recktecks
     */
    public BorderRectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Erzeugt ein neues Reckteck, das die zwei Punkte umfasst.
     *
     * @param p1 Erster Punkt
     * @param p2 Zweiter Punkt
     */
    public BorderRectangle(Point2D p1, Point2D p2) {
        x = Math.min(p1.getX(), p2.getX());
        y = Math.min(p1.getY(), p2.getY());
        width = Math.max(p1.getX(), p2.getX()) - x;
        height = Math.max(p1.getY(), p2.getY()) - y;
    }

    /**
     * Addiert den Wert von amount zur X-Koordinate dieses Rechtecks
     *
     * @param amount der zu addierende Wert
     */
    public void addToX(int amount) {
        this.x += amount;
        renewBounds = true;
    }

    /**
     * Addiert den Wert von amount zur Y-Koordinate dieses Rechtecks
     *
     * @param amount der zu addierende Wert
     */
    public void addToY(int amount) {
        this.y += amount;
        renewBounds = true;
    }

    /**
     * Erweitert das Rechteck um die angegebene Größe nach links.
     *
     * @param amount Größe
     */
    public void resizeLeft(int amount) {
        x -= amount;
        width += amount;
        renewBounds = true;
    }

    /**
     * Erweitert das Rechteck um die angegebene Größe nach oben.
     *
     * @param amount Größe
     */
    public void resizeTop(int amount) {
        y -= amount;
        height += amount;
        renewBounds = true;
    }

    /**
     * Erweitert das Rechteck nach rechts um die angegebene Größe
     *
     * @param amount Größe
     */
    public void resizeRight(int amount) {
        width += amount;
        renewBounds = true;
    }

    /**
     * Erweitert das Reckteck nach unten um die angegebene Größe
     *
     * @param amount Größe
     */
    public void resizeBottom(int amount) {
        height += amount;
        renewBounds = true;
    }

    /**
     * Gibt die Position der oberen linken Ecke zurück.
     *
     * @return Die obere linke Ecke
     */
    public Point2D getLocation() {
        return new Point2D.Double(x, y);
    }

    /**
     * Setzt die Position neu.
     *
     * @param x neue X-Koordinate der Ecke links oben
     * @param y neue Y-Koordinate der Ecke links oben
     */
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
        renewBounds = true;
    }

    /**
     * Setzt die Position neu.
     *
     * @param point Der neue Punkt links oben
     */
    public void setLocation(Point2D point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    /**
     * Prüft, ob der minimale Abstand des angegebene Punktes zu einer Linie des Rechtecks unter einem Grenzwert liegt.
     * Gibt eine Zahl zurück, die auf die entsprechende Linie deutet.
     *
     * @param x        X-Koordinate
     * @param y        Y-Koordinate
     * @param distance maximale Distanz des Cursors zu einer Linie
     * @return 0, wenn minimale Distanz > distance, 1 für links, 2 für oben, 3 für rechts, 4 für unten, 5 für oben
     * links, 6 für oben rechts, 7 für unten rechts, 8 für unten links
     */
    public int getResizableBorder(int x, int y, int distance) {
        if (renewBounds) {
            recreateBounds();
        }

        double lowestDistance = Integer.MAX_VALUE;
        double currentDistance;
        int border = 0;

        //Ecken prüfen
        currentDistance = topLeft.distance(x, y);
        if (currentDistance <= lowestDistance) {
            lowestDistance = currentDistance;
            border = 5;
        }

        currentDistance = topRight.distance(x, y);
        if (currentDistance <= lowestDistance) {
            lowestDistance = currentDistance;
            border = 6;
        }

        currentDistance = bottomRight.distance(x, y);
        if (currentDistance <= lowestDistance) {
            lowestDistance = currentDistance;
            border = 7;
        }

        currentDistance = bottomLeft.distance(x, y);
        if (currentDistance <= lowestDistance) {
            lowestDistance = currentDistance;
            border = 8;
        }

        if (lowestDistance <= distance) {
            return border;
        }

        //Kanten prüfen
        currentDistance = leftBorder.ptSegDist(x, y);
        if (currentDistance < lowestDistance) {
            lowestDistance = currentDistance;
            border = 1;
        }
        currentDistance = topBorder.ptSegDist(x, y);
        if (currentDistance < lowestDistance) {
            lowestDistance = currentDistance;
            border = 2;
        }
        currentDistance = rightBorder.ptSegDist(x, y);
        if (currentDistance < lowestDistance) {
            lowestDistance = currentDistance;
            border = 3;
        }
        currentDistance = bottomBorder.ptSegDist(x, y);
        if (currentDistance < lowestDistance) {
            lowestDistance = currentDistance;
            border = 4;
        }


        if (lowestDistance <= distance) {
            return border;
        }
        return 0;
    }

    /**
     * Erstellt die Eckpunkte und Kanten neu
     */
    private void recreateBounds() {
        leftBorder = new Line2D.Double(x, y, x, y + height);
        topBorder = new Line2D.Double(x, y, x + width, y);
        rightBorder = new Line2D.Double(x + width, y, x + width, y + height);
        bottomBorder = new Line2D.Double(x, y + height, x + width, y + height);

        topLeft = new Point2D.Double(x, y);
        topRight = new Point2D.Double(x + width, y);
        bottomLeft = new Point2D.Double(x, y + height);
        bottomRight = new Point2D.Double(x + width, y + height);

        renewBounds = false;
    }
}
