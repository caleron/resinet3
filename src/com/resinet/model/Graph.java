package com.resinet.model;/* com.resinet.model.Graph.java */

/* Auf den Klassen "com.resinet.model.Node" und "Edge" wird ein Zufallsgraph generiert. Mit einer Zufallszahl
werden die Konnektionsknoten festgelegt. Anschliessend werden die K-Baeume vom com.resinet.model.Graph abgeleitet.
Durch die K-Baeume erhaelt man die Minimalkombinationen.
*/

import com.resinet.algorithms.Con_check;
import com.resinet.util.MyIterator;
import com.resinet.util.MyList;
import com.resinet.util.MySet;

import java.util.Random;
import java.io.Serializable;

public class Graph implements Serializable {
    private int edge_quan;
    public int node_quan;

    private Random r = new Random();
    private int n;
    public int m;
    public  float offset;
    public float left_offset, right_offset; //Die linke und rechte "Ausbreitung" des Subbaumes beim Zeichnen des Faktorisierungsbaumes
    public int level;
    public int kind_of_reduction; //Art der Faktorisierung, durch die der com.resinet.model.Graph entstanden ist: 0==intakt, 1==defekt
    public int reduced_edge; //Nach welcher Kante wurde reduziert?

    public MyList child_Graphs;
    public MyList nodeList;
    public MyList edgeList;

    public double kprob;
    //fuer K-Reduktion (1-u1u2)(1-u3u4)...
    public String reducetype;
    //fuer Polygon-Ketten-Reduktion
    //wedge = (f1+f2)(f1+f3)/f1 oder wedge = (f1+f2)(f1+f3)(f1+f4)/f1^2
    public double wedge = 1.0;
    public MySet wedgeIndex = new MySet();

    public Object clone() throws CloneNotSupportedException {
        Graph graph = (Graph) super.clone();
        graph.edgeList = (MyList) edgeList.clone();
        graph.nodeList = (MyList) nodeList.clone();
        return graph;
    }

    public int getCnodeSize() {
        int c_size = 0;
        MyIterator it = nodeList.iterator();
        while (it.hasNext()) {
            Node node = (Node) it.next();
            if (node.c_node)
                c_size++;
        }
        return c_size;
    }

    private void node_generate(int x) {
        int m;
        if (x == 0) {
            do {
                m = 1 + Math.abs(r.nextInt()) % 200;
            }
            while (m < 2);

            // m ist Anzahl der Knoten
            // Hier wird die Anzahl voruebergehend 2 < m < 200
        } else
            m = x;

        do {
            n = 1 + Math.abs(r.nextInt()) % m;
        }
        while (n < 2);

        // Anzahl der Konnektionsknoten, 2 < n <= m

        nodeList = new MyList();
        int cnt;
        int c;

        for (cnt = 0; cnt < m; cnt++) {
            nodeList.add(new Node(cnt));
            // neue Knoten erzeugen von 0 bis auf m-1
        }

        for (cnt = 0; cnt < n; cnt++) {
            c = Math.abs(r.nextInt()) % m;

            while (((Node) nodeList.get(c)).c_node) {
                c = Math.abs(r.nextInt()) % m;
            }
            Node node = (Node) nodeList.get(c);
            node.c_node = true;

            // Knoten c wird als Konnektionsknoten kenngezeichnet
        }
    }

    private void edge_generate() {
        //Ein com.resinet.model.Graph mit m Knoten hat max. sum m Kanten
        edgeList = new MyList();
        int i;
        int j;
        double k;
        int w;

        for (i = 0; i < nodeList.size(); i++) {
            for (j = i + 1; j < nodeList.size(); j++)
            // Da der com.resinet.model.Graph ungerichtet ist
            {
                k = Math.random();
                if (k > 0.5) {
                    int s = edgeList.size();
                    Edge edge = new Edge(s);
                    Node node_i = (Node) nodeList.get(i);
                    Node node_j = (Node) nodeList.get(j);
                    edge.left_node = node_i;
                    edge.right_node = node_j;
                    edgeList.add(edge);
                    node_i.add_Edge(edge);
                    node_j.add_Edge(edge);
                }
            }
        }
    }

    private void connected() {
        int z, j;

        do {
            z = Con_check.check(this);
            if (z < 0)
                break;
            do {
                j = Math.abs(r.nextInt()) % nodeList.size();
            }
            while (z == j || !((Node) nodeList.get(j)).marked);
            int s = edgeList.size();
            Edge edge = new Edge(s);
            Node node_z = (Node) nodeList.get(z);
            Node node_j = (Node) nodeList.get(j);
            edge.left_node = node_z;
            edge.right_node = node_j;
            edgeList.add(edge);
            node_z.add_Edge(edge);
            node_j.add_Edge(edge);

        }
        while (z >= 0);

        edge_quan = edgeList.size();
        node_quan = nodeList.size();
    }

    public int delete_Node(Node n) //Ergänzt um die Rückgabe der Anzahl der entfernten Kanten.
    {
        if (!nodeList.contains(n)) {
            return (-1);
        }
        int delcnt = 0;
        MyList v = (MyList) n.node_edge.clone();
        MyIterator it = v.iterator();
        while (it.hasNext()) {
            Edge edge = (Edge) it.next();
            delete_Edge(edge);
            delcnt = delcnt + 1;
        }
        nodeList.remove(n);
        node_quan = nodeList.size();
        return delcnt;
    }

