/* MySet.java */
/* Meine Implementierung fuer HashSet mit pure java1.0 */

import java.util.Hashtable;
import java.util.Enumeration;

public class MySet extends Hashtable {
    public boolean add(Object o) {
        String s = "Y";
        boolean not = true;
        Object o1;
        o1 = super.put(o, s);
        if (o1 != null)
            not = false;

        return not;
    }

    public MyIterator iterator() {
        MyIterator it;
        Enumeration e = super.keys();
        it = new MyIterator(e);
        return it;
    }

    public boolean addAll(MySet s) {
        boolean b = false;
        Enumeration e = s.keys();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            String str = "Y";
            Object o1;
            o1 = super.put(o, str);
            if (o1 == null)
                b = true;
        }
        return b;
    }

    public boolean removeAll(MySet s) {
        boolean b = false;
        Enumeration e = s.keys();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            Object o1;
            o1 = super.remove(o);
            if (o1 != null)
                b = true;
        }
        return b;
    }

    public boolean retainAll(MySet s) {
        boolean b = false;
        Enumeration e = super.keys();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            if (!s.containsKey(o)) {
                super.remove(o);
                b = true;
            }
        }
        return b;
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
