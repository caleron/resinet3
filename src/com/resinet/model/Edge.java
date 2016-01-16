package com.resinet.model;/* Edge.java */

import java.io.Serializable;
import java.math.BigDecimal;

public class Edge implements Serializable {

    public final int edge_no;

    // Knoten einer Kante
    public Node left_node,right_node;

    public Edge q_left;
    public Edge q_right;

    public Edge left, right;
    //for com.resinet.model.KTree.java

    public boolean b_marked = false;
    public boolean in_q = false;
    //Mark fuer K-Baum (com.resinet.model.KTree.java)

    public boolean useless = false;


    public BigDecimal prob;
    //Intaktwahrscheinlichkeit der Kante

    public Edge(int edge_no) {
        this.edge_no = edge_no;
    }

}




