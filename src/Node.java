import java.util.*;
public class Node {
    private ArrayList<Edge> outgoingEdges = new ArrayList<>();
    private ArrayList<Edge> incomingEdges = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();//for the creation of a dag
    private int id;
    public Node(int nodeId){
        id = nodeId;
    }

    public void addEdge(Edge newEdge){
        outgoingEdges.add(newEdge);
        nodes.add(newEdge.getToNode());
        newEdge.getToNode().addIncomingEdge(newEdge);
    }

    public void addIncomingEdge(Edge newEdge) {
        incomingEdges.add(newEdge);
    }

    public void reduceEdge(Node toNode, int reduceWeight){
        //should not need to remove from node array here because nodes are only used at beginning
        Edge toReduce = findEdge(toNode);
        if(toReduce.getWeight() == reduceWeight) outgoingEdges.remove(toReduce);
        else toReduce.setWeight(toReduce.getWeight()-reduceWeight);
    }

    public ArrayList<Edge> getEdges(){ return outgoingEdges; }

    public ArrayList<Edge> getIncomingEdges() {return incomingEdges;}
    public ArrayList<Edge> getOutgoingEdges() {return outgoingEdges;}

    public int numIncomingEdges() {
        return incomingEdges.size();
    }

    public int numOutgoingEdges() {
        return outgoingEdges.size();
    }

    public ArrayList<Node> getToNodes(){ return nodes; }

    public ArrayList<Node> getFromNodes() {
        ArrayList<Node> fromNodes = new ArrayList<>();
        for(Edge e: incomingEdges) {
            fromNodes.add(e.getFromNode());
        }

        return fromNodes;
    }

    public int getId(){return id; }

    public Edge findEdge(Node toNode){
        for(int i = 0; i < outgoingEdges.size(); i++){
            if(outgoingEdges.get(i).getToNode() == toNode){
                return outgoingEdges.get(i);
            }
        }
        return null;
    }

    public String toString() {
        return "<"+id+">";
    }


}
