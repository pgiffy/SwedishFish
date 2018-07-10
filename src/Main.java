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
            String animal = "mouse";
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
                        int numPaths;
                        for(Network network: networks) {
                            numPaths = 0;
                            network.collapseEdges();
                            //network.printDOT("graph.dot");
                            ArrayList<Integer> sortedNodes = network.topoSort();
                            Network copy = new Network(network);
                            Network copy2 = new Network(network);
                            Network origNetwork = new Network(network);
                            ArrayList<Path> paths = new ArrayList<>();


                            // Max Frequencies
                            /*
                            if(network.numNodes() < 30) {
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
                                paths.add(maxPath);
                                numPaths++;
                            }
                            /**/

                            // Remove from beginning
                            /**/
                            ArrayList<Integer> valK = network.ValsToEnd();
                            Collections.sort(valK);
                            Collections.reverse(valK);

                            for (int k : valK) {
                                Path newPath = findMaxPath(network, k, sortedNodes, out);
                                if (newPath == null) {
                                    network = copy;
                                    valK = network.ValsFromZero();
                                    Collections.sort(valK);
                                    Collections.reverse(valK);
                                    numPaths = 0;
                                    break;
                                }
                                paths.add(newPath);
                                network.reducePath(newPath);
                                numPaths++;
                            }
                            /**/

                            //remove from end
                            /**/
                            for(int k: valK){
                                Path newPath = findMaxPath(network, k, sortedNodes, out);
                                if(newPath == null){
                                    network = copy2;
                                    numPaths = 0;
                                    break;
                                }

                                paths.add(newPath);
                                network.reducePath(newPath);
                                numPaths++;
                            }
                            /**/

                            //greedy-width
                            while(network.numEdges() > 0) {
                                Path selectedPath = findFattestPath(network);
                                network.reducePath(selectedPath);
                                paths.add(selectedPath);
                                numPaths++;
                            }

                            //print results
                            int truthPaths = numTruthPaths.get(count);
                            out.println("# Truth Paths = " + truthPaths + "\t # Actual Paths = " + numPaths);
                            if(truthPaths != numPaths) {
                                //origNetwork.printDOT("wrongGraphs/"+filenameNoExt+"_"+count+"_"+truthPaths+".dot", paths);
                            }
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
    public static Path findMaxPath(Network network, int k, ArrayList<Integer> sortedNodes, PrintWriter out) {

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
