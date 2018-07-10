public class Edge {
    private int weight;
    private Node toNode;
    private Node fromNode;
    private int count;
    private int id;

    public Edge(Node FromNode, Node ToNode, int edgeWeight, int newId) {
        weight = edgeWeight;
        toNode = ToNode;
        fromNode = FromNode;
        id = newId;
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
        return "Edge " + id + ": " + fromNode.toString() + " -> " + toNode.toString() + " (Weight = " + weight+", Count = "+count+")";
    }


}
