package com.resinet;/* com.resinet.Util.java */

import com.resinet.model.Edge;

import java.io.*;
import java.util.Hashtable;
//import java.util.*;

public class Util {
    public static Object serialClone(Object o)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);

        os.writeObject(o);
        os.flush();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream is = new ObjectInputStream(in);
        Object ret = is.readObject();
        is.close();
        os.close();
        return ret;
    }


    public static Graph inputGraph(String str)
            throws IOException {
        int[][] mx;
        int[] c;

        FileReader f1 = new FileReader(str);
        BufferedReader f2 = new BufferedReader(f1);
        String line;
        int s = 0;
        int m, n;

        line = f2.readLine();
        m = line.length();
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ')
                m--;
        }
        c = new int[m];
        for (int i = 0; i < m; i++) {
            if (line.charAt(i) == '1')
                c[i] = 1;
            else
                c[i] = 0;
        }

        line = f2.readLine();
        n = line.length();
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ')
                n--;
        }
        mx = new int[m][n];
        for (int i = 0; i < n; i++) {
            if (line.charAt(i) == '1')
                mx[0][i] = 1;
            else
                mx[0][i] = 0;
        }

        while ((line = f2.readLine()) != null) {
            if (line.charAt(0) == ' ')
                break;
            s++;
            for (int i = 0; i < n; i++) {
                if (line.charAt(i) == '1')
                    mx[s][i] = 1;
                else
                    mx[s][i] = 0;
            }
        }

        Graph g = new Graph(c, mx);
        return (g);
    }


    public static void skpReduce(Graph g, boolean factorisation) {

        MyList nd = g.nd;
        MyList br = g.br;
        boolean reduced = true;
        boolean a = false;
        boolean b = false;

        while (reduced) {
            reduced = false;
            int size = nd.size();
            for (int i = 0; i < size; i++) {
                Node node = (Node) nd.get(i);
                if (node.degree == 2) {
                    Edge edge1, edge2;
                    MyIterator it1 = node.node_edge.iterator();
                    edge1 = (Edge) it1.next();
                    edge2 = (Edge) it1.next();

                    Node node1, node2;
                    node1 = getOtherNode(edge1, node);
                    node2 = getOtherNode(edge2, node);

                    Edge edge3 = new Edge(-1);
                    //neue Kante

                    if (node1 == node2) {
                        //P-Reduktion
                        edge3.rdcType = "P";
                        edge3.parent_edges[0] = edge1;
                        edge3.parent_edges[1] = edge2;
                        edge3.left_node = node;
                        edge3.right_node = node1;

                        g.delete_Edge(edge1);
                        g.delete_Edge(edge2);
                        g.add_Edge(edge3);

                        reduced = true;
                    } else {
                        if (node.c_node) {
                            //K-Reduktion, node aus K-Menge
                            if (node1.c_node && node2.c_node) {
                                edge3.rdcType = "K";
                                edge3.parent_edges[0] = edge1;
                                edge3.parent_edges[1] = edge2;
                                edge3.left_node = node1;
                                edge3.right_node = node2;
                                g.delete_Node(node);
                                g.add_Edge(edge3);
                                reduced = true;
                            } else if (node1.degree > 2 && node2.degree > 2 && factorisation == false) {
                                //Polygon-Ketten-Reduktion 1, 2, 3, 4
                                Edge e1 = null;
                                Edge e2 = null;
                                Edge e3 = null;
                                Edge e4 = null;
                                MySet nodes1 = new MySet();
                                MyIterator it = node1.node_edge.iterator();
                                while (it.hasNext()) {
                                    Edge e = (Edge) it.next();
                                    Node n = getOtherNode(e, node1);
                                    if (!n.equals(node))
                                        nodes1.add(n);
                                    if (n.equals(node2))
                                        e3 = e;
                                }
                                if (nodes1.contains(node2)) {
                                    //polygon-type 1 oder 2
                                    e1 = edge1;
                                    e2 = edge2;
                                    g.delete_Edge(e1);
                                    g.delete_Edge(e2);
                                    g.delete_Edge(e3);
                                    Edge er = new Edge(-1);
                                    Edge es = new Edge(-1);
                                    if (node1.c_node || node2.c_node) {
                                        er.rdcType = "R2";
                                        es.rdcType = "S2";
                                    } else {
                                        er.rdcType = "R1";
                                        es.rdcType = "S1";
                                    }
                                    er.parent_edges[0] = e1;
                                    er.parent_edges[1] = e2;
                                    er.parent_edges[2] = e3;
                                    g.wedgeIndex.add(er);
                                    es.parent_edges[0] = e1;
                                    es.parent_edges[1] = e2;
                                    es.parent_edges[2] = e3;
                                    es.r = er;
                                    er.left_node = node1;
                                    er.right_node = node;
                                    es.left_node = node;
                                    es.right_node = node2;
                                    g.add_Edge(er);
                                    g.add_Edge(es);
                                    reduced = true;
                                } else {
                                    //mal gucken, ob es eine von polygon-type 3, 4, 5 gibt
                                    MySet nodes2 = new MySet();
                                    it = node2.node_edge.iterator();
                                    while (it.hasNext()) {
                                        Edge e = (Edge) it.next();
                                        Node n = getOtherNode(e, node2);
                                        if (!n.equals(node))
                                            nodes2.add(n);
                                    }
                                    //jetzt vergleichen, ob nodes1 und nodes2 gemeinsamen Knoten enthalten
                                    MySet tempSet = (MySet) nodes1.clone();
                                    tempSet.retainAll(nodes2);
                                    if (tempSet.size() > 0) {
                                        Node node3 = null;
                                        it = tempSet.iterator();
                                        while (it.hasNext()) {
                                            Node n = (Node) it.next();
                                            if (n.c_node && n.degree == 2) {
                                                node3 = n;
                                                break;
                                            }
                                        }
                                        if (node3 != null) {
                                            //polygon-typ 3 oder 4
                                            e1 = edge1;
                                            e2 = edge2;
                                            Edge er = new Edge(-1);
                                            Edge es = new Edge(-1);
                                            it = node3.node_edge.iterator();
                                            while (it.hasNext()) {
                                                Edge e = (Edge) it.next();
                                                Node n = getOtherNode(e, node3);
                                                if (n.equals(node1))
                                                    e3 = e;
                                                else if (n.equals(node2))
                                                    e4 = e;
                                            }
                                            if (node1.c_node || node2.c_node) {
                                                er.rdcType = "R3";
                                                es.rdcType = "S3";
                                                er.parent_edges[0] = e1;
                                                er.parent_edges[1] = e2;
                                                er.parent_edges[2] = e3;
                                                er.parent_edges[3] = e4;
                                                g.wedgeIndex.add(er);
                                                es.parent_edges[0] = e1;
                                                es.parent_edges[1] = e2;
                                                es.parent_edges[2] = e3;
                                                es.parent_edges[3] = e4;
                                                es.r = er;
                                                er.left_node = node1;
                                                er.right_node = node;
                                                es.left_node = node;
                                                es.right_node = node2;
                                                g.delete_Node(node3);
                                                g.delete_Edge(e1);
                                                g.delete_Edge(e2);
                                                g.add_Edge(er);
                                                g.add_Edge(es);
                                                reduced = true;
                                            } else {
                                                er.rdcType = "R4";
                                                es.rdcType = "S4";
                                                Edge et = new Edge(-1);
                                                et.rdcType = "T4";
                                                er.parent_edges[0] = e1;
                                                er.parent_edges[1] = e2;
                                                er.parent_edges[2] = e3;
                                                er.parent_edges[3] = e4;
                                                g.wedgeIndex.add(er);
                                                es.parent_edges[0] = e1;
                                                es.parent_edges[1] = e2;
                                                es.parent_edges[2] = e3;
                                                es.parent_edges[3] = e4;
                                                es.r = er;
                                                et.parent_edges[0] = e1;
                                                et.parent_edges[1] = e2;
                                                et.parent_edges[2] = e3;
                                                et.parent_edges[3] = e4;
                                                et.r = er;
                                                g.delete_Edge(e1);
                                                g.delete_Edge(e2);
                                                g.delete_Edge(e3);
                                                g.delete_Edge(e4);
                                                er.left_node = node1;
                                                er.right_node = node;
                                                es.left_node = node;
                                                es.right_node = node3;
                                                et.left_node = node3;
                                                et.right_node = node2;
                                                g.add_Edge(er);
                                                g.add_Edge(es);
                                                g.add_Edge(et);
                                                reduced = true;
                                            }

                                        }
                                    }//if(tempSet.size()>0)
                                }//else mal gucken
                            }//else if(node1.degree>2 && node2.degree>2)

                            else if (((a = (node1.degree > 2 && !node1.c_node &&
                                    node2.degree == 2 && node2.c_node)) ||
                                    (b = (node2.degree > 2 && !node2.c_node &&
                                            node1.degree == 2 && node1.c_node)))) {

                                if (b) {
                                    Node temp = node1;
                                    node1 = node2;
                                    node2 = temp;
                                }

                                Edge e21 = (Edge) node2.node_edge.get(0);
                                Edge e22 = (Edge) node2.node_edge.get(1);
                                Edge e1 = edge1;
                                Edge e2 = edge2;
                                Edge e3 = null;
                                Edge e4 = null;

                                if (e21.equals(e2))
                                    e3 = e22;
                                else
                                    e3 = e21;
                                Node node3 = getOtherNode(e3, node2);

                                if (node3.degree > 2 && !node3.c_node) {
                                    e4 = getCommonEdge(node1, node3);
                                    if (e4 != null && factorisation == false) {
                                        //Polygon-Typ5
                                        Edge er = new Edge(-1);
                                        Edge es = new Edge(-1);
                                        Edge et = new Edge(-1);
                                        er.rdcType = "R5";
                                        es.rdcType = "S5";
                                        et.rdcType = "T5";
                                        g.delete_Edge(e1);
                                        g.delete_Edge(e2);
                                        g.delete_Edge(e3);
                                        g.delete_Edge(e4);
                                        if (g.getCnodeSize() > 2) {
                                            er.left_node = node1;
                                            er.right_node = node;
                                            es.left_node = node;
                                            es.right_node = node2;
                                            es.r = er;
                                            et.left_node = node2;
                                            et.right_node = node3;
                                            et.r = er;
                                            er.parent_edges[0] = e1;
                                            er.parent_edges[1] = e2;
                                            er.parent_edges[2] = e3;
                                            er.parent_edges[3] = e4;
                                            g.wedgeIndex.add(er);
                                            es.parent_edges[0] = e1;
                                            es.parent_edges[1] = e2;
                                            es.parent_edges[2] = e3;
                                            es.parent_edges[3] = e4;
                                            et.parent_edges[0] = e1;
                                            et.parent_edges[1] = e2;
                                            et.parent_edges[2] = e3;
                                            et.parent_edges[3] = e4;
                                            g.add_Edge(er);
                                            g.add_Edge(es);
                                            g.add_Edge(et);
                                        } else if (g.getCnodeSize() == 2) {
                                            er.left_node = node1;
                                            er.right_node = node3;
                                            er.parent_edges[0] = e1;
                                            er.parent_edges[1] = e2;
                                            er.parent_edges[2] = e3;
                                            er.parent_edges[3] = e4;
                                            g.wedgeIndex.add(er);
                                            node1.c_node = true;
                                            node3.c_node = true;
                                            g.delete_Node(node);
                                            g.delete_Node(node2);
                                            g.add_Edge(er);
                                        }
                                        reduced = true;
                                    }//if(e4!=null)
                                    else if (e4 == null && factorisation == false) {
                                        //guck mal, ob typ 6 oder 7 moeglich ist
                                        Node node4 = getCommonNode(node1, node3);
                                        if (node4 != null && node4.c_node) {
                                            //typ 6
                                            e4 = getCommonEdge(node1, node4);
                                            Edge e5 = getCommonEdge(node3, node4);
                                            Edge er = new Edge(-1);
                                            Edge es = new Edge(-1);
                                            Edge et = new Edge(-1);
                                            er.rdcType = "R6";
                                            es.rdcType = "S6";
                                            et.rdcType = "T6";
                                            g.delete_Edge(e1);
                                            g.delete_Edge(e2);
                                            g.delete_Edge(e3);
                                            g.delete_Node(node4);
                                            er.left_node = node1;
                                            er.right_node = node;
                                            es.left_node = node;
                                            es.right_node = node2;
                                            et.left_node = node2;
                                            et.right_node = node3;
                                            er.parent_edges[0] = e1;
                                            er.parent_edges[1] = e2;
                                            er.parent_edges[2] = e3;
                                            er.parent_edges[3] = e4;
                                            er.parent_edges[4] = e5;
                                            g.wedgeIndex.add(er);
                                            es.parent_edges[0] = e1;
                                            es.parent_edges[1] = e2;
                                            es.parent_edges[2] = e3;
                                            es.parent_edges[3] = e4;
                                            es.parent_edges[4] = e5;
                                            es.r = er;
                                            et.parent_edges[0] = e1;
                                            et.parent_edges[1] = e2;
                                            et.parent_edges[2] = e3;
                                            et.parent_edges[3] = e4;
                                            et.parent_edges[4] = e5;
                                            et.r = er;
                                            g.add_Edge(er);
                                            g.add_Edge(es);
                                            g.add_Edge(et);
                                            reduced = true;
                                        }//if(node4!=null
                                        else {
                                            //vielleicht typ 7?
                                            MySet prenodes = new MySet();
                                            prenodes.add(node);
                                            prenodes.add(node1);
                                            prenodes.add(node2);
                                            prenodes.add(node3);
                                            Edge e5 = null;
                                            Node node5 = null;
                                            MySet nodes1 = new MySet();
                                            MySet nodes2 = new MySet();
                                            MyIterator it = node1.node_edge.iterator();
                                            while (it.hasNext()) {
                                                Edge e = (Edge) it.next();
                                                Node n = getOtherNode(e, node1);
                                                if (n.c_node && !prenodes.contains(n)) {
                                                    nodes1.add(n);
                                                }
                                            }
                                            it = node3.node_edge.iterator();
                                            while (it.hasNext()) {
                                                Edge e = (Edge) it.next();
                                                Node n = getOtherNode(e, node3);
                                                if (n.c_node && !prenodes.contains(n)) {
                                                    nodes2.add(n);
                                                }
                                            }
                                            it = nodes1.iterator();
                                            while (it.hasNext()) {
                                                boolean c = false;
                                                Node n1 = (Node) it.next();
                                                MyIterator it2 = nodes2.iterator();
                                                while (it2.hasNext()) {
                                                    Node n2 = (Node) it2.next();
                                                    Edge e = getCommonEdge(n1, n2);
                                                    if (e != null) {
                                                        e5 = e;
                                                        node4 = n1;
                                                        node5 = n2;
                                                        c = true;
                                                        break;
                                                    }
                                                }
                                                if (c)
                                                    break;
                                            }
                                            if (node4 != null && node5 != null) {
                                                //typ 7
                                                e4 = getCommonEdge(node1, node4);
                                                Edge e6 = getCommonEdge(node3, node5);
                                                Edge er = new Edge(-1);
                                                Edge es = new Edge(-1);
                                                Edge et = new Edge(-1);
                                                er.rdcType = "R7";
                                                es.rdcType = "S7";
                                                et.rdcType = "T7";
                                                g.delete_Edge(e1);
                                                g.delete_Edge(e2);
                                                g.delete_Edge(e3);
                                                g.delete_Node(node4);
                                                g.delete_Node(node5);
                                                er.left_node = node1;
                                                er.right_node = node;
                                                es.left_node = node;
                                                es.right_node = node2;
                                                et.left_node = node2;
                                                et.right_node = node3;
                                                er.parent_edges[0] = e1;
                                                er.parent_edges[1] = e2;
                                                er.parent_edges[2] = e3;
                                                er.parent_edges[3] = e4;
                                                er.parent_edges[4] = e5;
                                                er.parent_edges[5] = e6;
                                                g.wedgeIndex.add(er);
                                                es.parent_edges[0] = e1;
                                                es.parent_edges[1] = e2;
                                                es.parent_edges[2] = e3;
                                                es.parent_edges[3] = e4;
                                                es.parent_edges[4] = e5;
                                                es.parent_edges[5] = e6;
                                                es.r = er;
                                                et.parent_edges[0] = e1;
                                                et.parent_edges[1] = e2;
                                                et.parent_edges[2] = e3;
                                                et.parent_edges[3] = e4;
                                                et.parent_edges[4] = e5;
                                                et.parent_edges[5] = e6;
                                                et.r = er;
                                                g.add_Edge(er);
                                                g.add_Edge(es);
                                                g.add_Edge(et);
                                                reduced = true;
                                            }
                                        }
                                    }
                                }
                            }


                        }//if(node.c_node)
                        else {
                            //S-Reduktion, node NIHCT aus K-Menge
                            edge3.rdcType = "S";
                            edge3.parent_edges[0] = edge1;
                            edge3.parent_edges[1] = edge2;
                            edge3.left_node = node1;
                            edge3.right_node = node2;
                            g.delete_Node(node);
                            g.add_Edge(edge3);
                            reduced = true;
                        }
                    }

                } else {
                    if (node.degree > 2) {
                        //hier wird nur geprueft, ob es eine
                        //P-Reduktion gibt
                        MyIterator it2 = node.node_edge.iterator();
                        Hashtable node2edge = new Hashtable();
                        Edge edge1 = null;
                        Edge edge2 = null;
                        Edge edge3 = null;
                        Node node1 = null;

                        while (it2.hasNext()) {
                            edge1 = (Edge) it2.next();
                            node1 = getOtherNode(edge1, node);
                            edge2 = (Edge) node2edge.put(node1, edge1);
                            if (edge2 != null)
                                break;
                        }
                        if (edge2 != null) {
                            edge3 = new Edge(-1);
                            edge3.parent_edges[0] = edge1;
                            edge3.parent_edges[1] = edge2;
                            System.out.println("P");
                            edge3.rdcType = "P";
                            edge3.left_node = node;
                            edge3.right_node = node1;
                            g.delete_Edge(edge1);
                            g.delete_Edge(edge2);
                            g.add_Edge(edge3);
                            reduced = true;
                        }
                    }
                }
                size = nd.size();
            }
        }
    /*for(int i=0; i<nd.size(); i++)
	    {
		com.resinet.Node node = (com.resinet.Node)nd.get(i);
		node.node_no=i;
	    }
	for(int i=0; i<br.size(); i++)
	    {
		Edge edge = (Edge)br.get(i);
		edge.edge_no=i;
	    }*/
    }


    public static void getProbability(Graph g) {
        g.kprob = 1f;
        g.reducetype = "";
        MyIterator it = g.br.iterator();
        while (it.hasNext()) {
            Edge edge = (Edge) it.next();
            edge.prob = getEdgeProbability(edge, g);
            //System.out.println(edge.prob);
        }
    }


    public static float getProbabilityFact(Graph g, int level) {
	/*Zuverlässigkeiten der beiden sich durch die Faktorisierung ergebenden Netze*/
        float prob = 1;
        float p1, p2;

	/*die beiden sich durch die Faktorisierung ergebenden Netze*/
        Graph g1, g2;
        int edgenr = -2;
        MyList reducableEdges = new MyList();

        p1 = 0;
        p2 = 0;

	/*Falls das Netz aus einer Kante besteht, gib die Intaktwahrscheinlichkeit dieser Kante zurück.*/
        if (g.getEdgelist().size() == 1) {
            Edge e = (Edge) g.getEdgelist().get(0);
            if ((e.left_node.c_node == false) || (e.right_node.c_node == false))
                return 0;
            Renet4.factProb = Renet4.factProb + "r" + e.edge_no + ")";
            return ((Edge) g.getEdgelist().get(0)).prob;
        }

	/*Sonst beginne mit der Faktorisierung. Klone hierzu zunächst das Netz zweimal.*/
        g1 = null;
        g2 = null;
        int c_node_cnt = 0;

        boolean c_node_reduce = false;

	/*c_node_reduce wird true gesetzt, falls nur noch zwei Konnektionsknoten vorhanden, deren verbindende Kante gewählt wurde.*/
        boolean c_nodes_only = false;

	/*i_reduced wird true, wenn eine Reduktion stattfindet. Wichtig, um zu prüfen, ob es keine Kanten aus bizusammenhängenden Teilen des Netzes mehr gibt, nach denen faktorisiert werden könnte.*/
        boolean i_reduced = false;

        try {
            g1 = (Graph) Util.serialClone(g); //Klonen des Graphen
        } catch (IOException e1) {
            System.err.println(e1.toString());
        } catch (ClassNotFoundException e2) {
            System.err.println(e2.toString());
        }
        if (g1.getEdgelist().size() != 0) {
            for (int i = 0; i < g1.getEdgelist().size(); i++) {
                Edge edge = (Edge) (g1.getEdgelist()).get(i);
                int con_check = Con_check.check(g1, edge);
                if (con_check == 1 || con_check == 2) {
                    g1.delete_part_of_Graph();
                    i = i - 1;
                } //Entferne überflüssige Teile des Netzes.
                if (con_check == (-1))
                    reducableEdges.add(new Integer(edge.edge_no));
            }

		/*Prüfe, ob mindestens eine der Kanten, die sich in reducableEdges als reduzierbar gemerkt wurden nach dem Entfernen der überflüssigen Komponenten noch im Netz vorhanden ist und reduziere nach dieser.*/
            MyIterator it = reducableEdges.iterator();
            while (it.hasNext()) {
                edgenr = ((Integer) it.next()).intValue();
                if (g1.getEdge(edgenr) != null)
                    break;
                else
                    edgenr = (-2);
            }

		/* Clone den um überflüssige Komponenten bereinigten Graphen g1. */
            try {
                g2 = (Graph) Util.serialClone(g1);
            } //Klonen des Graphen
            catch (IOException e1) {
                System.err.println(e1.toString());
            } catch (ClassNotFoundException e2) {
                System.err.println(e2.toString());
            }

		/*Konnektionsknoten zählen.*/
            MyIterator it_c = g1.getNodelist().iterator();
            while (it_c.hasNext()) {
                Node n = (Node) it_c.next();
                if (n.c_node == true)
                    c_node_cnt = c_node_cnt + 1;
            }
            if (g1.node_quan == c_node_cnt)
                c_nodes_only = true;

            if (edgenr != (-2)) {
                Edge e = g1.getEdge(edgenr);
                p1 = e.prob;
                Renet4.counterFact = Renet4.counterFact + 1;

                if (c_node_cnt == 2 && e.left_node.c_node == true && e.right_node.c_node == true) {
                    c_node_reduce = true; //nur noch zwei Konnektionsknoten, deren verbindende Kante gewählt wurde.
                    i_reduced = true; //Eine Reduktion hat stattgefunden.
                } else {
                    g1.reduce_Edge_i(e);
                    i_reduced = true; //Eine Reduktion hat stattgefunden.
                }
            }
        }

        if (edgenr != (-2)) {
            Edge e = g2.getEdge(edgenr);
            p2 = e.prob;
            g2.reduce_Edge_d(e);
        }



	/*Berechnung der Zuverlässgkeit durch Faktorisierung*/

        if (c_node_reduce == true) //Fall 1
        {
            g.child_Graphs.add(g2);
            g2.level = level + 1;
            g2.kind_of_reduction = 1;
            g2.reduced_edge = edgenr;
            Renet4.generated_Graphs.add(g2);
            Renet4.factProb = Renet4.factProb + "r" + edgenr + "+(1-r" + edgenr + ")(";
            prob = p1 + (1 - p2) * (getProbabilityFact(g2, level + 1));

        }

        if (c_node_reduce == false && i_reduced == true) //Fall 2
        {
            g.child_Graphs.add(g2);
            g2.level = level + 1;
            g2.kind_of_reduction = 1;
            g2.reduced_edge = edgenr;
            Renet4.generated_Graphs.add(g2);
            g.child_Graphs.add(g1);
            g1.level = level + 1;
            g1.kind_of_reduction = 0;
            g1.reduced_edge = edgenr;
            Renet4.generated_Graphs.add(g1);
            Renet4.factProb = Renet4.factProb + "r" + edgenr + "(";
            prob = p1 * (getProbabilityFact(g1, level + 1));
            Renet4.factProb = Renet4.factProb + "+(1-r" + edgenr + ")(";
            prob = prob + (1 - p2) * (getProbabilityFact(g2, level + 1));
        }

        if (i_reduced == false) //Fall 3
        {
            int cnt = 0;
            MyIterator itprob = g1.getEdgelist().iterator();
            while (itprob.hasNext()) {
                cnt = 1;
                Edge e = (Edge) itprob.next();
                Renet4.factProb = Renet4.factProb + "r" + e.edge_no;
                prob = prob * e.prob;
            }
            prob = prob * cnt;
        }
        Renet4.factProb = Renet4.factProb + ")";
        return prob;
    }

    public static float[] drawTreeofGraphs(Graph g) {
        float[] result = new float[2];
        float[] result_child_left = new float[2];
        float[] result_child_right = new float[2];
        Graph child_left = null;
        Graph child_right = null;

        MyIterator it = g.child_Graphs.iterator();
        while (it.hasNext()) {
            Graph child = (Graph) it.next();
            if (child.kind_of_reduction == 0)
                child_left = child;
            if (child.kind_of_reduction == 1)
                child_right = child;
        }

        if (child_left != null && child_right != null) {
            result_child_left = drawTreeofGraphs(child_left);
            result_child_right = drawTreeofGraphs(child_right);
            g.offset = 2 + result_child_left[1] + result_child_right[0];
            g.left_offset = g.offset / 2 + result_child_left[0];
            g.right_offset = g.offset / 2 + result_child_right[1];
            result[0] = g.left_offset;
            result[1] = g.right_offset;
            return result;
        }

        if (child_left != null && child_right == null) {
            result_child_left = drawTreeofGraphs(child_left);
            g.offset = 2 + result_child_left[1];
            g.left_offset = g.offset / 2 + result_child_left[0];
            g.right_offset = 0;
            result[0] = g.left_offset;
            result[1] = 0;
            return result;
        }

        if (child_left == null && child_right != null) {
            result_child_right = drawTreeofGraphs(child_right);
            g.offset = 2 + result_child_right[0];
            g.left_offset = 0;
            g.right_offset = g.offset / 2 + result_child_right[1];
            result[0] = 0;
            result[1] = g.right_offset;
            return result;
        }

        /*if (child_left==null && child_right==null)*/
        g.offset = 0;
        g.left_offset = 0;
        g.right_offset = 0;
        result[0] = 0;
        result[1] = 0;
        return result;
    }


    private static float getEdgeProbability(Edge edge, Graph g) {
        float prob = 1f;

        Edge edge1, edge2, edge3, edge4, edge5, edge6;
        edge1 = edge.parent_edges[0];
        edge2 = edge.parent_edges[1];
        edge3 = edge.parent_edges[2];
        edge4 = edge.parent_edges[3];
        edge5 = edge.parent_edges[4];
        edge6 = edge.parent_edges[5];

        if (edge1 == null || edge2 == null)
            prob = edge.prob;
        else {
            edge1.prob = getEdgeProbability(edge1, g);
            edge2.prob = getEdgeProbability(edge2, g);
            if (edge3 != null)
                edge3.prob = getEdgeProbability(edge3, g);
            if (edge4 != null)
                edge4.prob = getEdgeProbability(edge4, g);
            if (edge5 != null)
                edge5.prob = getEdgeProbability(edge5, g);
            if (edge6 != null)
                edge6.prob = getEdgeProbability(edge6, g);

            String type = edge.rdcType;
            if (type.equals("S")) {
                prob = edge1.prob * edge2.prob;
                if (g.reducetype.indexOf('S') < 0)
                    g.reducetype = g.reducetype + "S";
            } else if (type.equals("P")) {
                prob = 1 - ((1 - edge1.prob) * (1 - edge2.prob));
                if (g.reducetype.indexOf('P') < 0)
                    g.reducetype = g.reducetype + "P";
            } else if (type.equals("K")) {
                prob = edge1.prob * edge2.prob / (1 - (1 - edge1.prob) * (1 - edge2.prob));
                if (g.reducetype.indexOf('K') < 0)
                    g.reducetype = g.reducetype + "K";
                float u1 = 1 - edge1.prob;
                float u2 = 1 - edge2.prob;
                //u=1-r, defektwahrscheinlichkeit, s.a Seite 127, HD
                g.kprob = g.kprob * (1 - u1 * u2);
            } else if (type.equals("R1") || type.equals("R2")) {
                float p1 = edge1.prob;
                float p2 = edge2.prob;
                float p3 = edge3.prob;
                float q1 = 1f - p1;
                float q2 = 1f - p2;
                float q3 = 1f - p3;
                float f1 = p1 * p2 * p3 + q1 * p2 * p3 + p1 * q2 * p3 + p1 * p2 * q3;
                float f2 = q1 * p2 * q3;
                float f3 = p1 * q2 * q3;
                prob = f1 / (f2 + f1);
                if (g.wedgeIndex.contains(edge)) {
                    g.wedge = g.wedge * ((f1 + f2) * (f1 + f3) / f1);
                    g.wedgeIndex.remove(edge);
                    if (g.reducetype.indexOf('1') < 0 && type.equals("R1"))
                        g.reducetype = g.reducetype + "1";
                    if (g.reducetype.indexOf('2') < 0 && type.equals("R2"))
                        g.reducetype = g.reducetype + "2";
                }
            } else if (type.equals("S1") || type.equals("S2")) {
                float p1 = edge1.prob;
                float p2 = edge2.prob;
                float p3 = edge3.prob;
                float q1 = 1f - p1;
                float q2 = 1f - p2;
                float q3 = 1f - p3;
                float f1 = p1 * p2 * p3 + q1 * p2 * p3 + p1 * q2 * p3 + p1 * p2 * q3;
                float f2 = q1 * p2 * q3;
                float f3 = p1 * q2 * q3;
                prob = f1 / (f3 + f1);
                Edge index = edge.r;
                if (g.wedgeIndex.contains(index)) {
                    g.wedge = g.wedge * ((f1 + f2) * (f1 + f3) / f1);
                    g.wedgeIndex.remove(index);
                    if (g.reducetype.indexOf('1') < 0 && type.equals("S1"))
                        g.reducetype = g.reducetype + "1";
                    if (g.reducetype.indexOf('2') < 0 && type.equals("S2"))
                        g.reducetype = g.reducetype + "2";
                }
            } else if (type.equals("R3") || type.equals("S3")) {
                float p1 = edge1.prob;
                float p2 = edge2.prob;
                float p3 = edge3.prob;
                float p4 = edge4.prob;
                float q1 = 1f - p1;
                float q2 = 1f - p2;
                float q3 = 1f - p3;
                float q4 = 1f - p4;
                float f1 = p1 * p2 * p3 * p4 + q1 * p2 * p3 * p4 + p1 * q2 * p3 * p4 + p1 * p2 * q3 * p4 + p1 * p2 * p3 * q4;
                float f2 = p1 * q2 * q3 * p4 + q1 * p2 * p3 * q4 + q1 * p2 * q3 * p4;
                float f3 = p1 * q2 * p3 * q4;
                Edge index = null;
                if (type.equals("R3")) {
                    prob = f1 / (f2 + f1);
                    index = edge;
                } else if (type.equals("S3")) {
                    prob = f1 / (f3 + f1);
                    index = edge.r;
                }
                if (g.wedgeIndex.contains(index)) {
                    g.wedge = g.wedge * ((f1 + f2) * (f1 + f3) / f1);
                    g.wedgeIndex.remove(index);
                    if (g.reducetype.indexOf('3') < 0)
                        g.reducetype = g.reducetype + "3";
                }
            } else if (type.equals("R4") || type.equals("S4") || type.equals("T4")) {
                float p1 = edge1.prob;
                float p2 = edge2.prob;
                float p3 = edge3.prob;
                float p4 = edge4.prob;
                float q1 = 1f - p1;
                float q2 = 1f - p2;
                float q3 = 1f - p3;
                float q4 = 1f - p4;
                float f1 = p1 * p2 * p3 * p4 + q1 * p2 * p3 * p4 + p1 * q2 * p3 * p4 + p1 * p2 * q3 * p4 + p1 * p2 * p3 * q4;
                float f2 = q1 * p2 * q3 * p4;
                float f3 = p1 * q2 * q3 * p4 + q1 * p2 * p3 * q4;
                float f4 = p1 * q2 * p3 * q4;
                Edge index = null;
                if (type.equals("R4")) {
                    prob = f1 / (f2 + f1);
                    index = edge;
                } else if (type.equals("S4")) {
                    prob = f1 / (f3 + f1);
                    index = edge.r;
                } else if (type.equals("T4")) {
                    prob = f1 / (f1 + f4);
                    index = edge.r;
                }
                if (g.wedgeIndex.contains(index)) {
                    g.wedge = g.wedge * ((f1 + f2) * (f1 + f3) * (f1 + f4) / (f1 * f1));
                    g.wedgeIndex.remove(index);
                    if (g.reducetype.indexOf('4') < 0)
                        g.reducetype = g.reducetype + "4";
                }
            } else if (type.equals("R5") || type.equals("S5") || type.equals("T5")) {
                float p1 = edge1.prob;
                float p2 = edge2.prob;
                float p3 = edge3.prob;
                float p4 = edge4.prob;
                float q1 = 1f - p1;
                float q2 = 1f - p2;
                float q3 = 1f - p3;
                float q4 = 1f - p4;
                float f1 = p1 * p2 * p3 * p4 + q1 * p2 * p3 * p4 + p1 * q2 * p3 * p4 + p1 * p2 * q3 * p4 + p1 * p2 * p3 * q4;
                float f2 = q1 * p2 * q3 * q4;
                float f3 = p1 * q2 * p3 * q4;
                float f4 = p1 * p2 * q3 * q4;
                Edge index = null;
                if (type.equals("R5")) {
                    index = edge;
                    if (g.getCnodeSize() > 2)
                        prob = f1 / (f2 + f1);
                    else if (g.getCnodeSize() == 2)
                        prob = (p2 + p1 * q2 * p3 * p4) / (p2 + p1 * q2 * p3);
                } else if (type.equals("S5")) {
                    prob = f1 / (f3 + f1);
                    index = edge.r;
                } else if (type.equals("T5")) {
                    prob = f1 / (f1 + f4);
                    index = edge.r;
                }
                if (g.wedgeIndex.contains(index)) {
                    if (g.getCnodeSize() > 2)
                        g.wedge = g.wedge * ((f1 + f2) * (f1 + f3) * (f1 + f4) / (f1 * f1));
                    else if (g.getCnodeSize() == 2)
                        g.wedge = g.wedge * (p2 + p1 * q2 * p3);
                    g.wedgeIndex.remove(index);
                    if (g.reducetype.indexOf('5') < 0)
                        g.reducetype = g.reducetype + "5";
                }
            } else if (type.equals("R6") || type.equals("S6") || type.equals("T6")) {
                float p1 = edge1.prob;
                float p2 = edge2.prob;
                float p3 = edge3.prob;
                float p4 = edge4.prob;
                float p5 = edge5.prob;
                float q1 = 1f - p1;
                float q2 = 1f - p2;
                float q3 = 1f - p3;
                float q4 = 1f - p4;
                float q5 = 1f - p5;
                float f1 = p1 * p2 * p3 * p4 * p5 + q1 * p2 * p3 * p4 * p5 + p1 * q2 * p3 * p4 * p5 + p1 * p2 * q3 * p4 * p5 + p1 * p2 * p3 * q4 * p5 + p1 * p2 * p3 * p4 * q5;
                float f2 = q1 * p2 * p3 * q4 * p5;
                float f3 = p1 * q2 * p3 * (p4 * q5 + q4 * p5) + p2 * (q1 * p3 * p4 * q5 + p1 * q3 * q4 * p5);
                float f4 = p1 * p2 * q3 * p4 * q5;
                Edge index = null;
                if (type.equals("R6")) {
                    prob = f1 / (f2 + f1);
                    index = edge;
                } else if (type.equals("S6")) {
                    prob = f1 / (f3 + f1);
                    index = edge.r;
                } else if (type.equals("T6")) {
                    prob = f1 / (f1 + f4);
                    index = edge.r;
                }
                if (g.wedgeIndex.contains(index)) {
                    g.wedge = g.wedge * ((f1 + f2) * (f1 + f3) * (f1 + f4) / (f1 * f1));
                    g.wedgeIndex.remove(index);
                    if (g.reducetype.indexOf('6') < 0)
                        g.reducetype = g.reducetype + "6";
                }
            } else if (type.equals("R7") || type.equals("S7") || type.equals("T7")) {
                float p1 = edge1.prob;
                float p2 = edge2.prob;
                float p3 = edge3.prob;
                float p4 = edge4.prob;
                float p5 = edge5.prob;
                float p6 = edge6.prob;
                float q1 = 1f - p1;
                float q2 = 1f - p2;
                float q3 = 1f - p3;
                float q4 = 1f - p4;
                float q5 = 1f - p5;
                float q6 = 1f - p6;
                float f1 = p1 * p2 * p3 * p4 * p5 * p6 * (1 + q1 / p1 + q2 / p2 + q3 / p3 + q4 / p4 + q5 / p5 + q6 / p6);
                float f2 = q1 * p2 * p3 * q4 * p5 * p6;
                float f3 = p1 * q2 * p3 * (q4 * p5 * p6 + p4 * q5 * p6 + p4 * p5 * q6) + p1 * p2 * q3 * p6 * (p4 * q5 + q4 * p5) +
                        q1 * p2 * p3 * p4 * (q5 * p6 + p5 * q6);
                float f4 = p1 * p2 * q3 * p4 * p5 * q6;
                Edge index = null;
                if (type.equals("R7")) {
                    prob = f1 / (f2 + f1);
                    index = edge;
                } else if (type.equals("S7")) {
                    prob = f1 / (f3 + f1);
                    index = edge.r;
                } else if (type.equals("T7")) {
                    prob = f1 / (f1 + f4);
                    index = edge.r;
                }
                if (g.wedgeIndex.contains(index)) {
                    g.wedge = g.wedge * ((f1 + f2) * (f1 + f3) * (f1 + f4) / (f1 * f1));
                    g.wedgeIndex.remove(index);
                    if (g.reducetype.indexOf('7') < 0)
                        g.reducetype = g.reducetype + "7";
                }
            }
        }//else
        return (prob);
    }


    public static Edge getCommonEdge(Node node1, Node node2) {
        Edge e = null;
        MyIterator it = node1.node_edge.iterator();
        while (it.hasNext()) {
            Edge e1 = (Edge) it.next();
            if (e1.left_node.equals(node2) || e1.right_node.equals(node2)) {
                e = e1;
                break;
            }
        }
        return e;
    }

    public static Node getCommonNode(Node node1, Node node2) {
        Node node3 = null;
        MySet nodes = new MySet();
        MyIterator it = node1.node_edge.iterator();
        while (it.hasNext()) {
            Edge e = (Edge) it.next();
            Node n = getOtherNode(e, node1);
            nodes.add(n);
        }
        it = node2.node_edge.iterator();
        while (it.hasNext()) {
            Edge e = (Edge) it.next();
            Node n = getOtherNode(e, node2);
            if (nodes.contains(n)) {
                node3 = n;
                break;
            }
        }
        return node3;
    }

    /**
     * Describe <code>getOtherNode</code> method here.
     *
     * @param edge an <code>Edge</code> value
     * @param node a <code>com.resinet.Node</code> value
     * @return a <code>com.resinet.Node</code> value
     */
    public static Node getOtherNode(Edge edge, Node node) {
        Node node2 = null;

        if (node == edge.left_node)
            node2 = edge.right_node;
        else {
            if (node == edge.right_node)
                node2 = edge.left_node;
            else {
                System.out.println("Error in the method com.resinet.Util.getOtherNode, line 241");
                System.exit(1);
            }
        }
        return (node2);
    }

}
