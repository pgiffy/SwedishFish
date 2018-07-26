import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {

        //config variables:
        String[] animals = {"human", "mouse", "salmon", "zebrafish"};   //only used in multiple read mode
        //String directory = "/home/dan/dev/instances/rnaseq";            //only used in multiple read mode
        //String file = "/home/dan/dev/instances/rnaseq/test/1.graph";   //only used in single read mode
        //String truthFile = "/home/dan/dev/instances/rnaseq/test/1.truth"; //only used in single read mode
        String directory = "/home/dan/dev/instances/rnaseq";
        String file = "/home/peter/Desktop/instances/rnaseq/test/1.graph";         //either single or multiple
        String truthFile = "/home/peter/Desktop/instances/rnaseq/test/1.truth";
        String importMode = "multiple";                                   //either single or multiple


        ArrayList<Network> networks;
        if(importMode.equals("multiple")) {
            String animal = "test";
            boolean debug = true;
            PrintWriter out = null;
            int[] resultBins = new int[100];
            int[] totals = new int[100];
            int numSuccess = 0;
            int numTotal = 0;
            int skipped = 0;

            try {
                out = new PrintWriter(new File("outputFile.txt"));

                File dir = new File(directory+"/"+animal);
                File[] files = dir.listFiles();
                for(int i = 0; i < 100; i++) resultBins[i] = 0;
                for(int i = 0; i < 100; i++) totals[i] = 0;

                for (File curFile : files) {

                    int pos = curFile.getName().lastIndexOf(".");
                    String ext = curFile.getName().substring(pos+1);
                    String filenameNoExt = curFile.getName().substring(0, pos);
                    String filename = curFile.getName();
                    if(ext.equals("graph")) {
                        networks = readGraphFile(directory+"/"+animal+"/"+filename);
                        ArrayList<Integer> numTruthPaths = readTruthFile(directory+"/"+animal+"/"+filenameNoExt+".truth");

                        for(int num: numTruthPaths) {
                            totals[num-1]++;
                            numTotal++;
                        }

                        System.out.print("*");
                        int count = 0;
                        ArrayList<Path> paths = new ArrayList<>();
                        for(Network network: networks) {
                            network.assignEdgeLetters();
                            Network expandedNetwork = new Network(network);
                            if(debug) network.printDOT("beforeCollapse.dot");
                            network.collapseEdges();
                            /* */
                            ArrayList<Edge> eList = network.getEdges();
                            for(Edge e : eList) {
                                System.out.println(e.toString() + ": " + e.getRemovedEdges().toString());
                            }
                            /* */
                            paths.clear();
                            if(network.numEdges() > 60) {

                                while(network.numEdges() > 0) {
                                    Path toReduce = findFattestPath(network);
                                    paths.add(toReduce);
                                    network.reducePath(toReduce);
                                }

                            } else {
                                ArrayList<Integer> predictedTruthWeights = getPredictedTruthWeights(network);
                                //System.out.println(predictedTruthWeights.toString());
                                //ArrayList<Integer> predictedTruthWeights = new ArrayList<>();
                                if (debug) network.printDOT("graph.dot");

                                int x = 1;
                                if (debug) System.out.println("file = " + filenameNoExt + ", graph # " + count);
                                while (network.numEdges() > 0) {
                                    Path toReduce = findMaxEdgeRemovePath(network, predictedTruthWeights);
                                    paths.add(toReduce);
                                    if (debug) network.printDOT("test/" + x + ".dot", toReduce.getEdges());
                                    x++;
                                    network.reducePath(toReduce);
                                    network.collapseEdges();
                                    if (debug) network.printDOT("graph2.dot");
                                }
                            }

                            int numPaths = paths.size();

                            //print paths to graphviz files
                            ArrayList<Path> expandedPaths = new ArrayList<>();
                            for(Path p : paths) {
                                ArrayList<Edge> expandedEdgeList = new ArrayList<>();
                                for(Edge e : p.getEdges()) {
                                    expandedEdgeList.addAll(e.getRemovedEdges());
                                }
                                Path expandedPath = new Path(expandedEdgeList);
                                expandedPaths.add(expandedPath);
                            }

                            int i = 1;
                            for(Path p : expandedPaths) {
                                expandedNetwork.printDOT("test/e"+i+".dot", p.getEdges());
                                i++;
                            }

                            //print results
                            int truthPaths = numTruthPaths.get(count);
                            out.println("# Truth Paths = " + truthPaths + "\t # Actual Paths = " + numPaths);
                            //network.printDOT("graph3.dot", selectedPath);
                            if(numPaths <= truthPaths) {
                                resultBins[truthPaths-1]++;
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

            System.out.println("Skipped: " + skipped);
            System.out.printf("\n# Paths\tSuccess Rate\n");
            for(int i = 0; i < 10; i++) {
                double successRate = ((double)resultBins[i] / totals[i]) * 100;
                System.out.printf("%d\t\t%.2f\n", i+1, successRate);
            }

        }
    }

    private static Path findMaxEdgeRemovePath(Network network, ArrayList<Integer> predictedTruthWeights) {
        ArrayList<Integer> sortedNodes = network.topoSort();
        ArrayList[] data = new ArrayList[network.numNodes()];
        for(int i = 0; i < data.length; i++) {
            data[i] = new ArrayList<HashMap<String, Object>>();
        }

        ArrayList<Edge> itemEdgeList = new ArrayList<>();
        for(int nodeId : sortedNodes) {
            ArrayList<HashMap<String, Object>> thisNodeData = data[nodeId];
            for(Edge e : network.getNode(nodeId).getOutgoingEdges()) {
                int toNodeId = e.getToNode().getId();

                //add new edge flow for each outgoing edge from 0
                if(nodeId == 0) {
                    ArrayList<Edge> newEdgeList = new ArrayList<>();
                    newEdgeList.add(e);
                    HashMap<String, Object> newItem = new HashMap<>();
                    newItem.put("weight", e.getWeight());
                    newItem.put("numEdges", 1);
                    newItem.put("edgeList", newEdgeList);
                    data[toNodeId].add(newItem);
                    continue;
                }

                //copy all flows that can be carried by current edge
                for(HashMap<String, Object> dataItem : thisNodeData) {
                    int itemWeight = (int) dataItem.get("weight");
                    int itemNumEdges = (int) dataItem.get("numEdges");
                    itemEdgeList.clear();
                    if(dataItem.get("edgeList") instanceof ArrayList) {
                        itemEdgeList.addAll((List) dataItem.get("edgeList"));
                    } else {
                        itemEdgeList = new ArrayList<>();
                    }

                    HashMap<String, Object> newItem = new HashMap<>();
                    if(itemWeight <= e.getWeight()) {
                        int itemNumEdges_new = itemNumEdges;
                        if(itemWeight == e.getWeight()) itemNumEdges_new = itemNumEdges + 1;
                        itemEdgeList.add(e);
                        ArrayList<Edge> newItemEdgeList = new ArrayList<>();
                        newItemEdgeList.addAll(itemEdgeList);
                        newItem.put("weight", itemWeight);
                        newItem.put("numEdges", itemNumEdges_new);
                        newItem.put("edgeList", newItemEdgeList);
                    } else {
                        itemEdgeList.add(e);
                        ArrayList<Edge> newItemEdgeList = new ArrayList<>();
                        newItemEdgeList.addAll(itemEdgeList);
                        itemEdgeList.clear();
                        newItem.put("weight", e.getWeight());
                        newItem.put("numEdges", 1);
                        newItem.put("edgeList", newItemEdgeList);
                    }

                    data[toNodeId].add(newItem);
                }

            }

            //System.out.println(nodeId+": "+thisNodeData.toString());
        }


        //find path that will remove largest # edges
        int lastNodeId = network.numNodes()-1;
        int maxNumEdges = -1;
        int maxWeight = -1;
        Path selectedPath = null;
        for(Object item : data[lastNodeId]) {
            HashMap<String, Object> item2 = (HashMap<String, Object>) item;
            int numEdges = (int) item2.get("numEdges");
            int weight = (int) item2.get("weight");
            if(numEdges > maxNumEdges) {
                maxNumEdges = numEdges;
                maxWeight = weight;
                //selectedPath = new Path((ArrayList) item2.get("edgeList"));
            }
        }

        //Break ties by choosing the path with the least amount of uncertainty
        HashMap<String, Integer> duplicates = new HashMap<>();
        for(Object item : data[lastNodeId]) {
            HashMap<String, Object> item2 = (HashMap<String, Object>) item;
            String id = item2.get("numEdges")+"-"+item2.get("weight");
            if((int) item2.get("numEdges") < maxNumEdges) continue;
            if(duplicates.get(id) == null) {
                duplicates.put(id, 1);
            } else {
                int prevVal = duplicates.get(id);
                duplicates.put(id, prevVal+1);
            }
        }

        //System.out.println(duplicates.toString());

        int minDuplicates = -1;
        ArrayList<String> idList = new ArrayList<>();
        for(Map.Entry<String, Integer> item : duplicates.entrySet()) {
            if(item.getValue() <= minDuplicates || minDuplicates < 0) {
                minDuplicates = item.getValue();
                idList.add(item.getKey());
            }
        }

        ArrayList<Path> possiblePaths = new ArrayList<>();
        for(String id : idList) {

            //find the paths with the selected ID
            for(Object item : data[lastNodeId]) {
                HashMap<String, Object> item2 = (HashMap<String, Object>) item;
                String testId = item2.get("numEdges")+"-"+item2.get("weight");
                if(testId.equals(id)) {
                    possiblePaths.add(new Path((ArrayList) item2.get("edgeList")));
                }
            }
        }

        //System.out.println(possiblePaths.toString());
        //System.out.println(predictedTruthWeights.toString());

        //1.5 tie-breaker: take paths that match a predicted truth weight
        ArrayList<Path> possiblePaths1 = new ArrayList<>();
        for(Path p : possiblePaths) {
            if(predictedTruthWeights.contains(p.getWeight())) {
                possiblePaths1.add(p);
            }
        }

        if(possiblePaths1.isEmpty()) {
            possiblePaths1.addAll(possiblePaths);
        }

        //2nd tie-breaker: take the path with the least number of predicted truth weights
                            // (that aren't the path weight)

        int minNumTruthEdges = -1;
        ArrayList<Path> possiblePaths2 = new ArrayList<>();
        for(Path p : possiblePaths1) {
            int pathWeight = p.getWeight();
            int numTruthEdges = 0;
            for(Edge e : p.getEdges()) {
                if(e.getWeight() != pathWeight && predictedTruthWeights.contains(e.getWeight())) {
                    numTruthEdges++;
                }
            }

            if(numTruthEdges <= minNumTruthEdges || minNumTruthEdges < 0) {
                if(numTruthEdges < minNumTruthEdges) possiblePaths2.clear();
                minNumTruthEdges = numTruthEdges;
                possiblePaths2.add(p);
            }
        }

        //3rd tie-breaker: take the path that has the least number of distinct edge weights
        /*
        ArrayList<Integer> foundWeights = new ArrayList<>();
        ArrayList<Path> possiblePaths3 = new ArrayList<>();
        int minNumFoundWeights = -1;
        System.out.println("***");
        for(Path p : possiblePaths2) {
            foundWeights.clear();
            for(Edge e : p.getEdges()) {
                if(!foundWeights.contains(e.getWeight())) {
                    foundWeights.add(e.getWeight());
                }
            }

            if(foundWeights.size() <= minNumFoundWeights || minNumFoundWeights < 0) {
                if(foundWeights.size() != minNumFoundWeights) possiblePaths3.clear();
                possiblePaths3.add(p);
                minNumFoundWeights = foundWeights.size();
            }
        }
        */

        //4th tie-breaker: take the path that contains the least number of truth-weights *after subtracting
        //each individual truth weight
        int minCount = -1;
        if(possiblePaths2.size() > 1) {
            for(Path p : possiblePaths2) {
                int count = 0;
                for(Edge e : p.getEdges()) {
                    for (int truthWeight : predictedTruthWeights) {
                        if(truthWeight == p.getWeight()) continue;
                        int testWeight = e.getWeight() - truthWeight;
                        if(predictedTruthWeights.contains(testWeight) && p.getWeight() != testWeight) count++;
                    }
                }

                if(count < minCount || minCount < 0) {
                    selectedPath = p;
                    minCount = count;
                }
            }
        } else {
            selectedPath = possiblePaths2.get(0);
        }

        //System.out.println(selectedPath.getEdges().toString());


        return selectedPath;
    }

    private static Path findFattestPath(Network network) {
        //System.out.println(network.toString());
        ArrayList<Integer> sortedNodes = network.topoSort();
        int flow[] = new int[network.numNodes()];
        Edge edges[] = new Edge[network.numNodes()];
        for (int i = 0; i < flow.length; i++) {
            flow[i] = -1;
            edges[i] = null;
        }
        for (int u : sortedNodes) {
            for (Edge e : network.getNode(u).getOutgoingEdges()) {
                int v = e.getToNode().getId();
                int weight = e.getWeight();
                if (weight < flow[u] || flow[u] < 0) {
                    if (weight >= flow[v]) {
                        flow[v] = weight;
                        edges[v] = e;
                    }
                } else {
                    if (flow[u] >= flow[v]) {
                        flow[v] = flow[u];
                        edges[v] = e;
                    }
                }
            }
        }
        ArrayList<Edge> pathEdges = new ArrayList<>();
        Edge e = edges[edges.length - 1];
        while (e != null) {
            pathEdges.add(e);
            e = edges[e.getFromNode().getId()];
        }
        return new Path(pathEdges);

    }

        private static ArrayList<Integer> getPredictedTruthWeights(Network network){
        ArrayList[] stackHolder = new ArrayList[network.numNodes()];
        for(int i = 0; i < stackHolder.length; i++){
            stackHolder[i] = new ArrayList<Integer>();
        }

        ArrayList<Edge> edgeList = network.getEdges();
        for(Edge e: edgeList){
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

    private static Network reconstructNetwork(Network origNetwork, ArrayList<Path> paths) {
        return null;
    }

    private static HashMap<Integer, Integer> getWeightFrequencies(Network network) {
        HashMap<Integer, Integer> freqs = new HashMap<>();

        ArrayList<Edge> edgeList = network.getEdges();
        for(Edge e : edgeList) {
            int weight = e.getWeight();
            if(freqs.get(weight) == null) {
                freqs.put(weight, 1);
            } else {
                int newFreq = freqs.get(weight) + 1;
                freqs.put(weight, newFreq);
            }
        }

        return freqs;
    }

    /**
     * Parses the lines of a .graph file into a Network object
     * @param graphs - each row contains a String representation of a graph
     * @return
     */
    public static ArrayList<Network> parseGraph(ArrayList<String> graphs) {
        ArrayList<Network> networks = new ArrayList<>();
        for(String graph: graphs) {
            Network network = new Network();
            String[] lines = graph.split("\n");
            int numNodes = Integer.parseInt(lines[0]);
            //System.out.println("***NEW GRAPH***");

            for(int i = 0; i < numNodes; i++) {
                network.addNode();
            }
            //System.out.println(numNodes + " nodes added!");

            for(int i = 1; i < lines.length; i++) {
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
            while(scan.hasNext()) {
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

        for(String animal: animals) {
            File dir = new File(directory+"/"+animal);
            File[] files = dir.listFiles();

            for (File file : files) {
                int pos = file.getName().lastIndexOf(".");
                String ext = file.getName().substring(pos+1);
                //System.out.println(ext);
                if(!ext.equals("graph")) continue;
                String filePath = file.getPath();
                networks.addAll(readGraphFile(filePath));
            }
        }

        return networks;
    }

    /**
     * Parses a .graph file into individual networks
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
            while(scan.hasNext()) {
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
     * @param k
     * @return length of the path (# of nodes, not weight)
     */
    public static Path findMaxPath(Network network, int k, ArrayList<Integer> sortedNodes) {

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

                    if (weight >= k && newLength >= lengths[toNodeId]) {
                        //take smaller weight as tie-breaker
                        if (newLength == lengths[toNodeId] && weight >= selectedEdges[toNodeId].getWeight()) continue;
                        lengths[toNodeId] = newLength;
                        selectedEdges[toNodeId] = e;
                    }
            }
        }

        int count = 0;
        int i = selectedEdges.length-1;
        //System.out.println(Arrays.toString(selectedEdges));
        Stack<Edge> edgesReverse = new Stack<Edge>();
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

        Path path = new Path(selectedEdges2);

        return path;
    }

    public static ArrayList<Path> getAllPaths(Network network) {
        Node src = network.getNode(0);
        Node dest = network.getNode(network.numNodes()-1);
        ArrayList<Path> paths = new ArrayList<>();
        ArrayList<Edge> path = new ArrayList<>();
        //System.out.println(network.toString());

        return getAllPathsUtil(network, src, dest, paths, path);
    }

    public static ArrayList<Path> getAllPathsUtil(Network network, Node src, Node dest, ArrayList<Path> paths, ArrayList<Edge> path) {
        src.setVisited(true);

        if(src.getId() == dest.getId()) {
            paths.add(new Path(path));
            //System.out.print(".");
        }

        for(Edge e: src.getOutgoingEdges()) {
            Node n = e.getToNode();
            if(!n.isVisited()) {
                path.add(e);
                getAllPathsUtil(network, n, dest, paths, path);
                path.remove(e);
            }
        }

        src.setVisited(false);

        return paths;
    }


}
