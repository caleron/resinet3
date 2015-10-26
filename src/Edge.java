/* Edge.java */

import java.io.Serializable;

public class Edge implements Serializable {

    int edge_no;
    Node left_node;
    Node right_node;
    // Knoten einer Kante

    Edge q_left;
    Edge q_right;

    Edge left, right;
    //for KTree.java

    boolean b_marked = false;
    boolean in_q = false;
    //Mark fuer K-Baum (KTree.java)

    boolean useless = false;
    boolean deleted = false;
    boolean marked = false;

    Edge[] parent_edges;
    //bei der Reduktion werden zwei Kanten eliminiert, und entsteht eine neue
    String rdcType;
    //Art der Reduktion, S, K, oder P
    Edge r = null;
    //fuer Polygon-Ketten-Reduktion
    float prob;
    //Intaktwahrscheinlichkeit der Kante

    int m = 0;

    Edge(int edge_no) {
        this.edge_no = edge_no;
        parent_edges = new Edge[6];
    }

}




