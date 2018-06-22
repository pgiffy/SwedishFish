import java.util.*;
public class Node {
    ArrayList<Edge> edges = new ArrayList<Edge>();
    int id;
    public Node(int nodeId){
        id = nodeId;
    }

    public void addEdge(Edge newEdge){
        edges.add(newEdge);
    }

    public void reduceEdge(Node toNode, int reduceWeight){
        Edge toReduce = findEdge(toNode);
        if(toReduce.getWeight() == reduceWeight) edges.remove(toReduce);
        else toReduce.setWeight(toReduce.getWeight()-reduceWeight);
    }

    public ArrayList<Edge> getEdges(){
        return edges;
    }
    public Edge findEdge(Node toNode){
        for(int i = 0; i < edges.size(); i++){
            if(edges.get(i).getToNode() == toNode){
                return edges.get(i);
            }
        }
        return null;
    }


}