    public void add_Node(Node n) {
        int size = n.degree;
        Edge[] temp = new Edge[size];
        int i = 0;
        MyIterator it = n.node_edge.iterator();
        while (it.hasNext()) {
            Edge edge = (Edge) it.next();
            temp[i] = edge;
            i++;
        }

        for (i = 0; i < size; i++)
            add_Edge(temp[i]);
        nodeList.add(n);
        node_quan = nodeList.size();
    }

    public void delete_Edge(Edge b) {
        Node left = b.left_node;
        Node right = b.right_node;

        left.delete_Edge(b);
        right.delete_Edge(b);
        b.deleted = true;
        edgeList.remove(b);
        edge_quan = edgeList.size();
    }


    /*Lässt zwei Knoten zusammenfallen, Kante wird als intakt vorausgesetzt*/
    public void reduce_Edge_i(Edge b) {
    /*Überprüfe, ob die Kante b in der Liste der Kanten edgeList enthalten ist.*/
        if (edgeList.contains(b)) {
        /*Merke "linken" (verschwindet) und "rechten" (bleibt erhalten) Knoten der Kante b.*/
            Node left = b.left_node;
            Node right = b.right_node;

		/*Lösche Kante b.*/
            delete_Edge(b);

		/*Durchlaufe alle Kanten des "linken" Knoten.*/
            for (int i = 0; i < left.node_edge.size(); i++) {
                Edge e = (Edge) left.node_edge.get(i); //nächste Kante

			/*Falls die Kante noch nicht in der Liste der Kanten des "rechten" Knoten ist,
			füge sie hinzu.*/
                if (!right.node_edge.contains(e)) {
                    right.add_Edge(e);
                } else
			   /*Falls noch eine zweite Kante zwischen left und right exisiert,
			   verschwindet diese auch. Das sollte hier allerdings aufgrund der
			   vorher durchgeführten Reduzierung nicht vorkommen.*/ {
                    delete_Edge(e);
                    i = i - 1;
                }


			/*Setze den "linken" und "rechten" Knoten der Kante entsprechend ihres neuen Kontextes.*/
                if (e.left_node == left)
                    e.left_node = right;
                else
                    e.right_node = right;
            }

		/*Falls left ein Konnektionsknoten ist, wird right auch Konnektionsknoten.*/
            if (left.c_node) {
                right.c_node = true;
                left.c_node = false;
            }

		/*Entferne left aus dem Netz und passe die Anzahl der Knoten an.*/
            nodeList.remove(left);
            node_quan = nodeList.size();
        }
    }

    public void reduce_Edge_d(Edge b) {
        delete_Edge(b);
    }

    /* Löscht einen Teil des Graphen und gibt dien Anzahl der gelöschten Kanten zurück. Betroffen sind alle Knoten, die markiert sind. Markierungen setzt z.B. die Methode public static int check(com.resinet.model.Graph g, Edge e) der Klasse com.resinet.algorithms.Con_check, die nutzlose Teile des Graphen erkennt. */
    public int delete_part_of_Graph() {
        int delcnt = 0;
        for (int i = 0; i < nodeList.size(); i++) {
            Node node = (Node) nodeList.get(i);
            if (node.marked) {
                delcnt = delcnt + delete_Node(node);
                i = i - 1;
            }
        }
        return delcnt;
    }

    public void add_Edge(Edge b) {
        if (!edgeList.contains(b)) {
            edgeList.add(b);
            b.deleted = false;

            Node left = b.left_node;
            Node right = b.right_node;

            left.add_Edge(b);
            right.add_Edge(b);

            edge_quan = edgeList.size();
        }
    }

    public Edge getEdge(int edge_no) {
        MyIterator it = edgeList.iterator();
        while (it.hasNext()) {
            Edge edge = (Edge) it.next();
            if (edge.edge_no == edge_no)
                return edge;
        }
        return null;
    }

    public MyList getNodelist() {
        return nodeList;
    }

    public MyList getEdgelist() {
        return edgeList;
    }

    public int getHighestNodeNr() {
        int high = 0;
        MyIterator it = nodeList.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            if (n.node_no > high)
                high = n.node_no;
        }
        return high;
    }

    public Graph(MyList nodeList, MyList edgeList) {
        this.nodeList = nodeList;
        this.edgeList = edgeList;
        node_quan = this.nodeList.size();
        edge_quan = this.edgeList.size();
    }

    public Graph(int x) {
        node_generate(x);
        edge_generate();
        connected();
    }

    public Graph(int[] c, int[][] mx) {
        int m = mx.length;
        int s = mx[0].length;
        n = 0;
        // Anzahl der C-Knoten

        nodeList = new MyList();
        edgeList = new MyList();

        for (int i = 0; i < m; i++) {
            Node node = new Node(i);
            if (c[i] == 1) {
                node.c_node = true;
                n++;
            }
            nodeList.add(node);
        }
        for (int j = 0; j < s; j++) {
            Edge edge = new Edge(j);
            boolean b = false;
            for (int i = 0; i < m; i++) {
                if (mx[i][j] == 1) {
                    Node node = (Node) nodeList.get(i);
                    if (!b)
                        edge.left_node = node;
                    else
                        edge.right_node = node;
                    b = true;
                    node.add_Edge(edge);
                }
            }
            edgeList.add(edge);
        }
        node_quan = nodeList.size();
        edge_quan = edgeList.size();
    }

}











