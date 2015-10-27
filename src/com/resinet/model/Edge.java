package com.resinet.model;/* Edge.java */

import com.resinet.Node;

import java.io.Serializable;

public class Edge implements Serializable {

    public int edge_no;
    public Node left_node;
    public Node right_node;
    // Knoten einer Kante

    public Edge q_left;
    public Edge q_right;

    public Edge left, right;
    //for com.resinet.KTree.java

    public boolean b_marked = false;
    public boolean in_q = false;
    //Mark fuer K-Baum (com.resinet.KTree.java)

    public boolean useless = false;
    public boolean deleted = false;
    public boolean marked = false;

    public Edge[] parent_edges;
    //bei der Reduktion werden zwei Kanten eliminiert, und entsteht eine neue
    public String rdcType;
    //Art der Reduktion, S, K, oder P
    public Edge r = null;
    //fuer Polygon-Ketten-Reduktion
    public float prob;
    //Intaktwahrscheinlichkeit der Kante

    public int m = 0;

    public Edge(int edge_no) {
        this.edge_no = edge_no;
        parent_edges = new Edge[6];
    }

}




