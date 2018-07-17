import java.util.*;
public class Edge {
    private int weight;
    private Node toNode;
    private Node fromNode;
    private int count;
    private ArrayList<Integer> paths = new ArrayList<>();

    public Edge(Node FromNode, Node ToNode, int edgeWeight) {
        weight = edgeWeight;
        toNode = ToNode;
        fromNode = FromNode;
    }

    public void removePossible(int remove){ paths.remove(new Integer(remove)); }

    public void addPath(int flow){ paths.add(flow); }

    public void addAllPaths(ArrayList<Integer> flows){ paths.addAll(flows); }

    public ArrayList<Integer> getPaths(){ return paths; }

    public void incrementCount() {
        count++;
    }

    public void setWeight(int newWeight) {
        weight = newWeight;
    }

    public int getWeight() {
        return weight;
    }

    public Node getToNode() {
        return toNode;
    }

    public Node getFromNode() {
        return fromNode;
    }

    public void setToNode(Node newToNode) {
        toNode = newToNode;
    }

    public void setFromNode(Node newFromNode) {
        fromNode = newFromNode;
    }

    public String toString() {
        return "Edge: " + fromNode.toString() + " -> " + toNode.toString() + " (Weight = " + weight+", Count = "+count+")";
    }


}
