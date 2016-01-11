package com.resinet.util;

import com.resinet.Resinet3;
import com.resinet.model.CalculationParams;
import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;
import com.resinet.views.NetPanel;
import com.sun.deploy.util.ArrayUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.nio.ch.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.tree.ExpandVetoException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.print.Book;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Vector;

public class GraphSaving {
    //TODO dateiformat hinzufügen, wo auch k-knoten gespeichert werden
    //Methode zum Einlesen von Netzen aus Textdateien im Pajek-Format
    public static void inputNet(Resinet3 resinet3, NetPanel netPanel) {
        //Dialog zum Datei auswählen
        JFileChooser chooseFile = new JFileChooser();
        chooseFile.setDialogTitle("Open File");
        FileNameExtensionFilter resinetFilter = new FileNameExtensionFilter("ResiNeTV-Networks", "resinet");
        FileNameExtensionFilter pajekFilter = new FileNameExtensionFilter("Pajek-Networks", "txt", "net");
        chooseFile.setFileFilter(resinetFilter);
        chooseFile.addChoosableFileFilter(pajekFilter);

        int state = chooseFile.showOpenDialog(null);
        File netFile;
        if (state == JFileChooser.APPROVE_OPTION) {
            netFile = chooseFile.getSelectedFile();
        } else {
            return;
        }

        resinet3.resetGraph();

        if (resinetFilter.accept(netFile)) {
            readResinetNetwork(netFile, resinet3);
        } else if (pajekFilter.accept(netFile)) {
            readPajekNetwork(netFile, netPanel);
            resinet3.updateSingleReliabilityProbPanel();
        } else {
            System.out.print("Error");
        }
    }

