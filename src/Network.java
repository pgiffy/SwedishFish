import java.io.PrintWriter;
import java.util.*;
public class Network {
    private int edgesRemoved = 0;
    private ArrayList<Edge> edges;
    private ArrayList<Node> nodes;
    //for tracking backedges
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

    //for the use of removing back edges and maybe double isolated edges later
    public void reduceEdge(Node fromNode, Node toNode){
        Edge temp = getEdge(fromNode, toNode);
        ArrayList<Node> tempRemove = new ArrayList<>();
        tempRemove.add(fromNode);
        tempRemove.add(toNode);
        Path remove = new Path(tempRemove, temp.getWeight());
        reducePath(remove);
        System.out.println(temp.toString());
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
    public ArrayList getNodes(){ return nodes; }
    public ArrayList getEdges(){ return edges; }

    //this adds visited nodes and nodes on stack to the list to check if back edges exist
    //also adds the next set of children to the stack
    public boolean removeBackEdgesUtil(int i, boolean[] visited, boolean[] recursiveStack){
        if (recursiveStack[i]) {
            //if nodes is already in the recursive stack then it means there is a loop
            return true;
        }
        if (visited[i])
            return false;
        visited[i] = true;
        recursiveStack[i] = true;
        ArrayList<Node> children = nodes.get(i).getToNodes();
        //recursive check for children
        for (Node c: children){
            if(removeBackEdgesUtil(c.getId(), visited,recursiveStack)){

                reduceEdge(nodes.get(i), c);
                return true;
            }
        }
        recursiveStack[i] = false;

        return false;
    }


    public void removeBackEdges(){
        boolean[] visited = new boolean[nodes.size()];
        boolean[] recursiveStack = new boolean[nodes.size()];

        for(int i = 0; i < nodes.size(); i++){
            if(removeBackEdgesUtil(i, visited, recursiveStack)){
                System.out.println("This is now a DAG");
            }
        }
        System.out.println("This was already a DAG");
    }


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
            int j = e.getToNode().getId();
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
