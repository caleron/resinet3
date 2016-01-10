package com.resinet.algorithms;

import com.resinet.model.*;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;
import com.resinet.util.MySet;
import com.resinet.util.Util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Für zukünftige Entwicklung soll hier die Zuverlässigkeitsberechnung stehen
 */
public class ProbabilityCalculator {
    CalculationProgressListener listener;
    CalculationParams params;

    Graph workingGraph;

    public static ProbabilityCalculator create(CalculationProgressListener listener, CalculationParams params) {
        return new ProbabilityCalculator(listener, params);
    }

    private ProbabilityCalculator(CalculationProgressListener listener, CalculationParams params) {
        this.listener = listener;
        this.params = params;
        renewWorkingGraph();
        reassignProbabilities();
    }

    /**
     * Klont den Graphen aus dem Parameterobjekt, damit er nach jeder Berechnung "frisch" ist
     * <p>
     * Dies ist vorerst nötig, da beim Auffinden minimaler Pfade (Klasse Tree) der Graph verändert wird, es aber
     * notwendig ist, dass das selbe Objekt für die Berechnung verwendet wird, damit die Referenzen auf die Elemente
     * stimmen. TODO Elemente im Graphen durch ID's identifizieren
     */
    private void renewWorkingGraph() {
        try {
            workingGraph = (Graph) Util.serialClone(params.graph);
        } catch (IOException | ClassNotFoundException e1) {
            System.err.println(e1.toString());
        }
    }

    public void calculateResilience() {

        getResilience(true);
    }

    public void calculateReliability() {
        getHeidtmannsReliability(true);
    }

    public void calculateResilienceSeries() {
        calculationSeries(CalculationSeriesMode.Resilience);
    }

    public void calculateReliabilitySeries() {
        calculationSeries(CalculationSeriesMode.Reliability);
    }

    /**
     * Startet die Zerlegung des Graphen
     *
     * @return Das Zerleg-Objekt
     */
    private Zerleg getDecomposition() {
        Zerleg zer = new Zerleg(workingGraph);
        zer.start();
        try {
            zer.join();
        } catch (InterruptedException ignored) {
        }
        return zer;
    }

    private double getHeidtmannsReliability(boolean writeOutput) {
        long startTime = new Date().getTime();
        double prob;

        reassignProbabilities();

        if (workingGraph.edgeList.size() == 1) {
            Edge e = (Edge) workingGraph.getEdgelist().get(0);

            //Prüfe, ob die beiden Endknoten K-Knoten sind
            if ((e.left_node.c_node) && (e.right_node.c_node)) {
                prob = e.prob;
                prob *= e.left_node.prob;
                prob *= e.right_node.prob;
            } else
                prob = 0;
            //updateStatus("The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob);

        } else {
            Zerleg zer = getDecomposition();

            System.out.println("Laufzeit Heidtmann bis Zerlegung: " + ((new Date()).getTime() - startTime));
            prob = 0;
            MyIterator it = zer.hz.iterator();
            while (it.hasNext()) {

                System.out.println("Neuer Pfad");

                MyList al = (MyList) it.next();
                MySet hs = (MySet) al.get(0);
                double p;
                //hs enthält hier anscheinend einen Pfad im Graphen zwischen den K-Knoten
                p = getPathProbability(hs);

                for (int i = 1; i < al.size(); i++) {
                    MySet hs1 = (MySet) al.get(i);
                    if (hs1.isEmpty())
                        continue;
                    p = p * (1 - getPathProbability(hs1));

                }
                System.out.println("Wahrscheinlichkeit: " + Double.toString(p));
                prob = prob + p;

            }

        }
        long runningTime = new Date().getTime() - startTime;
        System.out.println("Laufzeit Heidtmann: " + runningTime);
        System.out.println("Prob. Heidtmann: " + prob);

        if (writeOutput) {
            updateStatus("The reliability of the network is: " + prob);
        }

        return prob;
    }


