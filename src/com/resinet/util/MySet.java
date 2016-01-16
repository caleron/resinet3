package com.resinet.util;/* MySet.java */
/* Meine Implementierung fuer HashSet mit pure java1.0 */

import java.util.Hashtable;
import java.util.Enumeration;

public class MySet extends Hashtable {
    public void add(Object o) {
        String s = "Y";
        super.put(o, s);
    }

    public MyIterator iterator() {
        MyIterator it;
        Enumeration e = super.keys();
        it = new MyIterator(e);
        return it;
    }

    public void addAll(MySet s) {
        Enumeration e = s.keys();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            String str = "Y";
            super.put(o, str);
        }
    }

    public void removeAll(MySet s) {
        Enumeration e = s.keys();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            super.remove(o);
        }
    }

    public void retainAll(MySet s) {
        Enumeration e = super.keys();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (!s.containsKey(o)) {
                super.remove(o);
            }
        }
    }

    public boolean contains(Object o) {
        return super.containsKey(o);
    }

    public boolean containsAll(MySet s) {
        boolean b = true;
        Enumeration e = s.keys();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (!super.containsKey(o)) {
                b = false;
                break;
            }
        }
        return b;
    }
}
