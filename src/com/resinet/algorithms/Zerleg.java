package com.resinet.algorithms;/* Zerleg.java */

import com.resinet.model.Graph;
import com.resinet.model.GraphElement;

import java.util.*;

class Zerleg extends Thread {
    private final Tree ktrees;
    private final List<HashSet<GraphElement>> trs;

    /**
     * Enthält alle Zerlegungen nach Heidtmann. Ein Element ist eine Liste aus Pfaden, wobei der erste Pfad der hin-Pfad
     * ist und die weiteren Pfade die Ergebnisse aus dem Disjunktmachen mit den bisherigen Pfaden sind
     */
    public final HashSet<ArrayList<HashSet<GraphElement>>> hz;

    private final HZerleg hzer;

    public Zerleg(Graph graph) {
        ktrees = new Tree(graph);
        trs = ktrees.trs;
        hzer = new HZerleg();
        hz = new HashSet<>();

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
                        if (!ktrees.dead) {
                            trs.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }
                }
            }
            int pathCount = 0;
            for (int i = 1; i <= trs.size(); i++) {
                ArrayList<HashSet<GraphElement>> listK = new ArrayList<>();
                HashSet<GraphElement> setI0 = trs.get(i - 1);
                listK.add(setI0);
                for (int j = 1; j <= i; j++) {
                    listK.add(new HashSet<>());
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
            //System.out.println("Anzahl Pfade: " + pathCount);
        }

        private void hZer(ArrayList<HashSet<GraphElement>> listK, int k, int i) {
            if (i == k) {
                //listK.remove(k);
                ArrayList<HashSet<GraphElement>> al = cloneList(listK);
                hz.add(al);
            } else {
                //setIi ist der vorherige Pfad
                HashSet<GraphElement> setIi = trs.get(i - 1);
                boolean disjoint = false;

                for (int j = 1; j < i; j++) {
                    HashSet<GraphElement> setIl = listK.get(j);
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
                    ArrayList<HashSet<GraphElement>> listHI = cloneList(listK);
                    //fuer alle l=0 bis k HI(l)=I(l)

                    for (int j = 1; j < i; j++) {
                        HashSet<GraphElement> setIl = listK.get(j);
                        HashSet<GraphElement> setHIl = cloneSet(setIl);
                        setHIl.retainAll(setIi);
                        //HI(j)=I(j)/\Ii

                        if (!setHIl.isEmpty()) {
                            listHI.set(j, setHIl);
                            hZer(listHI, k, i + 1);
                        }
                        setHIl = cloneSet(setIl);
                        setHIl.removeAll(setIi);
                        listHI.set(j, setHIl);
                        //HI(j)=I(j)-Ii

                        HashSet<GraphElement> cut = cloneSet(setIl);
                        cut.retainAll(setIi);
                        HashSet<GraphElement> setHI0 = listHI.get(0);
                        setHI0.addAll(cut);
                        listHI.set(0, setHI0);
                        // HI(0)=HI(0)\/(I(j)/\Ii)
                    }

                    HashSet<GraphElement> joint = new HashSet<>();
                    for (int j = 0; j < i; j++) {
                        HashSet<GraphElement> temp = listK.get(j);
                        joint.addAll(temp);
                    }
                    HashSet<GraphElement> setHIi = cloneSet(setIi);
                    setHIi.removeAll(joint);
                    listHI.set(i, setHIi);
                    //HI(i) = Ii-\/I(l)

                    if (!setHIi.isEmpty()) {
                        for (int j = 1; j < i; j++) {
                            HashSet<GraphElement> setHIl = listHI.get(j);
                            setHIl.removeAll(setHIi);
                            listHI.set(j, setHIl);
                        }
                        //fuer alle l=1 bis i-1 bilde HI(l)=HI(l)-HI(i)

                        hZer(listHI, k, i + 1);
                    }
                }
            }
        }

    }//end class HZerlegen


    private static ArrayList<HashSet<GraphElement>> cloneList(ArrayList<HashSet<GraphElement>> al) {
        //hier handelt es sich um eine shallow copy bis zur zweiten ebene

        ArrayList<HashSet<GraphElement>> list = (ArrayList<HashSet<GraphElement>>) al.clone();

        for (int i = 0; i < al.size(); i++) {
            HashSet<GraphElement> set1 = al.get(i);
            HashSet<GraphElement> set2 = cloneSet(set1);
            list.set(i, set2);
        }
        return list;
    }

    private static HashSet<GraphElement> cloneSet(HashSet<GraphElement> input) {
        return (HashSet<GraphElement>) input.clone();
    }
}
