package com.resinet;/* MyList.java */
/* Meine Implementierung fuer List mit pure java1.0 */

import java.util.Enumeration;
import java.util.Vector;
import java.io.Serializable;

public class MyList implements Cloneable, Serializable {
    Vector v;

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
        MyIterator it = new MyIterator(e);
        return it;
    }

    public void add(int i, Object o) {
        v.insertElementAt(o, i);
    }

    public boolean add(Object o) {
        v.addElement(o);
        return true;
    }

    public Object get(int i) {
        Object o = v.elementAt(i);
        return o;
    }

    public int indexOf(Object o) {
        int i = v.indexOf(o);
        return i;
    }

    public int size() {
        int i = v.size();
        return i;
    }

    public boolean isEmpty() {
        boolean b = v.isEmpty();
        return b;
    }

    public Object remove(int i) {
        Object o = v.elementAt(i);
        v.removeElementAt(i);
        return o;
    }

    public boolean remove(Object o) {
        boolean b = v.removeElement(o);
        return b;
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
