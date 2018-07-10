import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
public class Network {

    private ArrayList<Edge> edges;
    private ArrayList<Node> nodes;
    private char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public Network(){
        edges = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    public Network(Network network){
        edges = new ArrayList<>();
        nodes = new ArrayList<>();
        ArrayList<Edge> oldEdges = network.getEdges();
        int numNodes = network.numNodes();

        for(int i = 0; i < numNodes; i++) {
            addNode();
        }

        for(Edge e: oldEdges) {
            int oldFromId = e.getFromNode().getId();
            int oldToId = e.getToNode().getId();
            Node fromNode = getNode(oldFromId);
            Node toNode = getNode(oldToId);
            int weight = e.getWeight();
            addEdge(fromNode, toNode, weight);
        }

    }

    public void addNode(){
        nodes.add(new Node(nodes.size()));
    }

    public void addEdge(Node fromNode, Node toNode, int weight){
        Edge newEdge = new Edge(fromNode, toNode, weight, edges.size());
        edges.add(newEdge);
        fromNode.addEdge(newEdge);
        toNode.addIncomingEdge(newEdge);
    }

    public void reducePath(Path toReduce) {
        int pathWeight = toReduce.getWeight();
        for(Edge e: toReduce.getEdges()) {
            int weight = e.getWeight();
            if(weight - pathWeight <= 0) {
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

        for(Node n: nodes) {
            out.println(n.printEdges()+" ");
        }
        out.println("*******");
        for(Edge e: edges) {
            out.println(e.toString());
        }
    }

    public String toString() {
        String str = "Network: " + numEdges() + " edges, " + numNodes() + " nodes\n";
        for(Edge e: edges) {
            str += e.toString()+" ";
        }

        return str;
    }

    public int getMinEdge() {
        int minWeight = -1;
        for(Edge e: edges) {
            if(e.getWeight() < minWeight || minWeight < 0) {
                minWeight = e.getWeight();
            }
        }

        return minWeight;
    }

    public int getMaxEdge() {
        int maxWeight = -1;
        for(Edge e: edges) {
            if(e.getWeight() > maxWeight || maxWeight < 0) {
                maxWeight = e.getWeight();
            }
        }

        return maxWeight;
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
                removeNode(node);
            }
        }

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

    public ArrayList<Integer> ValsToEnd(){
        ArrayList<Integer> vals = new ArrayList<>();
        for(Edge e: edges){
            if(e.getToNode().getId() == nodes.size()-1){
                vals.add(e.getWeight());
            }

        }
        return vals;
    }

    public ArrayList<Integer> ValsFromZero(){
        ArrayList<Integer> vals = new ArrayList<>();
        for(Edge e: edges){
            if(e.getFromNode().getId() == 0){
                    vals.add(e.getWeight());
            }
        }
        return vals;
    }


    public ArrayList<Integer> possibleVals(){
        ArrayList<Integer> toLast = new ArrayList<>();
        ArrayList<Integer> fromFirst = new ArrayList<>();
        ArrayList<Integer> toCheck = new ArrayList<>();

        for(Edge e: edges) if(e.getFromNode().getId() == 0) fromFirst.add(e.getWeight());

        for(Edge e: edges) if(e.getToNode().getId() == nodes.size()) toLast.add(e.getWeight());

        ArrayList<Integer> toRemoveFirst = new ArrayList<>();
        for(Integer i: fromFirst){
            if(toLast.contains(i)){
                toCheck.add(i);
                toRemoveFirst.add(i);
                toLast.remove(i);
            }
        }

        for(Integer i: toRemoveFirst) fromFirst.remove(i);

        ArrayList<Integer> temp = new ArrayList<>();

        for(Integer i: fromFirst){
            temp = findPairs(toLast, i);
            toCheck.addAll(temp);
        }
        for(Integer i: toLast){
            temp = findPairs(fromFirst, i);
            toCheck.addAll(temp);
        }

        return toCheck;
    }

    public static ArrayList<Integer> findPairs(ArrayList<Integer> nums, int sum){
        ArrayList<Integer> toReturn = new ArrayList<>();
        HashSet<Integer> s = new HashSet<Integer>();
        for (int i: nums)
        {
            int temp = sum-i;

            // checking for condition
            if (temp>=0 && s.contains(temp))
            {
               toReturn.add(i);
               toReturn.add(temp);
            }
            s.add(i);
        }
        return toReturn;
    }

    public void printDOT(String filename) {
        File outputFile = new File(filename);
        PrintWriter out = null;

        try {
            out = new PrintWriter(outputFile);

            out.println("digraph G {");

            for(Edge e: edges) {
                int fromNodeId = e.getFromNode().getId();
                int toNodeId = e.getToNode().getId();
                int weight = e.getWeight();
                out.printf("\t%d -> %d [label=\"%d\"]\n", fromNodeId, toNodeId, weight);
            }

            out.println("}");
        } catch (FileNotFoundException e) {
            System.out.println("file not found.");
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    public void printDOT(String filename, ArrayList<Path> paths) {
        File outputFile = new File(filename);
        PrintWriter out = null;
        String[] colors = {"green", "blue", "yellow", "purple", "pink", "orange", "navy", "aquamarine", "cyan", "lightsteelblue1"};
        HashMap<Integer, String> edgeColors = new HashMap<>();

        try {
            out = new PrintWriter(outputFile);

            out.println("digraph G {");

            //initially set all edges to black
            for(Edge e: edges) {
                edgeColors.put(e.getId(), "black");
            }

            int i = 0;
            for(Path p: paths) {
                String color;
                if(i > 9) color = "red";
                else color = colors[i];
                //System.out.println(color);

                for(Edge e: p.getEdges()) {
                    edgeColors.put(e.getId(), color);
                    //System.out.println(e.toString());
                    int fromNodeId = e.getFromNode().getId();
                    int toNodeId = e.getToNode().getId();
                    int weight = e.getWeight();
                    out.printf("\t%d -> %d [label=\"%d\", color=\"%s\"]\n", fromNodeId, toNodeId, weight, color);
                }

                i++;
            }

            out.println("}");
        } catch (FileNotFoundException e) {
            System.out.println("file not found.");
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

}
