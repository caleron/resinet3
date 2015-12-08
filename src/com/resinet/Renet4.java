package com.resinet;/* Renet4.java */

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


public class Renet4 extends JFrame
        implements ActionListener {
    Panel headerPanel, reliabilityPanelHeader, probabilitiesBtnPanel, reliabilityPanel, outputTextPanel;
    static Panel output;
    FlagPanel flagPanel;
    public NetPanel netPanel;
    ProbPanel probPanel;
    Label label0, headerLabel, label2;
    TextArea text;
    TextField pf, endProbabilityTextField, textfieldStepsize;
    Button drawBtn, resetGraphBtn, differentReliabilitiesOkBtn, sameReliabilityOkBtn, resetProbabilitiesBtn, probabilitiesOkBtn, calcReliabilityBtn, resilienceBtn, viewFactorTreeBtn, inputNetBtn, exportNetBtn;
    boolean sameReliability, inputNetBoolean;
    Choice ch;
    char lang = 'E';
    String s1, s2;

    public MyList drawnNodes;
    public MyList drawnEdges;
    public EdgeLine el;
    public boolean valid = false;
    boolean probability_mode = false;
    MyMouseListener drawMouseListener;
    MyMouseMotionListener drawMouseMoveListener;
    TextField[] edgeProbabilityTextFields;
    TextField[] nodeProbabilityTextFields;
    float[] edgeProbabilities;
    float[] nodeProbabilities;
    float prob;
    float probfact;
    String resultText;
    TextArea result;
    MyList nd;
    public static String reduceText;
    public static String factProb;
    public static int counterFact;

    Graph graphfact;
    Graph graph;
    public static MyList generated_Graphs;
    public float graph_width;
    public float graph_height;
    int smallest_x_pos;
    int highest_x_pos;
    int smallest_y_pos;
    int highest_y_pos;
    int cntedge;

    Zerleg zer;

    String enText, deText, resultTextEn;

    Color backgroundColor = new Color(85, 143, 180);

    ScrollPane probScrollPane;
    Image logo;
    Checkbox reliabilityCompareCheckBox;


    //Fuers Logo
    public void paint(Graphics g) {
        g.drawImage(logo, 270, 30, this);
    }

    public Renet4() {
        init();
    }

    public static void main(String[] args) {
        Renet4 r = new Renet4();
        r.pack();
        r.setSize(700, 825);
        r.setVisible(true);
    }

    public void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawnNodes = new MyList();
        drawnEdges = new MyList();

        startValue = BigDecimal.ZERO;
        endValue = BigDecimal.ZERO;
        stepSize = BigDecimal.ZERO;
        calculationSeriesMode = 0;
        onlyReliabilityFast = false;

        //color = color.brighter();
        setBackground(backgroundColor);
        GridBagLayout mainLayout = new GridBagLayout();
        setLayout(mainLayout);

        logo = getToolkit().getImage(getClass().getResource("img/logo.jpg"));
        prepareImage(logo, this);

        Panel halt0 = new Panel();
        GridBagConstraints halt0Gbc = makegbc(0, 0, 1, 1, "west");
        add(halt0, halt0Gbc);

        flagPanel = new FlagPanel();
        GridBagConstraints flagPanelGbc = makegbc(0, 0, 1, 5, "center");
        //GridBagConstraints flagPanelGbc = makegbc(0, 0, 1, 4, "center");
        flagPanelGbc.fill = GridBagConstraints.BOTH;
        //add(flagPanel, flagPanelGbc);

        //Sprachenauswahl nicht sichtbar!
        //flagPanel.setVisible(false);

        initNetPanel();

        initHeaderPanel();

        /*Panel halt1 = new Panel();
        GridBagConstraints halt1Gbc = makegbc(0, 7, 1, 1, "west");
        add(halt1, halt1Gbc);*/
        initReliabilityPanelHeader();
        initProbabilitiesPanel();

        /*Panel halt2 = new Panel();
        GridBagConstraints halt2Gbc = makegbc(0, 14, 1, 1, "west");
        add(halt2, halt2Gbc);*/

        initReliabilityPanel();
        initResiliencePanel();

        //reliabilityPanel.add(reliabilityCompareCheckBox);

        //reliabilityPanel.add(calcReliabilityBtn, decomBGbc);
        //reliabilityPanel.add(resilienceBtn, resilienceBGbc);

        // Fuer ResiNeT2 ist der Button nicht sichtbar
        viewFactorTreeBtn = new Button("View Tree of Factorisation");
        viewFactorTreeBtn.setEnabled(false);
        viewFactorTreeBtn.addActionListener(this);
        viewFactorTreeBtn.setVisible(false);

        //GridBagConstraints viewBGbc = makegbc(2, 0, 1, 1, "northwest");
        //reliabilityPanel.add(viewFactorTreeBtn, viewBGbc);
        initOutputTextPanel();
    }

    private void initOutputTextPanel() {
        outputTextPanel = new Panel();
        //GridBagConstraints panel7Gbc = makegbc(0, 16, 1, 4, "west" );
        GridBagConstraints panel7Gbc = makegbc(0, 17, 1, 4, "west");
        add(outputTextPanel, panel7Gbc);

        //result = new TextArea(" ", 10, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
        //result = new TextArea(" ", 3, 85, TextArea.SCROLLBARS_NONE);
        result = new TextArea(" ", 3, 85, TextArea.SCROLLBARS_VERTICAL_ONLY);
        result.setEditable(false);
        outputTextPanel.add(result);
        outputTextPanel.setBackground(backgroundColor);
    }

    private void initReliabilityPanelHeader() {
        //Panel unter dem Netzwerkgraphen
        reliabilityPanelHeader = new Panel();
        reliabilityPanelHeader.setLayout(new GridLayout(1, 1));
        GridBagConstraints panel3Gbc = makegbc(0, 7, 1, 1, "west");
        panel3Gbc.fill = GridBagConstraints.HORIZONTAL;
        add(reliabilityPanelHeader, panel3Gbc);
        reliabilityPanelHeader.setBackground(backgroundColor);
        label2 = new Label("   Now you can input the reliability of every edge:", Label.LEFT);
        reliabilityPanelHeader.add(label2);
    }

    private void initResiliencePanel() {
        Panel resiliencePanel = new Panel();
        GridBagConstraints resiliencePanelGbc = makegbc(0, 16, 1, 1, "west");

        resilienceBtn = new Button("Calculate the resilience of the network");
        resilienceBtn.setEnabled(false);
        resilienceBtn.addActionListener(this);

        resiliencePanel.add(resilienceBtn);
        resiliencePanel.setBackground(backgroundColor);
        add(resiliencePanel, resiliencePanelGbc);
    }

    private void initHeaderPanel() {
        headerPanel = new Panel();
        headerPanel.setBackground(backgroundColor);
        GridBagConstraints panel1Gbc = makegbc(0, 5, 1, 1, "west");
        add(headerPanel, panel1Gbc);

        GridBagLayout headerPanelLayout = new GridBagLayout();
        headerPanel.setLayout(headerPanelLayout);

        headerLabel = new Label("Please input your network model:");
        GridBagConstraints label1Gbc = makegbc(0, 0, 4, 1, "west");
        headerPanel.add(headerLabel, label1Gbc);

        drawBtn = new Button("Draw");
        drawBtn.addActionListener(this);
        GridBagConstraints drawBGbc = makegbc(0, 1, 1, 1, "west");
        headerPanel.add(drawBtn, drawBGbc);

        differentReliabilitiesOkBtn = new Button("Ok (components with different reliabilities)");
        differentReliabilitiesOkBtn.setEnabled(false);
        differentReliabilitiesOkBtn.addActionListener(this);
        GridBagConstraints ok1BGbc = makegbc(1, 1, 1, 1, "west");
        headerPanel.add(differentReliabilitiesOkBtn, ok1BGbc);

        sameReliabilityOkBtn = new Button("Ok (components with same reliability)");
        sameReliabilityOkBtn.setEnabled(false);
        sameReliabilityOkBtn.addActionListener(this);
        GridBagConstraints sameReliabilityBGbc = makegbc(2, 1, 1, 1, "west");
        headerPanel.add(sameReliabilityOkBtn, sameReliabilityBGbc);
        sameReliability = false;

        resetGraphBtn = new Button("Reset");
        resetGraphBtn.setEnabled(false);
        resetGraphBtn.addActionListener(this);
        GridBagConstraints reset1BGbc = makegbc(3, 1, 1, 1, "west");
        headerPanel.add(resetGraphBtn, reset1BGbc);

        //Button Input Network
        inputNetBtn = new Button("Load");
        inputNetBtn.setEnabled(true);
        inputNetBtn.addActionListener(this);
        GridBagConstraints inputNetGbc = makegbc(4, 1, 1, 1, "west");
        headerPanel.add(inputNetBtn, inputNetGbc);
        inputNetBoolean = false;

        //Button Output Network
        exportNetBtn = new Button("Save");
        exportNetBtn.setEnabled(true);
        exportNetBtn.addActionListener(this);
        GridBagConstraints exportNetGbc = makegbc(5, 1, 1, 1, "west");
        headerPanel.add(exportNetBtn, exportNetGbc);
    }

    private void initProbabilitiesPanel() {

        //Option 1: JScrollPane, funktioniert nicht
/*	JScrollPane probScrollPane = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
	probScrollPane.setPreferredSize(new java.awt.Dimension(600, 200));
	probPanel = new ProbPanel();
	probScrollPane.setViewportView( probPanel );
	GridBagConstraints spGbc = makegbc(0, 9, 1, 4, "west" );*/


        //Option 2: ScrollPane
        probScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        probPanel = new ProbPanel(this);
        //probScrollPane.setSize(625, 200);
        probScrollPane.setSize(625, 140);
        //probPanel.setSize(600, 400);
        probScrollPane.add(probPanel);
        GridBagConstraints spGbc = makegbc(0, 9, 1, 4, "west");
        add(probScrollPane, spGbc);

        probabilitiesBtnPanel = new Panel();
        GridBagConstraints panel5Gbc = makegbc(0, 13, 1, 1, "west");
        probabilitiesBtnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel5Gbc.fill = GridBagConstraints.HORIZONTAL;
        add(probabilitiesBtnPanel, panel5Gbc);
        resetProbabilitiesBtn = new Button("Reset");
        resetProbabilitiesBtn.setEnabled(false);
        resetProbabilitiesBtn.addActionListener(this);
        probabilitiesOkBtn = new Button("Ok");
        probabilitiesOkBtn.setEnabled(false);
        probabilitiesOkBtn.addActionListener(this);
        probabilitiesBtnPanel.add(resetProbabilitiesBtn);
        probabilitiesBtnPanel.add(probabilitiesOkBtn);

        probabilitiesBtnPanel.setBackground(backgroundColor);
    }

    private void initNetPanel() {
        netPanel = new NetPanel(this);
        drawMouseListener = new MyMouseListener();
        drawMouseMoveListener = new MyMouseMotionListener();
        netPanel.setBackground(Color.white);
        netPanel.setSize(625, 315);

        //netPanel.setSize(600, 200);
        netPanel.setVisible(true);
        GridBagConstraints panel2Gbc = makegbc(0, 6, 1, 1, "west");
        add(netPanel, panel2Gbc);
        reduceText = "";
        enText = "On this panel you can draw your network model after a click on the \"Draw\" button.\nPress the left button to draw a node and the right button to draw a connection-node. Delete a node by holding the <shift>-key an pressing the left button.\nTo draw an edge press the left button when the mouse pointer is on a node and hold it. Then drag the mouse to another node and release it. For deleting an edge delete its corresponding drawnNodes.\nAfter you have finished, click the \"Ok\" button.\n\nYou can also import a previously created network (ResiNeT or Pajek) by clicking the \"Load\" button. To turn an existing node into a connection-node hold the Ctrl-Key while left-clicking on the node.";
        deText = "Hier koennen Sie Ihr Netz eingeben. Klicken Sie dazu zunaechst auf \"Zeichnen\".\nEinen \"normalen\" Knoten erzeugen Sie, indem Sie die linke Maustaste betaetigen, einen K-Knoten durch Betaetigen der rechten Maustaste. Loeschen Sie einen Knoten, indem Sie die <shift>-Taste halten und die linke Maustaste betaetigen.\nUm eine Kante zu zeichnen, klicken Sie mit der linken Maustaste auf einen Knoten, halten diese solange gedrueckt, bis sich der Mauszeiger ueber dem Knoten befindet, zu dem die Kante fuehren soll. Eine Kante kann durch das Löschen ihrer inzidenten Knoten geloescht werden.\nHaben Sie Ihr Netz komplett eingegeben, klicken Sie bitte auf \"Ok\".\n";
        text = new TextArea(enText, 19, 85, TextArea.SCROLLBARS_NONE);
        text.setBackground(Color.white);
        text.setEditable(false);
        netPanel.add(text);
    }

    private void initReliabilityPanel() {
        reliabilityPanel = new Panel();
        GridBagConstraints panel6Gbc = makegbc(0, 15, 1, 1, "west");
        reliabilityPanel.setLayout(new GridBagLayout());
        add(reliabilityPanel, panel6Gbc);

        /*Panel halt3 = new Panel();
        GridBagConstraints halt3Gbc = makegbc(0, 1, 1, 3, "west");
        reliabilityPanel.add(halt3, halt3Gbc);*/

        calcReliabilityBtn = new Button("Calculate the reliability of the network");
        calcReliabilityBtn.setEnabled(false);
        calcReliabilityBtn.addActionListener(this);

        //GridBagConstraints resilienceBGbc = makegbc(0, 4, 1, 1, "southwest");
        GridBagConstraints resilienceBGbc = makegbc(2, 0, 1, 1, "west");
        GridBagConstraints decomBGbc = makegbc(1, 0, 1, 1, "west");

        Panel reliabilityButtons = new Panel();
        reliabilityButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        reliabilityButtons.add(calcReliabilityBtn);

        reliabilityCompareCheckBox = new Checkbox("Compare 3 algorithms");
        reliabilityButtons.add(reliabilityCompareCheckBox);
        //reliabilityButtons.add(resilienceBtn);
        reliabilityPanel.add(reliabilityButtons);

        reliabilityPanel.setBackground(backgroundColor);
    }

    private GridBagConstraints makegbc(int x, int y, int width, int height, String anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        if (anchor == "center")
            gbc.anchor = GridBagConstraints.CENTER;

        if (anchor == "north")
            gbc.anchor = GridBagConstraints.NORTH;

        if (anchor == "northeast")
            gbc.anchor = GridBagConstraints.NORTHEAST;

        if (anchor == "east")
            gbc.anchor = GridBagConstraints.EAST;

        if (anchor == "southeast")
            gbc.anchor = GridBagConstraints.SOUTHEAST;

        if (anchor == "south")
            gbc.anchor = GridBagConstraints.SOUTH;

        if (anchor == "southwest")
            gbc.anchor = GridBagConstraints.SOUTHWEST;

        if (anchor == "west")
            gbc.anchor = GridBagConstraints.WEST;

        if (anchor == "northwest")
            gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(1, 1, 1, 1);
        return gbc;
    }

    //wird nicht verwendet
    public void itemStateChanged(ItemEvent evt) {
        Choice ch = (Choice) evt.getItemSelectable();
        int i = ch.getSelectedIndex();
        switch (i) {
            case 0: {
                lang = 'E';
                result.setText(" ");
                netPanel.add(text);
                probPanel.removeAll();
                drawBtn.setEnabled(true);

                //flagPanel

                flagPanel.s = "../img/uk.gif";
                label0.setText("Select your language");
                flagPanel.invalidate();


                //headerPanel
                headerLabel.setText("Please input your network model:");
                drawBtn.setLabel("Draw");
                differentReliabilitiesOkBtn.setLabel("Ok (edges have different reliabilities)");
                sameReliabilityOkBtn.setLabel("Ok (all edges have the same reliability)");
                resetGraphBtn.setLabel("Reset");
                headerPanel.invalidate();

                //netPanel
                text.setText(enText);

                //reliabilityPanelHeader
                label2.setText("   Now you can input the reliability of every edge:");

                //probabilitiesBtnPanel
                resetProbabilitiesBtn.setLabel("Reset");
                probabilitiesBtnPanel.invalidate();

                //reliabilityPanel
                //reduceB.setLabel("Reduce this network");
                viewFactorTreeBtn.setLabel("View Tree of Factorisation");
                calcReliabilityBtn.setLabel("Calculate the reliability of the network");
                resilienceBtn.setLabel(" resilience of the network");
                reliabilityPanel.invalidate();

                validate();
                break;
            }
            case 1: {
                lang = 'D';
                result.setText(" ");
                netPanel.add(text);
                probPanel.removeAll();
                drawBtn.setEnabled(true);

                //flagPanel
                flagPanel.s = "../img/de.gif";
                label0.setText("Bitte waehlen Sie Ihre Sprache aus");
                flagPanel.invalidate();

                //headerPanel
                headerLabel.setText(" Bitte geben Sie hier Ihr Netz ein:");
                drawBtn.setLabel("Zeichnen");
                differentReliabilitiesOkBtn.setLabel("Ok (Verschiedene Kantenzuverlässigkeiten)");
                sameReliabilityOkBtn.setLabel("Ok (Einheitliche Kantenzuverlässigkeit)");
                resetGraphBtn.setLabel("Zurücksetzen");
                headerPanel.invalidate();

                //netPanel
                text.setText(deText);

                //reliabilityPanelHeader
                label2.setText("   Nun geben Sie bitte die Intaktwahrscheinlichkeit jeder Kante ein:");

                //probabilitiesBtnPanel
                resetProbabilitiesBtn.setLabel("Zurücksetzen");
                probabilitiesBtnPanel.invalidate();

                //reliabilityPanel
                //reduceB.setLabel("Das Netz reduzieren");
                //viewFactorTreeBtn.setLabel("Anzeigen");
                calcReliabilityBtn.setLabel("Die Zuverlaessigkeit des Netzes berechnen");
                resilienceBtn.setLabel("Die Resilienz des Netzes berechnen");
                viewFactorTreeBtn.setLabel("Faktorisierungsbaum anzeigen");
                reliabilityPanel.invalidate();

                validate();
                break;
            }
            default:
                flagPanel.s = "img/logo.jpg";
        }

    }

    public void actionPerformed(ActionEvent evt) {
        Button button = (Button) evt.getSource();

        if (button == drawBtn) {
            netPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            netPanel.addMouseListener(drawMouseListener);
            netPanel.addMouseMotionListener(drawMouseMoveListener);
            differentReliabilitiesOkBtn.setEnabled(true);
            sameReliabilityOkBtn.setEnabled(true);
            drawBtn.setEnabled(false);
            resetGraphBtn.setEnabled(true);
            if (netPanel.getComponentCount() != 0)
                netPanel.remove(text);
        }

        if (button == sameReliabilityOkBtn) {
            probPanel.removeAll();
            sameReliability = true;
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
            valid = false;
            probability_mode = false;
            probPanel.removeAll();
            probPanel.repaint();
            drawBtn.setEnabled(false);
            probabilitiesOkBtn.setEnabled(false);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);
            viewFactorTreeBtn.setEnabled(false);
            //result.setText(" ");
            //reduceText = "";
            sameReliability = false;
            sameReliabilityOkBtn.setEnabled(true);

            //probPanel.removeAll();
            inputNetBoolean = true;
            //resetGraphBtn.setEnabled(true);

            inputNet();

            //differentReliabilitiesOkBtn.setEnabled(true);
            //sameReliabilityOkBtn.setEnabled(true);
            //calcReliabilityBtn.setEnabled(false);
            //resilienceBtn.setEnabled(false);
            netPanel.remove(text);
            netPanel.repaint();

        }

        if (button == exportNetBtn) {
            exportNet();
        }

        if (button == differentReliabilitiesOkBtn) {
            probPanel.removeAll();
            sameReliability = false;
            resetGraphBtn.setEnabled(true);
            sameReliabilityOkBtn.setEnabled(true);
            checkGraphAndBuildProbPanel();
            differentReliabilitiesOkBtn.setEnabled(true);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);

        }

        if (button == resetGraphBtn) {
            startValue = BigDecimal.ZERO;
            endValue = BigDecimal.ZERO;
            stepSize = BigDecimal.ZERO;

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
            valid = false;
            probability_mode = false;
            netPanel.repaint();
            probPanel.removeAll();
            probPanel.repaint();
            drawBtn.setEnabled(false);
            probabilitiesOkBtn.setEnabled(false);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);
            viewFactorTreeBtn.setEnabled(false);
            result.setText(" ");
            reduceText = "";
            sameReliability = false;
            sameReliabilityOkBtn.setEnabled(true);
        }

        if (button == probabilitiesOkBtn) {
            int edgeCount = edgeProbabilityTextFields.length;
            int nodeCount = nodeProbabilityTextFields.length;
            edgeProbabilities = new float[edgeCount];
            nodeProbabilities = new float[nodeCount];

            MyList edgesWithMissingProbability = new MyList();
            MyList nodesWithMissingProbability = new MyList();

            for (int i = 0; i < edgeCount; i++) {
                String s = edgeProbabilityTextFields[i].getText();
                if (!textIsProbability(s))
                    edgesWithMissingProbability.add(String.valueOf(i));
            }

            for (int i = 0; i < nodeCount; i++) {
                String s = nodeProbabilityTextFields[i].getText();
                if (!textIsProbability(s))
                    nodesWithMissingProbability.add(String.valueOf(i));
            }

            if (sameReliability) {
                String sEnd = endProbabilityTextField.getText();
                if (sEnd.length() != 0) {
                    for (int i = 0; i < edgeCount; i++) {
                        if (!textIsProbability(sEnd))
                            edgesWithMissingProbability.add(String.valueOf(i));
                    }
                }
            }

            if (edgesWithMissingProbability.size() != 0 || nodesWithMissingProbability.size() != 0) {
                //Dieser Block zeigt nur ein Hinweisfenster an, falls Wahrscheinlichkeiten fehlen
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
                        "which is\nless than or equal to 1. Please check the in-\nput for edge\n" + missingProbabilityEdges +
                        "\n and for node\n" + missingProbabilityNodes;

                if (lang == 'D')
                    str = "Die Intaktwahrscheinlichkeit einer Kante muss eine Zahl kleiner oder gleich 1 im Format x.xxxxxx sein. Bitte ueberpruefen Sie die Eingabe bei Kante:\n" + missingProbabilityEdges;

                //Toolkit.getDefaultToolkit().beep();
                Frame frame = new Frame("Warning!");
                frame.setLayout(new BorderLayout());
                frame.addWindowListener(
                        new WindowAdapter() {
                            public void windowClosing(WindowEvent event) {
                                Frame f = (Frame) event.getSource();
                                f.setVisible(false);
                                f.dispose();
                            }
                        }
                );
                TextArea ta = new TextArea(str, 50, 40, TextArea.SCROLLBARS_NONE);
                frame.add("Center", ta);
                Panel buttonPanel = new Panel();
                buttonPanel.setLayout(new GridBagLayout());
                Button bn = new Button("Ok");
                bn.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent event) {
                                             Button b = (Button) event.getSource();
                                             b.getParent().setVisible(false);
                                             ((Frame) (b.getParent()).getParent()).dispose();
                                         }
                                     }
                );
                GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
                buttonPanel.add(bn, bnGbc);
                frame.add("South", buttonPanel);
                Point location = probPanel.getLocationOnScreen();
                frame.setLocation(location);
                frame.setVisible(true);
                frame.setSize(370, 200);
                return;
            }

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

            //TODO knotenwahrscheinlichkeiten zuweisen
        /*Wahrscheinlichkeiten neu zuordnen.*/
            reassignProbabilities();


            //End value und step size zuordnen
            //TODO dies kann eigentlich in den Block mit sameReliability
            if (edgeProbabilityTextFields[0].getText().length() != 0) {
                startValue = new BigDecimal(edgeProbabilityTextFields[0].getText());
                System.out.println("StartValue: " + startValue);
            }

            if (sameReliability) {
                if (endProbabilityTextField.getText().length() != 0) {
                    endValue = new BigDecimal(endProbabilityTextField.getText());
                    System.out.println("EndValue: " + endValue);
                }

                if (textfieldStepsize.getText().length() != 0) {
                    stepSize = new BigDecimal(textfieldStepsize.getText());
                    System.out.println("StepSize: " + stepSize);
                }
            }


        }


        if (button == resetProbabilitiesBtn) {
            startValue = BigDecimal.ZERO;
            endValue = BigDecimal.ZERO;
            stepSize = BigDecimal.ZERO;

            probabilitiesOkBtn.setEnabled(true);
            differentReliabilitiesOkBtn.setEnabled(true);
            resetProbabilitiesBtn.setEnabled(false);
            calcReliabilityBtn.setEnabled(false);
            resilienceBtn.setEnabled(false);
            edgeProbabilities = null;
            for (int i = 0; i < edgeProbabilityTextFields.length; i++) {
                edgeProbabilityTextFields[i].setText(null);
                edgeProbabilityTextFields[i].setEditable(true);
            }

            for (int i = 0; i < nodeProbabilityTextFields.length; i++) {
                nodeProbabilityTextFields[i].setText(null);
                nodeProbabilityTextFields[i].setEditable(true);
            }

            if (endProbabilityTextField != null) {
                endProbabilityTextField.setText(null);
            }
            if (textfieldStepsize != null) {
                textfieldStepsize.setText(null);
            }

            sameReliabilityOkBtn.setEnabled(true);

        }

        if (button == viewFactorTreeBtn) {
            Frame frame = new Frame("Tree of Factorisation");
            frame.setSize(800, 600);
            frame.addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(WindowEvent event) {
                            Frame f = (Frame) event.getSource();
                            f.setVisible(false);
                            f.dispose();
                        }
                    }
            );
            Panel output = new Panel();
            output.setBackground(Color.white);
            output.setLayout(null);
            ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
            sp.add(output);
            Dimension prefSize = new Dimension(600, 400);
            sp.setSize(prefSize);
            float tree_width = 0;
            float tree_depth = 0;

            MyIterator ite = generated_Graphs.iterator();
            while (ite.hasNext()) {
                Graph g = (Graph) ite.next();
                if (g.level > tree_depth)
                    tree_depth = g.level;
            }
            tree_width = graphfact.left_offset + graphfact.right_offset;
            output.setSize(new Dimension(Math.round((tree_width + 1) * (graph_width + 20)), Math.round((tree_depth + 1) * (graph_height + 100))));
            drawTree(graphfact, graphfact.left_offset * (graph_width + 20), output);

		/* Calculate the screen size */
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        /* Center frame on the screen */
            Dimension thisFrameSize = frame.getSize();
            if (thisFrameSize.height > screenSize.height)
                thisFrameSize.height = screenSize.height;
            if (thisFrameSize.width > screenSize.width)
                thisFrameSize.width = screenSize.width;
            frame.setLocation((screenSize.width - thisFrameSize.width) / 2, (screenSize.height - thisFrameSize.height) / 2);
            frame.add(sp);
            frame.pack();
            frame.setVisible(true);
        }


        if (button == resilienceBtn) {
            if (!endValue.equals(BigDecimal.ZERO) && !stepSize.equals(BigDecimal.ZERO) && sameReliability) {
                calculationSeriesMode = 1;
                calculationSeries();
            } else {
                calculationSeriesMode = 0;
                calculate_resilience();

                resultText = "The network has " + total_nodes + " Nodes, containing " + c_nodes + " c-Nodes.\n" + "There are " + combinations + " combinations.\n" + "The resilience of the network is: " + result_resilience;
                //resultText = "Das Netz hat " + total_nodes + " Knoten, davon " + c_nodes + " K-Knoten.\n" + "Es gibt also " + combinations + " Kombinationen.\n" + "Die Resilienz des Netzes ist: " + result_resilience;		
                result.setText(resultText);
            }
            viewFactorTreeBtn.setEnabled(true);
        }


        if (button == calcReliabilityBtn) {
            //heidtmanns_reliability();
            //fact_reliability();

            if (!endValue.equals(BigDecimal.ZERO) && !stepSize.equals(BigDecimal.ZERO) && sameReliability) {
                calculationSeriesMode = 2;
                calculationSeries();
            } else {
                calculationSeriesMode = 0;
                //Wenn die Checkbox angeklickt wurde, sollen die 3 Alg. verglichen werden. Sonst nicht.
                if (reliabilityCompareCheckBox.getState()) {
                    calculate_reliability_3_Algorithms();
                } else {
                    onlyReliabilityFast = true;
                    calculate_reliability_faster();
                    onlyReliabilityFast = false;
                }
            }

        }
    }

    /**
     * Weist alle Wahrscheinlichkeiten aus den Eingabefeldern den Elementen im Graphen neu zu
     */
    private void reassignProbabilities() {
        reassignProbabilites(false);
    }

    /**
     * Weist alle Wahrscheinlichkeiten aus den Eingabefeldern den Elementen im Graphen neu zu Berücksichtigt auf Wunsch
     * nur den "normalen" Graphen
     *
     * @param onlyNormalGraph bei true wird der Faktorisierungsgraph ignoriert
     */
    private void reassignProbabilites(boolean onlyNormalGraph) {
        /*
        Diese Listen sind Klone der Kantenlisten des Graphen, aber halten anscheinend die selben Referenzen auf Kanten
        wie die Kantenliste des Graphen, also eine flache Kopie.
        Wozu das ganze? Keine Ahnung.
        */
        MyList edgeList = graph.getEdgelist();
        MyList factEdgeList = graphfact.getEdgelist();
        //Kantenwahrscheinlichkeiten
        for (int i = 0; i < edgeProbabilities.length; i++) {
            Edge e = (Edge) edgeList.get(i);
            e.prob = edgeProbabilities[i];

            if (!onlyNormalGraph) {
                Edge e2 = (Edge) factEdgeList.get(i);
                e2.prob = edgeProbabilities[i];
            }
        }

        MyList nodeList = graph.getNodelist();
        MyList factNodeList = graphfact.getNodelist();

        //Knotenwahrscheinlichkeiten
        for (int i = 0; i < nodeProbabilities.length; i++) {
            Node e = (Node) nodeList.get(i);
            e.prob = nodeProbabilities[i];

            if (!onlyNormalGraph) {
                Node e2 = (Node) factNodeList.get(i);
                e2.prob = nodeProbabilities[i];
            }
        }
    }

    private float getP(MySet hs) {
        float p = 1;
        String output = "Pfad";

        MyIterator it = hs.iterator();
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
    private boolean textIsProbability(String str) {
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
        return b;
    }

    /**
     * Prüft den Graphen und baut die Anzeige mit den Textfeldern für die Wahrscheinlichkeiten auf
     */
    public void checkGraphAndBuildProbPanel() {
        if (drawnEdges.size() == 0) {
            //Dieser Block zeigt ein Hinweisfenster an, wenn keine Knoten vorhanden sind und bricht die Methode ab
            String str = "Your Network does not contain edges!";
            if (lang == 'D')
                str = "Ihr Netz besitzt keine Kanten!";

            Toolkit.getDefaultToolkit().beep();
            Frame frame = new Frame("Warning!");
            frame.setLayout(new BorderLayout());
            frame.addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(WindowEvent event) {
                            Frame f = (Frame) event.getSource();
                            f.setVisible(false);
                            f.dispose();
                        }
                    }
            );

            TextArea ta = new TextArea(str, 50, 40, TextArea.SCROLLBARS_NONE);
            frame.add("Center", ta);
            Panel buttonPanel = new Panel();
            buttonPanel.setLayout(new GridBagLayout());
            Button bn = new Button("Ok");
            bn.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent event) {
                                         Button b = (Button) event.getSource();
                                         b.getParent().setVisible(false);
                                         ((Frame) (b.getParent()).getParent()).dispose();
                                     }
                                 }
            );
            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
            buttonPanel.add(bn, bnGbc);
            frame.add("South", buttonPanel);
            Point location = probPanel.getLocationOnScreen();
            frame.setLocation(location);
            frame.setVisible(true);
            frame.setSize(370, 200);
            return;
        }

        //Anzahl Konnektionsknoten bestimmen
        int count = 0;
        MyIterator np = drawnNodes.iterator();
        while (np.hasNext()) {
            NodePoint n = (NodePoint) np.next();
            if (n.k == true)
                count = count + 1;
        }

        if (count < 2) {
            //Der Code in diesem Block zeigt nur ein Hinweisfenster an und bricht die Funktion ab
            String str = "Your Network does not contain at least 2 c-drawnNodes! You can draw a new c-node by pressing the right mouse button. If you want to transform an existing node into a c-node, please hold the Ctrl-Key on your keyboard while left-clicking on the node.";
            if (lang == 'D')
                str = "Ihr Netz besitzt nicht mindestens 2 Konnektionsknoten!";
            Frame frame = new Frame("Warning!");
            frame.setLayout(new BorderLayout());
            frame.addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(WindowEvent event) {
                            Frame f = (Frame) event.getSource();
                            f.setVisible(false);
                            f.dispose();
                        }
                    }
            );

            TextArea ta = new TextArea(str, 50, 40, TextArea.SCROLLBARS_NONE);
            frame.add("Center", ta);
            Panel buttonPanel = new Panel();
            buttonPanel.setLayout(new GridBagLayout());
            Button bn = new Button("Ok");
            bn.addActionListener(new ActionListener() {
                                     public void actionPerformed(ActionEvent event) {
                                         Button b = (Button) event.getSource();
                                         b.getParent().setVisible(false);
                                         ((Frame) (b.getParent()).getParent()).dispose();
                                     }
                                 }
            );
            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
            buttonPanel.add(bn, bnGbc);
            frame.add("South", buttonPanel);
            Point location = probPanel.getLocationOnScreen();
            frame.setLocation(location);
            frame.setVisible(true);
            frame.setSize(370, 200);
            return;
        }

        //Genug Knoten bzw. Konnektionsknoten vorhanden, weiter gehts

		/*Ermittle kleinste und größte Positionswerte der Knoten.*/
        smallest_x_pos = 2000;
        highest_x_pos = 0;
        smallest_y_pos = 2000;
        highest_y_pos = 0;
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

		/*Clone Graphen für Faktorisierung.*/
        try {
            graphfact = (Graph) Util.serialClone(graph); //clone Graphen
        } catch (java.io.IOException e1) {
            System.err.println(e1.toString());
        } catch (java.lang.ClassNotFoundException e2) {
            System.err.println(e2.toString());
        }

		/*Passe Positionierung des Graphen an.*/
        MyList node_pos = graphfact.getNodelist();
        np = node_pos.iterator();
        while (np.hasNext()) {
            Node n = (Node) np.next();
            n.xposition = n.xposition - smallest_x_pos;
            n.yposition = n.yposition - smallest_y_pos;
        }

        netPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        differentReliabilitiesOkBtn.setEnabled(false);
        resetGraphBtn.setEnabled(true);
        probability_mode = true;

        int edgeCount = drawnEdges.size();
        int nodeCount = drawnNodes.size();
        //probPanel.setSize(probPanel.getPreferredSize());
        //System.out.println(probPanel.getSize());
        /*Da drawnEdges.size() sich geaendert hat, muss hier die
          Size von probPanel nochmal festgelegt werden, oder man fuegt erst hier
		  probPanel zur ScrollPane probScrollPane:
		  probPanel = new ProbPanel();
		  probScrollPane.add(probPanel);
		  Dann hat probPanel die richtige Groesse.
		*/

        if (sameReliability) {
            probScrollPane.setScrollPosition(0, 0);
            probPanel.setSize(600, 179);
            probPanel.removeAll();
            //probPanel.setLayout(new GridLayout(1, 1));
            probPanel.setLayout(new GridLayout(6, 2));

            edgeProbabilityTextFields = new TextField[edgeCount];
            nodeProbabilityTextFields = new TextField[nodeCount];
            String str = "Reliability of every edge: ";
            if (lang == 'D') {
                str = "Intaktwahrscheinlichkeit: ";
            }

            //Kantenwahrscheinlichkeiten
            Label edgeProbLabel = new Label(str, Label.RIGHT);
            TextField edgeProbtextField = new TextField(20);
            edgeProbtextField.setBackground(Color.white);
            for (int i = 0; i < drawnEdges.size(); i++) {
                edgeProbabilityTextFields[i] = edgeProbtextField;
            }
            Panel edgeProbPanel = new Panel();
            edgeProbPanel.add(edgeProbLabel);
            edgeProbPanel.add(edgeProbtextField);
            probPanel.add(edgeProbPanel);

            //Kantenwahrscheinlichkeiten
            Label nodeProbLabel = new Label("Reliability of every node:", Label.RIGHT);
            TextField nodeProbtextField = new TextField(20);
            nodeProbtextField.setBackground(Color.white);
            for (int i = 0; i < drawnNodes.size(); i++) {
                nodeProbabilityTextFields[i] = nodeProbtextField;
            }
            Panel nodeProbPanel = new Panel();
            nodeProbPanel.add(nodeProbLabel);
            nodeProbPanel.add(nodeProbtextField);
            probPanel.add(nodeProbPanel);

            String str1 = "Optional for calculation series: ";
            Label l1 = new Label(str1, Label.RIGHT);
            Panel p1 = new Panel();
            p1.add(l1);
            probPanel.add(p1);

            String str2 = "End value: ";
            Label l2 = new Label(str2, Label.RIGHT);
            endProbabilityTextField = new TextField(20);
            endProbabilityTextField.setBackground(Color.white);
            Panel p2 = new Panel();
            p2.add(l2);
            p2.add(endProbabilityTextField);

            String str3 = "Step size (e.g. 0.01): ";
            Label l3 = new Label(str3, Label.RIGHT);
            textfieldStepsize = new TextField(20);
            textfieldStepsize.setBackground(Color.white);

            p2.add(l3);
            p2.add(textfieldStepsize);
            probPanel.add(p2);
        } else {

            probPanel.setSize(probPanel.getPreferredSize());
            probPanel.removeAll();
            //probPanel.setLayout(new GridLayout((edgeCount + nodeCount) / 2 + 1, 2));
            probPanel.setLayout(new GridLayout(0, 2));

            edgeProbabilityTextFields = new TextField[edgeCount];
            nodeProbabilityTextFields = new TextField[nodeCount];

            for (int i = 0; i < drawnEdges.size(); i++) {
                addFieldToProbPanel(i, false);
            }

            for (int i = 0; i < drawnNodes.size(); i++) {
                addFieldToProbPanel(i, true);
            }

            probScrollPane.validate();
        }
        probPanel.validate();


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

        Label label = new Label(text, Label.RIGHT);
        TextField textField = new TextField(20);
        textField.setBackground(Color.white);

        if (isNodeProb) {
            nodeProbabilityTextFields[number] = textField;
        } else {
            edgeProbabilityTextFields[number] = textField;
        }
        Panel panel = new Panel();
        panel.add(label);
        panel.add(textField);
        probPanel.add(panel);
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
        Graph g = new Graph(nodeList, edgeList);
        return g;
    }

    private void drawTree(Graph g, float pos, Panel outp) {
        Graph child_left = null;
        Graph child_right = null;

        GraphPanel gp = new GraphPanel(this, g);
        int xpos = Math.round(pos);
        int ypos = Math.round(g.level * (graph_height + 100));
        int width = Math.round(graph_width);
        int height = Math.round(graph_height);
        gp.setBounds(xpos, ypos, width, height);
        outp.add(gp);

        MyIterator it = g.child_Graphs.iterator();
        while (it.hasNext()) {
            Graph child = (Graph) it.next();
            if (child.kind_of_reduction == 0)
                child_left = child;
            if (child.kind_of_reduction == 1)
                child_right = child;
        }

        if (child_left != null) {
            float x;
            x = pos - (g.offset / 2) * graph_width - 20;
            LinePanel lp = new LinePanel(Math.round(xpos - (x + width)) - 2, 100, 0, child_left.reduced_edge);
            lp.setBounds(Math.round(x + width + 1), ypos + height + 1, Math.round(xpos - (x + width)) - 2, 100);
            outp.add(lp);
            drawTree(child_left, x, outp);
        }

        if (child_right != null) {
            float x;
            x = pos + (g.offset / 2) * graph_width + 20;
            LinePanel lp = new LinePanel(Math.round(x - (xpos + width) - 2), 100, 1, child_right.reduced_edge);
            lp.setBounds(xpos + width + 1, ypos + height + 1, Math.round(x - (xpos + width) - 2), 100);
            outp.add(lp);
            drawTree(child_right, x, outp);
        }

        return;
    }

    class MyMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            if (probability_mode == false) {
                int x1 = evt.getX();
                int y1 = evt.getY();
                int cnt1;
                int edlnode1;
                int edlnode2;
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
                                edlnode1 = edl.node1; //Knotennummern sichern
                                edlnode2 = edl.node2;
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
                            if (nps.k == true) {
                                nps.k = false;
                            } else if (nps.k == false) {
                                nps.k = true;
                            }

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
                cntedge = 0;
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
                            String str = "Input reliability of Edge " + cntedge;
                            if (lang == 'D')
                                str = "Intaktwahrscheinlichkeit von Kante " + cntedge;
                            Frame frame = new Frame("ReNeT");
                            frame.setLayout(new BorderLayout());
                            frame.addWindowListener(
                                    new WindowAdapter() {
                                        public void windowClosing(WindowEvent event) {
                                            Frame f = (Frame) event.getSource();
                                            f.setVisible(false);
                                            f.dispose();
                                        }
                                    });

                            TextField tf = new TextField(str);
                            tf.setEditable(false);
                            pf = new TextField(10);
                            Panel buttonPanel = new Panel();
                            Panel bp = new Panel();
                            buttonPanel.setLayout(new GridBagLayout());
                            bp.setLayout(new GridBagLayout());
                            Button bn = new Button("Ok");
                            bn.addActionListener(new ActionListener() {
                                                     public void actionPerformed(ActionEvent event) {
                                                         Button b = (Button) event.getSource();
                                                         b.getParent().setVisible(false);
                                                         edgeProbabilityTextFields[cntedge].setText(pf.getText());
                                                         ((Frame) (b.getParent()).getParent()).dispose();
                                                     }
                                                 }
                            );
                            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
                            buttonPanel.add(bn, bnGbc);
                            bp.add(pf, bnGbc);
                            Point location = probPanel.getLocationOnScreen();
                            frame.setLocation(location);
                            frame.setVisible(true);
                            frame.setSize(270, 140);
                            frame.add("South", buttonPanel);
                            frame.add("North", tf);
                            frame.add("Center", bp);
                            break;
                        }
                    }

                    if (y1 == y2 && min_x1x2 <= x3 && x3 <= max_x1x2) {
                        dr = Math.abs(y3 - y1);
                        if (dr <= r) {
                            String str = "Input reliability of Edge " + cntedge;
                            if (lang == 'D')
                                str = "Intaktwahrscheinlichkeit von Kante " + cntedge;
                            Frame frame = new Frame("ReNeT");
                            frame.setLayout(new BorderLayout());
                            frame.addWindowListener(
                                    new WindowAdapter() {
                                        public void windowClosing(WindowEvent event) {
                                            Frame f = (Frame) event.getSource();
                                            f.setVisible(false);
                                            f.dispose();
                                        }
                                    });

                            TextField tf = new TextField(str);
                            tf.setEditable(false);
                            pf = new TextField(10);
                            Panel buttonPanel = new Panel();
                            Panel bp = new Panel();
                            buttonPanel.setLayout(new GridBagLayout());
                            bp.setLayout(new GridBagLayout());
                            Button bn = new Button("Ok");
                            bn.addActionListener(new ActionListener() {
                                                     public void actionPerformed(ActionEvent event) {
                                                         Button b = (Button) event.getSource();
                                                         b.getParent().setVisible(false);
                                                         edgeProbabilityTextFields[cntedge].setText(pf.getText());
                                                         ((Frame) (b.getParent()).getParent()).dispose();
                                                     }
                                                 }
                            );
                            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
                            buttonPanel.add(bn, bnGbc);
                            bp.add(pf, bnGbc);
                            Point location = probPanel.getLocationOnScreen();
                            frame.setLocation(location);
                            frame.setVisible(true);
                            frame.setSize(270, 140);
                            frame.add("South", buttonPanel);
                            frame.add("North", tf);
                            frame.add("Center", bp);
                            break;
                        }
                    }

                    if (x1 != x2 && y1 != y2 && min_x1x2 <= x3 && x3 <= max_x1x2 && min_y1y2 <= y3 && y3 <= max_y1y2) {
                        dr = Math.sqrt(Math.pow(x3 - x1 - ((x3 - x1 + (-x3 * diff_y2y1 + y3 * diff_x2x1 + x1 * diff_y2y1 - y1 * diff_x2x1) / (diff_y2y1 + Math.pow(diff_x2x1, 2) / diff_y2y1)) / diff_x2x1) * diff_x2x1, 2) + Math.pow(y3 - y1 - ((x3 - x1 + (-x3 * diff_y2y1 + y3 * diff_x2x1 + x1 * diff_y2y1 - y1 * diff_x2x1) / (diff_y2y1 + Math.pow(diff_x2x1, 2) / diff_y2y1)) / diff_x2x1) * diff_y2y1, 2));
                        if (dr <= r) {
                            String str = "Input reliability of Edge " + cntedge;
                            if (lang == 'D')
                                str = "Intaktwahrscheinlichkeit von Kante " + cntedge;
                            Frame frame = new Frame("ReNeT");
                            frame.setLayout(new BorderLayout());
                            frame.addWindowListener(
                                    new WindowAdapter() {
                                        public void windowClosing(WindowEvent event) {
                                            Frame f = (Frame) event.getSource();
                                            f.setVisible(false);
                                            f.dispose();
                                        }
                                    });

                            TextField tf = new TextField(str);
                            tf.setEditable(false);
                            pf = new TextField(10);
                            Panel buttonPanel = new Panel();
                            Panel bp = new Panel();
                            buttonPanel.setLayout(new GridBagLayout());
                            bp.setLayout(new GridBagLayout());
                            Button bn = new Button("Ok");
                            bn.addActionListener(new ActionListener() {
                                                     public void actionPerformed(ActionEvent event) {
                                                         Button b = (Button) event.getSource();
                                                         b.getParent().setVisible(false);
                                                         edgeProbabilityTextFields[cntedge].setText(pf.getText());
                                                         ((Frame) (b.getParent()).getParent()).dispose();
                                                     }
                                                 }
                            );
                            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
                            buttonPanel.add(bn, bnGbc);
                            bp.add(pf, bnGbc);
                            Point location = probPanel.getLocationOnScreen();
                            frame.setLocation(location);
                            frame.setVisible(true);
                            frame.setSize(270, 140);
                            frame.add("South", buttonPanel);
                            frame.add("North", tf);
                            frame.add("Center", bp);
                            break;
                        }
                    }
                    cntedge++;
                }
                netPanel.repaint();
            }
        }

        public void mousePressed(MouseEvent evt) {
            if (evt.isShiftDown() || probability_mode == true)
                return;
            valid = false;
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
                    el = new EdgeLine();
                    el.x1 = px;
                    el.y1 = py;
                    el.node1 = cnt;
                    valid = true;
                    break;
                }
                //punkt (x,y) ist in dem Kreis(px, py)
            }
        }

        public void mouseReleased(MouseEvent evt) {
            if (!valid || evt.isShiftDown() || probability_mode == true)
                return;

            valid = false;
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
                if ((dx * dx + dy * dy) <= 100 && el.node1 != drawnNodes.indexOf(np)) {
                    cnt = drawnNodes.indexOf(np);
                    el.x2 = px;
                    el.y2 = py;
                    el.node2 = cnt;
                    drawnEdges.add(el);
                    valid = true;

                    int lx = el.x2 - el.x1;
                    int ly = el.y2 - el.y1;
                    el.x0 = el.x2 - lx / 2;
                    el.y0 = el.y2 - ly / 2;

                    break;
                }
                //punkt (x,y) ist in dem Kreis(px, py)
            }
            netPanel.repaint();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent evt) {
            if (valid && probability_mode == false) {
                int x = evt.getX();
                int y = evt.getY();
                el.x2 = x;
                el.y2 = y;
                netPanel.repaint();
            }
        }
    }

    class MessageFrame extends Frame {
        String newline;
        TextArea msgArea;

        MessageFrame() {
            super("Message Window");
            setBounds(300, 100, 480, 200);
            newline = new String(System.getProperty("line.separator"));
            msgArea = new TextArea("Hier kommen die Text Ausaben:" + newline);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    //System.out.println("window Closing Event");
                    destroy();
                }
            });
        }

        public void destroy() {
            if (this.isVisible()) this.dispose();
        }

        public void msgOut(String s, boolean sysOut) //String ausgeben 
        {
            msgArea.append(s + newline);
        }


        public void msgAreaClear() //inhalt löschen 
        {
            msgArea.setText("");
        }
    }


    /**
     * Zuverlaessigkeitsberechnung mit nur einem Algorithmus Bei einem zusammenhängenden Graphen wird Heidtmanns
     * Algorithmus verwendet, sonst die Faktorisierungsmethode
     */
    public void calculate_reliability_faster() {
        resultText = "Calculating...";
        result.setText(resultText);

        //Prüfen ob das Netz zusammenhängt
        boolean graphConnected;
        if (Con_check.check(graph) == -1) {
            //com.resinet.model.Graph ist zusammenhängend
            graphConnected = true;
        } else {
            graphConnected = false;
        }

        // Wahrscheinlichkeiten neu zuordnen. 
        /*edgeList = (MyList)graph.getEdgelist().clone();
        br_fact = (MyList)graphfact.getEdgelist().clone();
		for(int k=0; k<edgeProbabilities.length;k++)
		{
		    Edge e = (Edge)edgeList.get(k);
		    e.prob = edgeProbabilities[k];
		    Edge e2 = (Edge)br_fact.get(k);
		    e2.prob = edgeProbabilities[k];
		}*/

        if (graphConnected) {
            heidtmanns_reliability();
            resultText = "The reliability of the network is: " + prob;
        } else {
            fact_reliability();
            resultText = "The reliability of the network is: " + probfact;
        }
        result.setText(resultText);
    }

    /**
     * Zuverlaessigkeitsberechnung mit 3 Algorithmen (ReNeT)
     */
    public void calculate_reliability_3_Algorithms() {
        counterFact = 0;
        graphfact.level = 0;
        generated_Graphs = new MyList();
        generated_Graphs.add(graphfact);
        factProb = "(";
        graphfact.child_Graphs = new MyList();

        probfact = Util.getProbabilityFact(graphfact, 0);

        Util.drawTreeofGraphs(graphfact);

        System.out.println("Die  Zuverlaessigkeit des Netzes ist: " + probfact);

        resultText = "Please use the scrollbar to scroll through the results. \n \n";

        resultText = resultText + "The reliability of the network is calculated using the method of factorisation (no reduction):\nNumber of factorisations: " + counterFact + "\nP=" + factProb + "\nThe reliability of the network is:\n" + probfact;
        if (lang == 'D')
            resultText = "Die Zuverlaessigkeit des Netzes wird mit der Methode der Faktorisierung berechnet (keine Reduktion):\nAnzahl Faktorisierungen: " + counterFact + "\nP=" + factProb + "\nDie Zuverlaessigkeit des Netzes ist:\n" + probfact;
        resultText = resultText + "\n\n-------------------------\n\n";
        calcReliabilityBtn.setEnabled(true);
        resilienceBtn.setEnabled(true);

        zer = new Zerleg(graph);
        zer.start();
        try {
            zer.join();
        } catch (InterruptedException e) {
        }

        reassignProbabilites(true);

        //die IW jeder Kante zuweisen
        Util.getProbability(graph);
        if (graph.edgeList.size() == 1) {
            Edge e = (Edge) graph.getEdgelist().get(0);
            if ((e.left_node.c_node == true) && (e.right_node.c_node == true))
                prob = ((Edge) graph.edgeList.get(0)).prob;
            else prob = 0;
            resultText = "The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob;
            if (lang == 'D')
                resultText = "Das reduzierte Netz enthaelt nur eine Kante. Die Zuverlaessigkeit des Netzes ist:\nP=" + prob;
        } else {
            prob = 0;
            String str3 = "P=";
            int count = 0;
            MyIterator it = zer.hz.iterator();
            while (it.hasNext()) {
                MyList al = (MyList) it.next();
                MySet hs = (MySet) al.get(0);
                float p;
                p = getP(hs);
                String s = getNo(hs);
                for (int i = 1; i < al.size(); i++) {
                    MySet hs1 = (MySet) al.get(i);
                    if (hs1.isEmpty())
                        continue;
                    p = p * (1 - getP(hs1));
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

            if (lang == 'D') {
                str1 = "Das Netz wird mit dem Algorithmus von Heidtmann zerlegt:\n";
                str2 = "Die Zuverlaessigkeit des Netzes ist:\n";
            }
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
            if (lang == 'D') {
                str1 = "Das Netz wird mit dem Algorithmus von Abraham zerlegt:\n";
                str2 = "Die Zuverlaessigkeit des Netzes ist:\n";
            }
            resultText = resultText + str1 + s + "\n" + str2 + prob;

        }
        result.setText(resultText);
        viewFactorTreeBtn.setEnabled(true);
        //calcReliabilityBtn.setEnabled(false);
    }


//////////////////Zuverlaessigkeitsberechnung nur mit Heidtmann's Algorithm //////////////////  

    public float heidtmanns_reliability() {
        long start = new Date().getTime();


        if (calculationSeriesMode == 0 && !onlyReliabilityFast) {
            resultText = "Step " + counter + " of " + combinations;
            result.setText(resultText);
        }


        zer = new Zerleg(graph);
        zer.start();
        try {
            zer.join();
        } catch (InterruptedException e) {
        }

        //Die Zerlegung verbraucht die meiste Zeit, beim Testnetz etwa 300ms, während der Rest nur max 1 ms braucht
        System.out.println("Laufzeit Heidtmann bis Zerlegung: " + ((new Date()).getTime() - start));
        reassignProbabilites(true);

        //die IW jeder Kante zuweisen
        Util.getProbability(graph);
        if (graph.edgeList.size() == 1) {
            Edge e = (Edge) graph.getEdgelist().get(0);
            if ((e.left_node.c_node == true) && (e.right_node.c_node == true))
                prob = ((Edge) graph.edgeList.get(0)).prob;
            else prob = 0;
            resultText = "The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob;
            if (lang == 'D')
                resultText = "Das reduzierte Netz enthaelt nur eine Kante. Die Zuverlaessigkeit des Netzes ist:\nP=" + prob;
        } else {
            prob = 0;
            int count = 0;
            MyIterator it = zer.hz.iterator();
            while (it.hasNext()) {
                System.out.println("Neuer Pfad");
                MyList al = (MyList) it.next();
                MySet hs = (MySet) al.get(0);
                float p;
                //hs enthält hier anscheinend einen Pfad im Graphen zwischen den K-Knoten
                //TODO genauer untersuchen
                p = getP(hs);
                //p = getPathProbability(hs, false);

                for (int i = 1; i < al.size(); i++) {
                    MySet hs1 = (MySet) al.get(i);
                    if (hs1.isEmpty())
                        continue;
                    p = p * (1 - getP(hs1));
                    //p = p * (1 - getPathProbability(hs1, true));


                }
                count++;
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

            //result.setText(resultText);
            viewFactorTreeBtn.setEnabled(true);
        }
        long runningTime = new Date().getTime() - start;
        System.out.println("Laufzeit Heidtmann: " + runningTime);
        System.out.println("Prob. Heidtmann: " + prob);
        return prob;
    }


////////////////// Zuverlaessigkeitsberechnung nur mit Faktorisierung //////////////////  

    public float fact_reliability() {
        long start = new Date().getTime();

        if (calculationSeriesMode == 0 && !onlyReliabilityFast) {
            resultText = "Step " + counter + " of " + combinations;
            result.setText(resultText);
        }

        counterFact = 0;
        graphfact.level = 0;
        generated_Graphs = new MyList();
        generated_Graphs.add(graphfact);
        factProb = "(";
        graphfact.child_Graphs = new MyList();

        probfact = Util.getProbabilityFact(graphfact, 0);

        Util.drawTreeofGraphs(graphfact);
        calcReliabilityBtn.setEnabled(true);
        resilienceBtn.setEnabled(true);
        viewFactorTreeBtn.setEnabled(true);

        long runningTime = new Date().getTime() - start;
        System.out.println("Laufzeit Fact: " + runningTime);
        System.out.println("Prob. Fact: " + probfact);
        return probfact;
    }

///////////////////////////// Binomialkoeffizient ////////////////////////////////// 


    public BigInteger binomial(long n, long k) {
//    	long start = new Date().getTime();
        BigInteger binomialCoefficient = BigInteger.ONE;

        // Nutze die Symmetrie des Pascalschen Dreiecks um den Aufwand zu minimieren.
        if (k > (long) (n / 2)) {
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


    public int total_nodes;
    public int c_nodes;
    public BigInteger combinations;
    public float result_resilience;
    public float test_Summe;
    public int counter;
    public int resilienceMode; // 1 => Fact; 2 => Heidtmann

    // Hauptmethode, die den Algorithmus zur Berechnung der Resilienz beinhaltet.
    public void calculate_resilience() {
        long start = new Date().getTime();

        resultText = "Calculating...";
        result.setText(resultText);

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
        if (Con_check.check(graph) == -1) {
            //com.resinet.model.Graph ist zusammenhängend
            resilienceMode = 2;
        } else {
            resilienceMode = 1;
        }

        Graph graphSave = null;
        Graph graphSave2 = null;

        try {
            graphSave = (Graph) Util.serialClone(graph); //clone Graphen
            graphSave2 = (Graph) Util.serialClone(graphfact); //clone Graphen
        } catch (java.io.IOException e1) {
            System.err.println(e1.toString());
        } catch (java.lang.ClassNotFoundException e2) {
            System.err.println(e2.toString());
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
                if (d.contains(i)) {
                    node1.k = true;
                } else {
                    node1.k = false;
                }

                // Schreibe jeden Knoten neu in die Knotenliste.
                drawnNodes.set(i, node1);
                //graph.nodeList.set(i, node1);

            }

            // Erhöhe pro Kombination den Zähler um 1.
            counter++;

            if (resilienceMode == 2) {
                graph = makeGraph();
            } else {
                graphfact = makeGraph();
            }


            // Wahrscheinlichkeiten neu zuordnen.
            reassignProbabilities();

            // Berechne die Zuverlässigkeit für die aktuelle Kombination und addiere sie zur bisherigen Summe. 
            if (resilienceMode == 2) {
                result_resilience = result_resilience + heidtmanns_reliability();
            } else {
                result_resilience = result_resilience + fact_reliability();
            }

        }

        test_Summe = result_resilience;

        // Teile die Summe der Zuverlässigkeiten durch die Anzahl der Kombinationen.
        result_resilience = result_resilience / combinations.longValue();

        //K-Knotenliste zurücksetzen
        for (int i = 0; i < total_nodes; i++) {
            // Entsprechenden Knoten holen
            NodePoint nodeReset = (NodePoint) drawnNodes.get(i);

            // Dann auf true, falls K-Knoten
            if (cNodeList.charAt(i) == '1') {
                nodeReset.k = true;
            } else {
                nodeReset.k = false;
            }

            // Schreibe jeden Knoten neu in die Knotenliste.
            drawnNodes.set(i, nodeReset);
        }

        try {

            graph = (Graph) Util.serialClone(graphSave); //clone Graphen
            graphfact = (Graph) Util.serialClone(graphSave2); //clone Graphen
        } catch (java.io.IOException e1) {
            System.err.println(e1.toString());
        } catch (java.lang.ClassNotFoundException e2) {
            System.err.println(e2.toString());
        }

        long runningTime = new Date().getTime() - start;
        System.out.println("Laufzeit Resilienz: " + runningTime);
    }


    /// Hilfsmethode zum Erzeugen aller Kombinationen von K-Knoten
    public Set<String> generateCombinations(String inputString) {
        Set<String> combinationsSet = new HashSet<String>();
        if (inputString == "")
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
    public void inputNet() {
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
        LineNumberReader lineReader = null;

        try {
            lineReader = new LineNumberReader(new FileReader(netFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
            return;
        }
    }

    public void inputError() {
        //Error-Popup ausgeben

        String str = "Your input was invalid! Please choose a valid file created by Pajek or ResiNeT.";
        Frame frame = new Frame("Warning!");
        frame.setLayout(new BorderLayout());
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent event) {
                        Frame f = (Frame) event.getSource();
                        f.setVisible(false);
                        f.dispose();
                    }
                }
        );

        TextArea ta = new TextArea(str, 50, 40, TextArea.SCROLLBARS_NONE);
        frame.add("Center", ta);
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridBagLayout());
        Button bn = new Button("Ok");
        bn.addActionListener(new ActionListener() {
                                 public void actionPerformed(ActionEvent event) {
                                     Button b = (Button) event.getSource();
                                     b.getParent().setVisible(false);
                                     ((Frame) (b.getParent()).getParent()).dispose();
                                 }
                             }
        );
        GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
        buttonPanel.add(bn, bnGbc);
        frame.add("South", buttonPanel);
        Point location = netPanel.getLocationOnScreen();
        frame.setLocation(location);
        frame.setVisible(true);
        frame.setSize(370, 200);

    }

    public void exportNet() {
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
        Writer writer = null;

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
            return;
        }
    }

    public void exportError() {
        //Error-Popup ausgeben
        String str = "Your output was invalid! Please choose a valid filepath and use the file extension '.net'.";
        Frame frame = new Frame("Warning!");
        frame.setLayout(new BorderLayout());
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent event) {
                        Frame f = (Frame) event.getSource();
                        f.setVisible(false);
                        f.dispose();
                    }
                }
        );

        TextArea ta = new TextArea(str, 50, 40, TextArea.SCROLLBARS_NONE);
        frame.add("Center", ta);
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridBagLayout());
        Button bn = new Button("Ok");
        bn.addActionListener(new ActionListener() {
                                 public void actionPerformed(ActionEvent event) {
                                     Button b = (Button) event.getSource();
                                     b.getParent().setVisible(false);
                                     ((Frame) (b.getParent()).getParent()).dispose();
                                 }
                             }
        );
        GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
        buttonPanel.add(bn, bnGbc);
        frame.add("South", buttonPanel);
        Point location = netPanel.getLocationOnScreen();
        frame.setLocation(location);
        frame.setVisible(true);
        frame.setSize(370, 200);
    }


    public BigDecimal startValue;
    public BigDecimal endValue;
    public BigDecimal stepSize;
    public int calculationSeriesMode = 0; //1 = resilience, 2 = reliability;
    public boolean onlyReliabilityFast;

    //Für die Serienberechnung in Schritten
    public void calculationSeries() {
        //Sicherungskopien
        Graph graphSave = null;
        Graph graphSave2 = null;

        try {
            graphSave = (Graph) Util.serialClone(graph); //clone Graphen
            graphSave2 = (Graph) Util.serialClone(graphfact); //clone Graphen
        } catch (java.io.IOException e1) {
            System.err.println(e1.toString());
        } catch (java.lang.ClassNotFoundException e2) {
            System.err.println(e2.toString());
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
        Writer writer = null;

        try {
            writer = new FileWriter(filepath);

            if (calculationSeriesMode == 1) {
                writer.write("Reliability of every edge                 Resilience of the network");
            } else {
                writer.write("Reliability of every edge                 Reliability of the network");
            }

            writer.append(System.getProperty("line.separator"));

            //Prüfen ob das Netz zusammenhängt
            boolean graphConnected;
            if (Con_check.check(graph) == -1) {
                //com.resinet.model.Graph ist zusammenhängend
                graphConnected = true;
            } else {
                graphConnected = false;
            }

            //Ab hier Berechnungsserie
            int counter = 1;
            for (BigDecimal i = startValue; i.compareTo(endValue) <= 0; i = i.add(stepSize))
            //for(float i = startValue; i<=endValue; i=i+stepSize)
            {
                resultText = "Calculation Series: Step " + counter + " of " + ((endValue.subtract(startValue)).divide(stepSize)).add(BigDecimal.ONE);
                result.setText(resultText);
                counter++;

                BigDecimal reliability = i;

                //Neue/aktuelle Wahrscheinlichkeiten zuweisen
                for (int j = 0; j < edgeProbabilities.length; j++) {
                    edgeProbabilities[j] = reliability.floatValue();
                }

                if (calculationSeriesMode == 1) //Resilienz
                {
                    calculate_resilience();
                } else //Reliability
                {
                    // Wahrscheinlichkeiten neu zuordnen. (wird in calculate_resilience() auch gemacht)
                    reassignProbabilities();

                    if (graphConnected) {
                        heidtmanns_reliability();
                    } else {
                        fact_reliability();
                    }
                }

                writer.append(System.getProperty("line.separator"));

                String reliabilityString = reliability.toString();

                while (reliabilityString.length() < stepSize.toString().length()) {
                    reliabilityString = reliabilityString + "0";
                }

                while (reliabilityString.length() < 42) {
                    reliabilityString = reliabilityString + " ";
                }

                if (calculationSeriesMode == 1) {
                    writer.write(reliabilityString + result_resilience);
                } else {
                    if (graphConnected) {
                        writer.write(reliabilityString + prob);
                    } else {
                        writer.write(reliabilityString + probfact);
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
            graphfact = (Graph) Util.serialClone(graphSave2); //clone Graphen
        } catch (java.io.IOException e1) {
            System.err.println(e1.toString());
        } catch (java.lang.ClassNotFoundException e2) {
            System.err.println(e2.toString());
        }

        edgeProbabilities = probsSave.clone();


        calculationSeriesMode = 0;

        // Wahrscheinlichkeiten neu zuordnen.
        reassignProbabilities();

        resultText = "Calculation series finished. Please check your output file for the results.";
        result.setText(resultText);

    }


}
