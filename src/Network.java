import java.io.PrintWriter;
import java.util.*;
import java.io.*;
public class Network {

    private ArrayList<Edge> edges;
    private ArrayList<Node> nodes;

    public Network() {
        edges = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    public Network(Network network) {
        edges = new ArrayList<>();
        nodes = new ArrayList<>();
        ArrayList<Edge> oldEdges = network.getEdges();
        int numNodes = network.numNodes();

        for (int i = 0; i < numNodes; i++) {
            addNode();
        }

        for (Edge e : oldEdges) {
            int oldFromId = e.getFromNode().getId();
            int oldToId = e.getToNode().getId();
            Node fromNode = getNode(oldFromId);
            Node toNode = getNode(oldToId);
            int weight = e.getWeight();
            addEdge(fromNode, toNode, weight);
        }

    }

    public void addNode() {
        nodes.add(new Node(nodes.size()));
    }

    public void addEdge(Node fromNode, Node toNode, int weight) {
        Edge newEdge = new Edge(fromNode, toNode, weight);
        edges.add(newEdge);
        fromNode.addEdge(newEdge);
        toNode.addIncomingEdge(newEdge);
    }

    public void addEdge(Edge e) {
        edges.add(e);
        Node fromNode = e.getFromNode();
        Node toNode = e.getToNode();
        fromNode.addEdge(e);
        toNode.addIncomingEdge(e);
    }

    public void reducePath(Path toReduce) {
        int pathWeight = toReduce.getWeight();
        for (Edge e : toReduce.getEdges()) {
            int weight = e.getWeight();
            if (weight - pathWeight <= 0) {
                removeEdge(e);
            } else {
                //System.out.println("SET WEIGHT: " + e.toString() + " | " + (weight-pathWeight));
                e.setWeight(weight - pathWeight);
                //System.out.println(e.toString());
            }
        }
    }

    /**
     * Prints network details to specified output file
     */
    public void printDetails(PrintWriter out) {

        for (Node n : nodes) {
            out.println(n.printEdges() + " ");
        }
        out.println("*******");
        for (Edge e : edges) {
            out.println(e.toString());
        }
    }

    public String toString() {
        String str = "Network: " + numEdges() + " edges, " + numNodes() + " nodes\n";
        for (Edge e : edges) {
            str += e.toString() + " ";
        }

        return str;
    }

    public int getMinEdge() {
        int minWeight = -1;
        for (Edge e : edges) {
            if (e.getWeight() < minWeight || minWeight < 0) {
                minWeight = e.getWeight();
            }
        }

        return minWeight;
    }

    public int getMaxEdge() {
        int maxWeight = -1;
        for (Edge e : edges) {
            if (e.getWeight() > maxWeight || maxWeight < 0) {
                maxWeight = e.getWeight();
            }
        }

        return maxWeight;
    }

    public int numNodes() {
        return nodes.size();
    }

    public int numEdges() {
        return edges.size();
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    public Edge getEdge(Node fromNode, Node toNode) {
        return fromNode.findOutgoingEdge(toNode);
    }

    public Edge getEdge(Edge e) {
        return e.getFromNode().findOutgoingEdge(e.getToNode());
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void removeEdge(Edge e) {
        //remove from network edges list
        edges.remove(e);

        //remove from outgoing edge list (in node)
        e.getFromNode().removeOutgoingEdge(e);

        //remove from incoming edge list (in node)
        e.getToNode().removeIncomingEdge(e);
    }


    public void removeNode(Node node) {
        Node fromNode = node.getFromNodes().get(0);
        Node toNode = node.getToNodes().get(0);
        Edge outGoing = getEdge(node, toNode);
        Edge inComing = getEdge(fromNode, node);
        addEdge(fromNode, toNode, inComing.getWeight());
        removeEdge(outGoing);
        removeEdge(inComing);
        //System.out.println("UPDATED: " + node.printEdges());
        //System.out.println("fromNode = " + fromNode.printEdges());
        //System.out.println("toNode = " + toNode.printEdges());
    }


    public ArrayList<Integer> topoSort() {
        Stack stack = new Stack();
        boolean[] visited = new boolean[numNodes()];
        for (int i = 0; i < visited.length; i++) {
            visited[i] = false;
        }

        for (int i = 0; i < numNodes(); i++) {
            if (visited[i] == false) {
                topoSortVertex(i, visited, stack);
            }
        }

        ArrayList<Integer> sortedList = new ArrayList<>();
        while (!stack.empty()) {
            sortedList.add((int) stack.pop());
        }

        return sortedList;
    }

    private void topoSortVertex(int i, boolean[] visited, Stack stack) {
        visited[i] = true;
        for (Edge e : nodes.get(i).getOutgoingEdges()) {
            int j = e.getToNode().getId();
            if (visited[j] == false) {
                topoSortVertex(j, visited, stack);
            }
        }

        stack.push(i);
    }

    public void collapseEdges() {
        for (Node node : nodes) {
            if (node.numIncomingEdges() == 1 && node.numOutgoingEdges() == 1) {
                removeNode(node);
            }
        }

        //remove & renumber nodes
        int i = 0;
        ArrayList<Node> tempNodes = new ArrayList<>();
        tempNodes.addAll(nodes);
        for (Node n : nodes) {
            if (n.numOutgoingEdges() == 0 && n.numIncomingEdges() == 0) {
                tempNodes.remove(n);
            } else {
                n.setId(i);
                i++;
            }
        }
        nodes = tempNodes;
    }

    public void collapseEdges2() {
        for (Node node : nodes) {
            ArrayList<Integer> weightIncoming = new ArrayList<>();
            ArrayList<Integer> weightOutgoing = new ArrayList<>();
            for (Edge e : node.getOutgoingEdges()) weightOutgoing.add(e.getWeight());
            for (Edge e : node.getIncomingEdges()) weightIncoming.add(e.getWeight());
            if (compareEdges(weightIncoming, weightOutgoing)) removeNodes2(node);
        }

        int i = 0;
        ArrayList<Node> tempNodes = new ArrayList<>();
        tempNodes.addAll(nodes);
        for (Node n : nodes) {
            if (n.numOutgoingEdges() == 0 && n.numIncomingEdges() == 0) {
                tempNodes.remove(n);
            } else {
                n.setId(i);
                i++;
            }
        }
        nodes = tempNodes;
    }

    public void removeNodes2(Node node) {

        ArrayList<Edge> eToRemove = new ArrayList<>();
        ArrayList<Edge> jToRemove = new ArrayList<>();
        boolean checker = false;
        for (Edge e : node.getIncomingEdges()) {
            for (Edge j : node.getOutgoingEdges()) {
                if (e.getWeight() == j.getWeight()) {
                    checker = true;
                    Node fromNode = e.getFromNode();
                    Node toNode = j.getToNode();
                    int weight = e.getWeight();
                    addEdge(fromNode, toNode, weight);
                    eToRemove.add(e);
                    jToRemove.add(j);
                    break;
                }
            }
            if (checker) {
                checker = false;
                for (Edge j : jToRemove) removeEdge(j);
                jToRemove.clear();
                continue;
            }
        }
        for (Edge e : eToRemove) removeEdge(e);

    }

    public boolean compareEdges(ArrayList<Integer> list1, ArrayList<Integer> list2) {
        //null checking
        if (list1 == null && list2 == null) return true;
        if ((list1 == null && list2 != null) || (list1 != null && list2 == null)) return false;

        if (list1.size() != list2.size()) return false;

        for (Integer itemList1 : list1) {
            if (!list2.contains(itemList1)) return false;
        }

        return true;
    }


    public ArrayList<Integer> getAllEdgeWeight() {
        ArrayList<Integer> edgeWeight = new ArrayList<>();
        for (Edge e : edges) {
            if (!edgeWeight.contains(e.getWeight())) {
                edgeWeight.add(e.getWeight());
            }
        }
        return edgeWeight;
    }

    public void breakItDown() {
        for (int currentNode : topoSort()) {
            if (getNode(currentNode).numIncomingEdges() > 1 && getNode(currentNode).numOutgoingEdges() == 1) {
                Node newEnd = getNode(currentNode).getOutgoingEdges().get(0).getToNode();
                removeEdge(getNode(currentNode).getOutgoingEdges().get(0));
                for (Edge e : getNode(currentNode).getIncomingEdges()) {
                    addEdge(getNode(currentNode), newEnd, e.getWeight());
                }
            }
            if (getNode(currentNode).numIncomingEdges() == 1 && getNode(currentNode).numOutgoingEdges() > 1) {
                Node newEnd = getNode(currentNode).getIncomingEdges().get(0).getFromNode();
                removeEdge(getNode(currentNode).getIncomingEdges().get(0));
                for (Edge e : getNode(currentNode).getOutgoingEdges()) {
                    addEdge(newEnd, getNode(currentNode), e.getWeight());
                }
            }
        }

    }
    public void uglyBanana(){
        for(int currentNode: topoSort()){
            ArrayList<Integer> incoming = new ArrayList<>();
            ArrayList<Integer> outgoing = new ArrayList<>();
            ArrayList<Edge> incomingEdge = new ArrayList<>();
            ArrayList<Edge> outgoingEdge = new ArrayList<>();
            ArrayList<Integer> toCollapse = new ArrayList<>();
            for(Edge e: getNode(currentNode).getIncomingEdges()){
                incoming.add(e.getWeight());
                incomingEdge.add(e);
            }
            for(Edge e: getNode(currentNode).getOutgoingEdges()){
                outgoing.add(e.getWeight());
                outgoingEdge.add(e);
            }


            for(int i: incoming) {
                if(outgoing.contains(i)) {
                    toCollapse.add(i);
                    continue;
                }
            }
            removeDuplicates(toCollapse);

            for(int i: toCollapse){
                Edge in = null;
                Edge out = null;
                for(Edge e: outgoingEdge){
                    if(e.getWeight()==i){
                        out = e;
                        break;
                    }
                }
                for(Edge e: incomingEdge){
                    if(e.getWeight()==i){
                        in = e;
                        break;
                    }
                }
                addEdge(in.getFromNode(), out.getToNode(), i);
                removeEdge(in);
                removeEdge(out);
            }
        }
    }
    private static ArrayList<Integer> removeDuplicates(ArrayList<Integer> remove){

        Set<Integer> noDuplicate = new HashSet<>();
        noDuplicate.addAll(remove);
        remove.clear();
        remove.addAll(noDuplicate);
        return remove;
    }

}

