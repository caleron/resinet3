package com.resinet;/* Resinet3.java */

import com.resinet.algorithms.Con_check;
import com.resinet.algorithms.Zerleg;
import com.resinet.model.*;
import com.resinet.util.*;
import com.resinet.views.*;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Resinet3 extends JFrame
        implements ActionListener, NetPanel.GraphChangedListener {
    private NetPanel netPanel;

    private JTextArea graphPanelTextArea;
    private JTextField edgeEndProbabilityBox, edgeProbabilityStepSizeBox, nodeEndProbabilityBox, nodeProbabilityStepSizeBox,
            sameReliabilityEdgeProbBox, sameReliabilityNodeProbBox;
    private JButton drawBtn, resetGraphBtn, resetProbabilitiesBtn,
            probabilitiesOkBtn, calcReliabilityBtn, resilienceBtn, inputNetBtn, exportNetBtn;
    private JRadioButton singleReliabilityRadioBtn, sameReliabilityRadioBtn;


    JPanel sameReliabilityPanel, singleReliabilityPanel;

    public List<JTextField> edgeProbabilityBoxes = new ArrayList<>();
    private List<JTextField> nodeProbabilityBoxes = new ArrayList<>();
    private float[] edgeProbabilities;
    private float[] nodeProbabilities;
    private float prob;
    private String resultText;
    private JTextArea resultTextArea;

    private Graph graph;

    private Zerleg zer;

    //private Color backgroundColor = new Color(85, 143, 180);

    private JScrollPane probabilityFieldsScrollPane;
    private JCheckBox reliabilityCompareCheckBox;

    private static Resinet3 mainFrame;

    private GUI_STATES guiState;

    public enum GUI_STATES {
        SHOW_GRAPH_INFO,
        ENTER_GRAPH,
        CALCULATION_RUNNING
    }

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

        setGUIState(GUI_STATES.SHOW_GRAPH_INFO);
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
        resetGraphBtn.addActionListener(this);
        gbc = makegbc(1, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
        graphPanel.add(resetGraphBtn, gbc);

        //Button Input Network
        inputNetBtn = new JButton("Load Network");
        inputNetBtn.addActionListener(this);
        gbc = makegbc(2, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
        graphPanel.add(inputNetBtn, gbc);

        //Button Output Network
        exportNetBtn = new JButton("Save Network");
        exportNetBtn.addActionListener(this);
        gbc = makegbc(3, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
        graphPanel.add(exportNetBtn, gbc);


        netPanel = new NetPanel(this);
        netPanel.setBackground(Color.white);
        netPanel.setSize(625, 315);

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
        singleReliabilityRadioBtn = new JRadioButton("components have different reliabilities");
        buttonGroup.add(singleReliabilityRadioBtn);
        gbc = makegbc(0, 0, 2, 1, 1, 0, GridBagConstraints.BOTH, 5);
        probabilityGroupPanel.add(singleReliabilityRadioBtn, gbc);

        singleReliabilityRadioBtn.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setComponentReliabilityMode(false);
            }
        });

        sameReliabilityRadioBtn = new JRadioButton("components have same reliabilities", true);
        buttonGroup.add(sameReliabilityRadioBtn);
        gbc = makegbc(0, 1, 2, 1, 1, 0, GridBagConstraints.BOTH, 5);
        probabilityGroupPanel.add(sameReliabilityRadioBtn, gbc);

        sameReliabilityRadioBtn.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setComponentReliabilityMode(true);
            }
        });
        //Panel mit Wahrscheinlichkeitstextfeldern
        //probabilityFieldsPanel = new JPanel();
        probabilityFieldsScrollPane = new JScrollPane();//probabilityFieldsPanel);
        gbc = makegbc(0, 2, 2, 1, 1, 1);
        probabilityGroupPanel.add(probabilityFieldsScrollPane, gbc);

        //Reset-Button für alle Wahrscheinlichkeitstextfelder
        resetProbabilitiesBtn = new JButton("Reset");
        resetProbabilitiesBtn.addActionListener(this);
        gbc = makegbc(0, 3, 1, 1, 1, 0);
        probabilityGroupPanel.add(resetProbabilitiesBtn, gbc);

        probabilitiesOkBtn = new JButton("Ok");
        probabilitiesOkBtn.addActionListener(this);
        gbc = makegbc(1, 3, 1, 1, 1, 0);
        probabilityGroupPanel.add(probabilitiesOkBtn, gbc);

        initComponentReliabilityPanels();
    }

    /**
     * Baut die Anzeige mit den Textfeldern für die Wahrscheinlichkeiten auf
     */
    private void initComponentReliabilityPanels() {
        //probabilityFieldsScrollPane.getVerticalScrollBar().setValue(0);
        sameReliabilityPanel = new JPanel();
        sameReliabilityPanel.setLayout(new GridBagLayout());

        //Kantenwahrscheinlichkeiten
        JLabel edgeProbLabel = new JLabel("Reliability of every edge: ", SwingConstants.RIGHT);
        GridBagConstraints gbc = makegbc(0, 0, 2, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(edgeProbLabel, gbc);

        sameReliabilityEdgeProbBox = new JTextField(20);
        sameReliabilityEdgeProbBox.setBackground(Color.white);
        gbc = makegbc(2, 0, 2, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(sameReliabilityEdgeProbBox, gbc);

        //Knotenwahrscheinlichkeiten
        JLabel nodeProbLabel = new JLabel("Reliability of every node:", SwingConstants.RIGHT);
        gbc = makegbc(0, 1, 2, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(nodeProbLabel, gbc);

        sameReliabilityNodeProbBox = new JTextField(20);
        sameReliabilityNodeProbBox.setBackground(Color.white);
        gbc = makegbc(2, 1, 2, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(sameReliabilityNodeProbBox, gbc);

        JLabel stepValuesHeader = new JLabel("Optional for calculation series: ", SwingConstants.RIGHT);
        gbc = makegbc(0, 2, 4, 1, 1, 0, GridBagConstraints.VERTICAL);
        sameReliabilityPanel.add(stepValuesHeader, gbc);

        JLabel edgeProbEndValueLbl = new JLabel("Edge End value: ", SwingConstants.RIGHT);
        gbc = makegbc(0, 3, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(edgeProbEndValueLbl, gbc);

        edgeEndProbabilityBox = new JTextField(20);
        edgeEndProbabilityBox.setBackground(Color.white);
        gbc = makegbc(1, 3, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(edgeEndProbabilityBox, gbc);

        JLabel edgeStepSizeLbl = new JLabel("Step size (e.g. 0.01): ", SwingConstants.RIGHT);
        gbc = makegbc(2, 3, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(edgeStepSizeLbl, gbc);

        edgeProbabilityStepSizeBox = new JTextField(20);
        edgeProbabilityStepSizeBox.setBackground(Color.white);
        gbc = makegbc(3, 3, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(edgeProbabilityStepSizeBox, gbc);

        JLabel nodeEndValueLbl = new JLabel("Node End value: ", SwingConstants.RIGHT);
        gbc = makegbc(0, 4, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(nodeEndValueLbl, gbc);

        nodeEndProbabilityBox = new JTextField(20);
        nodeEndProbabilityBox.setBackground(Color.white);
        gbc = makegbc(1, 4, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(nodeEndProbabilityBox, gbc);

        JLabel nodeStepSizeLbl = new JLabel("Step size (e.g. 0.01): ", SwingConstants.RIGHT);
        gbc = makegbc(2, 4, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(nodeStepSizeLbl, gbc);

        nodeProbabilityStepSizeBox = new JTextField(20);
        nodeProbabilityStepSizeBox.setBackground(Color.white);
        gbc = makegbc(3, 4, 1, 1, 1, 0, GridBagConstraints.VERTICAL, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(nodeProbabilityStepSizeBox, gbc);

        //Einzelwahrscheinlichkeiten für die Komponenten
        singleReliabilityPanel = new JPanel();
        //singleReliabilityPanel.setPreferredSize(new Dimension(600, ((netPanel.drawnEdges.size() + netPanel.drawnNodes.size()) / 2 + 1) * 30));
        //singleReliabilityPanel.removeAll();
        singleReliabilityPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        //Mit Modus für gleiche Komponentenwahrscheinlichkeiten initialisieren
        setComponentReliabilityMode(true);
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
        resilienceBtn.addActionListener(this);
        gbc = makegbc(0, 0, 1, 1, 0, 0);
        calculatePanel.add(resilienceBtn, gbc);

        calcReliabilityBtn = new JButton("Calculate the reliability of the network");
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
        return makegbc(x, y, width, height, weightx, weighty, fill, leftInset, GridBagConstraints.CENTER);
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
     * @param anchor    Ausrichtung
     * @return GridBagConstraints
     */
    private GridBagConstraints makegbc(int x, int y, int width, int height, double weightx, double weighty, int fill, int leftInset, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;

        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.fill = fill;
        gbc.insets = new Insets(1, leftInset, 1, 1);
        gbc.anchor = anchor;

        return gbc;
    }


    /**
     * Setzt den aktuellen Status der Benutzeroberfläche, aktiviert und deaktiviert also Bedienelemente nach Bedarf
     *
     * @param state Der gewünschte Status
     */
    private void setGUIState(GUI_STATES state) {
        if (state == guiState)
            return;

        guiState = state;

        drawBtn.setEnabled(state == GUI_STATES.SHOW_GRAPH_INFO);
        exportNetBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        inputNetBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH || state == GUI_STATES.SHOW_GRAPH_INFO);
        resetGraphBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        netPanel.setVisible(state != GUI_STATES.SHOW_GRAPH_INFO);
        graphPanelTextArea.setVisible(state == GUI_STATES.SHOW_GRAPH_INFO);

        singleReliabilityRadioBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        sameReliabilityRadioBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        probabilitiesOkBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        resetProbabilitiesBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);

        calcReliabilityBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        resilienceBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        reliabilityCompareCheckBox.setEnabled(state == GUI_STATES.ENTER_GRAPH);
    }

    /**
     * Schaltet zwischen den Komponentenzuverlässigkeitsmodis um
     *
     * @param sameReliability Ob für alle Komponenten die selbe Wahrscheinlichkeit gilt
     */
    private void setComponentReliabilityMode(boolean sameReliability) {
        if (sameReliability) {
            probabilityFieldsScrollPane.getViewport().setView(sameReliabilityPanel);
        } else {
            probabilityFieldsScrollPane.getViewport().setView(singleReliabilityPanel);
            updateSingleReliabilityProbPanel();
        }
    }

    /**
     * Wird beim Mausklick auf Buttons ausgelöst
     *
     * @param evt Das Click-Event
     */
    public void actionPerformed(ActionEvent evt) {
        JButton button = (JButton) evt.getSource();

        if (button == drawBtn) {
            //Graph zeichnen
            setGUIState(GUI_STATES.ENTER_GRAPH);
        }

        if (button == inputNetBtn) {
            //Graph aus Datei laden
            setGUIState(GUI_STATES.ENTER_GRAPH);

            graph = null;
            zer = null;
            singleReliabilityPanel.removeAll();
            singleReliabilityPanel.repaint();

            netPanel.resetGraph();

            GraphSaving.inputNet(netPanel);
            netPanel.repaint();
        }

        if (button == exportNetBtn) {
            //Aktuellen Graph speichern
            GraphSaving.exportNet(netPanel);
        }

        if (button == resetGraphBtn) {
            //Graph zurücksetzen
            setGUIState(GUI_STATES.ENTER_GRAPH);

            netPanel.resetGraph();

            graph = null;
            zer = null;
            singleReliabilityPanel.removeAll();
            singleReliabilityPanel.repaint();
        }

        if (button == probabilitiesOkBtn) {
            int edgeCount = edgeProbabilityBoxes.size();
            int nodeCount = nodeProbabilityBoxes.size();
            edgeProbabilities = new float[edgeCount];
            nodeProbabilities = new float[nodeCount];
            boolean seriesValuesMissing = false;

            MyList edgesWithMissingProbability = new MyList();
            MyList nodesWithMissingProbability = new MyList();

            //Prüft alle Felder durch, ob da Wahrscheinlichkeiten drin stehen
            for (int i = 0; i < edgeCount; i++) {
                String s = edgeProbabilityBoxes.get(i).getText();
                if (textIsNotProbability(s))
                    edgesWithMissingProbability.add(String.valueOf(i));
            }

            for (int i = 0; i < nodeCount; i++) {
                String s = nodeProbabilityBoxes.get(i).getText();
                if (textIsNotProbability(s))
                    nodesWithMissingProbability.add(String.valueOf(i));
            }

            if (sameReliabilityRadioBtn.isSelected()) {
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
                edgeProbabilityBoxes.get(i).setEditable(false);
                String s = edgeProbabilityBoxes.get(i).getText();
                Float fo = Float.valueOf(s);
                edgeProbabilities[i] = fo;
            }

            for (int i = 0; i < nodeCount; i++) {
                nodeProbabilityBoxes.get(i).setEditable(false);
                String s = nodeProbabilityBoxes.get(i).getText();
                Float fo = Float.valueOf(s);
                nodeProbabilities[i] = fo;
            }

            resetProbabilitiesBtn.setEnabled(true);
            probabilitiesOkBtn.setEnabled(false);

            if (graph != null)
                calcReliabilityBtn.setEnabled(true);
            resilienceBtn.setEnabled(true);

        /*Wahrscheinlichkeiten neu zuordnen.*/
            reassignProbabilities();


            //End value und step size zuordnen
            if (sameReliabilityRadioBtn.isSelected()) {
                if (sameReliabilityEdgeProbBox.getText().length() != 0) {
                    edgeStartValue = new BigDecimal(sameReliabilityEdgeProbBox.getText());
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

                if (sameReliabilityNodeProbBox.getText().length() != 0) {
                    nodeStartValue = new BigDecimal(sameReliabilityNodeProbBox.getText());
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
            //Alle Wahrscheinlichkeitsfelder zurücksetzen
            setGUIState(GUI_STATES.ENTER_GRAPH);

            edgeProbabilities = null;

            for (JTextField edgeProbabilityTextField : edgeProbabilityBoxes) {
                edgeProbabilityTextField.setText(null);
                edgeProbabilityTextField.setEditable(true);
            }
            //TODO statt editable disabled verwenden
            for (JTextField nodeProbabilityTextField : nodeProbabilityBoxes) {
                nodeProbabilityTextField.setText(null);
                nodeProbabilityTextField.setEditable(true);
            }

            if (sameReliabilityRadioBtn.isSelected()) {
                edgeEndProbabilityBox.setText(null);
                edgeProbabilityStepSizeBox.setText(null);
                nodeEndProbabilityBox.setText(null);
                nodeProbabilityStepSizeBox.setText(null);
            }
        }

        if (button == resilienceBtn) {
            //Resilienzberechnung starten
            if (!edgeEndValue.equals(BigDecimal.ZERO) && !edgeStepSize.equals(BigDecimal.ZERO) &&
                    !nodeEndValue.equals(BigDecimal.ZERO) && !nodeStepSize.equals(BigDecimal.ZERO) && sameReliabilityRadioBtn.isSelected()) {

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
            //Zuverlässigkeitsberechnung starten
            //heidtmanns_reliability();
            //fact_reliability();

            if (!edgeEndValue.equals(BigDecimal.ZERO) && !edgeStepSize.equals(BigDecimal.ZERO) &&
                    !nodeEndValue.equals(BigDecimal.ZERO) && !nodeStepSize.equals(BigDecimal.ZERO) && sameReliabilityRadioBtn.isSelected()) {
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
     * Wird ausgelöst, wenn der Graph verändert wurde
     */
    @Override
    public void graphChanged() {
        updateSingleReliabilityProbPanel();
    }

    /**
     * Aktualisiert das Wahrscheinlichkeitspanel
     */
    private void updateSingleReliabilityProbPanel() {
        if (sameReliabilityRadioBtn.isSelected())
            return;


        int edgeCount = netPanel.drawnEdges.size();
        int edgeBoxCount = edgeProbabilityBoxes.size();
        int nodeCount = netPanel.drawnNodes.size();
        int nodeBoxCount = nodeProbabilityBoxes.size();

        for (int i = edgeBoxCount; i < edgeCount; i++) {
            addFieldToProbPanel(i, false);
        }

        for (int i = nodeBoxCount; i < nodeCount; i++) {
            addFieldToProbPanel(i, true);
        }

        for (int i = edgeBoxCount; i > edgeCount; i--) {
            JTextField textField =  edgeProbabilityBoxes.get(edgeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            edgeProbabilityBoxes.remove(textField);
        }

        for (int i = nodeBoxCount; i > nodeCount; i--) {
            JTextField textField =  nodeProbabilityBoxes.get(nodeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            nodeProbabilityBoxes.remove(textField);
        }
        probabilityFieldsScrollPane.validate();
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
            nodeProbabilityBoxes.add(textField);
        } else {
            edgeProbabilityBoxes.add(textField);
        }
        Panel panel = new Panel();
        panel.add(label);
        panel.add(textField);
        singleReliabilityPanel.add(panel);
    }

    private void checkGraph() {

        if (netPanel.drawnEdges.size() == 0) {
            //Dieser Block zeigt ein Hinweisfenster an, wenn keine Knoten vorhanden sind und bricht die Methode ab
            String str = "Your Network does not contain edges!";

            Toolkit.getDefaultToolkit().beep();

            JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Anzahl Konnektionsknoten bestimmen
        int count = 0;
        MyIterator np = netPanel.drawnNodes.iterator();
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
    }

    /**
     * Weist alle Wahrscheinlichkeiten aus den Eingabefeldern den Elementen im Graphen neu zu
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
     * Erstellt aus den Knoten und Kanten des gezeichneten Graphen ein Graph-Objekt
     *
     * @return das Graph-Objekt zum gezeichneten Graph
     */
    private Graph makeGraph() {
        MyList nodeList = new MyList();
        MyList edgeList = new MyList();

        MyIterator it = netPanel.drawnNodes.iterator();
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
        it = netPanel.drawnEdges.iterator();
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

    //TODO für die Berechnung graphen vorher checken
    //TODO graph für Berechnung erzeugen mit graph = makeGraph();

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
        total_nodes = netPanel.drawnNodes.size();

        //Anzahl der K-Knoten
        c_nodes = 0;

        // Sicherung der K-Knotenliste
        String cNodeList = "";
        for (int i = 0; i < total_nodes; i++) {
            NodePoint nodeSave = (NodePoint) netPanel.drawnNodes.get(i);
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
                NodePoint node1 = (NodePoint) netPanel.drawnNodes.get(i);
                //com.resinet.model.Node node1 = (com.resinet.model.Node)graph.nodeList.get(i);

                // Dann auf true, falls K-Knoten
                node1.k = d.contains(i);

                // Schreibe jeden Knoten neu in die Knotenliste.
                netPanel.drawnNodes.set(i, node1);
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
            NodePoint nodeReset = (NodePoint) netPanel.drawnNodes.get(i);

            // Dann auf true, falls K-Knoten
            nodeReset.k = cNodeList.charAt(i) == '1';

            // Schreibe jeden Knoten neu in die Knotenliste.
            netPanel.drawnNodes.set(i, nodeReset);
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
