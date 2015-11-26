package com.resinet.model;/* com.resinet.model.Node.java */

import com.resinet.util.MyList;

import java.io.Serializable;

public class Node implements Serializable {
    public int node_no;
    public boolean c_node = false;
    // Kennzeichen fuer Konnektionsknoten

    public Node left, right;

    public MyList node_edge = new MyList();

    public int xposition;
    public int yposition;
    public int degree = 0;

    int kt_nd = -1;

    public boolean marked = false;

    public boolean b_marked = false;
    //mark fuer K-Baum (com.resinet.model.KTree.java)

    //Intaktwahrscheinlichkeit
    public float prob;

    public boolean useless = false;

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
