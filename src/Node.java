import java.util.*;

public class Node {

    private ArrayList<Edge> outgoingEdges = new ArrayList<>();
    private ArrayList<Edge> incomingEdges = new ArrayList<>();
    private int id;

    public Node(int nodeId){ id = nodeId; }

    public void addEdge(Edge newEdge){ outgoingEdges.add(newEdge); }

    public void addIncomingEdge(Edge newEdge) { incomingEdges.add(newEdge); }

    public ArrayList<Edge> getIncomingEdges() {return incomingEdges;}
    public ArrayList<Edge> getOutgoingEdges() {return outgoingEdges;}

    public void removeIncomingEdge(Edge e) { incomingEdges.remove(e); }
    public void removeOutgoingEdge(Edge e) { outgoingEdges.remove(e); }

    public int numIncomingEdges() { return incomingEdges.size(); }

    public int numOutgoingEdges() { return outgoingEdges.size(); }

    public int getId(){ return id; }

    public void setId(int newId) { id = newId; }

    public String toString() { return "<" + id + ">"; }
}
