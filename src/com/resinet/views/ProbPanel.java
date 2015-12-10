package com.resinet.views;

import com.resinet.Renet4;

import java.awt.*;

/**
 * Created by Patrick on 27.10.2015.
 */
public class ProbPanel extends Panel {
    private Renet4 renet4;

    public ProbPanel(Renet4 renet4) {
        this.renet4 = renet4;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, ((renet4.drawnEdges.size() + renet4.drawnNodes.size()) / 2 + 1) * 30);
    }

}
