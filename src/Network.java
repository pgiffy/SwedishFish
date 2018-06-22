import java.io.PrintWriter;
import java.util.*;
public class Network {
    ArrayList<Edge> edges;
    ArrayList<Node> nodes;
    public Network(){
        edges = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    public void addNode(){
        nodes.add(new Node(nodes.size()));
    }
    public void addEdge(Node fromNode, Node toNode, int weight){
        Edge newEdge = new Edge(fromNode, toNode, weight);
        edges.add(newEdge);
        fromNode.addEdge(newEdge);
    }

    public void reducePath(){}

    /**
     * Prints network details to specified output file
     */
    public void printDetails(PrintWriter out) {
        for(Edge edge: edges) {
            out.println(edge.toString());
        }
    }

    public int numNodes(){
        return nodes.size();
    }
    public int numEdges(){
        return edges.size();
    }

    public Node getNode(int id){
        return nodes.get(id);
    }
    public Edge getEdge(Node fromNode, Node toNode){
        return fromNode.findEdge(toNode);
    }

    public void makeDAG(){}
    public void topoSort(){}



    public void isolateEdges(){//possilby use later
         }


}
