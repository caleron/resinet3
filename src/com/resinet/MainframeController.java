package com.resinet;

import com.resinet.algorithms.ProbabilityCalculator;
import com.resinet.util.Constants;
import com.resinet.views.NetPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainframeController implements ActionListener, NetPanel.GraphChangedListener, ProbabilityCalculator.CalculationProgressListener, Constants {
    ResinetMockup mainFrame;

    public void setMainFrame(ResinetMockup mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AbstractButton button = (AbstractButton) e.getSource();

        if (button == mainFrame.getCalcReliabilityBtn()) {

        } else if (button == mainFrame.getCalcResilienceBtn()) {

        }
    }

    @Override
    public void graphElementAdded(boolean isNode, int number) {

    }

    @Override
    public void graphElementDeleted(boolean isNode, int number) {

    }

    @Override
    public void setElementReliability(boolean isNode, int number, String value) {

    }


    @Override
    public void calculationProgressChanged(Integer currentStep) {

    }

    @Override
    public void calculationFinished(String status) {

    }

    @Override
    public void reportCalculationStepCount(Integer stepCount) {

    }

    public void resetGraph() {

    }
    //TODO statt beim klick auf ein Graphelement im Einzelzuverlässigkeitsmodus ein Fenster anzuzeigen, dass entsprechende Feld fokussieren
}
