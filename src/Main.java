import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {

        //config variables:
        String[] animals = {"human", "mouse", "salmon", "zebrafish"};   //only used in multiple read mode
        String directory = "/home/dan/dev/instances/simulation";            //only used in multiple read mode
        String file = "/home/dan/dev/instances/rnaseq/human/test.testgraph";   //only used in single read mode
        String truthFile = "/home/dan/dev/instances/simulation/param1/1000.10.50.truth"; //only used in single read mode
        //String directory = "/home/peter/Desktop/instances/rnaseq";
        //String file = "/home/peter/Desktop/instances/rnaseq/test/1.graph";         //either single or multiple
        String importMode = "single";                                   //either single or multiple



        ArrayList<Network> networks;
        if(importMode.equals("single")) {
            PrintWriter out = null;
            int[] resultBins = new int[10];
            int[] totals = new int[10];
            for(int i = 0; i < 10; i++) resultBins[i] = 0;
            for(int i = 0; i < 10; i++) totals[i] = 0;

            try {
                out = new PrintWriter(new File("outputFile.txt"));
                networks = readGraphFile(file);
                System.out.println(networks.toString());
                ArrayList<Integer> numTruthPaths = readTruthFile(truthFile);

                for(int num: numTruthPaths) {
                    if(num > 10) continue;
                    totals[num-1]++;
                }

                int count = 0;
                for(Network network: networks) {
                    out.println("Graph # " + count);
                    ArrayList<Path> paths = new ArrayList<>();
                    network.collapseEdges();

                    for(int k = network.getMinEdge(); k < network.getMaxEdge(); k++) {
                        paths.add(findMaxPath(network, k));
                    }

                    /*
                    int truthPaths = numTruthPaths.get(count);
                    out.println("# Truth Paths = " + truthPaths + "\t # Actual Paths = " + numPaths);
                    if(numPaths <= truthPaths) {
                        resultBins[truthPaths-1]++;
                    }
                    out.println();
                    count++;
                    */
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file.");
                e.printStackTrace();
            } finally {
                out.close();
            }

            System.out.printf("# Paths\tSuccess Rate\n");
            for(int i = 0; i < 10; i++) {
                double successRate = ((double)resultBins[i] / totals[i]) * 100;
                System.out.printf("%d\t\t%.2f\n", i+1, successRate);
            }
        }

        if(importMode.equals("multiple")) {
            PrintWriter out = null;
            int[] resultBins = new int[10];
            int[] totals = new int[10];
            int numSuccess = 0;
            int numTotal = 0;

            try {
                out = new PrintWriter(new File("outputFile.txt"));

                File dir = new File(directory+"/param1");
                File[] files = dir.listFiles();
                for(int i = 0; i < 10; i++) resultBins[i] = 0;
                for(int i = 0; i < 10; i++) totals[i] = 0;

                for (File curFile : files) {

                    int pos = curFile.getName().lastIndexOf(".");
                    String ext = curFile.getName().substring(pos+1);
                    String filenameNoExt = curFile.getName().substring(0, pos);
                    String filename = curFile.getName();
                    //System.out.println(ext);
                    if(ext.equals("graph")) {
                        networks = readGraphFile(directory+"/param1/"+filename);
                        ArrayList<Integer> numTruthPaths = readTruthFile(directory+"/param1/"+filenameNoExt+".truth");

                        for(int num: numTruthPaths) {
                            if(num > 10) continue;
                            totals[num-1]++;
                            numTotal++;
                        }

                        System.out.println(Arrays.toString(totals));
                        int count = 0;
                        for(Network network: networks) {
                            out.println("Graph # " + count);
                            ArrayList<Path> paths = new ArrayList<>();
                            network.collapseEdges();
                            //network.printDetails(out);
                            int numPaths = findPaths(network, paths, out);
                            int truthPaths = numTruthPaths.get(count);
                            out.println("# Truth Paths = " + truthPaths + "\t # Actual Paths = " + numPaths);
                            if(numPaths <= truthPaths) {
                                if(truthPaths > 10) continue;
                                resultBins[truthPaths-1]++;
                                numSuccess++;
                            }
                            out.println();
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

            System.out.printf("# Paths\tSuccess Rate\n");
            for(int i = 0; i < 10; i++) {
                double successRate = ((double)resultBins[i] / totals[i]) * 100;
                System.out.printf("%d\t\t%.2f\n", i+1, successRate);
            }

            System.out.println();
            double successRate = ((double) numSuccess / numTotal) * 100;
            System.out.printf("Total Success Rate = %.2f", successRate);
        }
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

    public static int findPaths(Network network, ArrayList<Path> paths, PrintWriter out) {

        ArrayList<Integer> sortedNodes = network.topoSort();
        while(network.numEdges() > 0) {
            int[] weights = new int[network.numNodes()];
            Edge[] selectedEdges = new Edge[network.numNodes()];

            for (int i = 0; i < weights.length; i++) weights[i] = -1;
            for (int i = 0; i < selectedEdges.length; i++) selectedEdges[i] = null;
            weights[0] = 0;

            for (int nodeId : sortedNodes) {
                Node n = network.getNode(nodeId);
                for (Edge e : n.getOutgoingEdges()) {
                    int outgoingId = e.getToNode().getId();
                    int weight = e.getWeight();
                    if (weight + weights[nodeId] > weights[outgoingId]) {
                        weights[outgoingId] = weight + weights[nodeId];
                        selectedEdges[outgoingId] = e;
                    }
                }
            }

            ArrayList<Edge> selectedEdges2 = new ArrayList<>();
            int nodeId = network.numNodes()-1;
            Edge e = selectedEdges[nodeId];
            while(nodeId >= 1) {
                if(e == null) break;
                selectedEdges2.add(e);
                e = selectedEdges[e.getFromNode().getId()];
            }

            Path path = new Path(selectedEdges2);
            paths.add(path);
            network.reducePath(path);
        }

        return paths.size();
    }

    /**
     * Finds the path of maximum length with a flow of k
     * @param k
     * @return length of the path (# of nodes, not weight)
     */
    public static Path findMaxPath(Network network, int k) {
        ArrayList<Integer> sortedNodes = network.topoSort();
        int[] lengths = new int[network.numNodes()];
        Edge[] selectedEdges = new Edge[network.numNodes()];
        for(int i = 0; i < lengths.length; i++) lengths[i] = -1;
        for(int i = 0; i < selectedEdges.length; i++) selectedEdges[i] = null;
        lengths[0] = 0;

        for(int nodeId: sortedNodes) {
            Node node = network.getNode(nodeId);

            for(Edge e: node.getOutgoingEdges()) {
                int weight = e.getWeight();
                int newLength = 1 + lengths[nodeId];
                int toNodeId = e.getToNode().getId();
                if(weight >= k && newLength >= lengths[toNodeId]) {
                    //take larger weight as tie-breaker
                    if(newLength == lengths[toNodeId] && weight <= selectedEdges[toNodeId].getWeight()) continue;
                    lengths[toNodeId] = newLength;
                    selectedEdges[toNodeId] = e;
                }
            }
        }

        //System.out.printf("MAX PATH (Flow %d) = " + Arrays.toString(lengths)+"\n" + Arrays.toString(selectedEdges)+"\n", k);

        Path path = new Path(selectedEdges, k);

        return path;
    }
}
