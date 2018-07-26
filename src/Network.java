import java.util.*;
public class Network {
    private ArrayList<Edge> edges;
    private ArrayList<Node> nodes;
    static ArrayList<ArrayList<Integer>> allSubsets = new ArrayList<>();
    public Network() {
        edges = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    public void addNode() {
        nodes.add(new Node(nodes.size()));
    }

    public void addEdge(Edge e) {
        Node fromNode = e.getFromNode();
        Node toNode = e.getToNode();
        edges.add(e);
        fromNode.addEdge(e);
        toNode.addIncomingEdge(e);
    }

    public void addEdge(Node fromNode, Node toNode, int weight) {
        Edge newEdge = new Edge(fromNode, toNode, weight);
        edges.add(newEdge);
        fromNode.addEdge(newEdge);
        toNode.addIncomingEdge(newEdge);
    }

    public void reducePath(Path toReduce) {
        int pathWeight = toReduce.getWeight();
        for (Edge e : toReduce.getEdges()) {
            int weight = e.getWeight();
            if (weight - pathWeight <= 0) { removeEdge(e); } else { e.setWeight(weight - pathWeight); }
        }
    }

    public String toString() {
        String str = "Network: " + numEdges() + " edges, " + numNodes() + " nodes\n";
        for (Edge e : edges) str += e.toString() + " ";
        return str;
    }

    public int numNodes() { return nodes.size(); }

    public int numEdges() { return edges.size(); }

    public Node getNode(int id) { return nodes.get(id); }

    public ArrayList<Edge> getEdges() { return edges; }

    public void removeEdge(Edge e) {
        edges.remove(e);
        e.getFromNode().removeOutgoingEdge(e);
        e.getToNode().removeIncomingEdge(e);
    }

    public ArrayList<Integer> topoSort() {
        Stack stack = new Stack();
        boolean[] visited = new boolean[numNodes()];
        for (int i = 0; i < visited.length; i++) visited[i] = false;
        for (int i = 0; i < numNodes(); i++) if (visited[i] == false) topoSortVertex(i, visited, stack);
        ArrayList<Integer> sortedList = new ArrayList<>();
        while (!stack.empty()) sortedList.add((int) stack.pop());
        return sortedList;
    }

    private void topoSortVertex(int i, boolean[] visited, Stack stack) {
        visited[i] = true;
        for (Edge e : nodes.get(i).getOutgoingEdges()) {
            int j = e.getToNode().getId();
            if (visited[j] == false) topoSortVertex(j, visited, stack);
        }
        stack.push(i);
    }

    public void collapseEdges2() {
        //collapses all edges of the same size entering and leaving a node.
        //ugly banana is different because this only works if incoming and outgoing are exactly the same
        //ugly banana can have other random edges mixed in
        //catfish paper shows how this could reduce optimality so maybe check it over
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
        //removes nodes after collapse edges2
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
        //checks if two arraylists have the same set of integers
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

    public void breakItDown() {
        //if there is a node with either one incoming edge or one outgoing edge it will replace it with the stack og multiple incoming or outgoing edges
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
            for(int i: incoming) if(outgoing.contains(i)) {
                    toCollapse.add(i);
                    continue;
                }
            removeDuplicates(toCollapse);
            for(int i: toCollapse){
                Edge in = null;
                Edge out = null;
                for(Edge e: outgoingEdge) if(e.getWeight()==i){
                        out = e;
                        break;
                    }
                for(Edge e: incomingEdge) if(e.getWeight()==i){
                        in = e;
                        break;
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
                findAllSubsets(arrayIncoming, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                findAllSubsets(arrayIncoming, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() || two.isEmpty()) {
                    Node m = identifySubgraph(getNode(i));
                    if(m != null){
                        reverseGraph(getNode(i), m);
                        findAllSubsets(arrayIncoming, n, oneWeight);
                        one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        findAllSubsets(arrayIncoming, n, twoWeight);
                        two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        if(one.isEmpty() || two.isEmpty()) continue;
                    }else{
                        continue;
                    }
                }
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
                            all.clear();
                            checker = true;
                            break;
                        }
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
                findAllSubsets(arrayOutgoing, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                findAllSubsets(arrayOutgoing, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() || two.isEmpty()) {
                    Node m = identifySubgraphBackToFront(getNode(i));
                    if(m != null){
                        reverseGraph(getNode(i), m);
                        findAllSubsets(arrayOutgoing, n, oneWeight);
                        one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        findAllSubsets(arrayOutgoing, n, twoWeight);
                        two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        if(one.isEmpty() || two.isEmpty()) continue;
                    }else{
                        continue;
                    }
                }
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
                            all.clear();
                            checker = true;
                            break;
                        }
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
                findAllSubsets(arrayIncoming, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                findAllSubsets(arrayIncoming, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                findAllSubsets(arrayIncoming, n, threeWeight);
                three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() || two.isEmpty() || three.isEmpty()) {
                    Node m = identifySubgraph(getNode(i));
                    if(m != null){
                        reverseGraph(getNode(i), m);
                        findAllSubsets(arrayIncoming, n, oneWeight);
                        one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        findAllSubsets(arrayIncoming, n, twoWeight);
                        two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        findAllSubsets(arrayIncoming, n, threeWeight);
                        three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        if(one.isEmpty() || two.isEmpty() || three.isEmpty()) continue;
                    }else{
                        continue;
                    }
                }
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
                                all.clear();
                                checker = true;
                                break;
                            }
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
                findAllSubsets(arrayOutgoing, n, oneWeight);
                one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                findAllSubsets(arrayOutgoing, n, twoWeight);
                two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                findAllSubsets(arrayOutgoing, n, threeWeight);
                three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                allSubsets.clear();
                if(one.isEmpty() || two.isEmpty() || three.isEmpty()) {
                    Node m = identifySubgraphBackToFront(getNode(i));
                    if(m != null){
                        reverseGraph(getNode(i), m);
                        findAllSubsets(arrayOutgoing, n, oneWeight);
                        one = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        findAllSubsets(arrayOutgoing, n, twoWeight);
                        two = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        findAllSubsets(arrayOutgoing, n, threeWeight);
                        three = (ArrayList<ArrayList<Integer>>) allSubsets.clone();
                        allSubsets.clear();
                        if(one.isEmpty() || two.isEmpty() || three.isEmpty()) continue;
                    }else{
                        continue;
                    }
                }
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
                                all.clear();
                                checker = true;
                                break;
                            }
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

    //this is used in the above subsets methods

    static boolean grid[][];
    static void addSubset(ArrayList<Integer> v) {
        ArrayList<Integer> copy = (ArrayList<Integer>) v.clone();
        allSubsets.add(copy);
    }

    static void printSubsetsRec(int arr[], int i, int sum, ArrayList<Integer> p) {
        if (i == 0 && sum != 0 && grid[0][sum]) {
            p.add(arr[i]);
            addSubset(p);
            p.clear();
            return;
        }
        if (i == 0 && sum == 0) {
            addSubset(p);
            p.clear();
            return;
        }
        if (grid[i-1][sum]) {
            ArrayList<Integer> b = new ArrayList<>();
            b.addAll(p);
            printSubsetsRec(arr, i-1, sum, b);
        }
        if (sum >= arr[i] && grid[i-1][sum-arr[i]]) {
            p.add(arr[i]);
            printSubsetsRec(arr, i-1, sum-arr[i], p);
        }
    }

    static void findAllSubsets(int arr[], int n, int sum) {
        if (n == 0 || sum < 0) return;
        grid = new boolean[n][sum + 1];
        for (int i=0; i<n; ++i) grid[i][0] = true;
        if (arr[0] <= sum) grid[0][arr[0]] = true;
        for (int i = 1; i < n; ++i)
            for (int j = 0; j < sum + 1; ++j)
                grid[i][j] = (arr[i] <= j) ? grid[i-1][j] || grid[i-1][j-arr[i]] : grid[i - 1][j];
        if (grid[n-1][sum] == false) return;
        ArrayList<Integer> p = new ArrayList<>();
        printSubsetsRec(arr, n-1, sum, p);
    }

    static ArrayList<Integer> findCompliments(ArrayList<Integer> originalSet){
        ArrayList<Integer> compliments = new ArrayList<>();
        compliments.addAll(originalSet);
        Collections.reverse(compliments);
        return compliments;
    }

    public Node identifySubgraph(Node startNode){
        ArrayList<Integer> topoSorted = topoSort();
        ArrayList<Integer> toCheck = new ArrayList<>();
        ArrayList<Edge> edgeToCheck = new ArrayList<>();
        boolean checker = false;
        for(int i : topoSorted) if(i > startNode.getId()) toCheck.add(i);
        Collections.sort(toCheck);
        Collections.reverse(toCheck);
        for(int i : toCheck){
            ArrayList<Integer> addEdges = new ArrayList<>();
            for(int j : toCheck) if(j < i) addEdges.add(j);
            for(int j : addEdges){
                edgeToCheck.addAll(getNode(j).getOutgoingEdges());
                edgeToCheck.addAll(getNode(j).getIncomingEdges());
            }
            for(Edge e : edgeToCheck){
                if(e.getToNode().getId() > i || e.getFromNode().getId() < startNode.getId()){
                    checker = true;
                    break;
                }
            }
            if(checker) {
                checker = false;
                continue;
            }
            return getNode(i);
        }
        return null;
    }

    public Node identifySubgraphBackToFront(Node endNode){
        ArrayList<Integer> topoSorted = topoSort();
        ArrayList<Integer> toCheck = new ArrayList<>();
        ArrayList<Edge> edgeToCheck = new ArrayList<>();
        boolean checker = false;
        for(int i : topoSorted) if(i < endNode.getId()) toCheck.add(i);
        Collections.sort(toCheck);
        for(int i : toCheck){
            ArrayList<Integer> addEdges = new ArrayList<>();
            for(int j : toCheck) if(j > i) addEdges.add(j);
            for(int j : addEdges){
                edgeToCheck.addAll(getNode(j).getOutgoingEdges());
                edgeToCheck.addAll(getNode(j).getIncomingEdges());
            }
            for(Edge e : edgeToCheck){
                if(e.getToNode().getId() > endNode.getId() || e.getFromNode().getId() < i){
                    checker = true;
                    break;
                }
            }
            if(checker) {
                checker = false;
                continue;
            }
            return getNode(i);
        }
        return null;
    }


    public void reverseGraph(Node one, Node two){
        ArrayList<Integer> topoSorted = topoSort();
        int oneId = one.getId();
        int twoId = two.getId();
        ArrayList<Edge> toAdd = new ArrayList<>();
        ArrayList<Edge> toRemove = new ArrayList<>();
        ArrayList<Integer> toReverse = new ArrayList<>();
        for(int id : topoSorted) if(id <= twoId && id >= oneId) toReverse.add(id);
        Collections.sort(toReverse);
        ArrayList<Integer> compliments = findCompliments(toReverse);
        //now they are the reverse numbers of each other
        for(int currentNode : toReverse){
            for(Edge e : getNode(currentNode).getOutgoingEdges()){
                int start = currentNode;
                int end = e.getToNode().getId();
                int weight = e.getWeight();
                int startIndex = toReverse.indexOf(new Integer(start));
                int endIndex = toReverse.indexOf(new Integer(end));
                int newStart = compliments.get(endIndex);
                int newEnd = compliments.get(startIndex);
                toAdd.add(new Edge(getNode(newStart), getNode(newEnd), weight));
                toRemove.add(e);
            }
        }
        for(Edge e : toRemove) removeEdge(e);
        for(Edge e : toAdd) addEdge(e);
    }
}