    private static void readPajekNetwork(File netFile, NetPanel netPanel) {

        //Ab hier zeilenweises Einlesen der ausgewählten Datei
        String actRow;
        LineNumberReader lineReader;

        try {
            lineReader = new LineNumberReader(new FileReader(netFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            actRow = lineReader.readLine();
            actRow = actRow.substring(10);
            int nodesCount = Integer.parseInt(actRow);
            int panelHeight = netPanel.getHeight() - 20;
            int panelWidth = netPanel.getWidth() - 20;

            //Erzeuge Knoten
            for (int i = 0; i < nodesCount; i++) {
                actRow = lineReader.readLine();
                NodePoint node1 = new NodePoint();
                node1.c_node = false;

                //Hole x-Koordinate
                int position = actRow.indexOf('.');

                Double xCoordinate = Double.parseDouble(actRow.substring(position - 1, position + 5));
                node1.x = (int) (xCoordinate * panelWidth);

                //Hole y-Koordinate
                Double yCoordinate = Double.parseDouble(actRow.substring(position + 10, position + 16));
                node1.y = (int) (yCoordinate * panelHeight);

                netPanel.drawnNodes.add(node1);
            }

            //Zeile überspringen: *Arcs oder *Edges
            lineReader.readLine();

            //Lies Kanten aus
            while (lineReader.ready()) {
                actRow = lineReader.readLine();

                //Achtung, Bei Leerzeile wird abgebrochen!
                if (actRow == null || actRow.length() == 0) {
                    System.out.println("Leerzeile in der Quelldatei. Lesevorgang abgebrochen.");
                    return;
                }

                //Startknoten
                String startnode = "";
                int position = 0;

                //Gehe in der Zeile nach rechts bis zur ersten Ziffer
                while (actRow.charAt(position) == ' ') {
                    position++;
                }

                //Hole Startknoten
                while (actRow.charAt(position) != ' ') {
                    startnode = startnode + actRow.charAt(position);
                    position++;
                }

                //Endknoten
                String endnode = "";

                //Gehe in der Zeile nach rechts bis zur ersten Ziffer
                while (actRow.charAt(position) == ' ') {
                    position++;
                }

                //Hole Endknoten
                while (actRow.charAt(position) != ' ') {
                    endnode = endnode + actRow.charAt(position);
                    position++;
                }

                //Füge aktuelle Kante inkl. Start- und Endknoten hinzu
                EdgeLine edge1 = new EdgeLine();
                edge1.node1 = Integer.parseInt(startnode) - 1;
                edge1.node2 = Integer.parseInt(endnode) - 1;

                NodePoint startNodePoint = (NodePoint) netPanel.drawnNodes.get(edge1.node1);
                edge1.x1 = startNodePoint.x + 10;
                edge1.y1 = startNodePoint.y + 10;

                NodePoint endNodePoint = (NodePoint) netPanel.drawnNodes.get(edge1.node2);
                edge1.x2 = endNodePoint.x + 10;
                edge1.y2 = endNodePoint.y + 10;

                int labelX = edge1.x2 - edge1.x1;
                int labelY = edge1.y2 - edge1.y1;
                edge1.x0 = edge1.x2 - labelX / 2;
                edge1.y0 = edge1.y2 - labelY / 2;

                netPanel.drawnEdges.add(edge1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            inputError(netPanel);
        }
    }

    private static void readResinetNetwork(File netFile, Resinet3 resinet3) {
        MyList drawnNodes = resinet3.netPanel.drawnNodes;
        MyList drawnEdges = resinet3.netPanel.drawnEdges;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(netFile);

            //Normalisieren (hier wahrscheinlich unnötig, aber kann Fehler vermeiden)
            doc.getDocumentElement().normalize();

            //Knotern einlesen
            NodeList nodeList = doc.getElementsByTagName("node");
            for (Integer i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;
                    NodePoint np = new NodePoint();
                    //Nummer und Koordinaten einlesen
                    Integer position = Integer.parseInt(nodeElement.getAttribute("node_number"));
                    np.x = Integer.parseInt(nodeElement.getAttribute("x"));
                    np.y = Integer.parseInt(nodeElement.getAttribute("y"));
                    np.c_node = Boolean.parseBoolean(nodeElement.getAttribute("c_node"));

                    //An der richtigen Position einfügen
                    drawnNodes.add(position, np);
                }
            }

            //Kanten einlesen
            nodeList = doc.getElementsByTagName("edge");
            for (Integer i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;
                    EdgeLine el = new EdgeLine();
                    //Anliegende Knoten bestimmen
                    Integer node1Number = Integer.parseInt(nodeElement.getAttribute("node1"));
                    Integer node2Number = Integer.parseInt(nodeElement.getAttribute("node2"));
                    NodePoint node1 = (NodePoint) drawnNodes.get(node1Number);
                    NodePoint node2 = (NodePoint) drawnNodes.get(node2Number);

                    el.node1 = node1Number;
                    el.node2 = node2Number;

                    //Position der Linie bestimmen
                    el.x1 = node1.x + 10;
                    el.y1 = node1.y + 10;
                    el.x2 = node2.x + 10;
                    el.y2 = node2.y + 10;

                    //Textkoordinaten bestimmen
                    int labelX = el.x2 - el.x1;
                    int labelY = el.y2 - el.y1;
                    el.x0 = el.x2 - labelX / 2;
                    el.y0 = el.y2 - labelY / 2;

                    //An der richtigen Position einfügen
                    Integer position = Integer.parseInt(nodeElement.getAttribute("edge_number"));
                    drawnEdges.add(position, el);
                }
            }

            //Zuverlässigkeiten einlesen
            CalculationParams calculationParams = new CalculationParams(null);
            //Modus einlesen
            nodeList = doc.getElementsByTagName("reliabilityMode");
            Node reliabilityModeNode = nodeList.item(0);
            Element reliabilityModeNodeElement = (Element) reliabilityModeNode;
            Boolean sameReliabilityMode = Boolean.parseBoolean(reliabilityModeNodeElement.getAttribute("same_reliability"));
            calculationParams.setReliabilityMode(sameReliabilityMode);

            if (sameReliabilityMode) {
                nodeList = doc.getElementsByTagName("seriesParam");
                //Parameter für die Serienberechnung und den Modus für gleiche Wahrscheinlichkeiten einlesen
                for (Integer i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;

                        switch (nodeElement.getAttribute("type")) {
                            case "nodeStart":
                                calculationParams.nodeStartValue = new BigDecimal(nodeElement.getAttribute("value"));
                                calculationParams.nodeValue = calculationParams.nodeStartValue;
                                break;
                            case "nodeEnd":
                                calculationParams.nodeEndValue = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                            case "nodeStepSize":
                                calculationParams.nodeStepSize = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                            case "edgeStart":
                                calculationParams.edgeStartValue = new BigDecimal(nodeElement.getAttribute("value"));
                                calculationParams.edgeValue = calculationParams.edgeStartValue;
                                break;
                            case "edgeEnd":
                                calculationParams.edgeEndValue = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                            case "edgeStepSize":
                                calculationParams.edgeStepSize = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                        }
                    }
                }
            } else {
                nodeList = doc.getElementsByTagName("singleReliability");

                //Arraylisten mit der größe der gesamten Liste initialisieren, damit mit Sicherheit alle reinpassen
                Vector<Double> edgeProbabilities = new Vector<>(),
                        nodeProbabilities = new Vector<>();

                for (Integer i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;
                        Integer position = Integer.parseInt(nodeElement.getAttribute("number"));
                        Double reliability = Double.parseDouble(nodeElement.getAttribute("reliability"));
                        if (nodeElement.getAttribute("type").equals("node")) {
                            nodeProbabilities.set(position, reliability);
                        } else {
                            edgeProbabilities.set(position, reliability);
                        }
                    }
                }
                //Vector in primitive Arrays umwandeln und dann in das Objekt einspeisen
                calculationParams.setSingleReliabilityParams(Util.toPrimitiveDoubleArray(edgeProbabilities),
                        Util.toPrimitiveDoubleArray(nodeProbabilities));

            }

            //Oberfläche von Resinet updaten lassen
            resinet3.updateSingleReliabilityProbPanel();
            resinet3.loadCalculationParams(calculationParams);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void inputError(Component parentComponent) {
        //Error-Popup ausgeben
        String str = "Your input was invalid! Please choose a valid file created by Pajek or ResiNeT.";

        JOptionPane.showMessageDialog(parentComponent, str, "Warning!", JOptionPane.ERROR_MESSAGE);
    }

    public static void exportNet(Resinet3 resinet3, NetPanel netPanel) {
        //Dialog zum Datei auswählen
        JFileChooser chooseSaveFile = new JFileChooser();
        chooseSaveFile.setDialogType(JFileChooser.SAVE_DIALOG);

        //Speichern in zwei Formaten anbieten
        FileNameExtensionFilter pajekFilter = new FileNameExtensionFilter("Pajek-Networks", "net");
        FileNameExtensionFilter resinetvFilter = new FileNameExtensionFilter("ResiNeTV-Networks with Reliabilities", "resinet");
        chooseSaveFile.setFileFilter(resinetvFilter);
        chooseSaveFile.addChoosableFileFilter(pajekFilter);
        chooseSaveFile.setDialogTitle("Save as...");
        chooseSaveFile.setSelectedFile(new File("myNetwork.resinet"));

        File saveNetFile;
        String path;

        int state = chooseSaveFile.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            path = chooseSaveFile.getSelectedFile().toString();
            saveNetFile = new File(path);

            if (pajekFilter.accept(saveNetFile)) {
                writePajekNetwork(path, netPanel);
            } else if (resinetvFilter.accept(saveNetFile)) {
                writeResinetNetwork(path, resinet3);
            } else {
                //Dateierweiterung fehlt wohl
                //Ausgewählten Filter finden
                javax.swing.filechooser.FileFilter selectedFilter = chooseSaveFile.getFileFilter();

                //entsprechende Dateierweiterung an den Pfad anfügen und dann so speichern
                if (selectedFilter.equals(pajekFilter)) {
                    writePajekNetwork(path + ".net", netPanel);
                } else if (selectedFilter.equals(resinetvFilter)) {
                    writeResinetNetwork(path + ".resinet", resinet3);
                }
            }
        }
    }

