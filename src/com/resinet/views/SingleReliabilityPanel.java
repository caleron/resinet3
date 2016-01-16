package com.resinet.views;

import javax.swing.*;
import java.awt.*;

/**
 * Stellt ein Panel für eine Einzelkomponentenwahrscheinlichkeit mit Label und Textfeld dar
 */
public class SingleReliabilityPanel  extends JPanel{

    JTextField textField;
    boolean isNode;

    public SingleReliabilityPanel(boolean isNode, int number) {
        super();
        this.isNode = isNode;

        String text;
        String type = isNode ? "Vertex " : "Edge ";
        text = type + number;

        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        textField = new JTextField(10);
        textField.setBackground(Color.white);

        setLayout(new FlowLayout());
        setPreferredSize(new Dimension(160, 30));
        setDoubleBuffered(true);
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
