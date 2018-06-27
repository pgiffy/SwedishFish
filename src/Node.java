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
        Edge toReduce = findOutgoingEdge(toNode);
        if(toReduce.getWeight() == reduceWeight) outgoingEdges.remove(toReduce);
        else toReduce.setWeight(toReduce.getWeight()-reduceWeight);
    }

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

    public int getId(){ return id; }
    public void setId(int newId) {
        id = newId;
    }

    public Edge findOutgoingEdge(Node toNode){
        for(int i = 0; i < outgoingEdges.size(); i++){
            if(outgoingEdges.get(i).getToNode() == toNode){
                return outgoingEdges.get(i);
            }
        }
        return null;
    }

    public Edge findOutgoingEdge(Node toNode, int weight){
        for(int i = 0; i < outgoingEdges.size(); i++){
            if(outgoingEdges.get(i).getToNode() == toNode && outgoingEdges.get(i).getWeight() == weight){
                return outgoingEdges.get(i);
            }
        }
        return null;
    }

    public Edge findIncomingEdge(Node fromNode) {
        for(int i = 0; i < incomingEdges.size(); i++){
            if(incomingEdges.get(i).getToNode() == fromNode){
                return incomingEdges.get(i);
            }
        }
        return null;
    }

    public String toString() {
        return "<"+id+">";
    }

    public String printEdges() {
        String str = "["+id+": ";
        for(Edge e: incomingEdges) {
            str += e.toString()+" ";
        }
        for(Edge e: outgoingEdges) {
            str += e.toString();
        }
        str += "]";
        return str;
    }


}
