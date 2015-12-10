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

    public MyList(MySet set) {
        v = new Vector();
        MyIterator it = set.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            v.addElement(o);
        }
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

    public boolean isEmpty() {
        return v.isEmpty();
    }

    public Object remove(int i) {
        Object o = v.elementAt(i);
        v.removeElementAt(i);
        return o;
    }

    public boolean remove(Object o) {
        return v.removeElement(o);
    }

    public Object set(int i, Object o) {
        Object o1 = v.elementAt(i);
        v.setElementAt(o, i);
        return o1;
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
