public class Edge {
    private int weight;
    private Node toNode;
    private Node fromNode;

    public Edge(Node FromNode, Node ToNode, int edgeWeight) {
        weight = edgeWeight;
        toNode = ToNode;
        fromNode = FromNode;
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

    public String toString() { return "Edge: " + fromNode.toString() + " -> " + toNode.toString() + " (Weight = " + weight+")"; }
}
