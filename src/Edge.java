import java.util.*;
public class Edge {
    int weight;
    Node toNode;
    Node fromNode;
    public Edge(Node FromNode, Node ToNode, int edgeWeight) {
        weight = edgeWeight;
        toNode = ToNode;
        fromNode = FromNode;
    }

    public void setWeight(int newWeight){weight = newWeight;}

    //these for possible future merging edges
    public void setToNode(Node newToNode){toNode = newToNode;}
    public void setFromNode(Node newFromNode){fromNode = newFromNode;}

    public int getWeight(){return weight;}
    public Node getToNode(){return toNode;}
    public Node getFromNode(){return fromNode;}
    public String toString(){return "Edge: " + fromNode.toString() + " -> " + toNode.toString() + " (Weight = " + weight+")";}


}
