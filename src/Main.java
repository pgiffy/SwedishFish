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
            PrintWriter out = null;
            int[] resultBins = new int[100];
            int[] totals = new int[100];
            int[] methodUsage = new int[4];
            for(int i = 0; i < 4; i++) methodUsage[i] = 0;
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

                            //greedy edge removal
                            Network network1 = new Network(network);
                            Network network1a = new Network(network);
                            ArrayList<Path> paths1 = new ArrayList<>();
                            ArrayList<Path> toRemoveList = new ArrayList<>();
                            Path toRemove = null;
                            toRemoveList = greedyEdgeRemove(network1);

                            //if greedy edge fails, just use regular greedy
                            toRemoveList = null;
                            if(toRemoveList == null) {
                                toRemoveList = greedyWidth(network1a);
                            }
                            paths1.addAll(toRemoveList);

                            //removeStart -> greedy
                            Network network2 = new Network(network);
                            ArrayList<Path> paths2 = new ArrayList<>();

                            toRemoveList = removeFromStart(network2);
                            if(toRemoveList != null) {
                                paths2.addAll(toRemoveList);
                                for(Path p: toRemoveList) {
                                    network2.reducePath(p);
                                }
                            }

                            toRemoveList = greedyWidth(network2);
                            paths2.addAll(toRemoveList);
                            for(Path p: toRemoveList) {
                                network2.reducePath(p);
                            }

                            //maxFreq -> greedy
                            Network network3 = new Network(network);
                            ArrayList<Path> paths3 = new ArrayList<>();
                            toRemove = removeMaxFreq(network3);
                            if(toRemove != null) {
                                paths3.add(toRemove);
                                network3.reducePath(toRemove);
                            }

                            toRemoveList = greedyWidth(network3);
                            paths3.addAll(toRemoveList);

                            //greedy only
                            Network network4 = new Network(network);
                            ArrayList<Path> paths4 = new ArrayList<>();
                            toRemoveList = greedyWidth(network4);
                            paths4.addAll(toRemoveList);

                            //pick whichever method works better
                            int[] pathSizes = {paths1.size(), paths2.size(), paths3.size(), paths4.size()};
                            Network[] networkList = {network1, network2, network3, network4};
                            ArrayList<ArrayList<Path>> pathList = new ArrayList<>();
                            pathList.add(paths1);
                            pathList.add(paths2);
                            pathList.add(paths3);
                            pathList.add(paths4);

                            int minSize = -1;
                            int minSizeIndex = -1;
                            for(int i = 0; i < pathSizes.length; i++) {
                                if(pathSizes[i] < minSize || minSize < 0) {
                                    minSize = pathSizes[i];
                                    minSizeIndex = i;
                                    methodUsage[i]++;
                                }
                            }

                            Network selectedNetwork = networkList[minSizeIndex];
                            ArrayList<Path> selectedPath = pathList.get(minSizeIndex);

                            //print results
                            int truthPaths = numTruthPaths.get(count);
                            int numPaths = selectedPath.size();
                            //network.printDOT("graph.dot");
                            //network.printDOT("paths.dot", selectedPath);
                            //System.out.println(Arrays.toString(pathSizes));
                            for(Path p: selectedPath) {
                                //System.out.println(p.toString());
                            }
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

            System.out.println("Method Usage:");
            String[] names = {"Greedy Edge Removal", "Remove Start -> Greedy", "Max Freq -> Greedy", "Greedy Only"};
            int total = 0;
            for(int i = 0; i < 4; i++) total += methodUsage[i];
            for(int i = 0; i < 4; i++) {
                System.out.printf("%s: %.2f\n", names[i], ((double)methodUsage[i]/(double)total * 100));
            }

        }
    }

    private static Path removeMaxFreq(Network network) {
        if(network.numNodes() < 30) { //get all paths only works on smaller graphs
            //find weight that appears on the most edges
            HashMap<Integer, Integer> frequencies = new HashMap<>();
            ArrayList<Edge> edges = network.getEdges();
            for (Edge e : edges) {
                int weight = e.getWeight();
                if (frequencies.get(weight) == null) {
                    frequencies.put(weight, 1);
                } else {
                    int oldFreq = frequencies.get(weight);
                    frequencies.put(weight, oldFreq + 1);
                }
            }

            int maxFreqWeight = -1;
            int maxFreq = -1;
            for (Map.Entry entry : frequencies.entrySet()) {
                if ((int) entry.getValue() > maxFreq) {
                    maxFreq = (int) entry.getValue();
                    maxFreqWeight = (int) entry.getKey();
                }
            }

            //find the path that has the largest concentration of edges with the
            //max-frequency weight
            ArrayList<Path> allPaths = getAllPaths(network);
            int max = -1;
            Path maxPath = null;
            for (Path path : allPaths) {
                if (path.getWeightFreq().get(maxFreqWeight) != null && path.getWeightFreq().get(maxFreqWeight) > max) {
                    max = path.getWeightFreq().get(maxFreqWeight);
                    maxPath = path;
                }
            }

            network.reducePath(maxPath);
            return maxPath;
        }
        return null;
    }

    private static ArrayList<Path> removeFromStart(Network network) {
        ArrayList<Integer> valK = network.ValsToEnd();
        Collections.sort(valK);
        Collections.reverse(valK);
        ArrayList<Path> pathsToRemove = new ArrayList<>();
        ArrayList<Integer> sortedNodes = network.topoSort();

        for (int k : valK) {
            Path newPath = findMaxPath(network, k, sortedNodes);
            if (newPath == null) {
                valK = network.ValsFromZero();
                Collections.sort(valK);
                Collections.reverse(valK);
                return null;
            }
            pathsToRemove.add(newPath);
        }

        return pathsToRemove;
    }

    private static ArrayList<Path> greedyWidth(Network network) {
        ArrayList<Path> paths = new ArrayList<>();
       // System.out.println(network.toString());
        int x = 0;
        while(network.numEdges() > 0) {
            Path selectedPath = findFattestPath(network);
            if(selectedPath.getWeight() < 0) break;
            paths.add(selectedPath);

            ArrayList<Path> singlePathList = new ArrayList<>();
            singlePathList.add(selectedPath);
            //network.printDOT("greedy"+x+".dot", singlePathList);

            network.reducePath(selectedPath);
            x++;
        }

        return paths;
    }

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

       // System.out.println(Arrays.toString(flow));
       // System.out.println(Arrays.toString(edges));

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
     * Returns a list of the 3 highest-flow paths in the network
     */
    private static ArrayList<Path> greedySelect(Network network) {
        Network tempNetwork = new Network(network);
        ArrayList<Path> paths = new ArrayList<>();
        while(tempNetwork.numEdges() > 0) {
            Path selectedPath = findFattestPath(tempNetwork);
            if(selectedPath.getWeight() < 0) break;
            paths.add(selectedPath);
            tempNetwork.reducePath(selectedPath);
        }

        return paths;
    }

    private static ArrayList<Path> greedyEdgeRemove(Network network) {
        ArrayList<Path> paths = new ArrayList<>();

        while(network.numEdges() > 0) {
            int maxEdgeCount = -1;
            Path pathToRemove = null;
            ArrayList<Path> pathsToCheck = greedySelect(network);
            for (Path p : pathsToCheck) {
                int flow = p.getWeight();
                int edgeCount = 0;
                for (Edge e : p.getEdges()) {
                    if (e.getWeight() <= flow) {
                        edgeCount++;
                    }
                }

               // System.out.println(edgeCount);

                if (edgeCount > maxEdgeCount) {
                    maxEdgeCount = edgeCount;
                    pathToRemove = p;
                }

            }

            if(pathToRemove == null) {
                return null;
            }

            //Convert path edges to network edges
            ArrayList<Edge> newEdges = new ArrayList<>();
            for(Edge e: pathToRemove.getEdges()) {
                Edge newEdge = network.findEdgeById(e.getId());
                newEdges.add(newEdge);
            }

            pathToRemove = new Path(newEdges);

            //System.out.println(pathToRemove);
            network.reducePath(pathToRemove);
            //System.out.println(network.numEdges());
            paths.add(pathToRemove);
        }

        return paths;
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
