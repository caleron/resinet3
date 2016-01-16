package com.resinet.util;/* MyList.java */
/* Meine Implementierung fuer List mit pure java1.0 */

import java.util.Enumeration;
import java.util.Vector;
import java.io.Serializable;

public class MyList implements Cloneable, Serializable {
    private Vector v;

    public MyList() {
        v = new Vector();
    }

    public MyIterator iterator() {
        Enumeration e = v.elements();
        return new MyIterator(e);
    }

    public void add(int i, Object o) {
        v.insertElementAt(o, i);
    }

    public void add(Object o) {
        v.addElement(o);
    }

    public Object get(int i) {
        return v.elementAt(i);
    }

    public int indexOf(Object o) {
        return v.indexOf(o);
    }

    public int size() {
        return v.size();
    }

    public void remove(Object o) {
        v.removeElement(o);
    }

    public void set(int i, Object o) {
        v.setElementAt(o, i);
    }

    public void clear() {
        v.removeAllElements();
    }

    public boolean contains(Object o) {
        return v.contains(o);
    }

    public Object clone() {
        MyList l = null;
        try {
            l = (MyList) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        l.v = (Vector) v.clone();
        return l;
    }
}
