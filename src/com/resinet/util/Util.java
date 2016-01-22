package com.resinet.util;/* com.resinet.util.Util.java */

import com.resinet.Resinet3;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;

public class Util {
    /**
     * Führt eine tiefe Kopie mittels Serialisierung durch
     *
     * @param obj Das zu serialisierende Objekt
     * @return Eine tiefe Kopie des Objekts
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object serialClone(Object obj)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);

        os.writeObject(obj);
        os.flush();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream is = new ObjectInputStream(in);
        Object ret = is.readObject();
        is.close();
        os.close();
        return ret;
    }


    /**
     * Errechnet den Binomialkoeffizienten
     *
     * @param n Zahl n
     * @param k Zahl k
     * @return Binomialkoeffizient
     */
    public static BigInteger binomial(int n, int k) {
        BigInteger binomialCoefficient = BigInteger.ONE;

        // Nutze die Symmetrie des Pascalschen Dreiecks um den Aufwand zu minimieren.
        if (k > n / 2) {
            k = n - k;
        }

        if (k > n) {
            binomialCoefficient = BigInteger.ZERO;
        } else if (k == 0 || n == k) {
            binomialCoefficient = BigInteger.ONE;
        } else if (k == 1 || k == n - 1) {
            binomialCoefficient = BigInteger.valueOf(n);
        } else {
            for (long i = 1; i <= k; i++) {
                binomialCoefficient = binomialCoefficient.multiply(BigInteger.valueOf(n - k + i));
                binomialCoefficient = binomialCoefficient.divide(BigInteger.valueOf(i));
            }
        }
        return binomialCoefficient;
    }

    /**
     * Diese Methode dient, dazu, das Programm neu zu starten.
     * <p>
     * Funktioniert noch nicht bei exe-Dateien.
     * <p>
     * Quelle: http://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application
     */
    public static void restartApplication() {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        final File currentCodePath;
        try {
            currentCodePath = new File(Resinet3.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            return;
        }

        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);

        //wenns keine JAR-Datei ist, vorerst abbrechen, in Zukunft für exe-Release dies auch dann funktionieren lassen
        if (currentCodePath.getName().endsWith(".jar")) {
            //Befehl bauen: java -jar application.jar
            command.add("-jar");
            command.add(currentCodePath.getPath());
        } else {
            //Programm liegt als .class vor (während Debugging höchstwahrscheinlich)
            command.add("-cp");
            command.add(currentCodePath.getPath());
            command.add("com.resinet.Resinet3");
        }

        final ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
            System.exit(0);
        } catch (IOException ignored) {
        }
    }
}
