package com.resinet;/* Resinet3.java */

import com.resinet.algorithms.Con_check;
import com.resinet.algorithms.Zerleg;
import com.resinet.model.*;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;
import com.resinet.util.MySet;
import com.resinet.util.Util;
import com.resinet.views.*;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.*;
import java.util.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Resinet3 extends JFrame
        implements ActionListener, NetPanel.GraphChangedListener {
    private NetPanel netPanel;
    private ProbPanel probabilityFieldsPanel;
    private JTextArea graphPanelTextArea;
    private JTextField edgeEndProbabilityBox, edgeProbabilityStepSizeBox, nodeEndProbabilityBox, nodeProbabilityStepSizeBox;
    private JButton drawBtn, resetGraphBtn, differentReliabilitiesOkBtn, sameReliabilityOkBtn, resetProbabilitiesBtn,
            probabilitiesOkBtn, calcReliabilityBtn, resilienceBtn, inputNetBtn, exportNetBtn;
    private boolean sameReliabilityMode = false;

    public MyList drawnNodes;
    public MyList drawnEdges;

    private boolean probability_mode = false;
    private MyMouseListener drawMouseListener;
    private MyMouseMotionListener drawMouseMoveListener;
    private JTextField[] edgeProbabilityTextFields;
    private JTextField[] nodeProbabilityTextFields;
    private float[] edgeProbabilities;
    private float[] nodeProbabilities;
    private float prob;
    private String resultText;
    private JTextArea resultTextArea;

    private Graph graph;
    public float graph_width;
    public float graph_height;

    private Zerleg zer;

    //private Color backgroundColor = new Color(85, 143, 180);

    private JScrollPane probabilityFieldsScrollPane;
    private JCheckBox reliabilityCompareCheckBox;

    private static Resinet3 mainFrame;

    private Resinet3() {
        init();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            mainFrame = new Resinet3();
            mainFrame.pack();
            mainFrame.setSize(700, 825);
            mainFrame.setMinimumSize(new Dimension(600, 700));
            mainFrame.setTitle("ResinetV");
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }

    /**
     * Leitet alle Initialisierungen ein
     */
    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Windows Look-and-Feel setzen
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        drawnNodes = new MyList();
        drawnEdges = new MyList();

        //Initialwerte setzen
        edgeStartValue = BigDecimal.ZERO;
        edgeEndValue = BigDecimal.ZERO;
        edgeStepSize = BigDecimal.ZERO;
        nodeStartValue = BigDecimal.ZERO;
        nodeEndValue = BigDecimal.ZERO;
        nodeStepSize = BigDecimal.ZERO;

        calculationSeriesMode = 0;
        onlyReliabilityFast = false;

        GridBagLayout mainLayout = new GridBagLayout();
        setLayout(mainLayout);

        initLogo();

        initGraphPanel();

        initProbabilitiesPanel();

        initCalculatePanel();

        initOutputTextPanel();
    }

    /**
     * Zeigt das Logo an
     */
    private void initLogo() {
        Image logo = getToolkit().getImage(getClass().getResource("img/logo_neu.png"));
        prepareImage(logo, this);
        JLabel logoLabel = new JLabel(new ImageIcon(logo));
        GridBagConstraints gbc = makegbc(0, 0, 1, 1, 0, 0);
        add(logoLabel, gbc);
    }

    /**
     * Baut das Oberste Panel mit Graph auf
     */
    private void initGraphPanel() {
        JPanel graphPanel = new JPanel();
        graphPanel.setBorder(BorderFactory.createTitledBorder("Please input your network model:"));
        GridBagConstraints gbc = makegbc(0, 1, 1, 6, 1, 0.4);
        add(graphPanel, gbc);

        GridBagLayout graphPanelLayout = new GridBagLayout();
        graphPanel.setLayout(graphPanelLayout);

        drawBtn = new JButton("Draw Network");
        drawBtn.addActionListener(this);
        gbc = makegbc(0, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
        graphPanel.add(drawBtn, gbc);

        resetGraphBtn = new JButton("Reset Network");
        resetGraphBtn.setEnabled(false);
        resetGraphBtn.addActionListener(this);
        gbc = makegbc(1, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
        graphPanel.add(resetGraphBtn, gbc);

        //Button Input Network
        inputNetBtn = new JButton("Load Network");
        inputNetBtn.setEnabled(true);
        inputNetBtn.addActionListener(this);
        gbc = makegbc(2, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
        graphPanel.add(inputNetBtn, gbc);

        //Button Output Network
        exportNetBtn = new JButton("Save Network");
        exportNetBtn.setEnabled(true);
        exportNetBtn.addActionListener(this);
        gbc = makegbc(3, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
        graphPanel.add(exportNetBtn, gbc);


        netPanel = new NetPanel(this);
        drawMouseListener = new MyMouseListener();
        drawMouseMoveListener = new MyMouseMotionListener();
        netPanel.setBackground(Color.white);
        netPanel.setSize(625, 315);

        netPanel.setVisible(false);
        gbc = makegbc(0, 1, 4, 5, 1, 1);

        graphPanel.add(netPanel, gbc);
        String notetext = "On this panel you can draw your network model after a click on the \"Draw\" button.\n" +
                "Press the left button to draw a node and the right button to draw a connection-node. " +
                "Delete a node by holding the <shift>-key an pressing the left button.\nTo draw an edge press the " +
                "left button when the mouse pointer is on a node and hold it. Then drag the mouse to another " +
                "node and release it. For deleting an edge delete its corresponding drawnNodes.\nAfter you have " +
                "finished, click the \"Ok\" button.\n\nYou can also import a previously created network " +
                "(ResiNeT or Pajek) by clicking the \"Load\" button. To turn an existing node into a connection-node " +
                "hold the Ctrl-Key while left-clicking on the node.";

        graphPanelTextArea = new JTextArea(notetext, 10, 50);
        graphPanelTextArea.setFont(new Font("Calibri", Font.PLAIN, 12));
        graphPanelTextArea.setBackground(Color.white);
        graphPanelTextArea.setLineWrap(true);
        graphPanelTextArea.setWrapStyleWord(true);
        graphPanelTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(graphPanelTextArea);
        //scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        graphPanel.add(scrollPane, gbc);


        //TODO entfernen und dafür Radioboxen verwenden
        differentReliabilitiesOkBtn = new JButton("Ok (components with different reliabilities)");
        differentReliabilitiesOkBtn.setEnabled(false);
        differentReliabilitiesOkBtn.addActionListener(this);
        //gbc = makegbc(1, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL);
        //graphPanel.add(differentReliabilitiesOkBtn, gbc);

        sameReliabilityOkBtn = new JButton("Ok (components with same reliability)");
        sameReliabilityOkBtn.setEnabled(false);
        sameReliabilityOkBtn.addActionListener(this);
        //gbc = makegbc(2, 2, 1, 1, 0, 0, GridBagConstraints.HORIZONTAL);
        //graphPanel.add(sameReliabilityOkBtn, gbc);
    }


    /**
     * Baut das Panel zur Eingabe von Wahrscheinlichkeiten und Parametern auf
     */
    private void initProbabilitiesPanel() {

        //Panel unter dem Netzwerkgraphen
        JPanel probabilityGroupPanel = new JPanel();
        probabilityGroupPanel.setBorder(BorderFactory.createTitledBorder("Now you can input the reliability of every edge:"));
        probabilityGroupPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = makegbc(0, 7, 1, 8, 1, 0.4);
        add(probabilityGroupPanel, gbc);

        //Radiobuttons zum auswählen des Modus
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton singleReliabilityRadioBtn = new JRadioButton("components have different reliabilities");
        buttonGroup.add(singleReliabilityRadioBtn);
        gbc = makegbc(0, 0, 2, 1, 1, 0, GridBagConstraints.BOTH, 5);
        probabilityGroupPanel.add(singleReliabilityRadioBtn, gbc);

        JRadioButton sameReliabilityRadioBtn = new JRadioButton("components have same reliabilities", true);
        buttonGroup.add(sameReliabilityRadioBtn);
        gbc = makegbc(0, 1, 2, 1, 1, 0, GridBagConstraints.BOTH, 5);
        probabilityGroupPanel.add(sameReliabilityRadioBtn, gbc);

        //Panel mit Wahrscheinlichkeitstextfeldern
        probabilityFieldsPanel = new ProbPanel(this);
        probabilityFieldsScrollPane = new JScrollPane(probabilityFieldsPanel);
        gbc = makegbc(0, 2, 2, 1, 1, 1);
        probabilityGroupPanel.add(probabilityFieldsScrollPane, gbc);

        //Reset-Button für alle Wahrscheinlichkeitstextfelder
        resetProbabilitiesBtn = new JButton("Reset");
        resetProbabilitiesBtn.setEnabled(false);
        resetProbabilitiesBtn.addActionListener(this);
        gbc = makegbc(0, 3, 1, 1, 1, 0);
        probabilityGroupPanel.add(resetProbabilitiesBtn, gbc);

        probabilitiesOkBtn = new JButton("Ok");
        probabilitiesOkBtn.setEnabled(false);
        probabilitiesOkBtn.addActionListener(this);
        gbc = makegbc(1, 3, 1, 1, 1, 0);
        probabilityGroupPanel.add(probabilitiesOkBtn, gbc);
    }

    /**
     * Baut das Panel mit den Berechnungsoptionen auf
     */
    private void initCalculatePanel() {
        JPanel calculatePanel = new JPanel();
        calculatePanel.setBorder(BorderFactory.createTitledBorder("Start calculation"));
        calculatePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = makegbc(0, 16, 1, 1, 1, 0);
        add(calculatePanel, gbc);

        resilienceBtn = new JButton("Calculate the resilience of the network");
        resilienceBtn.setEnabled(false);
        resilienceBtn.addActionListener(this);
        gbc = makegbc(0, 0, 1, 1, 0, 0);
        calculatePanel.add(resilienceBtn, gbc);

        calcReliabilityBtn = new JButton("Calculate the reliability of the network");
        calcReliabilityBtn.setEnabled(false);
        calcReliabilityBtn.addActionListener(this);
        gbc = makegbc(0, 1, 1, 1, 0, 0);
        calculatePanel.add(calcReliabilityBtn, gbc);

        reliabilityCompareCheckBox = new JCheckBox("Compare 2 algorithms");
        gbc = makegbc(1, 1, 1, 1, 1, 0);
        calculatePanel.add(reliabilityCompareCheckBox, gbc);
    }

    /**
     * Baut das Textausgabepanel auf
     */
    private void initOutputTextPanel() {
        JPanel outputTextPanel = new JPanel();
        outputTextPanel.setBorder(BorderFactory.createTitledBorder("Output"));
        outputTextPanel.setLayout(new BorderLayout());

        GridBagConstraints gbc = makegbc(0, 17, 1, 4, 1, 0.2);
        add(outputTextPanel, gbc);

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        JScrollPane sp = new JScrollPane(resultTextArea);

        //Borderlayout mit CENTER nutzt den gesamten übrigen Platz
        outputTextPanel.add(sp, BorderLayout.CENTER);
    }

    /**
     * Erstellt ein GridBagConstraints-Objekt mit den angegebenen Parametern
     *
     * @param x       X-Position im Grid (gridx)
     * @param y       Y-Position im Grid (gridy)
     * @param width   beanspruchte Spaltenanzahl im Grid (gridwidth)
     * @param height  beanspruchte Zeilenanzahl im Grid (gridheight)
     * @param weightx Gewichtung auf X-Achse (für Verteilung des freien Platzes)
     * @param weighty Gewichtung auf Y-Achse (für Verteilung des freien Platzes
     * @return GridBagConstraints
     */
    private GridBagConstraints makegbc(int x, int y, int width, int height, double weightx, double weighty) {
        return makegbc(x, y, width, height, weightx, weighty, GridBagConstraints.BOTH);
    }

    /**
     * Erstellt ein GridBagConstraints-Objekt mit den angegebenen Parametern
     *
     * @param x       X-Position im Grid (gridx)
     * @param y       Y-Position im Grid (gridy)
     * @param width   beanspruchte Spaltenanzahl im Grid (gridwidth)
     * @param height  beanspruchte Zeilenanzahl im Grid (gridheight)
     * @param weightx Gewichtung auf X-Achse (für Verteilung des freien Platzes)
     * @param weighty Gewichtung auf Y-Achse (für Verteilung des freien Platzes
     * @param fill    GridBagConstraints-Konstante für Füllung des freien Raumes in der Zelle
     * @return GridBagConstraints
     */
    private GridBagConstraints makegbc(int x, int y, int width, int height, double weightx, double weighty, int fill) {
        return makegbc(x, y, width, height, weightx, weighty, fill, 1);
    }

    /**
     * Erstellt ein GridBagConstraints-Objekt mit den angegebenen Parametern
     *
     * @param x         X-Position im Grid (gridx)
     * @param y         Y-Position im Grid (gridy)
     * @param width     beanspruchte Spaltenanzahl im Grid (gridwidth)
     * @param height    beanspruchte Zeilenanzahl im Grid (gridheight)
     * @param weightx   Gewichtung auf X-Achse (für Verteilung des freien Platzes)
     * @param weighty   Gewichtung auf Y-Achse (für Verteilung des freien Platzes
     * @param fill      GridBagConstraints-Konstante für Füllung des freien Raumes in der Zelle
     * @param leftInset Abstand links
     * @return GridBagConstraints
     */
    private GridBagConstraints makegbc(int x, int y, int width, int height, double weightx, double weighty, int fill, int leftInset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;

        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.fill = fill;
        gbc.insets = new Insets(1, leftInset, 1, 1);
        return gbc;
    }


    public void actionPerformed(ActionEvent evt) {
        JButton button = (JButton) evt.getSource();

        if (button == drawBtn) {
            graphPanelTextArea.setVisible(false);
            netPanel.setVisible(true);

            netPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            netPanel.addMouseListener(drawMouseListener);
            netPanel.addMouseMotionListener(drawMouseMoveListener);
            differentReliabilitiesOkBtn.setEnabled(true);
            sameReliabilityOkBtn.setEnabled(true);
            drawBtn.setEnabled(false);
            resetGraphBtn.setEnabled(true);
            if (netPanel.getComponentCount() != 0)
                netPanel.remove(graphPanelTextArea);
        }

        if (button == sameReliabilityOkBtn) {
            probabilityFieldsPanel.removeAll();
            sameReliabilityMode = true;
            resetGraphBtn.setEnabled(true);
            sameReliabilityOkBtn.setEnabled(true);
            checkGraphAndBuildProbPanel();
            differentReliabilitiesOkBtn.setEnabled(true);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);
        }

        if (button == inputNetBtn) {
            netPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            differentReliabilitiesOkBtn.setEnabled(true);
            resetGraphBtn.setEnabled(true);
            netPanel.removeMouseListener(drawMouseListener);
            netPanel.removeMouseMotionListener(drawMouseMoveListener);
            netPanel.addMouseListener(drawMouseListener);
            netPanel.addMouseMotionListener(drawMouseMoveListener);
            drawnNodes.clear();
            drawnEdges.clear();
            graph = null;
            zer = null;
            netPanel.valid = false;
            probability_mode = false;
            probabilityFieldsPanel.removeAll();
            probabilityFieldsPanel.repaint();
            drawBtn.setEnabled(false);
            probabilitiesOkBtn.setEnabled(false);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);
            //resultTextArea.setText(" ");
            //reduceText = "";
            sameReliabilityMode = false;
            sameReliabilityOkBtn.setEnabled(true);

            //probabilityFieldsPanel.removeAll();
            //resetGraphBtn.setEnabled(true);

            inputNet();

            //differentReliabilitiesOkBtn.setEnabled(true);
            //sameReliabilityOkBtn.setEnabled(true);
            //calcReliabilityBtn.setEnabled(false);
            //resilienceBtn.setEnabled(false);
            netPanel.remove(graphPanelTextArea);
            netPanel.repaint();

        }

        if (button == exportNetBtn) {
            exportNet();
        }

        if (button == differentReliabilitiesOkBtn) {
            probabilityFieldsPanel.removeAll();
            sameReliabilityMode = false;
            resetGraphBtn.setEnabled(true);
            sameReliabilityOkBtn.setEnabled(true);
            checkGraphAndBuildProbPanel();
            differentReliabilitiesOkBtn.setEnabled(true);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);

        }

        if (button == resetGraphBtn) {
            edgeStartValue = BigDecimal.ZERO;
            edgeEndValue = BigDecimal.ZERO;
            edgeStepSize = BigDecimal.ZERO;
            nodeStartValue = BigDecimal.ZERO;
            nodeEndValue = BigDecimal.ZERO;
            nodeStepSize = BigDecimal.ZERO;

            netPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            differentReliabilitiesOkBtn.setEnabled(true);
            resetGraphBtn.setEnabled(true);
            netPanel.removeMouseListener(drawMouseListener);
            netPanel.removeMouseMotionListener(drawMouseMoveListener);
            netPanel.addMouseListener(drawMouseListener);
            netPanel.addMouseMotionListener(drawMouseMoveListener);
            drawnNodes.clear();
            drawnEdges.clear();
            graph = null;
            zer = null;
            netPanel.valid = false;
            probability_mode = false;
            netPanel.repaint();
            probabilityFieldsPanel.removeAll();
            probabilityFieldsPanel.repaint();
            drawBtn.setEnabled(false);
            probabilitiesOkBtn.setEnabled(false);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);
            resultTextArea.setText(" ");
            sameReliabilityMode = false;
            sameReliabilityOkBtn.setEnabled(true);
        }

        if (button == probabilitiesOkBtn) {
            int edgeCount = edgeProbabilityTextFields.length;
            int nodeCount = nodeProbabilityTextFields.length;
            edgeProbabilities = new float[edgeCount];
            nodeProbabilities = new float[nodeCount];
            boolean seriesValuesMissing = false;

            MyList edgesWithMissingProbability = new MyList();
            MyList nodesWithMissingProbability = new MyList();

            //Prüft alle Felder durch, ob da Wahrscheinlichkeiten drin stehen
            for (int i = 0; i < edgeCount; i++) {
                String s = edgeProbabilityTextFields[i].getText();
                if (textIsNotProbability(s))
                    edgesWithMissingProbability.add(String.valueOf(i));
            }

            for (int i = 0; i < nodeCount; i++) {
                String s = nodeProbabilityTextFields[i].getText();
                if (textIsNotProbability(s))
                    nodesWithMissingProbability.add(String.valueOf(i));
            }

            if (sameReliabilityMode) {
                //Felder für die Berechnungsserien prüfen
                ArrayList<JTextField> checkingList = new ArrayList<>();
                checkingList.add(edgeEndProbabilityBox);
                checkingList.add(nodeEndProbabilityBox);
                checkingList.add(edgeProbabilityStepSizeBox);
                checkingList.add(nodeProbabilityStepSizeBox);

                for (JTextField field : checkingList) {
                    String value = field.getText();

                    if (value.length() != 0 && textIsNotProbability(value)) {
                        seriesValuesMissing = true;
                        break;
                    }
                }
            }

            if (edgesWithMissingProbability.size() != 0 || nodesWithMissingProbability.size() != 0 || seriesValuesMissing) {
                //Dieser Block zeigt nur ein Hinweisfenster an, falls Wahrscheinlichkeiten fehlen
                //Strings für fehlende Kanten und Knoten generieren
                MyIterator it = edgesWithMissingProbability.iterator();
                String missingProbabilityEdges = (String) it.next();
                while (it.hasNext()) {
                    String s2 = (String) it.next();
                    missingProbabilityEdges = missingProbabilityEdges + ", " + s2;
                }

                it = nodesWithMissingProbability.iterator();
                String missingProbabilityNodes = (String) it.next();
                while (it.hasNext()) {
                    String s2 = (String) it.next();
                    missingProbabilityNodes = missingProbabilityNodes + ", " + s2;
                }

                String str = "The reliability of an edge is a probability, thus\nit must be a number in format x.xxxxxx " +
                        "which is\nless than or equal to 1. \n";

                //Passende Texte hinzufügen
                if (edgesWithMissingProbability.size() != 0 || nodesWithMissingProbability.size() != 0) {
                    str += "Please check the in-\nput for edge\n" + missingProbabilityEdges +
                            "\n and for node\n" + missingProbabilityNodes + "\n";
                }

                if (seriesValuesMissing) {
                    str += "Please check the input for the calculation series.";
                }

                //Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);

                return;
            }
            //bis hierhin in diesem Block: testen, ob felder ausgefüllt wurden

            for (int i = 0; i < edgeCount; i++) {
                edgeProbabilityTextFields[i].setEditable(false);
                String s = edgeProbabilityTextFields[i].getText();
                Float fo = Float.valueOf(s);
                edgeProbabilities[i] = fo;
            }

            for (int i = 0; i < nodeCount; i++) {
                nodeProbabilityTextFields[i].setEditable(false);
                String s = nodeProbabilityTextFields[i].getText();
                Float fo = Float.valueOf(s);
                nodeProbabilities[i] = fo;
            }

            resetProbabilitiesBtn.setEnabled(true);
            probabilitiesOkBtn.setEnabled(false);
            netPanel.removeMouseListener(drawMouseListener);
            netPanel.removeMouseMotionListener(drawMouseMoveListener);

            if (graph != null)
                calcReliabilityBtn.setEnabled(true);
            resilienceBtn.setEnabled(true);

        /*Wahrscheinlichkeiten neu zuordnen.*/
            reassignProbabilities();


            //End value und step size zuordnen
            if (sameReliabilityMode) {
                if (edgeProbabilityTextFields[0].getText().length() != 0) {
                    edgeStartValue = new BigDecimal(edgeProbabilityTextFields[0].getText());
                    System.out.println("StartValue Edges: " + edgeStartValue);
                }

                if (edgeEndProbabilityBox.getText().length() != 0) {
                    edgeEndValue = new BigDecimal(edgeEndProbabilityBox.getText());
                    System.out.println("EndValue Edges: " + edgeEndValue);
                }

                if (edgeProbabilityStepSizeBox.getText().length() != 0) {
                    edgeStepSize = new BigDecimal(edgeProbabilityStepSizeBox.getText());
                    System.out.println("StepSize Edges: " + edgeStepSize);
                }

                if (nodeProbabilityTextFields[0].getText().length() != 0) {
                    nodeStartValue = new BigDecimal(nodeProbabilityTextFields[0].getText());
                    System.out.println("StartValue Nodes: " + nodeStartValue);
                }

                if (nodeEndProbabilityBox.getText().length() != 0) {
                    nodeEndValue = new BigDecimal(nodeEndProbabilityBox.getText());
                    System.out.println("EndValue Nodes: " + nodeEndValue);
                }

                if (nodeProbabilityStepSizeBox.getText().length() != 0) {
                    nodeStepSize = new BigDecimal(nodeProbabilityStepSizeBox.getText());
                    System.out.println("StepSize Nodes: " + nodeStepSize);
                }
            }


        }


        if (button == resetProbabilitiesBtn) {
            edgeStartValue = BigDecimal.ZERO;
            edgeEndValue = BigDecimal.ZERO;
            edgeStepSize = BigDecimal.ZERO;
            nodeStartValue = BigDecimal.ZERO;
            nodeEndValue = BigDecimal.ZERO;
            nodeStepSize = BigDecimal.ZERO;

            probabilitiesOkBtn.setEnabled(true);
            differentReliabilitiesOkBtn.setEnabled(true);
            resetProbabilitiesBtn.setEnabled(false);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);
            edgeProbabilities = null;
            for (JTextField edgeProbabilityTextField : edgeProbabilityTextFields) {
                edgeProbabilityTextField.setText(null);
                edgeProbabilityTextField.setEditable(true);
            }

            for (JTextField nodeProbabilityTextField : nodeProbabilityTextFields) {
                nodeProbabilityTextField.setText(null);
                nodeProbabilityTextField.setEditable(true);
            }

            if (edgeEndProbabilityBox != null) {
                edgeEndProbabilityBox.setText(null);
            }
            if (edgeProbabilityStepSizeBox != null) {
                edgeProbabilityStepSizeBox.setText(null);
            }

            if (nodeEndProbabilityBox != null) {
                nodeEndProbabilityBox.setText(null);
            }
            if (nodeProbabilityStepSizeBox != null) {
                nodeProbabilityStepSizeBox.setText(null);
            }

            sameReliabilityOkBtn.setEnabled(true);

        }

        if (button == resilienceBtn) {
            if (!edgeEndValue.equals(BigDecimal.ZERO) && !edgeStepSize.equals(BigDecimal.ZERO) &&
                    !nodeEndValue.equals(BigDecimal.ZERO) && !nodeStepSize.equals(BigDecimal.ZERO) && sameReliabilityMode) {

                calculationSeriesMode = 1;
                calculationSeries();
            } else {
                calculationSeriesMode = 0;
                calculate_resilience();

                resultText = "The network has " + total_nodes + " Nodes, containing " + c_nodes + " c-Nodes.\n" +
                        "There are " + combinations + " combinations.\n" + "The resilience of the network is: " + result_resilience;
                resultTextArea.setText(resultText);
            }
        }


        if (button == calcReliabilityBtn) {
            //heidtmanns_reliability();
            //fact_reliability();

            if (!edgeEndValue.equals(BigDecimal.ZERO) && !edgeStepSize.equals(BigDecimal.ZERO) &&
                    !nodeEndValue.equals(BigDecimal.ZERO) && !nodeStepSize.equals(BigDecimal.ZERO) && sameReliabilityMode) {
                calculationSeriesMode = 2;
                calculationSeries();
            } else {
                calculationSeriesMode = 0;
                //Wenn die Checkbox angeklickt wurde, sollen die 3 Alg. verglichen werden. Sonst nicht.
                if (reliabilityCompareCheckBox.isSelected()) {
                    calculate_reliability_2_Algorithms();
                } else {
                    onlyReliabilityFast = true;
                    calculate_reliability_faster();
                    onlyReliabilityFast = false;
                }
            }

        }
    }

    /**
     * Weist alle Wahrscheinlichkeiten aus den Eingabefeldern den Elementen im Graphen neu zu Berücksichtigt auf Wunsch
     * nur den "normalen" Graphen
     */
    private void reassignProbabilities() {
        /*
        Diese Listen sind Klone der Kantenlisten des Graphen, aber halten anscheinend die selben Referenzen auf Kanten
        wie die Kantenliste des Graphen, also eine flache Kopie.
        */
        MyList edgeList = graph.getEdgelist();
        //Kantenwahrscheinlichkeiten
        for (int i = 0; i < edgeProbabilities.length; i++) {
            Edge e = (Edge) edgeList.get(i);
            e.prob = edgeProbabilities[i];
        }

        MyList nodeList = graph.getNodelist();

        //Knotenwahrscheinlichkeiten
        for (int i = 0; i < nodeProbabilities.length; i++) {
            Node e = (Node) nodeList.get(i);
            e.prob = nodeProbabilities[i];
        }
    }

    private float getPathProbability(MySet path) {
        float p = 1;
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
        System.out.println(Float.toString(p) + " für " + output);
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
     * Prüft, ob der gegebene String eine (Gleitkomma)Zahl zwischen 0 und 1 ist
     *
     * @param str der zu überprüfende String
     * @return Boolean, ob der Text eine Wahrscheinlichkeit ist
     */
    private boolean textIsNotProbability(String str) {
        boolean b = true;
        boolean temp = false;
        if (str.length() == 0)
            b = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (i == 0 && c != '0') {
                if (c != '1') {
                    b = false;
                    break;
                } else {
                    temp = true;
                    continue;
                }
            }
            if (i == 1) {
                if (c != '.') {
                    b = false;
                    break;
                }
                continue;
            }
            if (!Character.isDigit(c)) {
                b = false;
                break;
            } else {
                if (temp && c != '0') {
                    b = false;
                    break;
                }
            }
        }
        return !b;
    }

    /**
     * Prüft den Graphen und baut die Anzeige mit den Textfeldern für die Wahrscheinlichkeiten auf
     */
    private void checkGraphAndBuildProbPanel() {
        if (drawnEdges.size() == 0) {
            //Dieser Block zeigt ein Hinweisfenster an, wenn keine Knoten vorhanden sind und bricht die Methode ab
            String str = "Your Network does not contain edges!";

            Toolkit.getDefaultToolkit().beep();

            JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Anzahl Konnektionsknoten bestimmen
        int count = 0;
        MyIterator np = drawnNodes.iterator();
        while (np.hasNext()) {
            NodePoint n = (NodePoint) np.next();
            if (n.k)
                count = count + 1;
        }

        if (count < 2) {
            //Der Code in diesem Block zeigt nur ein Hinweisfenster an und bricht die Funktion ab
            String str = "Your Network does not contain at least 2 c-drawnNodes! \nYou can draw a new c-node by " +
                    "pressing the right mouse button. \nIf you want to transform an existing node into a c-node, " +
                    "\nplease hold the Ctrl-Key on your keyboard while left-clicking on the node.";

            JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Genug Knoten bzw. Konnektionsknoten vorhanden, weiter gehts

		/*Ermittle kleinste und größte Positionswerte der Knoten.*/
        int smallest_x_pos = 2000;
        int highest_x_pos = 0;
        int smallest_y_pos = 2000;
        int highest_y_pos = 0;
        np = drawnNodes.iterator();
        while (np.hasNext()) {
            NodePoint n = (NodePoint) np.next();
            if (n.x < smallest_x_pos)
                smallest_x_pos = n.x;
            if (n.x > highest_x_pos)
                highest_x_pos = n.x;
            if (n.y < smallest_y_pos)
                smallest_y_pos = n.y;
            if (n.y > highest_y_pos)
                highest_y_pos = n.y;
        }

        graph_width = highest_x_pos - smallest_x_pos + 25;
        graph_height = highest_y_pos - smallest_y_pos + 25;

		/*Erzeuge Graphen.*/
        graph = makeGraph();

        netPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        differentReliabilitiesOkBtn.setEnabled(false);
        resetGraphBtn.setEnabled(true);
        probability_mode = true;

        int edgeCount = drawnEdges.size();
        int nodeCount = drawnNodes.size();
        //probabilityFieldsPanel.setSize(probabilityFieldsPanel.getPreferredSize());
        //System.out.println(probabilityFieldsPanel.getSize());
        /*Da drawnEdges.size() sich geaendert hat, muss hier die
          Size von probabilityFieldsPanel nochmal festgelegt werden, oder man fuegt erst hier
		  probabilityFieldsPanel zur ScrollPane probabilityFieldsScrollPane:
		  probabilityFieldsPanel = new ProbPanel();
		  probabilityFieldsScrollPane.add(probabilityFieldsPanel);
		  Dann hat probabilityFieldsPanel die richtige Groesse.
		*/

        if (sameReliabilityMode) {
            probabilityFieldsScrollPane.getVerticalScrollBar().setValue(0);
            probabilityFieldsPanel.setSize(600, 190);
            probabilityFieldsPanel.removeAll();

            probabilityFieldsPanel.setLayout(new GridLayout(7, 2));

            edgeProbabilityTextFields = new JTextField[edgeCount];
            nodeProbabilityTextFields = new JTextField[nodeCount];
            String str = "Reliability of every edge: ";

            //Kantenwahrscheinlichkeiten
            Label edgeProbLabel = new Label(str, Label.RIGHT);
            JTextField edgeProbtextField = new JTextField(20);
            edgeProbtextField.setBackground(Color.white);
            for (int i = 0; i < drawnEdges.size(); i++) {
                edgeProbabilityTextFields[i] = edgeProbtextField;
            }
            Panel edgeProbPanel = new Panel();
            edgeProbPanel.add(edgeProbLabel);
            edgeProbPanel.add(edgeProbtextField);
            probabilityFieldsPanel.add(edgeProbPanel);

            //Kantenwahrscheinlichkeiten
            Label nodeProbLabel = new Label("Reliability of every node:", Label.RIGHT);
            JTextField nodeProbtextField = new JTextField(20);
            nodeProbtextField.setBackground(Color.white);
            for (int i = 0; i < drawnNodes.size(); i++) {
                nodeProbabilityTextFields[i] = nodeProbtextField;
            }
            Panel nodeProbPanel = new Panel();
            nodeProbPanel.add(nodeProbLabel);
            nodeProbPanel.add(nodeProbtextField);
            probabilityFieldsPanel.add(nodeProbPanel);

            Label stepValuesHeader = new Label("Optional for calculation series: ", Label.RIGHT);
            Panel stepValuesPanel = new Panel();
            stepValuesPanel.add(stepValuesHeader);
            probabilityFieldsPanel.add(stepValuesPanel);

            JLabel edgeProbEndValueLbl = new JLabel("Edge End value: ", Label.RIGHT);
            edgeEndProbabilityBox = new JTextField(20);
            edgeEndProbabilityBox.setBackground(Color.white);
            Panel p2 = new Panel();
            p2.add(edgeProbEndValueLbl);
            p2.add(edgeEndProbabilityBox);

            JLabel edgeStepSizeLbl = new JLabel("Step size (e.g. 0.01): ", Label.RIGHT);
            edgeProbabilityStepSizeBox = new JTextField(20);
            edgeProbabilityStepSizeBox.setBackground(Color.white);

            p2.add(edgeStepSizeLbl);
            p2.add(edgeProbabilityStepSizeBox);

            JLabel nodeEndValueLbl = new JLabel("Node End value: ", Label.RIGHT);
            nodeEndProbabilityBox = new JTextField(20);
            nodeEndProbabilityBox.setBackground(Color.white);
            Panel nodePanel = new Panel();
            nodePanel.add(nodeEndValueLbl);
            nodePanel.add(nodeEndProbabilityBox);

            JLabel nodeStepSizeLbl = new JLabel("Step size (e.g. 0.01): ", Label.RIGHT);
            nodeProbabilityStepSizeBox = new JTextField(20);
            nodeProbabilityStepSizeBox.setBackground(Color.white);
            nodePanel.add(nodeStepSizeLbl);
            nodePanel.add(nodeProbabilityStepSizeBox);

            probabilityFieldsPanel.add(p2);
            probabilityFieldsPanel.add(nodePanel);
        } else {

            probabilityFieldsPanel.setSize(probabilityFieldsPanel.getPreferredSize());
            probabilityFieldsPanel.removeAll();
            //probabilityFieldsPanel.setLayout(new GridLayout((edgeCount + nodeCount) / 2 + 1, 2));
            probabilityFieldsPanel.setLayout(new GridLayout(0, 2));

            edgeProbabilityTextFields = new JTextField[edgeCount];
            nodeProbabilityTextFields = new JTextField[nodeCount];

            for (int i = 0; i < drawnEdges.size(); i++) {
                addFieldToProbPanel(i, false);
            }

            for (int i = 0; i < drawnNodes.size(); i++) {
                addFieldToProbPanel(i, true);
            }

            probabilityFieldsScrollPane.validate();
        }
        probabilityFieldsPanel.validate();


        probabilitiesOkBtn.setEnabled(true);
        sameReliabilityOkBtn.setEnabled(true);
        differentReliabilitiesOkBtn.setEnabled(true);
    }

    /**
     * Fügt dem Wahrscheinlichkeitspanel ein Panel für die Wahrscheinlichkeit einer Komponente hinzu
     *
     * @param number     Nummer des Felds
     * @param isNodeProb True, wenn das Feld für einen Knoten ist, false bei Kante
     */
    private void addFieldToProbPanel(int number, boolean isNodeProb) {
        String text;
        String type = isNodeProb ? "Node " : "Edge";
        if (number < 10)
            text = type + number + " ";
        else
            text = type + number;

        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        JTextField textField = new JTextField(20);
        textField.setBackground(Color.white);

        if (isNodeProb) {
            nodeProbabilityTextFields[number] = textField;
        } else {
            edgeProbabilityTextFields[number] = textField;
        }
        Panel panel = new Panel();
        panel.add(label);
        panel.add(textField);
        probabilityFieldsPanel.add(panel);
    }

    /**
     * Erstellt aus den Knoten und Kanten des gezeichneten Graphen ein Graph-Objekt
     *
     * @return das Graph-Objekt zum gezeichneten Graph
     */
    private Graph makeGraph() {
        MyList nodeList = new MyList();
        MyList edgeList = new MyList();

        MyIterator it = drawnNodes.iterator();
        int cnt = 0;
        while (it.hasNext()) {
            NodePoint np = (NodePoint) it.next();
            Node node = new Node(cnt);
            node.xposition = np.x;
            node.yposition = np.y;
            if (np.k)
                node.c_node = true;
            nodeList.add(node);
            cnt++;
        }
        //fertig mit dem Eintragen von Knoten
        it = drawnEdges.iterator();
        cnt = 0;
        while (it.hasNext()) {
            EdgeLine e = (EdgeLine) it.next();
            int m = e.node1;
            int n = e.node2;
            Edge edge = new Edge(cnt);
            Node node1 = (Node) nodeList.get(m);
            Node node2 = (Node) nodeList.get(n);
            edge.left_node = node1;
            edge.right_node = node2;
            edgeList.add(edge);
            node1.add_Edge(edge);
            node2.add_Edge(edge);
            cnt++;
        }
        return new Graph(nodeList, edgeList);
    }

    @Override
    public void graphChanged() {

    }

    private class MyMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            if (!probability_mode) {
                int x1 = evt.getX();
                int y1 = evt.getY();
                int cnt1;
                MyIterator it = drawnNodes.iterator();
                while (it.hasNext()) {
                    NodePoint nps = (NodePoint) it.next();
                    cnt1 = drawnNodes.indexOf(nps);
                    int px = nps.x;
                    int py = nps.y;
                    px = px + 10;
                    py = py + 10;
                    int dx = x1 - px;
                    int dy = y1 - py;
                    if ((dx * dx + dy * dy) <= 100) {

                        if (evt.isShiftDown()) //zum Löschen von Knoten
                        {
                            drawnNodes.remove(nps);
                            for (int i = 0; i < drawnEdges.size(); i++) {
                                EdgeLine edl = (EdgeLine) drawnEdges.get(i);

                                if (edl.node1 == cnt1 || edl.node2 == cnt1) {
                                    drawnEdges.remove(edl);
                                    i = i - 1;
                                } else {
                                    if (edl.node1 > cnt1)
                                        edl.node1 = edl.node1 - 1;
                                    if (edl.node2 > cnt1)
                                        edl.node2 = edl.node2 - 1;
                                }
                            }
                        }

                        //Vorhandene Knoten zu K-Knoten machen
                        if (evt.isControlDown()) {
                            nps.k = !nps.k;

                            drawnNodes.set(cnt1, nps);
                        }
                        netPanel.repaint();
                        return;
                    }
                    //punkt (x,y) ist in dem Kreis(px, py)
                }
                NodePoint np = new NodePoint();
                if ((x1 % 20) < 10) //Am Raster ausrichten. Kreise haben Durchmesser von 20.
                    np.x = x1 - (x1 % 20) - 10;
                else
                    np.x = x1 + 20 - (x1 % 20) - 10;
                if ((y1 % 20) < 10)
                    np.y = y1 - (y1 % 20) - 10;
                else
                    np.y = y1 + 20 - (y1 % 20) - 10;
                if (evt.isMetaDown())
                    np.k = true;
                drawnNodes.add(np);
                netPanel.repaint();
            } else {
                int r = 5;
                double dr;
                int cntedge = 0;
                int x3 = evt.getX();
                int y3 = evt.getY();
                MyIterator it = drawnEdges.iterator();
                while (it.hasNext()) {
                    EdgeLine edg = (EdgeLine) it.next();
                    int x1 = edg.x1;
                    int y1 = edg.y1;
                    int x2 = edg.x2;
                    int y2 = edg.y2;
                    int diff_x2x1 = x2 - x1;
                    int diff_y2y1 = y2 - y1;
                    int min_x1x2 = x1;
                    int max_x1x2 = x2;
                    int min_y1y2 = y1;
                    int max_y1y2 = y2;
                    if (x2 < min_x1x2) {
                        min_x1x2 = x2;
                        max_x1x2 = x1;
                    }
                    if (y2 < min_y1y2) {
                        min_y1y2 = y2;
                        max_y1y2 = y1;
                    }

                    if (x1 == x2 && min_y1y2 <= y3 && y3 <= max_y1y2) {
                        dr = Math.abs(x3 - x1);
                        if (dr <= r) {
                            showInputEdgeProbDialog(cntedge);
                            break;
                        }
                    }

                    if (y1 == y2 && min_x1x2 <= x3 && x3 <= max_x1x2) {
                        dr = Math.abs(y3 - y1);
                        if (dr <= r) {
                            showInputEdgeProbDialog(cntedge);
                            break;
                        }
                    }

                    if (x1 != x2 && y1 != y2 && min_x1x2 <= x3 && x3 <= max_x1x2 && min_y1y2 <= y3 && y3 <= max_y1y2) {
                        dr = Math.sqrt(Math.pow(x3 - x1 - ((x3 - x1 + (-x3 * diff_y2y1 + y3 * diff_x2x1 + x1 *
                                diff_y2y1 - y1 * diff_x2x1) / (diff_y2y1 + Math.pow(diff_x2x1, 2) / diff_y2y1)) / diff_x2x1)
                                * diff_x2x1, 2) + Math.pow(y3 - y1 - ((x3 - x1 + (-x3 * diff_y2y1 + y3 * diff_x2x1 + x1
                                * diff_y2y1 - y1 * diff_x2x1) / (diff_y2y1 + Math.pow(diff_x2x1, 2) / diff_y2y1)) / diff_x2x1)
                                * diff_y2y1, 2));
                        if (dr <= r) {
                            showInputEdgeProbDialog(cntedge);
                            break;
                        }
                    }
                    cntedge++;
                }
                netPanel.repaint();
            }
        }

        private void showInputEdgeProbDialog(int edgeNumber) {
            String str = "Input reliability of Edge " + edgeNumber;
            String res = JOptionPane.showInputDialog(mainFrame, str);
            if (res != null && res.length() > 0) {
                edgeProbabilityTextFields[edgeNumber].setText(res);
            }
        }

        public void mousePressed(MouseEvent evt) {
            if (evt.isShiftDown() || probability_mode)
                return;
            netPanel.valid = false;
            int x = evt.getX();
            int y = evt.getY();
            int cnt;
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint np = (NodePoint) it.next();
                int px = np.x;
                int py = np.y;
                px = px + 10;
                py = py + 10;
                int dx = x - px;
                int dy = y - py;
                if ((dx * dx + dy * dy) <= 100) {
                    cnt = drawnNodes.indexOf(np);
                    netPanel.el = new EdgeLine();
                    netPanel.el.x1 = px;
                    netPanel.el.y1 = py;
                    netPanel.el.node1 = cnt;
                    netPanel.valid = true;
                    break;
                }
                //punkt (x,y) ist in dem Kreis(px, py)
            }
        }

        public void mouseReleased(MouseEvent evt) {
            if (!netPanel.valid || evt.isShiftDown() || probability_mode)
                return;

            netPanel.valid = false;
            int x = evt.getX();
            int y = evt.getY();
            int cnt;
            MyIterator it = drawnNodes.iterator();
            while (it.hasNext()) {
                NodePoint np = (NodePoint) it.next();
                int px = np.x;
                int py = np.y;
                px = px + 10;
                py = py + 10;
                int dx = x - px;
                int dy = y - py;
                if ((dx * dx + dy * dy) <= 100 && netPanel.el.node1 != drawnNodes.indexOf(np)) {
                    cnt = drawnNodes.indexOf(np);
                    netPanel.el.x2 = px;
                    netPanel.el.y2 = py;
                    netPanel.el.node2 = cnt;
                    drawnEdges.add(netPanel.el);
                    netPanel.valid = true;

                    int lx = netPanel.el.x2 - netPanel.el.x1;
                    int ly = netPanel.el.y2 - netPanel.el.y1;
                    netPanel.el.x0 = netPanel.el.x2 - lx / 2;
                    netPanel.el.y0 = netPanel.el.y2 - ly / 2;

                    break;
                }
                //punkt (x,y) ist in dem Kreis(px, py)
            }
            netPanel.repaint();
        }
    }

    private class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent evt) {
            if (netPanel.valid && !probability_mode) {
                int x = evt.getX();
                int y = evt.getY();
                netPanel.el.x2 = x;
                netPanel.el.y2 = y;
                netPanel.repaint();
            }
        }
    }

    /**
     * Zuverlaessigkeitsberechnung mit nur einem Algorithmus Bei einem zusammenhängenden Graphen wird Heidtmanns
     * Algorithmus verwendet, sonst die Faktorisierungsmethode
     */
    private void calculate_reliability_faster() {
        resultText = "Calculating...";
        resultTextArea.setText(resultText);

        //Prüfen ob das Netz zusammenhängt
        boolean graphConnected;
        //com.resinet.model.Graph ist zusammenhängend
        graphConnected = (Con_check.check(graph) == -1);

        if (graphConnected) {
            heidtmanns_reliability();
            resultText = "The reliability of the network is: " + prob;
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Your Graph is not connected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        resultTextArea.setText(resultText);
    }

    /**
     * Zuverlaessigkeitsberechnung mit 2 Algorithmen (ReNeT)
     */
    private void calculate_reliability_2_Algorithms() {
        resultText = "Please use the scrollbar to scroll through the results. \n \n";

        calcReliabilityBtn.setEnabled(true);
        resilienceBtn.setEnabled(true);

        zer = new Zerleg(graph);
        zer.start();
        try {
            zer.join();
        } catch (InterruptedException ignored) {
        }

        reassignProbabilities();

        if (graph.edgeList.size() == 1) {
            Edge e = (Edge) graph.getEdgelist().get(0);
            if ((e.left_node.c_node) && (e.right_node.c_node)) {
                prob = e.prob;
                prob *= e.left_node.prob;
                prob *= e.right_node.prob;
            } else
                prob = 0;
            resultText = "The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob;

        } else {
            prob = 0;
            String str3 = "P=";
            int count = 0;
            MyIterator it = zer.hz.iterator();
            while (it.hasNext()) {
                MyList al = (MyList) it.next();
                MySet hs = (MySet) al.get(0);
                float p;
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
            prob = 0;
            String s = "P=";
            it = zer.az.iterator();
            while (it.hasNext()) {
                ResultA ra = (ResultA) it.next();
                MySet si = ra.i;
                MySet sd = ra.d;
                float pi = 1;
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
                float pd = 1;
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
        resultTextArea.setText(resultText);
        //calcReliabilityBtn.setEnabled(false);
    }


//////////////////Zuverlaessigkeitsberechnung nur mit Heidtmann's Algorithm //////////////////  

    private float heidtmanns_reliability() {
        long start = new Date().getTime();


        if (calculationSeriesMode == 0 && !onlyReliabilityFast) {
            resultText = "Step " + counter + " of " + combinations;
            resultTextArea.setText(resultText);
        }


        zer = new Zerleg(graph);
        zer.start();
        try {
            zer.join();
        } catch (InterruptedException ignored) {
        }

        //Die Zerlegung verbraucht die meiste Zeit, beim Testnetz etwa 300ms, während der Rest nur max 1 ms braucht
        System.out.println("Laufzeit Heidtmann bis Zerlegung: " + ((new Date()).getTime() - start));
        reassignProbabilities();

        if (graph.edgeList.size() == 1) {
            Edge e = (Edge) graph.getEdgelist().get(0);
            if ((e.left_node.c_node) && (e.right_node.c_node)) {
                prob = e.prob;
                prob *= e.left_node.prob;
                prob *= e.right_node.prob;
            } else prob = 0;
            resultText = "The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob;

        } else {
            prob = 0;
            MyIterator it = zer.hz.iterator();
            while (it.hasNext()) {
                System.out.println("Neuer Pfad");
                MyList al = (MyList) it.next();
                MySet hs = (MySet) al.get(0);
                float p;
                //hs enthält hier anscheinend einen Pfad im Graphen zwischen den K-Knoten
                p = getPathProbability(hs);

                for (int i = 1; i < al.size(); i++) {
                    MySet hs1 = (MySet) al.get(i);
                    if (hs1.isEmpty())
                        continue;
                    p = p * (1 - getPathProbability(hs1));

                }
                System.out.println("Wahrscheinlichkeit: " + Float.toString(p));
                prob = prob + p;

            }
        /*
        String str1 = "The network is decomposed with Heidtmann's Algorithm:\n";
		String str2 = "The reliability of the network is:\n";

		if(lang=='D')
		    {
			str1 = "Das Netz wird mit dem Algorithmus von Heidtmann zerlegt:\n";
			str2 = "Die Zuverlaessigkeit des Netzes ist:\n";
		    }
		    */

            //resultText="Schritt " + counter + " von " + combinations;
            //resultText = resultText+str1+"\n"+str2+prob;

            calcReliabilityBtn.setEnabled(true);
            resilienceBtn.setEnabled(true);

            //resultTextArea.setText(resultText);
        }
        long runningTime = new Date().getTime() - start;
        System.out.println("Laufzeit Heidtmann: " + runningTime);
        System.out.println("Prob. Heidtmann: " + prob);
        return prob;
    }


///////////////////////////// Binomialkoeffizient ////////////////////////////////// 


    private BigInteger binomial(long n, long k) {
//    	long start = new Date().getTime();
        BigInteger binomialCoefficient = BigInteger.ONE;

        // Nutze die Symmetrie des Pascalschen Dreiecks um den Aufwand zu minimieren.
        if (k > n / 2) {
            k = n - k;
        }

        if (k > n) {
            binomialCoefficient = BigInteger.ZERO;
        } else if (k == 0 | n == k) {
            binomialCoefficient = BigInteger.ONE;
        } else if (k == 1 | k == n - 1) {
            binomialCoefficient = BigInteger.valueOf(n);
        } else {
            for (long i = 1; i <= k; i++) {
                binomialCoefficient = binomialCoefficient.multiply(BigInteger.valueOf(n - k + i));
                binomialCoefficient = binomialCoefficient.divide(BigInteger.valueOf(i));
            }
        }
        //System.out.println(binomialCoefficient);
//		long runningTime = new Date().getTime() - start; 
//        System.out.println("Laufzeit: " + runningTime);
        return binomialCoefficient;
    }


///////////////////////////// Resilienz //////////////////////////////////    


    private int total_nodes;
    private int c_nodes;
    private BigInteger combinations;
    private float result_resilience;
    private int counter;

    // Hauptmethode, die den Algorithmus zur Berechnung der Resilienz beinhaltet.
    private void calculate_resilience() {
        long start = new Date().getTime();

        resultText = "Calculating...";
        resultTextArea.setText(resultText);

        //Anzahl der Knoten     		
        total_nodes = drawnNodes.size();

        //Anzahl der K-Knoten
        c_nodes = 0;

        // Sicherung der K-Knotenliste
        String cNodeList = "";
        for (int i = 0; i < total_nodes; i++) {
            NodePoint nodeSave = (NodePoint) drawnNodes.get(i);
            if (nodeSave.k) {
                c_nodes++;
                cNodeList = cNodeList + "1";
            } else {
                cNodeList = cNodeList + "0";
            }
        }

        //Prüfen ob das Netz zusammenhängt
        if (Con_check.check(graph) != -1) {
            JOptionPane.showMessageDialog(mainFrame, "Your Graph is not connected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Graph graphSave = null;

        try {
            graphSave = (Graph) Util.serialClone(graph); //clone Graphen
        } catch (IOException | ClassNotFoundException e1) {
            System.err.println(e1.toString());
        }

        // Berechne Anzahl der Kombinationen
        combinations = binomial(total_nodes, c_nodes);

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

        counter = 0;
        result_resilience = 0;

        // Für jede Kombination der K-Knoten
        for (Object c : set1) {
            HashSet d = (HashSet) c;

            // Für jeden Knoten: Falls er in der aktuellen Kombination enthalten ist, setze ihn auf "K-Knoten".
            for (int i = 0; i < total_nodes; i++) {
                // Entsprechenden Knoten holen
                NodePoint node1 = (NodePoint) drawnNodes.get(i);
                //com.resinet.model.Node node1 = (com.resinet.model.Node)graph.nodeList.get(i);

                // Dann auf true, falls K-Knoten
                node1.k = d.contains(i);

                // Schreibe jeden Knoten neu in die Knotenliste.
                drawnNodes.set(i, node1);
                //graph.nodeList.set(i, node1);

            }

            // Erhöhe pro Kombination den Zähler um 1.
            counter++;

            graph = makeGraph();


            // Wahrscheinlichkeiten neu zuordnen.
            reassignProbabilities();

            // Berechne die Zuverlässigkeit für die aktuelle Kombination und addiere sie zur bisherigen Summe. 
            result_resilience = result_resilience + heidtmanns_reliability();

        }

        // Teile die Summe der Zuverlässigkeiten durch die Anzahl der Kombinationen.
        result_resilience = result_resilience / combinations.longValue();

        //K-Knotenliste zurücksetzen
        for (int i = 0; i < total_nodes; i++) {
            // Entsprechenden Knoten holen
            NodePoint nodeReset = (NodePoint) drawnNodes.get(i);

            // Dann auf true, falls K-Knoten
            nodeReset.k = cNodeList.charAt(i) == '1';

            // Schreibe jeden Knoten neu in die Knotenliste.
            drawnNodes.set(i, nodeReset);
        }

        try {

            graph = (Graph) Util.serialClone(graphSave); //clone Graphen
        } catch (IOException | ClassNotFoundException e1) {
            System.err.println(e1.toString());
        }

        long runningTime = new Date().getTime() - start;
        System.out.println("Laufzeit Resilienz: " + runningTime);
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

    //Methode zum Einlesen von Netzen aus Textdateien im Pajek-Format
    private void inputNet() {
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

                drawnNodes.add(node1);
            }

            //Zeile überspringen: *Arcs oder *Edges
            lineReader.readLine();

            //Lies Kanten aus
            while (lineReader.ready()) {
                actRow = lineReader.readLine();

                //Achtung, Bei Leerzeile wird abgebrochen!
                if (actRow == null | actRow.length() == 0) {
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

                NodePoint startNodePoint = (NodePoint) drawnNodes.get(edge1.node1);
                edge1.x1 = startNodePoint.x + 10;
                edge1.y1 = startNodePoint.y + 10;

                NodePoint endNodePoint = (NodePoint) drawnNodes.get(edge1.node2);
                edge1.x2 = endNodePoint.x + 10;
                edge1.y2 = endNodePoint.y + 10;

                int labelX = edge1.x2 - edge1.x1;
                int labelY = edge1.y2 - edge1.y1;
                edge1.x0 = edge1.x2 - labelX / 2;
                edge1.y0 = edge1.y2 - labelY / 2;

                drawnEdges.add(edge1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            inputError();
        }
    }

    private void inputError() {
        //Error-Popup ausgeben
        String str = "Your input was invalid! Please choose a valid file created by Pajek or ResiNeT.";

        JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
    }

    private void exportNet() {
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
                exportError();
                return;
            }
        } else {
            return;
        }

        //Ab hier in die Datei schreiben
        Writer writer;

        try {
            writer = new FileWriter(path);
            writer.write("*Vertices " + drawnNodes.size());


            int nodesDigitsCount = String.valueOf(drawnNodes.size()).length();

            //Für jeden Knoten eine Zeile schreiben
            for (int i = 1; i < drawnNodes.size() + 1; i++) {
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

                NodePoint node = (NodePoint) drawnNodes.get(i - 1);
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
            for (int i = 0; i < drawnEdges.size(); i++) {
                writer.append(System.getProperty("line.separator"));
                EdgeLine edge = (EdgeLine) drawnEdges.get(i);
                String node1 = Integer.toString(edge.node1 + 1);
                String node2 = Integer.toString(edge.node2 + 1);

                while (node1.length() < Integer.toString(drawnNodes.size()).length() + 1) {
                    node1 = " " + node1;
                }

                while (node2.length() < Integer.toString(drawnNodes.size()).length() + 1) {
                    node2 = " " + node2;
                }
                writer.write(node1 + " " + node2 + " 1");
            }
            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
            exportError();
        }
    }

    private void exportError() {
        //Error-Popup ausgeben
        String str = "Your output was invalid! Please choose a valid filepath and use the file extension '.net'.";

        JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
    }


    private BigDecimal edgeStartValue, edgeEndValue, edgeStepSize;
    private BigDecimal nodeStartValue, nodeEndValue, nodeStepSize;
    private int calculationSeriesMode = 0; //1 = resilience, 2 = reliability;
    private boolean onlyReliabilityFast;

    //Für die Serienberechnung in Schritten
    private void calculationSeries() {
        //Prüfen ob das Netz zusammenhängt

        if (Con_check.check(graph) != -1) {
            JOptionPane.showMessageDialog(mainFrame, "Your Graph is not connected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Sicherungskopie
        Graph graphSave = null;

        try {
            graphSave = (Graph) Util.serialClone(graph); //clone Graphen
        } catch (IOException | ClassNotFoundException e1) {
            System.err.println(e1.toString());
        }

        float[] probsSave = edgeProbabilities.clone();

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

            if (calculationSeriesMode == 1) {
                writer.write("Reliability of every edge                 Reliability of every node                 Resilience of the network");
            } else {
                writer.write("Reliability of every edge                 Reliability of every node                 Reliability of the network");
            }

            writer.append(System.getProperty("line.separator"));

            //Ab hier Berechnungsserie
            int counter = 1;

            BigInteger edgeStepCount = edgeEndValue.subtract(edgeStartValue).divide(edgeStepSize, BigDecimal.ROUND_FLOOR)
                    .add(BigDecimal.ONE).toBigInteger();
            BigInteger nodeStepCount = nodeEndValue.subtract(nodeStartValue).divide(nodeStepSize, BigDecimal.ROUND_FLOOR)
                    .add(BigDecimal.ONE).toBigInteger();

            String stepCount = edgeStepCount.multiply(nodeStepCount).toString();

            for (BigDecimal currentEdgeProb = edgeStartValue; currentEdgeProb.compareTo(edgeEndValue) <= 0;
                 currentEdgeProb = currentEdgeProb.add(edgeStepSize)) {

                for (BigDecimal currentNodeProb = nodeStartValue; currentNodeProb.compareTo(nodeEndValue) <= 0;
                     currentNodeProb = currentNodeProb.add(nodeStepSize)) {

                    resultText = "Calculation Series: Step " + counter + " of " + stepCount;
                    resultTextArea.setText(resultText);
                    counter++;

                    //currentEdgeProb ist reliability
                    //Neue/aktuelle Wahrscheinlichkeiten zuweisen
                    for (int j = 0; j < edgeProbabilities.length; j++) {
                        edgeProbabilities[j] = currentEdgeProb.floatValue();
                    }
                    for (int j = 0; j < nodeProbabilities.length; j++) {
                        nodeProbabilities[j] = currentNodeProb.floatValue();
                    }

                    if (calculationSeriesMode == 1) {
                        //Resilienz
                        calculate_resilience();
                    } else {
                        //Reliability
                        // Wahrscheinlichkeiten neu zuordnen. (wird in calculate_resilience() auch gemacht)
                        reassignProbabilities();

                        heidtmanns_reliability();
                    }

                    writer.append(System.getProperty("line.separator"));

                    String reliabilityString = currentEdgeProb.toString();

                    while (reliabilityString.length() < edgeStepSize.toString().length()) {
                        reliabilityString = reliabilityString + "0";
                    }

                    while (reliabilityString.length() < 42) {
                        reliabilityString = reliabilityString + " ";
                    }

                    reliabilityString += currentNodeProb.toString();
                    while (reliabilityString.length() < 42 + nodeStepSize.toString().length()) {
                        reliabilityString = reliabilityString + "0";
                    }

                    while (reliabilityString.length() < 84) {
                        reliabilityString = reliabilityString + " ";
                    }

                    if (calculationSeriesMode == 1) {
                        writer.write(reliabilityString + result_resilience);
                    } else {
                        writer.write(reliabilityString + prob);
                    }
                }

            }

            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //Setze den intern gespeicherten Graphen auf Anfangszustand zurück
        try {

            graph = (Graph) Util.serialClone(graphSave); //clone Graphen
        } catch (IOException | ClassNotFoundException e1) {
            System.err.println(e1.toString());
        }

        edgeProbabilities = probsSave.clone();


        calculationSeriesMode = 0;

        // Wahrscheinlichkeiten neu zuordnen.
        reassignProbabilities();

        resultText = "Calculation series finished. Please check your output file for the results.";
        resultTextArea.setText(resultText);

    }


}
