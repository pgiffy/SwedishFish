import java.io.PrintWriter;
import java.util.*;
import java.io.*;
public class Network {

    private ArrayList<Edge> edges;
    private ArrayList<Node> nodes;
    static ArrayList<ArrayList<Integer>> allSubsets = new ArrayList<>();
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
            if (!list2.contains(itemList1)){
                return false;
            }else{
                list2.remove(new Integer(itemList1));
            }
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
        //finds matching incoming and outgoing edges of a node
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

    public void subsetGod2(){
         //calculate subsets for pairs of paths that the incoming or outgoing edges are at least double
        //compare sets to work
        for(int i: topoSort()){
            if(getNode(i).getOutgoingEdges().size() == 2 && getNode(i).getOutgoingEdges().size() < getNode(i).getIncomingEdges().size()){
                ArrayList <Integer> incomingWeights = new ArrayList<>();
                ArrayList<ArrayList<Integer>> one;
                ArrayList<ArrayList<Integer>> two;
                for(Edge e: getNode(i).getIncomingEdges()) incomingWeights.add(e.getWeight());
                int[] arrayIncoming = new int[incomingWeights.size()];
                for (int j = 0; j < arrayIncoming.length; j++) arrayIncoming[j] = incomingWeights.get(j);
                int n = arrayIncoming.length;
                int oneWeight = getNode(i).getOutgoingEdges().get(0).getWeight();
                int twoWeight = getNode(i).getOutgoingEdges().get(1).getWeight();
                printAllSubsets(arrayIncoming, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayIncoming, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() && two.isEmpty()) continue;
                ArrayList<Integer> all = new ArrayList<>();
                boolean checker = false;
                ArrayList<Edge> oneEdges = new ArrayList<>();
                ArrayList<Edge> twoEdges = new ArrayList<>();
                Node oneEnd = null;
                Node twoEnd = null;
                for(ArrayList<Integer> arr1: one){
                    for(ArrayList<Integer> arr2: two){
                        all.addAll(arr1);
                        all.addAll(arr2);
                        if(compareEdges(all, incomingWeights)){
                            oneEnd = getNode(i).getOutgoingEdges().get(0).getToNode();
                            twoEnd = getNode(i).getOutgoingEdges().get(1).getToNode();
                            //Get all the edges with weights in one and two
                            for(Edge e : getNode(i).getIncomingEdges()){
                                if(arr1.contains(new Integer(e.getWeight()))){
                                    oneEdges.add(e);
                                    arr1.remove(new Integer(e.getWeight()));
                                    continue;
                                }
                                if(arr2.contains(new Integer(e.getWeight()))){
                                    twoEdges.add(e);
                                    arr2.remove(new Integer(e.getWeight()));
                                    continue;
                                }
                            }
                            checker = true;
                            break;
                        }
                        all.clear();

                    }
                    if(checker) break;
                }
                if(checker == false) continue;
                ArrayList<Edge> toRemove = new ArrayList<>();
                toRemove.addAll(getNode(i).getOutgoingEdges());
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: oneEdges){
                    addEdge(e.getFromNode(), oneEnd, e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: twoEdges){
                    addEdge(e.getFromNode(),twoEnd,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();

            }
            if(getNode(i).getIncomingEdges().size() == 2 && getNode(i).getIncomingEdges().size() < getNode(i).getOutgoingEdges().size()){
                ArrayList <Integer> outGoingWeights = new ArrayList<>();
                ArrayList<ArrayList<Integer>> one;
                ArrayList<ArrayList<Integer>> two;
                for(Edge e: getNode(i).getOutgoingEdges()) outGoingWeights.add(e.getWeight());
                int[] arrayOutgoing = new int[outGoingWeights.size()];
                for (int j = 0; j < arrayOutgoing.length; j++) arrayOutgoing[j] = outGoingWeights.get(j);
                int n = arrayOutgoing.length;
                int oneWeight = getNode(i).getIncomingEdges().get(0).getWeight();
                int twoWeight = getNode(i).getIncomingEdges().get(1).getWeight();
                printAllSubsets(arrayOutgoing, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayOutgoing, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() && two.isEmpty()) continue;
                ArrayList<Integer> all = new ArrayList<>();
                boolean checker = false;
                ArrayList<Edge> oneEdges = new ArrayList<>();
                ArrayList<Edge> twoEdges = new ArrayList<>();
                Node oneStart = null;
                Node twoStart = null;
                for(ArrayList<Integer> arr1: one){
                    for(ArrayList<Integer> arr2: two){
                        all.addAll(arr1);
                        all.addAll(arr2);
                        if(compareEdges(all, outGoingWeights)){
                            oneStart = getNode(i).getIncomingEdges().get(0).getFromNode();
                            twoStart = getNode(i).getIncomingEdges().get(1).getFromNode();
                            //Get all the edges with weights in one and two
                            for(Edge e : getNode(i).getOutgoingEdges()){
                                if(arr1.contains(new Integer(e.getWeight()))){
                                    oneEdges.add(e);
                                    arr1.remove(new Integer(e.getWeight()));
                                    continue;
                                }
                                if(arr2.contains(new Integer(e.getWeight()))){
                                    twoEdges.add(e);
                                    arr2.remove(new Integer(e.getWeight()));
                                    continue;
                                }
                            }
                            checker = true;
                            break;
                        }
                        all.clear();

                    }
                    if(checker) break;
                }
                if(checker == false) continue;
                ArrayList<Edge> toRemove = new ArrayList<>();
                toRemove.addAll(getNode(i).getIncomingEdges());
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: oneEdges){
                    addEdge(oneStart, e.getToNode(), e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: twoEdges){
                    addEdge(twoStart, e.getToNode() ,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
            }
        }
    }

    public void subsetGod3(){
        for(int i: topoSort()){
            if(getNode(i).getOutgoingEdges().size() == 3 && getNode(i).getOutgoingEdges().size() < getNode(i).getIncomingEdges().size()){
                ArrayList <Integer> incomingWeights = new ArrayList<>();
                ArrayList<ArrayList<Integer>> one;
                ArrayList<ArrayList<Integer>> two;
                ArrayList<ArrayList<Integer>> three;
                for(Edge e: getNode(i).getIncomingEdges()) incomingWeights.add(e.getWeight());
                int[] arrayIncoming = new int[incomingWeights.size()];
                for (int j = 0; j < arrayIncoming.length; j++) arrayIncoming[j] = incomingWeights.get(j);
                int n = arrayIncoming.length;
                int oneWeight = getNode(i).getOutgoingEdges().get(0).getWeight();
                int twoWeight = getNode(i).getOutgoingEdges().get(1).getWeight();
                int threeWeight = getNode(i).getOutgoingEdges().get(2).getWeight();
                printAllSubsets(arrayIncoming, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayIncoming, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayIncoming, n, threeWeight);
                three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() && two.isEmpty() && three.isEmpty()) continue;
                ArrayList<Integer> all = new ArrayList<>();
                boolean checker = false;
                ArrayList<Edge> oneEdges = new ArrayList<>();
                ArrayList<Edge> twoEdges = new ArrayList<>();
                ArrayList<Edge> threeEdges = new ArrayList<>();
                Node oneEnd = null;
                Node twoEnd = null;
                Node threeEnd = null;
                for(ArrayList<Integer> arr1: one){
                    for(ArrayList<Integer> arr2: two){
                        for(ArrayList<Integer> arr3: three) {
                            all.addAll(arr1);
                            all.addAll(arr2);
                            all.addAll(arr3);
                            if (compareEdges(all, incomingWeights)) {
                                oneEnd = getNode(i).getOutgoingEdges().get(0).getToNode();
                                twoEnd = getNode(i).getOutgoingEdges().get(1).getToNode();
                                threeEnd = getNode(i).getOutgoingEdges().get(2).getToNode();
                                for (Edge e : getNode(i).getIncomingEdges()) {
                                    if (arr1.contains(new Integer(e.getWeight()))) {
                                        oneEdges.add(e);
                                        arr1.remove(new Integer(e.getWeight()));
                                        continue;
                                    }
                                    if (arr2.contains(new Integer(e.getWeight()))) {
                                        twoEdges.add(e);
                                        arr2.remove(new Integer(e.getWeight()));
                                        continue;
                                    }
                                    if (arr3.contains(new Integer(e.getWeight()))) {
                                        threeEdges.add(e);
                                        arr3.remove(new Integer(e.getWeight()));
                                        continue;
                                    }
                                }
                                checker = true;
                                break;
                            }
                            all.clear();
                        }
                        if(checker) break;
                    }
                    if(checker) break;
                }
                if(checker == false) continue;
                ArrayList<Edge> toRemove = new ArrayList<>();
                toRemove.addAll(getNode(i).getOutgoingEdges());
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: oneEdges){
                    addEdge(e.getFromNode(), oneEnd, e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: twoEdges){
                    addEdge(e.getFromNode(),twoEnd,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: threeEdges){
                    addEdge(e.getFromNode(),threeEnd,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
            }
            if(getNode(i).getIncomingEdges().size() == 3 && getNode(i).getIncomingEdges().size() < getNode(i).getOutgoingEdges().size()){
                ArrayList <Integer> outGoingWeights = new ArrayList<>();
                ArrayList<ArrayList<Integer>> one;
                ArrayList<ArrayList<Integer>> two;
                ArrayList<ArrayList<Integer>> three;
                for(Edge e: getNode(i).getOutgoingEdges()) outGoingWeights.add(e.getWeight());
                int[] arrayOutgoing = new int[outGoingWeights.size()];
                for (int j = 0; j < arrayOutgoing.length; j++) arrayOutgoing[j] = outGoingWeights.get(j);
                int n = arrayOutgoing.length;
                int oneWeight = getNode(i).getIncomingEdges().get(0).getWeight();
                int twoWeight = getNode(i).getIncomingEdges().get(1).getWeight();
                int threeWeight = getNode(i).getOutgoingEdges().get(2).getWeight();
                printAllSubsets(arrayOutgoing, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayOutgoing, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayOutgoing, n, threeWeight);
                three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() && two.isEmpty() && three.isEmpty()) continue;
                ArrayList<Integer> all = new ArrayList<>();
                boolean checker = false;
                ArrayList<Edge> oneEdges = new ArrayList<>();
                ArrayList<Edge> twoEdges = new ArrayList<>();
                ArrayList<Edge> threeEdges = new ArrayList<>();
                Node oneStart = null;
                Node twoStart = null;
                Node threeStart = null;
                for(ArrayList<Integer> arr1: one){
                    for(ArrayList<Integer> arr2: two){
                        for(ArrayList<Integer> arr3: three) {
                            all.addAll(arr1);
                            all.addAll(arr2);
                            all.addAll(arr3);
                            if (compareEdges(all, outGoingWeights)) {
                                oneStart = getNode(i).getIncomingEdges().get(0).getFromNode();
                                twoStart = getNode(i).getIncomingEdges().get(1).getFromNode();
                                threeStart = getNode(i).getIncomingEdges().get(2).getFromNode();
                                //Get all the edges with weights in one and two
                                for (Edge e : getNode(i).getOutgoingEdges()) {
                                    if (arr1.contains(new Integer(e.getWeight()))) {
                                        oneEdges.add(e);
                                        arr1.remove(new Integer(e.getWeight()));
                                        continue;
                                    }
                                    if (arr2.contains(new Integer(e.getWeight()))) {
                                        twoEdges.add(e);
                                        arr2.remove(new Integer(e.getWeight()));
                                        continue;
                                    }
                                    if (arr3.contains(new Integer(e.getWeight()))) {
                                        threeEdges.add(e);
                                        arr3.remove(new Integer(e.getWeight()));
                                        continue;
                                    }
                                }
                                checker = true;
                                break;
                            }
                            all.clear();
                        }
                        if(checker) break;
                    }
                    if(checker) break;
                }
                if(checker == false) continue;
                ArrayList<Edge> toRemove = new ArrayList<>();
                toRemove.addAll(getNode(i).getIncomingEdges());
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: oneEdges){
                    addEdge(oneStart, e.getToNode(), e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: twoEdges){
                    addEdge(twoStart, e.getToNode() ,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: threeEdges){
                    addEdge(threeStart, e.getToNode() ,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
            }
        }
    }

    public void subsetGod4(){
        for(int i: topoSort()){
            if(getNode(i).getOutgoingEdges().size() == 4 && getNode(i).getOutgoingEdges().size() < getNode(i).getIncomingEdges().size()){
                ArrayList <Integer> incomingWeights = new ArrayList<>();
                ArrayList<ArrayList<Integer>> one;
                ArrayList<ArrayList<Integer>> two;
                ArrayList<ArrayList<Integer>> three;
                ArrayList<ArrayList<Integer>> four;
                for(Edge e: getNode(i).getIncomingEdges()) incomingWeights.add(e.getWeight());
                int[] arrayIncoming = new int[incomingWeights.size()];
                for (int j = 0; j < arrayIncoming.length; j++) arrayIncoming[j] = incomingWeights.get(j);
                int n = arrayIncoming.length;
                int oneWeight = getNode(i).getOutgoingEdges().get(0).getWeight();
                int twoWeight = getNode(i).getOutgoingEdges().get(1).getWeight();
                int threeWeight = getNode(i).getOutgoingEdges().get(2).getWeight();
                int fourWeight = getNode(i).getOutgoingEdges().get(3).getWeight();
                printAllSubsets(arrayIncoming, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayIncoming, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayIncoming, n, threeWeight);
                three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayIncoming, n, fourWeight);
                four = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() && two.isEmpty() && three.isEmpty() && four.isEmpty()) continue;
                ArrayList<Integer> all = new ArrayList<>();
                boolean checker = false;
                ArrayList<Edge> oneEdges = new ArrayList<>();
                ArrayList<Edge> twoEdges = new ArrayList<>();
                ArrayList<Edge> threeEdges = new ArrayList<>();
                ArrayList<Edge> fourEdges = new ArrayList<>();
                Node oneEnd = null;
                Node twoEnd = null;
                Node threeEnd = null;
                Node fourEnd = null;
                for(ArrayList<Integer> arr1: one){
                    for(ArrayList<Integer> arr2: two){
                        for(ArrayList<Integer> arr3: three) {
                            for(ArrayList<Integer> arr4: four) {
                                all.addAll(arr1);
                                all.addAll(arr2);
                                all.addAll(arr3);
                                all.addAll(arr4);
                                if (compareEdges(all, incomingWeights)) {
                                    oneEnd = getNode(i).getOutgoingEdges().get(0).getToNode();
                                    twoEnd = getNode(i).getOutgoingEdges().get(1).getToNode();
                                    threeEnd = getNode(i).getOutgoingEdges().get(2).getToNode();
                                    fourEnd = getNode(i).getOutgoingEdges().get(3).getToNode();
                                    for (Edge e : getNode(i).getIncomingEdges()) {
                                        if (arr1.contains(new Integer(e.getWeight()))) {
                                            oneEdges.add(e);
                                            arr1.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                        if (arr2.contains(new Integer(e.getWeight()))) {
                                            twoEdges.add(e);
                                            arr2.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                        if (arr3.contains(new Integer(e.getWeight()))) {
                                            threeEdges.add(e);
                                            arr3.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                        if (arr4.contains(new Integer(e.getWeight()))) {
                                            fourEdges.add(e);
                                            arr4.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                    }
                                    checker = true;
                                    break;
                                }
                                all.clear();
                            }
                            if(checker) break;
                        }
                        if(checker) break;
                    }
                    if(checker) break;
                }
                if(checker == false) continue;
                ArrayList<Edge> toRemove = new ArrayList<>();
                toRemove.addAll(getNode(i).getOutgoingEdges());
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: oneEdges){
                    addEdge(e.getFromNode(), oneEnd, e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: twoEdges){
                    addEdge(e.getFromNode(),twoEnd,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: threeEdges){
                    addEdge(e.getFromNode(),threeEnd,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: fourEdges){
                    addEdge(e.getFromNode(),fourEnd,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
            }
            if(getNode(i).getIncomingEdges().size() == 4 && getNode(i).getIncomingEdges().size() < getNode(i).getOutgoingEdges().size()){
                ArrayList <Integer> outGoingWeights = new ArrayList<>();
                ArrayList<ArrayList<Integer>> one;
                ArrayList<ArrayList<Integer>> two;
                ArrayList<ArrayList<Integer>> three;
                ArrayList<ArrayList<Integer>> four;
                for(Edge e: getNode(i).getOutgoingEdges()) outGoingWeights.add(e.getWeight());
                int[] arrayOutgoing = new int[outGoingWeights.size()];
                for (int j = 0; j < arrayOutgoing.length; j++) arrayOutgoing[j] = outGoingWeights.get(j);
                int n = arrayOutgoing.length;
                int oneWeight = getNode(i).getIncomingEdges().get(0).getWeight();
                int twoWeight = getNode(i).getIncomingEdges().get(1).getWeight();
                int threeWeight = getNode(i).getOutgoingEdges().get(2).getWeight();
                int fourWeight = getNode(i).getOutgoingEdges().get(3).getWeight();
                printAllSubsets(arrayOutgoing, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayOutgoing, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayOutgoing, n, threeWeight);
                three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                printAllSubsets(arrayOutgoing, n, fourWeight);
                four = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() && two.isEmpty() && three.isEmpty() && four.isEmpty()) continue;
                ArrayList<Integer> all = new ArrayList<>();
                boolean checker = false;
                ArrayList<Edge> oneEdges = new ArrayList<>();
                ArrayList<Edge> twoEdges = new ArrayList<>();
                ArrayList<Edge> threeEdges = new ArrayList<>();
                ArrayList<Edge> fourEdges = new ArrayList<>();
                Node oneStart = null;
                Node twoStart = null;
                Node threeStart = null;
                Node fourStart = null;
                for(ArrayList<Integer> arr1: one){
                    for(ArrayList<Integer> arr2: two){
                        for(ArrayList<Integer> arr3: three) {
                            for(ArrayList<Integer> arr4: four) {
                                all.addAll(arr1);
                                all.addAll(arr2);
                                all.addAll(arr3);
                                all.addAll(arr4);
                                if (compareEdges(all, outGoingWeights)) {
                                    oneStart = getNode(i).getIncomingEdges().get(0).getFromNode();
                                    twoStart = getNode(i).getIncomingEdges().get(1).getFromNode();
                                    threeStart = getNode(i).getIncomingEdges().get(2).getFromNode();
                                    fourStart = getNode(i).getIncomingEdges().get(3).getFromNode();
                                    //Get all the edges with weights in one and two
                                    for (Edge e : getNode(i).getOutgoingEdges()) {
                                        if (arr1.contains(new Integer(e.getWeight()))) {
                                            oneEdges.add(e);
                                            arr1.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                        if (arr2.contains(new Integer(e.getWeight()))) {
                                            twoEdges.add(e);
                                            arr2.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                        if (arr3.contains(new Integer(e.getWeight()))) {
                                            threeEdges.add(e);
                                            arr3.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                        if (arr4.contains(new Integer(e.getWeight()))) {
                                            fourEdges.add(e);
                                            arr4.remove(new Integer(e.getWeight()));
                                            continue;
                                        }
                                    }
                                    checker = true;
                                    break;
                                }
                                all.clear();
                            }
                            if(checker) break;
                        }
                        if(checker) break;
                    }
                    if(checker) break;
                }
                if(checker == false) continue;
                ArrayList<Edge> toRemove = new ArrayList<>();
                toRemove.addAll(getNode(i).getIncomingEdges());
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: oneEdges){
                    addEdge(oneStart, e.getToNode(), e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: twoEdges){
                    addEdge(twoStart, e.getToNode() ,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: threeEdges){
                    addEdge(threeStart, e.getToNode() ,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
                for(Edge e: fourEdges){
                    addEdge(fourStart, e.getToNode() ,e.getWeight());
                    toRemove.add(e);
                }
                for(Edge e: toRemove) removeEdge(e);
                toRemove.clear();
            }
        }
    }

    static boolean[][] dp;


    static void display(ArrayList<Integer> v)
    {
        ArrayList<Integer> copy = (ArrayList<Integer>) v.clone();
        allSubsets.add(copy);
    }

    // A recursive function to print all subsets with the
    // help of dp[][]. Vector p[] stores current subset.
    static void printSubsetsRec(int arr[], int i, int sum,
                                ArrayList<Integer> p)
    {
        ArrayList<ArrayList<Integer>> allSubsets = new ArrayList<>();
        // If we reached end and sum is non-zero. We print
        // p[] only if arr[0] is equal to sun OR dp[0][sum]
        // is true.
        if (i == 0 && sum != 0 && dp[0][sum])
        {
            p.add(arr[i]);
            display(p);
            p.clear();
            return;
        }

        // If sum becomes 0
        if (i == 0 && sum == 0)
        {
            display(p);
            p.clear();
            return;
        }

        // If given sum can be achieved after ignoring
        // current element.
        if (dp[i-1][sum])
        {
            // Create a new vector to store path
            ArrayList<Integer> b = new ArrayList<>();
            b.addAll(p);
            printSubsetsRec(arr, i-1, sum, b);
        }

        // If given sum can be achieved after considering
        // current element.
        if (sum >= arr[i] && dp[i-1][sum-arr[i]])
        {
            p.add(arr[i]);
            printSubsetsRec(arr, i-1, sum-arr[i], p);
        }
    }

    // Prints all subsets of arr[0..n-1] with sum 0.
    static void printAllSubsets(int arr[], int n, int sum)
    {
        if (n == 0 || sum < 0)
            return;

        // Sum 0 can always be achieved with 0 elements
        dp = new boolean[n][sum + 1];
        for (int i=0; i<n; ++i)
        {
            dp[i][0] = true;
        }

        // Sum arr[0] can be achieved with single element
        if (arr[0] <= sum)
            dp[0][arr[0]] = true;

        // Fill rest of the entries in dp[][]
        for (int i = 1; i < n; ++i)
            for (int j = 0; j < sum + 1; ++j)
                dp[i][j] = (arr[i] <= j) ? (dp[i-1][j] ||
                        dp[i-1][j-arr[i]])
                        : dp[i - 1][j];
        if (dp[n-1][sum] == false) return;


        // Now recursively traverse dp[][] to find all
        // paths from dp[n-1][sum]
        ArrayList<Integer> p = new ArrayList<>();
        printSubsetsRec(arr, n-1, sum, p);
    }

}

