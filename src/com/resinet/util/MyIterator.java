package com.resinet.util;/* MyIterator.java */

import java.util.Enumeration;

public class MyIterator {
    Enumeration e;

    public MyIterator(Enumeration e) {
        this.e = e;
    }

    public boolean hasNext() {
        boolean b = e.hasMoreElements();
        return b;
    }

    public Object next() {
        Object o = e.nextElement();
        return o;
    }
}