    /**
     * Hauptmethode, die den Algorithmus zur Berechnung der Resilienz beinhaltet.
     *
     * @param writeOutput True, wenn das Ergebnis als Statusupdate ausgegeben werden soll
     * @return Die Resilienz des Netzwerks
     */
    private Double getResilience(boolean writeOutput) {
        long start = new Date().getTime();

        updateStatus("Calculating...");

        //Anzahl der Knoten
        int total_nodes = workingGraph.nodeList.size();

        //Anzahl der K-Knoten
        int c_nodes = 0;

        // Sicherung der K-Knotenliste
        String cNodeList = "";
        for (int i = 0; i < total_nodes; i++) {
            Node nodeSave = (Node) workingGraph.nodeList.get(i);
            if (nodeSave.c_node) {
                c_nodes++;
                cNodeList = cNodeList + "1";
            } else {
                cNodeList = cNodeList + "0";
            }
        }

        // Berechne Anzahl der Kombinationen
        BigInteger combinations = Util.binomial(total_nodes, c_nodes);

        // Erzeuge leere Menge für die Knotenmengen.
        Set set1 = new HashSet();

        //Hier werden alle Kombinationen an Binärstrings erzeugt, die k Einsen und n-k Nullen haben
        Set<String> combinationStrings = generateCombinations(cNodeList);

        for (String binary : combinationStrings) {
            // Erzeuge neue Teilmenge
            Set subset = new HashSet();

            // Für jeden Knoten: Wenn in der Binärzahl an der Stelle j eine 1 steht, füge den Knoten j der Teilmenge hinzu.
            for (int j = 0; j < total_nodes; j++) {
                if (binary.charAt(j) == '1') {
                    subset.add(j);
                }
            }
            set1.add(subset);
        }

        int counter = 0;
        double result = 0;

        // Für jede Kombination der K-Knoten
        for (Object c : set1) {
            renewWorkingGraph();

            HashSet d = (HashSet) c;
            // Für jeden Knoten: Falls er in der aktuellen Kombination enthalten ist, setze ihn auf "K-Knoten".
            for (int i = 0; i < total_nodes; i++) {
                // Entsprechenden Knoten holen
                Node node1 = (Node) workingGraph.nodeList.get(i);

                // Dann auf true, falls K-Knoten
                node1.c_node = d.contains(i);

                // Schreibe jeden Knoten neu in die Knotenliste.
                workingGraph.nodeList.set(i, node1);
                //graph.nodeList.set(i, node1);

            }

            // Erhöhe pro Kombination den Zähler um 1.
            counter++;

            // Wahrscheinlichkeiten neu zuordnen.
            reassignProbabilities();

            updateStatus("Step " + counter + " of " + combinations);

            // Berechne die Zuverlässigkeit für die aktuelle Kombination und addiere sie zur bisherigen Summe.
            result += getHeidtmannsReliability(false);

        }

        // Teile die Summe der Zuverlässigkeiten durch die Anzahl der Kombinationen.
        result = result / combinations.longValue();

        long runningTime = new Date().getTime() - start;
        System.out.println("Laufzeit Resilienz: " + runningTime);

        if (writeOutput) {
            updateStatus("The network has " + total_nodes + " Nodes, containing " + c_nodes + " c-Nodes.\n" +
                    "There are " + combinations + " combinations.\n" + "The resilience of the network is: " + result);
        }

        return result;
    }


    /// Hilfsmethode zum Erzeugen aller Kombinationen von K-Knoten
    private Set<String> generateCombinations(String inputString) {
        Set<String> combinationsSet = new HashSet<>();
        if (inputString.length() == 0)
            return combinationsSet;

        Character c = inputString.charAt(0);

        if (inputString.length() > 1) {
            inputString = inputString.substring(1);

            Set<String> permSet = generateCombinations(inputString);

            for (String s : permSet) {
                for (int i = 0; i <= s.length(); i++) {
                    combinationsSet.add(s.substring(0, i) + c + s.substring(i));
                }
            }
        } else {
            combinationsSet.add(c + "");
        }
        return combinationsSet;
    }


    //Für die Serienberechnung in Schritten
    private void calculationSeries(CalculationSeriesMode calculationSeriesMode) {

        //Dialog zum Datei auswählen
        JFileChooser chooseSaveFile = new JFileChooser();
        chooseSaveFile.setDialogType(JFileChooser.SAVE_DIALOG);

        FileNameExtensionFilter resultFilter = new FileNameExtensionFilter("Textdateien", "txt");
        chooseSaveFile.setFileFilter(resultFilter);
        chooseSaveFile.setDialogTitle("Save results as...");
        chooseSaveFile.setSelectedFile(new File("myResults.txt"));

        String filepath;

        int state = chooseSaveFile.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            filepath = chooseSaveFile.getSelectedFile().toString();
        } else {
            return;
        }

        //Ab hier in die Datei schreiben
        Writer writer;

