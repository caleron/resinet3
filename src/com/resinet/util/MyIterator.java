package com.resinet.util;/* MyIterator.java */

import java.util.Enumeration;

public class MyIterator {
    private Enumeration e;

    public MyIterator(Enumeration e) {
        this.e = e;
    }

    public boolean hasNext() {
        return e.hasMoreElements();
    }

    public Object next() {
        return e.nextElement();
    }
}
