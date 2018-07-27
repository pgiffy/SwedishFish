
import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {
            String directory = "/home/peter/Desktop/instances/rnaseq/";
            ArrayList<Network> networks;
            PrintWriter out = null;
            int[] resultBins = new int[101];
            int[] totals = new int[101];
            String animal = "human";
            try{
                out = new PrintWriter(new File("outputFile.txt"));
                File dir = new File(directory + "/" + animal);
                File[] files = dir.listFiles();
                for (int i = 0; i < 101; i++) resultBins[i] = 0;
                for (int i = 0; i < 101; i++) totals[i] = 0;
                for (File curFile : files) {
                    int pos = curFile.getName().lastIndexOf(".");
                    String ext = curFile.getName().substring(pos + 1);
                    String filenameNoExt = curFile.getName().substring(0, pos);
                    String filename = curFile.getName();
                    if (ext.equals("graph")) {
                        networks = readGraphFile(directory + "/" + animal + "/" + filename);
                        ArrayList<Integer> numTruthPaths = readTruthFile(directory + "/" + animal + "/" + filenameNoExt + ".truth");
                        for (int num : numTruthPaths) totals[num - 1]++;
                        System.out.print("?");
                        int count = 0;
                        for (Network network : networks) {
                            int numPaths = 0;
                            try {// sometimes in salmon there are graphs that overload the stack so this try catch is implemented to deal with that
                                //currently is just skips the graph so report on that when writing
                                network.collapseEdges2();
                                for (int i = 0; i < 7; i++) {
                                    //collapse down the network as much as possible before removing any edges
                                    network.breakItDown();
                                    network.uglyBanana();
                                }
                                reversal(network);
                                for (int i = 0; i < 2; i++) {
                                    //collapse down the network as much as possible before removing any edges
                                    network.breakItDown();
                                    network.uglyBanana();
                                }
                                for (int i = 0; i < 3; i++) rotation(network);
                                reversal(network);
                                rotation(network);
                                ArrayList<Integer> sortedNodes;
                                ArrayList<Integer> valK = stackFlow(network);
                                Collections.sort(valK);
                                Collections.reverse(valK);
                                int k = valK.get(0);
                                while (network.numEdges() > 0) {
                                    sortedNodes = network.topoSort();
                                    Path newPath = findMaxPath(network, k, sortedNodes, out);
                                    if (newPath == null) {
                                        Path selectedPath = findFattestPath(network);
                                        network.reducePath(selectedPath);
                                        numPaths++;
                                        network.collapseEdges2();
                                        rotation(network);
                                        reversal(network);
                                        rotation(network);
                                        valK = stackFlow(network);
                                        Collections.sort(valK);
                                        Collections.reverse(valK);
                                        k = valK.get(0);
                                    } else {
                                        network.reducePath(newPath);
                                        numPaths++;
                                        network.collapseEdges2();
                                        rotation(network);
                                        reversal(network);
                                        rotation(network);
                                        valK = stackFlow(network);
                                        Collections.sort(valK);
                                        Collections.reverse(valK);
                                        if (valK.isEmpty()) break;
                                        k = valK.get(0);
                                    }
                                }
                            }catch(OutOfMemoryError e){
                                //if subsets overload it just finish whats left of the graph with greedy
                                while (network.numEdges() > 0) {
                                    Path selectedPath = findFattestPath(network);
                                    network.reducePath(selectedPath);
                                    numPaths++;
                                    network.collapseEdges2();
                                    network.breakItDown();
                                    network.uglyBanana();
                                }
                            }
                            int truthPaths = numTruthPaths.get(count);
                            //fail safe for accidentally setting numPaths to 0
                            if (numPaths == 0) numPaths = 100;
                            out.println("# Truth Paths = " + truthPaths + "\t # Actual Paths = " + numPaths);
                            if (numPaths <= truthPaths) resultBins[truthPaths - 1]++;
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
            //loop changes the scope of the answers shown
            for (int i = 0; i < 10; i++) {
                double successRate = ((double) resultBins[i] / totals[i]) * 100;
                System.out.printf("%d\t\t%.2f\n", i + 1, successRate);
            }
    }

    //finds longest path by edge number
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
        //finds the greatest stack of edges crossing the same point in the set of topologically sorted nodes and their edges
        ArrayList[] stackHolder = new ArrayList[network.numNodes()];
        for(int i = 0; i < stackHolder.length; i++) stackHolder[i] = new ArrayList<Integer>();
        for(Edge e: network.getEdges()){
            int start = e.getFromNode().getId();
            int end = e.getToNode().getId();
            for(int i = start; i < end; i++) stackHolder[i].add(e.getWeight());
        }
        int largestSize = 0;
        for(int i = 0; i < stackHolder.length; i++) if(stackHolder[i].size()>largestSize) largestSize = stackHolder[i].size();
        //holds all the values held by biggest stacks
        ArrayList<ArrayList<Integer>> allBiggest = new ArrayList<>();
        for(int i = 0; i < stackHolder.length; i++) if(stackHolder[i].size() == largestSize) allBiggest.add(stackHolder[i]);
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

    //Greedy Width
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
        return new Path(pathEdges);
    }


    public static ArrayList<Network> parseGraph(ArrayList<String> graphs) {
        ArrayList<Network> networks = new ArrayList<>();
        for (String graph : graphs) {
            Network network = new Network();
            String[] lines = graph.split("\n");
            int numNodes = Integer.parseInt(lines[0]);
            for (int i = 0; i < numNodes; i++) network.addNode();
            for (int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(" ");
                Node fromNode = network.getNode(Integer.parseInt(data[0]));
                Node toNode = network.getNode(Integer.parseInt(data[1]));
                int weight = (int) Double.parseDouble(data[2]);
                network.addEdge(fromNode, toNode, weight);
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
        networks = parseGraph(graphs);
        return networks;
    }

    //picks edges randomly
    //I just felt like adding it cause why not
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

    //takes in network and calls reduction methods on it
    private static void rotation(Network network){
            network.breakItDown();
            network.uglyBanana();
            network.subsetGod3();
            network.uglyBanana();
            network.subsetGod2();
            network.uglyBanana();
            network.breakItDown();
            network.uglyBanana();
    }

    //makes all possible reversals
    private static void reversal(Network network){
        for(int i = 0; i < network.numNodes(); i++){
            Node m = network.identifySubgraph(network.getNode(i));
            if(m == null) continue;
            network.reverseGraph(network.getNode(i), m);
            continue;
        }
    }
}