    /**
     * Schreibt das Netzwerk in ein Pajek-kompatibles Dateiformat
     *
     * @param path     Der Zielpfad
     * @param netPanel Das NetPanel als Datenquelle
     */
    private static void writePajekNetwork(String path, NetPanel netPanel) {
        //Ab hier in die Datei schreiben
        Writer writer;

        try {
            writer = new FileWriter(path);
            writer.write("*Vertices " + netPanel.drawnNodes.size());


            int nodesDigitsCount = String.valueOf(netPanel.drawnNodes.size()).length();

            //Für jeden Knoten eine Zeile schreiben
            for (int i = 1; i < netPanel.drawnNodes.size() + 1; i++) {
                writer.append(System.getProperty("line.separator"));

                int digitsCurrentNode = String.valueOf(i).length();
                int addSpaces = nodesDigitsCount - digitsCurrentNode;
                int spacesLength = 0;

                //Leerzeichen vorne auffüllen
                for (int j = 0; j < addSpaces + 1; j++) {
                    writer.write(" ");
                    spacesLength++;
                }

                //Schreibe Knotennummer
                String nodesNumber = Integer.toString(i) + " \"v" + Integer.toString(i) + "\"";
                writer.write(nodesNumber);

                //Nochmal so viele Leerzeichen auffüllen bis Position 46 erreicht ist
                for (int k = 0; k < (46 - spacesLength - nodesNumber.length()); k++) {
                    writer.write(" ");
                }

                NodePoint node = (NodePoint) netPanel.drawnNodes.get(i - 1);
                double xCoordinate = (double) node.x;
                double yCoordinate = (double) node.y;

                if (xCoordinate < 5) {
                    xCoordinate = 5;
                }
                if (yCoordinate < 5) {
                    yCoordinate = 5;
                }

                xCoordinate = xCoordinate / (double) (netPanel.getWidth());
                yCoordinate = yCoordinate / (double) (netPanel.getHeight());

                // Auf 4 Nachkommastellen runden
                xCoordinate = Math.round(xCoordinate * 10000.0) / 10000.0;
                yCoordinate = Math.round(yCoordinate * 10000.0) / 10000.0;

                String xCoordinateString = Double.toString(xCoordinate);
                String yCoordinateString = Double.toString(yCoordinate);

                // Stellen auffüllen, z.b. 0.25 => 0.2500
                while (xCoordinateString.length() < 6) {
                    xCoordinateString = xCoordinateString + "0";
                }

                while (yCoordinateString.length() < 6) {
                    yCoordinateString = yCoordinateString + "0";
                }

                //Schreibe Koordinaten in die Datei
                writer.write(xCoordinateString + "    " + yCoordinateString + "    0.5000");
            }

            writer.append(System.getProperty("line.separator"));
            writer.write("*Edges");

            //Für jede Kante eine Zeile
            for (int i = 0; i < netPanel.drawnEdges.size(); i++) {
                writer.append(System.getProperty("line.separator"));
                EdgeLine edge = (EdgeLine) netPanel.drawnEdges.get(i);
                String node1 = Integer.toString(edge.node1 + 1);
                String node2 = Integer.toString(edge.node2 + 1);

                while (node1.length() < Integer.toString(netPanel.drawnNodes.size()).length() + 1) {
                    node1 = " " + node1;
                }

                while (node2.length() < Integer.toString(netPanel.drawnNodes.size()).length() + 1) {
                    node2 = " " + node2;
                }
                writer.write(node1 + " " + node2 + " 1");
            }
            writer.close();

            JOptionPane.showMessageDialog(netPanel, "Successfully saved.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(netPanel, "Saving failed!");
        }
    }

    /**
     * Speichert den Graphen und alle Parameter in einem eigenen XML-Format
     *
     * @param path     Der Zieldateipfad
     * @param resinet3 Das Hauptfenster
     */
    private static void writeResinetNetwork(String path, Resinet3 resinet3) {
        CalculationParams params = resinet3.buildCalculationParams(true);

        if (params == null) {
            //Voraussetzungen zum Berechnen sind nicht erfüllt
            //Fragen, ob dann nur der Graph gespeichert werden soll

            int dialogResult = JOptionPane.showConfirmDialog(resinet3,
                    "Not all necessary input values are given to save the graph with reliability.\n Only save the graph? " +
                            "Otherwise saving will be cancelled."
                    , "Save only Graph?", JOptionPane.OK_CANCEL_OPTION);

            if (dialogResult == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        MyList drawnNodes = resinet3.netPanel.drawnNodes;
        MyList drawnEdges = resinet3.netPanel.drawnEdges;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("graph");
            doc.appendChild(rootElement);

            //Knoten schreiben
            for (Integer i = 0; i < drawnNodes.size(); i++) {
                NodePoint graphNode = (NodePoint) drawnNodes.get(i);

                Element node = doc.createElement("node");
                node.setAttribute("node_number", i.toString());
                node.setAttribute("x", Integer.toString(graphNode.x));
                node.setAttribute("y", Integer.toString(graphNode.y));
                node.setAttribute("c_node", Boolean.toString(graphNode.c_node));

                rootElement.appendChild(node);
            }


            //Kanten schreiben
            for (Integer i = 0; i < drawnEdges.size(); i++) {
                EdgeLine graphEdge = (EdgeLine) drawnEdges.get(i);

                Element edge = doc.createElement("edge");
                edge.setAttribute("edge_number", i.toString());
                edge.setAttribute("node1", Integer.toString(graphEdge.node1));
                edge.setAttribute("node2", Integer.toString(graphEdge.node2));

                rootElement.appendChild(edge);
            }

            //Wahrscheinlichkeiten nur ausfüllen, wenn alle Felder zur Berechnung ausgefüllt wurden
            if (params != null) {
                //Wahrscheinlichkeiten schreiben
                Element reliabilityMode = doc.createElement("reliabilityMode");
                reliabilityMode.setAttribute("same_reliability", Boolean.toString(params.sameReliabilityMode));
                rootElement.appendChild(reliabilityMode);

                if (params.sameReliabilityMode) {

                    Element nodeStartValue = doc.createElement("seriesParam");
                    nodeStartValue.setAttribute("type", "nodeStart");
                    nodeStartValue.setAttribute("value", params.nodeValue.toString());
                    rootElement.appendChild(nodeStartValue);

                    Element edgeStartValue = doc.createElement("seriesParam");
                    edgeStartValue.setAttribute("type", "edgeStart");
                    edgeStartValue.setAttribute("value", params.edgeValue.toString());
                    rootElement.appendChild(edgeStartValue);

                    if (params.calculationSeries) {
                        //Serienberechnungsparameter speichern
                        Element nodeEndValue = doc.createElement("seriesParam");
                        nodeStartValue.setAttribute("type", "nodeEnd");
                        nodeEndValue.setAttribute("value", params.nodeEndValue.toString());
                        rootElement.appendChild(nodeEndValue);

                        Element nodeStepSizeValue = doc.createElement("seriesParam");
                        nodeStartValue.setAttribute("type", "nodeStepSize");
                        nodeStepSizeValue.setAttribute("value", params.nodeStepSize.toString());
                        rootElement.appendChild(nodeStepSizeValue);


                        Element edgeEndValue = doc.createElement("seriesParam");
                        edgeStartValue.setAttribute("type", "edgeEnd");
                        edgeEndValue.setAttribute("value", params.edgeEndValue.toString());
                        rootElement.appendChild(edgeEndValue);

                        Element edgeStepSizeValue = doc.createElement("seriesParam");
                        edgeStartValue.setAttribute("type", "edgeStepSize");
                        edgeStepSizeValue.setAttribute("value", params.edgeStepSize.toString());
                        rootElement.appendChild(edgeStepSizeValue);
                    }
                } else {
                    //Einzelwahrscheinlichkeitsmodus

                    //Knotenwahrscheinlichkeiten
                    for (Integer i = 0; i < params.nodeProbabilities.length; i++) {
                        Element singleReliability = doc.createElement("singleReliability");
                        singleReliability.setAttribute("type", "node");
                        singleReliability.setAttribute("number", i.toString());
                        singleReliability.setAttribute("reliability", Double.toString(params.nodeProbabilities[i]));
                        rootElement.appendChild(singleReliability);
                    }

                    //Kantenwahrscheinlichkeiten
                    for (Integer i = 0; i < params.edgeProbabilities.length; i++) {
                        Element singleReliability = doc.createElement("singleReliability");
                        singleReliability.setAttribute("type", "edge");
                        singleReliability.setAttribute("number", i.toString());
                        singleReliability.setAttribute("reliability", Double.toString(params.edgeProbabilities[i]));
                        rootElement.appendChild(singleReliability);
                    }
                }
            }

            //In XML-Datei schreiben
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));

            transformer.transform(source, result);
            JOptionPane.showMessageDialog(resinet3, "Successfully saved.");

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(resinet3, "Saving failed!");
        }

    }

}
