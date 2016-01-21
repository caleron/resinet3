package com.resinet.util;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Klasse zur Lokalisation, die Strings für die UI liefert.
 */
public class Strings {
    static ResourceBundle bundle;
    static Preferences preferences;
    public static Locale currentLocale;

    /**
     * Statische Initialisierung der Sprache beim Laden des Programms
     */
    static {
        //Standard-Sprache setzen (funktioniert auch ohne)
        Locale.setDefault(new Locale("en"));

        //Eingestellte Sprache aus Einstellungen laden
        preferences = Preferences.userNodeForPackage(com.resinet.Resinet3.class);
        String language = preferences.get("language", "en");

        currentLocale = new Locale(language);
        //Strings laden
        bundle = ResourceBundle.getBundle("strings", currentLocale);
    }

    /**
     * Gibt einen String aus den Ressourcen für die aktuelle Sprache zurück
     *
     * @param key Der Schlüssel
     * @return Lokalisierter String
     */
    public static String getLocalizedString(String key) {
        return bundle.getString(key);
    }

    /**
     * Setzt die Sprache und startet das Programm neu. Zeigt vorher einen Dialog an, dass dadurch alle Eingaben gelöscht
     * sind
     *
     * @param language Die neue Sprache als Kürzel, etwa "de", "en", ...
     */
    public static void setLanguageAndRestart(Component parentComponent, String language) {
        int result = JOptionPane.showConfirmDialog(parentComponent, Strings.getLocalizedString("restart.for.language.dialog"),
                Strings.getLocalizedString("warning"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            preferences.put("language", language);
            Util.restartApplication();
        }
    }
}
