package com.resinet;

import com.resinet.algorithms.Con_check;
import com.resinet.algorithms.ProbabilityCalculator;
import com.resinet.model.*;
import com.resinet.util.Constants;
import com.resinet.util.GraphSaving;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;
import com.resinet.views.NetPanel;
import com.resinet.views.SingleReliabilitiesPanel;
import com.resinet.views.SingleReliabilityPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse stellt das Hauptfenster dar. Die Projektstruktur ist wie folgt aufgebaut:
 * <p>
 * <strong>algorithms:</strong> Alle Algorithmen, die die Hauptfunktionen des Programms betreffen
 * <p>
 * <strong>img:</strong> Bilder und Grafiken
 * <p>
 * <strong>model:</strong> Klassen, die wenig oder keine Funktionalität bieten, also nur dazu dienen, Daten zu halten
 * <p>
 * <strong>util:</strong> Hilfsklassen und -funktionen, die nicht die Hauptfunktion des Programms enthalten, aber diese
 * unterstützen
 * <p>
 * <strong>views:</strong> GUI-Komponentenklassen
 */
public class Resinet3 extends JFrame
        implements ActionListener, NetPanel.GraphChangedListener, ProbabilityCalculator.CalculationProgressListener, Constants {
    public NetPanel netPanel;

    private JTextArea graphPanelTextArea, resultTextArea;
    private JTextField edgeEndProbabilityBox, edgeProbabilityStepSizeBox, nodeEndProbabilityBox, nodeProbabilityStepSizeBox,
            sameReliabilityEdgeProbBox, sameReliabilityNodeProbBox;
    private JButton drawBtn, resetGraphBtn, resetProbabilitiesBtn,
            calcReliabilityBtn, resilienceBtn, inputNetBtn, exportNetBtn;
    private JRadioButton singleReliabilityRadioBtn, sameReliabilityRadioBtn;
    private JCheckBox considerNodesBox, considerEdgesBox;

    private JScrollPane probabilityFieldsScrollPane;

    private JProgressBar calculationProgressBar;

    private JPanel sameReliabilityPanel;
    private SingleReliabilitiesPanel singleReliabilitiesPanel;

    private List<JTextField> edgeProbabilityBoxes = new ArrayList<>();
    private List<JTextField> nodeProbabilityBoxes = new ArrayList<>();

    private static Resinet3 mainFrame;

    private GUI_STATES guiState;
    private boolean considerNodeSingleReliabilities = true;
    private boolean considerEdgeSingleReliabilities = true;

    private enum GUI_STATES {
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
            mainFrame.setTitle("ResiNet3");
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }

    /**
     * Leitet alle Initialisierungen ein
     */
    private void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Windows Look-and-Feel setzen
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        GridBagLayout mainLayout = new GridBagLayout();
        setLayout(mainLayout);

        initLogo();

        initGraphPanel();

        initProbabilitiesPanel();

        initCalculatePanel();

        initOutputTextPanel();

        setGUIState(GUI_STATES.SHOW_GRAPH_INFO);
    }
    //TODO Texte in Lokalisationsdatei auslagern
    //TODO weitere Funktionen auslagern, wie Überprüfung des Graphen
    //TODO Logo entfernen und Menü hinzufügen
    //TODO beim Speichern vom Graphen "weiße Flächen" an den Rändern entfernen
    //TODO Zuletzt geöffnet-Liste, Graph generieren, Serienparallelreduktion, neues GUI-Layout mit größerer Zeichenfläche

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
        graphPanel.setBorder(BorderFactory.createTitledBorder("Please first input your network graph:"));
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
        String notetext = "On this panel you can draw the graph of your network after a click on the \"Draw\" button.\n" +
                "Distinguish between two types of vertices:\n" +
                "Press the left button of your input device (i.e. mouse) to draw a white non-terminal vertex and the " +
                "right button to draw a black terminal vertex.\n" +
                "To turn an already drawn vertex into a terminal vertex point to it, hold the Ctrl-Key and press the right button of your input device.\n" +
                "Delete a vertex or an edge by holding the <shift>-key and left clicking it.\n" +
                "To draw an edge press the left button when the mouse pointer is on a vertex and hold it. " +
                "Then drag the mouse to another vertex and release it. \n" +
                "\n" +
                "When you have completed the graph of the network, you may save it as a file by pressing the Save Network button above.\n" +
                "\nYou can also import a previously created network " +
                "(ResiNeT or Pajek) by clicking the \"Load\" button. ";

        graphPanelTextArea = new JTextArea(notetext, 10, 50);
        graphPanelTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
        graphPanelTextArea.setBackground(Color.white);
        graphPanelTextArea.setLineWrap(true);
        graphPanelTextArea.setWrapStyleWord(true);
        graphPanelTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(graphPanelTextArea);
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
        gbc = makegbc(0, 0, 1, 1, 1, 0, GridBagConstraints.BOTH, 5);
        probabilityGroupPanel.add(singleReliabilityRadioBtn, gbc);

        singleReliabilityRadioBtn.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setComponentReliabilityMode(false);
            }
        });

        considerNodesBox = new JCheckBox("Consider vertex reliabilities");
        gbc = makegbc(1, 0, 1, 1, 1, 0);
        probabilityGroupPanel.add(considerNodesBox, gbc);
        considerNodesBox.setSelected(true);
        considerNodesBox.addItemListener((e) -> {
            considerNodeSingleReliabilities = (e.getStateChange() == ItemEvent.SELECTED);
            updateConsideredComponents();
        });

        considerEdgesBox = new JCheckBox("Consider edge reliabilities");
        gbc = makegbc(1, 1, 1, 1, 1, 0);
        probabilityGroupPanel.add(considerEdgesBox, gbc);
        considerEdgesBox.setSelected(true);
        considerEdgesBox.addItemListener((e) -> {
            considerEdgeSingleReliabilities = (e.getStateChange() == ItemEvent.SELECTED);
            updateConsideredComponents();
        });

        sameReliabilityRadioBtn = new JRadioButton("components have same reliabilities", true);
        buttonGroup.add(sameReliabilityRadioBtn);
        gbc = makegbc(0, 1, 1, 1, 1, 0, GridBagConstraints.BOTH, 5);
        probabilityGroupPanel.add(sameReliabilityRadioBtn, gbc);

        sameReliabilityRadioBtn.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setComponentReliabilityMode(true);
            }
        });
        //Panel mit Wahrscheinlichkeitstextfeldern
        //probabilityFieldsPanel = new JPanel();
        probabilityFieldsScrollPane = new JScrollPane();//probabilityFieldsPanel);
        probabilityFieldsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        probabilityFieldsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        probabilityFieldsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        probabilityFieldsScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        //probabilityFieldsScrollPane.setDoubleBuffered(true);
        gbc = makegbc(0, 2, 2, 1, 1, 1);
        probabilityGroupPanel.add(probabilityFieldsScrollPane, gbc);

        //Reset-Button für alle Wahrscheinlichkeitstextfelder
        resetProbabilitiesBtn = new JButton("Reset");
        resetProbabilitiesBtn.addActionListener(this);
        gbc = makegbc(0, 3, 2, 1, 1, 0);
        probabilityGroupPanel.add(resetProbabilitiesBtn, gbc);

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
        GridBagConstraints gbc = makegbc(0, 0, 2, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(edgeProbLabel, gbc);

        sameReliabilityEdgeProbBox = new JTextField(20);
        sameReliabilityEdgeProbBox.setBackground(Color.white);
        gbc = makegbc(2, 0, 2, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(sameReliabilityEdgeProbBox, gbc);

        //Knotenwahrscheinlichkeiten
        JLabel nodeProbLabel = new JLabel("Reliability of every vertex:", SwingConstants.RIGHT);
        gbc = makegbc(0, 1, 2, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(nodeProbLabel, gbc);

        sameReliabilityNodeProbBox = new JTextField(20);
        sameReliabilityNodeProbBox.setBackground(Color.white);
        gbc = makegbc(2, 1, 2, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(sameReliabilityNodeProbBox, gbc);

        JLabel stepValuesHeader = new JLabel("Optional for calculation series: ", SwingConstants.RIGHT);
        gbc = makegbc(0, 2, 4, 1, 1, 0, GridBagConstraints.NONE);
        sameReliabilityPanel.add(stepValuesHeader, gbc);

        JLabel edgeProbEndValueLbl = new JLabel("Edge End value: ", SwingConstants.RIGHT);
        gbc = makegbc(0, 3, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(edgeProbEndValueLbl, gbc);

        edgeEndProbabilityBox = new JTextField(20);
        edgeEndProbabilityBox.setBackground(Color.white);
        gbc = makegbc(1, 3, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(edgeEndProbabilityBox, gbc);

        JLabel edgeStepSizeLbl = new JLabel("Step size (e.g. 0.01): ", SwingConstants.RIGHT);
        gbc = makegbc(2, 3, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(edgeStepSizeLbl, gbc);

        edgeProbabilityStepSizeBox = new JTextField(20);
        edgeProbabilityStepSizeBox.setBackground(Color.white);
        gbc = makegbc(3, 3, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(edgeProbabilityStepSizeBox, gbc);

        JLabel nodeEndValueLbl = new JLabel("Vertex End value: ", SwingConstants.RIGHT);
        gbc = makegbc(0, 4, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(nodeEndValueLbl, gbc);

        nodeEndProbabilityBox = new JTextField(20);
        nodeEndProbabilityBox.setBackground(Color.white);
        gbc = makegbc(1, 4, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(nodeEndProbabilityBox, gbc);

        JLabel nodeStepSizeLbl = new JLabel("Step size (e.g. 0.01): ", SwingConstants.RIGHT);
        gbc = makegbc(2, 4, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_END);
        sameReliabilityPanel.add(nodeStepSizeLbl, gbc);

        nodeProbabilityStepSizeBox = new JTextField(20);
        nodeProbabilityStepSizeBox.setBackground(Color.white);
        gbc = makegbc(3, 4, 1, 1, 1, 0, GridBagConstraints.NONE, 1, GridBagConstraints.LINE_START);
        sameReliabilityPanel.add(nodeProbabilityStepSizeBox, gbc);

        //Einzelwahrscheinlichkeiten für die Komponenten
        singleReliabilitiesPanel = new SingleReliabilitiesPanel();
        //singleReliabilitiesPanel.setPreferredSize(probabilityFieldsScrollPane.getSize());

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

        calcReliabilityBtn = new JButton("Calculate the reliability of the network");
        calcReliabilityBtn.addActionListener(this);
        gbc = makegbc(0, 0, 1, 1, 1, 0);
        calculatePanel.add(calcReliabilityBtn, gbc);

        resilienceBtn = new JButton("Calculate the resilience of the network");
        resilienceBtn.addActionListener(this);
        gbc = makegbc(1, 0, 1, 1, 1, 0);
        calculatePanel.add(resilienceBtn, gbc);

        calculationProgressBar = new JProgressBar();
        calculationProgressBar.setStringPainted(true);
        gbc = makegbc(0, 1, 2, 1, 1, 0);
        calculatePanel.add(calculationProgressBar, gbc);
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
        resultTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
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

        boolean sameReliability = sameReliabilityRadioBtn.isSelected();

        singleReliabilityRadioBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        sameReliabilityRadioBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        considerNodesBox.setEnabled(state == GUI_STATES.ENTER_GRAPH && !sameReliability);
        considerEdgesBox.setEnabled(state == GUI_STATES.ENTER_GRAPH && !sameReliability);
        resetProbabilitiesBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);

        calcReliabilityBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);
        resilienceBtn.setEnabled(state == GUI_STATES.ENTER_GRAPH);

        //Status für alle Wahrscheinlichkeitsfelder setzen
        boolean probabilityBoxesEnabled = (state == GUI_STATES.ENTER_GRAPH);
        if (singleReliabilityRadioBtn.isSelected()) {
            setChildrenEnabled(singleReliabilitiesPanel, probabilityBoxesEnabled);
        } else {
            setChildrenEnabled(sameReliabilityPanel, probabilityBoxesEnabled);
        }
    }

    /**
     * Setzt den Enabled-Status für alle Subkomponenten
     *
     * @param el      Der Container
     * @param enabled Enabled-Status der Subkomponenten
     */
    private void setChildrenEnabled(Container el, boolean enabled) {
        for (Component component : el.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                setChildrenEnabled((Container) component, enabled);
            }
        }
    }

    /**
     * Schaltet zwischen den Komponentenzuverlässigkeitsmodis um
     *
     * @param sameReliability Ob für alle Komponenten die selbe Wahrscheinlichkeit gilt
     */
    private void setComponentReliabilityMode(boolean sameReliability) {
        considerEdgesBox.setEnabled(!sameReliability);
        considerNodesBox.setEnabled(!sameReliability);
        if (sameReliability) {
            probabilityFieldsScrollPane.getViewport().setView(sameReliabilityPanel);
        } else {
            probabilityFieldsScrollPane.getViewport().setView(singleReliabilitiesPanel);
            updateSingleReliabilityProbPanel();
        }
        netPanel.setReliabilityMode(sameReliability);
    }

    /**
     * Setzt den Text in der Ergebnistextbox
     *
     * @param text Der Text
     */
    private void setResultText(String text) {
        resultTextArea.setText(text);
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
            GraphSaving.inputNet(mainFrame, netPanel);
            netPanel.repaint();
            updateSingleReliabilityProbPanel();
        }

        if (button == exportNetBtn) {
            //Aktuellen Graph speichern
            GraphSaving.exportNet(mainFrame, netPanel);
        }

        if (button == resetGraphBtn) {
            resetGraph();
        }

        if (button == resetProbabilitiesBtn) {
            //Alle Wahrscheinlichkeitsfelder zurücksetzen
            resetProbabilityFields();
        }

        if (button == resilienceBtn) {
            //Resilienzberechnung starten
            startCalculation(CALCULATION_MODES.RESILIENCE);
        }

        if (button == calcReliabilityBtn) {
            //Zuverlässigkeitsberechnung starten
            startCalculation(CALCULATION_MODES.RELIABILITY);
        }
    }

    /**
     * Setzt den Graph und alle damit verbundenen Variablen zurück und stellt die GUI um
     */
    public void resetGraph() {
        setGUIState(GUI_STATES.ENTER_GRAPH);

        netPanel.resetGraph();

        singleReliabilitiesPanel.removeAll();
        singleReliabilitiesPanel.repaint();

        nodeProbabilityBoxes.clear();
        edgeProbabilityBoxes.clear();
    }

    /**
     * Setzt die Werte aller Einzelwahrscheinlichkeitsfelder zurück
     */
    private void resetProbabilityFields() {
        for (JTextField edgeProbabilityTextField : edgeProbabilityBoxes) {
            edgeProbabilityTextField.setText(null);
        }

        for (JTextField nodeProbabilityTextField : nodeProbabilityBoxes) {
            nodeProbabilityTextField.setText(null);
        }

        sameReliabilityEdgeProbBox.setText(null);
        sameReliabilityNodeProbBox.setText(null);
        edgeEndProbabilityBox.setText(null);
        edgeProbabilityStepSizeBox.setText(null);
        nodeEndProbabilityBox.setText(null);
        nodeProbabilityStepSizeBox.setText(null);
    }


    /**
     * Wird ausgelöst, wenn sich der Berechnungsstatus ändert
     *
     * @param currentStep Der aktuelle Schritt
     */
    @Override
    public void calculationProgressChanged(Integer currentStep) {
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> calculationProgressChanged(currentStep));
            return;
        }
        calculationProgressBar.setValue(currentStep);
        //Damit lässt sich der Text auf der Progressbar ändern (für später)
        //calculationProgressBar.setString("");
        setResultText("Step " + currentStep + " of " + calculationProgressBar.getMaximum());
    }

    /**
     * Wird ausgelöst, um die Schrittzahl festzusetzen
     *
     * @param stepCount Die maximale Anzahl an Schritten der aktuellen Berechnungsaufgabe
     */
    @Override
    public void reportCalculationStepCount(Integer stepCount) {
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> reportCalculationStepCount(stepCount));
            return;
        }
        calculationProgressBar.setValue(0);
        calculationProgressBar.setMaximum(stepCount);
        setResultText("Step 0 of " + stepCount);
    }

    /**
     * Wird ausgelöst, wenn die Berechnung fertig ist
     *
     * @param status Das Ergebnis
     */
    @Override
    public void calculationFinished(String status) {
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> calculationFinished(status));
            return;
        }
        setResultText(status);
        //GUI wieder aktivieren, da Berechnung fertig
        setGUIState(GUI_STATES.ENTER_GRAPH);
    }


    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente hinzugefügt wird
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    public void graphElementAdded(boolean isNode, int number) {
        if (sameReliabilityRadioBtn.isSelected())
            return;

        if ((!isNode && !considerEdgeSingleReliabilities) || (isNode && !considerNodeSingleReliabilities))
            return;

        addFieldToProbPanel(number, isNode);

        refreshSingleReliabilityScrollPane();
    }

    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente gelöscht wird.
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    public void graphElementDeleted(boolean isNode, int number) {
        if (sameReliabilityRadioBtn.isSelected())
            return;

        List<JTextField> list;
        if (isNode) {
            list = nodeProbabilityBoxes;
        } else {
            list = edgeProbabilityBoxes;
        }

        //Alle Wahrscheinlichkeiten ein Feld vorrücken lassen
        for (int i = number; i < list.size() - 1; i++) {
            list.get(i).setText(list.get(i + 1).getText());
        }

        //Letztes Element entfernen
        JTextField textField = list.get(list.size() - 1);
        textField.getParent().getParent().remove(textField.getParent());
        list.remove(textField);

        refreshSingleReliabilityScrollPane();
    }

    /**
     * Setzt die Zuverlässigkeit einer Netzwerkkomponente
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     * @param value  Die Zuverlässigkeit der Komponente
     */
    public void setElementReliability(boolean isNode, int number, String value) {
        if (isNode) {
            nodeProbabilityBoxes.get(number).setText(value);
        } else {
            edgeProbabilityBoxes.get(number).setText(value);
        }
    }

    /**
     * Aktualisiert, was im Graphen angeklickt werden kann und aktualisiert das Wahrscheinlichkeitspanel
     */
    private void updateConsideredComponents() {
        netPanel.edgeClickable = considerEdgeSingleReliabilities;
        netPanel.nodeClickable = considerNodeSingleReliabilities;
        updateSingleReliabilityProbPanel();
    }

    /**
     * Aktualisiert das Wahrscheinlichkeitspanel
     */
    public void updateSingleReliabilityProbPanel() {
        if (sameReliabilityRadioBtn.isSelected())
            return;

        //Knoten/Kantenzahl auf 0 setzen, wenn sie nicht berücksichtigt werden sollen
        int edgeCount = considerEdgeSingleReliabilities ? netPanel.drawnEdges.size() : 0;
        int edgeBoxCount = edgeProbabilityBoxes.size();
        int nodeCount = considerNodeSingleReliabilities ? netPanel.drawnNodes.size() : 0;
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
            JTextField textField = edgeProbabilityBoxes.get(edgeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            edgeProbabilityBoxes.remove(textField);
        }

        //Überflüssige Knotenwahrscheinlichkeitsfelder entfernen
        for (int i = nodeBoxCount; i > nodeCount; i--) {
            JTextField textField = nodeProbabilityBoxes.get(nodeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            nodeProbabilityBoxes.remove(textField);
        }

        refreshSingleReliabilityScrollPane();
    }

    /**
     * Soll das Layout vom Scrollpane neu auslösen und die Größe festlegen
     */
    private void refreshSingleReliabilityScrollPane() {
        probabilityFieldsScrollPane.revalidate();
        probabilityFieldsScrollPane.repaint();
    }


    /**
     * Fügt dem Wahrscheinlichkeitspanel ein Panel für die Wahrscheinlichkeit einer Komponente hinzu
     *
     * @param number     Nummer des Felds
     * @param isNodeProb True, wenn das Feld für einen Knoten ist, false bei Kante
     */
    private void addFieldToProbPanel(int number, boolean isNodeProb) {
        SingleReliabilityPanel newPanel = new SingleReliabilityPanel(isNodeProb, number);

        if (isNodeProb) {
            nodeProbabilityBoxes.add(newPanel.getTextField());
            singleReliabilitiesPanel.add(newPanel);
        } else {
            edgeProbabilityBoxes.add(newPanel.getTextField());

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
                singleReliabilitiesPanel.add(newPanel);
            }
        }
    }

    /**
     * Trägt die Berechnungsparameter in die Felder ein. Voraussetzung ist, dass bereits der richtige Graph geladen
     * wurde
     *
     * @param params Die Berechnungsparameter
     */
    public void loadCalculationParams(CalculationParams params) {
        //richtiges Panel laden
        setComponentReliabilityMode(params.sameReliabilityMode);
        if (params.sameReliabilityMode) {
            //Radiobutton auswählen
            sameReliabilityRadioBtn.setSelected(true);

            //Werte eintragen
            sameReliabilityEdgeProbBox.setText(params.edgeStartValue.toString());
            sameReliabilityNodeProbBox.setText(params.nodeStartValue.toString());

            //Die Parameter sind null, wenn keine Berechnungsserie eingestellt ist
            if (params.calculationSeries) {
                edgeProbabilityStepSizeBox.setText(params.edgeStepSize.toString());
                nodeProbabilityStepSizeBox.setText(params.nodeStepSize.toString());

                edgeEndProbabilityBox.setText(params.edgeEndValue.toString());
                nodeEndProbabilityBox.setText(params.nodeEndValue.toString());
            } else {
                //Felder leer setzen, wenn nichts dazu eingespeichert
                edgeProbabilityStepSizeBox.setText("");
                nodeProbabilityStepSizeBox.setText("");

                edgeEndProbabilityBox.setText("");
                nodeEndProbabilityBox.setText("");
            }
        } else {
            singleReliabilityRadioBtn.setSelected(true);
            //Einzelwahrscheinlichkeiten in die Felder eintragen
            for (int i = 0; i < edgeProbabilityBoxes.size(); i++) {
                edgeProbabilityBoxes.get(i).setText(params.edgeProbabilities[i].toString());
            }

            for (int i = 0; i < nodeProbabilityBoxes.size(); i++) {
                nodeProbabilityBoxes.get(i).setText(params.nodeProbabilities[i].toString());
            }
        }
    }

    /**
     * Prüft alle Eingabefelder auf zulässige Werte
     *
     * @return True, wenn alle Felder Wahrscheinlichkeiten zwischen 0 und 1 enthalten
     */
    private boolean probabilitiesValid(boolean suppressWarning) {
        int edgeCount = edgeProbabilityBoxes.size();
        int nodeCount = nodeProbabilityBoxes.size();
        boolean seriesValuesMissing = false;
        boolean sameReliabilityValuesMissing = false;

        MyList edgesWithMissingProbability = new MyList();
        MyList nodesWithMissingProbability = new MyList();

        if (sameReliabilityRadioBtn.isSelected()) {
            //Felder für die Berechnungsserien prüfen
            boolean seriesFieldFilled = false;
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
                } else {
                    seriesFieldFilled = true;
                }
            }
            //Fehler nur anzeigen, falls mindestens ein und nicht alle Felder ausgefüllt sind
            seriesValuesMissing = seriesFieldFilled && seriesValuesMissing;

            //Felder für die Wahrscheinlichkeit für alle Kanten/Knoten prüfen
            String edgeValue = sameReliabilityEdgeProbBox.getText(),
                    nodeValue = sameReliabilityNodeProbBox.getText();
            if (textIsNotProbability(edgeValue) || textIsNotProbability(nodeValue)) {
                sameReliabilityValuesMissing = true;
            }
        } else {
            //Einzelwahrscheinlichkeitsmodus
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
        }


        if (edgesWithMissingProbability.size() != 0 || nodesWithMissingProbability.size() != 0
                || seriesValuesMissing || sameReliabilityValuesMissing) {

            if (suppressWarning) {
                //Wenn keine Warnungen ausgegeben werden soll, einfach abbrechen
                return false;
            }

            //Dieser Block zeigt nur ein Hinweisfenster an, falls Wahrscheinlichkeiten fehlen
            //Strings für fehlende Kanten und Knoten generieren
            String missingProbabilityEdges = "";
            if (edgesWithMissingProbability.size() != 0) {
                MyIterator it = edgesWithMissingProbability.iterator();
                missingProbabilityEdges = (String) it.next();
                while (it.hasNext()) {
                    missingProbabilityEdges += ", " + it.next();
                }
            }

            String missingProbabilityNodes = "";
            if (nodesWithMissingProbability.size() != 0) {
                MyIterator it = nodesWithMissingProbability.iterator();
                missingProbabilityNodes = (String) it.next();
                while (it.hasNext()) {
                    missingProbabilityNodes += ", " + it.next();
                }
            }

            String str = "The reliability of an edge is a probability, thus\nit must be a number in format x.xxxxxx " +
                    "which is\nless than or equal to 1. \n";

            //Passende Texte hinzufügen
            if (edgesWithMissingProbability.size() != 0 || nodesWithMissingProbability.size() != 0) {
                str += "Please check the in-\nput for edge\n" + missingProbabilityEdges +
                        "\n and for vertex\n" + missingProbabilityNodes + "\n";
            }

            if (seriesValuesMissing) {
                str += "Please check the input for the calculation series.";
            }

            if (sameReliabilityValuesMissing) {
                str += "Please check the input for the same reliability fields.";
            }

            JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Prüft alle Wahrscheinlichkeitsfelder und speicher die Werte in die entsprechenden Variablen
     *
     * @return Das Objekt oder null, wenn nicht alle Voraussetzungen erfüllt sind
     */
    public CalculationParams buildCalculationParams(CALCULATION_MODES mode, boolean suppressWarning) {
        Graph graph = makeGraph();
        if (!graphIsValid(graph))
            return null;

        CalculationParams params = new CalculationParams(mode, graph);

        if (!probabilitiesValid(suppressWarning)) {
            //Abbrechen, falls nicht alle Wahrscheinlichkeiten zulässig sind
            return null;
        }

        if (sameReliabilityRadioBtn.isSelected()) {
            params.setReliabilityMode(true);
            //Serienberechnungsvariablen zuweisen
            BigDecimal edgeStartValue = new BigDecimal(sameReliabilityEdgeProbBox.getText());
            BigDecimal nodeStartValue = new BigDecimal(sameReliabilityNodeProbBox.getText());
            BigDecimal edgeEndValue = BigDecimal.ZERO, edgeStepSize = BigDecimal.ZERO, nodeEndValue = BigDecimal.ZERO, nodeStepSize = BigDecimal.ZERO;
            try {
                edgeEndValue = new BigDecimal(edgeEndProbabilityBox.getText());
                edgeStepSize = new BigDecimal(edgeProbabilityStepSizeBox.getText());
                nodeEndValue = new BigDecimal(nodeEndProbabilityBox.getText());
                nodeStepSize = new BigDecimal(nodeProbabilityStepSizeBox.getText());
            } catch (Exception ignored) {
            }

            //Nur wenn alle Variablen für die Serienberechnung gegeben sind, Serienberechnung zulassen
            if (edgeEndValue.compareTo(BigDecimal.ZERO) == 1 && edgeStepSize.compareTo(BigDecimal.ZERO) == 1
                    && nodeStepSize.compareTo(BigDecimal.ZERO) == 1 && nodeEndValue.compareTo(BigDecimal.ZERO) == 1) {

                params.setSeriesParams(edgeStartValue, edgeEndValue, edgeStepSize, nodeStartValue, nodeEndValue, nodeStepSize);
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
            for (int i = 0; i < edgeCount; i++) {
                if (considerEdgeSingleReliabilities) {
                    edgeProbabilities[i] = new BigDecimal(edgeProbabilityBoxes.get(i).getText());
                } else {
                    edgeProbabilities[i] = BigDecimal.ONE;
                }
            }

            for (int i = 0; i < nodeCount; i++) {
                if (considerNodeSingleReliabilities) {
                    nodeProbabilities[i] = new BigDecimal(nodeProbabilityBoxes.get(i).getText());
                } else {
                    nodeProbabilities[i] = BigDecimal.ONE;
                }
            }

            params.setSingleReliabilityParams(edgeProbabilities, nodeProbabilities);
        }
        return params;
    }

    /**
     * Prüft, ob der Graph alle notwendigen Bedingungen erfüllt
     *
     * @return Ob der Graph zulässig ist
     */
    private boolean graphIsValid(Graph graph) {
        if (netPanel.drawnEdges.size() == 0) {
            //Dieser Block zeigt ein Hinweisfenster an, wenn keine Knoten vorhanden sind und bricht die Methode ab
            String str = "Your Network does not contain edges!";
            JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Anzahl Konnektionsknoten bestimmen
        int cNodeCount = 0;
        MyIterator np = netPanel.drawnNodes.iterator();
        while (np.hasNext()) {
            NodePoint node = (NodePoint) np.next();
            if (node.c_node)
                cNodeCount = cNodeCount + 1;
        }

        if (cNodeCount < 2) {
            //Der Code in diesem Block zeigt nur ein Hinweisfenster an und bricht die Funktion ab
            String str = "Your Network does not contain at least 2 c-vertices! \nYou can draw a new c-vertex by " +
                    "pressing the right mouse button. \nIf you want to transform an existing vertex into a c-vertex, " +
                    "\nplease hold the Ctrl-Key on your keyboard while left-clicking on the vertex.";

            JOptionPane.showMessageDialog(mainFrame, str, "Warning!", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Prüfen ob das Netz zusammenhängt
        if (!Con_check.isConnected(graph)) {
            JOptionPane.showMessageDialog(mainFrame, "Your Graph is not connected", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob der gegebene String eine (Gleitkomma)Zahl zwischen 0 und 1 ist
     *
     * @param str der zu überprüfende String
     * @return Boolean, ob der Text eine Wahrscheinlichkeit ist
     */
    private boolean textIsNotProbability(String str) {
        boolean isProbability = true;
        boolean temp = false;
        if (str.length() == 0)
            isProbability = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (i == 0 && c != '0') {
                if (c != '1') {
                    isProbability = false;
                    break;
                } else {
                    temp = true;
                    continue;
                }
            }
            if (i == 1) {
                if (c != '.') {
                    isProbability = false;
                    break;
                }
                continue;
            }
            if (!Character.isDigit(c)) {
                isProbability = false;
                break;
            } else {
                if (temp && c != '0') {
                    isProbability = false;
                    break;
                }
            }
        }
        return !isProbability;
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
            if (np.c_node)
                node.c_node = true;
            nodeList.add(node);
            cnt++;
        }
        //fertig mit dem Eintragen von Knoten
        it = netPanel.drawnEdges.iterator();
        cnt = 0;
        while (it.hasNext()) {
            EdgeLine e = (EdgeLine) it.next();
            int m = netPanel.drawnNodes.indexOf(e.startNode);
            int n = netPanel.drawnNodes.indexOf(e.endNode);
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
        setGUIState(GUI_STATES.CALCULATION_RUNNING);

        System.out.println("startCalculation: " + Thread.currentThread().getName());
        //Starte die Berechnung
        calculator.start();
    }
}