        try {
            writer = new FileWriter(filepath);

            if (calculationSeriesMode == CalculationSeriesMode.Resilience) {
                writer.write("Reliability of every edge                 Reliability of every node                 Resilience of the network");
            } else {
                writer.write("Reliability of every edge                 Reliability of every node                 Reliability of the network");
            }

            writer.append(System.getProperty("line.separator"));

            //Ab hier Berechnungsserie
            int counter = 1;


            BigInteger edgeStepCount = params.edgeEndValue.subtract(params.edgeStartValue).divide(params.edgeStepSize, BigDecimal.ROUND_FLOOR)
                    .add(BigDecimal.ONE).toBigInteger();
            BigInteger nodeStepCount = params.nodeEndValue.subtract(params.nodeStartValue).divide(params.nodeStepSize, BigDecimal.ROUND_FLOOR)
                    .add(BigDecimal.ONE).toBigInteger();

            String stepCount = edgeStepCount.multiply(nodeStepCount).toString();

            for (BigDecimal currentEdgeProb = params.edgeStartValue; currentEdgeProb.compareTo(params.edgeEndValue) <= 0;
                 currentEdgeProb = currentEdgeProb.add(params.edgeStepSize)) {

                for (BigDecimal currentNodeProb = params.nodeStartValue; currentNodeProb.compareTo(params.nodeEndValue) <= 0;
                     currentNodeProb = currentNodeProb.add(params.nodeStepSize)) {

                    //TODO berechnung im Background-Thread, damit GUI während der Berechnung aktualisiert werden kann
                    updateStatus("Calculation Series: Step " + counter + " of " + stepCount);
                    counter++;

                    //Neue/aktuelle Wahrscheinlichkeiten zuweisen
                    params.nodeValue = currentNodeProb;
                    params.edgeValue = currentEdgeProb;

                    Double prob;
                    if (calculationSeriesMode == CalculationSeriesMode.Resilience) {
                        //Resilienz
                        prob = getResilience(false);
                    } else {
                        //Reliability
                        // Wahrscheinlichkeiten neu zuordnen. (wird in getResilience() auch gemacht)
                        reassignProbabilities();

                        prob = getHeidtmannsReliability(false);
                    }

                    writer.append(System.getProperty("line.separator"));

                    String reliabilityString = currentEdgeProb.toString();

                    //Nullen hinzufügen, damit alle Ausgangswahrscheinlichkeiten die selbe Länge haben
                    while (reliabilityString.length() < params.edgeStepSize.toString().length()) {
                        reliabilityString = reliabilityString + "0";
                    }

                    while (reliabilityString.length() < 42) {
                        reliabilityString = reliabilityString + " ";
                    }

                    reliabilityString += currentNodeProb.toString();
                    while (reliabilityString.length() < 42 + params.nodeStepSize.toString().length()) {
                        reliabilityString = reliabilityString + "0";
                    }

                    while (reliabilityString.length() < 84) {
                        reliabilityString = reliabilityString + " ";
                    }

                    writer.write(reliabilityString + prob);
                }

            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Wahrscheinlichkeiten neu zuordnen.
        reassignProbabilities();

        updateStatus("Calculation series finished. Please check your output file for the results.");
    }

    /**
     * Zuverlaessigkeitsberechnung mit 2 Algorithmen (ReNeT)
     */
    private void calculate_reliability_2_Algorithms() {
        Double prob;

        String resultText = "Please use the scrollbar to scroll through the results. \n \n";

        Zerleg zer = getDecomposition();

        reassignProbabilities();

        if (workingGraph.edgeList.size() == 1) {
            Edge e = (Edge) workingGraph.getEdgelist().get(0);
            if ((e.left_node.c_node) && (e.right_node.c_node)) {
                prob = e.prob;
                prob *= e.left_node.prob;
                prob *= e.right_node.prob;
            } else
                prob = 0.0;
            resultText = "The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob;

        } else {
            prob = 0.0;
            String str3 = "P=";
            int count = 0;
            MyIterator it = zer.hz.iterator();
            while (it.hasNext()) {
                MyList al = (MyList) it.next();
                MySet hs = (MySet) al.get(0);
                double p;
                p = getPathProbability(hs);
                String s = getNo(hs);
                for (int i = 1; i < al.size(); i++) {
                    MySet hs1 = (MySet) al.get(i);
                    if (hs1.isEmpty())
                        continue;
                    p = p * (1 - getPathProbability(hs1));
                    String s2 = getNo(hs1);
                    if (s2.lastIndexOf('r') == 0) {
                        s2 = s2.replace('r', 'u');
                        s2 = s2.replace('n', 'v');
                        s = s + s2;
                    } else
                        s = s + "(1-" + s2 + ")";
                }
                if (count != 0)
                    s = "+" + s;
                count++;
                prob = prob + p;
                str3 = str3 + s;
            }
            String str1 = "The network is decomposed with Heidtmann's Algorithm:\n";
            String str2 = "The reliability of the network is:\n";

            resultText = resultText + str1 + str3 + "\n" + str2 + prob;

            //now beginning to calculate the value from AZerleg

            resultText = resultText + "\n\n-------------------------\n\n";
            prob = 0.0;
            String s = "P=";
            it = zer.az.iterator();
            while (it.hasNext()) {
                ResultA ra = (ResultA) it.next();
                MySet si = ra.i;
                MySet sd = ra.d;
                double pi = 1;
                MyIterator it1 = si.iterator();
                while (it1.hasNext()) {
                    Object obj = it1.next();
                    if (obj instanceof Edge) {
                        Edge e = (Edge) obj;
                        pi = pi * e.prob;
                        s = s + "r" + String.valueOf(e.edge_no);
                    } else {
                        Node n = (Node) obj;
                        pi = pi * n.prob;
                        s = s + "r" + String.valueOf(n.node_no);
                    }
                }
                double pd = 1;
                MyIterator it2 = sd.iterator();
                while (it2.hasNext()) {
                    Object obj = it2.next();
                    if (obj instanceof Edge) {
                        Edge e = (Edge) obj;
                        pd = pd * (1 - e.prob);
                        s = s + "u" + String.valueOf(e.edge_no);
                    } else {
                        Node n = (Node) obj;
                        pd = pd * (1 - n.prob);
                        s = s + "v" + String.valueOf(n.node_no);
                    }
                }
                prob = prob + pi * pd;
                s = s + '+';
            }
            int last_id = s.lastIndexOf('+');
            if (last_id != -1) {
                s = s.substring(0, last_id); //remove the last "+"
            }
            str1 = "The network is decomposed with Abraham's Algorithm:\n";
            str2 = "The reliability of the network is:\n";

            resultText = resultText + str1 + s + "\n" + str2 + prob;

        }
        updateStatus(resultText);
        //calcReliabilityBtn.setEnabled(false);
    }


    private double getPathProbability(MySet path) {
        double p = 1;
        String output = "Pfad";

        MyIterator it = path.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Edge) {
                Edge e = (Edge) obj;
                p = p * e.prob;
                output += " e" + e.edge_no;
            } else {
                Node n = ((Node) obj);
                p = p * n.prob;
                output += " n" + n.node_no;
            }
        }
        System.out.println(Double.toString(p) + " für " + output);
        return p;
    }

    private String getNo(MySet hs) {
        String s = "";
        MyIterator it = hs.iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Edge) {
                Edge e = (Edge) obj;
                s += " r" + e.edge_no;
            } else {
                Node n = ((Node) obj);
                s += " n" + n.node_no;
            }
        }
        return s;
    }

    /**
     * Weist alle Wahrscheinlichkeiten aus den Eingabefeldern den Elementen im Graphen neu zu
     */
    private void reassignProbabilities() {
        /*
        Diese Listen sind Klone der Kantenlisten des Graphen, aber halten die selben Referenzen auf Kanten
        wie die Kantenliste des Graphen, also eine flache Kopie.
        */
        MyList edgeList = workingGraph.getEdgelist();
        int edgeCount = edgeList.size();
        //Kantenwahrscheinlichkeiten
        for (int i = 0; i < edgeCount; i++) {
            Edge e = (Edge) edgeList.get(i);

            if (params.sameReliabilityMode) {
                e.prob = params.edgeValue.doubleValue();
            } else {
                e.prob = params.edgeProbabilities[i];
            }
        }

        MyList nodeList = workingGraph.getNodelist();
        int nodeCount = nodeList.size();

        //Knotenwahrscheinlichkeiten
        for (int i = 0; i < nodeCount; i++) {
            Node e = (Node) nodeList.get(i);

            if (params.sameReliabilityMode) {
                e.prob = params.nodeValue.doubleValue();
            } else {
                e.prob = params.nodeProbabilities[i];
            }
        }
    }


    private void updateStatus(String status) {
        listener.calculationProgressChanged(status);
    }

    public interface CalculationProgressListener {
        void calculationProgressChanged(String status);
    }

    private enum CalculationSeriesMode {
        Resilience,
        Reliability
    }
}
