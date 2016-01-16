package com.resinet.algorithms;/* Zerleg.java */


import com.resinet.util.MyList;
import com.resinet.util.MySet;
import com.resinet.model.Graph;

class Zerleg extends Thread {
    private Tree ktrees;
    private final MyList trs;

    /**
     * Enthält alle Zerlegungen nach Heidtmann. Ein Element ist eine Liste aus Pfaden, wobei der erste Pfad der hin-Pfad
     * ist und die weiteren Pfade die Ergebnisse aus dem Disjunktmachen mit den bisherigen Pfaden sind
     */
    public MySet hz;

    private HZerleg hzer;

    public Zerleg(Graph graph) {
        ktrees = new Tree(graph);
        trs = ktrees.trs;
        hzer = new HZerleg();
        hz = new MySet();
    }

    public void run() {
        ktrees.start();
        hzer.start();

        try {
            ktrees.join();
            hzer.join();
        } catch (Exception ignored) {
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
            int pathCount = 0;
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
                            //Durch diese Abfrage treten auch ohne Wartezeit keine Deadlocks mehr auf
                            if (!ktrees.dead) {
                                trs.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                pathCount++;
            }
            System.out.println("Anzahl Pfade: " + pathCount);
        }

        private void hZer(MyList listK, int k, int i) {
            if (i == k) {
                //listK.remove(k);
                MyList al = cloneList(listK);
                hz.add(al);
            } else {
                //setIi ist der vorherige Pfad
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
