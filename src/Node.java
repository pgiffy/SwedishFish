import java.util.*;
public class Node {
    ArrayList<Edge> edges = new ArrayList<>();
    ArrayList<Node> nodes = new ArrayList<>();//for the creation of a dag
    int id;
    public Node(int nodeId){
        id = nodeId;
    }

    public void addEdge(Edge newEdge){
        edges.add(newEdge);
        nodes.add(newEdge.getToNode());
    }

    public void reduceEdge(Node toNode, int reduceWeight){
        //should not need to remove from node array here because nodes are only used at beginning
        Edge toReduce = findEdge(toNode);
        if(toReduce.getWeight() == reduceWeight) edges.remove(toReduce);
        else toReduce.setWeight(toReduce.getWeight()-reduceWeight);
    }

    public ArrayList<Edge> getEdges(){ return edges; }
    public ArrayList<Node> getToNodes(){ return nodes; }

    public Edge findEdge(Node toNode){
        for(int i = 0; i < edges.size(); i++){
            if(edges.get(i).getToNode() == toNode){
                return edges.get(i);
            }
        }
        return null;
    }

    public String toString() {
        return "<"+id+">";
    }


}
