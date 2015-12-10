package com.resinet.algorithms;/*Tree.java*/


import com.resinet.model.Edge;
import com.resinet.model.Graph;
import com.resinet.model.KTree;
import com.resinet.model.Node;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;
import com.resinet.util.MySet;

import java.util.ArrayList;

class Tree extends Thread {
    private Graph graph;
    private KTree b;
    MyList trs;
    //Menge aller k-Baeume
    private Q q;

    private int ktr_i;
    //Anzahl der K-Baeume

    public boolean dead = false;

    public Tree(Graph graph) {
        //graph = (com.resinet.model.Graph)Sc.serialClone(graph);
        this.graph = graph;
        q = new Q();
        b = new KTree();
        trs = new MyList();
        //Initialisierung

        Node node;
        int i;

        MyIterator it = this.graph.nodeList.iterator();
        while (it.hasNext()) {
            Node node2 = (Node) it.next();
            if (node2.c_node) {
                node = node2;
                b.add_Node(node);
                outside_add(node);
                break;
            }
        }
        //Ende der Initialisierung

        //tree();
    }

    @Override
    public void run() {
        tree();
        dead = true;
        try {
            sleep(300);
        } catch (InterruptedException ignored) {
        }
        synchronized (trs) {
            trs.notifyAll();
        }

    }


    private Edge[] outside_add(Node w) {
        Edge[] n = new Edge[w.degree];
        int n_i = 0;
        int i;

        MyIterator it = w.node_edge.iterator();
        while (it.hasNext()) {
            Edge edge = (Edge) it.next();
            if (!edge.left_node.b_marked || !edge.right_node.b_marked) {
                q.add(edge);
                n[n_i] = edge;
                n_i++;
            }
        }
        if (n_i == 0)
            return (null);
        else
            return (n);
    }

    private Edge[] inside_delete(Node w) {
        Edge[] m = new Edge[w.degree];
        int m_i = 0;
        int i;

        MyIterator it = w.node_edge.iterator();
        while (it.hasNext()) {
            Node n;
            Edge e;

            e = (Edge) it.next();

            if (e.left_node == w)
                n = e.right_node;
            else
                n = e.left_node;

            if (e.in_q && n.b_marked) {
                q.delete(e);
                m[m_i] = e;
                m_i++;
            }
        }
        if (m_i == 0)
            return (null);
        else
            return (m);
    }

    private void outside_delete(Edge[] n) {
        int i;
        int l;

        if (n == null)
            return;

        l = n.length;

        for (i = 0; i < l; i++) {
            if (n[i] == null)
                break;
            q.delete(n[i]);
        }
    }

    private void inside_add(Edge[] m) {
        int i;

        if (m == null)
            return;

        for (i = m.length - 1; i >= 0; i--) {
            if (m[i] == null)
                continue;
            q.insert(m[i]);
        }
    }

    private void output_ktree() {
        String s;
        Edge e = b.first_br;

        ktr_i++;

        MySet ktree = new MySet();
        while (e != null) {
            if (!e.useless) {
                ktree.add(e);
            }
            e = e.right;
        }


        ArrayList<Node> consideredNodes = new ArrayList<>();


        MySet treeCopy = (MySet) ktree.clone();

        MyIterator it = treeCopy.iterator();
        String output = "Pfad";
        while (it.hasNext()) {
            Edge edge = (Edge) it.next();
            output += " e" + edge.edge_no;
            //TODO testen

            if (!consideredNodes.contains(edge.left_node)) {
                consideredNodes.add(edge.left_node);
                ktree.add(edge.left_node);
                output += " n" + edge.left_node.node_no;
            }

            if (!consideredNodes.contains(edge.right_node)) {
                consideredNodes.add(edge.right_node);
                ktree.add(edge.right_node);
                output += " n" + edge.right_node.node_no;
            }
        }
        System.out.println(output);

        synchronized (trs) {
            if (!trs.contains(ktree)) {
                trs.add(ktree);
                trs.notifyAll();
            }
        }
    }

    private void tree_search(Node node, Edge edge) {
        Edge e;
        Node n;
        boolean bl = true;

        MyIterator it = node.node_edge.iterator();
        while (it.hasNext()) {
            e = (Edge) it.next();
            if (!e.b_marked)
                continue;
            if (e == edge)
                continue;

            if (e.left_node == node)
                n = e.right_node;
            else
                n = e.left_node;
            tree_search(n, e);

            bl = bl && e.useless;
        }
        if (bl && !node.c_node) {
            edge.useless = true;
            b.no_use++;
            node.useless = true;
        }
    }


