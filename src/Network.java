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
    public Edge addEdge(Node fromNode, Node toNode, int weight){
        Edge newEdge = new Edge(fromNode, toNode, weight);
        edges.add(newEdge);
        fromNode.addEdge(newEdge);
        return newEdge;
    }

    //for the use of removing back edges and maybe double isolated edges later
    public void reduceEdge(Node fromNode, Node toNode) {
        Edge temp = getEdge(fromNode, toNode);
        ArrayList<Node> tempRemove = new ArrayList<>();
        tempRemove.add(fromNode);
        tempRemove.add(toNode);
        Path remove = new Path(tempRemove, temp.getWeight());
        reducePath(remove);
        System.out.println(temp.toString());
    }


    public void reducePath(Path toReduce){


    public void reducePath(Path toReduce) {
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

        for(Node n: nodes) {
            out.println(n.printEdges()+" ");
        }
        out.println("*******");
        for(Edge e: edges) {
            out.println(e.toString());
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
        return fromNode.findOutgoingEdge(toNode);
    }

    public ArrayList getNodes(){ return nodes; }
    public ArrayList getEdges(){ return edges; }

    public void removeEdge(Edge e) {
        //int toNodeId = e.getToNode().getId();
        //int fromNodeId = e.getFromNode().getId();

        //remove from network edges list
        edges.remove(e);

        //remove from outgoing edge list (in node)
        e.getFromNode().getOutgoingEdges().remove(e);

        //remove from incoming edge list (in node)
        e.getToNode().getIncomingEdges().remove(e);
    }


    public void removeNode(Node node) {
        Edge incomingEdge = node.getIncomingEdges().get(0);
        Edge outgoingEdge = node.getOutgoingEdges().get(0);
        Node fromNode = incomingEdge.getFromNode();
        Node toNode = outgoingEdge.getToNode();
        int weight = incomingEdge.getWeight();

        //re-route edges around removed node
        Edge oldIncomingEdge = getEdge(fromNode, node);
        oldIncomingEdge.setToNode(toNode);
        //oldIncomingEdge.incrementCount();
        Edge oldOutgoingEdge = getEdge(node, toNode);
        oldOutgoingEdge.setFromNode(fromNode);
        oldOutgoingEdge.incrementCount();

        //delete incoming/outgoing edges from removed node
        node.getIncomingEdges().remove(incomingEdge);
        node.getOutgoingEdges().remove(outgoingEdge);

        System.out.println("UPDATED: " + node.printEdges());
        System.out.println("fromNode = " + fromNode.printEdges());
        System.out.println("toNode = " + toNode.printEdges());

    }

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
        for(Edge e: nodes.get(i).getOutgoingEdges()) {
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

    public void collapseEdges() {
        ArrayList<Node> toRemove = new ArrayList<>();
        for(Node node: nodes) {
            if(node.numIncomingEdges() == 1 && node.numOutgoingEdges() == 1) {
                toRemove.add(node);
                //removeNode(node);
            }
        }

        for(Node n: toRemove) {
            removeNode(n);
        }

        //remove edges that should have been removed before
        ArrayList<Edge> edgesTemp = new ArrayList<>();
        ArrayList<Edge> removedEdges = new ArrayList<>();
        edgesTemp.addAll(edges);
        for(Edge e: edges) {
            Node toNode = e.getToNode();
            Node fromNode = e.getFromNode();
            if(toRemove.contains(toNode) || toRemove.contains(fromNode)) {
                edgesTemp.remove(e);
                removedEdges.add(e);
            }
        }

        edges = edgesTemp;

        //remove duplicate edges
        ArrayList<Edge> foundEdges = new ArrayList<>();
        for(Edge e: edges) {
            if(!edgeIsFound(foundEdges, e)) {
                foundEdges.add(e);
            }

        }

        edges = foundEdges;

        findMatchingEdges(removedEdges);

        //remove & renumber nodes
        int i = 0;
        ArrayList<Node> tempNodes = new ArrayList<>();
        tempNodes.addAll(nodes);
        for(Node n: nodes) {
            if(n.numOutgoingEdges() == 0 && n.numIncomingEdges() == 0) {
                tempNodes.remove(n);
            } else {
                n.setId(i);
                i++;
            }
        }
        nodes = tempNodes;

    }

    private void findMatchingEdges(ArrayList<Edge> removedEdges) {
        System.out.println(removedEdges.toString());
        for(Edge e: removedEdges) {
            Node toNode = e.getToNode();
            Node fromNode = e.getFromNode();
            int weight = e.getWeight();

            for(Edge foundEdge: edges) {
                if((foundEdge.getToNode() == toNode || foundEdge.getFromNode() == fromNode) && foundEdge.getWeight() == weight) {
                    foundEdge.incrementCount();
                    break;
                }
            }
        }

    }

    private boolean edgeIsFound(ArrayList<Edge> foundEdges, Edge edge) {
        Node fromNode = edge.getFromNode();
        Node toNode = edge.getToNode();
        int weight = edge.getWeight();

        for(Edge e: foundEdges) {
            if(e.getFromNode() == fromNode && e.getToNode() == toNode && e.getWeight() == weight)
                return true;
        }

        return false;
    }


}
