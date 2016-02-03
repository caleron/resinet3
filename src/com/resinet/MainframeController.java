package com.resinet;

import com.resinet.algorithms.ProbabilityCalculator;
import com.resinet.model.CalculationParams;
import com.resinet.model.Graph;
import com.resinet.util.Constants;
import com.resinet.util.GraphSaving;
import com.resinet.util.GraphUtil;
import com.resinet.util.Strings;
import com.resinet.views.NetPanel;
import com.resinet.views.ProbabilitySpinner;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class MainframeController extends WindowAdapter implements ActionListener, NetPanel.GraphChangedListener,
        ProbabilityCalculator.CalculationProgressListener, Constants, ItemListener {
    ResinetMockup mainFrame;

    private final List<ProbabilitySpinner> edgeProbabilityBoxes = new ArrayList<>();
    private final List<ProbabilitySpinner> nodeProbabilityBoxes = new ArrayList<>();

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
            startCalculation(CALCULATION_MODES.RELIABILITY);
        } else if (button == mainFrame.getCalcResilienceBtn()) {
            startCalculation(CALCULATION_MODES.RESILIENCE);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (mainFrame == null)
            return;

        AbstractButton checkbox = (AbstractButton) e.getSource();
        if (checkbox == mainFrame.getCalculationSeriesCheckBox()) {
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
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> calculationProgressChanged(currentStep));
            return;
        }
        JProgressBar progressBar = mainFrame.getCalculationProgressBar();
        progressBar.setValue(currentStep);

        mainFrame.setResultText(MessageFormat.format(Strings.getLocalizedString("calculation.progress"), currentStep, progressBar.getMaximum()));
    }

    @Override
    public void calculationFinished(String status) {

        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> calculationFinished(status));
            return;
        }
        mainFrame.setResultText(status);
        //GUI wieder aktivieren, da Berechnung fertig
        mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH);
    }

    @Override
    public void reportCalculationStepCount(Integer stepCount) {
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> reportCalculationStepCount(stepCount));
            return;
        }
        JProgressBar progressBar = mainFrame.getCalculationProgressBar();
        progressBar.setValue(0);
        progressBar.setMaximum(stepCount);
        mainFrame.setResultText(MessageFormat.format(Strings.getLocalizedString("calculation.progress"), "0", stepCount));
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

    public void resetGraph() {
        mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH);

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

        resetGraph();

        //Graphelemente hinzufügen
        netPanel.drawnEdges.addAll(params.graphEdges);
        netPanel.drawnNodes.addAll(params.graphNodes);

        //TODO einzelwahrscheinlichkeitspanel aktualisieren

        if (params.sameReliabilityMode) {
            mainFrame.setReliabilityMode(RELIABILITY_MODES.SAME);

            mainFrame.getSameReliabilityNodeProbBox().setValue(params.nodeValue);
            mainFrame.getSameReliabilityEdgeProbBox().setValue(params.edgeValue);
            if (params.calculationSeries) {
                mainFrame.getNodeEndProbabilityBox().setValue(params.nodeEndValue);
                mainFrame.getNodeProbabilityStepSizeBox().setValue(params.nodeStepSize);

                mainFrame.getEdgeEndProbabilityBox().setValue(params.edgeEndValue);
                mainFrame.getEdgeProbabilityStepSizeBox().setValue(params.edgeStepSize);
            }
        } else {
            mainFrame.setReliabilityMode(RELIABILITY_MODES.SINGLE);

            //Einzelwahrscheinlichkeiten in die Felder eintragen
            for (int i = 0; i < edgeProbabilityBoxes.size(); i++) {
                edgeProbabilityBoxes.get(i).setValue(params.edgeProbabilities[i]);
            }

            for (int i = 0; i < nodeProbabilityBoxes.size(); i++) {
                nodeProbabilityBoxes.get(i).setValue(params.nodeProbabilities[i]);
            }
        }

        //verzögert Repaint auslösen
        SwingUtilities.invokeLater(netPanel::repaint);
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

        //Das Graphobjekt muss auf jeden Fall erzeugt werden, um zu überprüfen, ob der Graph den Anforderungen entspricht
        params = new CalculationParams(mode, graph);

        //Falls die Parameter gespeichert werden sollen, die Graphelementlisten setzen
        if (forSaving) {
            params.setGraphLists(netPanel.drawnNodes, netPanel.drawnEdges);
        }

        if (reliabilityMode == RELIABILITY_MODES.SAME) {
            //Gleiche Zuverlässigkeiten
            params.setReliabilityMode(true);

            //Einzelwerte auslesen
            BigDecimal edgeStartValue = mainFrame.getSameReliabilityEdgeProbBox().getBigDecimalValue();
            BigDecimal nodeStartValue = mainFrame.getSameReliabilityNodeProbBox().getBigDecimalValue();

            //Falls eine Berechnungsserie gemacht werden soll, entsprechende Parameter setzen, sonst nur die Einzelzuverlässigkeiten
            if (mainFrame.getCalculationSeriesCheckBox().isSelected()) {
                BigDecimal edgeEndValue = mainFrame.getEdgeEndProbabilityBox().getBigDecimalValue();
                BigDecimal nodeEndValue = mainFrame.getNodeEndProbabilityBox().getBigDecimalValue();
                BigDecimal edgeStepSize = mainFrame.getEdgeProbabilityStepSizeBox().getBigDecimalValue();
                BigDecimal nodeStepSize = mainFrame.getNodeProbabilityStepSizeBox().getBigDecimalValue();

                params.setSeriesParams(edgeStartValue, edgeEndValue, edgeStepSize,
                        nodeStartValue, nodeEndValue, nodeStepSize);
            } else {
                params.setSameReliabilityParams(edgeStartValue, nodeStartValue);
            }
        } else {
            params.setReliabilityMode(false);

            //Einzelintaktwahrscheinlichkeiten einlesen
            //Dabei die Intaktwahrscheinlichkeiten auf 1 setzen, wenn sie nicht berücksichtigt werden sollen.
            int edgeCount = edgeProbabilityBoxes.size();
            int nodeCount = nodeProbabilityBoxes.size();
            BigDecimal[] edgeProbabilities = new BigDecimal[edgeCount];
            BigDecimal[] nodeProbabilities = new BigDecimal[nodeCount];

            boolean considerNodes = mainFrame.getConsiderNodesBox().isSelected();
            boolean considerEdges = mainFrame.getConsiderEdgesBox().isSelected();

            for (int i = 0; i < edgeCount; i++) {
                if (considerEdges) {
                    edgeProbabilities[i] = edgeProbabilityBoxes.get(i).getBigDecimalValue();
                } else {
                    edgeProbabilities[i] = BigDecimal.ONE;
                }
            }

            for (int i = 0; i < nodeCount; i++) {
                if (considerNodes) {
                    nodeProbabilities[i] = nodeProbabilityBoxes.get(i).getBigDecimalValue();
                } else {
                    nodeProbabilities[i] = BigDecimal.ONE;
                }
            }

            params.setSingleReliabilityParams(edgeProbabilities, nodeProbabilities);
        }

        return params;
    }

    /**
     * Startet die Berechnung
     *
     * @param mode Resilienz oder Zuverlässigkeit
     */
    private void startCalculation(CALCULATION_MODES mode) {
        CalculationParams params = buildCalculationParams(mode, false);

        //Wenn params == null, sind nicht alle Voraussetzungen erfüllt und eine Meldung wurde angezeigt
        if (params == null)
            return;

        ProbabilityCalculator calculator = ProbabilityCalculator.create(this, params);

        //Elemente der GUI während der Berechnung deaktivieren
        mainFrame.setGuiState(GUI_STATES.CALCULATION_RUNNING);

        System.out.println("startCalculation: " + Thread.currentThread().getName());
        //Starte die Berechnung
        calculator.start();
    }
//TODO statt beim klick auf ein Graphelement im Einzelzuverlässigkeitsmodus ein Fenster anzuzeigen, das entsprechende Feld fokussieren
}
