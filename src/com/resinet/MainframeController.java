package com.resinet;

import com.resinet.algorithms.ProbabilityCalculator;
import com.resinet.model.CalculationParams;
import com.resinet.model.Graph;
import com.resinet.util.*;
import com.resinet.views.NetPanel;
import com.resinet.views.ProbabilitySpinner;
import com.resinet.views.SingleReliabilityPanel;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class MainframeController extends WindowAdapter implements ActionListener, GraphChangedListener,
        CalculationProgressListener, Constants, ItemListener, ChangeListener, PropertyChangeListener {
    ResinetMockup mainFrame;

    JComponent permanentFocusOwner;

    private final List<ProbabilitySpinner> edgeProbabilityBoxes = new ArrayList<>();
    private final List<ProbabilitySpinner> nodeProbabilityBoxes = new ArrayList<>();

    public MainframeController() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addPropertyChangeListener("permanentFocusOwner", this);
    }

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
        } else if (button == mainFrame.collapseOutputBtn) {
            mainFrame.setStatusBarCollapsed(true);
        } else if (permanentFocusOwner.equals(mainFrame.getNetPanel())){
            mainFrame.getNetPanel().actionPerformed(e);
        }
    }
    //TODO Zuletzt geöffnet-Liste, Graph generieren, Serienparallelreduktion, neues GUI-Layout mit größerer Zeichenfläche
    //TODO rückgängig machen, copy&paste
    //TODO Graph optimieren bezüglich Anordnung nach verschiedenen Algorithmen

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (mainFrame == null)
            return;

        AbstractButton checkbox = (AbstractButton) e.getSource();
        if (checkbox == mainFrame.getCalculationSeriesCheckBox()) {
            mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH, true);
        } else if (checkbox == mainFrame.getConsiderEdgesBox() || checkbox == mainFrame.getConsiderNodesBox()) {
            updateSingleReliabilityProbPanel();
        }
    }

    @Override
    public void graphElementAdded(boolean isNode, int number) {
        //Feld nur hinzufügen, falls der Einzelzuverlässigkeitsmodus aktiv ist und die Komponente berücksichtigt werden soll
        if (mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME)
            return;

        if ((!isNode && !mainFrame.getConsiderEdgesBox().isSelected()) || (isNode && !mainFrame.getConsiderNodesBox().isSelected()))
            return;

        addFieldToProbPanel(number, isNode);

        //Scrollpane updaten
        refreshSingleReliabilityScrollPane();
    }


    @Override
    public void graphElementDeleted(boolean isNode, int number) {
        if (mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME)
            return;

        List<ProbabilitySpinner> list;
        if (isNode) {
            list = nodeProbabilityBoxes;
        } else {
            list = edgeProbabilityBoxes;
        }

        //Alle Wahrscheinlichkeiten ein Feld vorrücken lassen
        for (int i = number; i < list.size() - 1; i++) {
            list.get(i).setValue(list.get(i + 1).getValue());
        }

        //Letztes Element aus Liste und aus GUI entfernen
        //if (list.size() > 0) {
        ProbabilitySpinner spinner = list.get(list.size() - 1);
        spinner.getParent().getParent().remove(spinner.getParent());
        list.remove(spinner);
        //}

        refreshSingleReliabilityScrollPane();
    }

    @Override
    public void graphElementClicked(boolean isNode, int number) {
        if (mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME) {
            if (isNode) {
                mainFrame.getSameReliabilityNodeProbBox().requestFocusInWindow();
            } else {
                mainFrame.getSameReliabilityEdgeProbBox().requestFocusInWindow();
            }
        } else {
            if (isNode) {
                nodeProbabilityBoxes.get(number).requestFocusInWindow();
            } else {
                edgeProbabilityBoxes.get(number).requestFocusInWindow();
            }
        }
    }

    @Override
    public void graphChanged() {
        updateSingleReliabilityProbPanel();
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
     * Wird beim Tabwechsel ausgelöst
     *
     * @param e das ChangeEvent
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        updateSingleReliabilityProbPanel();
    }

    /**
     * Zeigt einen Dialog an, damit das Fenster nur nach Bestätigung geschlossen wird.
     *
     * @param e Das Event
     */
    @Override
    public void windowClosing(@Nullable WindowEvent e) {
        /*int result = JOptionPane.showConfirmDialog(mainFrame.getContentPane(),
                "Do you really wanna close ResiNet?\nAll unsaved data will be lost.", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        //Programm beenden, wenn Ja angeklickt
        if (result == JOptionPane.YES_OPTION) {*/
        System.exit(0);
        //}
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object o = evt.getNewValue();
        if (o instanceof JComponent) {
            permanentFocusOwner = (JComponent) o;
        } else {
            permanentFocusOwner = null;
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

        CalculationParams params = GraphSaver.inputNet(mainFrame.getContentPane(), width, height);

        if (params == null)
            return;

        resetGraph();

        //Graphelemente hinzufügen
        netPanel.drawnEdges.addAll(params.graphEdges);
        netPanel.drawnNodes.addAll(params.graphNodes);

        updateSingleReliabilityProbPanel();
        if (params.probabilitiesLoaded) {
            //Nur wenn Wahrscheinlichkeiten eingespeichert wurden, diese auch laden

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
        }
        netPanel.centerGraphOnNextPaint();
        //verzögert Repaint auslösen
        SwingUtilities.invokeLater(netPanel::repaint);
    }

    /**
     * Aktualisiert das Wahrscheinlichkeitspanel
     */
    public void updateSingleReliabilityProbPanel() {
        if (mainFrame == null || mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME)
            return;

        boolean considerNodes = mainFrame.getConsiderNodesBox().isSelected();
        boolean considerEdges = mainFrame.getConsiderEdgesBox().isSelected();

        //Knoten/Kantenzahl auf 0 setzen, wenn sie nicht berücksichtigt werden sollen
        int edgeCount = considerEdges ? mainFrame.getNetPanel().drawnEdges.size() : 0;
        int edgeBoxCount = edgeProbabilityBoxes.size();
        int nodeCount = considerNodes ? mainFrame.getNetPanel().drawnNodes.size() : 0;
        int nodeBoxCount = nodeProbabilityBoxes.size();

        //Fehlende Kantenwahrscheinlichkeitsfelder hinzufügen
        for (int i = edgeBoxCount; i < edgeCount; i++) {
            addFieldToProbPanel(i, false);
        }
        //Fehlende Knotenwahrscheinlichkeitsfelder hinzufügen
        for (int i = nodeBoxCount; i < nodeCount; i++) {
            addFieldToProbPanel(i, true);
        }

        //Überflüssige Kantenwahrscheinlichkeitsfelder entfernen
        for (int i = edgeBoxCount; i > edgeCount; i--) {
            ProbabilitySpinner textField = edgeProbabilityBoxes.get(edgeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            edgeProbabilityBoxes.remove(textField);
        }

        //Überflüssige Knotenwahrscheinlichkeitsfelder entfernen
        for (int i = nodeBoxCount; i > nodeCount; i--) {
            ProbabilitySpinner textField = nodeProbabilityBoxes.get(nodeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            nodeProbabilityBoxes.remove(textField);
        }

        refreshSingleReliabilityScrollPane();
    }

    private void refreshSingleReliabilityScrollPane() {
        mainFrame.getSingleReliabilitiesScrollPane().revalidate();
        mainFrame.getSingleReliabilitiesScrollPane().repaint();
    }

    /**
     * Fügt dem Wahrscheinlichkeitspanel ein Panel für die Wahrscheinlichkeit einer Komponente hinzu
     *
     * @param number     Nummer des Felds
     * @param isNodeProb True, wenn das Feld für einen Knoten ist, false bei Kante
     */
    private void addFieldToProbPanel(int number, boolean isNodeProb) {
        SingleReliabilityPanel newPanel = new SingleReliabilityPanel(isNodeProb, number);
        JPanel singleReliabilitiesPanel = mainFrame.getSingleReliabilitiesContainer();

        if (isNodeProb) {
            //Falls es ein Knoten ist, einfach am Ende hinzufügen
            nodeProbabilityBoxes.add(newPanel.getSpinner());
            singleReliabilitiesPanel.add(newPanel);
        } else {
            //Falls es eine Kante ist
            edgeProbabilityBoxes.add(newPanel.getSpinner());

            //Vor dem ersten Knotenzuverlässigkeitsfeld einfügen, falls es dieses gibt
            if (nodeProbabilityBoxes.size() > 0) {
                int insertPosition = 0;

                for (int i = 0; i < singleReliabilitiesPanel.getComponents().length; i++) {
                    SingleReliabilityPanel panel = (SingleReliabilityPanel) singleReliabilitiesPanel.getComponent(i);
                    if (panel.isNode()) {
                        insertPosition = i;
                        break;
                    }
                }
                singleReliabilitiesPanel.add(newPanel, insertPosition);
            } else {
                //Sonst auch am Ende einfügen
                singleReliabilitiesPanel.add(newPanel);
            }
        }
    }

    private void saveData() {
        NetPanel netPanel = mainFrame.getNetPanel();
        int width = netPanel.getWidth();
        int height = netPanel.getHeight();

        CalculationParams params = buildCalculationParams(null, true);

        GraphSaver.exportNet(params, mainFrame.getContentPane(), width, height);
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

}
