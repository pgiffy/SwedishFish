import java.io.PrintWriter;
import java.util.*;
public class Network {

    private ArrayList<Edge> edges;
    private ArrayList<Node> nodes;

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

    public void reducePath(Path toReduce) {
        int pathWeight = toReduce.getWeight();
        for(Edge e: toReduce.getEdges()) {
            int weight = e.getWeight();
            if(weight - pathWeight <= 0) {
                removeEdge(e);
            } else {
                System.out.println("SET WEIGHT: " + e.toString() + " | " + (weight-pathWeight));
                e.setWeight(weight - pathWeight);
                System.out.println(e.toString());
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

    public Edge getEdge(Node fromNode, Node toNode, int weight){
        return fromNode.findOutgoingEdge(toNode, weight);
    }

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

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
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
