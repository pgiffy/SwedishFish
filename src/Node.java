import java.util.*;

public class Node {

    private ArrayList<Edge> outgoingEdges = new ArrayList<>();
    private ArrayList<Edge> incomingEdges = new ArrayList<>();
    private ArrayList<Integer> possibleFlows = new ArrayList<>();
    private int id;
    private boolean isVisited;

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean newVal) {
        isVisited = newVal;
    }


    public Node(int nodeId){
        id = nodeId;
    }

    public void addEdge(Edge newEdge){
        outgoingEdges.add(newEdge);
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

    public void addPossible(int possible){ possibleFlows.add(possible); }
    public void addAllPossible(ArrayList<Integer> possible){possibleFlows.addAll(possible);}

    public ArrayList<Integer> getPossible(){ return possibleFlows; }

    public ArrayList<Edge> getIncomingEdges() {return incomingEdges;}
    public ArrayList<Edge> getOutgoingEdges() {return outgoingEdges;}

    public void removeIncomingEdge(Edge e) { incomingEdges.remove(e); }
    public void removeOutgoingEdge(Edge e) { outgoingEdges.remove(e); }

    public int numIncomingEdges() { return incomingEdges.size(); }

    public int numOutgoingEdges() { return outgoingEdges.size(); }

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

    public int getId(){ return id; }

    public void setId(int newId) { id = newId; }

    public Edge findOutgoingEdge(Node toNode){
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
