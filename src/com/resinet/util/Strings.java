package com.resinet.util;

import java.util.ResourceBundle;

/**
 * Klasse zur Lokalisation, die Strings für die UI liefert.
 */
public class Strings {
    static ResourceBundle bundle;

    static {
        bundle = ResourceBundle.getBundle("strings");
    }

    public static String getLocalizedString(String key) {
        return bundle.getString(key);
    }

}
