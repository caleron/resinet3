package com.resinet.algorithms;/* Zerleg.java */


import com.resinet.util.MyIterator;
import com.resinet.util.MyList;
import com.resinet.util.MySet;
import com.resinet.model.Graph;
import com.resinet.model.ResultA;

public class Zerleg extends Thread {
    Graph g;
    Tree ktrees;
    MyList trs;

    public MySet az;
    public MySet hz;

    AZerleg azer;
    HZerleg hzer;

    int azer_i, hzer_i;

    public Zerleg(Graph graph) {
        g = graph;
        ktrees = new Tree(g);
        trs = ktrees.trs;
        azer = new AZerleg();
        hzer = new HZerleg();
        az = new MySet();
        hz = new MySet();
    }

    public void run() {
        ktrees.start();
        azer.start();
        hzer.start();

        try {
            ktrees.join();
            azer.join();
            hzer.join();
        } catch (Exception e) {
        }
    }

    class AZerleg extends Thread {

        public void run() {
            aZerlegen();
        }

        private void aZerlegen() {
            if (!ktrees.dead) {
                synchronized (trs) {
                    try {
                        trs.wait(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }

            for (int i = 0; i < trs.size(); i++) {
                MySet setK = (MySet) trs.get(i);
                MySet setD = new MySet();
                aZer(setK, setD, i, 0);

                if (i == trs.size() - 1 && !ktrees.dead) {
                    synchronized (trs) {
                        try {
                            trs.wait();
                        } catch (InterruptedException e) {
                            System.out.println(e.toString());
                        }
                    }
                }
            }

        }

        private void aZer(MySet superI, MySet superD, int k, int i) {
            MySet setI = (MySet) superI.clone();
            MySet setD = (MySet) superD.clone();

            if (i == k) {
                ResultA ra = new ResultA(setI, setD);
                azer_i++;
                az.add(ra);
            } else {
                MySet setIi = (MySet) trs.get(i);

                MySet cut = (MySet) setIi.clone();
                cut.retainAll(setD);

                // falls (Ii /\ D) != {}
                if (!cut.isEmpty())
                    aZer(setI, setD, k, i + 1);
                else {
                    MySet rest = (MySet) setIi.clone();
                    rest.removeAll(setI);
                    rest.removeAll(setD);
                    // Ii-(I\/D)

                    MyIterator it = rest.iterator();

                    while (it.hasNext()) {
                        Object ob = it.next();
                        MySet setD2 = (MySet) setD.clone();
                        setD2.add(ob);
                        //setD2 = setD + ob, setD soll unveraendert bleiben
                        aZer(setI, setD2, k, i + 1);
                        setI.add(ob);
                    }
                }
            }
        }

    }


    class HZerleg extends Thread {
        public void run() {
            hZerlegen();
        }

        private void hZerlegen() {
            if (!ktrees.dead) {
                synchronized (trs) {
                    try {
                        trs.wait(1000);
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }
                }
            }

            for (int i = 1; i <= trs.size(); i++) {
                MyList listK = new MyList();
                MySet setI0 = (MySet) trs.get(i - 1);
                listK.add(setI0);
                for (int c = 1; c <= i; c++) {
                    MySet temp = new MySet();
                    listK.add(temp);
                }
                //insgesamt i+1 Elemente, setI0 = I(0)

                hZer(listK, i, 1);

                if (i == trs.size() && !ktrees.dead) {
                    synchronized (trs) {
                        try {
                            trs.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private void hZer(MyList listK, int k, int i) {
            if (i == k) {
                hzer_i++;
                //listK.remove(k);
                MyList al = cloneList(listK);
                hz.add(al);
            } else {
                MySet setIi = (MySet) trs.get(i - 1);
                boolean disjoint = false;

                for (int c = 1; c < i; c++) {
                    MySet setIl = (MySet) listK.get(c);
                    if (setIl.isEmpty())
                        continue;
                    if (setIi.containsAll(setIl)) {
                        disjoint = true;
                        break;
                    }
                }
                //falls es ein l mit 0<l<i und {}<I(l)<=Ii gibt, d.h. Disjunktheit

                if (disjoint) {
                    hZer(listK, k, i + 1);
                } else {
                    MyList listHI = cloneList(listK);
                    //fuer alle l=0 bis k HI(l)=I(l)

                    for (int l = 1; l < i; l++) {
                        MySet setIl = (MySet) listK.get(l);
                        MySet setHIl = (MySet) setIl.clone();
                        setHIl.retainAll(setIi);
                        //HI(l)=I(l)/\Ii

                        if (!setHIl.isEmpty()) {
                            listHI.set(l, setHIl);
                            hZer(listHI, k, i + 1);
                        }
                        setHIl = (MySet) setIl.clone();
                        setHIl.removeAll(setIi);
                        listHI.set(l, setHIl);
                        //HI(l)=I(l)-Ii

                        MySet cut = (MySet) setIl.clone();
                        cut.retainAll(setIi);
                        MySet setHI0 = (MySet) listHI.get(0);
                        setHI0.addAll(cut);
                        listHI.set(0, setHI0);
                        // HI(0)=HI(0)\/(I(l)/\Ii)
                    }

                    MySet joint = new MySet();
                    for (int c = 0; c < i; c++) {
                        MySet temp = (MySet) listK.get(c);
                        joint.addAll(temp);
                    }
                    MySet setHIi = (MySet) setIi.clone();
                    setHIi.removeAll(joint);
                    listHI.set(i, setHIi);
                    //HI(i) = Ii-\/I(l)

                    if (!setHIi.isEmpty()) {
                        for (int l = 1; l < i; l++) {
                            MySet setHIl = (MySet) listHI.get(l);
                            setHIl.removeAll(setHIi);
                            listHI.set(l, setHIl);
                        }
                        //fuer alle l=1 bis i-1 bilde HI(l)=HI(l)-HI(i)

                        hZer(listHI, k, i + 1);
                    }
                }
            }
        }

    }//end class HZerlegen


    private MyList cloneList(MyList al) {
        //hier handelt es sich um eine shallow copy bis zur zweiten ebene

        MyList list = (MyList) al.clone();

        for (int i = 0; i < al.size(); i++) {
            MySet set1 = (MySet) al.get(i);
            MySet set2 = (MySet) set1.clone();
            list.set(i, set2);
        }
        return list;
    }

}
