package com.resinet.views;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Erweitert die Klasse JSpinner. Wird auf das Wahrscheinlichkeitsformat eingestellt, sodass die Eingaben nicht mehr
 * validiert werden müssen.
 */
public class ProbabilitySpinner extends JSpinner {

    /**
     * Die Spaltenzahl des Eingabefeldes
     */
    private static final int COLUMNS = 15;

    /**
     * Erstellt einen neuen JSpinner für Wahrscheinlichkeiten
     */
    public ProbabilitySpinner() {
        super();
        setModel(new SpinnerNumberModel(1, 0, 1, 0.01));

        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();
        editor.getTextField().setColumns(COLUMNS);

        //Anzahl Nachkommastellen auf 50 festlegen
        DecimalFormat format = editor.getFormat();
        format.setMaximumFractionDigits(50);
        format.setMinimumFractionDigits(0);

        textField.addFocusListener(new MyFocusAdapter(textField));

        textField.addKeyListener(new MyKeyAdapter(textField));
    }

    public BigDecimal getBigDecimalValue() {
        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();

        return new BigDecimal(textField.getText());
    }

    /**
     * Sorgt dafür, dass der Text markiert wird, wenn der JSpinner fokussiert wird
     */
    private static class MyFocusAdapter extends FocusAdapter {
        private final JFormattedTextField textField;

        public MyFocusAdapter(JFormattedTextField textField) {
            this.textField = textField;
        }

        @Override
        public void focusGained(FocusEvent e) {
            SwingUtilities.invokeLater(textField::selectAll);
        }
    }

    /**
     * Ersetzt automatisch ein eingegebenes Komma durch einen Punkt und lässt das Textfeld nicht auf weitere Kommata
     * oder Punkte reagieren, wenn bereits eines im Textfeld ist.
     */
    private static class MyKeyAdapter extends KeyAdapter {
        private final JFormattedTextField textField;

        public MyKeyAdapter(JFormattedTextField textField) {
            this.textField = textField;
        }

        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if ((c == ',' || c == '.') && textField.getText().indexOf('.') >= 0) {
                //Konsumiert den Tastenschlag, also damit nichts weiteres passiert
                e.consume();
            } else if (c == ',')
                e.setKeyChar('.');
        }
    }

}
