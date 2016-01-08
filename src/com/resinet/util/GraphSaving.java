package com.resinet.util;

import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;
import com.resinet.views.NetPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

public class GraphSaving {
    //TODO dateiformat hinzufügen, wo auch k-knoten gespeichert werden
    //Methode zum Einlesen von Netzen aus Textdateien im Pajek-Format
    public static void inputNet(NetPanel netPanel) {
        //Dialog zum Datei auswählen
        JFileChooser chooseFile = new JFileChooser();
        chooseFile.setDialogTitle("Open File");
        chooseFile.setFileFilter(new FileNameExtensionFilter("Pajek-Networks", "txt", "net"));
        int state = chooseFile.showOpenDialog(null);
        File netFile;
        if (state == JFileChooser.APPROVE_OPTION) {
            netFile = chooseFile.getSelectedFile();
        } else {
            return;
        }

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
                node1.k = false;

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

    public static void inputError(Component parentComponent) {
        //Error-Popup ausgeben
        String str = "Your input was invalid! Please choose a valid file created by Pajek or ResiNeT.";

        JOptionPane.showMessageDialog(parentComponent, str, "Warning!", JOptionPane.ERROR_MESSAGE);
    }

    public static void exportNet(NetPanel netPanel) {
        //Dialog zum Datei auswählen
        JFileChooser chooseSaveFile = new JFileChooser();
        chooseSaveFile.setDialogType(JFileChooser.SAVE_DIALOG);

        FileNameExtensionFilter pajekFilter = new FileNameExtensionFilter("Pajek-Networks", "net");
        chooseSaveFile.setFileFilter(pajekFilter);
        chooseSaveFile.setDialogTitle("Save as...");
        chooseSaveFile.setSelectedFile(new File("myNetwork.net"));

        File saveNetFile;
        String path;

        int state = chooseSaveFile.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            path = chooseSaveFile.getSelectedFile().toString();
            saveNetFile = new File(path);

            //Akzeptiert nur .net Dateien. Andernfalls Abbruch!
            if (!pajekFilter.accept(saveNetFile)) {
                exportError(netPanel);
                return;
            }
        } else {
            return;
        }

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


        } catch (IOException e) {
            e.printStackTrace();
            exportError(netPanel);
        }
    }

    public static void exportError(Component parentComponent) {
        //Error-Popup ausgeben
        String str = "Your output was invalid! Please choose a valid filepath and use the file extension '.net'.";

        JOptionPane.showMessageDialog(parentComponent, str, "Warning!", JOptionPane.ERROR_MESSAGE);
    }

}
