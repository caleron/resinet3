package com.resinet.views;

import com.resinet.Resinet3;

import java.awt.*;

/**
 * Created by Patrick on 27.10.2015.
 */
public class ProbPanel extends Panel {
    private Resinet3 resinet3;

    public ProbPanel(Resinet3 resinet3) {
        this.resinet3 = resinet3;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, ((resinet3.drawnEdges.size() + resinet3.drawnNodes.size()) / 2 + 1) * 30);
    }

}
