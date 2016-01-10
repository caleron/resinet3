package com.resinet.model;

import java.math.BigDecimal;

/**
 * Dies ist eine Wrapper-Klasse für die Berechnungsparameter
 */
public class CalculationParams {
    public Graph graph;
    public boolean sameReliabilityMode = false;
    public boolean calculationSeries = false;

    //für die Serienberechnung
    public BigDecimal edgeStartValue, edgeEndValue, edgeStepSize, nodeStartValue, nodeEndValue, nodeStepSize;

    //Für Berechnung mit gleichen Wahrscheinlichkeiten für alle Komponenten
    public BigDecimal edgeValue, nodeValue;

    //Für Berechnung mit einzelnen Wahrscheinlichkeiten
    public double[] edgeProbabilities, nodeProbabilities;

    /**
     * Erzeugt ein Objekt für die Berechnungsparameter
     *
     * @param graph Der Graph
     */
    public CalculationParams(Graph graph) {
        this.graph = graph;
    }

    /**
     * Setzt die Parameter für die Berechnung mit einzelnen Wahrscheinlichkeiten
     *
     * @param edgeProbabilities Array aus Kantenwahrscheinlichkeiten
     * @param nodeProbabilities Array aus Knotenwahrscheinlichkeiten
     */
    public void setSingleReliabilityParams(double[] edgeProbabilities, double[] nodeProbabilities) {
        this.edgeProbabilities = edgeProbabilities;
        this.nodeProbabilities = nodeProbabilities;
    }

    /**
     * Setzt den Modus fest
     *
     * @param isSameReliabilityMode True, wenn alle Komponenten die selben Wahrscheinlichkeiten haben
     */
    public void setReliabilityMode(boolean isSameReliabilityMode) {
        sameReliabilityMode = isSameReliabilityMode;
    }

    /**
     * Setzt die Parameter für die Berechnung mit gleichn Wahrscheinlichkeiten
     *
     * @param edgeValue Wahrscheinlichkeit für alle Kanten
     * @param nodeValue Wahrscheinlichkeit für alle Knoten
     */
    public void setSameReliabilityParams(BigDecimal edgeValue, BigDecimal nodeValue) {
        this.edgeValue = edgeValue;
        this.nodeValue = nodeValue;
    }

    /**
     * Setzt die Parameter für die Serienberechnung
     *
     * @param edgeStartValue Der Startwert für die Kanten
     * @param edgeEndValue   Der Endwert für die Kanten
     * @param edgeStepSize   Die Schrittweiter für die Kanten
     * @param nodeStartValue Der Startwert für die Kanten
     * @param nodeEndValue   Der Endwert für die Kanten
     * @param nodeStepSize   Die Schrittweite für die Kanten
     */
    public void setSeriesParams(BigDecimal edgeStartValue, BigDecimal edgeEndValue, BigDecimal edgeStepSize,
                                BigDecimal nodeStartValue, BigDecimal nodeEndValue, BigDecimal nodeStepSize) {
        this.edgeValue = edgeStartValue;
        this.edgeStartValue = edgeStartValue;
        this.edgeEndValue = edgeEndValue;
        this.edgeStepSize = edgeStepSize;

        this.nodeValue = nodeStartValue;
        this.nodeStartValue = nodeStartValue;
        this.nodeEndValue = nodeEndValue;
        this.nodeStepSize = nodeStepSize;

        calculationSeries = true;
    }
}
