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

    public void reducePath(Path toReduce){
        ArrayList<Node> pathNodes = toReduce.getNodes();
        int reduceWeight = toReduce.getWeight();
        for(int i = 0; i < pathNodes.size()-1; i++){
            if(getEdge(pathNodes.get(i),pathNodes.get(i+1)).getWeight()-reduceWeight == 0){
                //if edge is 0 then it is removed
                edges.remove(getEdge(pathNodes.get(i),pathNodes.get(i+1)));
                //the reduce edge method in node should handle this delete but check later
                pathNodes.get(i).reduceEdge(pathNodes.get(i+1),reduceWeight);
            }else {
                //just reduces the weight of an edge if it will not go to zero
                getEdge(pathNodes.get(i), pathNodes.get(i + 1)).setWeight(getEdge(pathNodes.get(i), pathNodes.get(i + 1)).getWeight() - reduceWeight);
                //doing the same for the node
                pathNodes.get(i).reduceEdge(pathNodes.get(i+1),reduceWeight);
            }
        }
    }

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

    public ArrayList<Integer> topoSort() {
        Stack stack = new Stack();
        boolean[] visited = new boolean[numNodes()];
        for(int i = 0; i < visited.length; i++) {
            visited[i] = false;
        }

        for(int i = 0; i < numNodes(); i++) {
            if(visited[i] == false) {
                topoSortVertex(i, visited, stack);
            }
        }

        ArrayList<Integer> sortedList = new ArrayList<>();
        while(!stack.empty()) {
            sortedList.add((int) stack.pop());
        }

        return sortedList;
    }

    private void topoSortVertex(int i, boolean[] visited, Stack stack) {
        visited[i] = true;
        for(Edge e: nodes.get(i).getEdges()) {
            int j = e.toNode.id;
            if(visited[j] == false) {
                topoSortVertex(j, visited, stack);
            }
        }

        stack.push(i);
    }



    public void isolateEdges(){
        //possibly use later
         }


}
