package com.resinet;/* com.resinet.Node.java */

import com.resinet.model.Edge;

import java.io.Serializable;

public class Node implements Serializable {
    public int node_no;
    public boolean c_node = false;
    // Kennzeichen fuer Konnektionsknoten

    Node left, right;

    MyList node_edge = new MyList();

    public int xposition;
    public int yposition;
    public int degree = 0;

    int kt_nd = -1;

    public boolean marked = false;

    public boolean b_marked = false;
    //mark fuer K-Baum (com.resinet.KTree.java)

    boolean useless = false;

    Node(int node_no) {
        this.node_no = node_no;
    }

    protected void add_Edge(Edge edge) {
        node_edge.add(edge);
        degree = node_edge.size();
    }

    void delete_Edge(Edge edge) {
        node_edge.remove(edge);
        degree = node_edge.size();
    }

}
