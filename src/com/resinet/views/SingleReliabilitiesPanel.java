package com.resinet.views;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Dies ist das Panel, dass sich im Einzelzuverlässigkeitsmodus im JScrollPane befindet
 */
public class SingleReliabilitiesPanel extends JPanel {

    private Timer resizeTimer;

    private int currentColumns = 2;
    private ResizeListener resizeListener;

    public SingleReliabilitiesPanel() {
        setLayout(new GridLayout(0, currentColumns));
        resizeListener = new ResizeListener();
        resizeTimer = new Timer(100, (e) -> rebuildLayout());
        resizeTimer.setRepeats(false);
        addAncestorListener(new MyAncestorListener());
    }

    private void rebuildLayout() {
        int parentWidth = getParent().getWidth();

        if (Math.abs((parentWidth / SingleReliabilityPanel.PREF_WIDTH) - currentColumns) >= 1) {
            currentColumns = Math.max(parentWidth / SingleReliabilityPanel.PREF_WIDTH, 2);

            setLayout(new GridLayout(0, currentColumns));
        }
    }

    private class MyAncestorListener implements AncestorListener {

        @Override
        public void ancestorAdded(AncestorEvent event) {
            getParent().addComponentListener(resizeListener);
        }

        @Override
        public void ancestorRemoved(AncestorEvent event) {
            //getParent().removeComponentListener(resizeListener);
        }

        @Override
        public void ancestorMoved(AncestorEvent event) {
        }
    }

    private class ResizeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
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
