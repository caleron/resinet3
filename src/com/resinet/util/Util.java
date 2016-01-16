package com.resinet.util;/* com.resinet.util.Util.java */

import java.io.*;
import java.math.BigInteger;
//import java.util.*;

public class Util {
    public static Object serialClone(Object o)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);

        os.writeObject(o);
        os.flush();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream is = new ObjectInputStream(in);
        Object ret = is.readObject();
        is.close();
        os.close();
        return ret;
    }


    public static BigInteger binomial(long n, long k) {
//    	long start = new Date().getTime();
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
        //System.out.println(binomialCoefficient);
//		long runningTime = new Date().getTime() - start;
//        System.out.println("Laufzeit: " + runningTime);
        return binomialCoefficient;
    }
}
