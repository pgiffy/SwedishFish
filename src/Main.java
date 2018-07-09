import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {

        //config variables:
        //String directory = "/home/dan/dev/instances/rnaseq";            //only used in multiple read mode
        //String file = "/home/dan/dev/instances/rnaseq/test/1.graph";   //only used in single read mode
        //String truthFile = "/home/dan/dev/instances/rnaseq/test/1.truth"; //only used in single read mode
        String directory = "/home/peter/Desktop/instances/rnaseq";
        String file = "/home/peter/Desktop/instances/rnaseq/test/1.graph";         //either single or multiple
        String truthFile = "/home/peter/Desktop/instances/rnaseq/test/1.truth";
        String importMode = "multiple";                                   //either single or multiple

        ArrayList<Network> networks;
        if (importMode.equals("multiple")) {
            PrintWriter out = null;
            int[] resultBins = new int[100];
            int[] totals = new int[100];
            int numSuccess = 0;
            int numTotal = 0;

            try {
                out = new PrintWriter(new File("outputFile.txt"));

                File dir = new File(directory + "/test");
                File[] files = dir.listFiles();
                for (int i = 0; i < 100; i++) resultBins[i] = 0;
                for (int i = 0; i < 100; i++) totals[i] = 0;

                for (File curFile : files) {

                    int pos = curFile.getName().lastIndexOf(".");
                    String ext = curFile.getName().substring(pos + 1);
                    String filenameNoExt = curFile.getName().substring(0, pos);
                    String filename = curFile.getName();
                    if (ext.equals("graph")) {
                        networks = readGraphFile(directory + "/test/" + filename);
                        ArrayList<Integer> numTruthPaths = readTruthFile(directory + "/test/" + filenameNoExt + ".truth");

                        for (int num : numTruthPaths) {
                            totals[num - 1]++;
                            numTotal++;
                        }

                        System.out.print("*");
                        int count = 0;
                        for (Network network : networks) {
                            network.collapseEdges();
                            ArrayList<Integer> sortedNodes = network.topoSort();
                            ArrayList<Path> paths = new ArrayList<>();
                            int numPaths = shortTerm(network, sortedNodes);

                            int truthPaths = numTruthPaths.get(count);

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
     * Reads in all graph files for selected animal subfolders
     */
    public static ArrayList<Network> readGraphFiles(String directory, String[] animals) {
        ArrayList<Network> networks = new ArrayList<>();

        for (String animal : animals) {
            File dir = new File(directory + "/" + animal);
            File[] files = dir.listFiles();

            for (File file : files) {
                int pos = file.getName().lastIndexOf(".");
                String ext = file.getName().substring(pos + 1);
                //System.out.println(ext);
                if (!ext.equals("graph")) continue;
                String filePath = file.getPath();
                networks.addAll(readGraphFile(filePath));
            }
        }

        return networks;
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


    /**
     * Finds the path of maximum length with a flow of k
     *
     * @param k
     * @return length of the path (# of nodes, not weight)
     */


    public static Path findMaxPath(Network network, int k, ArrayList<Integer> sortedNodes, PrintWriter out) {

        int[] lengths = new int[network.numNodes()];
        Edge[] selectedEdges = new Edge[network.numNodes()];
        for (int i = 0; i < lengths.length; i++) lengths[i] = -1;
        for (int i = 0; i < selectedEdges.length; i++) selectedEdges[i] = null;
        lengths[0] = 0;

        //System.out.println(sortedNodes);
        for (int nodeId : sortedNodes) {
            Node node = network.getNode(nodeId);

            for (Edge e : node.getOutgoingEdges()) {
                int weight = e.getWeight();
                int newLength = 1 + lengths[nodeId];
                int toNodeId = e.getToNode().getId();

                if (weight >= k && newLength >= lengths[toNodeId]) {
                    //take smaller weight as tie-breaker
                    if (newLength == lengths[toNodeId] && weight >= selectedEdges[toNodeId].getWeight()) continue;
                    lengths[toNodeId] = newLength;
                    selectedEdges[toNodeId] = e;
                }

            }
        }

        int count = 0;
        int i = selectedEdges.length - 1;
        //System.out.println(Arrays.toString(selectedEdges));
        Stack<Edge> edgesReverse = new Stack<Edge>();
        while (i > 0) {
            Edge e = selectedEdges[i];
            if (e == null) return null;
            Node fromNode = e.getFromNode();
            i = fromNode.getId();
            edgesReverse.push(e);
            count++;
            if (count > selectedEdges.length) return null;
        }

        ArrayList<Edge> selectedEdges2 = new ArrayList<>();
        while (!edgesReverse.empty()) {
            Edge e = edgesReverse.pop();
            selectedEdges2.add(e);
        }

        //out.printf("MAX PATH (Flow %d) = " + Arrays.toString(lengths)+"\n" + Arrays.toString(selectedEdges)+"\n", k);

        Path path = new Path(selectedEdges2);

        return path;
    }

    public static ArrayList<Path> getAllPaths(Network network) {
        Node src = network.getNode(0);
        Node dest = network.getNode(network.numNodes() - 1);
        ArrayList<Path> paths = new ArrayList<>();
        ArrayList<Edge> path = new ArrayList<>();
        //System.out.println(network.toString());

        return getAllPathsUtil(network, src, dest, paths, path);
    }

    public static ArrayList<Path> getAllPathsUtil(Network network, Node src, Node dest, ArrayList<Path> paths, ArrayList<Edge> path) {
        src.setVisited(true);

        if (src.getId() == dest.getId()) {
            paths.add(new Path(path));
            //System.out.print(".");
        }

        for (Edge e : src.getOutgoingEdges()) {
            Node n = e.getToNode();
            if (!n.isVisited()) {
                path.add(e);
                getAllPathsUtil(network, n, dest, paths, path);
                path.remove(e);
            }
        }

        src.setVisited(false);

        return paths;
    }


    static ArrayList<Integer> toAdd = new ArrayList<>();
    public static int shortTerm(Network network, ArrayList<Integer> sortedNodes) {

        int numPaths = 0;
        ArrayList<Integer> possibleVals = network.getAllEdgeWeight();
        network.getNode(0).addAllPossible(possibleVals);

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
                printAllSubsets(vals, length, weight);
                e.getToNode().addAllPossible(toAdd);
                toAdd.clear();
                //System.out.println(toAdd);
            }

        }

        ArrayList<Integer> solution = network.getNode(network.numNodes() - 1).getPossible();

        for(int node: sortedNodes){

            ArrayList<Integer> toRemove = new ArrayList<>();
            for(int val: network.getNode(node).getPossible()){
                if(!solution.contains(val)){
                    toRemove.add(val);
                }
            }
            for(int i: toRemove) {
                network.getNode(node).removePossible(i);
            }
        }

        //System.out.println(network.getNode(0).getPossible());
        numPaths = network.getNode(network.numNodes()-1).getPossible().size();
        return numPaths;
    }



    static boolean[][] dp;


    static void addVals(ArrayList<Integer> v){
        toAdd.addAll(v);
    }

    static void display(ArrayList<Integer> v)
    {
        System.out.println(v);
    }


    static void printSubsetsRec(int arr[], int i, int sum, ArrayList<Integer> p)
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
            System.out.println("There are no subsets with sum " + sum);
            return;
        }
        // Now recursively traverse dp[][] to find all
        // paths from dp[n-1][sum]
        ArrayList<Integer> p = new ArrayList<>();
        printSubsetsRec(arr, n-1, sum, p);
    }
}

