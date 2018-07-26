import java.util.ArrayList;

public class Edge {
    private int weight;
    private Node toNode;
    private Node fromNode;
    private int count;
    private int id;
    private String label;
    private ArrayList<Edge> removedEdges;

    public Edge(Node FromNode, Node ToNode, int edgeWeight, int newId) {
        weight = edgeWeight;
        toNode = ToNode;
        fromNode = FromNode;
        id = newId;
        removedEdges = new ArrayList<>();
    }

    public Edge(Node FromNode, Node ToNode, int edgeWeight, int newId, String newLabel) {
        weight = edgeWeight;
        toNode = ToNode;
        fromNode = FromNode;
        id = newId;
        label = newLabel;
        removedEdges = new ArrayList<>();
    }

    public Edge(Node FromNode, Node ToNode, int edgeWeight, int newId, String newLabel, ArrayList<Edge> newRemovedEdges) {
        weight = edgeWeight;
        toNode = ToNode;
        fromNode = FromNode;
        id = newId;
        label = newLabel;
        removedEdges = new ArrayList<>();
        removedEdges.addAll(newRemovedEdges);
    }

    public void addRemovedEdge(Edge e) {
        removedEdges.add(e);
    }

    public ArrayList<Edge> getRemovedEdges() {
        return removedEdges;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }

    public int getId() {
        return id;
    }

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
        //return "Edge " + id + ": " + fromNode.toString() + " -> " + toNode.toString() + " (Weight = " + weight+", Count = "+count+")";
        return getLabel();
    }


}
