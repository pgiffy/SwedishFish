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

    public ArrayList<Node> getToNodes(){
        ArrayList<Node> toNodes = new ArrayList<>();
        for(Edge e: outgoingEdges) {
            toNodes.add(e.getToNode());
        }
        return toNodes;
    }

    public ArrayList<Node> getFromNodes() {
        ArrayList<Node> fromNodes = new ArrayList<>();
        for(Edge e: incomingEdges) {
            fromNodes.add(e.getFromNode());
        }
        return fromNodes;
    }

    public Edge findOutgoingEdge(Node toNode){
        for(int i = 0; i < outgoingEdges.size(); i++){
            if(outgoingEdges.get(i).getToNode() == toNode){
                return outgoingEdges.get(i);
            }
        }
        return null;
    }


}
