
import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {

        String directory = "/home/peter/Desktop/instances/rnaseq";

        ArrayList<Network> networks;
            PrintWriter out = null;
            int[] resultBins = new int[101];
            int[] totals = new int[101];


            try {
                out = new PrintWriter(new File("outputFile.txt"));

                File dir = new File(directory + "/human");
                File[] files = dir.listFiles();
                for (int i = 0; i < 100; i++) resultBins[i] = 0;
                for (int i = 0; i < 100; i++) totals[i] = 0;

                for (File curFile : files) {

                    int pos = curFile.getName().lastIndexOf(".");
                    String ext = curFile.getName().substring(pos + 1);
                    String filenameNoExt = curFile.getName().substring(0, pos);
                    String filename = curFile.getName();
                    if (ext.equals("graph")) {
                        networks = readGraphFile(directory + "/human/" + filename);
                        ArrayList<Integer> numTruthPaths = readTruthFile(directory + "/human/" + filenameNoExt + ".truth");

                        for (int num : numTruthPaths) totals[num - 1]++;


                        System.out.print("*");
                        int count = 0;
                        for (Network network : networks) {
                            network.collapseEdges();
                            network.breakItDown();
                            network.collapseEdges2();
                            ArrayList<Integer> sortedNodes = network.topoSort();
                            int numPaths = 0;
                            ArrayList<Integer> valK = stackFlow(network);
                            Collections.sort(valK);
                            Collections.reverse(valK);
                            for (int k : valK) {
                                Path newPath = findMaxPath(network, k, sortedNodes, out);
                                if (newPath == null) {
                                    break;
                                }
                                network.reducePath(newPath);
                                numPaths++;
                            }

                            while(network.numEdges() > 0) {

                                Path selectedPath = findFattestPath(network);
                                network.reducePath(selectedPath);
                                //paths.add(selectedPath);
                                numPaths++;
                            }

                            int truthPaths = numTruthPaths.get(count);

                            if(numPaths == 0){
                                numPaths = 100;
                            }

                            out.println("# Truth Paths = " + truthPaths + "\t # Actual Paths = " + numPaths);
                            if (numPaths <= truthPaths) {
                                resultBins[truthPaths - 1]++;
                            }

                            count++;
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file.");
                e.printStackTrace();
            } finally {
                out.close();
            }

            System.out.printf("\n# Paths\tSuccess Rate\n");
            for (int i = 0; i < 10; i++) {
                double successRate = ((double) resultBins[i] / totals[i]) * 100;
                System.out.printf("%d\t\t%.2f\n", i + 1, successRate);
            }


    }



    private static Path findMaxPath(Network network, int k, ArrayList<Integer> sortedNodes, PrintWriter out) {
        int[] lengths = new int[network.numNodes()];
        Edge[] selectedEdges = new Edge[network.numNodes()];
        for(int i = 0; i < lengths.length; i++) lengths[i] = -1;
        for(int i = 0; i < selectedEdges.length; i++) selectedEdges[i] = null;
        lengths[0] = 0;

        //System.out.println(sortedNodes);
        for(int nodeId: sortedNodes) {
            Node node = network.getNode(nodeId);

            for(Edge e: node.getOutgoingEdges()) {
                int weight = e.getWeight();
                int newLength = 1 + lengths[nodeId];
                int toNodeId = e.getToNode().getId();
                if(weight >= k && newLength >= lengths[toNodeId]) {
                    //take smaller weight as tie-breaker
                    if(newLength == lengths[toNodeId] && weight > selectedEdges[toNodeId].getWeight()) continue;
                    lengths[toNodeId] = newLength;
                    selectedEdges[toNodeId] = e;
                }
            }
        }

        int count = 0;
        int i = selectedEdges.length-1;
        //System.out.println(Arrays.toString(selectedEdges));
        Stack<Edge> edgesReverse = new Stack<>();
        while(i > 0) {
            Edge e = selectedEdges[i];
            if(e == null) return null;
            Node fromNode = e.getFromNode();
            i = fromNode.getId();
            edgesReverse.push(e);
            count++;
            if(count > selectedEdges.length) return null;
        }

        ArrayList<Edge> selectedEdges2 = new ArrayList<>();
        while(!edgesReverse.empty()) {
            Edge e = edgesReverse.pop();
            selectedEdges2.add(e);
        }

        //out.printf("MAX PATH (Flow %d) = " + Arrays.toString(lengths)+"\n" + Arrays.toString(selectedEdges)+"\n", k);

        return new Path(selectedEdges2, k);


    }


    private static ArrayList<Integer> stackFlow(Network network){
        ArrayList[] stackHolder = new ArrayList[network.numNodes()];
        for(int i = 0; i < stackHolder.length; i++){
            stackHolder[i] = new ArrayList<Integer>();
        }
        for(Edge e: network.getEdges()){
            int start = e.getFromNode().getId();
            int end = e.getToNode().getId();
            for(int i = start; i < end; i++) stackHolder[i].add(e.getWeight());

        }
        int largestSize = 0;
        for(int i = 0; i < stackHolder.length; i++){
            if(stackHolder[i].size()>largestSize){
                largestSize = stackHolder[i].size();
            }
        }
        //holds all the values held by biggest stacks
        ArrayList<ArrayList<Integer>> allBiggest = new ArrayList<>();
        for(int i = 0; i < stackHolder.length; i++){
            if(stackHolder[i].size() == largestSize){
                allBiggest.add(stackHolder[i]);
            }
        }
        // allBiggest now holds all of the greatest size paths.
        if(allBiggest.size() == 1) return allBiggest.get(0);

        //this makes it a little better by picking the list with the lowest highest number
        int smallestLargest = 100000000;
        ArrayList<Integer> bestList = new ArrayList<>();
        for(ArrayList<Integer> list: allBiggest){
            Collections.sort(list);
            Collections.reverse(list);
            if(list.get(0) < smallestLargest){
                smallestLargest = list.get(0);
                bestList = list;
            }
        }
        return bestList;

    }

    //CODE NOT BEING CHANGED CURRENTLY/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CODE NOT BEING CHANGED CURRENTLY/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CODE NOT BEING CHANGED CURRENTLY/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CODE NOT BEING CHANGED CURRENTLY/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CODE NOT BEING CHANGED CURRENTLY/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CODE NOT BEING CHANGED CURRENTLY/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //GREEDY
    private static Path findFattestPath(Network network) {
        //System.out.println(network.toString());
        ArrayList<Integer> sortedNodes = network.topoSort();
        int flow[] = new int[network.numNodes()];
        Edge edges[] = new Edge[network.numNodes()];
        for(int i = 0; i < flow.length; i++) {
            flow[i] = -1;
            edges[i] = null;
        }

        for(int u: sortedNodes) {
            for(Edge e: network.getNode(u).getOutgoingEdges()) {
                int v = e.getToNode().getId();
                int weight = e.getWeight();

                if(weight < flow[u] || flow[u] < 0) {
                    if(weight >= flow[v]) {
                        flow[v] = weight;
                        edges[v] = e;
                    }
                } else {
                    if(flow[u] >= flow[v]) {
                        flow[v] = flow[u];
                        edges[v] = e;
                    }
                }
            }
        }

        ArrayList<Edge> pathEdges = new ArrayList<>();
        Edge e = edges[edges.length-1];
        while(e != null) {
            pathEdges.add(e);
            e = edges[e.getFromNode().getId()];
        }

        //System.out.println(pathEdges.toString());

        return new Path(pathEdges);
    }
    /**
     * Parses the lines of a .graph file into a Network object
     *
     * @param graphs - each row contains a String representation of a graph
     * @return
     */
    public static ArrayList<Network> parseGraph(ArrayList<String> graphs) {
        ArrayList<Network> networks = new ArrayList<>();
        for (String graph : graphs) {
            Network network = new Network();
            String[] lines = graph.split("\n");
            int numNodes = Integer.parseInt(lines[0]);
            //System.out.println("***NEW GRAPH***");

            for (int i = 0; i < numNodes; i++) {
                network.addNode();
            }
            //System.out.println(numNodes + " nodes added!");

            for (int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(" ");
                Node fromNode = network.getNode(Integer.parseInt(data[0]));
                Node toNode = network.getNode(Integer.parseInt(data[1]));
                int weight = (int) Double.parseDouble(data[2]);
                network.addEdge(fromNode, toNode, weight);
                //System.out.println("Edge: "+fromNode+" -> "+toNode+": "+weight);
            }
            networks.add(network);
        }

        return networks;
    }

    public static ArrayList<Integer> readTruthFile(String file) {
        File inputFile = new File(file);
        ArrayList<Integer> numTruthPaths = new ArrayList<>();
        Scanner scan;

        try {
            scan = new Scanner(inputFile);
            scan.useDelimiter("#[\\s\\S]+?[\\n]"); //splits into graphs by # XXX
            while (scan.hasNext()) {
                String[] lines = scan.next().split("\n");
                numTruthPaths.add(lines.length);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + inputFile.toString());
            e.printStackTrace();
        }

        return numTruthPaths;
    }

    /**
     * Parses a .graph file into individual networks
     *
     * @param file - path to the file
     * @return - list of networks after running on parseGraph()
     */
    public static ArrayList<Network> readGraphFile(String file) {
        File inputFile = new File(file);
        ArrayList<Network> networks;
        ArrayList<String> graphs = new ArrayList<>();
        Scanner scan;

        try {
            scan = new Scanner(inputFile);
            scan.useDelimiter("#[\\s\\S]+?[\\n]"); //splits into graphs by # XXX
            while (scan.hasNext()) {
                graphs.add(scan.next());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + inputFile.toString());
            e.printStackTrace();
        }

        //System.out.println(graphs.toString());
        //System.out.print(".");
        networks = parseGraph(graphs);

        return networks;
    }


    static ArrayList<Integer> toAdd = new ArrayList<>();



    private static ArrayList<Integer> compareBest(ArrayList<Integer> toCheck, ArrayList<Integer> knownTruth){
        //adds smallest because it wont be split by other numbers
        for(int i: toCheck){
            int[] known = knownTruth.stream().mapToInt(k->k).toArray();
            if(isSubsetSum(known, known.length, i)){
                continue;
            }else{
                knownTruth.add(i);
            }
        }

        return knownTruth;
    }



    // Returns true if there is a subset of
    // set[] with sun equal to given sum
    static boolean isSubsetSum(int set[],
                               int n, int sum)
    {
        // The value of subset[i][j] will be
        // true if there is a subset of
        // set[0..j-1] with sum equal to i
        boolean subset[][] =
                new boolean[sum+1][n+1];

        // If sum is 0, then answer is true
        for (int i = 0; i <= n; i++)
            subset[0][i] = true;

        // If sum is not 0 and set is empty,
        // then answer is false
        for (int i = 1; i <= sum; i++)
            subset[i][0] = false;

        // Fill the subset table in botton
        // up manner
        for (int i = 1; i <= sum; i++)
        {
            for (int j = 1; j <= n; j++)
            {
                subset[i][j] = subset[i][j-1];
                if (i >= set[j-1])
                    subset[i][j] = subset[i][j] ||
                            subset[i - set[j-1]][j-1];
            }
        }
        return subset[sum][n];
    }
/*static void printSubsetsRec(int arr[], int i, int sum, ArrayList<Integer> p)
    {
        // If we reached end and sum is non-zero. We print
        // p[] only if arr[0] is equal to sun OR dp[0][sum]
        // is true.
        if (i == 0 && sum != 0 && dp[0][sum])
        {
            p.add(arr[i]);
            addVals(p);
            p.clear();
            return;
        }

        // If sum becomes 0
        if (i == 0 && sum == 0)
        {
            addVals(p);
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
        if (dp[n-1][sum] == false)
        {
            //System.out.println("There are no subsets with sum " + sum);
            return;
        }
        // Now recursively traverse dp[][] to find all
        // paths from dp[n-1][sum]
        ArrayList<Integer> p = new ArrayList<>();
        printSubsetsRec(arr, n-1, sum, p);
    }*/


    public static Path random(Network network){
        Random rand = new Random();
        ArrayList<Edge> path = new ArrayList<>();
        int cap = network.numNodes();
        int current = 0;
        int currentPossible;
        int choice;
        while(current != cap && network.getNode(current).getOutgoingEdges().size() > 0){
            currentPossible = network.getNode(current).getOutgoingEdges().size();
            choice = rand.nextInt(currentPossible);
            path.add(network.getNode(current).getOutgoingEdges().get(choice));
            current = network.getNode(current).getOutgoingEdges().get(choice).getToNode().getId();
        }
        Path randomPath = new Path(path);
        return randomPath;
    }

    public static ArrayList<Integer> shortTerm(Network network, ArrayList<Integer> sortedNodes) {

        int numPaths = 0;
        ArrayList<Integer> possibleVals = network.getAllEdgeWeight();
        network.getNode(0).addAllPossible(possibleVals);
        //System.out.println(network.toString());
        for(int node = 0; node < sortedNodes.size(); node++){
            ArrayList<Integer> valsToUse = network.getNode(node).getPossible();
            for(Edge e: network.getNode(node).getOutgoingEdges()){
                int weight = e.getWeight();
                int[] vals = new int[valsToUse.size()];
                for(int i= 0; i < valsToUse.size(); i++){
                    vals[i] = valsToUse.get(i);
                }
                int length = vals.length;

                //assigns the possible paths to nodes
                findTargetSumSubsets(vals, weight, "", 0);
                for (String str: allSubsets) {
                    String[] strArray = str.split(" ");
                    int[] intArray = new int[strArray.length];
                    for(int i = 0; i < strArray.length; i++) {
                        intArray[i] = Integer.parseInt(strArray[i]);
                    }
                    for(int i: intArray) {

                        if (!e.getToNode().getPossible().contains(i)) {
                            e.getToNode().addPossible(i);
                            e.addPath(i);
                        }
                    }
                    //System.out.println(e.getPaths());
                }
                allSubsets.clear();
            }

        }

        //ArrayList<Integer> solution = network.getNode(network.numNodes() - 1).getPossible();

        /*for(int node: sortedNodes){

            ArrayList<Integer> toRemove = new ArrayList<>();
            for(int val: network.getNode(node).getPossible()){
                if(!solution.contains(val)){
                    toRemove.add(val);
                }
            }
            for(int i: toRemove) {
                network.getNode(node).removePossible(i);
            }
           // System.out.println(node);
            //System.out.println(network.getNode(node).getPossible());
        }*/

        /*for(Edge e: network.getEdges()){

            ArrayList<Integer> toRemove = new ArrayList<>();
            for(int val: network.getEdge(e).getPaths()){
                if(!solution.contains(val)){
                    toRemove.add(val);
                }
            }
            for(int i: toRemove) {
                e.removePossible(i);
            }
            // System.out.println(node);
            //System.out.println(network.getNode(node).getPossible());
        }*/

        //System.out.println(network.getNode(0).getPossible());
        numPaths = network.getNode(network.numNodes()-1).getPossible().size();
        return network.getNode(network.numNodes()-1).getPossible();
    }
    private static HashSet<String> allSubsets = new HashSet<>();

    /**
     * The String token
     */
    private static final String token = " ";
    public static void findTargetSumSubsets(int[] input, int target, String ramp, int index) {

        if(index > (input.length - 1)) {
            if(getSum(ramp) == target) {
                allSubsets.add(ramp);
            }
            return;
        }

        //First recursive call going ahead selecting the int at the currenct index value
        findTargetSumSubsets(input, target, ramp + input[index] + token, index + 1);
        //Second recursive call going ahead WITHOUT selecting the int at the currenct index value
        findTargetSumSubsets(input, target, ramp, index + 1);
    }

    /**
     * A helper Method for calculating the sum from a string of integers
     *
     * @param intString the string subset
     * @return the sum of the string subset
     */
    private static int getSum(String intString) {
        int sum = 0;
        StringTokenizer sTokens = new StringTokenizer(intString, token);
        while (sTokens.hasMoreElements()) {
            sum += Integer.parseInt((String) sTokens.nextElement());
        }
        return sum;
    }


    static boolean[][] dp;


    static void addVals(ArrayList<Integer> v){
        toAdd.addAll(v);
    }

    static void display(ArrayList<Integer> v)
    {
        System.out.println(v);
    }

    private static ArrayList<Integer> removeDuplicates(ArrayList<Integer> remove){

        Set<Integer> noDuplicate = new HashSet<>();
        noDuplicate.addAll(remove);
        remove.clear();
        remove.addAll(noDuplicate);
        return remove;
    }
}
