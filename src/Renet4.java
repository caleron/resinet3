/* Renet4.java */

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Renet4 extends JFrame
        implements ActionListener, ItemListener {
    Panel panel1, panel3, panel5, panel6, panel7;
    static Panel output;
    FlagPanel panel0;
    NetPanel panel2;
    ProbPanel panel4;
    Label label0, label1, label2;
    TextArea text;
    TextField pf, textfieldEndvalue, textfieldStepsize;
    Button drawB, reset1B, ok1B, sameReliabilityB, reset2B, ok2B, decomB, resilienceB, viewB, inputNet, exportNet;
    boolean sameReliability, inputNetBoolean;
    Choice ch;
    char lang = 'E';
    String s1, s2;

    MyList nodes;
    MyList edges;
    EdgeLine el;
    boolean valid = false;
    boolean probability_mode = false;
    MyMouseListener ml;
    MyMouseMotionListener mml;
    TextField[] probabs;
    float[] probs;
    float prob;
    float probfact;
    String resultText;
    TextArea result;
    MyList nd;
    static String reduceText;
    static String factProb;
    static int counterFact;

    Graph graphfact;
    Graph graph;
    static MyList generated_Graphs;
    float graph_width;
    float graph_height;
    int smallest_x_pos;
    int highest_x_pos;
    int smallest_y_pos;
    int highest_y_pos;
    int cntedge;
    MyList br, br_fact, brc;
    Zerleg zer;

    String enText, deText, resultTextEn;

    ScrollPane sp;
    Image logo;
    Checkbox reliabilityCheckBox;


    //Fuers Logo
    public void paint(Graphics g) {
        g.drawImage(logo, 270, 7, this);
    }

    public Renet4() {
        init();
    }

    public static void main(String[] args) {
        Renet4 r = new Renet4();
        r.setSize(new Dimension(800, 1000));
        r.setVisible(true);
    }

    public void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        nodes = new MyList();
        edges = new MyList();

        startValue = BigDecimal.ZERO;
        endValue = BigDecimal.ZERO;
        stepSize = BigDecimal.ZERO;
        calculationSeriesMode = 0;
        onlyReliabilityFast = false;

        Color color = new Color(85, 143, 180);
        //color = color.brighter();
        setBackground(color);
        GridBagLayout mainLayout = new GridBagLayout();
        setLayout(mainLayout);

        logo = getToolkit().getImage(getClass().getResource("logo.jpg"));
        prepareImage(logo, this);

        Panel halt0 = new Panel();
        GridBagConstraints halt0Gbc = makegbc(0, 0, 1, 1, "west");
        add(halt0, halt0Gbc);

        panel0 = new FlagPanel();
        GridBagConstraints panel0Gbc = makegbc(0, 0, 1, 4, "center");
        //GridBagConstraints panel0Gbc = makegbc(0, 0, 1, 4, "center");
        panel0Gbc.fill = GridBagConstraints.BOTH;
        //add(panel0, panel0Gbc);

        //Sprachenauswahl nicht sichtbar!
        //panel0.setVisible(false);

        panel1 = new Panel();
        GridBagConstraints panel1Gbc = makegbc(0, 5, 1, 1, "west");
        add(panel1, panel1Gbc);
        GridBagLayout panel1Layout = new GridBagLayout();
        panel1.setLayout(panel1Layout);
        label1 = new Label("Please input your network model:");
        GridBagConstraints label1Gbc = makegbc(0, 0, 4, 1, "west");
        panel1.add(label1, label1Gbc);
        drawB = new Button("Draw");
        drawB.addActionListener(this);
        GridBagConstraints drawBGbc = makegbc(0, 1, 1, 1, "west");
        panel1.add(drawB, drawBGbc);
        ok1B = new Button("Ok (edges have different reliabilities)");
        ok1B.setEnabled(false);
        ok1B.addActionListener(this);
        GridBagConstraints ok1BGbc = makegbc(1, 1, 1, 1, "west");
        panel1.add(ok1B, ok1BGbc);
        reset1B = new Button("Reset");
        reset1B.setEnabled(false);
        reset1B.addActionListener(this);
        GridBagConstraints reset1BGbc = makegbc(3, 1, 1, 1, "west");
        panel1.add(reset1B, reset1BGbc);

        sameReliabilityB = new Button("Ok (all edges have the same reliability)");
        sameReliabilityB.setEnabled(false);
        sameReliabilityB.addActionListener(this);
        GridBagConstraints sameReliabilityBGbc = makegbc(2, 1, 1, 1, "west");
        panel1.add(sameReliabilityB, sameReliabilityBGbc);
        sameReliability = false;


        //Button Input Network
        inputNet = new Button("Load");
        inputNet.setEnabled(true);
        inputNet.addActionListener(this);
        GridBagConstraints inputNetGbc = makegbc(4, 1, 1, 1, "west");
        panel1.add(inputNet, inputNetGbc);
        inputNetBoolean = false;

        //Button Output Network
        exportNet = new Button("Save");
        exportNet.setEnabled(true);
        exportNet.addActionListener(this);
        GridBagConstraints exportNetGbc = makegbc(5, 1, 1, 1, "west");
        panel1.add(exportNet, exportNetGbc);

        panel2 = new NetPanel();
        ml = new MyMouseListener();
        mml = new MyMouseMotionListener();
        panel2.setBackground(Color.white);
        panel2.setSize(625, 315);
        //panel2.setSize(600, 200);
        panel2.setVisible(true);
        GridBagConstraints panel2Gbc = makegbc(0, 6, 1, 1, "west");
        add(panel2, panel2Gbc);
        reduceText = "";
        enText = "On this panel you can draw your network model after a click on the \"Draw\" button.\nPress the left button to draw a node and the right button to draw a connection-node. Delete a node by holding the <shift>-key an pressing the left button.\nTo draw an edge press the left button when the mouse pointer is on a node and hold it. Then drag the mouse to another node and release it. For deleting an edge delete its corresponding nodes.\nAfter you have finished, click the \"Ok\" button.\n\nYou can also import a previously created network (ResiNeT or Pajek) by clicking the \"Load\" button. To turn an existing node into a connection-node hold the Ctrl-Key while left-clicking on the node.";
        deText = "Hier koennen Sie Ihr Netz eingeben. Klicken Sie dazu zunaechst auf \"Zeichnen\".\nEinen \"normalen\" Knoten erzeugen Sie, indem Sie die linke Maustaste betaetigen, einen K-Knoten durch Betaetigen der rechten Maustaste. Loeschen Sie einen Knoten, indem Sie die <shift>-Taste halten und die linke Maustaste betaetigen.\nUm eine Kante zu zeichnen, klicken Sie mit der linken Maustaste auf einen Knoten, halten diese solange gedrueckt, bis sich der Mauszeiger ueber dem Knoten befindet, zu dem die Kante fuehren soll. Eine Kante kann durch das Löschen ihrer inzidenten Knoten geloescht werden.\nHaben Sie Ihr Netz komplett eingegeben, klicken Sie bitte auf \"Ok\".\n";
        text = new TextArea(enText, 19, 85, TextArea.SCROLLBARS_NONE);
        text.setBackground(Color.white);
        text.setEditable(false);
        panel2.add(text);


        Panel halt1 = new Panel();
        GridBagConstraints halt1Gbc = makegbc(0, 7, 1, 1, "west");
        add(halt1, halt1Gbc);


        panel3 = new Panel();
        panel3.setLayout(new GridLayout(1, 1));
        GridBagConstraints panel3Gbc = makegbc(0, 7, 1, 1, "west");
        panel3Gbc.fill = GridBagConstraints.HORIZONTAL;
        add(panel3, panel3Gbc);
        label2 = new Label("   Now you can input the reliability of every edge:", Label.LEFT);
        panel3.add(label2);


        //Option 1: JScrollPane, funktioniert nicht
/*	JScrollPane sp = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );	
	sp.setPreferredSize(new java.awt.Dimension(600, 200));
	panel4 = new ProbPanel();
	sp.setViewportView( panel4 );	
	GridBagConstraints spGbc = makegbc(0, 9, 1, 4, "west" );*/


        //Option 2: ScrollPane
        sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        panel4 = new ProbPanel();
        //sp.setSize(625, 200);
        sp.setSize(625, 140);
        //panel4.setSize(600, 400);	
        sp.add(panel4);
        GridBagConstraints spGbc = makegbc(0, 9, 1, 4, "west");
        add(sp, spGbc);

        panel5 = new Panel();
        GridBagConstraints panel5Gbc = makegbc(0, 13, 1, 1, "west");
        panel5.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel5Gbc.fill = GridBagConstraints.HORIZONTAL;
        add(panel5, panel5Gbc);
        reset2B = new Button("Reset");
        reset2B.setEnabled(false);
        reset2B.addActionListener(this);
        ok2B = new Button("Ok");
        ok2B.setEnabled(false);
        ok2B.addActionListener(this);
        panel5.add(reset2B);
        panel5.add(ok2B);

        Panel halt2 = new Panel();
        GridBagConstraints halt2Gbc = makegbc(0, 14, 1, 1, "west");
        add(halt2, halt2Gbc);

        panel6 = new Panel();
        GridBagConstraints panel6Gbc = makegbc(0, 15, 1, 1, "west");
        panel6.setLayout(new GridBagLayout());
        add(panel6, panel6Gbc);

        Panel halt3 = new Panel();
        GridBagConstraints halt3Gbc = makegbc(0, 1, 1, 3, "west");
        panel6.add(halt3, halt3Gbc);

        decomB = new Button("Calculate the reliability of the network");
        decomB.setEnabled(false);
        decomB.addActionListener(this);

        resilienceB = new Button("Calculate the resilience of the network");
        resilienceB.setEnabled(false);
        resilienceB.addActionListener(this);

        //GridBagConstraints resilienceBGbc = makegbc(0, 4, 1, 1, "southwest");
        GridBagConstraints resilienceBGbc = makegbc(2, 0, 1, 1, "west");
        GridBagConstraints decomBGbc = makegbc(1, 0, 1, 1, "west");

        Panel reliabilityButtons = new Panel();
        reliabilityButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        reliabilityButtons.add(decomB);

        reliabilityCheckBox = new Checkbox("Compare 3 algorithms");
        reliabilityButtons.add(reliabilityCheckBox);
        //reliabilityButtons.add(resilienceB);	
        panel6.add(reliabilityButtons);

        Panel resiliencePanel = new Panel();
        GridBagConstraints resiliencePanelGbc = makegbc(0, 16, 1, 1, "west");
        resiliencePanel.add(resilienceB);
        add(resiliencePanel, resiliencePanelGbc);

        //panel6.add(reliabilityCheckBox);

        //panel6.add(decomB, decomBGbc);
        //panel6.add(resilienceB, resilienceBGbc);

        viewB = new Button("View Tree of Factorisation");
        viewB.setEnabled(false);
        viewB.addActionListener(this);

        // Fuer ResiNeT2 ist der Button nicht sichtbar
        viewB.setVisible(false);

        //GridBagConstraints viewBGbc = makegbc(2, 0, 1, 1, "northwest");
        //panel6.add(viewB, viewBGbc);
        panel7 = new Panel();
        //GridBagConstraints panel7Gbc = makegbc(0, 16, 1, 4, "west" );
        GridBagConstraints panel7Gbc = makegbc(0, 17, 1, 4, "west");
        add(panel7, panel7Gbc);

        //result = new TextArea(" ", 10, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
        //result = new TextArea(" ", 3, 85, TextArea.SCROLLBARS_NONE);
        result = new TextArea(" ", 3, 85, TextArea.SCROLLBARS_VERTICAL_ONLY);
        result.setEditable(false);
        panel7.add(result);
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

    public void itemStateChanged(ItemEvent evt) {
        Choice ch = (Choice) evt.getItemSelectable();
        int i = ch.getSelectedIndex();
        switch (i) {
            case 0: {
                lang = 'E';
                result.setText(" ");
                panel2.add(text);
                panel4.removeAll();
                drawB.setEnabled(true);

                //panel0

                panel0.s = "uk.gif";
                label0.setText("Select your language");
                panel0.invalidate();


                //panel1
                label1.setText("Please input your network model:");
                drawB.setLabel("Draw");
                ok1B.setLabel("Ok (edges have different reliabilities)");
                sameReliabilityB.setLabel("Ok (all edges have the same reliability)");
                reset1B.setLabel("Reset");
                panel1.invalidate();

                //panel2
                text.setText(enText);

                //panel3
                label2.setText("   Now you can input the reliability of every edge:");

                //panel5
                reset2B.setLabel("Reset");
                panel5.invalidate();

                //panel6
                //reduceB.setLabel("Reduce this network");
                viewB.setLabel("View Tree of Factorisation");
                decomB.setLabel("Calculate the reliability of the network");
                resilienceB.setLabel(" resilience of the network");
                panel6.invalidate();

                validate();
                break;
            }
            case 1: {
                lang = 'D';
                result.setText(" ");
                panel2.add(text);
                panel4.removeAll();
                drawB.setEnabled(true);

                //panel0
                panel0.s = "de.gif";
                label0.setText("Bitte waehlen Sie Ihre Sprache aus");
                panel0.invalidate();

                //panel1
                label1.setText(" Bitte geben Sie hier Ihr Netz ein:");
                drawB.setLabel("Zeichnen");
                ok1B.setLabel("Ok (Verschiedene Kantenzuverlässigkeiten)");
                sameReliabilityB.setLabel("Ok (Einheitliche Kantenzuverlässigkeit)");
                reset1B.setLabel("Zurücksetzen");
                panel1.invalidate();

                //panel2
                text.setText(deText);

                //panel3
                label2.setText("   Nun geben Sie bitte die Intaktwahrscheinlichkeit jeder Kante ein:");

                //panel5
                reset2B.setLabel("Zurücksetzen");
                panel5.invalidate();

                //panel6
                //reduceB.setLabel("Das Netz reduzieren");
                //viewB.setLabel("Anzeigen");
                decomB.setLabel("Die Zuverlaessigkeit des Netzes berechnen");
                resilienceB.setLabel("Die Resilienz des Netzes berechnen");
                viewB.setLabel("Faktorisierungsbaum anzeigen");
                panel6.invalidate();

                validate();
                break;
            }
            default:
                panel0.s = "logo.jpg";
        }

    }

    public void actionPerformed(ActionEvent evt) {
        Button button = (Button) evt.getSource();

        if (button == drawB) {
            panel2.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            panel2.addMouseListener(ml);
            panel2.addMouseMotionListener(mml);
            ok1B.setEnabled(true);
            sameReliabilityB.setEnabled(true);
            drawB.setEnabled(false);
            reset1B.setEnabled(true);
            if (panel2.getComponentCount() != 0)
                panel2.remove(text);
        }

        if (button == sameReliabilityB) {
            panel4.removeAll();
            sameReliability = true;
            reset1B.setEnabled(true);
            sameReliabilityB.setEnabled(true);
            okButton();
            ok1B.setEnabled(true);
            decomB.setEnabled(false);
            resilienceB.setEnabled(false);
        }

        if (button == inputNet) {
            panel2.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            ok1B.setEnabled(true);
            reset1B.setEnabled(true);
            panel2.removeMouseListener(ml);
            panel2.removeMouseMotionListener(mml);
            panel2.addMouseListener(ml);
            panel2.addMouseMotionListener(mml);
            nodes.clear();
            edges.clear();
            graph = null;
            zer = null;
            valid = false;
            probability_mode = false;
            panel4.removeAll();
            panel4.repaint();
            drawB.setEnabled(false);
            ok2B.setEnabled(false);
            decomB.setEnabled(false);
            resilienceB.setEnabled(false);
            viewB.setEnabled(false);
            //result.setText(" ");
            //reduceText = "";
            sameReliability = false;
            sameReliabilityB.setEnabled(true);

            //panel4.removeAll();
            inputNetBoolean = true;
            //reset1B.setEnabled(true);			

            inputNet();

            //ok1B.setEnabled(true);
            //sameReliabilityB.setEnabled(true);
            //decomB.setEnabled(false);
            //resilienceB.setEnabled(false);
            panel2.remove(text);
            panel2.repaint();

        }

        if (button == exportNet) {
            exportNet();
        }

        if (button == ok1B) {
            panel4.removeAll();
            sameReliability = false;
            reset1B.setEnabled(true);
            sameReliabilityB.setEnabled(true);
            okButton();
            ok1B.setEnabled(true);
            decomB.setEnabled(false);
            resilienceB.setEnabled(false);

        }

        if (button == reset1B) {
            startValue = BigDecimal.ZERO;
            endValue = BigDecimal.ZERO;
            stepSize = BigDecimal.ZERO;

            panel2.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            ok1B.setEnabled(true);
            reset1B.setEnabled(true);
            panel2.removeMouseListener(ml);
            panel2.removeMouseMotionListener(mml);
            panel2.addMouseListener(ml);
            panel2.addMouseMotionListener(mml);
            nodes.clear();
            edges.clear();
            graph = null;
            zer = null;
            valid = false;
            probability_mode = false;
            panel2.repaint();
            panel4.removeAll();
            panel4.repaint();
            drawB.setEnabled(false);
            ok2B.setEnabled(false);
            decomB.setEnabled(false);
            resilienceB.setEnabled(false);
            viewB.setEnabled(false);
            result.setText(" ");
            reduceText = "";
            sameReliability = false;
            sameReliabilityB.setEnabled(true);
        }

        if (button == ok2B) {
            int n = probabs.length;
            probs = new float[n];
            MyList l = new MyList();

            for (int i = 0; i < n; i++) {
                String s = probabs[i].getText();
                if (!checkText(s))
                    l.add(String.valueOf(i));
            }

            if (sameReliability) {
                String sEnd = textfieldEndvalue.getText();
                if (sEnd.length() != 0) {
                    for (int i = 0; i < n; i++) {
                        if (!checkText(sEnd))
                            l.add(String.valueOf(i));
                    }
                }
            }

            if (l.size() != 0) {
                MyIterator it = l.iterator();
                String s1 = (String) it.next();
                while (it.hasNext()) {
                    String s2 = (String) it.next();
                    s1 = s1 + ", " + s2;
                }
                String str = "The reliability of an edge is a probability, thus\nit must be a number in format x.xxxxxx which is\nless than or equal to 1. Please check the in-\nput for edge\n" + s1;

                if (lang == 'D')
                    str = "Die Intaktwahrscheinlichkeit einer Kante muss eine Zahl kleiner oder gleich 1 im Format x.xxxxxx sein. Bitte ueberpruefen Sie die Eingabe bei Kante:\n" + s1;

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
                Point location = panel4.getLocationOnScreen();
                frame.setLocation(location);
                frame.setVisible(true);
                frame.setSize(370, 200);
                return;
            }

            for (int i = 0; i < n; i++) {
                probabs[i].setEditable(false);
                String s = probabs[i].getText();
                Float fo = Float.valueOf(s);
                float f = fo.floatValue();
                probs[i] = f;
            }
            reset2B.setEnabled(true);
            ok2B.setEnabled(false);
            panel2.removeMouseListener(ml);
            panel2.removeMouseMotionListener(mml);

            if (graph != null)
                decomB.setEnabled(true);
            resilienceB.setEnabled(true);

		/*Wahrscheinlichkeiten neu zuordnen.*/
            br = (MyList) graph.getEdgelist().clone();
            br_fact = (MyList) graphfact.getEdgelist().clone();
            for (int i = 0; i < probs.length; i++) {
                Edge e = (Edge) br.get(i);
                e.prob = probs[i];
                Edge e2 = (Edge) br_fact.get(i);
                e2.prob = probs[i];
            }

            //End value und step size zuordnen
            if (probabs[0].getText().length() != 0) {
                startValue = new BigDecimal(probabs[0].getText());
                System.out.println("StartValue: " + startValue);
            }

            if (sameReliability) {
                if (textfieldEndvalue.getText().length() != 0) {
                    endValue = new BigDecimal(textfieldEndvalue.getText());
                    System.out.println("EndValue: " + endValue);
                }

                if (textfieldStepsize.getText().length() != 0) {
                    stepSize = new BigDecimal(textfieldStepsize.getText());
                    System.out.println("StepSize: " + stepSize);
                }
            }


        }


        if (button == reset2B) {
            startValue = BigDecimal.ZERO;
            endValue = BigDecimal.ZERO;
            stepSize = BigDecimal.ZERO;

            ok2B.setEnabled(true);
            ok1B.setEnabled(true);
            reset2B.setEnabled(false);
            decomB.setEnabled(false);
            resilienceB.setEnabled(false);
            probs = null;
            for (int i = 0; i < probabs.length; i++) {
                probabs[i].setText(null);
                probabs[i].setEditable(true);
            }

            if (textfieldEndvalue != null) {
                textfieldEndvalue.setText(null);
            }
            if (textfieldStepsize != null) {
                textfieldStepsize.setText(null);
            }

            sameReliabilityB.setEnabled(true);

        }

        if (button == viewB) {
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


        if (button == resilienceB) {
            if (!endValue.equals(BigDecimal.ZERO) && !stepSize.equals(BigDecimal.ZERO) && sameReliability) {
                calculationSeriesMode = 1;
                calculationSeries();
            } else {
                calculationSeriesMode = 0;
                calculate_resilience();

                resultText = "The network has " + total_nodes + " nodes, containing " + c_nodes + " c-nodes.\n" + "There are " + combinations + " combinations.\n" + "The resilience of the network is: " + result_resilience;
                //resultText = "Das Netz hat " + total_nodes + " Knoten, davon " + c_nodes + " K-Knoten.\n" + "Es gibt also " + combinations + " Kombinationen.\n" + "Die Resilienz des Netzes ist: " + result_resilience;		
                result.setText(resultText);
            }
            viewB.setEnabled(true);
        }


        if (button == decomB) {
            //heidtmanns_reliability();
            //fact_reliability();

            if (!endValue.equals(BigDecimal.ZERO) && !stepSize.equals(BigDecimal.ZERO) && sameReliability) {
                calculationSeriesMode = 2;
                calculationSeries();
            } else {
                calculationSeriesMode = 0;
                //Wenn die Checkbox angeklickt wurde, sollen die 3 Alg. verglichen werden. Sonst nicht.
                if (reliabilityCheckBox.getState()) {
                    calculate_reliability();
                } else {
                    onlyReliabilityFast = true;
                    calculate_reliability_faster();
                    onlyReliabilityFast = false;
                }
            }

        }
    }

    private float getP(MySet hs) {
        float p = 1;
        MyIterator it = hs.iterator();
        while (it.hasNext()) {
            Edge e = (Edge) it.next();
            p = p * e.prob;
        }
        return p;
    }

    private String getNo(MySet hs) {
        String s;
        MyIterator it = hs.iterator();
        Edge e = (Edge) it.next();
        s = "r" + String.valueOf(e.edge_no);
        while (it.hasNext()) {
            e = (Edge) it.next();
            s = s + "r" + e.edge_no;
        }
        return s;
    }

    private boolean checkText(String str) {
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
                    ;
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

    public void okButton() {
        if (edges.size() == 0) {
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
            Point location = panel4.getLocationOnScreen();
            frame.setLocation(location);
            frame.setVisible(true);
            frame.setSize(370, 200);
            return;
        }

        int count = 0;
        MyIterator np = nodes.iterator();
        while (np.hasNext()) {
            NodePoint n = (NodePoint) np.next();
            if (n.k == true)
                count = count + 1;
        }

        if (count < 2) {
            String str = "Your Network does not contain at least 2 c-nodes! You can draw a new c-node by pressing the right mouse button. If you want to transform an existing node into a c-node, please hold the Ctrl-Key on your keyboard while left-clicking on the node.";
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
            Point location = panel4.getLocationOnScreen();
            frame.setLocation(location);
            frame.setVisible(true);
            frame.setSize(370, 200);
            return;
        }

		/*Ermittle kleinste und größte Positionswerte der Knoten.*/
        smallest_x_pos = 2000;
        highest_x_pos = 0;
        smallest_y_pos = 2000;
        highest_y_pos = 0;
        np = nodes.iterator();
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

        panel2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        ok1B.setEnabled(false);
        reset1B.setEnabled(true);
        probability_mode = true;

        int n = edges.size();
        //panel4.setSize(panel4.getPreferredSize());
        //System.out.println(panel4.getSize());
        /*Da edges.size() sich geaendert hat, muss hier die
          Size von panel4 nochmal festgelegt werden, oder man fuegt erst hier
		  panel4 zur ScrollPane sp:
		  panel4 = new ProbPanel();
		  sp.add(panel4);
		  Dann hat panel4 die richtige Groesse.
		*/

        if (sameReliability) {
            sp.setScrollPosition(0, 0);
            panel4.setSize(600, 179);
            panel4.removeAll();
            //panel4.setLayout(new GridLayout(1, 1));		
            panel4.setLayout(new GridLayout(4, 2));
            probabs = new TextField[n];
            String str = "Reliability of every edge: ";
            if (lang == 'D') {
                str = "Intaktwahrscheinlichkeit: ";
            }

            Label l = new Label(str, Label.RIGHT);
            TextField tf = new TextField(20);
            tf.setBackground(Color.white);
            for (int i = 0; i < edges.size(); i++) {
                probabs[i] = tf;
            }
            Panel p = new Panel();
            p.add(l);
            p.add(tf);
            panel4.add(p);


            String str1 = "Optional for calculation series: ";
            Label l1 = new Label(str1, Label.RIGHT);
            Panel p1 = new Panel();
            p1.add(l1);
            panel4.add(p1);

            String str2 = "End value: ";
            Label l2 = new Label(str2, Label.RIGHT);
            textfieldEndvalue = new TextField(20);
            textfieldEndvalue.setBackground(Color.white);
            Panel p2 = new Panel();
            p2.add(l2);
            p2.add(textfieldEndvalue);

            String str3 = "Step size (e.g. 0.01): ";
            Label l3 = new Label(str3, Label.RIGHT);
            textfieldStepsize = new TextField(20);
            textfieldStepsize.setBackground(Color.white);

            p2.add(l3);
            p2.add(textfieldStepsize);
            panel4.add(p2);
        } else {
            panel4.setSize(panel4.getPreferredSize());
            panel4.removeAll();
            panel4.setLayout(new GridLayout(n / 2 + 1, 2));
            //panel4.setLayout(new GridLayout(0, 3));	
            probabs = new TextField[n];
            String str = "Edge ";
            if (lang == 'D')
                str = "Kante ";

            for (int i = 0; i < edges.size(); i++) {
                String s;
                if (i < 10)
                    s = str + i + " ";
                else
                    s = str + i;
                Label l = new Label(s, Label.RIGHT);
                TextField tf = new TextField(20);
                //TextField tf = new TextField(8);
                tf.setBackground(Color.white);
                probabs[i] = tf;
                Panel p = new Panel();
                p.add(l);
                p.add(tf);
                panel4.add(p);
            }
            sp.validate();
        }
        panel4.validate();


        ok2B.setEnabled(true);
        sameReliabilityB.setEnabled(true);
        ok1B.setEnabled(true);
    }

    private Graph makeGraph() {
        MyList nodeList = new MyList();
        MyList edgeList = new MyList();

        MyIterator it = nodes.iterator();
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
        it = edges.iterator();
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

        GraphPanel gp = new GraphPanel(g);
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

    class FlagPanel extends Panel {
        private Image dbImage;
        private Graphics dbGraphics;
        //String s = "uk.gif";
        String s = "logo.jpg";

        //int width;
        int width = 20;

        public void paint(Graphics g) {
            Image img;
            URL url = null;
            // URL url = getClass().getResource(s);
            try {

                url = getClass().getResource(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            img = getToolkit().getImage(url);
            //Point p = ch.getLocation();
            //width = (int)(p.x+ch.getSize().width+2);
            //g.drawImage(img, width, -6, this);
            g.drawImage(img, 250, -6, this);
        }

        public void update(Graphics g) {
            if (dbImage == null) {
                dbImage = createImage(this.getSize().width, this.getSize().height);
                dbGraphics = dbImage.getGraphics();
            }
            dbGraphics.setColor(getBackground());
            dbGraphics.fillRect(0, 0, this.getSize().width, this.getSize().height);
            dbGraphics.setColor(getForeground());
            paint(dbGraphics);
            g.drawImage(dbImage, 0, 0, this);
        }
        //ueberschreibe die Methode update, um den Bildschirm nicht zu loeschen
    }

    class NetPanel extends Panel {
        private Image dbImage;
        private Graphics dbGraphics;

        public void paint(Graphics g) {
            //g.drawRect(0, 0, 600, 200);
            //System.out.println(panel2.getHeight());		
            g.drawRect(0, 0, 625, panel2.getHeight());

            MyIterator it = nodes.iterator();
            int cnt = 0;
            while (it.hasNext()) {
                g.setColor(Color.black);
                NodePoint np = (NodePoint) it.next();
                int x = np.x;
                int y = np.y;
                String s = String.valueOf(cnt);
                if (!np.k)
                    g.drawOval(x, y, 20, 20);
                else {
                    g.fillOval(x, y, 20, 20);
                    g.setColor(Color.white);
                }
                if (cnt < 10)
                    g.drawString(s, x + 6, y + 13);
                else
                    g.drawString(s, x + 1, y + 13);
                cnt++;
            }

            g.setColor(Color.black);
            it = edges.iterator();
            while (it.hasNext()) {
                EdgeLine e = (EdgeLine) it.next();
                g.drawLine(e.x1, e.y1, e.x2, e.y2);
                String s = String.valueOf(edges.indexOf(e));
                g.drawString(s, e.x0, e.y0);
            }


            if (valid)
                g.drawLine(el.x1, el.y1, el.x2, el.y2);
        }

        public void update(Graphics g) {
            if (dbImage == null) {
                dbImage = createImage(this.getSize().width, this.getSize().height);
                dbGraphics = dbImage.getGraphics();
            }
            dbGraphics.setColor(getBackground());
            dbGraphics.fillRect(0, 0, this.getSize().width, this.getSize().height);
            dbGraphics.setColor(getForeground());
            paint(dbGraphics);
            g.drawImage(dbImage, 0, 0, this);
        }
        //ueberschreiben der Methode update, um den Bildschirm nicht zu loeschen
    }

    class ProbPanel extends Panel {

        public Dimension getPreferredSize() {
            Dimension dm = new Dimension(600, (edges.size() / 2 + 1) * 30);
            return dm;
        }

    }

    class GraphPanel extends Panel {
        Graph graph;

        public GraphPanel(Graph graph) {
            this.graph = graph;
        }

        public void paint(Graphics g) {
            int h = graph.getHighestNodeNr();
            int m = graph.getNodelist().size();
            int[] px = new int[h + 1];
            int[] py = new int[h + 1];

            MyIterator it = graph.getNodelist().iterator();
            while (it.hasNext()) {
                Node node = (Node) it.next();
                int i = node.node_no;
                g.setColor(Color.black);
                int x = node.xposition;
                int y = node.yposition;
                String s = String.valueOf(node.node_no);
                if (!node.c_node)
                    g.drawOval(x, y, 20, 20);
                else {
                    g.fillOval(x, y, 20, 20);
                    g.setColor(Color.white);
                }
                if (i < 10)
                    g.drawString(s, x + 6, y + 13);
                else
                    g.drawString(s, x + 1, y + 13);
            }
            g.setColor(Color.black);
            int n = graph.getEdgelist().size();
            for (int i = 0; i < n; i++) {
                Edge edge = (Edge) graph.getEdgelist().get(i);
                int x1 = edge.left_node.xposition + 10;
                int y1 = edge.left_node.yposition + 10;
                int x2 = edge.right_node.xposition + 10;
                int y2 = edge.right_node.yposition + 10;
                int x0 = (x1 + x2) / 2;
                int y0 = (y1 + y2) / 2;
                g.drawLine(x1, y1, x2, y2);
                g.drawString(String.valueOf(edge.edge_no), x0, y0);
            }
        }

        public Dimension getPreferredSize() {
            Dimension dimension = new Dimension(Math.round(graph_width) + 25, Math.round(graph_height) + 25);
            return dimension;
        }
    }

    class LinePanel extends Panel {
        int width, height, type, edge; //type 0: von oben rechts nach unten links, type1: von oben links nach unten rechts

        public LinePanel(int width, int height, int type, int edge) {
            this.width = width;
            this.height = height;
            this.type = type;
            this.edge = edge;
        }

        public void paint(Graphics g) {
            if (type == 0) {
                g.setColor(Color.blue);
                int x0 = width / 2 - 10;
                int y0 = height / 2;
                String s = "" + edge + "i";
                g.drawLine(width, 0, 0, height);
                g.drawString(s, x0, y0);
            }
            if (type == 1) {
                g.setColor(Color.red);
                int x0 = width / 2 - 10;
                int y0 = height / 2;
                String s = "" + edge + "d";
                g.drawLine(0, 0, width, height);
                g.drawString(s, x0, y0);
            }
        }
    }

    class MyMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            if (probability_mode == false) {
                int x1 = evt.getX();
                int y1 = evt.getY();
                int cnt1;
                int edlnode1;
                int edlnode2;
                MyIterator it = nodes.iterator();
                while (it.hasNext()) {
                    NodePoint nps = (NodePoint) it.next();
                    cnt1 = nodes.indexOf(nps);
                    int px = nps.x;
                    int py = nps.y;
                    px = px + 10;
                    py = py + 10;
                    int dx = x1 - px;
                    int dy = y1 - py;
                    if ((dx * dx + dy * dy) <= 100) {

                        if (evt.isShiftDown()) //zum Löschen von Knoten
                        {
                            nodes.remove(nps);
                            for (int i = 0; i < edges.size(); i++) {
                                EdgeLine edl = (EdgeLine) edges.get(i);
                                edlnode1 = edl.node1; //Knotennummern sichern
                                edlnode2 = edl.node2;
                                if (edl.node1 == cnt1 || edl.node2 == cnt1) {
                                    edges.remove(edl);
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

                            nodes.set(cnt1, nps);
                        }
                        panel2.repaint();
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
                nodes.add(np);
                panel2.repaint();
            } else {
                int r = 5;
                double dr;
                cntedge = 0;
                int x3 = evt.getX();
                int y3 = evt.getY();
                MyIterator it = edges.iterator();
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
                                                         probabs[cntedge].setText(pf.getText());
                                                         ((Frame) (b.getParent()).getParent()).dispose();
                                                     }
                                                 }
                            );
                            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
                            buttonPanel.add(bn, bnGbc);
                            bp.add(pf, bnGbc);
                            Point location = panel4.getLocationOnScreen();
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
                                                         probabs[cntedge].setText(pf.getText());
                                                         ((Frame) (b.getParent()).getParent()).dispose();
                                                     }
                                                 }
                            );
                            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
                            buttonPanel.add(bn, bnGbc);
                            bp.add(pf, bnGbc);
                            Point location = panel4.getLocationOnScreen();
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
                                                         probabs[cntedge].setText(pf.getText());
                                                         ((Frame) (b.getParent()).getParent()).dispose();
                                                     }
                                                 }
                            );
                            GridBagConstraints bnGbc = makegbc(2, 0, 1, 1, "center");
                            buttonPanel.add(bn, bnGbc);
                            bp.add(pf, bnGbc);
                            Point location = panel4.getLocationOnScreen();
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
                panel2.repaint();
            }
        }

        public void mousePressed(MouseEvent evt) {
            if (evt.isShiftDown() || probability_mode == true)
                return;
            valid = false;
            int x = evt.getX();
            int y = evt.getY();
            int cnt;
            MyIterator it = nodes.iterator();
            while (it.hasNext()) {
                NodePoint np = (NodePoint) it.next();
                int px = np.x;
                int py = np.y;
                px = px + 10;
                py = py + 10;
                int dx = x - px;
                int dy = y - py;
                if ((dx * dx + dy * dy) <= 100) {
                    cnt = nodes.indexOf(np);
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
            MyIterator it = nodes.iterator();
            while (it.hasNext()) {
                NodePoint np = (NodePoint) it.next();
                int px = np.x;
                int py = np.y;
                px = px + 10;
                py = py + 10;
                int dx = x - px;
                int dy = y - py;
                if ((dx * dx + dy * dy) <= 100 && el.node1 != nodes.indexOf(np)) {
                    cnt = nodes.indexOf(np);
                    el.x2 = px;
                    el.y2 = py;
                    el.node2 = cnt;
                    edges.add(el);
                    valid = true;

                    int lx = el.x2 - el.x1;
                    int ly = el.y2 - el.y1;
                    el.x0 = el.x2 - lx / 2;
                    el.y0 = el.y2 - ly / 2;

                    break;
                }
                //punkt (x,y) ist in dem Kreis(px, py)
            }
            panel2.repaint();
        }
    }

    class MyMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent evt) {
            if (valid && probability_mode == false) {
                int x = evt.getX();
                int y = evt.getY();
                el.x2 = x;
                el.y2 = y;
                panel2.repaint();
            }
        }
    }

    class NodePoint {
        int x, y;
        boolean k = false;
    }

    class EdgeLine {
        int x1, y1, x2, y2, node1, node2, x0, y0;
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


    //Zuverlaessigkeitsberechnung mit nur einem Algorithmus
    public void calculate_reliability_faster() {
        resultText = "Calculating...";
        result.setText(resultText);

        //Prüfen ob das Netz zusammenhängt
        boolean graphConnected;
        if (Con_check.check(graph) == -1) {
            //Graph ist zusammenhängend
            graphConnected = true;
        } else {
            graphConnected = false;
        }

        // Wahrscheinlichkeiten neu zuordnen. 
        /*br = (MyList)graph.getEdgelist().clone();
        br_fact = (MyList)graphfact.getEdgelist().clone();
		for(int k=0; k<probs.length;k++)
		{
		    Edge e = (Edge)br.get(k);
		    e.prob = probs[k];
		    Edge e2 = (Edge)br_fact.get(k);
		    e2.prob = probs[k];
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

    //Zuverlaessigkeitsberechnung mit 3 Algorithmen (ReNeT)
    public void calculate_reliability() {
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
        decomB.setEnabled(true);
        resilienceB.setEnabled(true);

        zer = new Zerleg(graph);
        zer.start();
        try {
            zer.join();
        } catch (InterruptedException e) {
        }

        for (int i = 0; i < probs.length; i++) {
            Edge e = (Edge) br.get(i);
            e.prob = probs[i];
        }
        //die IW jeder Kante zuweisen
        Util.getProbability(graph);
        if (graph.br.size() == 1) {
            Edge e = (Edge) graph.getEdgelist().get(0);
            if ((e.left_node.c_node == true) && (e.right_node.c_node == true))
                prob = ((Edge) graph.br.get(0)).prob;
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
                    Edge e = (Edge) it1.next();
                    pi = pi * e.prob;
                    s = s + "r" + String.valueOf(e.edge_no);
                }
                float pd = 1;
                MyIterator it2 = sd.iterator();
                while (it2.hasNext()) {
                    Edge e = (Edge) it2.next();
                    pd = pd * (1 - e.prob);
                    s = s + "u" + String.valueOf(e.edge_no);
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
        viewB.setEnabled(true);
        //decomB.setEnabled(false);	
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

        for (int i = 0; i < probs.length; i++) {
            Edge e = (Edge) br.get(i);
            e.prob = probs[i];
        }
        //die IW jeder Kante zuweisen
        Util.getProbability(graph);
        if (graph.br.size() == 1) {
            Edge e = (Edge) graph.getEdgelist().get(0);
            if ((e.left_node.c_node == true) && (e.right_node.c_node == true))
                prob = ((Edge) graph.br.get(0)).prob;
            else prob = 0;
            resultText = "The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob;
            if (lang == 'D')
                resultText = "Das reduzierte Netz enthaelt nur eine Kante. Die Zuverlaessigkeit des Netzes ist:\nP=" + prob;
        } else {
            prob = 0;

            int count = 0;
            MyIterator it = zer.hz.iterator();
            while (it.hasNext()) {
                MyList al = (MyList) it.next();
                MySet hs = (MySet) al.get(0);
                float p;
                p = getP(hs);

                for (int i = 1; i < al.size(); i++) {
                    MySet hs1 = (MySet) al.get(i);
                    if (hs1.isEmpty())
                        continue;
                    p = p * (1 - getP(hs1));


                }

                count++;
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

            decomB.setEnabled(true);
            resilienceB.setEnabled(true);

            //result.setText(resultText);
            viewB.setEnabled(true);
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
        decomB.setEnabled(true);
        resilienceB.setEnabled(true);
        viewB.setEnabled(true);

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
        total_nodes = nodes.size();

        //Anzahl der K-Knoten
        c_nodes = 0;

        // Sicherung der K-Knotenliste
        String cNodeList = "";
        for (int i = 0; i < total_nodes; i++) {
            NodePoint nodeSave = (NodePoint) nodes.get(i);
            if (nodeSave.k) {
                c_nodes++;
                cNodeList = cNodeList + "1";
            } else {
                cNodeList = cNodeList + "0";
            }
        }

        //Prüfen ob das Netz zusammenhängt
        if (Con_check.check(graph) == -1) {
            //Graph ist zusammenhängend
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
                NodePoint node1 = (NodePoint) nodes.get(i);
                //Node node1 = (Node)graph.nd.get(i); 

                // Dann auf true, falls K-Knoten
                if (d.contains(i)) {
                    node1.k = true;
                } else {
                    node1.k = false;
                }

                // Schreibe jeden Knoten neu in die Knotenliste.
                nodes.set(i, node1);
                //graph.nd.set(i, node1);

            }

            // Erhöhe pro Kombination den Zähler um 1.
            counter++;

            if (resilienceMode == 2) {
                graph = makeGraph();
            } else {
                graphfact = makeGraph();
            }


            // Wahrscheinlichkeiten neu zuordnen.
            br = (MyList) graph.getEdgelist().clone();
            br_fact = (MyList) graphfact.getEdgelist().clone();
            for (int i = 0; i < probs.length; i++) {
                Edge e = (Edge) br.get(i);
                e.prob = probs[i];
                Edge e2 = (Edge) br_fact.get(i);
                e2.prob = probs[i];
            }

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
            NodePoint nodeReset = (NodePoint) nodes.get(i);

            // Dann auf true, falls K-Knoten
            if (cNodeList.charAt(i) == '1') {
                nodeReset.k = true;
            } else {
                nodeReset.k = false;
            }

            // Schreibe jeden Knoten neu in die Knotenliste.
            nodes.set(i, nodeReset);
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
            int panelHeight = panel2.getHeight() - 20;
            int panelWidth = panel2.getWidth() - 20;

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

                nodes.add(node1);
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

                NodePoint startNodePoint = (NodePoint) nodes.get(edge1.node1);
                edge1.x1 = startNodePoint.x + 10;
                edge1.y1 = startNodePoint.y + 10;

                NodePoint endNodePoint = (NodePoint) nodes.get(edge1.node2);
                edge1.x2 = endNodePoint.x + 10;
                edge1.y2 = endNodePoint.y + 10;

                int labelX = edge1.x2 - edge1.x1;
                int labelY = edge1.y2 - edge1.y1;
                edge1.x0 = edge1.x2 - labelX / 2;
                edge1.y0 = edge1.y2 - labelY / 2;

                edges.add(edge1);
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
        Point location = panel2.getLocationOnScreen();
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
            writer.write("*Vertices " + nodes.size());


            int nodesDigitsCount = String.valueOf(nodes.size()).length();

            //Für jeden Knoten eine Zeile schreiben
            for (int i = 1; i < nodes.size() + 1; i++) {
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

                NodePoint node = (NodePoint) nodes.get(i - 1);
                double xCoordinate = (double) node.x;
                double yCoordinate = (double) node.y;

                if (xCoordinate < 5) {
                    xCoordinate = 5;
                }
                if (yCoordinate < 5) {
                    yCoordinate = 5;
                }

                xCoordinate = xCoordinate / (double) (panel2.getWidth());
                yCoordinate = yCoordinate / (double) (panel2.getHeight());

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
            for (int i = 0; i < edges.size(); i++) {
                writer.append(System.getProperty("line.separator"));
                EdgeLine edge = (EdgeLine) edges.get(i);
                String node1 = Integer.toString(edge.node1 + 1);
                String node2 = Integer.toString(edge.node2 + 1);

                while (node1.length() < Integer.toString(nodes.size()).length() + 1) {
                    node1 = " " + node1;
                }

                while (node2.length() < Integer.toString(nodes.size()).length() + 1) {
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
        Point location = panel2.getLocationOnScreen();
        frame.setLocation(location);
        frame.setVisible(true);
        frame.setSize(370, 200);
    }


    public BigDecimal startValue;
    public BigDecimal endValue;
    public BigDecimal stepSize;
    public int calculationSeriesMode = 0; //1 = resilience, 2 = reliability;
    public boolean onlyReliabilityFast;

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

        float[] probsSave = probs.clone();

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
                //Graph ist zusammenhängend
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
                for (int j = 0; j < probs.length; j++) {
                    probs[j] = reliability.floatValue();
                }

                if (calculationSeriesMode == 1) //Resilienz
                {
                    calculate_resilience();
                } else //Reliability
                {
                    // Wahrscheinlichkeiten neu zuordnen. (wird in calculate_resilience() auch gemacht)
                    br = (MyList) graph.getEdgelist().clone();
                    br_fact = (MyList) graphfact.getEdgelist().clone();
                    for (int k = 0; k < probs.length; k++) {
                        Edge e = (Edge) br.get(k);
                        e.prob = probs[k];
                        Edge e2 = (Edge) br_fact.get(k);
                        e2.prob = probs[k];
                    }

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
            // TODO Auto-generated catch block
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

        probs = probsSave.clone();


        calculationSeriesMode = 0;

        // Wahrscheinlichkeiten neu zuordnen.
        br = (MyList) graph.getEdgelist().clone();
        br_fact = (MyList) graphfact.getEdgelist().clone();
        for (int i = 0; i < probs.length; i++) {
            Edge e = (Edge) br.get(i);
            e.prob = probs[i];
            Edge e2 = (Edge) br_fact.get(i);
            e2.prob = probs[i];
        }

        resultText = "Calculation series finished. Please check your output file for the results.";
        result.setText(resultText);

    }


}