    private void tree() {
        Edge e;
        Node w;
        Edge[] n, m, q1;

        q1 = new Edge[q.count];
        int n_i = 0;
        int m_i = 0;
        int q1_i = 0;

        if (q.count == 0) {
            Edge eg = b.last_br;
            while (eg != null) {
                eg.useless = false;
                eg = eg.left;
            }
            b.no_use = 0;
            //initialisierung fuer tree_search
            tree_search(b.first_nd, null);
            output_ktree();
            //if N(G,B)=0 then output spannender Baum B
        } else {
            while (Con_check.check(graph) < 0) {
                e = q.last;
                //e := letztes Element von Q

                q.delete(e);
                //Q := Q - e

                if (!e.left_node.b_marked)
                    w = e.left_node;
                else
                    w = e.right_node;
                //w := Endknoten von e der nicht in B ist

                b.add_Edge(e);
                b.add_Node(w);
                //B := B+{e}

                n = outside_add(w);
                //fuege N ={e<-E: I(e)={w,v},v nicht in B} bei Q hinten an

                m = inside_delete(w);
                //entferne M = {e<-E: I(e)={u,w}, u in B} aus Q;

                tree();

                inside_add(m);
                //fuege m wieder Q bei

                outside_delete(n);
                //entferne N aus Q

                b.delete_Node(w);
                b.delete_Edge(e);

                //entfern e aus B;

                graph.delete_Edge(e);
                //entfern e aus G

                q1[q1_i] = e;
                q1_i++;
                //fuege e der Liste Q1 hinten an


		/* wenn diese Kante End-Kante des Graphens ist, dann
           bricht die While-Schleife ab. */
            }

            while (q1_i > 0) {
                Edge e1;
                e1 = q1[q1_i - 1];
                q.add(e1);
                graph.add_Edge(e1);
                q1_i--;
            }
        }
    }

    private class Q {
        int count;
        Edge last;

        public Q() {
            count = 0;
        }

        void add(Edge edge) {
            if (count == 0)
                edge.q_left = null;
            else {
                edge.q_left = last;
                last.q_right = edge;
            }
            edge.in_q = true;
            edge.q_right = null;
            count++;
            last = edge;
        }

        void insert(Edge edge) {
            count++;
            edge.in_q = true;
            if (edge.q_left != null)
                edge.q_left.q_right = edge;
            if (edge.q_right != null)
                edge.q_right.q_left = edge;
            else
                last = edge;
        }

        void delete(Edge edge) {
            count--;
            edge.in_q = false;
            if (edge.q_left != null)
                edge.q_left.q_right = edge.q_right;
            if (edge.q_right != null)
                edge.q_right.q_left = edge.q_left;
            else
                last = edge.q_left;
        }

    }
}






/* Modifikation des Algorithmus von Kohlas

Da es sich hier um ein k-von-n Problem handelt, wobei k<=n, wird der
Algorithmus von Kohlas leicht modifiziert.

Line 231: Jedesmal, nachdem eine neue Kante e in den aktuellen Baum b hinzu-
gefuegt wurde, wird gepr�ft, ob die letzte Kante, d.h. e.left, eine s.graph. Endkante
und der dazugehoerige Knoten ein NICHT-Konnectionsknoten ist. Falls ja, wird 
weiter geprueft, ob der Knoten bereits erreicht wurde (w.reached == true). Falls
ja, wird die Schleife abgebrochen und kein Baum ausgegeben.

Line 342:
Wenn der Knoten w ein Nicht-K-Knoten ist und w.reached == true, w aber er kein
Endknoten ist, wird �berprueft, ob alle Knoten bzw. Kanten UNTER w "use-
less" sind, d.h. Nicht-k-Knoten. Wenn der Knoten EINMAL useless 
ist, wird der Knoten beim Loeschen als "reached"-Knoten gekenn-
zeichnet und erst bei erneutem Hinzufuegen die Attribute "reached" 
wieder auf "false" gesetzt . Beim Hinzufuegen eines Knotens in den Baum b wird
geprueft, ob das Attribut "reached" bereits "true" ist und der Knoten kein
Endknoten ist. Der Vorgang wird sonst trotzdem fortsetzt und das Attribut b.val 
auf "false" gesetzt.
Beim Output eines Baums wird der Wert von b.val geprueft. Falls er "false"
ist, wird der Baum nicht ausgegeben.

Die Idee: Wenn ein Knoten "useless" ist, kann man ihn auch als Nicht-K- und
gleichzeitig End-Knoten betrachten, also als erreichten (reached) Knoten.

Alle erreichten Knoten sind Nicht-K-Knoten.

Erreichen ("reached"):
Da eine "useless" Kante geloescht werden muss, sollte eine solche Kante
in einer Schleife nur einmal gezaehlt werden. Die Baeume sollten nicht
ausgegeben (b.val = true) werden, nachdem die "useless"-Kante des Graphen geloescht
wurde, wenn der (ebenfalls "useless") Knoten ueber eine andere Kante NOCHMAL er-
reicht wird. Sonst kommt es zu doppelten Eintraegen in der Menge der K-Baeume.

Ueber com.resinet.model.KTree:
Alle K-Baeume werden direkt in die Datei ktree.txt geschrieben, damit 
das "Memory-Out-Problem" vermieden wird. S.a java.memory.tar/Tree.java.


*/  
