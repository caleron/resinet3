package com.resinet.model;/* com.resinet.model.Node.java */

import com.resinet.util.MyList;

import java.io.Serializable;
import java.math.BigDecimal;

public class Node implements Serializable {
    //Knotennummer
    public int node_no;

    // Kennzeichen fuer Konnektionsknoten
    public boolean c_node = false;

    //Attribut für KTree
    public Node left, right;
    //Anliegende Kanten
    public MyList node_edge = new MyList();

    //Grad des Knotens, d.h. Anzahl anliegender Kanten
    public int degree = 0;

    //Für die Tiefensuche beim Prüfen, ob der Graph zusammenhängend ist
    public boolean marked = false;

    //mark fuer K-Baum (com.resinet.model.KTree.java)
    public boolean b_marked = false;

    //Intaktwahrscheinlichkeit
    public BigDecimal prob;

    //Für die Baumsuche, wird aber niemals ausgelesen
    public boolean useless = false;

    public Node(int node_no, boolean c_node) {
        this.node_no = node_no;
        this.c_node = c_node;
    }

    public Node(int node_no) {
        this.node_no = node_no;
    }

    public void add_Edge(Edge edge) {
        node_edge.add(edge);
        degree = node_edge.size();
    }

    void delete_Edge(Edge edge) {
        node_edge.remove(edge);
        degree = node_edge.size();
    }

}
