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
            String animal = "human";
            boolean debug = false;
            PrintWriter out = null;
            int[] resultBins = new int[100];
            int[] totals = new int[100];
            int numSuccess = 0;
            int numTotal = 0;

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
                        for(Network network: networks) {
                            network.collapseEdges();
                            ArrayList<Integer> predictedTruthWeights = getPredictedTruthWeights(network);
                            //System.out.println(predictedTruthWeights.toString());
                            //ArrayList<Integer> predictedTruthWeights = new ArrayList<>();
                            if(debug) network.printDOT("graph.dot");
                            ArrayList<Path> paths = new ArrayList<>();

                            int x = 1;
                            if(debug) System.out.println("file = " + filenameNoExt + ", graph # "+count);
                            while(network.numEdges() > 0) {
                                ArrayList<Integer> wrongWeights = new ArrayList<>();
                                Path toReduce = findMaxEdgeRemovePath(network, predictedTruthWeights, wrongWeights, paths);
                                paths.add(toReduce);
                                if(debug) network.printDOT("test/"+x+".dot", toReduce.getEdges());
                                x++;
                                network.reducePath(toReduce);
                                network.collapseEdges();
                                if(debug) network.printDOT("graph2.dot");
                            }

                            int numPaths = paths.size();


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

            System.out.printf("\n# Paths\tSuccess Rate\n");
            for(int i = 0; i < 10; i++) {
                double successRate = ((double)resultBins[i] / totals[i]) * 100;
                System.out.printf("%d\t\t%.2f\n", i+1, successRate);
            }

        }
    }

    private static Path findMaxEdgeRemovePath(Network network, ArrayList<Integer> predictedTruthWeights, ArrayList<Integer> wrongWeights, ArrayList<Path> paths) {
        ArrayList<Integer> sortedNodes = network.topoSort();

        int[] numEdges = new int[network.numNodes()];
        ArrayList[] edgeList = new ArrayList[network.numNodes()];
        ArrayList[] flowList = new ArrayList[network.numNodes()];
        HashMap[] edgeFlowMap = new HashMap[network.numNodes()];

        for(int i = 0; i < network.numNodes(); i++) {
            numEdges[i] = 0;
            edgeList[i] = new ArrayList<Edge>();
            flowList[i] = new ArrayList<Integer>();
            edgeFlowMap[i] = new HashMap<Integer, Edge>();
        }

        for(int nodeId : sortedNodes) {
            Node node = network.getNode(nodeId);
            ArrayList<Edge> outgoingEdges = node.getOutgoingEdges();
            ArrayList<Integer> newFlowFound = new ArrayList<>();
            for(Edge e : outgoingEdges) {
                int weight = e.getWeight();
                int toNodeId = e.getToNode().getId();
                int prevEdgesRemoved = numEdges[nodeId];
                int newFlow;
                int newEdgesRemoved = 0;

                //special case for the first node
                if(nodeId == 0) {
                    newFlow = weight;
                    flowList[toNodeId].add(newFlow);
                    edgeList[toNodeId].add(e);
                    edgeFlowMap[toNodeId].put(newFlow, e);
                    numEdges[toNodeId] = 1;
                    continue;
                }

                //for each flow that could go through this node
                for(int i = 0; i < flowList[nodeId].size(); i++) {
                    int flow = (int) flowList[nodeId].get(i);


                    if(weight > flow && numEdges[nodeId] >= numEdges[toNodeId] && !newFlowFound.contains(toNodeId)) {

                        if(numEdges[nodeId] > numEdges[toNodeId]) {
                            flowList[toNodeId].clear();
                            edgeList[toNodeId].clear();
                            edgeFlowMap[toNodeId].clear();
                        }

                        flowList[toNodeId].add(flow);
                        edgeList[toNodeId].add(e);

                        if(!(edgeFlowMap[toNodeId].containsKey(flow) && predictedTruthWeights.contains(toNodeId))) {
                            edgeFlowMap[toNodeId].put(flow, e);
                        }

                        numEdges[toNodeId] = numEdges[nodeId];
                    }

                    else if(weight < flow && (numEdges[nodeId] > numEdges[toNodeId]) && !newFlowFound.contains(toNodeId)) {

                        flowList[toNodeId].add(weight);
                        edgeList[toNodeId].add(e);
                        edgeFlowMap[toNodeId].put(weight, e);

                        if(numEdges[toNodeId] == 0) numEdges[toNodeId] = 1;
                        else numEdges[toNodeId] = numEdges[nodeId];
                    }

                    else if(weight == flow && numEdges[nodeId]+1 >= numEdges[toNodeId] && !newFlowFound.contains(toNodeId)) {

                        numEdges[toNodeId] = numEdges[nodeId] + 1;
                        flowList[toNodeId].clear();
                        edgeList[toNodeId].clear();
                        edgeFlowMap[toNodeId].clear();
                        flowList[toNodeId].add(weight);
                        edgeList[toNodeId].add(e);
                        edgeFlowMap[toNodeId].put(weight, e);

                        newFlowFound.add(toNodeId);
                    }

                    else if(weight == flow && newFlowFound.contains(toNodeId)) {

                        //numEdges[toNodeId] = numEdges[nodeId]+1;
                        //flowList[toNodeId].clear();
                        //edgeList[toNodeId].clear();
                        //edgeFlowMap[toNodeId].clear();
                        flowList[toNodeId].add(weight);
                        edgeList[toNodeId].add(e);
                        edgeFlowMap[toNodeId].put(weight, e);
                    }
                }

            }
        }

        //System.out.println(network.toString());

        //System.out.println(Arrays.toString(numEdges));
        //System.out.println(Arrays.toString(flowList));
        //System.out.println(Arrays.toString(edgeList));
        //System.out.println(Arrays.toString(edgeFlowMap));

        int finalWeight = (int) flowList[network.numNodes()-1].get(flowList[network.numNodes()-1].size()-1);
        ArrayList<Edge> finalEdgeList = new ArrayList<>();
        int i = network.numNodes()-1;

        HashMap<Integer, Integer> freqs = getWeightFrequencies(network);
        //System.out.println(freqs.toString());
        //System.out.println(predictedTruthWeights.toString());

        while(i > 0) {
            HashMap<Integer, Edge> map = edgeFlowMap[i];
            Edge newEdge = map.get(finalWeight);

            if(newEdge == null && !map.keySet().isEmpty()) {
                Set<Integer> possibleWeights = map.keySet();

                int tieBreakWeight = 0;
                int minFreq = -1;
                for(int w : possibleWeights) {
                    if(w < finalWeight) continue; //must be able to carry flow
                    int projectedWeight = w - finalWeight;
                    if(predictedTruthWeights.contains(projectedWeight)) {
                        tieBreakWeight = w;
                        break;
                    } else if(freqs.get(w) < minFreq || minFreq < 0) {
                        minFreq = freqs.get(w);
                        tieBreakWeight = w;
                    }
                }

                /*
                if(predictedTruthWeights.contains(tieBreakWeight) && !chosenWeights.contains(tieBreakWeight) && wrongWeights.size() < 3) {
                    if(wrongWeights.isEmpty() || wrongWeights.get(0) > 0) {
                        wrongWeights.add(tieBreakWeight);
                        //System.out.println(wrongWeights.toString());
                        return findMaxEdgeRemovePath(network, predictedTruthWeights, wrongWeights, paths);
                    }
                }*/

                newEdge = map.get(tieBreakWeight);
            }

            else if(newEdge == null) break;

            i = newEdge.getFromNode().getId();

            //System.out.println("EDGE #"+i + newEdge.toString());
            finalEdgeList.add(newEdge);
        }

        //System.out.println("TO REMOVE: " + finalEdgeList.toString());
        //System.out.println(Arrays.toString(edgeFlowMap));

        return new Path(finalEdgeList);
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
