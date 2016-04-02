package com.resinet.views;

import com.resinet.model.GraphWrapper;
import com.resinet.util.GbcBuilder;
import com.resinet.util.GraphGenerator;
import com.resinet.util.GraphGeneratorListener;
import com.resinet.util.Strings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

public class GenerateGraphFrame implements ItemListener, KeyListener {

    private JFrame frame;
    private JPanel cardPanel;
    private static final String BRIDGE = "Bridge";

    private static final String LINE = "Line";
    private static final String RING = "Ring";
    private static final String TREE = "Tree";
    private static final String COMPLETE_GRAPH = "Complete Graph";

    private static final String BRIDGE_PANEL_TAG = "bridgePanel";
    private static final String NODE_COUNT_PANEL_TAG = "nodeCountPanel";
    private static final String TREE_PANEL_TAG = "treePanel";

    private static final ArrayList<String> types;

    private final GraphGeneratorListener listener;

    static {
        types = new ArrayList<>(Arrays.asList(LINE, BRIDGE, RING, TREE, COMPLETE_GRAPH));
    }

    private JComboBox<String> typeBox;
    private NumberSpinner nodeCountBox;
    private NumberSpinner treeHeightBox;
    private NumberSpinner treeInnerDescendantCountBox;
    private NumberSpinner treeLeafCountBox;

    private GenerateGraphFrame(GraphGeneratorListener listener) {
        this.listener = listener;
        frame = new JFrame(Strings.getLocalizedString("generate.graph"));

        frame.setLayout(new BorderLayout());

        initTypeSelectionBar();
        initCards();
        initButtonBar();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initButtonBar() {
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener((e) -> generateButtonClick());
        buttonBar.add(generateButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> closeFrame());
        buttonBar.add(cancelButton);

        frame.add(buttonBar, BorderLayout.PAGE_END);
    }

    private void initTypeSelectionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Type of graph:");
        panel.add(label);

        typeBox = new JComboBox<>(types.toArray(new String[types.size()]));
        typeBox.addItemListener(this);
        typeBox.addKeyListener(this);
        panel.add(typeBox);

        frame.add(panel, BorderLayout.PAGE_START);
    }

    private void initCards() {
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        frame.add(cardPanel, BorderLayout.CENTER);

        initNodeCountPanel();

        initBrigdePanel();

        initTreePanel();
    }

    private void initNodeCountPanel() {
        JPanel linePanel = new JPanel();

        JLabel label = new JLabel("Node count:");
        linePanel.add(label);

        nodeCountBox = new NumberSpinner(5, 2, 10000);
        nodeCountBox.addKeyListener(this);
        linePanel.add(nodeCountBox);

        cardPanel.add(linePanel, NODE_COUNT_PANEL_TAG);
    }

    private void initTreePanel() {
        JPanel treePanel = new JPanel(new GridBagLayout());

        JLabel heightLabel = new JLabel("Height:");
        treePanel.add(heightLabel, GbcBuilder.build(0, 0).left());

        JLabel descendantCountLabel = new JLabel("Descendant count of inner nodes:");
        treePanel.add(descendantCountLabel, GbcBuilder.build(0, 1).left());

        JLabel leafCountLabel = new JLabel("Descendant leaf count:");
        treePanel.add(leafCountLabel, GbcBuilder.build(0, 2).left());

        treeHeightBox = new NumberSpinner(3, 2, 50);
        treeHeightBox.addKeyListener(this);
        treePanel.add(treeHeightBox, GbcBuilder.build(1, 0).fillBoth().weightx(1));

        treeInnerDescendantCountBox = new NumberSpinner(2, 2, 20);
        treeInnerDescendantCountBox.addKeyListener(this);
        treePanel.add(treeInnerDescendantCountBox, GbcBuilder.build(1, 1).fillBoth().weightx(1));

        treeLeafCountBox = new NumberSpinner(2, 1, 20);
        treeLeafCountBox.addKeyListener(this);
        treePanel.add(treeLeafCountBox, GbcBuilder.build(1, 2).fillBoth().weightx(1));

        cardPanel.add(treePanel, TREE_PANEL_TAG);
    }

    private void initBrigdePanel() {
        JPanel brigdePanel = new JPanel();
        cardPanel.add(brigdePanel, BRIDGE_PANEL_TAG);
    }

    public static void show(GraphGeneratorListener listener) {
        new GenerateGraphFrame(listener);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, (String) e.getItem());

        switch (types.indexOf(e.getItem())) {
            case 0:
            case 2:
            case 4:
                cardLayout.show(cardPanel, NODE_COUNT_PANEL_TAG);
                break;

            case 1:
                cardLayout.show(cardPanel, BRIDGE_PANEL_TAG);
                break;
            case 3:
                cardLayout.show(cardPanel, TREE_PANEL_TAG);
                break;
        }
    }

    private void generateButtonClick() {
        GraphWrapper wrapper = null;
        switch (typeBox.getSelectedIndex()) {
            case 0: //Linie
                wrapper = GraphGenerator.generateLine(nodeCountBox.getIntValue());
                break;
            case 1: //Brücke
                wrapper = GraphGenerator.generateBridge();
                break;
            case 2: //Ring
                wrapper = GraphGenerator.generateRing(nodeCountBox.getIntValue());
                break;
            case 3: //Baum
                wrapper = GraphGenerator.generateTree(treeHeightBox.getIntValue(),
                        treeInnerDescendantCountBox.getIntValue(),
                        treeLeafCountBox.getIntValue());
                break;
            case 4: //Vollständiger Graph
                wrapper = GraphGenerator.generateComplete(nodeCountBox.getIntValue());
                break;
        }
        if (wrapper != null) {
            listener.graphGenerated(wrapper);
            closeFrame();
        }
    }

    private void closeFrame() {
        frame.setVisible(false);
        frame.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            generateButtonClick();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            closeFrame();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
