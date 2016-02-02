package com.resinet;

import com.resinet.algorithms.ProbabilityCalculator;
import com.resinet.model.CalculationParams;
import com.resinet.model.Graph;
import com.resinet.util.Constants;
import com.resinet.util.GraphSaving;
import com.resinet.util.GraphUtil;
import com.resinet.views.NetPanel;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MainframeController extends WindowAdapter implements ActionListener, NetPanel.GraphChangedListener,
        ProbabilityCalculator.CalculationProgressListener, Constants, ItemListener {
    ResinetMockup mainFrame;

    private final List<JTextField> edgeProbabilityBoxes = new ArrayList<>();
    private final List<JTextField> nodeProbabilityBoxes = new ArrayList<>();

    public void setMainFrame(ResinetMockup mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (mainFrame == null)
            return;

        AbstractButton button = (AbstractButton) e.getSource();

        if (button == mainFrame.getResetMenuItem()) {
            resetGraph();
        } else if (button == mainFrame.getOpenMenuItem()) {
            loadSavedData();
        } else if (button == mainFrame.getSaveMenuItem()) {
            saveData();
        } else if (button == mainFrame.getCloseMenuItem()) {
            windowClosing(null);
        } else if (button == mainFrame.getAboutMenuItem()) {
            //TODO about window
        } else if (button == mainFrame.getTutorialMenuItem()) {
            //TODO tutorial oder hilfe
        } else if (button == mainFrame.getCalcReliabilityBtn()) {

        } else if (button == mainFrame.getCalcResilienceBtn()) {

        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (mainFrame == null)
            return;

        AbstractButton checkbox = (AbstractButton) e.getSource();
        if (checkbox == mainFrame.getStepValuesCheckBox()) {
            mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH, true);
        } else if (checkbox == mainFrame.getConsiderEdgesBox()) {

        } else if (checkbox == mainFrame.getConsiderNodesBox()) {

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
        mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH, false);

        mainFrame.getNetPanel().resetGraph();

        mainFrame.getSingleReliabilitiesContainer().removeAll();
        mainFrame.getSingleReliabilitiesContainer().repaint();

        nodeProbabilityBoxes.clear();
        edgeProbabilityBoxes.clear();
    }

    private void loadSavedData() {
        NetPanel netPanel = mainFrame.getNetPanel();
        int width = netPanel.getWidth();
        int height = netPanel.getHeight();

        CalculationParams params = GraphSaving.inputNet(mainFrame.getContentPane(), width, height);

        if (params == null)
            return;

        //TODO daten laden
    }

    private void saveData() {
        NetPanel netPanel = mainFrame.getNetPanel();
        int width = netPanel.getWidth();
        int height = netPanel.getHeight();

        CalculationParams params = buildCalculationParams(null, true);

        GraphSaving.exportNet(params, mainFrame.getContentPane(), width, height);
    }

    private CalculationParams buildCalculationParams(CALCULATION_MODES mode, boolean forSaving) {
        NetPanel netPanel = mainFrame.getNetPanel();
        RELIABILITY_MODES reliabilityMode = mainFrame.getReliabilityMode();
        CalculationParams params;

        //Graph erzeugen
        Graph graph = GraphUtil.makeGraph(netPanel);

        //Falls der Graph nicht in Ordnung ist, wird hier schon eine Fehlermeldung ausgegeben
        if (!GraphUtil.graphIsValid(netPanel, graph))
            return null;

        params = new CalculationParams(mode, graph);
        //Falls die Parameter gespeichert werden sollen, die Graphelementlisten setzen
        if (forSaving) {
            params.setGraphLists(netPanel.drawnNodes, netPanel.drawnEdges);
        }

        if (reliabilityMode == RELIABILITY_MODES.SAME) {
            //TODO felder als Strings auslesen, damit weniger Rundungsfehler kommen
        } else {

        }

        return params;
    }

    /**
     * Zeigt einen Dialog an, damit das Fenster nur nach Bestätigung geschlossen wird.
     *
     * @param e Das Event
     */
    @Override
    public void windowClosing(@Nullable WindowEvent e) {
        int result = JOptionPane.showConfirmDialog(mainFrame.getContentPane(),
                "Do you really wanna close ResiNet?\nAll unsaved data will be lost.", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        //Programm beenden, wenn Ja angeklickt
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
//TODO statt beim klick auf ein Graphelement im Einzelzuverlässigkeitsmodus ein Fenster anzuzeigen, dass entsprechende Feld fokussieren
}
