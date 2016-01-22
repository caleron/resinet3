package com.resinet.views;

import javax.swing.*;
import java.awt.*;

/**
 * Stellt ein Panel für eine Einzelkomponentenwahrscheinlichkeit mit Label und Textfeld dar
 */
public class SingleReliabilityPanel extends JPanel {

    private final JTextField textField;
    private final boolean isNode;

    /**
     * Die bevorzugte Breite
     */
    public static final int PREF_WIDTH = 160;
    /**
     * Die bevorzugte Höhe
     */
    private static final int PREF_HEIGHT = 30;

    /**
     * Konstruktor
     *
     * @param isNode True wenn ein Knoten repräsentiert wird, sonst false
     * @param number Die Nummer des Elements
     */
    public SingleReliabilityPanel(boolean isNode, int number) {
        super();
        this.isNode = isNode;

        String text;
        String type = isNode ? "Vertex " : "Edge ";
        text = type + number;

        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        textField = new JTextField(10);
        textField.setBackground(Color.white);

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        //setDoubleBuffered(true);
        add(label);
        add(textField);
    }

    public boolean isNode() {
        return isNode;
    }

    public JTextField getTextField() {
        return textField;
    }
}
