package com.resinet.views;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Dies ist das Panel, dass sich im Einzelzuverlässigkeitsmodus im JScrollPane befindet. Das Panel reagiert auf
 * Größenveränderungen des direkt übergeordneten Elements und passt daraufhin die Spaltenzahl des Layouts an.
 * <p>
 * Das würde nicht funktionieren, wenn das Panel auf die eigenen Größenveränderungen reagiert, da das übergeordnete
 * Element (JScrollPane) die Größe dieses Panels anhand von der PreferredSize dieses Panels festlegt, die wiederum durch
 * das Layout festgelegt wird. Auf diese Art und Weise stellt das JScrollPane immer automatisch die Größe und seine
 * Scrollbars ein.
 */
public class SingleReliabilitiesPanel extends JPanel {

    private Timer resizeTimer;

    private int currentColumns = 2;
    private ResizeListener resizeListener;

    public SingleReliabilitiesPanel() {
        setLayout(new GridLayout(0, currentColumns));
        resizeListener = new ResizeListener();

        //200ms nach Timerstart soll das Layout neu aufgebaut werden
        resizeTimer = new Timer(200, (e) -> rebuildLayout());

        //Timer soll nach einem Start nur ein mal die Funktion rebuildLayout() auslösen
        resizeTimer.setRepeats(false);
        addAncestorListener(new MyAncestorListener());
    }

    /**
     * Stellt die Spaltenzahl des Layouts neu ein
     */
    private void rebuildLayout() {
        int parentWidth = getParent().getWidth();

        //Falls die Breite eine neue Spalte oder eine Spalte weniger zulässt, die Spaltenzahl neu bestimmen
        if (Math.abs((parentWidth / SingleReliabilityPanel.PREF_WIDTH) - currentColumns) >= 1) {
            currentColumns = Math.max(parentWidth / SingleReliabilityPanel.PREF_WIDTH, 2);

            setLayout(new GridLayout(0, currentColumns));
        }
    }

    /**
     * Dient dazu, dem übergeordneten Element einen angepassten ComponentListener hinzuzufügen
     */
    private class MyAncestorListener implements AncestorListener {

        @Override
        public void ancestorAdded(AncestorEvent event) {
            getParent().addComponentListener(resizeListener);
        }

        @Override
        public void ancestorRemoved(AncestorEvent event) {
        }

        @Override
        public void ancestorMoved(AncestorEvent event) {
        }
    }

    /**
     * Startet einen Timer, wenn das übergeordnete Element in seiner Größe verändert wird
     */
    private class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            //Falls der Timer bereits läuft, soll er nur neugestartet werden,
            //damit der Timer wie ein "ResizeEnd"-Event wirkt
            if (resizeTimer.isRunning()) {
                resizeTimer.restart();
            } else {
                resizeTimer.start();
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

}
