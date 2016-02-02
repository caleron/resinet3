package com.resinet;

import com.resinet.util.Constants;
import com.resinet.util.GbcBuilder;
import com.resinet.util.Strings;
import com.resinet.util.Util;
import com.resinet.views.NetPanel;
import com.resinet.views.ProbabilitySpinner;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ResinetMockup implements Constants {
    MainframeController controller;

    private JPanel contentPane;
    private JMenuBar menuBar;

    private JPanel sidePanel;

    private NetPanel netPanel;

    private JSpinner edgeEndProbabilityBox;
    private JSpinner edgeProbabilityStepSizeBox;
    private JSpinner nodeEndProbabilityBox;
    private JSpinner nodeProbabilityStepSizeBox;
    private JSpinner sameReliabilityEdgeProbBox;
    private JSpinner sameReliabilityNodeProbBox;

    private JButton calcReliabilityBtn, calcResilienceBtn;
    private JProgressBar calculationProgressBar;

    private JTextPane outputField;

    private JLabel statusBarLabel;
    private JPanel singleReliabilitiesContainer;

    private GUI_STATES guiState;
    private JTabbedPane reliabilitiesTabbedPane;
    private JMenuItem tutorialMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem resetMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem closeMenuItem;
    private JCheckBox stepValuesCheckBox;
    private JCheckBox considerNodesBox;
    private JCheckBox considerEdgesBox;

    public ResinetMockup(MainframeController controller) {
        this.controller = controller;
        contentPane = new JPanel(new BorderLayout());

        //Windows Look-and-Feel setzen
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initMenu();
        initNetPanel();
        initSideBar();
        initStatusBar();

        setGuiState(GUI_STATES.ENTER_GRAPH, false);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainframeController controller = new MainframeController();

            ResinetMockup resinetMockup = new ResinetMockup(controller);
            controller.setMainFrame(resinetMockup);

            JFrame mainFrame = new JFrame("Mockup");
            mainFrame.addWindowListener(controller);

            mainFrame.setContentPane(resinetMockup.getContentPane());
            mainFrame.setJMenuBar(resinetMockup.getMenuBar());

            //Beim Klicken auf X das Programm beenden
            mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            //Minimale Größe setzen
            mainFrame.setMinimumSize(new Dimension(1000, 700));

            //Auf dem Bildschirm zentrieren und sichtbar machen
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        initFileMenu();
        initGenerateMenu();
        initLanguageMenu();
        initHelpMenu();
    }

    private void initHelpMenu() {
        //Hilfemenü aufbauen
        JMenu helpMenu = new JMenu(Strings.getLocalizedString("help"));

        tutorialMenuItem = new JMenuItem(Strings.getLocalizedString("start.tutorial"));
        tutorialMenuItem.addActionListener(controller);
        helpMenu.add(tutorialMenuItem);

        helpMenu.addSeparator();

        aboutMenuItem = new JMenuItem(Strings.getLocalizedString("about.resinet"));
        aboutMenuItem.addActionListener(controller);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);
    }

    private void initLanguageMenu() {
        //Sprachmenü aufbauen
        JMenu languageMenu = new JMenu(Strings.getLocalizedString("language"));

        for (Map.Entry lang : Strings.languages.entrySet()) {
            JMenuItem itm = new JCheckBoxMenuItem((String) lang.getValue());
            itm.addActionListener((e) -> {
                if (!Strings.setLanguageAndRestart(contentPane, (String) lang.getKey())) {
                    itm.setSelected(false);
                }
            });
            //Falls der Menüeintrag der aktuellen Sprache entspricht, abhaken und deaktivieren
            if (Strings.currentLocale.getLanguage().equals(new Locale((String) lang.getKey()).getLanguage())) {
                itm.setSelected(true);
                itm.setEnabled(false);
            }
            languageMenu.add(itm);
        }

        menuBar.add(languageMenu);
    }

    private void initGenerateMenu() {
        //Menu zum Graph Generieren aufbauen
        JMenu generateGraphMenu = new JMenu(Strings.getLocalizedString("generate.graph"));

        JMenuItem generateLineMenuItem = new JMenuItem(Strings.getLocalizedString("line"));
        generateGraphMenu.add(generateLineMenuItem);

        JMenuItem generateBridgeMenuItem = new JMenuItem(Strings.getLocalizedString("bridge.network"));
        generateGraphMenu.add(generateBridgeMenuItem);

        JMenuItem generateRingMenuItem = new JMenuItem(Strings.getLocalizedString("ring"));
        generateGraphMenu.add(generateRingMenuItem);

        JMenuItem generateTreeMenuItem = new JMenuItem(Strings.getLocalizedString("tree"));
        generateGraphMenu.add(generateTreeMenuItem);

        JMenuItem completeNetworkMenuItem = new JMenuItem(Strings.getLocalizedString("complete.graph"));
        generateGraphMenu.add(completeNetworkMenuItem);

        menuBar.add(generateGraphMenu);
    }

    private void initFileMenu() {
        //Menü "Datei" aufbauen
        JMenu fileMenu = new JMenu(Strings.getLocalizedString("file"));

        resetMenuItem = new JMenuItem(Strings.getLocalizedString("reset.network"));
        resetMenuItem.addActionListener(controller);
        //Tastenkombination Strg+R
        resetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        fileMenu.add(resetMenuItem);

        fileMenu.addSeparator();

        openMenuItem = new JMenuItem(Strings.getLocalizedString("load.network"));
        openMenuItem.addActionListener(controller);
        //Tastenkombination Strg+O
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        fileMenu.add(openMenuItem);

        saveMenuItem = new JMenuItem(Strings.getLocalizedString("save.network"));
        saveMenuItem.addActionListener(controller);
        //Tastenkombination Strg+S
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        closeMenuItem = new JMenuItem(Strings.getLocalizedString("close"));
        closeMenuItem.addActionListener(controller);
        //Alt+F4 abfangen
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);
    }

    private void initNetPanel() {
        netPanel = new NetPanel(controller);
        netPanel.setBackground(Color.white);

        JScrollPane graphScrollPane = new JScrollPane(netPanel);
        contentPane.add(graphScrollPane, BorderLayout.CENTER);
    }

    private void initSideBar() {
        sidePanel = new JPanel(new BorderLayout());

        initProbabilitiesPanel();
        initCalculatePanel();

        contentPane.add(sidePanel, BorderLayout.LINE_END);
    }

    private void initProbabilitiesPanel() {
        reliabilitiesTabbedPane = new JTabbedPane(JTabbedPane.TOP);

        initSingleReliabilitiesTab();

        initSameReliabilityTab();

        sidePanel.add(reliabilitiesTabbedPane, BorderLayout.CENTER);
    }

    private void initSameReliabilityTab() {
        JPanel sameReliabilityPanel = new JPanel(new GridBagLayout());
        sameReliabilityPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        reliabilitiesTabbedPane.addTab(Strings.getLocalizedString("same.reliabilities"), sameReliabilityPanel);

        //Kantenwahrscheinlichkeiten
        JLabel edgeProbLabel = new JLabel(Strings.getLocalizedString("reliability.of.every.edge"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(edgeProbLabel, GbcBuilder.build(0, 0, 1, 1, 1, 0).bottom(10).left());

        sameReliabilityEdgeProbBox = new ProbabilitySpinner();
        sameReliabilityPanel.add(sameReliabilityEdgeProbBox, GbcBuilder.build(1, 0, 1, 1, 1, 0).bottom(10));

        //Knotenwahrscheinlichkeiten
        JLabel nodeProbLabel = new JLabel(Strings.getLocalizedString("reliability.of.every.vertex"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(nodeProbLabel, GbcBuilder.build(0, 1, 1, 1, 1, 0).bottom(10).left());

        sameReliabilityNodeProbBox = new ProbabilitySpinner();
        sameReliabilityPanel.add(sameReliabilityNodeProbBox, GbcBuilder.build(1, 1, 1, 1, 1, 0).bottom(10));

        //Checkbox für Berechnungsserie
        stepValuesCheckBox = new JCheckBox(Strings.getLocalizedString("perform.calculation.series"));
        stepValuesCheckBox.addItemListener(controller);
        sameReliabilityPanel.add(stepValuesCheckBox, GbcBuilder.build(0, 2, 2, 1, 1, 0).bottom(10).left());

        //Eingabefelder für Berechnungsserie
        JLabel edgeProbEndValueLbl = new JLabel(Strings.getLocalizedString("edge.end.value"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(edgeProbEndValueLbl, GbcBuilder.build(0, 3, 1, 1, 1, 0).bottom(10).left());

        edgeEndProbabilityBox = new ProbabilitySpinner();
        edgeEndProbabilityBox.setEnabled(false);
        sameReliabilityPanel.add(edgeEndProbabilityBox, GbcBuilder.build(1, 3, 1, 1, 1, 0).bottom(10));

        JLabel edgeStepSizeLbl = new JLabel(Strings.getLocalizedString("edge.step.size"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(edgeStepSizeLbl, GbcBuilder.build(0, 4, 1, 1, 1, 0).bottom(10).left());

        edgeProbabilityStepSizeBox = new ProbabilitySpinner();
        edgeProbabilityStepSizeBox.setEnabled(false);
        sameReliabilityPanel.add(edgeProbabilityStepSizeBox, GbcBuilder.build(1, 4, 1, 1, 1, 0).bottom(10));

        JLabel nodeEndValueLbl = new JLabel(Strings.getLocalizedString("vertex.end.value"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(nodeEndValueLbl, GbcBuilder.build(0, 5, 1, 1, 1, 0).bottom(10).left());

        nodeEndProbabilityBox = new ProbabilitySpinner();
        nodeEndProbabilityBox.setEnabled(false);
        sameReliabilityPanel.add(nodeEndProbabilityBox, GbcBuilder.build(1, 5, 1, 1, 1, 0).bottom(10));

        JLabel nodeStepSizeLbl = new JLabel(Strings.getLocalizedString("vertex.step.size"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(nodeStepSizeLbl, GbcBuilder.build(0, 6, 1, 1, 1, 0).bottom(10).left());

        nodeProbabilityStepSizeBox = new ProbabilitySpinner();
        nodeProbabilityStepSizeBox.setEnabled(false);
        sameReliabilityPanel.add(nodeProbabilityStepSizeBox, GbcBuilder.build(1, 6, 1, 1, 1, 0).bottom(10));
    }

    private void initSingleReliabilitiesTab() {
        JPanel singleReliabilitiesPanel = new JPanel(new BorderLayout());
        reliabilitiesTabbedPane.addTab(Strings.getLocalizedString("different.reliabilities"), singleReliabilitiesPanel);

        JPanel considerComponentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        considerNodesBox = new JCheckBox(Strings.getLocalizedString("consider.vertex.reliabilities"));
        considerNodesBox.addItemListener(controller);
        considerComponentsPanel.add(considerNodesBox);
        considerNodesBox.setSelected(true);

        considerEdgesBox = new JCheckBox(Strings.getLocalizedString("consider.edge.reliabilities"));
        considerEdgesBox.addItemListener(controller);
        considerComponentsPanel.add(considerEdgesBox);
        considerEdgesBox.setSelected(true);

        singleReliabilitiesContainer = new JPanel(new GridLayout(0, 4));
        JScrollPane singleReliabilitiesScrollPane = new JScrollPane(singleReliabilitiesContainer);
        singleReliabilitiesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        singleReliabilitiesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        singleReliabilitiesScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        singleReliabilitiesScrollPane.getHorizontalScrollBar().setUnitIncrement(10);

        singleReliabilitiesPanel.add(considerComponentsPanel, BorderLayout.PAGE_START);
        singleReliabilitiesPanel.add(singleReliabilitiesScrollPane, BorderLayout.CENTER);
    }

    private void initCalculatePanel() {
        JPanel calculatePanel = new JPanel();

        calculatePanel.setBorder(BorderFactory.createTitledBorder(Strings.getLocalizedString("start.calculation")));
        calculatePanel.setLayout(new GridBagLayout());

        calcReliabilityBtn = new JButton(Strings.getLocalizedString("calculate.reliability"));
        calcReliabilityBtn.addActionListener(controller);
        calculatePanel.add(calcReliabilityBtn, GbcBuilder.build(0, 0, 1, 1, 1, 0).fillBoth());

        calcResilienceBtn = new JButton(Strings.getLocalizedString("calculate.resilience"));
        calcResilienceBtn.addActionListener(controller);
        calculatePanel.add(calcResilienceBtn, GbcBuilder.build(0, 1, 1, 1, 1, 0).fillBoth());

        calculationProgressBar = new JProgressBar();
        calculationProgressBar.setStringPainted(true);
        calculatePanel.add(calculationProgressBar, GbcBuilder.build(0, 2, 2, 1, 1, 0).fillBoth());

        outputField = new JTextPane();
        outputField.setEditable(false);
        outputField.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        outputField.setBorder(null);
        outputField.setBackground(null);
        outputField.setFont(new Font("Tahoma", Font.PLAIN, 13));
        outputField.setText("The Reliability of the network is:\n0.3885443333");
        calculatePanel.add(outputField, GbcBuilder.build(0, 3, 1, 1, 1, 0).fillBoth().vertical(10));

        sidePanel.add(calculatePanel, BorderLayout.PAGE_END);
    }


    private void initStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

        statusBarLabel = new JLabel("Status", SwingConstants.LEFT);
        statusBar.add(statusBarLabel);
        contentPane.add(statusBar, BorderLayout.PAGE_END);
    }

    /**
     * Setzt den aktuellen Status der Benutzeroberfläche, aktiviert und deaktiviert also Bedienelemente nach Bedarf
     *
     * @param state Der gewünschte Status
     */
    public void setGuiState(GUI_STATES state, boolean force) {
        if (guiState == state && !force)
            return;

        guiState = state;

        calcReliabilityBtn.setEnabled(state != GUI_STATES.CALCULATION_RUNNING);
        calcResilienceBtn.setEnabled(state != GUI_STATES.CALCULATION_RUNNING);

        reliabilitiesTabbedPane.setEnabled(state != GUI_STATES.CALCULATION_RUNNING);

        boolean reliabilityBoxesEnabled = (state != GUI_STATES.CALCULATION_RUNNING);
        Util.setChildrenEnabled(reliabilitiesTabbedPane, reliabilityBoxesEnabled);

        if (reliabilityBoxesEnabled && !stepValuesCheckBox.isSelected()) {
            edgeProbabilityStepSizeBox.setEnabled(false);
            nodeProbabilityStepSizeBox.setEnabled(false);
            edgeEndProbabilityBox.setEnabled(false);
            nodeEndProbabilityBox.setEnabled(false);
        }
    }

    public RELIABILITY_MODES getReliabilityMode() {
        if (reliabilitiesTabbedPane.getSelectedIndex() == 0) {
            return RELIABILITY_MODES.SINGLE;
        } else {
            return RELIABILITY_MODES.SAME;
        }
    }

    public void setResultText(String text) {
        outputField.setText(text);
    }

    public void setStatusBarText(String text) {
        statusBarLabel.setText(text);
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public JCheckBox getStepValuesCheckBox() {
        return stepValuesCheckBox;
    }

    public JCheckBox getConsiderNodesBox() {
        return considerNodesBox;
    }

    public JCheckBox getConsiderEdgesBox() {
        return considerEdgesBox;
    }

    public JSpinner getEdgeEndProbabilityBox() {
        return edgeEndProbabilityBox;
    }

    public JSpinner getEdgeProbabilityStepSizeBox() {
        return edgeProbabilityStepSizeBox;
    }

    public JSpinner getNodeEndProbabilityBox() {
        return nodeEndProbabilityBox;
    }

    public JSpinner getNodeProbabilityStepSizeBox() {
        return nodeProbabilityStepSizeBox;
    }

    public JSpinner getSameReliabilityEdgeProbBox() {
        return sameReliabilityEdgeProbBox;
    }

    public JSpinner getSameReliabilityNodeProbBox() {
        return sameReliabilityNodeProbBox;
    }

    public JButton getCalcReliabilityBtn() {
        return calcReliabilityBtn;
    }

    public JButton getCalcResilienceBtn() {
        return calcResilienceBtn;
    }

    public JProgressBar getCalculationProgressBar() {
        return calculationProgressBar;
    }

    public JMenuItem getTutorialMenuItem() {
        return tutorialMenuItem;
    }

    public JMenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public JMenuItem getResetMenuItem() {
        return resetMenuItem;
    }

    public JMenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public JMenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public JMenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public JPanel getSingleReliabilitiesContainer() {
        return singleReliabilitiesContainer;
    }

    public NetPanel getNetPanel() {
        return netPanel;
    }
}
